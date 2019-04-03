/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:JvmName("MasqueradeUI")
package com.instructure.loginapi.login.util

import android.app.Activity
import android.os.Build
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.loginapi.login.R
import com.instructure.pandautils.utils.lastAncestorOrNull
import com.instructure.pandautils.utils.onClick
import kotlinx.android.synthetic.main.layout_masquerade_notification.view.*

/**
 * Adds a masquerade UI to this Activity if the user is currently masquerading.
 */
@Suppress("unused") // Added at compile time via MasqueradeUIInjector
fun Activity.showMasqueradeNotification(startingClass: Class<Activity>? = null) {
    window.showMasqueradeNotification(startingClass)
}

/**
 * Adds a masquerade UI if the user is currently masquerading AND this DialogFragment is being displayed as a dialog.
 */
@Suppress("unused") // Added at compile time via MasqueradeUIInjector
fun DialogFragment.showMasqueradeNotification(startingClass: Class<Activity>? = null) {
    dialog?.window?.showMasqueradeNotification(startingClass)
}

/**
 * Adds a masquerade UI if the user is currently masquerading AND this DialogFragment is being displayed as a dialog.
 */
@Suppress("unused") // Added at compile time via MasqueradeUIInjector
fun android.app.DialogFragment.showMasqueradeNotification(startingClass: Class<Activity>? = null) {
    dialog?.window?.showMasqueradeNotification(startingClass)
}

private fun Window.showMasqueradeNotification(startingClass: Class<Activity>? = null) {
    if (!ApiPrefs.isMasquerading) return
    decorView.rootView?.let { rootView ->
        if (findViewById<View>(R.id.masqueradeUINotificationContainer) != null) return
        // Add border
        val borderDrawable = ContextCompat.getDrawable(context, R.drawable.masquerade_outline)
        @Suppress("CascadeIf")
        if (Build.VERSION.SDK_INT >= 23) {
            rootView.foreground = borderDrawable
        } else if (rootView is FrameLayout) {
            rootView.foreground = borderDrawable
        } else {
            val borderView = View(context)
            borderView.background = borderDrawable
            (rootView as? ViewGroup)?.addView(borderView)
        }

        // Add notification view
        rootView.findViewById<View>(android.R.id.content).lastAncestorOrNull<LinearLayout>()?.let {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_masquerade_notification, it, false)
            view.masqueradeLabel.text = context.getString(R.string.masqueradingAs, ApiPrefs.user?.name)
            view.cancelMasqueradeButton.onClick {
                AlertDialog.Builder(context)
                    .setTitle(R.string.stopActingAsTitle)
                    .setMessage(context.getString(R.string.stopActingAsMessage, ApiPrefs.user?.name))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        MasqueradeHelper.stopMasquerading(startingClass)
                    }
                    .show()
            }
            it.addView(view, 0)
        }
    }
}


