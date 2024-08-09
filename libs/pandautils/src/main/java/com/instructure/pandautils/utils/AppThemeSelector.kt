package com.instructure.pandautils.utils

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.instructure.pandautils.R

object AppThemeSelector {

    fun showAppThemeSelectorDialog(context: Context, appThemeStatusText: TextView) {
        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.selectAppTheme)
        val appThemes = AppTheme.values().map { context.getString(it.themeNameRes) }.toTypedArray()

        val currentAppTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        builder.setSingleChoiceItems(appThemes, currentAppTheme.ordinal) { dialog, itemIndex ->
            val newAppTheme = AppTheme.fromIndex(itemIndex)
            appThemeStatusText.setText(newAppTheme.themeNameRes)
            setAppTheme(newAppTheme, dialog, context)
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun showAppThemeSelectorDialog(context: Context, onThemeChanged: (AppTheme) -> Unit) {
        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.selectAppTheme)
        val appThemes = AppTheme.values().map { context.getString(it.themeNameRes) }.toTypedArray()

        val currentAppTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        builder.setSingleChoiceItems(appThemes, currentAppTheme.ordinal) { dialog, itemIndex ->
            val newAppTheme = AppTheme.fromIndex(itemIndex)
            setAppTheme(newAppTheme, dialog, context)
            onThemeChanged(newAppTheme)
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setAppTheme(appTheme: AppTheme, dialog: DialogInterface, context: Context) {
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
        ThemePrefs.appTheme = appTheme.ordinal

        val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        ThemePrefs.isThemeApplied = false

        dialog.dismiss()
    }
}

enum class AppTheme(@StringRes val themeNameRes: Int, val nightModeType: Int) {
    LIGHT(R.string.appThemeLight, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.string.appThemeDark, AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(R.string.appThemeSystem, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {
        fun fromIndex(index: Int) = values()[index]
    }
}