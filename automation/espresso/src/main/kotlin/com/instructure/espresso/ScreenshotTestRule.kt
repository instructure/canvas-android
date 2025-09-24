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



package com.instructure.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.espresso.matchers.WaitForCheckMatcher
import com.instructure.espresso.matchers.WaitForViewMatcher
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.atomic.*

class ScreenshotTestRule : TestRule {

    // Run all test methods tryCount times. Take screenshots on failure.
    // A method rule would allow targeting specific (method.getAnnotation(Retry.class))
    private val tryCount = 5

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var error: Throwable? = null

                val errorHandled = AtomicBoolean(false)

                // Espresso failure handler will capture accurate UI screenshots.
                // if we wait for `try { base.evaluate() } catch ()` then the UI will be in a different state
                //
                // Only espresso failures trigger the espresso failure handlers. For JUnit assert errors,
                // those must be captured in `try { base.evaluate() } catch ()`
                Espresso.setFailureHandler { throwable, matcher ->
                    // Don't save intermediate waitForView/waitForCheck screenshots
                    if (WaitForViewMatcher.finishedWaiting() &&
                            WaitForCheckMatcher.finishedWaiting() &&
                            ClickUntilMethod.finishedWaiting()) {
                        EspressoScreenshot.takeScreenshot(description)
                    }
                    errorHandled.set(true)
                    DefaultFailureHandler(InstrumentationRegistry.getInstrumentation().targetContext).handle(throwable, matcher)
                }

                for (i in 0 until tryCount) {
                    errorHandled.set(false)
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        if (!errorHandled.get()) {
                            EspressoScreenshot.takeScreenshot(description)
                        }
                        error = t

                        // Report all failures, using "disposition" to distinguish between fails/retries
                        if(failureHandler != null) {
                            val disposition = if(i == tryCount-1) "failed" else "retry"
                            failureHandler!!(error, description.methodName, description.className, disposition)
                        }
                    }

                }

                if (error != null) throw error
            }
        }
    }

    // Allow for higher-level code to register a hook for retries.
    companion object {
        var failureHandler : ((t: Throwable, testName: String, testClass: String, disposition: String) -> Unit)? = null
        fun registerFailureHandler(handler: (t: Throwable, testName: String, testClass: String, disposition: String) -> Unit) {
            failureHandler = handler
        }
    }
}
