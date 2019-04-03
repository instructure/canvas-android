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


package api.cloud

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import normal.NormalBuild
import normal.ToNormalApp
import normal.toNormalBuild
import java.time.ZonedDateTime

/** Generic wrapper for BitriseApps and BuddybuildApps */
class CloudApps(private val isBitrise: Boolean) {
    val title: String = if (isBitrise) "Bitrise" else "Buddybuild"

    fun getApps(): List<ToNormalApp> {
        return BitriseApps.getAppsForOrg()
    }

    companion object {
        fun getNormalBuilds(app: Any,
                            limitDateAfter: ZonedDateTime? = null,
                            limitDateBefore: ZonedDateTime? = null): List<NormalBuild> {
            return when (app) {
                is BitriseAppObject -> BitriseApps.getBuilds(app, limitDateAfter, limitDateBefore).mapNotNull { it.toNormalBuild() }
                else -> throw RuntimeException("Unknown app type ${app::class}")
            }
        }
    }
}
