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
package com.instructure.parentapp.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.CompoundButton
import android.widget.Toast
import com.instructure.canvasapi2.managers.AlertThresholdManager
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.ObserverAlertThreshold
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.R
import com.instructure.parentapp.dialogs.StudentThresholdDialog
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.StringUtilities
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_student_details.*

import java.util.ArrayList

import kotlinx.android.synthetic.main.view_student_settings_details.*
import okhttp3.ResponseBody

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class StudentDetailsActivity : BaseParentActivity(), StudentThresholdDialog.StudentThresholdChanged {

    private val alertThresholds = ArrayList<ObserverAlertThreshold>()

    private var assignmentMissingCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var teacherAnnouncementsCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var institutionAnnouncementsCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    private var hasGradeAboveThreshold = false
    private var hasGradeBelowThreshold = false
    private var hasAssignmentMissingThreshold = false
    private var hasAssignmentGradeAboveThreshold = false
    private var hasAssignmentGradeBelowThreshold = false
    private var hasTeacherAnnouncementsThreshold = false
    private var hasInstitutionAnnouncementsThreshold = false

    private val student: User by lazy { intent.extras.getParcelable<User>(Const.STUDENT) }

    private var thresholdJob: WeaveJob? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_student_details)
        setupViews()

        thresholdJob?.cancel()
        thresholdJob = tryWeave {
            alertThresholds.clear()
            alertThresholds.addAll(awaitApi<List<ObserverAlertThreshold>> { AlertThresholdManager.getObserverAlertThresholds(student.id, it) })
            updateThresholds()
        } catch { }
    }

    override fun onDestroy() {
        super.onDestroy()
        thresholdJob?.cancel()
    }

    private fun setupViews() {
        toolbar?.let {
            it.setNavigationIcon(R.drawable.ic_close_white)
            it.setNavigationContentDescription(R.string.close)
            it.setTitle(R.string.action_settings)
            it.setNavigationOnClickListener { onBackPressed() }
        }
        Picasso.with(this@StudentDetailsActivity)
                .load(student.avatarUrl)
                .placeholder(R.drawable.ic_cv_user)
                .error(R.drawable.ic_cv_user)
                .fit()
                .into(avatar)

        studentName.text = student.shortName

        configureSwitches()
        configureListeners()
    }

    //region Threshold

    private fun getThresholdIdByAlertType(alertType: Alert.AlertType): String {
        for (alert in alertThresholds) {
            if (alert.alertType == Alert.alertTypeToAPIString(alertType)) {
                return alert.id.toString()
            }
        }
        return "-1"
    }

    private fun removeThresholdByAlertType(alertType: Alert.AlertType) {
        for (i in alertThresholds.indices) {
            if (alertThresholds[i].alertType == Alert.alertTypeToAPIString(alertType)) {
                alertThresholds.removeAt(i)
            }
        }
    }

    //endregion

    //region Configure

    private fun configureListeners() {
        gradeAboveValue.setOnClickListener {
            showAlertThresholdDialog(GRADE_ABOVE,
                    resources.getString(R.string.gradeAbove), gradeAboveValue.text.toString())
        }

        gradeBelowValue.setOnClickListener {
            showAlertThresholdDialog(GRADE_BELOW,
                    resources.getString(R.string.gradeBelow), gradeBelowValue.text.toString())
        }

        assignmentGradeAboveValue.setOnClickListener {
            showAlertThresholdDialog(ASSIGNMENT_GRADE_ABOVE,
                    resources.getString(R.string.assignmentGradeAbove),
                    assignmentGradeAboveValue.text.toString())
        }

        assignmentGradeBelowValue.setOnClickListener {
            showAlertThresholdDialog(ASSIGNMENT_GRADE_BELOW,
                    resources.getString(R.string.assignmentGradeBelow),
                    assignmentGradeBelowValue.text.toString())
        }
    }

    private fun configureSwitches() {
        assignmentMissingCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            //disable it to prevent users from hitting it multiple times
            assignmentMissingSwitch.isEnabled = false

            //make the API call
            if (isChecked) {
                //if it already exists, just update
                if (hasAssignmentMissingThreshold) {
                    updateAlertThreshold(Alert.AlertType.ASSIGNMENT_MISSING)
                } else {
                    //otherwise, create it
                    createThreshold(Alert.AlertType.ASSIGNMENT_MISSING)
                }
            } else {
                //delete the threshold
                deleteThreshold(Alert.AlertType.ASSIGNMENT_MISSING)
            }
        }

        teacherAnnouncementsCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            //disable it to prevent users from hitting it multiple times
            teacherAnnouncementsSwitch.isEnabled = false
            //make the API call
            if (isChecked) {
                //if it already exists, just update
                if (hasTeacherAnnouncementsThreshold) {
                    updateAlertThreshold(Alert.AlertType.COURSE_ANNOUNCEMENT)
                } else {
                    //otherwise, create it
                    createThreshold(Alert.AlertType.COURSE_ANNOUNCEMENT)
                }
            } else {
                //delete the threshold
                deleteThreshold(Alert.AlertType.COURSE_ANNOUNCEMENT)
            }
        }

        institutionAnnouncementsCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            //disable it to prevent users from hitting it multiple times
            institutionAnnouncementsSwitch.isEnabled = false

            //make the API call
            if (isChecked) {
                //if it already exists, just update
                if (hasInstitutionAnnouncementsThreshold) {
                    updateAlertThreshold(Alert.AlertType.INSTITUTION_ANNOUNCEMENT)
                } else {
                    //otherwise, create it
                    createThreshold(Alert.AlertType.INSTITUTION_ANNOUNCEMENT)
                }
            } else {
                //delete the threshold
                deleteThreshold(Alert.AlertType.INSTITUTION_ANNOUNCEMENT)
            }
        }
    }

    //endregion

    private fun showAlertThresholdDialog(thresholdType: Int, title: String, currentThreshold: String) {
        //cleanup the threshold string
        val threshold = getCleanThresholdValue(currentThreshold)

        StudentThresholdDialog.newInstance(title, threshold, thresholdType)
                .show(supportFragmentManager, StudentThresholdDialog::class.java.simpleName)
    }

    private fun isThresholdValid(threshold: String, comparedThreshold: String): Boolean {
        return StringUtilities.isStringNumeric(threshold) && StringUtilities.isStringNumeric(getCleanThresholdValue(comparedThreshold))
    }

    /**
     * Make sure that the lower grade threshold isn't above the higher threshold and vice versa
     * @return
     */
    private fun isGradeThresholdValid(thresholdType: Int, thresholdValue: String): Boolean {
        if (thresholdType == GRADE_ABOVE && hasGradeBelowThreshold) {
            //make sure the threshold isn't less than the below threshold

            //first, make sure the thresholds are numeric, then we'll compare them
            if (isThresholdValid(thresholdValue, gradeBelowValue.text.toString())) {
                val currentThreshold = Integer.parseInt(thresholdValue)
                val belowThreshold = Integer.parseInt(getCleanThresholdValue(gradeBelowValue.text.toString()))

                return currentThreshold > belowThreshold
            }
            return false
        } else if (thresholdType == GRADE_BELOW && hasGradeAboveThreshold) {
            //first, make sure the thresholds are numeric, then we'll compare them
            if (isThresholdValid(thresholdValue, gradeAboveValue.text.toString())) {
                val currentThreshold = Integer.parseInt(thresholdValue)
                val aboveThreshold = Integer.parseInt(getCleanThresholdValue(gradeAboveValue.text.toString()))

                return currentThreshold < aboveThreshold
            }
            return false
        }

        return true
    }

    /**
     * Make sure that the lower assignment threshold isn't above the higher threshold and vice versa
     * @return
     */
    private fun isAssignmentThresholdValid(thresholdType: Int, thresholdValue: String): Boolean {
        if (thresholdType == ASSIGNMENT_GRADE_ABOVE && hasAssignmentGradeBelowThreshold) {
            //make sure the threshold isn't less than the below threshold

            //first, make sure the thresholds are numeric, then we'll compare them
            if (isThresholdValid(thresholdValue, assignmentGradeBelowValue.text.toString())) {
                val currentThreshold = Integer.parseInt(thresholdValue)
                val belowThreshold = Integer.parseInt(getCleanThresholdValue(assignmentGradeBelowValue.text.toString()))

                return currentThreshold > belowThreshold
            }
            return false
        } else if (thresholdType == ASSIGNMENT_GRADE_BELOW && hasAssignmentGradeAboveThreshold) {
            //first, make sure the thresholds are numeric, then we'll compare them
            if (isThresholdValid(thresholdValue, assignmentGradeAboveValue.text.toString())) {
                val currentThreshold = Integer.parseInt(thresholdValue)
                val aboveThreshold = Integer.parseInt(getCleanThresholdValue(assignmentGradeAboveValue.text.toString()))

                return currentThreshold < aboveThreshold
            }
            return false
        }

        return true
    }

    @SuppressLint("SetTextI18n")
    override fun handlePositiveThreshold(thresholdType: Int, threshold: String) {
        when (thresholdType) {
            GRADE_ABOVE -> {
                if (!isGradeThresholdValid(thresholdType, threshold)) {
                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.course_threshold_grade_above_invalid), Toast.LENGTH_SHORT).show()
                    return
                }
                if (hasGradeAboveThreshold) {
                    updateAlertThreshold(threshold, Alert.AlertType.COURSE_GRADE_HIGH)
                } else {
                    createThreshold(threshold, Alert.AlertType.COURSE_GRADE_HIGH)
                    hasGradeAboveThreshold = true
                }
                gradeAboveValue.text = "$threshold%"
            }
            GRADE_BELOW -> {
                if (!isGradeThresholdValid(thresholdType, threshold)) {
                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.course_threshold_grade_below_invalid), Toast.LENGTH_SHORT).show()
                    return
                }
                if (hasGradeBelowThreshold) {
                    updateAlertThreshold(threshold, Alert.AlertType.COURSE_GRADE_LOW)
                } else {
                    createThreshold(threshold, Alert.AlertType.COURSE_GRADE_LOW)
                    hasGradeBelowThreshold = true
                }
                gradeBelowValue.text = "$threshold%"
            }
            ASSIGNMENT_GRADE_ABOVE -> {
                if (!isAssignmentThresholdValid(thresholdType, threshold)) {
                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.assignment_threshold_grade_above_invalid), Toast.LENGTH_SHORT).show()
                    return
                }
                if (hasAssignmentGradeAboveThreshold) {
                    updateAlertThreshold(threshold, Alert.AlertType.ASSIGNMENT_GRADE_HIGH)
                } else {
                    createThreshold(threshold, Alert.AlertType.ASSIGNMENT_GRADE_HIGH)
                    hasAssignmentGradeAboveThreshold = true
                }
                assignmentGradeAboveValue.text = "$threshold%"
            }
            ASSIGNMENT_GRADE_BELOW -> {
                if (!isAssignmentThresholdValid(thresholdType, threshold)) {
                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.assignment_threshold_grade_below_invalid), Toast.LENGTH_SHORT).show()
                    return
                }
                if (hasAssignmentGradeBelowThreshold) {
                    updateAlertThreshold(threshold, Alert.AlertType.ASSIGNMENT_GRADE_LOW)
                } else {
                    createThreshold(threshold, Alert.AlertType.ASSIGNMENT_GRADE_LOW)
                    hasAssignmentGradeBelowThreshold = true
                }
                assignmentGradeBelowValue.text = "$threshold%"
            }
        }

    }

    override fun handleNeutralThreshold(thresholdType: Int) {
        //delete the threshold
        when (thresholdType) {
            GRADE_ABOVE -> {
                gradeAboveValue.text = resources.getString(R.string.never)
                deleteThreshold(Alert.AlertType.COURSE_GRADE_HIGH)
            }
            GRADE_BELOW -> {
                gradeBelowValue.text = resources.getString(R.string.never)
                deleteThreshold(Alert.AlertType.COURSE_GRADE_LOW)
            }
            ASSIGNMENT_GRADE_ABOVE -> {
                assignmentGradeAboveValue.text = resources.getString(R.string.never)
                deleteThreshold(Alert.AlertType.ASSIGNMENT_GRADE_HIGH)
            }
            ASSIGNMENT_GRADE_BELOW -> {
                assignmentGradeBelowValue.text = resources.getString(R.string.never)
                deleteThreshold(Alert.AlertType.ASSIGNMENT_GRADE_LOW)
            }
        }
    }

    private fun deleteThreshold(alertType: Alert.AlertType) {
        AnalyticUtils.trackButtonPressed(AnalyticUtils.MODIFY_THRESHOLD)

        val thresholdId = getThresholdIdByAlertType(alertType)
        if (thresholdId != "-1") {
            thresholdJob?.cancel()
            thresholdJob = tryWeave {
                awaitApi<ResponseBody> {  AlertThresholdManager.deleteObserverAlertThreshold(
                        thresholdId, it) }

                when (alertType) {
                    Alert.AlertType.COURSE_GRADE_LOW -> hasGradeBelowThreshold = false
                    Alert.AlertType.COURSE_GRADE_HIGH -> hasGradeAboveThreshold = false
                    Alert.AlertType.ASSIGNMENT_MISSING -> hasAssignmentMissingThreshold = false
                    Alert.AlertType.COURSE_ANNOUNCEMENT -> hasTeacherAnnouncementsThreshold = false
                    Alert.AlertType.INSTITUTION_ANNOUNCEMENT -> hasInstitutionAnnouncementsThreshold = false
                    Alert.AlertType.ASSIGNMENT_GRADE_HIGH -> hasAssignmentGradeAboveThreshold = false
                    Alert.AlertType.ASSIGNMENT_GRADE_LOW -> hasAssignmentGradeBelowThreshold = false
                }
                enableSwitches(alertType)

                removeThresholdByAlertType(alertType)

            } catch {
                enableSwitches(alertType)
                Toast.makeText(this@StudentDetailsActivity, getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCleanThresholdValue(currentThreshold: String): String {
        if (!TextUtils.isEmpty(currentThreshold) && currentThreshold.contains("%")
                && currentThreshold.length >= 2
                && currentThreshold.indexOf('%') == currentThreshold.length - 1) {
            val cleanThreshold = currentThreshold.substring(0, currentThreshold.length - 1)
            return if (StringUtilities.isStringNumeric(cleanThreshold)) {
                cleanThreshold
            } else {
                ""
            }
        } else {
            return ""
        }
    }

    private fun updateAlertThreshold(newThreshold: String, alertType: Alert.AlertType) {
        AnalyticUtils.trackButtonPressed(AnalyticUtils.MODIFY_THRESHOLD)

        val thresholdId = getThresholdIdByAlertType(alertType)
        if (thresholdId != "-1") {
            thresholdJob?.cancel()
            thresholdJob = tryWeave {
                awaitApi<ObserverAlertThreshold> {
                    AlertThresholdManager.updateObserverAlertThreshold(
                            thresholdId,
                            newThreshold,
                            it)
                }
            } catch {

            }
        }
    }

    private fun updateAlertThreshold(alertType: Alert.AlertType) {
        AnalyticUtils.trackButtonPressed(AnalyticUtils.MODIFY_THRESHOLD)

        val thresholdId = getThresholdIdByAlertType(alertType)
        if (thresholdId != "-1") {
            thresholdJob?.cancel()
            thresholdJob = tryWeave {
                awaitApi<ObserverAlertThreshold> {  AlertThresholdManager.updateObserverAlertThreshold(
                        thresholdId,
                        "", it) }

                enableSwitches(alertType)
            } catch {
                Toast.makeText(this@StudentDetailsActivity, getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show()
                enableSwitches(alertType)
            }
        }
    }


    private fun createThreshold(alertType: Alert.AlertType) {
        AnalyticUtils.trackButtonPressed(AnalyticUtils.MODIFY_THRESHOLD)

        thresholdJob?.cancel()
        thresholdJob = tryWeave {
            val alert = awaitApi<ObserverAlertThreshold> {  AlertThresholdManager.createObserverAlertThreshold(
                    student.id,
                    Alert.alertTypeToAPIString(alertType)!!, it)}

            alertThresholds.add(alert)
            enableSwitches(alertType)
        } catch {
            Toast.makeText(this@StudentDetailsActivity, getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show()
            enableSwitches(alertType)
        }
    }

    private fun createThreshold(threshold: String, alertType: Alert.AlertType) {
        AnalyticUtils.trackButtonPressed(AnalyticUtils.MODIFY_THRESHOLD)

        thresholdJob?.cancel()
        thresholdJob = tryWeave {
            val alert = awaitApi<ObserverAlertThreshold> {  AlertThresholdManager.createObserverAlertThreshold(
                    student.id,
                    Alert.alertTypeToAPIString(alertType)!!,
                    threshold, it) }

            alertThresholds.add(alert)
        } catch {
            Toast.makeText(this@StudentDetailsActivity, getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Switched get disabled when the user interacts with them until the api call is finished. This
     * prevents weird race condition issues and prevents them from spamming our servers
     *
     * @param alertType
     */
    fun enableSwitches(alertType: Alert.AlertType) {
        if (alertType == Alert.AlertType.ASSIGNMENT_MISSING) {
            assignmentMissingSwitch.isEnabled = true
        } else if (alertType == Alert.AlertType.COURSE_ANNOUNCEMENT) {
            teacherAnnouncementsSwitch.isEnabled = true
        } else if (alertType == Alert.AlertType.INSTITUTION_ANNOUNCEMENT) {
            institutionAnnouncementsSwitch.isEnabled = true
        }
    }

    private fun updateThresholds() {

        //turn off all the switches
        //set the listeners to be null to not trigger an API call
        teacherAnnouncementsSwitch.setOnCheckedChangeListener(null)
        assignmentMissingSwitch.setOnCheckedChangeListener(null)
        institutionAnnouncementsSwitch.setOnCheckedChangeListener(null)


        teacherAnnouncementsSwitch.isChecked = false
        assignmentMissingSwitch.isChecked = false
        institutionAnnouncementsSwitch.isChecked = false

        //clear the edit texts so we don't put incorrect data in there
        gradeAboveValue.text = getString(R.string.never)
        gradeBelowValue.text = getString(R.string.never)
        assignmentGradeAboveValue.text = getString(R.string.never)
        assignmentGradeBelowValue.text = getString(R.string.never)

        //change the switches based on which thresholds have been set
        for (threshold in alertThresholds) {
            if (!TextUtils.isEmpty(threshold.alertType) && Alert.getAlertTypeFromString(threshold.alertType) != null) {
                when (Alert.getAlertTypeFromString(threshold.alertType)) {
                    Alert.AlertType.COURSE_GRADE_HIGH -> if (!TextUtils.isEmpty(threshold.threshold)) {
                        gradeAboveValue.text = threshold.threshold + "%"
                        hasGradeAboveThreshold = true
                    }
                    Alert.AlertType.COURSE_GRADE_LOW -> if (!TextUtils.isEmpty(threshold.threshold)) {
                        gradeBelowValue.text = threshold.threshold + "%"
                        hasGradeBelowThreshold = true
                    }
                    Alert.AlertType.COURSE_ANNOUNCEMENT -> {
                        teacherAnnouncementsSwitch.isChecked = true
                        hasTeacherAnnouncementsThreshold = true
                    }
                    Alert.AlertType.ASSIGNMENT_MISSING -> {
                        assignmentMissingSwitch.isChecked = true
                        hasAssignmentMissingThreshold = true
                    }
                    Alert.AlertType.ASSIGNMENT_GRADE_HIGH -> if (!TextUtils.isEmpty(threshold.threshold)) {
                        assignmentGradeAboveValue.text = threshold.threshold + "%"
                        hasAssignmentGradeAboveThreshold = true
                    }
                    Alert.AlertType.ASSIGNMENT_GRADE_LOW -> if (!TextUtils.isEmpty(threshold.threshold)) {
                        assignmentGradeBelowValue.text = threshold.threshold + "%"
                        hasAssignmentGradeBelowThreshold = true
                    }
                    Alert.AlertType.INSTITUTION_ANNOUNCEMENT -> {
                        institutionAnnouncementsSwitch.isChecked = true
                        hasInstitutionAnnouncementsThreshold = true
                    }
                }
            }
        }

        //reset the listeners
        teacherAnnouncementsSwitch.setOnCheckedChangeListener(teacherAnnouncementsCheckedChangeListener)
        assignmentMissingSwitch.setOnCheckedChangeListener(assignmentMissingCheckedChangeListener)
        institutionAnnouncementsSwitch.setOnCheckedChangeListener(institutionAnnouncementsCheckedChangeListener)
    }

    override fun unBundle(extras: Bundle) {

    }

    override fun applyThemeAutomagically(): Boolean {
        return false
    }

    companion object {

        private val GRADE_ABOVE = 1
        private val GRADE_BELOW = 2
        private val ASSIGNMENT_GRADE_ABOVE = 3
        private val ASSIGNMENT_GRADE_BELOW = 4

        fun createIntent(context: Context, student: User): Intent {
            val intent = Intent(context, StudentDetailsActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(Const.STUDENT, student)
            intent.putExtras(extras)
            return intent
        }
    }
}
