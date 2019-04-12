# UpdateBitriseYamls

## Objective

Automatically fix lint issues discovered by `LintBitriseYamls`

```
Warning! You are about to update the bitrise.yml file for every Instructure job running on Bitrise.
You must ensure that the existing yaml files have been properly backed up.

Are you sure you want to proceed? Please type 'Yes' to confirm:
Yes

Updating: canvas iOS UI Tests https://www.bitrise.io/app/21c95f472d50f580# ...  ✅
Updating: canvas-android https://www.bitrise.io/app/c7219e86fcecb15f# ...  ✅
Updating: canvas-ios unit tests https://www.bitrise.io/app/9b66b2e82404d2f7# ...  ✅
Updating: canvas-ios https://www.bitrise.io/app/94c7e4f1b42e88c7# ...  ✅

Process finished with exit code 0

```

## Solution

`UpdateBitriseYamls` provides an automated way of fixing lint issues found with `LintBitriseYamls`.
Currently the update step supports rewriting workflows to use the latest versions of steps.
Using the latest steps helps us collaborate better with the Bitrise team as the platform evolves with new features and bug fixes.
We can rewrite all the workflows with confidence because the YAMLs are backed up by `BackupBitriseYamls`.

```kotlin
override fun execute() {
    warnUser()

    for (app in BitriseApps.getOnlyInstructureApps()) {
        val yaml = getYaml(app.slug)
        val warnings = stepVersionWarnings(app, yaml)
        if (warnings.isEmpty()) {
            continue
        }

        print("Updating: ${app.title} https://www.bitrise.io/app/${app.slug}# ... ")
        val newYaml = fixStepVersions(app, yaml, warnings)
        postYaml(app.slug, newYaml)
        println(" ✅")
    }
}
```

See [UpdateBitriseYamls.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/UpdateBitriseYamls.kt
