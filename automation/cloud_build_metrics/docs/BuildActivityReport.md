# BuildActivityReport.md

## Objective

Print all job activity. Enables easy identification of orphaned jobs.

```
Bitrise app count	58
Canvas LMS Docker	4 minutes ago
data seeding api	12 minutes ago
Cloud Build Metrics	12 minutes ago
Android Teacher UI Tests	12 minutes ago
android-polling	12 minutes ago
Android Parent	12 minutes ago
Android Teacher	12 minutes ago
Android Student	12 minutes ago
Android Student UI Tests	2 hours ago
Android Parent UI Tests	2 hours ago
Android Open Source Parent	2 hours ago
Android Open Source Student	2 hours ago
Android Open Source Teacher	2 hours ago
Canvas iOS Translations	3 hours ago
Canvas iOS Lint New Student	14 hours ago
Canvas iOS Student UI Tests	14 hours ago
iOS Open Source Parent	14 hours ago
iOS Open Source Student	14 hours ago
iOS Open Source Teacher	14 hours ago
Canvas iOS Teacher	14 hours ago
Canvas iOS Parent	14 hours ago
Canvas iOS Student	14 hours ago
Canvas iOS Danger	14 hours ago
mobile-shared Espresso	1 months ago
Android Pact	4 months ago
SoSeedy Kubernetes	5 months ago
earlgrey-2-binary-example	5 months ago
mobile android	8 months ago
expo-ui-test	9 months ago
Test Job for Cloud Build Metrics	12 months ago
android-teacher-robo	12 months ago
android-parent-robo	12 months ago
Create React App TypeScript	17 months ago


Process finished with exit code 0
```

## Solution

The build activity report allows us to easily identify jobs that are abandoned based on the last build time.
It’s great being able to see exactly what jobs are active and inactive. Pruning inactive jobs was a bit painful 
before this report existed.

```kotlin
override fun execute() {
    jobActivityReport(BitriseApps)
}
```

See [BuildActivityReport.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/BuildActivityReport.kt
