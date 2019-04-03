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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.CompoundButton
import android.widget.Toast
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.database.DatabaseHandler
import com.instructure.parentapp.receivers.AlarmReceiver
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.RouteMatcher
import com.instructure.parentapp.util.ViewUtils
import kotlinx.android.synthetic.main.fragment_assignment.*
import java.sql.SQLException
import java.util.*

class AssignmentFragment : ParentFragment(), TimePickerFragment.TimePickerFragmentListener, TimePickerFragment.TimePickerCancelListener, DatePickerFragment.DatePickerFragmentListener, DatePickerFragment.DatePickerCancelListener {

    private var course by NullableParcelableArg<Course>(key = Const.COURSE)
    private var assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private var student by ParcelableArg<User>(key = Const.STUDENT)
    private var databaseHandler: DatabaseHandler? = null
    private var alarmId = -1
    private var checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    // Views
    private var timePicker: TimePickerFragment? = null
    private var datePickerDialog: DatePickerFragment? = null
    private var setDate: Calendar? = null

    private var submissionJob: WeaveJob? = null

    override val rootLayout: Int
        get() = R.layout.fragment_assignment

    override fun onPause() {
        super.onPause()
        assignmentWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        assignmentWebView.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        setupDialogToolbar(view)

        setupAlarmInfo()

        setupGradeAndStatus()
        if (assignment.submission == null) {
            getSubmission()
        }
    }

    private fun getSubmission() {
        submissionJob = tryWeave {
            assignment.submission = awaitApi<Submission> {
                SubmissionManager.getSingleSubmission(
                        course!!.id,
                        assignment.id,
                        student.id,
                        it,
                        true)
            }
            setupGradeAndStatus()
        } catch  {

        }
    }

    private fun setupAlarmInfo() {
        databaseHandler = DatabaseHandler(activity)
        try {
            databaseHandler?.open()
            val alarm = databaseHandler?.getAlarmByAssignmentId(assignment.id)
            if (alarm != null) {
                alarmId = databaseHandler?.getRowIdByAssignmentId(assignment.id) ?: -1

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
            // Couldn't find the alarm in the database, so don't show that there is an alarm
            alarmSwitch.isChecked = false
            alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)
        }

    }

    private fun cancelAlarm() {
        // Cancel the alarm
        val alarmReceiver = AlarmReceiver()
        var subTitle = ""
        if (assignment.dueAt != null) {
            subTitle = getString(R.string.due) + " " + DateHelper.getDateTimeString(context, assignment.dueDate)
        }
        alarmReceiver.cancelAlarm(context, assignment.id, assignment.name, subTitle)

        // Remove it from the database
        if (databaseHandler == null) {
            databaseHandler = DatabaseHandler(activity)
        }
        try {
            databaseHandler?.open()
            val id = databaseHandler?.getRowIdByAssignmentId(assignment.id)
            databaseHandler?.deleteAlarm(id)
            databaseHandler?.close()
        } catch (e: SQLException) {
            // Couldn't delete the alarm, so it will remain in the database. But the actual
            // alarm should have been canceled above.
        }

    }

    private fun setupViews() {

        assignmentName.text = assignment.name
        if (assignment.dueAt != null) {
            dueDate.text = getString(R.string.due) + " " + DateHelper.getDateTimeString(activity, assignment.dueDate)
        }
        courseName.text = course?.name

        alarmSwitch.setOnCheckedChangeListener(checkedChangeListener)

        assignmentWebView.addVideoClient(activity)

        assignmentWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                // Create and add the InternalWebviewFragment to deal with the link they clicked
                val internalWebviewFragment = InternalWebViewFragment()
                internalWebviewFragment.arguments = InternalWebViewFragment.createBundle(url, "", null, student)

                val ft = requireFragmentManager().beginTransaction()
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.javaClass.name)
                ft.addToBackStack(internalWebviewFragment.javaClass.name)
                ft.commitAllowingStateLoss()
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                return true
            }
        }

        assignmentWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
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

        //assignment description can be null
        var description: String?
        description = when {
            assignment.isLocked -> getLockedInfoHTML(assignment.lockInfo!!, requireContext(), R.string.locked_assignment_desc)
            assignment.lockDate != null && assignment.lockDate!!.before(Calendar.getInstance(Locale.getDefault()).time) ->
                // If an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
                // but no lock info because it isn't locked as part of a module
                assignment.lockExplanation
            else -> assignment.description
        }

        if (description == null || description == "null" || description == "") {
            description = getString(R.string.no_description)
        }

        assignmentWebView.formatHTML(description, assignment.name)

    }

    private fun getLockedInfoHTML(lockInfo: LockInfo, context: Context, explanationFirstLine: Int): String {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        // Get the locked message and make the module name bold
        var lockedMessage = ""

        if (lockInfo.lockedModuleName != null) {
            lockedMessage = "<p>" + String.format(getString(explanationFirstLine), "<b>" + lockInfo.lockedModuleName + "</b>") + "</p>"
        }
        if (lockInfo.modulePrerequisiteNames!!.size > 0) {
            // We only want to add this text if there are module completion requirements
            lockedMessage += getString(R.string.mustComplete) + "<ul>"
            for (i in 0 until lockInfo.modulePrerequisiteNames!!.size) {
                lockedMessage += "<li>" + lockInfo.modulePrerequisiteNames!![i] + "</li>"  //"&#8226; "
            }
            lockedMessage += "</ul>"
        }

        // Check to see if there is an unlocked date
        if (lockInfo.unlockAt != null && lockInfo.unlockDate!!.after(Date())) {
            val unlocked = DateHelper.getDateTimeString(context, lockInfo.unlockDate)
            //If there is an unlock date but no module then the assignment is locked
            if (lockInfo.contextModule == null) {
                lockedMessage = "<p>" + getString(R.string.locked_assignment_not_module) + "</p>"
            }
            lockedMessage += getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>"
        }

        return lockedMessage
    }

    private fun setupListeners() {
        checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alarmDetails.visibility = View.VISIBLE
                setDate = Calendar.getInstance()
                val cal = Calendar.getInstance()
                if (assignment.dueAt != null) {
                    cal.time = assignment.dueDate
                }

                datePickerDialog = DatePickerFragment.newInstance(this@AssignmentFragment, this@AssignmentFragment)

                datePickerDialog?.show(requireFragmentManager(), DATE_PICKER_TAG)

            } else {
                alarmDetails.visibility = View.INVISIBLE
                alarmDetails.text = ""
                cancelAlarm()
            }
        }
    }

    override fun onCancel() {
        alarmSwitch.isChecked = false
    }


    override fun onDateSet(year: Int, month: Int, day: Int) {
        setDate?.set(Calendar.YEAR, year)
        setDate?.set(Calendar.MONTH, month)
        setDate?.set(Calendar.DAY_OF_MONTH, day)

        datePickerDialog?.dismiss()
        timePicker = TimePickerFragment.newInstance(this, this)

        timePicker?.isCancelable = false

        timePicker?.show(requireFragmentManager(), TIME_PICKER_TAG)
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int) {

        setDate?.set(Calendar.HOUR_OF_DAY, hourOfDay)
        setDate?.set(Calendar.MINUTE, minute)

        alarmDetails.text = DateHelper.getShortDateTimeStringUniversal(context, setDate?.time)
        // Save/update the alarm information
        try {
            databaseHandler?.open()

            val subTitle = if (assignment.dueAt != null) {
                getString(R.string.due) + " " + DateHelper.getShortDateTimeStringUniversal(context, assignment.dueDate)
            } else {
                getString(R.string.no_due_date)
            }

            databaseHandler?.createAlarm(setDate!!.get(Calendar.YEAR), setDate!!.get(Calendar.MONTH), setDate!!.get(Calendar.DAY_OF_MONTH), setDate!!.get(Calendar.HOUR_OF_DAY), setDate!!.get(Calendar.MINUTE), assignment.id, assignment.name, subTitle)

            databaseHandler?.close()

        } catch (e: SQLException) {
            //couldn't save the alarm in the database, so stop here and don't actually create one. If the database
            //doesn't have the alarm in it, the user will think that it didn't save
            Toast.makeText(context, getString(R.string.alarmNotSet), Toast.LENGTH_SHORT).show()
            timePicker?.dismiss()
            return
        }

        val alarmReceiver = AlarmReceiver()

        val subTitle = if (assignment.dueAt != null) {
            getString(R.string.due) + " " + DateHelper.getShortDateTimeStringUniversal(context, assignment.dueDate)
        } else {
            getString(R.string.no_due_date)
        }

        alarmReceiver.setAlarm(context, setDate, assignment.id, assignment.name, subTitle)

        AnalyticUtils.trackButtonPressed(AnalyticUtils.REMINDER_ASSIGNMENT)

        timePicker?.dismiss()
    }

    private fun setupGradeAndStatus() {

        val assignmentState = AssignmentUtils2.getAssignmentState(assignment, assignment.submission)

        when (assignmentState) {
            AssignmentUtils2.ASSIGNMENT_STATE_MISSING -> {
                grade.visibility = View.GONE
                status.setBackgroundResource(R.drawable.submission_missing_background)
                status.text = getString(R.string.missing)
                status.visibility = View.VISIBLE
                grade.visibility = View.VISIBLE
                grade.text = (getString(R.string.grade) + " "
                        + ViewUtils.getPointsPossibleMissing(assignment.pointsPossible))
            }
            AssignmentUtils2.ASSIGNMENT_STATE_GRADED -> {
                grade.visibility = View.VISIBLE
                grade.text = (getString(R.string.grade) + " "
                        + ViewUtils.getPercentGradeForm(assignment.submission!!.score,
                        assignment.pointsPossible) + " "
                        + ViewUtils.getPointsGradeForm(assignment.submission!!.score,
                        assignment.pointsPossible))
                status.setBackgroundResource(R.drawable.assignment_submitted_background)
                status.text = getString(R.string.submitted)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED -> {
                grade.visibility = View.VISIBLE
                grade.text = getString(R.string.grade) + " " + getString(R.string.notGraded)
                status.setBackgroundResource(R.drawable.assignment_submitted_background)
                status.text = getString(R.string.submitted)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE -> {
                grade.visibility = View.VISIBLE
                grade.text = (getString(R.string.grade) + " "
                        + ViewUtils.getPercentGradeForm(assignment.submission!!.score,
                        assignment.pointsPossible) + " "
                        + ViewUtils.getPointsGradeForm(assignment.submission!!.score,
                        assignment.pointsPossible))
                status.setBackgroundResource(R.drawable.late_assignment_background)
                status.text = getString(R.string.late)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING -> {
                grade.visibility = View.VISIBLE
                grade.text = (getString(R.string.grade) + " "
                        + ViewUtils.getPercentGradeForm(assignment.submission!!.score,
                    assignment.pointsPossible) + " "
                        + ViewUtils.getPointsGradeForm(assignment.submission!!.score,
                    assignment.pointsPossible))
                status.setBackgroundResource(R.drawable.submission_missing_background)
                status.text = getString(R.string.missing)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE -> {
                grade.visibility = View.VISIBLE
                grade.text = getString(R.string.grade) + " " + getString(R.string.notGraded)
                status.setBackgroundResource(R.drawable.late_assignment_background)
                status.text = getString(R.string.late)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED -> {
                grade.visibility = View.GONE
                status.setBackgroundResource(R.drawable.assignment_submitted_background)
                status.text = getString(R.string.excused)
                status.visibility = View.VISIBLE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_DUE -> {
                grade.visibility = View.GONE
                status.visibility = View.GONE
            }
            AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS -> {
                grade.visibility = View.VISIBLE
                grade.text = getString(R.string.grade) + " " + getString(R.string.notGraded)
                status.setBackgroundResource(R.drawable.assignment_in_class_background)
                status.text = getString(R.string.in_class)
                status.visibility = View.VISIBLE
            }
        }
    }

    companion object {

        private const val DATE_PICKER_TAG = "datePicker"
        private const val TIME_PICKER_TAG = "timePicker"

        fun newInstance(assignment: Assignment, course: Course, student: User): AssignmentFragment {
            val args = Bundle()
            args.putParcelable(Const.ASSIGNMENT, assignment)
            args.putParcelable(Const.COURSE, course)
            args.putParcelable(Const.STUDENT, student)
            val fragment = AssignmentFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
