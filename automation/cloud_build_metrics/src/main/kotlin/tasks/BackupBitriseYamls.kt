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
import api.bitrise.yaml.BitriseYaml.getYaml
import java.io.File

object BackupBitriseYamls : Task {

    internal var keybasePath = "../../../secrets"

    internal fun keybaseRepo(): File {
        val path = File(keybasePath)
        if (!path.exists()) {
            throw RuntimeException("Secrets directory not found!")
        }
        return path
    }

    internal fun writeYamls(repo: File, apps: List<BitriseAppObject>) {
        apps.forEach { app ->
            val appSlug = app.slug
            val yaml = getYaml(appSlug)
            repo.resolve("bitrise/$appSlug.yml").writeText(yaml)
        }
    }

    internal fun appsMap(apps: List<BitriseAppObject>): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        apps.forEach { app ->
            map[app.slug] = app.title
        }
        return map
    }

    internal fun updateReadme(repo: File, appsMap: MutableMap<String, String>) {
        val ok = "✅"
        val missing = "⛔️"
        val readme = repo.resolve("README.md")

        // only update the Slug to Title table in the readme
        val newReadme = StringBuilder()
        var tableFound = false
        val currentReadme = readme.readText()
        for (line in currentReadme.lines()) {
            if (line.startsWith("## Slug to Title")) {
                tableFound = true
            }

            if (!tableFound) {
                newReadme.appendln(line)
                continue
            }

            if (line.startsWith("#") || line.startsWith("Slug") || line.startsWith("---") || line.isEmpty()) {
                newReadme.appendln(line)
                continue
            }

            // split to get slug, title, status
            val tokens = line.split("|")
            if (tokens.size != 3) {
                throw RuntimeException("README.md table has ${tokens.size} number of columns! Expected only 3 columns!")
            }

            // extract just the slug from the table row: [3875692b192c4eb3](./bitrise/3875692b192c4eb3)
            var oldSlug = tokens[0]
            oldSlug = oldSlug.substring(oldSlug.indexOf("[") + 1)
            oldSlug = oldSlug.substring(0, oldSlug.indexOf("]"))

            val oldTitle = tokens[1]
            val link = "[$oldSlug](./bitrise/$oldSlug.yml)"
            val appTitle = appsMap.remove(oldSlug)
            if (appTitle == null) {
                // if appTitle is null then it must have been deleted!
                newReadme.appendln("$link|$oldTitle|$missing")
            } else {
                // always use the latest job title
                newReadme.appendln("$link|$appTitle|$ok")
            }
        }

        // append bitrise jobs that were created since last time
        appsMap.forEach { slug, title ->
            newReadme.appendln("[$slug](./bitrise/$slug.yml)|$title|$ok")
        }

        readme.writeText(newReadme.trim().toString())
    }

    override fun execute() {
        val apps = BitriseApps.getAppsForOrg()
        val appsMap = appsMap(apps)
        val repo = keybaseRepo()
        writeYamls(repo, apps)
        updateReadme(repo, appsMap)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        execute()
    }
}
