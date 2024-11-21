/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.pandautils.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_COLOR_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.DialogColorPickerBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.views.ColorPickerIcon
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_COLOR_PICKER)
class ColorPickerDialog: BaseCanvasAppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var callback: (Int) -> Unit by Delegates.notNull()
    private var course by ParcelableArg<Course>(key = Const.COURSE)

    companion object {
        fun newInstance(manager: FragmentManager, course: Course, callback: (Int) -> Unit): ColorPickerDialog {
            manager.dismissExisting<ColorPickerDialog>()
            val dialog = ColorPickerDialog()
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            dialog.arguments = args
            dialog.callback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val binding = DialogColorPickerBinding.inflate(LayoutInflater.from(ContextThemeWrapper(requireActivity(), 0)))
        setupViews(binding)
        builder.setView(binding.root)
        builder.setTitle(R.string.colorPickerDialogTitle)
        builder.setCancelable(true)
        return builder.create()
    }

    fun setupViews(binding: DialogColorPickerBinding) {
        val currentColor = course.color
        listOf(
            ColorPickerItem(binding.courseColor1, R.color.courseColor1, R.color.courseColor1light),
            ColorPickerItem(binding.courseColor2, R.color.courseColor2, R.color.courseColor2light),
            ColorPickerItem(binding.courseColor3, R.color.courseColor3, R.color.courseColor3light),
            ColorPickerItem(binding.courseColor4, R.color.courseColor4, R.color.courseColor4light),
            ColorPickerItem(binding.courseColor5, R.color.courseColor5, R.color.courseColor5light),
            ColorPickerItem(binding.courseColor6, R.color.courseColor6, R.color.courseColor6light),
            ColorPickerItem(binding.courseColor7, R.color.courseColor7, R.color.courseColor7light),
            ColorPickerItem(binding.courseColor8, R.color.courseColor8, R.color.courseColor8light),
            ColorPickerItem(binding.courseColor9, R.color.courseColor9, R.color.courseColor9light),
            ColorPickerItem(binding.courseColor10, R.color.courseColor10, R.color.courseColor10light),
            ColorPickerItem(binding.courseColor11, R.color.courseColor11, R.color.courseColor11light),
            ColorPickerItem(binding.courseColor12, R.color.courseColor12, R.color.courseColor12light)
        ).map { it.copy(displayColor = requireContext().getColor(it.displayColor), lightColor = requireContext().getColor(it.lightColor)) }
            .onEach { colorPickerItem ->
                ColorUtils.colorIt(colorPickerItem.displayColor, colorPickerItem.view.circle)
                colorPickerItem.view.onClick {
                    callback(colorPickerItem.lightColor)
                    dismiss()
                }
                if (colorPickerItem.displayColor == currentColor) colorPickerItem.view.setSelected()
            }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}

data class ColorPickerItem(val view: ColorPickerIcon, val displayColor: Int, val lightColor: Int)
