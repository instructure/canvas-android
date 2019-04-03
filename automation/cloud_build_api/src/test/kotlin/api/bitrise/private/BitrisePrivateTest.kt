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


package api.bitrise.private

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import util.getEnv

class BitrisePrivateTest {
    private val testJobForCloudBuildMetrics = "4c2f997f51ab7b62"
    private val appSlug = testJobForCloudBuildMetrics

    @Before
    fun setUp() {
        val email = getEnv("BITRISE_USER")
        val password = getEnv("BITRISE_PASS")
        Users.signIn(email, password)
    }

    private fun assertConfig(config: RollingBuildsConfig, state: Boolean) {
        assertThat(config.pr, `is`(state))
        assertThat(config.push, `is`(state))
        assertThat(config.running, `is`(state))
    }

    @Test
    fun testAllRollingBuildAPIs() {
        // Disable / enable resets the configuration state to false.
        RollingBuilds.disable(appSlug)
        RollingBuilds.enable(appSlug)
        assertConfig(RollingBuilds.getConfig(appSlug), false)

        RollingBuilds.setConfigPR(appSlug, true)
        RollingBuilds.setConfigPush(appSlug, true)
        RollingBuilds.setConfigRunning(appSlug, true)
        assertConfig(RollingBuilds.getConfig(appSlug), true)
    }

    @Test
    fun buildCacheDelete() {
        val response = BuildCache.delete(appSlug)
        assertThat(response.status, `is`("ok"))
    }

    @Test
    fun buildCacheGet() {
        val response = BuildCache.get(appSlug)
        assertThat(response.size, `is`(0))
    }
}
