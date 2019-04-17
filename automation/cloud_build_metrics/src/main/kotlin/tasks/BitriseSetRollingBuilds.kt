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
import api.bitrise.private.RollingBuilds
import util.getOnlyInstructureApps
import java.lang.Thread.sleep

object BitriseSetRollingBuilds : BitriseTask {
    override fun execute() {
        signIn()
        val apps = BitriseApps.getOnlyInstructureApps()

        var updatedCount = 0
        for (app in apps) {
            val appSlug = app.slug

            val config = try {
                RollingBuilds.getConfig(appSlug)
            } catch (e: Exception) {
                RollingBuilds.enable(appSlug)
                // bitrise doesn't immediately enable rolling builds
                sleep(2000)
                RollingBuilds.getConfig(appSlug)
            }

            if (config.pr && config.push && config.running) continue

            println("Updating build config for: ${app.title}")
            updatedCount += 1

            RollingBuilds.enable(appSlug)
            RollingBuilds.setConfigPR(appSlug, true)
            RollingBuilds.setConfigPush(appSlug, true)
            RollingBuilds.setConfigRunning(appSlug, true)
        }

        println("Updated $updatedCount of ${apps.size} apps")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        this.execute()
    }
}
