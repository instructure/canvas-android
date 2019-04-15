# Cloud Build Metrics

The cloud build metrics project uses the Bitrise API to gather data related to build health. Here are a few tasks that have been automated:

Task | Description
---  | ---
[BackupBitriseYamls](./BackupBitriseYamls.md) | Nightly backup of all Bitrise workflows to an encrypted git repo.
[BitriseCacheRefresh](./BitriseCacheRefresh.md)| Nightly refresh of the build cache.
[BitriseCacheReport](./BitriseCacheReport.md)| List build cache size for all apps.
[BitriseSetRollingBuilds](./BitriseSetRollingBuilds.md)| Enforce rolling build settings across all apps in an org.
[BuildActivityReport](./BuildActivityReport.md)| Print build activity from most recent to least.
[BuildErrorReport](./BuildErrorReport.md)| Parse build results and identify failed steps.
[LintBitriseYamls](./LintBitriseYamls.md)| Lint bitrise yamls for pinned steps.
[StartBitriseJob / ReportBitriseJob](./StartBitriseJob_ReportBitriseJobs.md)| Start a batch of jobs and then asynchronously view the status
[SprintReport](./SprintReport.md) | Update Google Sheet build health dashboard.
[UpdateBitriseYamls](./UpdateBitriseYamls.md) | Rewrites all Bitrise YAMLs to use master version of steps
