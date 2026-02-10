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
import android.os.Build
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat

object EdgeToEdgeHelper {

    fun enableEdgeToEdge(
        activity: Activity,
        statusBarStyle: SystemBarStyle? = null,
        navigationBarStyle: SystemBarStyle? = null
    ) {
        when (activity) {
            is ComponentActivity -> {
                if (statusBarStyle != null && navigationBarStyle != null) {
                    activity.enableEdgeToEdge(statusBarStyle, navigationBarStyle)
                } else {
                    activity.enableEdgeToEdge()
                }
            }
            else -> {
                WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            }
        }
    }

    fun enableEdgeToEdge(
        activity: Activity,
        lightStatusBar: Boolean,
        lightNavigationBar: Boolean
    ) {
        val statusBarStyle = if (lightStatusBar) {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        } else {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        }

        val navigationBarStyle = if (lightNavigationBar) {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        } else {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        }

        enableEdgeToEdge(activity, statusBarStyle, navigationBarStyle)
    }

    fun setTransparentSystemBars(window: Window) {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun setStatusBarColor(window: Window, @ColorInt color: Int) {
        window.statusBarColor = color
    }

    fun setNavigationBarColor(window: Window, @ColorInt color: Int) {
        window.navigationBarColor = color
    }

    fun isEdgeToEdgeEnforced(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }
}