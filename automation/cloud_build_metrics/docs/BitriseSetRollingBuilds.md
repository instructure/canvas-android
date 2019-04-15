# BitriseSetRollingBuilds

## Objective

Automatically set rolling builds on all apps.

```
tasks.BitriseSetRollingBuilds is running...

Updating build config for: earlgrey-2-binary-example
Updating build config for: gwiz-bot
Updated 2 of 50 apps

Process finished with exit code 0
```

## Solution

The Bitrise private API allows us to set rolling builds for every app. This represents a great time savings for our team,
 as previously this was done manually.

```kotlin
override fun execute() {
    signIn()
    val apps = BitriseApps.getOnlyInstructureApps()

    var updatedCount = 0
    for (app in apps) {
        val appSlug = app.slug

        val config = try {
            RollingBuilds.getConfig(appSlug)
        } catch (e: Exception) {
            RollingBuilds.enable(appSlug)
            RollingBuilds.getConfig(appSlug)
        }

        if (config.pr && config.push && config.running) continue

        println("Updating build config for: ${app.title}")
        updatedCount += 1

        RollingBuilds.enable(appSlug)
        RollingBuilds.setConfigPR(appSlug, true)
        RollingBuilds.setConfigPush(appSlug, true)
        RollingBuilds.setConfigRunning(appSlug, true)
    }

    println("Updated $updatedCount of ${apps.size} apps")
}
```

See [BitriseSetRollingBuilds.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/BitriseSetRollingBuilds.kt
