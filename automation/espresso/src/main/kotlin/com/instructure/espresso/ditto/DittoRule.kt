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

import okreplay.ComposedMatchRule
import okreplay.DittoRecorder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** Returns this string if is it non-null and non-blank, otherwise returns null */
@Suppress("NOTHING_TO_INLINE")
inline fun String?.validOrNull(): String? = this?.takeIf { !it.isBlank() }

/**
 * A test rule which automatically enables VCR functionality for tests annotated with [@Ditto][Ditto]
 */
class DittoRule(private val config: DittoConfig) : TestRule {

    private val recorder = DittoRecorder(config)

    override fun apply(statement: Statement, description: Description): Statement {
        // Get the @Ditto annotation; skip if there is none
        val annotation = description.getAnnotation(Ditto::class.java) ?: return statement

        // Get mode of the test class (if annotated)
        val classMode = description.testClass?.getAnnotation(DittoClassMode::class.java)?.mode ?: DittoMode.PLAY

        // Select highest-priority mode from global config, test class, and individual test
        val runMode = maxOf(config.globalMode, classMode, annotation.mode)

        // Skip if mode is LIVE
        if (runMode == DittoMode.LIVE) return statement

        return object : Statement() {
            override fun evaluate() {
                try {
                    val tapeName = annotation.tapeName.validOrNull() ?: DittoRecorder.defaultTapeName(description)
                    val matchRules = annotation.matchRules.takeIf { it.isNotEmpty() }?.let { ComposedMatchRule.of(*it) }
                    recorder.start(tapeName, annotation.sequential, matchRules, runMode)
                    statement.evaluate()
                    recorder.stop(true)
                } catch (e: Exception) {
                    recorder.stop(false)
                    throw e
                }
            }
        }
    }

}
