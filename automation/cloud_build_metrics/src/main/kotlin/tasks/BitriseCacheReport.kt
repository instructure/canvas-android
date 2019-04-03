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
import api.bitrise.private.BuildCache
import util.getOnlyInstructureApps
import util.humanReadable

/** List all cached branches  **/
object BitriseCacheReport : BitriseTask {
    override fun execute() {
        signIn()
        val apps = BitriseApps.getOnlyInstructureApps()

        for (app in apps) {
            val cache = BuildCache.get(app.slug)
            if (cache.isNotEmpty()) {
                println(app.title)
                for (item in cache) {
                    val size = item.file_size_bytes.humanReadable()
                    val branch = item.the_cache_item_key
                    println("  $size - $branch")
                }
            }
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        this.execute()
    }
}
