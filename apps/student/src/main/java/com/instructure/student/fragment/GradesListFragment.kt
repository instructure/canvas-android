/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.GradesListRecyclerAdapter
import com.instructure.student.adapter.TermSpinnerAdapter
import com.instructure.student.dialog.WhatIfDialogStyled
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_course_grades.*
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@PageView(url = "{canvasContext}/grades")
class GradesListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private var computeGradesJob: WeaveJob? = null

    private var termAdapter: TermSpinnerAdapter? = null
    private var gradingPeriodsList = ArrayList<GradingPeriod>()

    private var isWhatIfGrading = false

    private lateinit var allTermsGradingPeriod: GradingPeriod
    private lateinit var recyclerAdapter: GradesListRecyclerAdapter

    private val course: Course
        get() = canvasContext as Course

    override fun title(): String = getString(R.string.grades)

    override fun getSelectedParamName(): String = RouterParams.ASSIGNMENT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allTermsGradingPeriod = GradingPeriod()
        allTermsGradingPeriod.title = getString(R.string.allGradingPeriods)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_course_grades, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = GradesListRecyclerAdapter(requireContext(), course, adapterToFragmentCallback, adapterToGradesCallback, gradingPeriodsCallback, object : WhatIfDialogStyled.WhatIfDialogCallback {
            override fun onClick(assignment: Assignment, position: Int) {
                WhatIfDialogStyled.show(requireFragmentManager(), assignment, course.color) { whatIf, _ ->
                    //Create dummy submission for what if grade
                    //check to see if grade is empty for reset
                    if (whatIf == null) {
                        assignment.submission = null
                        recyclerAdapter.assignmentsHash[assignment.id]?.submission = null
                    } else {
                        recyclerAdapter.assignmentsHash[assignment.id]?.submission = Submission(
                                score = whatIf,
                                grade = whatIf.toString()
                        )
                    }

                    //Compute new overall grade
                    computeGrades(showTotalCheckBox.isChecked, position)
                }
            }
        })
        view.let {
            configureViews(it)
            configureRecyclerView(it, requireContext(), recyclerAdapter, R.id.swipeRefreshLayout, R.id.gradesEmptyView, R.id.listView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        computeGradesJob?.cancel()
        recyclerAdapter.cancel()
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, course)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { configureRecyclerView(it, requireContext(), recyclerAdapter, R.id.swipeRefreshLayout, R.id.gradesEmptyView, R.id.listView) }
    }

    private fun configureViews(rootView: View) {
        val appBarLayout = rootView.findViewById<AppBarLayout>(R.id.appbar)

        val lockDrawable = ColorKeeper.getColoredDrawable(requireContext(), R.drawable.ic_lock, ContextCompat.getColor(requireContext(), R.color.canvasTextDark))
        lockedGradeImage.setImageDrawable(lockDrawable)

        setupListeners()
        lockGrade(course.hideFinalGrades)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, i ->
            // workaround for Toolbar not showing with swipe to refresh
            if (i == 0) setRefreshingEnabled(true) else setRefreshingEnabled(false)
        })
    }

    private fun setupListeners() {
        gradeToggleView.setOnClickListener { showTotalCheckBox.toggle() }

        showTotalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (showWhatIfCheckBox.isChecked) {
                computeGrades(showTotalCheckBox.isChecked, -1)
            } else {
                val gradeString = formatGrade(recyclerAdapter.courseGrade, !isChecked)
                txtOverallGrade.text = gradeString
                txtOverallGrade.contentDescription = getContentDescriptionForMinusGradeString(gradeString, requireContext())
            }

            lockGrade(course.hideFinalGrades)
        }

        whatIfView.setOnClickListener { showWhatIfCheckBox.toggle() }

        showWhatIfCheckBox.setOnCheckedChangeListener { _, _ ->
            val currentScoreVal = recyclerAdapter.courseGrade?.currentScore ?: 0.0

            val currentScore = NumberHelper.doubleToPercentage(currentScoreVal)
            if (!showWhatIfCheckBox.isChecked) {
                txtOverallGrade.text = currentScore
            } else if (recyclerAdapter.whatIfGrade != null) {
                txtOverallGrade.text = NumberHelper.doubleToPercentage(recyclerAdapter.whatIfGrade)
            }

            // If the user is turning off what if grades we need to do a full refresh, should be
            // cached data, so fast.
            if (!showWhatIfCheckBox.isChecked) {
                recyclerAdapter.whatIfGrade = null
                recyclerAdapter.refresh()
            } else {
                // Only log when what if grades is checked on
                Analytics.logEvent(AnalyticsEventConstants.WHAT_IF_GRADES)
                recyclerAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun round(value: Double, places: Int): Double {
        if (places < 0) throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    private val adapterToGradesCallback = object : GradesListRecyclerAdapter.AdapterToGradesCallback {
        override fun setTermSpinnerState(isEnabled: Boolean) {
            if (!isAdded) return
            termSpinner.isEnabled = isEnabled
            termAdapter?.let {
                it.isLoading = !isEnabled
                it.notifyDataSetChanged()
            }
        }

        override fun notifyGradeChanged(courseGrade: CourseGrade?) {
            Logger.d("Logging for Grades E2E, current total grade is: ${txtOverallGrade.text}")
            if (!isAdded) return
            val gradeString = formatGrade(courseGrade, !showTotalCheckBox.isChecked)
            Logger.d("Logging for Grades E2E, new total grade is: $gradeString")
            txtOverallGrade.text = gradeString
            txtOverallGrade.contentDescription = getContentDescriptionForMinusGradeString(gradeString, requireContext())
            lockGrade(course.hideFinalGrades || courseGrade?.isLocked == true)
        }

        // showWhatIfCheckBox is accessed a little too early when this fragment is loaded, so we add an elvis operator here
        override val isEdit: Boolean get() = showWhatIfCheckBox?.isChecked ?: false

        override fun setIsWhatIfGrading(isWhatIfGrading: Boolean) {
            whatIfView.setVisible(isWhatIfGrading)
            this@GradesListFragment.isWhatIfGrading = isWhatIfGrading
        }
    }

    private val adapterToFragmentCallback = object : AdapterToFragmentCallback<Assignment> {
        override fun onRowClicked(assignment: Assignment, position: Int, isOpenDetail: Boolean) {
            RouteMatcher.route(requireContext(), AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id))
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
        }
    }

    /*
    * This code is similar to code in the AssignmentListFragment.
    * If you make changes here, make sure to check the same callback in the AssignmentListFrag.
    */
    private val gradingPeriodsCallback = object : StatusCallback<GradingPeriodResponse>() {

        override fun onResponse(response: Response<GradingPeriodResponse>, linkHeaders: LinkHeaders, type: ApiType) {
            gradingPeriodsList = ArrayList()
            gradingPeriodsList.addAll(response.body()!!.gradingPeriodList)
            // Add "select all" option
            gradingPeriodsList.add(allTermsGradingPeriod)
            termAdapter = TermSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, gradingPeriodsList)
            termSpinner.adapter = termAdapter
            termSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // The current item must always be set first
                    recyclerAdapter.currentGradingPeriod = termAdapter?.getItem(position)
                    if (termAdapter?.getItem(position)?.title == getString(R.string.allGradingPeriods)) {
                        recyclerAdapter.loadData()
                    } else {
                        if(termAdapter?.isEmpty == false) {
                            recyclerAdapter.loadAssignmentsForGradingPeriod(termAdapter?.getItem(position)?.id ?: 0, true)
                            termSpinner.isEnabled = false
                            termAdapter?.isLoading = true
                            termAdapter?.notifyDataSetChanged()
                        }
                    }
                    showTotalCheckBox.isChecked = true
                }
            }

            // If we have a "current" grading period select it
            if (recyclerAdapter.currentGradingPeriod != null) {
                val position = termAdapter?.getPositionForId(recyclerAdapter.currentGradingPeriod?.id ?: -1) ?: -1
                if (position != -1) {
                    termSpinner.setSelection(position)
                } else {
                    Toast.makeText(requireActivity(), com.instructure.loginapi.login.R.string.errorOccurred, Toast.LENGTH_SHORT).show()
                }
            }
            termSpinner.setVisible()
        }
    }

    private fun formatGrade(courseGrade: CourseGrade?, isFinal: Boolean): String {
        if (courseGrade == null) return getString(R.string.noGradeText)
        return if (isFinal) {
            if (courseGrade.noFinalGrade) getString(R.string.noGradeText) else NumberHelper.doubleToPercentage(courseGrade.finalScore) + if (courseGrade.hasFinalGradeString()) String.format(" (%s)", courseGrade.finalGrade) else ""
        } else {
            if (courseGrade.noCurrentGrade) getString(R.string.noGradeText) else NumberHelper.doubleToPercentage(courseGrade.currentScore) + if (courseGrade.hasCurrentGradeString()) String.format(" (%s)", courseGrade.currentGrade) else ""
        }
    }

    private fun lockGrade(isLocked: Boolean) {
        if (isLocked || recyclerAdapter.isAllGradingPeriodsSelected && !course.isTotalsForAllGradingPeriodsEnabled) {
            txtOverallGrade.setInvisible()
            lockedGradeImage.setVisible()
            gradeToggleView.setGone()
            whatIfView.setGone()
        } else {
            txtOverallGrade.setVisible()
            lockedGradeImage.setInvisible()
            gradeToggleView.setVisible()
            if (isWhatIfGrading) whatIfView.setVisible()
        }
    }

    private fun computeGrades(isShowTotalGrade: Boolean, lastPositionChanged: Int) {
        computeGradesJob = weave {
            val result = inBackground {
                if (!isShowTotalGrade) {
                    if (course.isApplyAssignmentGroupWeights) {
                        calcGradesTotal(recyclerAdapter.assignmentGroups)
                    } else {
                        calcGradesTotalNoWeight(recyclerAdapter.assignmentGroups)
                    }
                } else { //Calculates grade based on only graded assignments
                    if (course.isApplyAssignmentGroupWeights) {
                        calcGradesGraded(recyclerAdapter.assignmentGroups)
                    } else {
                        calcGradesGradedNoWeight(recyclerAdapter.assignmentGroups)
                    }
                }
            }

            recyclerAdapter.whatIfGrade = result
            txtOverallGrade.text = NumberHelper.doubleToPercentage(result)

            if(lastPositionChanged >= 0) recyclerAdapter.notifyItemChanged(lastPositionChanged)
        }
    }

    //region Grade Calculations

    /**
     * This helper method is used to calculated a courses total grade
     * based on all assignments, this maps to the online check box in the UNCHECKED state:
     *
     * "Calculated based only on graded assignments"
     *
     * @param groups: A list of assignment groups for the course
     * @return: the grade as a rounded double, IE: 85.6
     */
    private fun calcGradesTotal(groups: ArrayList<AssignmentGroup>): Double {
        var earnedScore = 0.0

        for (g in groups) {
            var earnedPoints = 0.0
            var totalPoints = 0.0
            val weight = g.groupWeight
            for (a in g.assignments) {
                val tempAssignment = recyclerAdapter.assignmentsHash[a.id]
                val tempSub = tempAssignment?.submission
                if (tempSub?.grade != null && !tempAssignment.submissionTypesRaw.isEmpty()) {
                    earnedPoints += tempSub.score
                }
                if(tempAssignment != null) totalPoints += tempAssignment.pointsPossible
            }

            if (totalPoints != 0.0 && earnedPoints != 0.0) {
                earnedScore += earnedPoints / totalPoints * weight //Cumulative
            }
        }

        return round(earnedScore, 2)
    }

    /**
     * This helper method is used to calculated a courses total grade
     * based on all assignments, this maps to the online check box in the CHECKED state:
     *
     * "Calculated based only on graded assignments"
     *
     * @param groups: A list of assignment groups for the course
     * @return: the grade as a rounded double, IE: 85.6
     */
    private fun calcGradesGraded(groups: ArrayList<AssignmentGroup>): Double {
        var totalWeight = 0.0
        var earnedScore = 0.0

        for (g in groups) {
            var totalPoints = 0.0
            var earnedPoints = 0.0
            val weight = g.groupWeight
            var assignCount = 0
            for (a in g.assignments) {
                val tempAssignment = recyclerAdapter.assignmentsHash[a.id]
                val tempSub = tempAssignment?.submission
                if (tempSub?.grade != null && !tempAssignment.submissionTypesRaw.isEmpty() && Const.PENDING_REVIEW != tempSub.workflowState) {
                    assignCount++ // Determines if a group contains assignments
                    totalPoints += tempAssignment.pointsPossible
                    earnedPoints += tempSub.score
                }
            }

            if (totalPoints != 0.0) {
                earnedScore += earnedPoints / totalPoints * weight
            }

            /*
                In order to appropriately weight assignments when only some of the weight
                categories contain graded assignments a totalWeight is created, based on the
                weight of the missing categories.
                */
            if (assignCount != 0) {
                totalWeight += weight
            }
        }

        if (totalWeight < 100 && earnedScore != 0.0) { //Not sure if earnedScore !=0 needed
            earnedScore = earnedScore / totalWeight * 100//Cumulative
        }

        return round(earnedScore, 2)
    }

    /**
     * This helper method is used to calculated a courses total grade
     * based on all assignments, this maps to the online check box in the UNCHECKED state:
     *
     * "Calculated based only on graded assignments"
     *
     * AND
     *
     * When a course has the API object member "apply_assignment_group_weights" set to false.
     *
     * @param groups: A list of assignment groups for the course
     * @return: the grade as a rounded double, IE: 85.6
     */
    private fun calcGradesTotalNoWeight(groups: ArrayList<AssignmentGroup>): Double {
        var earnedScore = 0.0
        var earnedPoints = 0.0
        var totalPoints = 0.0
        for (g in groups) {
            for (a in g.assignments) {
                val tempAssignment = recyclerAdapter.assignmentsHash[a.id]
                val tempSub = tempAssignment?.submission
                if (tempSub?.grade != null && !tempAssignment.submissionTypesRaw.isEmpty() && Const.PENDING_REVIEW != tempSub.workflowState) {
                    earnedPoints += tempSub.score
                }
                if(tempAssignment != null) totalPoints += tempAssignment.pointsPossible
            }
        }
        if (totalPoints != 0.0 && earnedPoints != 0.0) {
            earnedScore += earnedPoints / totalPoints * 100 //Cumulative
        }

        return round(earnedScore, 2)
    }

    /**
     * This helper method is used to calculated a courses total grade
     * based on all assignments, this maps to the online check box in the CHECKED state:
     *
     * "Calculated based only on graded assignments"
     *
     * AND
     *
     * When a course has the API object member "apply_assignment_group_weights" set to false.
     *
     * @param groups: A list of assignment groups for the course
     * @return: the grade as a rounded double, IE: 85.6
     */
    private fun calcGradesGradedNoWeight(groups: ArrayList<AssignmentGroup>): Double {
        var earnedScore = 0.0
        var totalPoints = 0.0
        var earnedPoints = 0.0
        for (g in groups) {
            for (a in g.assignments) {
                val tempAssignment = recyclerAdapter.assignmentsHash[a.id]
                val tempSub = tempAssignment?.submission
                if (tempSub?.grade != null && !tempAssignment.submissionTypesRaw.isEmpty()) {
                    totalPoints += tempAssignment.pointsPossible
                    earnedPoints += tempSub.score
                }
            }
        }
        if (totalPoints != 0.0) {
            earnedScore += earnedPoints / totalPoints * 100
        }

        return round(earnedScore, 2)
    }

    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    companion object {

        fun newInstance(route: Route) : GradesListFragment? {
            return if(validRoute(route)) GradesListFragment().apply {
                arguments = route.arguments
                canvasContext = route.canvasContext!!
            } else null
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null && route.canvasContext is Course // Only courses are supported in Grades List
        }

        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(null, GradesListFragment::class.java, canvasContext, canvasContext.makeBundle())
        }
    }
}
