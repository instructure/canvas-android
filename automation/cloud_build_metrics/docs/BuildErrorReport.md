# BuildErrorReport

## Objective

Identify which step builds are failing on.

```
Found 58 apps in org
Building error report for Canvas iOS Student UI Tests
Monday, January 7 - Sunday, January 5
"Canvas iOS Student UI Tests" has 175 failed builds out of 265 builds
Downloading: 829237e811f23cf7 to: /var/folders/14/h2zk5sx1315b__blzdt7hz1m4k0by8/T/57b22415f35222e3
Downloading: 79eaa06117b38494 to: /var/folders/14/h2zk5sx1315b__blzdt7hz1m4k0by8/T/57b22415f35222e3
...
Parsing errors from 175 logs
https://www.bitrise.io/build/829237e811f23cf7, Build Student app (exit code: 65)                            
https://www.bitrise.io/build/79eaa06117b38494, Build Student app (exit code: 65)                            
https://www.bitrise.io/build/7eb299b86be1f66f, Build Student app (exit code: 65)                            
https://www.bitrise.io/build/e1fe31d9a2ea410b, Build Student app (exit code: 65)                            
https://www.bitrise.io/build/c48267e4bde4b399, flank (exit code: 1)                                         
https://www.bitrise.io/build/9542b9372c434da1, flank (exit code: 1)                                         
https://www.bitrise.io/build/b5da9e738dd073b1, flank (exit code: 1)                                         
https://www.bitrise.io/build/fdf9db2a3a4e7f46, flank (exit code: 1)                                         
https://www.bitrise.io/build/fdf9db2a3a4e7f46, deploy-to-bitrise-io (exit code: 1)                          
https://www.bitrise.io/build/56cede26cfd592e8, flank (exit code: 1)                                         
https://www.bitrise.io/build/7dd2a1abe783a2e8, Build Student app (exit code: 65)                                                                 
https://www.bitrise.io/build/8ee15b609d9f3d93, flank (exit code: 2)                                         
https://www.bitrise.io/build/c521fb889e4e0ddb, flank (exit code: 2)                                         
https://www.bitrise.io/build/1f6f0caeefbe4b88, flank (exit code: 2)                                         
https://www.bitrise.io/build/d2331dc187eda13f, flank (exit code: 1)                                         
https://www.bitrise.io/build/eab7e551e9dd9f67, Build Student app (exit code: 65)                            
https://www.bitrise.io/build/31e0301c85282bbf, Build Student app (exit code: 65)                            
                                

Process finished with exit code 0
```

## Solution

The Bitrise API allows us to download and parse the build output for each failed job. In addition to pinpointing exactly
 which steps failed, we’re also able to get additional details on the error message via a regular expression. 
 Automating error reporting has saved us a significant amount of time when diagnosing infrastructure failures 
 identified by the build graphs. Previously, an engineer would have to manually click through each build and
 figure out what step failed.

```kotlin
override fun execute() {
    val targetAppSlug = "57b22415f35222e3"
    val appsInOrg = BitriseApps.getAppsForOrg()

    println("Found ${appsInOrg.size} apps in org")
    val app = appsInOrg.first { it.slug == targetAppSlug }

    println("Building error report for ${app.title}")
    val weekStart = LocalDate.parse("Monday, January 7 2019", prettyDateTimeYear)
    val range = weekRange(52, weekStart)

    println("${range.after.pretty()} - ${range.before.pretty()}")
    buildErrorReport(app,
            deleteCache = false,
            limitAfter = range.limitAfter,
            limitBefore = range.limitBefore)
}
```

See [BuildErrorReport.kt][1] for the full source.

[1]: https://github.com/instructure/canvas-android/blob/f455db88520d37be007af2f7b9e36d17e45182f5/automation/cloud_build_metrics/src/main/kotlin/tasks/BuildErrorReport.kt
