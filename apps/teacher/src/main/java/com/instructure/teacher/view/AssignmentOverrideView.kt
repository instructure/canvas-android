/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.utils.formatOrDoubleDash
import kotlinx.android.synthetic.main.view_assignment_override.view.*
import java.util.*
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.view_assignment_override.view.dueDate as dueDateText

class AssignmentOverrideView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mDueDateGroup: DueDateGroup by Delegates.notNull()
    private val mDateFormat = DateHelper.getFullMonthNoLeadingZeroDateFormat()
    private val mTimeFormat by lazy { DateHelper.getPreferredTimeFormat(context) }

    // Default time to use if none is set; 11:59pm
    private var mDefaultTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }
    // Default date to use if none is set; Defaults to today's date
    private var mDefaultDate = Calendar.getInstance().apply { time = Date()}

    // Should only show remove if there is more than one assignment override
    private var mShowRemove = true

    init {
        LayoutInflater.from(context).inflate(R.layout.view_assignment_override, this, true)
    }

    /**
     * If we just want the Available From and To Fields (Currently used when creating discussions)
     *
     */
    fun toAndFromDatesOnly() {
        dueDateTextInput.setGone()
        dueTimeTextInput.setGone()
        assignToTextInput.setGone()
    }

    /**
     *
     * @param dueDateGroup A DueDateGroup with the dates we need to populate this view
     * @param showRemove Whether or not the 'Remove' button should show (only if there's more than one override)
     * @param assigneesList List of assignees
     * @param datePickerClickListener Listener for when a date is picked
     * @param timePickerClickListener Listener for when a time is picked
     * @param removeOverrideClickListener Listener for when the 'Remove' button is clicked; Returns true if we should show the 'Remove' button, false otherwise
     * @param assigneeClickListener Listener for when assignees has changed
     */
    fun setupOverride(
            index: Int,
            dueDateGroup: DueDateGroup,
            showRemove: Boolean,
            assigneesList: List<String>,
            datePickerClickListener: (date: Date?, (Int, Int, Int) -> Unit) -> Unit,
            timePickerClickListener: (date: Date?, (Int, Int) -> Unit) -> Unit,
            removeOverrideClickListener: (DueDateGroup) -> Unit,
            assigneeClickListener: () -> Unit
    ) {

        mDueDateGroup = dueDateGroup
        mShowRemove = showRemove

        // Setup views
        assignTo.setText(if (assigneesList.isNotEmpty()) assigneesList.joinToString() else " ")

        dueDateGroup.let {
            with(it.coreDates) {
                dueDateText.setText(mDateFormat.formatOrDoubleDash(dueDate))
                dueTime.setText(mTimeFormat.formatOrDoubleDash(dueDate))
                toDate.setText(mDateFormat.formatOrDoubleDash(lockDate))
                toTime.setText(mTimeFormat.formatOrDoubleDash(lockDate))
                fromDate.setText(mDateFormat.formatOrDoubleDash(unlockDate))
                fromTime.setText(mTimeFormat.formatOrDoubleDash(unlockDate))
            }
        }

        assignTo.setOnClickListener {
            assigneeClickListener()
            clearAssigneeError()
        }

        // Apply theming
        val textList = arrayOf(dueDateText, dueTime, toDate, toTime, fromDate, fromTime, assignTo)
        textList.forEach {
            ViewStyler.themeEditText(context, it, ThemePrefs.brandColor)
            // Prevent user from long clicking
            it.setOnLongClickListener { true }
            // Don't show cursor
            it.isCursorVisible = false
        }

        dueDateText.setOnClickListener {
            datePickerClickListener(dueDateGroup.coreDates.dueDate) { year, month, dayOfMonth ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupDateCalendar(year, month, dayOfMonth, dueDate)
                    dueDateText.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    dueTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    dueDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        dueTime.setOnClickListener {
            timePickerClickListener(dueDateGroup.coreDates.dueDate) { hour, min ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupTimeCalendar(hour, min, dueDate)
                    dueTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    dueDateText.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    dueDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        toDate.setOnClickListener {
            datePickerClickListener(dueDateGroup.coreDates.lockDate) { year, month, dayOfMonth ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupDateCalendar(year, month, dayOfMonth, lockDate)
                    toDate.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    toTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    lockDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        toTime.setOnClickListener {
            timePickerClickListener(dueDateGroup.coreDates.lockDate) { hour, min ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupTimeCalendar(hour, min, lockDate)
                    toTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    toDate.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    lockDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        fromDate.setOnClickListener {
            datePickerClickListener(dueDateGroup.coreDates.unlockDate) { year, month, dayOfMonth ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupDateCalendar(year, month, dayOfMonth, unlockDate)
                    fromDate.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    fromTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    unlockDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        fromTime.setOnClickListener {
            timePickerClickListener(dueDateGroup.coreDates.unlockDate) { hour, min ->
                with(dueDateGroup.coreDates) {
                    val updatedDate = setupTimeCalendar(hour, min, unlockDate)
                    fromTime.setText(mTimeFormat.formatOrDoubleDash(updatedDate))
                    fromDate.setText(mDateFormat.formatOrDoubleDash(updatedDate))
                    unlockDate = updatedDate
                }
            }
            clearDateTimeErrors()
        }

        if (!showRemove)
            removeOverride.setGone()

        if (BuildConfig.IS_TESTING) removeOverride.contentDescription = "remove_override_button_$index"

        removeOverride.setOnClickListener {
            removeOverrideClickListener(dueDateGroup)
        }
    }

    fun validateInput(): Boolean {
        var saveError = false
        // Check
        // unlock date <= due date <= lock date

        // unlock date cannot be after due date
        // unlock date cannot be after lock date
        with(mDueDateGroup.coreDates) {
            //            unlockDate ?: 0 + ?: 0
            dueDate?.let {
                if (unlockDate?.after(it) ?: false) {
                    // Error: Unlock date cannot be after due date
                    saveError = true
                    // Set error on textview
                    fromDateTextInput.isErrorEnabled = true
                    fromDateTextInput.error = context.getString(R.string.unlock_after_due_date_error)
                }

                if (lockDate?.before(it) ?: false) {
                    // Error: Lock date cannot be before due date
                    saveError = true
                    toDateTextInput.isErrorEnabled = true
                    toDateTextInput.error = context.getString(R.string.lock_before_due_date_error)
                }
            }
            unlockDate?.let {
                if (lockDate?.before(it) ?: false) {
                    // Error: Lock date cannot be before unlock date
                    saveError = true
                    toDateTextInput.isErrorEnabled = true
                    toDateTextInput.error = context.getString(R.string.lock_after_unlock_error)
                }
            }
        }

        // Make sure someone is assigned to this assignment
        if (!mDueDateGroup.hasOverrideAssignees && !mDueDateGroup.isEveryone) {
            assignToTextInput.error = context.getString(R.string.assignee_blank_error)
            saveError = true
        }

        return saveError
    }

    private fun clearDateTimeErrors() {
        val textInputList = arrayOf(toDateTextInput, toTimeTextInput, fromDateTextInput, fromTimeTextInput, dueDateTextInput, dueTimeTextInput)
        textInputList.forEach {
            it.error = null
            it.isErrorEnabled = false
        }
    }

    private fun clearAssigneeError() {
        assignToTextInput.error = null
        assignToTextInput.isErrorEnabled = false
    }

    /**
     * Handles time changes to the Date passed in. Sets a default date if there is none set already.
     *
     * @return The resulting date after updating it with the hour and minute
     */
    private fun setupTimeCalendar(hour: Int, min: Int, date: Date?): Date {
        return Calendar.getInstance().apply { time = date ?: mDefaultDate.time; set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, min) }.time
    }

    /**
     * Handles time changes to the Date passed in. Sets a default time if there is none set already.
     *
     * @return The resulting date after updating it with the new year, month and day
     */
    private fun setupDateCalendar(year: Int, month: Int, dayOfMonth: Int, date: Date?): Date {
        return Calendar.getInstance().apply { time = date ?: mDefaultTime.time; set(year, month, dayOfMonth) }.time
    }
}
