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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.analytics.SCREEN_VIEW_COLOR_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.DialogColorPickerBinding
import com.instructure.teacher.utils.getColorCompat
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_COLOR_PICKER)
class ColorPickerDialog: AppCompatDialogFragment() {

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
        val currentColor = course.backgroundColor
        listOf(
            binding.colorCottonCandy to R.color.colorCottonCandy,
            binding.colorBarbie to R.color.colorBarbie,
            binding.colorBarneyPurple to R.color.colorBarneyPurple,
            binding.colorEggplant to R.color.colorEggplant,
            binding.colorUltramarine to R.color.colorUltramarine,
            binding.colorOcean11 to R.color.colorOcean11,
            binding.colorCyan to R.color.colorCyan,
            binding.colorAquaMarine to R.color.colorAquaMarine,
            binding.colorEmeraldGreen to R.color.colorEmeraldGreen,
            binding.colorFreshCutLawn to R.color.colorFreshCutLawn,
            binding.colorChartreuse to R.color.colorChartreuse,
            binding.colorSunFlower to R.color.colorSunFlower,
            binding.colorTangerine to R.color.colorTangerine,
            binding.colorBloodOrange to R.color.colorBloodOrange,
            binding.colorSriracha to R.color.colorSriracha
        ).map { it.first to requireContext().getColorCompat(it.second) }
            .onEach { (view, color) ->
                ColorUtils.colorIt(color, view.circle)
                view.onClick {
                    callback(color)
                    dismiss()
                }
                if (color == currentColor) view.setSelected()
            }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
