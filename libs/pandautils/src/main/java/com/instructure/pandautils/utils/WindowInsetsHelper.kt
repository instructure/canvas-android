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
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

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

fun View.applyTopSystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(top = systemBars.top)
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

fun View.applyBottomSystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(bottom = systemBars.bottom)
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

private val TAG_ORIGINAL_MIN_HEIGHT = "originalMinHeight".hashCode()

fun View.applyBottomSystemBarInsetsWithHeight() {
    // Only capture original minHeight once to prevent accumulation on multiple calls
    val originalMinHeight = getTag(TAG_ORIGINAL_MIN_HEIGHT) as? Int ?: minimumHeight.also {
        setTag(TAG_ORIGINAL_MIN_HEIGHT, it)
    }
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(bottom = systemBars.bottom)
        view.minimumHeight = originalMinHeight + systemBars.bottom
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

private val TAG_ORIGINAL_BOTTOM_MARGIN = "originalBottomMargin".hashCode()

fun View.applyBottomSystemBarMargin() {
    // Only capture original margin once to prevent accumulation on multiple calls
    val originalBottomMargin = getTag(TAG_ORIGINAL_BOTTOM_MARGIN) as? Int ?: run {
        val margin = (layoutParams as? android.view.ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        setTag(TAG_ORIGINAL_BOTTOM_MARGIN, margin)
        margin
    }
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val layoutParams = view.layoutParams as? android.view.ViewGroup.MarginLayoutParams
        layoutParams?.bottomMargin = originalBottomMargin + systemBars.bottom
        view.layoutParams = layoutParams
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

private val TAG_ORIGINAL_BOTTOM_RIGHT_MARGINS = "originalBottomRightMargins".hashCode()

fun View.applyBottomAndRightSystemBarMargin() {
    // Only capture original margins once to prevent accumulation on multiple calls
    val originalMargins = getTag(TAG_ORIGINAL_BOTTOM_RIGHT_MARGINS) as? Pair<Int, Int> ?: run {
        val params = layoutParams as? android.view.ViewGroup.MarginLayoutParams
        val margins = Pair(params?.bottomMargin ?: 0, params?.rightMargin ?: 0)
        setTag(TAG_ORIGINAL_BOTTOM_RIGHT_MARGINS, margins)
        margins
    }
    val (originalBottomMargin, originalRightMargin) = originalMargins

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val layoutParams = view.layoutParams as? android.view.ViewGroup.MarginLayoutParams

        // In landscape mode, when nav bar is on the right, use original margin instead of adding system bars
        // to avoid creating extra gap
        val configuration = context.resources.configuration
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        layoutParams?.bottomMargin = originalBottomMargin + systemBars.bottom
        layoutParams?.rightMargin = if (isLandscape && systemBars.right > 0) {
            // In landscape with right nav bar, don't add extra margin - the nav bar already creates space
            originalRightMargin
        } else {
            originalRightMargin + systemBars.right
        }
        view.layoutParams = layoutParams
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

private val TAG_ORIGINAL_BOTTOM_RIGHT_PADDING = "originalBottomRightPadding".hashCode()

fun View.applyBottomAndRightSystemBarPadding() {
    // Only capture original padding once to prevent accumulation on multiple calls
    val originalPadding = getTag(TAG_ORIGINAL_BOTTOM_RIGHT_PADDING) as? Pair<Int, Int> ?: run {
        val padding = Pair(paddingBottom, paddingRight)
        setTag(TAG_ORIGINAL_BOTTOM_RIGHT_PADDING, padding)
        padding
    }
    val (originalBottomPadding, originalRightPadding) = originalPadding

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            bottom = originalBottomPadding + systemBars.bottom,
            right = originalRightPadding + systemBars.right
        )
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

fun View.applyHorizontalSystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(left = systemBars.left, right = systemBars.right)
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

fun View.applySystemBarInsets(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false,
    consumed: Boolean = false
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            top = if (top) systemBars.top else view.paddingTop,
            bottom = if (bottom) systemBars.bottom else view.paddingBottom,
            left = if (left) systemBars.left else view.paddingLeft,
            right = if (right) systemBars.right else view.paddingRight
        )
        if (consumed) WindowInsetsCompat.CONSUMED else insets
    }
}

fun View.applyImeAndSystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}

fun View.doOnApplyWindowInsets(block: (view: View, insets: Insets) -> WindowInsetsCompat) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        block(view, systemBars)
    }
}

/**
 * Applies horizontal padding to prevent content from extending behind display cutouts (e.g., camera cutout).
 * This is useful for constraining content width in landscape mode where the cutout may be on the sides.
 * Also helps constrain keyboard width to match the content area.
 */
fun View.applyDisplayCutoutInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(left = displayCutout.left, right = displayCutout.right)
        insets
    }
    // Request insets to be dispatched immediately if view is attached
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    }
}