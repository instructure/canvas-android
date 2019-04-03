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
import api.bitrise.yaml.BitriseYaml.postYaml
import api.bitrise.yaml.YamlParser.stepVersionWarnings
import util.getOnlyInstructureApps

object UpdateBitriseYamls : Task {

    private fun warnUser() {
        val runningOnBitrise = System.getenv("BITRISE_IO") != null
        if (runningOnBitrise) return

        val warningMessage = """
            Warning! You are about to update the bitrise.yml file for every Instructure job running on Bitrise.
            You must ensure that the existing yaml files have been properly backed up.

            Are you sure you want to proceed? Please type 'Yes' to confirm:
        """.trimIndent()

        println(warningMessage)
        if (!readLine().equals("Yes", ignoreCase = true)) {
            println("Thanks for doing the right thing!")
            System.exit(1)
        }
        println()
    }

    internal fun fixStepVersions(app: BitriseAppObject, yaml: String, warnings: Map<String, List<String>>): String {
        var newYaml = yaml
        warnings[app.title]?.forEach { warning ->
            val tokens = warning.split("@")
            val step = tokens.first()
            newYaml = newYaml.replace(warning, step)
        }
        return newYaml
    }

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

    @JvmStatic
    fun main(args: Array<String>) {
        UpdateBitriseYamls.execute()
    }
}
