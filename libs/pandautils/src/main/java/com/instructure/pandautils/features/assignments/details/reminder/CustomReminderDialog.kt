/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.assignments.details.reminder

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.DialogCustomReminderBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewModel
import com.instructure.pandautils.features.assignments.details.ReminderChoice
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomReminderDialog : BaseCanvasDialogFragment() {

    private lateinit var binding: DialogCustomReminderBinding
    private val parentViewModel: AssignmentDetailsViewModel by viewModels(ownerProducer = {
        requireParentFragment()
    })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCustomReminderBinding.inflate(layoutInflater, null, false)

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.customReminderTitle)
            .setPositiveButton(R.string.done) { _, _ ->
                val quantity = binding.quantity.text.toString().toIntOrNull() ?: return@setPositiveButton
                when (binding.choices.checkedRadioButtonId) {
                    R.id.minutes -> parentViewModel.onReminderSelected(ReminderChoice.Minute(quantity))
                    R.id.hours -> parentViewModel.onReminderSelected(ReminderChoice.Hour(quantity))
                    R.id.days -> parentViewModel.onReminderSelected(ReminderChoice.Day(quantity))
                    R.id.weeks -> parentViewModel.onReminderSelected(ReminderChoice.Week(quantity))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ThemePrefs.textButtonColor)
                    setupPositiveButton(getButton(AlertDialog.BUTTON_POSITIVE))
                }
            }
    }

    private fun setupPositiveButton(button: Button) {
        button.isEnabled = false
        button.setTextColor(
            ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(requireContext().getColor(R.color.textDark), ThemePrefs.textButtonColor)
            )
        )
        binding.choices.setOnCheckedChangeListener { _, _ -> updateButtonState(button) }
        binding.quantity.doAfterTextChanged { updateButtonState(button) }
    }

    private fun updateButtonState(button: Button) {
        button.isEnabled = binding.choices.checkedRadioButtonId != -1 && binding.quantity.text.toString().toIntOrNull().orDefault() > 0
    }

    companion object {
        fun newInstance() = CustomReminderDialog()
    }
}