# LintBitriseYamls

## Objective

Define global lint rules to identify problems in workflows.

```
Linting 50 apps in org
Exception in thread "main" java.lang.RuntimeException: 💥 Bitrise steps with pinned versions found!

canvas iOS UI Tests (21c95f472d50f580)
  brew-install@0.9.0
canvas-android (c7219e86fcecb15f)
  gradle-runner@1.8.4
  sonarqube-scanner@1.0.5
canvas-ios unit tests (9b66b2e82404d2f7)
  brew-install@0.9.0
  codecov@1.1.5
canvas-ios (94c7e4f1b42e88c7)
  brew-install@0.9.0
  slack@3.1.2

	at tasks.LintBitriseYamls.checkWarnings$cloud_build_metrics_main(LintBitriseYamls.kt:38)
	at tasks.LintBitriseYamls.execute(LintBitriseYamls.kt:53)
	at tasks.LintBitriseYamls.main(LintBitriseYamls.kt:58)

```

## Solution

The Bitrise API allows us to fetch all YAML files and assert they’re following best practices. 
By default, Bitrise uses old pinned versions of steps (for example git clone).
As new improvements and fixes are made, none of the workflows benefit from the new versions. 
The linter proactively identifies workflows using pinned steps and sends a Slack message so we can fix the problem.
In the future, we may expand the linter to find other areas for improvement.

```kotlin
override fun execute() {
    val warnings = mutableMapOf<String, List<String>>()
    val appsInOrg = BitriseApps.getOnlyInstructureApps()
    println("Linting ${appsInOrg.size} apps in org")
    appsInOrg.forEach { app ->
        val yaml = BitriseYaml.getYaml(app.slug)
        warnings.putAll(stepVersionWarnings(app, yaml))
    }

    checkWarnings(warnings)
}
```

See [LintBitriseYamls.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/LintBitriseYamls.kt
