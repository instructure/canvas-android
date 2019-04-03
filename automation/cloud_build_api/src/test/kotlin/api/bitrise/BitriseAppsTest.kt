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


package api.bitrise

import normal.toNormalBuild
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import util.dateToZonedTime
import util.getEnv

@Suppress("LocalVariableName")
class BitriseAppsTest {

    @Test
    fun getAppsForOrg() {
        val result = BitriseApps.getAppsForOrg(getEnv("BITRISE_ORG"))
        assertThat(result.size, greaterThan(1))
    }

    @Test
    fun getBuilds() {
        val mockApp = mock(BitriseAppObject::class.java)
        `when`(mockApp.slug).thenReturn("18a96964420643c0")

        val result = BitriseApps.getBuilds(mockApp, dateToZonedTime("2017-10-30"))
        assertThat(result.size, greaterThan(10))
    }

    @Test
    fun bitriseBuildObjectToNormalBuild() {
        val triggered_at = "2017-10-31T20:41:29Z"
        val started_on_worker_at = "2017-10-31T20:41:29Z"
        val environment_prepare_finished_at = "2017-10-31T20:42:46Z"
        val finished_at = "2017-10-31T20:48:42Z"
        val slug = "352646333a6a176b"
        val status_text = "error"

        val mockData = mock(BitriseBuildObject::class.java)
        `when`(mockData.triggered_at).thenReturn(triggered_at)
        `when`(mockData.started_on_worker_at).thenReturn(started_on_worker_at)
        `when`(mockData.environment_prepare_finished_at).thenReturn(environment_prepare_finished_at)
        `when`(mockData.finished_at).thenReturn(finished_at)
        `when`(mockData.slug).thenReturn(slug)
        `when`(mockData.status_text).thenReturn(status_text)

        val normalBuild = mockData.toNormalBuild()!!
        assertThat(normalBuild.buildId, `is`(slug))
        assertThat(normalBuild.buildDate, `is`("2017-10-31"))
        assertThat(normalBuild.buildYearweek, `is`("2017 44"))
        assertThat(normalBuild.buildQueued, `is`(0L))
        assertThat(normalBuild.buildDuration, `is`(433L))
        assertThat(normalBuild.buildSuccessful, `is`(false))
    }
}
