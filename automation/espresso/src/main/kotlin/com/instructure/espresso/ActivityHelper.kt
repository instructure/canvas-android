/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Nico Küchler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.instructure.espresso

import android.app.Activity
import androidx.test.espresso.core.internal.deps.guava.base.Preconditions
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.concurrent.atomic.AtomicReference

/**
 * source: https://github.com/nenick/espresso-macchiato/blob/2c85c7461065f1cee36bbb06386e91adaef47d86/espresso-macchiato/src/main/java/de/nenick/espressomacchiato/testbase/EspCloseAllActivitiesFunction.java
 */
object ActivityHelper {
    fun currentActivity(): Activity {
        // fix: java.lang.IllegalStateException: Querying activity state off main thread is not allowed.
        val activity = AtomicReference<Activity>(null)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activity.set(Iterables.getOnlyElement(getActivitiesInStages(Stage.RESUMED)) as Activity?) }

        val result = activity.get()
        Preconditions.checkNotNull(result)
        return result
    }

    private fun getActivitiesInStages(vararg stages: Stage): Set<Activity> {
        val activities = HashSet<Activity>()
        val instance = ActivityLifecycleMonitorRegistry.getInstance()
        for (stage in stages) {
            activities.addAll(instance.getActivitiesInStage(stage))
        }
        return activities
    }
}
