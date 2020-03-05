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

package com.instructure.parentapp

import android.content.Intent
import android.net.Uri
import androidx.annotation.NonNull
import com.instructure.parentapp.plugins.DataSeedingPlugin
import com.instructure.parentapp.plugins.OldAppMigrations
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        OldAppMigrations.init(flutterEngine, applicationContext)
        DataSeedingPlugin.init(flutterEngine)

        checkForLinkEvent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkForLinkEvent(intent, newIntent = true)
    }

    private fun checkForLinkEvent(intent: Intent, newIntent: Boolean = false) {
        val data = intent.data
        if (intent.action != Intent.ACTION_VIEW || data == null) return

        // NOTE: The `url` query param has to match ParentRouter.dart in the flutter code
        val route = "/external?url=${Uri.encode(data.toString())}"
        if (newIntent) {
            flutterEngine?.navigationChannel?.pushRoute(route)
        } else {
            this.intent = intent.putExtra("initial_route", route)
        }
    }
}
