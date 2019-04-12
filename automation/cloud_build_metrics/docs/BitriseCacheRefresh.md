# BitriseCacheRefresh

## Objective

Automatically ensure the Bitrise cache is fresh.

```
App title renamed android-teacher -> Android Teacher. Verify and update.
App title renamed android-parent -> Android Parent. Verify and update.
App title renamed android-student -> Android Student. Verify and update.
App title renamed Android Teacher Espresso -> Android Teacher UI Tests. Verify and update.
App title renamed cloud_build_metrics -> Cloud Build Metrics. Verify and update.

tasks.BitriseCacheRefresh is running...

Android Teacher build cache deleted
  1 GB - master
Android Parent build cache deleted
  872 MB - master
Android Student build cache deleted
  1 GB - master
android-polling build cache deleted
  782 MB - master
data seeding api build cache deleted
  694 MB - master
Android Teacher UI Tests build cache deleted
  1 GB - master
Cloud Build Metrics build cache deleted
  709 MB - master
Triggering build for Android Teacher using workflow debug
  https://app.bitrise.io/build/48d1dc6727b9ac5b
Triggering build for Android Parent using workflow debug
  https://app.bitrise.io/build/d5ae8a3d0c29d745
Triggering build for Android Student using workflow debug
  https://app.bitrise.io/build/95b5256a92888729
Triggering build for android-polling using workflow debug
  https://app.bitrise.io/build/217aca865c4872ac
Triggering build for data seeding api using workflow primary
  https://app.bitrise.io/build/989f8637c8cf06da
Triggering build for Android Teacher UI Tests using workflow primary
  https://app.bitrise.io/build/3a4dbc026d5667f1
Triggering build for Cloud Build Metrics using workflow RunUnitTests
  https://app.bitrise.io/build/85574c05121ad050

Process finished with exit code 0
```

## Solution

Using the Bitrise private API, we’re able to programmatically delete caches for all jobs that run on pull requests.
To populate the cache, we need to figure out the default workflow for each app. There’s no API support for this, 
so the YAML file is downloaded and parsed directly. After determining the workflow, we use Bitrise’s new public API 
for scheduling a build. The cache refresh task runs nightly. Each business day, we’re starting with a freshly optimized 
cache. This has been a great win for optimizing build times.

```kotlin
override fun execute() {
    signIn()
    deleteCaches()
    populateCaches()
}
```

See [BitriseCacheRefresh.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/BitriseCacheRefresh.kt

There's an open feature request to [Improve cache reliability](https://discuss.bitrise.io/t/rethink-the-cache-system-to-be-more-reliable/3290).

