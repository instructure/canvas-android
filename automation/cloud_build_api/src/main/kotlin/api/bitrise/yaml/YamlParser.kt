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

package api.bitrise.yaml

import api.bitrise.BitriseAppObject
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

object YamlParser {
    private val mapper by lazy {
        val map = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        map.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    fun parse(yaml: String): JsonNode {
        return YamlParser.mapper.readTree(yaml)
    }

    fun workflows(yaml: String): JsonNode {
        return parse(yaml).findValue("workflows") ?: throw RuntimeException("Could not find workflows!")
    }

    /**
    workflows:
    prepare:
    steps:
    - activate-ssh-key:
    run_if: '{{getenv "SOME_ENV" | ne ""}}'
    - git-clone: {}
    - cache-pull: {}
     */
    fun stepVersionWarnings(app: BitriseAppObject, yaml: String): Map<String, List<String>> {
        val warnings = mutableMapOf<String, MutableList<String>>()
        val steps = workflows(yaml).findValues("steps") ?: throw RuntimeException("Could not find steps!")
        val stepRegex = Regex(".*@\\d.*")
        val key = "${app.title} (${app.slug})"
        steps.forEach {
            it.forEach { step ->
                // Isolate the step name and version
                //   {"brew-install@0.9.0":{"inputs":[{"packages":"yarn hub"}],"title":"Brew install yarn & hub"}}
                //   {"git-clone@4.0.5":{}}
                val stepName = step.fieldNames().next()
                        ?: throw RuntimeException("Unable to extract step name from: $step")

                // Any step not set to use the "latest version" will end with "@<version>"
                //   {"git-clone":{}}
                //   {"git-clone@4.0.5":{}}
                if (stepRegex.matches(stepName)) {
                    if (warnings[key] == null) {
                        warnings[key] = mutableListOf(stepName)
                    } else {
                        warnings[key]?.add(stepName)
                    }
                }
            }
        }
        return warnings
    }
}
