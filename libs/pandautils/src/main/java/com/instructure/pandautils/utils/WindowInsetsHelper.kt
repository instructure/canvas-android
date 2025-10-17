/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.utils

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object WindowInsetsHelper {

    fun setStatusBarAppearance(window: Window, isLightStatusBar: Boolean) {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightStatusBar
    }

    fun setNavigationBarAppearance(window: Window, isLightNavigationBar: Boolean) {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = isLightNavigationBar
    }

    fun setSystemBarsAppearance(window: Window, isLight: Boolean) {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLight
        insetsController.isAppearanceLightNavigationBars = isLight
    }

    fun setSystemBarsAppearanceAuto(activity: Activity) {
        val isLightMode = !isDarkModeEnabled(activity)
        setSystemBarsAppearance(activity.window, isLightMode)
    }

    fun setStatusBarColor(activity: Activity, @ColorInt color: Int, lightIcons: Boolean = false) {
        activity.window.statusBarColor = color
        setStatusBarAppearance(activity.window, isLightStatusBar = !lightIcons)
    }

    fun setStatusBarDark(activity: Activity, @ColorInt color: Int) {
        activity.window.statusBarColor = ThemePrefs.darker(color)
        setStatusBarAppearance(activity.window, isLightStatusBar = false)
    }

    fun setStatusBarLight(activity: Activity) {
        activity.window.statusBarColor = activity.getColor(com.instructure.pandautils.R.color.dimLighterGray)
        setStatusBarAppearance(activity.window, isLightStatusBar = true)
    }

    private fun isDarkModeEnabled(activity: Activity): Boolean {
        val nightModeFlags = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun getInsetsController(view: View): WindowInsetsControllerCompat? {
        return ViewCompat.getWindowInsetsController(view)
    }

    fun hideSystemBars(window: Window) {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun showSystemBars(window: Window) {
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}