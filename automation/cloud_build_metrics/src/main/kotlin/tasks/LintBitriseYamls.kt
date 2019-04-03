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

import api.bitrise.BitriseApps
import api.bitrise.yaml.BitriseYaml
import api.bitrise.yaml.YamlParser.stepVersionWarnings
import util.getOnlyInstructureApps

object LintBitriseYamls : Task {

    internal fun checkWarnings(warnings: Map<String, List<String>>) {
        if (warnings.isNotEmpty()) {
            val errors = StringBuilder()
            warnings.forEach { app, warningsList ->
                errors.appendln(app)
                warningsList.forEach { warning ->
                    errors.appendln("  $warning")
                }
            }

            throw RuntimeException("💥 Bitrise steps with pinned versions found!\n\n$errors")
        }

        println("🎉 No Bitrise steps with pinned versions were found.")
    }

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

    @JvmStatic
    fun main(args: Array<String>) {
        LintBitriseYamls.execute()
    }
}
