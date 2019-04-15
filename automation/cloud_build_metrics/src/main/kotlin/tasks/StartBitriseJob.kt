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

import api.bitrise.*
import util.defaultBitriseAppSlug
import util.defaultBitriseTxt
import support.upload.apks.Apk
import java.io.File
import java.lang.System.exit

object StartBitriseJob : Task {

    private const val JOBS_TO_TRIGGER = 1
    private val BUILD_IDS: MutableList<String> = mutableListOf()
    var environmentVariables: List<Apk> = listOf()

    private fun getApp(): BitriseAppObject {
        val apps = BitriseApps.getAppsForOrg()
        val app = apps.find { it.slug == defaultBitriseAppSlug }
        if (app == null) {
            throw RuntimeException("Could not find app_id: '$defaultBitriseAppSlug'")
        } else {
            return app
        }
    }

    private fun warnUser() {
        val warningMessage = """
            Warning! You are about to trigger $JOBS_TO_TRIGGER builds on Bitrise.
            Doing this during business hours may consume all available resources in CI and may prevent any other builds from running.

            Are you sure you want to proceed? Please type 'Yes' to confirm:
        """.trimIndent()

        println(warningMessage)
        if (!readLine().equals("Yes", ignoreCase = true)) {
            println("Thanks for doing the right thing!")
            exit(1)
        }
    }

    private fun writeFile() {
        val file = File(defaultBitriseTxt)
        file.parentFile.mkdirs()
        file.bufferedWriter().use { out ->
            BUILD_IDS.forEach {
                out.write(it)
                out.newLine()
            }
        }
    }

    override fun execute() {
        warnUser()

        val appSlug = "693f666c209a029b"
        val buildRequest = BitriseTriggerBuildRequest(
                build_params = BuildParams(workflow_id = "primary", environments = emptyList())
        )

        repeat(JOBS_TO_TRIGGER) {
            val build = BitriseApps.triggerBuild(appSlug, buildRequest)
            BUILD_IDS.add(build.build_slug)
            println("Build triggered: ${build.build_url}")
        }

        writeFile()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        execute()
    }
}
