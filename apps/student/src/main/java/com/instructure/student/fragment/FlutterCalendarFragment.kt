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
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.util.AppManager
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.*

class FlutterCalendarFragment : FlutterFragment() {
    var calendarScreenChannel = CalendarScreenChannel()
    var hidden: Boolean = false

    override fun provideFlutterEngine(context: Context): FlutterEngine? = AppManager.flutterEngine

    // Use texture mode instead of surface mode so the FlutterView doesn't render on top of the nav drawer and a11y borders
    override fun getRenderMode() = RenderMode.texture

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        FlutterComm.routeToCalendar(calendarScreenChannel.channelId)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        /* If there are multiple calendar fragments in the back stack, backgrounding and resuming the app
         will cause onStart and onResume to be called for all of them, causing each fragment's Flutter delegate
         to attempt to re-attach to the flutter engine nearly simultaneously. This places all but the top FlutterView
         into an invalid state where the view thinks it is attached, but is not.

         Because of this, subsequent calls to attach (triggered via onHiddenChanged) will be skipped and navigating
         back to previous fragments will result in a white screen. To work around this, we will only make the super
         call to onStart/onResume if this fragment not currently hidden. */
        if (hidden) skipLifecycleCall() else super.onStart()
    }

    override fun onResume() {
        // See comments in onStart
        if (hidden) skipLifecycleCall() else super.onResume()
    }

    /**
     *  Fakes a super call to a lifecycle method by setting 'mCalled' to true, allowing us to skip the side effects
     *  of the super call while also avoiding a SuperNotCalledException.
     */
    private fun skipLifecycleCall() {
        val field = Fragment::class.java.getDeclaredField("mCalled")
        field.isAccessible = true
        field.set(this, true)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        this.hidden = hidden
        // Workaround for an issue where the engine does not re-attach after additional FlutterCalendarFragments have been added and removed
        val field = FlutterFragment::class.java.getDeclaredField("delegate")
        field.isAccessible = true
        val delegate = field.get(this)

        val resuming = !hidden

        fun getFlutterView(): FlutterView {
            val flutterViewField = delegate::class.java.getDeclaredField("flutterView")
            flutterViewField.isAccessible = true
            return flutterViewField.get(delegate) as FlutterView
        }

        if (resuming) getFlutterView().attachToFlutterEngine(AppManager.flutterEngine)

        val lifecycle1 = delegate::class.java.getDeclaredMethod(if (resuming) "onStart" else "onPause")
        lifecycle1.isAccessible = true
        lifecycle1.invoke(delegate)

        val lifecycle2 = delegate::class.java.getDeclaredMethod(if (resuming) "onResume" else "onStop")
        lifecycle2.isAccessible = true
        lifecycle2.invoke(delegate)

        if (!resuming) getFlutterView().detachFromFlutterEngine()
    }

    fun handleBackPressed(): Boolean {
        // Get the current 'shouldPop' value. For now it is expected that 'shouldPop'  will only be true if the
        // current Flutter route is a CalendarScreen.
        val shouldPop = FlutterComm.shouldPop

        // Perform onBackPressed on the FlutterFragment, which will attempt to pop the current route and update
        // the 'shouldPop' value for future use.
        onBackPressed()

        // If 'shouldPop' was true it means we just popped a CalendarScreen in Flutter and that we should also
        // allow this fragment to be popped
        return !shouldPop
    }

    override fun onDestroyView() {
        super.onDestroyView()
        calendarScreenChannel.dispose()
    }
}

class CalendarScreenChannel {
    val channelId: String = UUID.randomUUID().toString()
    private val channel = MethodChannel(AppManager.flutterEngine.dartExecutor.binaryMessenger, channelId)

    var onRouteToItem: ((item: PlannerItem) -> Unit)? = null

    var onOpenDrawer: (() -> Unit)? = null

    init {
        channel.setMethodCallHandler { call, _ ->
            when (call.method) {
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
