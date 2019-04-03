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

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import api.bitrise.BitriseTriggerBuildRequest
import api.bitrise.BuildParams
import api.bitrise.private.BuildCache
import api.bitrise.yaml.BitriseYaml
import api.bitrise.yaml.YamlParser
import com.fasterxml.jackson.databind.JsonNode
import support.BitriseApp
import util.humanReadable

/** Bitrise cache doesn't expire naturally. Purge & refresh for optimal performance.  **/
object BitriseCacheRefresh : BitriseTask {

    private val appsToRefresh = listOf(
            BitriseApp.Android.teacher,
            BitriseApp.Android.parent,
            BitriseApp.Android.student,
            BitriseApp.Android.polling,
            BitriseApp.Automation.dataSeedingApi,
            BitriseApp.Android.Automation.teacherEspresso,
            BitriseApp.Automation.cloudBuildMetrics
    )

    private fun deleteCaches() {
        for (app in appsToRefresh) {
            val cache = BuildCache.get(app.slug)
            if (cache.isNotEmpty()) {
                println("${app.title} build cache deleted")
                for (item in cache) {
                    val size = item.file_size_bytes.humanReadable()
                    val branch = item.the_cache_item_key
                    println("  $size - $branch")
                }
                BuildCache.delete(app.slug)
            }
        }
    }

    private fun triggerDefaultWorkflow(app: BitriseAppObject) {
        // Alternatively write YAML to disk and use the CLI to resolve the trigger map.
        // bitrise trigger-check --config /tmp/bitrise.yml --pr-source-branch develop --pr-target-branch master
        val yml = BitriseYaml.getYaml(app.slug)
        val triggerMap = YamlParser.parse(yml).findValue("trigger_map")

        var triggered = false
        for (map in triggerMap) {
            val sourceBranch: JsonNode? = map.findValue("pull_request_source_branch")

            if (sourceBranch != null && sourceBranch.asText() == "*") {
                val workflow = map.findValue("workflow").asText()
                println("Triggering build for ${app.title} using workflow $workflow")

                val buildParams = BuildParams(workflow_id = workflow)
                val build = BitriseApps.triggerBuild(app.slug, BitriseTriggerBuildRequest(build_params = buildParams))
                println("  ${build.build_url}")
                triggered = true
                break
            }
        }

        if (!triggered) throw RuntimeException("Failed to trigger job for ${app.title}")
    }

    private fun populateCaches() {
        // Start new jobs for each app that runs on PRs to populate the cache.
        // This will ensure that future builds are fully cached.
        appsToRefresh.forEach { app -> triggerDefaultWorkflow(app) }
    }

    override fun execute() {
        signIn()
        deleteCaches()
        populateCaches()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        this.execute()
    }
}
