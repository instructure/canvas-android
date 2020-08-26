/**
 * Copyright (C) 2017 Drew Hannay
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.runner
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry

// https://gist.github.com/drewhannay/7fa758847cad8a6dc26f0d1d3cb068ad

/**
 * Helper class for calling protected methods in MonitoringInstrumentation
 */
object MonitoringInstrumentationAccessor {

    private val TAG = MonitoringInstrumentationAccessor::class.java.simpleName

    private val HANDLER_FOR_MAIN_LOOPER = Handler(Looper.getMainLooper())

    /**
     * Synchronously finish all currently started activities
     */
    fun finishAllActivities() {
        val instrumentation = InstrumentationRegistry.getInstrumentation() as MonitoringInstrumentation
        val activityFinisher = instrumentation.ActivityFinisher()

        HANDLER_FOR_MAIN_LOOPER.post(activityFinisher)

        val startTime = System.currentTimeMillis()
        instrumentation.waitForActivitiesToComplete()
        val endTime = System.currentTimeMillis()
        Log.i(TAG, String.format("waitForActivitiesToComplete() took: %sms", endTime - startTime))
    }
}
