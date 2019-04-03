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
package com.instructure.student.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.events.CalendarEventCreated
import com.instructure.student.events.post
import kotlinx.android.synthetic.main.create_calendar_event_layout.*
import java.net.URLEncoder
import java.util.*

class CreateCalendarEventFragment : ParentFragment(), TimePickerFragment.TimePickerCancelListener {

    private var defaultTime by LongArg(System.currentTimeMillis(), Const.CALENDAR_EVENT_START_DATE)

    private var saveJob: WeaveJob? = null

    private val startCalendar by lazy {
        GregorianCalendar().apply {
            timeInMillis = defaultTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private val endCalendar by lazy {
        GregorianCalendar().apply {
            timeInMillis = defaultTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    override fun title(): String = getString(R.string.newEvent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTablet) setStyle(STYLE_NORMAL, R.style.LightStatusBarDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_create_calendar_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        setUpListeners()
        applyTheme()
    }

    override fun onDestroy() {
        saveJob?.cancel()
        super.onDestroy()
    }

    override fun applyTheme() {
        toolbar.title = title()
        setupToolbarMenu(toolbar, R.menu.menu_save_generic)
        toolbar.setupAsCloseButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSave -> {
                saveData()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (!isTablet) dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun initViews() {
        // Set the date to the current day
        eventDateText.text = getFullDateString(startCalendar.time)
        eventStartTimeText.text = DateHelper.getDayHourDateString(requireContext(), startCalendar.time)
        eventEndTimeText.text = DateHelper.getDayHourDateString(requireContext(), endCalendar.time)
    }

    private fun setUpListeners() {
        eventDateText.onClick {
            DatePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, null) { year, month, dayOfMonth ->
                startCalendar.set(year, month, dayOfMonth)
                endCalendar.set(year, month, dayOfMonth)
                eventDateText.text = getFullDateString(startCalendar.time)
            }.show(requireActivity().supportFragmentManager, TAG_DATE_PICKER)
        }

        eventStartTimeText.onClick {
            TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, startCalendar.time) { hour, minute ->
                startCalendar.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                eventStartTimeText.text = DateHelper.getDayHourDateString(requireContext(), startCalendar.time)
                if (startCalendar.after(endCalendar)) {
                    // Calendar is either equal or after, set end time = to start time.
                    endCalendar.timeInMillis = startCalendar.timeInMillis
                    eventEndTimeText.text = DateHelper.getDayHourDateString(requireContext(), endCalendar.time)
                }
            }.show(requireActivity().supportFragmentManager, TAG_TIME_PICKER_START)
        }

        eventEndTimeText.onClick {
            TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, endCalendar.time) { hour, minute ->
                endCalendar.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                eventEndTimeText.text = DateHelper.getDayHourDateString(requireContext(), endCalendar.time)
                if (endCalendar.before(startCalendar)) {
                    // Calendar is either equal or before start time, set start time = to end time
                    startCalendar.timeInMillis = endCalendar.timeInMillis
                    eventStartTimeText.text = DateHelper.getDayHourDateString(requireContext(), startCalendar.time)
                }
            }.show(requireActivity().supportFragmentManager, TAG_TIME_PICKER_END)
        }
    }

    @Suppress("DEPRECATION")
    private fun saveData() {
        saveJob = tryWeave {
            val item: ScheduleItem = awaitApi {
                val description: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(eventNoteText.text.toString(), Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    Html.fromHtml(eventNoteText.text.toString()).toString()
                }
                CalendarEventManager.createCalendarEvent(
                    ApiPrefs.user!!.contextId,
                    URLEncoder.encode(titleEditText.text.toString(), "UTF-8"),
                    URLEncoder.encode(description, "UTF-8"),
                    startCalendar.time.toApiString()!!,
                    endCalendar.time.toApiString()!!,
                    URLEncoder.encode(locationEditText.text.toString(), "UTF-8"),
                    it
                )
            }
            CalendarEventCreated(item).post()
            showToast(R.string.eventSuccessfulCreation)
            requireActivity().onBackPressed()
        } catch {
            toast(R.string.errorOccurred)
        }
    }

    private fun getFullDateString(date: Date?): String {
        if (date == null) return ""
        return "${DateHelper.getFullDayFormat().format(date)} ${DateHelper.getFormattedDate(requireContext(), date)}"
    }

    override fun onCancel() {}

    companion object {

        const val TAG_DATE_PICKER = "datePicker"
        const val TAG_TIME_PICKER_START = "timePickerStart"
        const val TAG_TIME_PICKER_END = "timePickerEnd"

        @JvmStatic
        fun makeRoute(time: Long): Route {
            val bundle = Bundle().apply { putLong(Const.CALENDAR_EVENT_START_DATE, time) }
            return Route(CreateCalendarEventFragment::class.java, null, bundle)
        }

        private fun validateBundle(route: Route): Boolean {
            return route.arguments.containsKey(Const.CALENDAR_EVENT_START_DATE)
        }

        fun newInstance(route: Route): CreateCalendarEventFragment? {
            if (!validateBundle(route)) return null
            return CreateCalendarEventFragment().withArgs(route.argsWithContext)
        }
    }
}
