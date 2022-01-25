/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.*
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ScaleXSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R

object ViewStyler {

    fun setToolbarElevationSmall(context: Context, toolbar: Toolbar) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(R.dimen.utils_toolbar_elevation_small))
    }

    fun setToolbarElevation(context: Context, toolbar: Toolbar) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(R.dimen.utils_toolbar_elevation))
    }

    fun setToolbarElevation(context: Context, toolbar: Toolbar, @DimenRes elevation: Int) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(elevation))
    }

    fun colorToolbarIconsAndText(activity: Activity, toolbar: Toolbar, @ColorInt color: Int) {
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        ToolbarColorizeHelper.colorizeToolbar(toolbar, color, activity)
    }

    fun themeEditText(context: Context, editText: AppCompatEditText, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.canvasEditTextColor)
        editText.supportBackgroundTintList = makeColorStateList(defaultColor, brand)
        editText.highlightColor = ThemePrefs.increaseAlpha(brand)
    }

    fun themeRadioButton(context: Context, radioButton: AppCompatRadioButton, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.canvasEditTextColor)
        radioButton.supportButtonTintList = makeColorStateList(defaultColor, brand)
        radioButton.highlightColor = ThemePrefs.increaseAlpha(defaultColor)
    }

    fun themeSpinner(context: Context, spinner: AppCompatSpinner, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.canvasEditTextColor)
        spinner.supportBackgroundTintList = makeColorStateList(defaultColor, brand)
    }

    fun themeSwitch(context: Context, switch: SwitchCompat, @ColorInt brand: Int = ThemePrefs.brandColor) {
        val thumbColor = brand

        // trackColor is the thumbColor with 30% transparency (77)
        val trackColor = Color.argb(77, Color.red(thumbColor), Color.green(thumbColor), Color.blue(thumbColor))

        // setting the thumb color
        switch.thumbDrawable.setTintList(ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(thumbColor, ContextCompat.getColor(context, R.color.defaultThumbColor)))
        )

        // setting the track color
        switch.trackDrawable.setTintList(ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(trackColor, ContextCompat.getColor(context, R.color.defaultTrackColor))))
    }

    fun themeInputTextLayout(textInputLayout: TextInputLayout, @ColorInt color: Int) {
        try {
            val fDefaultTextColor = TextInputLayout::class.java.getDeclaredField("mDefaultTextColor")
            fDefaultTextColor.isAccessible = true
            fDefaultTextColor.set(textInputLayout, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(color)))

            val fFocusedTextColor = TextInputLayout::class.java.getDeclaredField("mFocusedTextColor")
            fFocusedTextColor.isAccessible = true
            fFocusedTextColor.set(textInputLayout, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(color)))
        } catch (e: Exception) { }
    }

    fun themeActionBar(activity: Activity?, actionBar: ActionBar?, @ColorInt backgroundColor: Int) {
        if (activity != null && actionBar != null) {
            val colorDrawable = ColorDrawable(backgroundColor)
            actionBar.setBackgroundDrawable(colorDrawable)
            setStatusBarDark(activity, backgroundColor)
        }
    }

    fun themeToolbar(activity: Activity, toolbar: Toolbar?, canvasContext: CanvasContext?) {
        if(toolbar == null || canvasContext == null) return
        themeToolbar(activity, toolbar, canvasContext.color, Color.WHITE, true)
    }

    fun themeToolbar(activity: Activity, toolbar: Toolbar, @ColorInt backgroundColor: Int, @ColorInt contentColor: Int) {
        themeToolbar(activity, toolbar, backgroundColor, contentColor, true)
    }

    fun themeToolbar(activity: Activity, toolbar: Toolbar, @ColorInt backgroundColor: Int, @ColorInt contentColor: Int, darkStatusBar: Boolean) {
        toolbar.setBackgroundColor(backgroundColor)
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        colorToolbarIconsAndText(activity, toolbar, contentColor)
        if(darkStatusBar) {
            setStatusBarDark(activity, backgroundColor)
        } else {
            setStatusBarLight(activity)
        }
    }

    fun themeToolbarBottomSheet(activity: Activity, isTablet: Boolean, toolbar: Toolbar, @ColorInt contentColor: Int, darkStatusBar: Boolean) {
        toolbar.setBackgroundColor(Color.WHITE)
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        colorToolbarIconsAndText(activity, toolbar, contentColor)
        if(!isTablet) {
            if (darkStatusBar) {
                setStatusBarDark(activity, Color.WHITE)
            } else {
                setStatusBarLight(activity)
            }
        }
    }

    fun themeProgressBar(progressBar: ProgressBar, @ColorInt brand: Int) {
        progressBar.indeterminateTintList = makeColorStateList(brand, brand)
    }

    fun themeCheckBox(context: Context, checkBox: AppCompatCheckBox, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.canvasEditTextColor)
        checkBox.supportButtonTintList = makeColorStateList(defaultColor, brand)
        checkBox.highlightColor = ThemePrefs.increaseAlpha(defaultColor)
    }

    fun themeFAB(fab: FloatingActionButton, @ColorInt brand: Int) {
        fab.backgroundTintList = makeColorStateList(brand, ThemePrefs.darker(brand))
        fab.setImageDrawable(ColorUtils.colorIt(Color.WHITE, fab.drawable))
    }

    fun themeButton(button: Button) {
        val drawable = button.background
        drawable.mutate().colorFilter = PorterDuffColorFilter(ThemePrefs.buttonColor, PorterDuff.Mode.SRC_ATOP)
        button.background = drawable
        button.setTextColor(ThemePrefs.buttonTextColor)
    }

    fun themeButton(button: Button, backgroundColor: Int, textColor: Int) {
        val drawable = button.background
        drawable.mutate().colorFilter = PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
        button.background = drawable
        button.setTextColor(textColor)
    }

    fun setStatusBarDark(activity: Activity, @ColorInt color: Int) {
        activity.window.statusBarColor = ThemePrefs.darker(color)
        var flags = activity.window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        activity.window.decorView.systemUiVisibility = flags
    }

    fun setStatusBarLight(activity: Activity) {
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.dimLighterGray)
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun colorImageView(imageView: ImageView, color: Int) {
        val drawable = imageView.drawable ?: return
        drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        imageView.setImageDrawable(drawable)
    }

    fun makeColorStateList(defaultColor: Int, brand: Int) = generateColorStateList(
            intArrayOf(-android.R.attr.state_enabled) to defaultColor,
            intArrayOf(android.R.attr.state_focused, -android.R.attr.state_pressed) to brand,
            intArrayOf(android.R.attr.state_focused, android.R.attr.state_pressed) to brand,
            intArrayOf(-android.R.attr.state_focused, android.R.attr.state_pressed) to brand,
            intArrayOf(android.R.attr.state_checked) to brand,
            intArrayOf() to defaultColor
    )

    fun makeColorStateListForButton() = generateColorStateList(
            intArrayOf() to ThemePrefs.buttonColor
    )

    fun makeColorStateListForRadioGroup(uncheckedColor: Int, checkedColor: Int) = generateColorStateList(
            intArrayOf(-android.R.attr.state_checked) to uncheckedColor,
            intArrayOf(android.R.attr.state_checked) to checkedColor,
            intArrayOf() to uncheckedColor
    )

    fun generateColorStateList(vararg stateColors: Pair<IntArray, Int>) = ColorStateList(
            Array(stateColors.size) { stateColors[it].first },
            Array(stateColors.size) { stateColors[it].second }.toIntArray()
    )

    fun applyKerning(src: CharSequence?, kerning: Float): Spannable? {
        if (src == null) return null
        val srcLength = src.length
        if (srcLength < 2)
            return src as? Spannable ?: SpannableString(src)

        val nonBreakingSpace = "\u00A0"
        val builder = src as? SpannableStringBuilder ?: SpannableStringBuilder(src)
        for (i in src.length - 1 downTo 1) {
            builder.insert(i, nonBreakingSpace)
            builder.setSpan(ScaleXSpan(kerning), i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return builder
    }
}

fun AppCompatCheckBox.applyTheme(@ColorInt brand: Int) {
    val defaultColor = ContextCompat.getColor(context, R.color.canvasEditTextColor)
    supportButtonTintList = ViewStyler.makeColorStateList(defaultColor, brand)
    highlightColor = ThemePrefs.increaseAlpha(defaultColor)
}

fun SwitchCompat.applyTheme(@ColorInt color: Int = ThemePrefs.brandColor) {
    ViewStyler.themeSwitch(context, this, color)
}

fun AlertDialog.Builder.showThemed() {
    val dialog = create()
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ThemePrefs.buttonColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ThemePrefs.buttonColor)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(ThemePrefs.buttonColor)
    }
    dialog.show()
}

fun BottomNavigationView.applyTheme(@ColorInt selectedColor: Int = ThemePrefs.brandColor, @ColorInt unselectedColor: Int = Color.BLACK) {
    val colorStateList = ViewStyler.makeColorStateList(unselectedColor, selectedColor)
    this.itemIconTintList = colorStateList
    this.itemTextColor = colorStateList
}
