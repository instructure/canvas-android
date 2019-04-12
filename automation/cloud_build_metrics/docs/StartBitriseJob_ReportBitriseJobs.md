# StartBitriseJob / ReportBitriseJob

## Objective

Batch create jobs and report on their status. Primarily used to measure build health.

```
Warning! You are about to trigger 1 builds on Bitrise.
Doing this during business hours may consume all available resources in CI and may prevent any other builds from running.

Are you sure you want to proceed? Please type 'Yes' to confirm:
Yes
Build triggered: https://app.bitrise.io/build/da85df2045c553a0

Process finished with exit code 0
```

```
$ cat bitriseJobs.txt
da85df2045c553a0
```

```
https://www.bitrise.io/build/da85df2045c553a0 - error

Total builds: 1
Bitrise build status
error: 1

Log failures
Success: 0
Failure: 1

Process finished with exit code 0
```

## Solution

Start/Report bitrise job allows us to create batches of jobs and then check the results asynchronously later.
This is useful for stability testing new workflows. We can start a new batch of 100x jobs, and then check 
to make sure they all completed successfully.

```kotlin
// StartBitriseJob
override fun execute() {
    warnUser()

    val appSlug = "693f666c209a029b"
    val buildRequest = BitriseTriggerBuildRequest(
            build_params = BuildParams(workflow_id = "primary", environments = emptyList())
    )

    repeat(JOBS_TO_TRIGGER) {
        val build = BitriseApps.triggerBuild(appSlug, buildRequest)
        BUILD_IDS.add(build.build_slug)
        println("Build triggered: ${build.build_url}")
    }

    writeFile()
}
```

See [StartBitriseJob.kt][1] and [ReportBitriseJob.kt][2] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/StartBitriseJob.kt
[2]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/ReportBitriseJob.kt
