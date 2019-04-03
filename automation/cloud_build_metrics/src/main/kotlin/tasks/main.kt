//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package tasks

object Main {

    private fun name(klass: Any): String {
        return klass::class.java.simpleName
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val tasks = setOf(
                BackupBitriseYamls,
                BitriseCacheRefresh,
                BitriseCacheReport,
                BitriseSetRollingBuilds,
                BuildActivityReport,
                BuildErrorReport,
                DataStudioUpdate,
                LintBitriseYamls,
                ReportBitriseJob,
                SprintReport,
                StartBitriseJob,
                UpdateBitriseYamls,
                UploadApks)
        val taskNames = tasks.joinToString(", ") { name(it) }
        if (args.isEmpty()) throw RuntimeException("Must provide task name: $taskNames")
        val targetTask = args.first()
        var executed = false

        for (task in tasks) {
            if (targetTask == name(task)) {
                println("Running task: $targetTask")
                task.execute()
                executed = true
                break
            }
        }

        if (!executed) {
            throw RuntimeException("No task matching '$targetTask'. Valid tasks: $taskNames")
        }
    }
}
