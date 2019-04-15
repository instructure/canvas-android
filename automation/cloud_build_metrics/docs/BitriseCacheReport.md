# BitriseCacheReport

## Objective

Print the cache size for every app. Monitoring cache size helps ensure builds stay fast.

```
tasks.BitriseCacheReport is running...

Android Student UI Tests
  1 GB - master
Android Parent UI Tests
  945 MB - master
data seeding api
  694 MB - master
Canvas iOS Danger
  3 GB - master
Cloud Build Metrics
  709 MB - master
Android Teacher UI Tests
  1 GB - master
android-polling
  782 MB - master
Android Parent
  872 MB - master
Android Teacher
  1 GB - master
Android Student
  1 GB - master

Process finished with exit code 0
```

## Solution

The Bitrise private API enables generating an organization wide cache report for each job. This has been helpful in 
making sure jobs are cached appropriately and ensuring we’re staying under the 2GB limit.

```kotlin
override fun execute() {
    signIn()
    val apps = BitriseApps.getOnlyInstructureApps()

    for (app in apps) {
        val cache = BuildCache.get(app.slug)
        if (cache.isNotEmpty()) {
            println(app.title)
            for (item in cache) {
                val size = item.file_size_bytes.humanReadable()
                val branch = item.the_cache_item_key
                println("  $size - $branch")
            }
        }
    }
}
```

See [BitriseCacheReport.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/BitriseCacheReport.kt
