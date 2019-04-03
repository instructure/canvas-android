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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class BackupBitriseYamlsTest {

    private val apps = BitriseApps.getAppsForOrg()
    private val fixturesPath = "./src/test/kotlin/fixtures"
    private val readmeTableHeader = """
        ## Slug to Title
        Slug | Title | Status
        -----|-------|:--------:""".trimIndent()
    private val tableHeaderLines = readmeTableHeader.lines().size

    @Before
    fun setUp() {
        assertTrue(apps.isNotEmpty())
        BackupBitriseYamls.keybasePath = fixturesPath
        File(fixturesPath).deleteRecursively()
        File(fixturesPath).mkdirs()
        File(fixturesPath).resolve("bitrise").mkdir()
        File(fixturesPath).resolve("README.md").writeText(readmeTableHeader)
    }

    @Test
    fun keybaseRepo() {
        assertTrue(BackupBitriseYamls.keybaseRepo().exists())
    }

    @Test
    fun writeYamls() {
        val bitrise = File(fixturesPath).resolve("bitrise")
        BackupBitriseYamls.writeYamls(File(fixturesPath), apps)
        assertTrue(bitrise.exists())
        assertEquals(apps.size, bitrise.list().size)
        bitrise.list().forEach { file ->
            assertNotNull(apps.find { app -> app.slug == file.split(".").first() })
        }
    }

    @Test
    fun appsMap() {
        val map = BackupBitriseYamls.appsMap(apps)
        assertEquals(apps.size, map.size)
        apps.forEach { app ->
            assertNotNull(map[app.slug])
            assertEquals(app.title, map[app.slug])
        }
    }

    @Test
    fun updateReadme() {
        // task must have the same results when executed multiple times
        repeat(2) {
            BackupBitriseYamls.updateReadme(File(fixturesPath), BackupBitriseYamls.appsMap(apps))
            var readme = File(fixturesPath).resolve("README.md").readText()
            assertTrue(readme.isNotEmpty())

            val readmeLines = readme.lines()
            assertEquals(apps.size, readmeLines.size - tableHeaderLines)

            readmeLines.subList(tableHeaderLines, readmeLines.size).forEach { line ->
                var slug = line.substring(line.indexOf("[") + 1)
                slug = slug.substring(0, slug.indexOf("]"))
                val app = apps.find { app -> app.slug == slug }
                        ?: throw RuntimeException("App with slug $slug not found!")
                assertEquals(app.title, line.split("|")[1])
                assertTrue(line.endsWith("✅"))
            }
        }
    }
}
