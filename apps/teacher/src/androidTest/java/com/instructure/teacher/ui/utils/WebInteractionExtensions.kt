package com.instructure.teacher.ui.utils

import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.Evaluation
import androidx.test.espresso.web.sugar.Web

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
