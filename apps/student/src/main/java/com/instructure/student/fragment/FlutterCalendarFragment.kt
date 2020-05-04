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
 *
 */

package com.instructure.student.fragment

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.util.AppManager
import io.flutter.embedding.android.*
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.*

class FlutterCalendarFragment : FlutterFragment() {
    var calendarScreenChannel = CalendarScreenChannel()

    override fun provideFlutterEngine(context: Context): FlutterEngine? = AppManager.flutterEngine

    // Use texture mode instead of surface mode so the FlutterView doesn't render on top of the nav drawer and a11y borders
    override fun getRenderMode() = FlutterView.RenderMode.texture

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        FlutterComm.routeToCalendar(calendarScreenChannel.channelId)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        // Workaround for an issue where the engine does not re-attach after additional FlutterCalendarFragments have been added and removed
        val field = FlutterFragment::class.java.getDeclaredField("delegate")
        field.isAccessible = true
        val delegate = field.get(this)

        val resuming = !hidden

        val lifecycle1 = delegate::class.java.getDeclaredMethod(if (resuming) "onStart" else "onPause")
        lifecycle1.isAccessible = true
        lifecycle1.invoke(delegate)

        val lifecycle2 = delegate::class.java.getDeclaredMethod(if (resuming) "onResume" else "onStop")
        lifecycle2.isAccessible = true
        lifecycle2.invoke(delegate)
    }

    fun handleBackPressed(): Boolean {
        val shouldPop = FlutterComm.shouldPop
        onBackPressed()
        return !shouldPop
    }

    override fun onDestroyView() {
        super.onDestroyView()
        calendarScreenChannel.dispose()
    }
}

class CalendarScreenChannel{
    val channelId: String = UUID.randomUUID().toString()
    private val channel = MethodChannel(AppManager.flutterEngine.dartExecutor.binaryMessenger, channelId)

    var onRouteToItem: ((item: PlannerItem) -> Unit)? = null

    var onOpenDrawer: (() -> Unit)? = null

    init {
        channel.setMethodCallHandler { call, _ ->
            when(call.method) {
                "openDrawer" -> onOpenDrawer?.invoke()
                "routeToItem" -> {
                    val item = Gson().fromJson(call.arguments as String, PlannerItem::class.java)
                    onRouteToItem?.invoke(item)
                }
            }
        }
    }

    fun dispose() {
        channel.setMethodCallHandler(null)
    }
}
