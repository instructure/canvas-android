import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

import java.util.concurrent.TimeUnit

// A listener that will allow us to monitor task timings and, ultimately, APK size
class TimingsListener implements TaskExecutionListener, BuildListener {
    private long startTime
    private timings = [:]
    private Project refProject
    private long buildStartTime

    TimingsListener(Project _refProject) {
        refProject = _refProject
        buildStartTime = System.nanoTime()
    }


    @Override
    void beforeExecute(Task task) {
        startTime = System.nanoTime()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
        timings.put(task.name, ms)
    }

    @Override
    void buildFinished(BuildResult result) {

        // Compute build time
        def totalBuildTimeMs = TimeUnit.MILLISECONDS.convert(System.nanoTime() - buildStartTime, TimeUnit.NANOSECONDS)

        // Grab the Splunk-mobile token from Bitrise
        def splunkToken = System.getenv("SPLUNK_MOBILE_TOKEN")

        // Let's abort early if (1) the build failed, or (2) we're not on bitrise
        if(result.failure != null) {
            println("Build report logic aborting due to failed build")
            return
        }

        if(splunkToken == null || splunkToken.isEmpty()) {
            println("Build report logic aborting because we're not on bitrise")
            return
        }

        // Grab the gradle tasks passed on the command line for the job
        def startTaskNames = result.gradle.startParameter.taskNames.join(",")

        // Sort the timings in descending time order, compute our top 10
        timings = timings.sort { -it.value }
        def top10 = timings.take(10).entrySet()

        // Figure out our build type
        def buildType = "debug"
        if(startTaskNames.contains("Release")) {
            buildType = "release"
        }

        // Figure out our build flavor
        def buildFlavor = "qa"
        if(startTaskNames.contains("Dev")) {
            buildFlavor = "dev"
        }
        else if(startTaskNames.contains("Prod")) {
            buildFlavor = "prod"
        }

        // Grab some data from the environment
        def bitriseWorkflow = System.getenv("BITRISE_TRIGGERED_WORKFLOW_ID")
        def bitriseApp = System.getenv("BITRISE_APP_TITLE")
        def bitriseBranch = System.getenv("BITRISE_GIT_BRANCH")
        def bitriseBuildNumber = System.getenv("BITRISE_BUILD_NUMBER")

        // Determine our project name.
        // It's not as simple as looking at refProject.name; since we add this listener via the
        // student project, refProject.name will always be "student".  Glean our actual project name
        // via the bitrise app name.
        def projectName = ""
        if(bitriseApp.contains("Student")) {
            projectName = "student"
        }
        else if(bitriseApp.contains("Teacher")) {
            projectName = "teacher"
        }
        else if(bitriseApp.toLowerCase().contains("parent")) {
            projectName = "parent"
        }
        else {
            projectName = "unknown" // Punt
        }
        println("projectName = $projectName")

        // Locate the apk
        def file = null
        def fileSizeInMB = 0.0
        if(projectName!="parent") {
            // We don't necessarily want refProject.buildDir, since it will always be the student buildDir
            def buildDir = refProject.buildDir.toString().replace("student",projectName)
            file = new File("$buildDir/outputs/apk/$buildFlavor/$buildType/$projectName-$buildFlavor-${buildType}.apk")
            fileSizeInMB = file.length() == 0 ? 0 : (file.length() / (1024.0 * 1024.0)).round(3)
        }
        else {
            // Different location logic for flutter parent apk file
            def buildDir = refProject.buildDir.toString()
            file = new File("$buildDir/outputs/apk/$buildType/app-${buildType}.apk")
            fileSizeInMB = file.length() == 0 ? 0 : (file.length() / (1024.0 * 1024.0))
            fileSizeInMB = (fileSizeInMB * 1000.0).toInteger() / 1000.0 // Round to three decimal places
        }
        println("file name=${file.path} length=${file.length()}")


        // Construct the JSON payload for our "buildComplete" event
        def payloadBuilder = new groovy.json.JsonBuilder()
        payloadBuilder buildTime: totalBuildTimeMs,
            gradleTasks: startTaskNames,
            apkFilePath: file.path,
            apkSize: fileSizeInMB,
            bitriseWorkflow: bitriseWorkflow,
            bitriseApp: bitriseApp,
            bitriseBranch: bitriseBranch,
            bitriseBuildNumber: bitriseBuildNumber,
            topTasks: top10

        // Create the event payload.  Change key/value in top 10 tasks to task/ms.
        def payload = payloadBuilder.toString().replaceAll("\"key\"", "\"task\"").replaceAll("\"value\"", "\"ms\"")

        println("event payload: $payload")

        // Let's issue our curl command to emit our data
        refProject.exec {
            executable "curl"
            args "-k", "https://http-inputs-inst.splunkcloud.com:443/services/collector", "-H", "Authorization: Splunk $splunkToken",
                    "-d", "{\"sourcetype\" : \"mobile-android-build\", \"event\" : $payload}"
        }

    }

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}
}

