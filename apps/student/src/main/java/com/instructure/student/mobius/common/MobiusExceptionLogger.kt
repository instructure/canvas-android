/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.common

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.student.BuildConfig
import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.android.AndroidLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Intercepts exceptions in the mobius loop's update and init operations and logs them to Crashlytics.
 * For debug builds the exception will be logged locally and then thrown.
 */
class MobiusExceptionLogger<MODEL, EVENT, EFFECT> : AndroidLogger<MODEL, EVENT, EFFECT>("Mobius") {
    override fun afterUpdate(model: MODEL, event: EVENT, result: Next<MODEL, EFFECT>) {
        if (BuildConfig.DEBUG) super.afterUpdate(model, event, result)
    }

    override fun afterInit(model: MODEL, result: First<MODEL, EFFECT>) {
        if (BuildConfig.DEBUG) super.afterInit(model, result)
    }

    override fun beforeInit(model: MODEL) {
        if (BuildConfig.DEBUG) super.beforeInit(model)
    }

    override fun beforeUpdate(model: MODEL, event: EVENT) {
        if (BuildConfig.DEBUG) super.beforeUpdate(model, event)
    }

    override fun exceptionDuringInit(model: MODEL, exception: Throwable) {
        if (BuildConfig.DEBUG) {
            super.exceptionDuringInit(model, exception)
            // Must throw as a separate message, otherwise Mobius might consume the exception
            GlobalScope.launch(Dispatchers.Main) { throw exception }
        } else {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    override fun exceptionDuringUpdate(model: MODEL, event: EVENT, exception: Throwable) {
        if (BuildConfig.DEBUG) {
            super.exceptionDuringUpdate(model, event, exception)
            // Must throw as a separate message, otherwise Mobius might consume the exception
            GlobalScope.launch(Dispatchers.Main) { throw exception }
        } else {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }
}
