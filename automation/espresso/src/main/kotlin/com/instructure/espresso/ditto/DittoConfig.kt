/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.espresso.ditto

import okhttp3.OkHttpClient
import okreplay.*

/**
 * The global configuration to be passed into [DittoRule] during test setup.
 *
 * @param globalMode The global mode in which tests will run. See [DittoMode] for possible values and their behavior.
 *
 * @param tapeRoot A [TapeRoot] object which controls the storage location of tapes. For default behavior this should
 * be an instance of [AndroidTapeRoot] which reads tapes from the app assets and writes new tapes to external storage.
 *
 * @param matchRules An array of [MatchRules] which determine how a particular request will be matched with a recorded
 * request and its associated response. For the most requests, [MatchRules.uri] and [MatchRules.method] are sufficient.
 */
class DittoConfig(
    val globalMode: DittoMode = DittoMode.PLAY,
    val tapeRoot: TapeRoot,
    matchRules: Array<MatchRules> = emptyArray()
) {

    val matchRule: MatchRule = ComposedMatchRule.of(*matchRules)

    companion object {

        val interceptor = DittoInterceptor()

        /** Returns a clone of the provided [client] to which a [DittoInterceptor] has been added */
        fun setupClient(client: OkHttpClient): OkHttpClient = client
            .newBuilder()
            .addInterceptor(interceptor)
            .build()

    }

}
