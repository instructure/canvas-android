/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
 */

package com.instructure.parentapp.plugins

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

object UrlLauncher {
    private const val CHANNEL = "com.instructure.parentapp/url_launcher"
    private const val METHOD_LAUNCH = "launch"
    private const val METHOD_CAN_LAUNCH = "canLaunch"

    private val excludeComponents = arrayOf(
        // Student
        ComponentName("com.instructure.candroid", "com.instructure.student.activity.InterwebsToApplication"),

        // Parent (native)
        ComponentName("com.instructure.parentapp", "com.instructure.parentapp.activity.RouteValidatorActivity"),

        // Parent (flutter)
        ComponentName("com.instructure.parentapp", "com.instructure.parentapp.MainActivity"),

        // Teacher
        ComponentName("com.instructure.teacher", "com.instructure.teacher.activities.RouteValidatorActivity")
    )

    private lateinit var activity: Activity
    private lateinit var applicationContext: Context

    fun init(flutterEngine: FlutterEngine, applicationContext: Context, activity: Activity) {
        this.applicationContext = applicationContext
        this.activity = activity
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler(::handleCall)
    }

    private fun handleCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            METHOD_LAUNCH -> launch(call, result)
            METHOD_CAN_LAUNCH -> canLaunch(call, result)
            else -> result.notImplemented()
        }
    }

    private fun canLaunch(call: MethodCall, result: MethodChannel.Result) {
        val url: String = call.argument("url")!!;
        val excludeInstructure: Boolean = call.argument("excludeInstructure")!!;
        val intent: Intent = makeIntent(url, excludeInstructure)

        val componentName: ComponentName? = intent.resolveActivity(applicationContext.packageManager);
        val canLaunch = componentName != null
                && componentName.toShortString() != "{com.android.fallback/com.android.fallback.Fallback}";
        result.success(canLaunch)
    }

    private fun launch(call: MethodCall, result: MethodChannel.Result) {
        val url: String = call.argument("url")!!;
        val excludeInstructure: Boolean = call.argument("excludeInstructure")!!;
        val intent: Intent = makeIntent(url, excludeInstructure)

        activity.startActivity(intent);
        result.success(null)
    }

    private fun makeIntent(url: String, excludeInstructure: Boolean): Intent {
        var intent: Intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        if (excludeInstructure) {
            intent = Intent.createChooser(intent, null).apply {
                putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeComponents)
            }
        }
        return intent
    }
}
