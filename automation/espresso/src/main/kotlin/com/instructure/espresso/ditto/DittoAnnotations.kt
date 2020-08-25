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

import com.instructure.espresso.ditto.DittoMode.*
import okreplay.MatchRules

/**
 * The mode in which ditto tests should run. This mode can be set for individual tests via the [mode][Ditto.mode]
 * parameter of the [@Ditto][Ditto] annotation, for all Ditto tests in the same class via [DittoClassMode], or globally
 * for all tests via [DittoConfig]. In all instances the default mode is [PLAY].
 *
 * The actual run mode for a given test will always be the highest-priority mode set either for that test, for its
 * containing class, or for the [global config][DittoConfig]. [LIVE] is given the highest priority, followed by [RECORD],
 * and then by [PLAY].
 */
enum class DittoMode {
    PLAY, RECORD, LIVE // Declared in ascending order of priority
}

/**
 * Annotation for test methods to enable Ditto's VCR functionality for network requests.
 *
 * Use [tapeName] to specify the name of the tape file where network interactions will be stored. If no name is
 * specified (recommended) then a default value will be generated using the test method name.
 *
 * By default only one response is recorded/played per matching request. For example, if the [matchRules] match against
 * the URL, and two different requests use the same URL, then only the most recent response will be recorded/played.
 * By setting [sequential] to true, a different response will be recorded/played for each unique request, and matching
 * requests will be played back in the order they were recorded.
 *
 * The [mode] parameter specifies the mode in which the test should run: [PLAY] (default), [RECORD], or [LIVE].
 *
 *  - In [PLAY] mode, the responses to network requests will be read from a previously-recorded tape. If no such tape
 *    exists, or if a request is made that does not have a recorded response, the test will fail and must be re-recorded
 *    to include the missing interactions.
 *
 *  - In [RECORD] mode, all network interactions are performed live and the responses are recorded to a new tape. If a
 *    test fails in this mode then its tape will be discarded.
 *
 *  - In [LIVE] mode, Ditto's VCR functionality is disabled and tests will run normally with live network requests.
 *
 *  The mode can also be specified for entire test classes using the [@DittoClassMode][DittoClassMode] annotation, or
 *  globally for all ditto tests via the [DittoConfig] instance passed to [DittoRule]. Note that the global mode does
 *  not necessarily override the individual test / class mode or vice versa. Rather, the mode with the highest priority
 *  is always used. See [DittoMode] for more information on mode priority.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Ditto(
    val tapeName: String = "",
    val sequential: Boolean = false,
    val mode: DittoMode = DittoMode.PLAY,
    val matchRules: Array<MatchRules> = []
)

/**
 * Specifies the [DittoMode] for all _@Ditto_ tests in a test class
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DittoClassMode(val mode: DittoMode)
