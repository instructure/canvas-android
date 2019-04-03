/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.database.DatabaseHandler
import com.instructure.parentapp.receivers.AlarmReceiver
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.android.synthetic.main.fragment_event.*
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

class EventFragment : ParentFragment(), TimePickerFragment.TimePickerFragmentListener, TimePickerFragment.TimePickerCancelListener, DatePickerFragment.DatePickerFragmentListener, DatePickerFragment.DatePickerCancelListener {

    private var databaseHandler: DatabaseHandler? = null
    private var alarmId = -1
    private var checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var timePicker: TimePickerFragment? = null
    private var datePickerDialog: DatePickerFragment? = null
    private var setDate: Calendar = Calendar.getInstance()

    // model variables
    private var scheduleItem: ScheduleItem by ParcelableArg(key = Const.SCHEDULE_ITEM)
    private var student: User by ParcelableArg(key = Const.STUDENT)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogToolbar(view)
        initViews()
        populateViews()
        setupListeners()
        setupAlarmInfo()
    }

    override val rootLayout: Int
        get() = R.layout.fragment_event

    override fun setupDialogToolbar(rootView: View) {
        super.setupDialogToolbar(rootView)

        toolbarTitle.text = scheduleItem.title
    }

    override fun onPause() {
        super.onPause()
        eventWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        eventWebView.onResume()
    }

    private fun setupListeners() {
        checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alarmDetails.visibility = View.VISIBLE
                setDate = Calendar.getInstance()
                val cal = Calendar.getInstance()
                if (scheduleItem.startAt != null) {
                    cal.time = scheduleItem.startDate
                }

                datePickerDialog = DatePickerFragment.newInstance(this@EventFragment, this@EventFragment)

                datePickerDialog?.show(requireFragmentManager(), DATE_PICKER_EVENT_TAG)

            } else {
                alarmDetails.visibility = View.INVISIBLE
                alarmDetails.text = ""
                cancelAlarm()
            }
        }
    }

    private fun setupAlarmInfo() {
        databaseHandler = DatabaseHandler(activity)
        try {
            databaseHandler?.open()
            val alarm = databaseHandler?.getAlarmByAssignmentId(scheduleItem.id)
            if (alarm != null) {
                alarmId = databaseHandler?.getRowIdByAssignmentId(scheduleItem.id) ?: -1

                alarmDetails.visibility = View.VISIBLE
                alarmDetails.text = DateHelper.getShortDateTimeStringUniversal(context, alarm.time)
                //set the listener to null so we don't trigger the onCheckChangedListener when we set the value
                alarmSwitch.setOnCheckedChangeListener(null)
                alarmSwitch.isChecked = true
                alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)
            } else {
                alarmSwitch.isChecked = false
                alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)
            }
            databaseHandler?.close()
        } catch (e: SQLException) {
            //couldn't find the alarm in the database, so don't show that there is an alarm
            alarmSwitch.isChecked = false
            alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)
        }

    }

    private fun cancelAlarm() {
        //cancel the alarm
        val alarmReceiver = AlarmReceiver()
        var subTitle = ""
        if (scheduleItem.startAt != null) {
            subTitle = DateHelper.getDateTimeString(context, scheduleItem.startDate)
        }
        alarmReceiver.cancelAlarm(context, scheduleItem.id, scheduleItem.title, subTitle)

        //remove it from the database
        if (databaseHandler == null) {
            databaseHandler = DatabaseHandler(activity)
        }
        try {
            databaseHandler?.open()
            val id = databaseHandler?.getRowIdByAssignmentId(scheduleItem.id)
            databaseHandler?.deleteAlarm(id)
            databaseHandler?.close()
        } catch (e: SQLException) {
            //couldn't delete the alarm, so it will remain in the database. But the actual
            //alarm should have been canceled above.
        }

    }

    private fun initViews() {

        alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)

        eventWebView.addVideoClient(activity)
        eventWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                val internalWebviewFragment = InternalWebViewFragment()
                internalWebviewFragment.arguments = InternalWebViewFragment.createBundle(url, "", null, student)

                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.javaClass.name)
                ft.addToBackStack(internalWebviewFragment.javaClass.name)
                ft.commitAllowingStateLoss()
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                return true
            }
        }

        eventWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {

            }

            override fun onPageStartedCallback(webView: WebView, url: String) {

            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {

            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(null, url, student, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, student, ApiPrefs.domain, true)
            }
        }
    }

    private fun populateViews() {
        val content = scheduleItem.description

        eventWebView.visibility = View.GONE

        if (scheduleItem.isAllDay) {
            date1.text = getString(R.string.allDayEvent)
            date2.text = getFullDateString(scheduleItem.endDate)
        } else {
            //Setup the calendar event start/end times
            if (scheduleItem.startDate != null && scheduleItem.endDate != null && scheduleItem.startDate!!.time != scheduleItem.endDate!!.time) {
                //Our date times are different so we display two strings
                date1.text = getFullDateString(scheduleItem.endDate)
                val startTime = DateHelper.getFormattedTime(context, scheduleItem.startDate)
                val endTime = DateHelper.getFormattedTime(context, scheduleItem.endDate)
                date2.text = "$startTime - $endTime"
            } else {
                date1.text = getFullDateString(scheduleItem.startDate)
                date2.visibility = View.INVISIBLE
            }
        }

        val noLocationTitle = TextUtils.isEmpty(scheduleItem.locationName)
        val noLocation = TextUtils.isEmpty(scheduleItem.locationAddress)

        if (noLocation && noLocationTitle) {
            address1.text = getString(R.string.noLocation)
            address2.visibility = View.INVISIBLE
        } else {
            if (noLocationTitle) {
                address1.text = scheduleItem.locationAddress
            } else {
                address1.text = scheduleItem.locationName
                address2.text = scheduleItem.locationAddress
            }
        }

        if (!TextUtils.isEmpty(content)) {
            eventWebView.visibility = View.VISIBLE
            if (resources.getBoolean(R.bool.isTablet)) {
                eventWebView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                eventWebView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.canvasBackgroundLight))
            }
            eventWebView.formatHTML(content, scheduleItem.title)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CallBack
    ///////////////////////////////////////////////////////////////////////////

    private fun getFullDateString(date: Date?): String {
        if (date == null) {
            return ""
        }

        val dateFormat = SimpleDateFormat("EEEE,")
        val dayOfWeek = dateFormat.format(date)
        val dateString = DateHelper.getFormattedDate(context, date)

        return "$dayOfWeek $dateString"
    }

    override fun onCancel() {
        alarmSwitch.isChecked = false
    }


    override fun onDateSet(year: Int, month: Int, day: Int) {
        setDate.set(Calendar.YEAR, year)
        setDate.set(Calendar.MONTH, month)
        setDate.set(Calendar.DAY_OF_MONTH, day)

        datePickerDialog?.dismiss()

        timePicker = TimePickerFragment.newInstance(this, this)

        timePicker!!.isCancelable = false

        timePicker!!.show(requireFragmentManager(), TIME_PICKER_EVENT_TAG)
    }


    override fun onTimeSet(hourOfDay: Int, minute: Int) {

        setDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
        setDate.set(Calendar.MINUTE, minute)

        alarmDetails.text = DateHelper.getShortDateTimeStringUniversal(context, setDate.time)
        //save/update the alarm information
        try {
            databaseHandler?.open()

            val subTitle = if (scheduleItem.startAt != null) {
                DateHelper.getShortDateTimeStringUniversal(context, scheduleItem.startDate)
            } else {
                ""
            }

            databaseHandler?.createAlarm(setDate.get(Calendar.YEAR), setDate.get(Calendar.MONTH), setDate.get(Calendar.DAY_OF_MONTH), setDate.get(Calendar.HOUR_OF_DAY), setDate.get(Calendar.MINUTE), scheduleItem.id, scheduleItem.title, subTitle)

            databaseHandler?.close()

        } catch (e: SQLException) {
            //couldn't save the alarm in the database, so stop here and don't actually create one. If the database
            //doesn't have the alarm in it, the user will think that it didn't save
            Toast.makeText(context, getString(R.string.alarmNotSet), Toast.LENGTH_SHORT).show()
            timePicker?.dismiss()
            return
        }

        val alarmReceiver = AlarmReceiver()
        val subTitle = if (scheduleItem.startAt != null) {
            DateHelper.getShortDateTimeStringUniversal(context, scheduleItem.startDate)
        } else {
            ""
        }

        alarmReceiver.setAlarm(context, setDate, scheduleItem.id, scheduleItem.title, subTitle)

        AnalyticUtils.trackButtonPressed(AnalyticUtils.REMINDER_EVENT)

        timePicker?.dismiss()
    }

    companion object {

        private const val DATE_PICKER_EVENT_TAG = "datePickerEvent"
        private const val TIME_PICKER_EVENT_TAG = "timePickerEvent"

        fun newInstance(scheduleItem: ScheduleItem, student: User): EventFragment {
            val args = Bundle()
            args.putParcelable(Const.SCHEDULE_ITEM, scheduleItem)
            args.putParcelable(Const.STUDENT, student)
            val fragment = EventFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
