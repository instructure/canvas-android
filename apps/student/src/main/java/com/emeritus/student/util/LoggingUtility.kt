/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.emeritus.student.util

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Logger.canLogUserDetails
import com.instructure.canvasapi2.utils.tryOrNull

object LoggingUtility {
    private const val TAG = "canvasLog"

    /**
     * Logs Console [android.util.Log.d] and Crashlytics [Crashlytics.log]}
     *
     * @param priority The priority of the logging. Examples are [android.util.Log.DEBUG] and [android.util.Log.ERROR]
     * @param msg      The message to log to console
     * @see FirebaseCrashlytics.log
     * @see android.util.Log.d
     */
    fun log(priority: Int, msg: String?) {
        // Will write to crashlytics and logcat
        if (canLogUserDetails()) FirebaseCrashlytics.getInstance().log("Priority: $priority, Tag: $TAG, Message: $msg")
    }

    fun logIntent(intent: Intent?): String {
        intent ?: return ""
        val bundle = intent.extras
        return logBundle(bundle)
    }

    /**
     * Logs all data of Intent using HelpDesk, Console [android.util.Log.d], and FirebaseCrashlytics [FirebaseCrashlytics.log]
     *
     * @param bundle  The bundle that we want to log.
     * @return The string that was actually logged.
     */
    private fun logBundle(bundle: Bundle?): String {
        bundle ?: return ""
        val key = bundle.keySet() ?: return ""
        val iterator: Iterator<String> = key.iterator()
        var logMSG = ""
        tryOrNull { logMSG += bundle.getString("__previous") }
        tryOrNull { logMSG += " --> " + bundle.getString("__current") + ";" }
        logMSG += "\n"
        while (iterator.hasNext()) {
            val keyString = iterator.next()
            if (keyString == "__current" || keyString == "__previous") continue
            val o = bundle[keyString] ?: continue
            logMSG += "$keyString:$o;\n"
        }
        log(Log.DEBUG, logMSG)
        return logMSG
    }
}
