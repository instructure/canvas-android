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

import normal.NormalApp
import normal.ToNormalApp

interface GenericApp {
    val normalApps: List<NormalApp>
    val apps: List<ToNormalApp>
    val pullRequestApps: List<ToNormalApp>

    @Suppress("UNCHECKED_CAST")
    fun <T> getGeneric(appName: String, id: String): T {
        val appIndex = normalApps.indexOfFirst { app -> app.id == id }
        if (appIndex == -1) throw RuntimeException("App with id $id not found")

        val app = normalApps[appIndex]
        if (app.name != appName) {
            println("App title renamed $appName -> ${app.name}. Verify and update.")
        }

        return apps[appIndex] as T
    }

}
