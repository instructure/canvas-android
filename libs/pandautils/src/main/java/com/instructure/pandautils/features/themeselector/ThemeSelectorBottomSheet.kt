/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.themeselector

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.CompoundButtonCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instructure.pandautils.R
import com.instructure.pandautils.base.BaseCanvasBottomSheetDialogFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.BottomSheetThemeSelectorBinding
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick

class ThemeSelectorBottomSheet : BaseCanvasBottomSheetDialogFragment() {

    private val binding by viewBinding(BottomSheetThemeSelectorBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_theme_selector, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val radioButtonColor = ViewStyler.makeColorStateListForRadioGroup(requireContext().getColor(R.color.textDarkest), requireContext().getColor(R.color.textInfo))
        CompoundButtonCompat.setButtonTintList(buttonLightTheme, radioButtonColor)
        CompoundButtonCompat.setButtonTintList(buttonDarkTheme, radioButtonColor)
        CompoundButtonCompat.setButtonTintList(buttonDeviceTheme, radioButtonColor)

        saveButton.onClick {
            val appTheme = when {
                buttonLightTheme.isChecked -> AppTheme.LIGHT
                buttonDarkTheme.isChecked -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
            setAppTheme(appTheme)
        }

        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setAppTheme(appTheme: AppTheme) {
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
        ThemePrefs.appTheme = appTheme.ordinal

        val nightModeFlags: Int = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        ThemePrefs.isThemeApplied = false

        dismiss()
    }

}