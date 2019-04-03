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
import api.bitrise.Owner
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateBitriseYamlsTest {

    private val app = BitriseAppObject(
            slug = "appSlug",
            title = "appTitle",
            project_type = "projectType",
            provider = "provider",
            repo_owner = "repoOwner",
            repo_url = "repoUrl",
            repo_slug = "repoSlug",
            is_disabled = false,
            status = 1,
            is_public = true,
            owner = Owner("a", "b", "c")
    )

    private val cleanYaml = """
        workflows:
            prepare:
                steps:
                    - cache-pull: {}
                    - git-clone: {}
            teardown:
                steps:
                    - slack: {}
                    - cache-push: {}
        """.trimIndent()

    private val dirtyYaml = """
        workflows:
            prepare:
                steps:
                    - cache-pull: {}
                    - git-clone@1.2.3: {}
            teardown:
                steps:
                    - slack: {}
                    - cache-push@1.2.3: {}
        """.trimIndent()

    private val warnings = mapOf(Pair(app.title, listOf("git-clone@1.2.3", "cache-push@1.2.3")))

    @Test
    fun fixStepVersions() {
        val yaml = UpdateBitriseYamls.fixStepVersions(app, dirtyYaml, warnings)
        assertEquals(cleanYaml, yaml)
    }
}
