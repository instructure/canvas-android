/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.ui.utils.extensions

import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.Evaluation
import androidx.test.espresso.web.sugar.Web
import com.adevinta.android.barista.interaction.BaristaSleepInteractions.sleep

private fun execute(block: () -> Any): Boolean {
    return try {
        block()
        true
    } catch (e: Exception) {
        false
    }
}

// espresso-core-3.0.2-sources.jar!/android/support/test/espresso/action/RepeatActionUntilViewState.java
private fun Web.WebInteraction<*>.repeatedlyUntil(
        action: Atom<Evaluation>,
        desiredStateMethod: () -> Web.WebInteraction<*>,
        maxAttempts: Int,
        timeoutMillis: Long = 500,
        throwsException: Boolean = false) {
    var noOfAttempts = 1

    while (execute(desiredStateMethod) == throwsException
            && noOfAttempts <= maxAttempts) {
        sleep(timeoutMillis)
        execute({ this.perform(action) })
        noOfAttempts++
    }

    if (noOfAttempts > maxAttempts) {
        throw RuntimeException("Failed to achieve view state after $maxAttempts attempts")
    }
}

fun Web.WebInteraction<*>.repeatedlyUntil(
        action: Atom<Evaluation>,
        desiredStateMatcher: () -> Web.WebInteraction<*>,
        maxAttempts: Int) {
    repeatedlyUntil(action = action,
            desiredStateMethod = desiredStateMatcher,
            maxAttempts = maxAttempts)
}

fun Web.WebInteraction<*>.repeatedlyUntilNot(
        action: Atom<Evaluation>,
        desiredStateMatcher: () -> Web.WebInteraction<*>,
        maxAttempts: Int) {
    repeatedlyUntil(action = action,
            desiredStateMethod = desiredStateMatcher,
            throwsException = true,
            maxAttempts = maxAttempts)
}
