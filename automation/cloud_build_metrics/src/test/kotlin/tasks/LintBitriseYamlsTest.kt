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

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LintBitriseYamlsTest {

    @Test
    fun checkWarnings() {
        LintBitriseYamls.checkWarnings(mapOf())
    }

    @Test
    fun checkWarnings_whenWarningsExist() {
        val app = "app"
        val warnings = listOf("git-clone@1.2.3", "script@1.2.3")
        try {
            LintBitriseYamls.checkWarnings(mapOf(Pair(app, warnings)))
            fail("checkWarnings did not throw an exception!")
        } catch (e: RuntimeException) {
            val message = e.message ?: throw RuntimeException("Exception message was null!")
            assertTrue(message.contains(app))
            warnings.forEach { warning ->
                assertTrue(message.contains(warning))
            }
        }
    }
}
