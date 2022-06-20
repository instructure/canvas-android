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
package com.instructure.teacher.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.GroupCategoriesManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment.Companion.GPA_SCALE_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.LETTER_GRADE_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.NOT_GRADED_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.PASS_FAIL_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.PERCENT_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.POINTS_TYPE
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.dialog.ConfirmRemoveAssignmentOverrideDialog
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.AssignmentOverrideView
import kotlinx.android.synthetic.main.fragment_edit_assignment_details.*
import kotlinx.android.synthetic.main.view_assignment_override.view.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat
import java.text.ParseException
import java.util.Date

@ScreenView(SCREEN_VIEW_EDIT_ASSIGNMENT_DETAILS)
class EditAssignmentDetailsFragment : BaseFragment() {

    private var mCourse: Course by ParcelableArg(Course())
    private var mAssignment: Assignment by ParcelableArg(key = ASSIGNMENT)
    private var mIsPublished: Boolean = true
    private var mScrollToDates: Boolean by BooleanArg(key = SHOULD_SCROLL_TO_DATES)
    private var mDisplayGradeAs: String? = null
    private var mSessionAuthJob: Job? = null
    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    private var rceImageUploadJob: Job? = null

    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)

    private val groupsMapped = hashMapOf<Long, Group>()
    private val sectionsMapped = hashMapOf<Long, Section>()
    private val studentsMapped = hashMapOf<Long, User>()

    // Keeps track of which override we were editing so we can scroll back to it when the user returns from editing assignees
    private var scrollBackToOverride: AssignmentOverrideView? = null

    private var mDueDateApiCalls: Job? = null
    private var mPutAssignmentCall: Job? = null

    private var mScrollHandler: Handler = Handler()

    private var mScrollToRunnable: Runnable = Runnable {
        if(isAdded) scrollView.fullScroll(ScrollView.FOCUS_DOWN)
    }

    // We maintain a copy of the groupedDueDates to manipulate and use to display
    // overrides. When pushing changes, we update the original assignment object
    // with the changes in the copy.
    private var mEditDateGroups: EditDateGroups = arrayListOf()

    private val datePickerOnClick: (date: Date?, (Int, Int, Int) -> Unit) -> Unit = { date, callback ->
        DatePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, date) { year, month, dayOfMonth ->
            callback(year, month, dayOfMonth)
        }.show(requireActivity().supportFragmentManager, DatePickerDialogFragment::class.java.simpleName)
    }

    private val timePickerOnClick: (date: Date?, (Int, Int) -> Unit) -> Unit = { date, callback ->
        TimePickerDialogFragment.getInstance(requireActivity().supportFragmentManager, date) { hour, min ->
            callback(hour, min)
        }.show(requireActivity().supportFragmentManager, TimePickerDialogFragment::class.java.simpleName)
    }

    private val removeOverrideClick: (DueDateGroup) -> Unit = { callback ->
        // Show confirmation dialog
        ConfirmRemoveAssignmentOverrideDialog.show(requireActivity().supportFragmentManager) {
            if (mEditDateGroups.contains(callback)) mEditDateGroups.remove(callback)
            setupOverrides()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun layoutResId() = R.layout.fragment_edit_assignment_details

    override fun onCreateView(view: View) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            @Suppress("UNCHECKED_CAST")
            mEditDateGroups = (savedInstanceState.getSerializable(EDIT_DATE_GROUPS) as ArrayList<DueDateGroup>)
        }

        // Hide Keyboard
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setupViews()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, mCourse, requireActivity()) { text, alt -> descriptionEditor.insertImage(text, alt) }
            }
        }
    }

    private fun setupToolbar() {
        toolbar.setupCloseButton(this)
        toolbar.title = getString(R.string.edit_assignment)
        toolbar.setupMenu(R.menu.menu_save_generic) { saveAssignment() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        saveButton?.setTextColor(ThemePrefs.buttonColor)
    }

    private fun setupPublishSwitch() = with(mAssignment) {
        // If a student has submitted something, we can't let the teacher unpublish the assignment
        if (!mAssignment.unpublishable) {
            publishWrapper.setGone()
            mIsPublished = true
            return
        }
        // Publish status
        publishSwitch.applyTheme()
        publishSwitch.isChecked = published
        mIsPublished = published

        publishSwitch.setOnCheckedChangeListener { _, isChecked -> mIsPublished = isChecked }
    }

    private fun setupDisplayGradeAs() {
        // Filters spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.display_grade_as_types, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayGradeAsSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), displayGradeAsSpinner, ThemePrefs.brandColor)
        displayGradeAsSpinner.onItemSelectedListener = null

        when(mDisplayGradeAs) {
            POINTS_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.points)))
            GPA_SCALE_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.gpa_scale)))
            LETTER_GRADE_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.letter_grade)))
            PASS_FAIL_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.complete_incomplete)))
            PERCENT_TYPE-> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.percentage)))
            NOT_GRADED_TYPE -> displayGradeAsSpinner.setSelection(spinnerAdapter.getPosition(getString(R.string.not_graded)))
        }

        displayGradeAsSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(view == null) return
                when((view as TextView).text.toString()) {
                    getString(R.string.points) -> mDisplayGradeAs = POINTS_TYPE
                    getString(R.string.gpa_scale) -> mDisplayGradeAs = GPA_SCALE_TYPE
                    getString(R.string.letter_grade) -> mDisplayGradeAs = LETTER_GRADE_TYPE
                    getString(R.string.complete_incomplete) -> mDisplayGradeAs = PASS_FAIL_TYPE
                    getString(R.string.percentage) -> mDisplayGradeAs = PERCENT_TYPE
                    getString(R.string.not_graded) -> mDisplayGradeAs = NOT_GRADED_TYPE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupViews() = with(mAssignment) {

        descriptionEditor.hideEditorToolbar()
        descriptionEditor.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@EditAssignmentDetailsFragment) }

        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        // Assignment name
        editAssignmentName.setText(name)
        editAssignmentName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) descriptionEditor.hideEditorToolbar()
        }
        // set the underline to be the brand color
        ViewStyler.themeEditText(requireContext(), editAssignmentName, ThemePrefs.brandColor)
        editAssignmentName.onTextChanged {
            if (it.isBlank()) {
                assignmentNameTextInput.error = getString(R.string.assignment_name_must_be_set)
            } else {
                assignmentNameTextInput.isErrorEnabled = false
            }
        }
        // Points possible
        editGradePoints.setText(NumberHelper.formatDecimal(pointsPossible, 2, true))
        // set the underline to be the brand color
        ViewStyler.themeEditText(requireContext(), editGradePoints, ThemePrefs.brandColor)
        editGradePoints.onTextChanged {
            if (it.isBlank()) {
                gradeTotalTextInput.error = getString(R.string.assignment_points_must_be_set)
            } else {
                gradeTotalTextInput.isErrorEnabled = false
            }
        }
        setupPublishSwitch()

        if(mDisplayGradeAs == null) {
            mDisplayGradeAs = gradingType
        }
        setupDisplayGradeAs()

        ViewStyler.themeInputTextLayout(assignmentNameTextInput, requireContext().getColorCompat(R.color.textDark))
        ViewStyler.themeInputTextLayout(gradeTotalTextInput, requireContext().getColorCompat(R.color.textDark))
        ViewStyler.setToolbarElevation(requireContext(), toolbar, R.dimen.toolbar_elevation_small)

        // Description
        setupDescription()

        if (mEditDateGroups.isEmpty()) mEditDateGroups.addAll(mAssignment.groupedDueDates)

        mDueDateApiCalls = weave {
            try {
                if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {
                    val sections = awaitApi<List<Section>> { SectionManager.getAllSectionsForCourse(courseId, it, false) }
                    val groups = if (groupCategoryId > 0L) awaitApi<List<Group>> { GroupCategoriesManager.getAllGroupsForCategory(groupCategoryId, it, false) } else emptyList()
                    val students = awaitApi<List<User>> { UserManager.getAllPeopleList(mCourse, it, false) }
                    groupsMapped += groups.associateBy { it.id }
                    sectionsMapped += sections.associateBy { it.id }
                    studentsMapped += students.associateBy { it.id }
                }
                setupAddOverrideButton()
                setupOverrides()

                if (mScrollToDates) {
                    mScrollToDates = false
                    // We came from the Dates page, scroll to the dates for editing
                    mScrollHandler.postDelayed(mScrollToRunnable, 300)
                }

                scrollBackToOverride?.let {
                    if (!mScrollToDates)
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    scrollBackToOverride = null
                }
            } catch (e: Throwable) {
                if (isAdded) {
                    toast(R.string.error_occurred)
                }
            }
        }
    }

    private fun setupAddOverrideButton() {
        addOverride.setVisible(true)

        // Theme add button and plus image
        addOverrideText.setTextColor(ThemePrefs.buttonColor)
        plus.setColorFilter(ThemePrefs.buttonColor)

        addOverride.setOnClickListener {
            mEditDateGroups.add(DueDateGroup())
            setupOverrides()
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            // This opens the assignees page to save the user a click.
            overrideContainer.descendants<AssignmentOverrideView>().last().assignTo.performClick()
        }
    }

    private fun setupOverrides() {
        overrideContainer.removeAllViews()
        // Load in overrides
        mEditDateGroups.forEachIndexed { index, dueDateGroup ->
            val assignees = ArrayList<CharSequence>()
            val v = AssignmentOverrideView(requireActivity())
            if (dueDateGroup.isEveryone) {
                assignees += getString(if (mEditDateGroups.any { it.hasOverrideAssignees }) R.string.everyone_else else R.string.everyone)
            }
            dueDateGroup.groupIds.forEach { assignees.add(groupsMapped[it]?.name!!) }
            dueDateGroup.sectionIds.forEach { assignees.add(sectionsMapped[it]?.name!!) }
            dueDateGroup.studentIds.forEach {
                val student = studentsMapped[it]
                if (student != null) {
                    assignees.add(student.let { user -> Pronouns.span(user.name, user.pronouns) })
                }
            }

            v.setupOverride(index, dueDateGroup, mEditDateGroups.size > 1, assignees, datePickerOnClick, timePickerOnClick, removeOverrideClick) {
                val args = AssigneeListFragment.makeBundle(
                        mEditDateGroups,
                        index,
                        sectionsMapped.values.toList(),
                        groupsMapped.values.toList(),
                        studentsMapped.values.toList())
                RouteMatcher.route(requireContext(), Route(AssigneeListFragment::class.java, mCourse, args))
                scrollBackToOverride = v
            }

            overrideContainer.addView(v)
        }

        overrideContainer.descendants<TextInputLayout>().forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    private fun setupDescription() {
        // Show progress bar while loading description
        descriptionProgressBar.announceForAccessibility(getString(R.string.loading))
        descriptionProgressBar.setVisible()

        // Load description
        // If the html has a Studio LTI url, we want to authenticate so the user doesn't have to login again
        if (CanvasWebView.containsLTI(mAssignment.description.orEmpty(), "UTF-8")) {
            descriptionEditor.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), mAssignment.description ?: "") { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.assignmentDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor)

        } else {
            descriptionEditor.setHtml(mAssignment.description,
                    getString(R.string.assignmentDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.buttonColor)
        }
        // when the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionEditor.setLabel(assignmentDescLabel, R.color.textDarkest, R.color.textDark)

        // Take down progress bar
        descriptionProgressBar.setGone()
    }

    private fun saveAssignment() {
        // Both name and points are required
        if (editAssignmentName.text.isNullOrBlank() || editGradePoints.text.isNullOrBlank()) return

        // Validate points possible
        val pointsPossible = try {
            DecimalFormat().parse(editGradePoints.text.toString()).toDouble()
        } catch (e: ParseException) {
            editGradePoints.error = getString(R.string.assignment_points_must_be_a_number)
            return
        }

        // Check due, unlock, and lock dates
        if (overrideContainer.children<AssignmentOverrideView>().any { it.validateInput() }) return

        val postData = AssignmentPostBody()
        postData.name = editAssignmentName.text.toString()
        postData.pointsPossible = pointsPossible
        postData.setGroupedDueDates(mEditDateGroups)
        postData.description = handleLTIPlaceHolders(placeHolderList, descriptionEditor.html)
        postData.notifyOfUpdate = false
        postData.gradingType = mDisplayGradeAs

        // TODO: remove this section when we support editing submission types
        // There is some weirdness with the API dealing with not graded stuff. When you change it from not graded you also
        // need to set the submission type to be something. When we implement submission type editing we won't need this here
        if(mAssignment.gradingType == NOT_GRADED_TYPE && mDisplayGradeAs != NOT_GRADED_TYPE) {
            val type = "none"
            val submissionList = listOf(type)
            postData.submissionTypes = submissionList
        }

        // if we want to set the type as not graded, we don't want a submission type or points possible
        if(mDisplayGradeAs == NOT_GRADED_TYPE) {
            //set points to 0 if we aren't grading it
            postData.pointsPossible = null
            val type = NOT_GRADED_TYPE
            val submissionList = listOf(type)
            postData.submissionTypes = submissionList
        }

        // only set the published flag if we can unpublish/publish the assignment
        if (mAssignment.unpublishable) postData.published = mIsPublished
        else postData.published = mAssignment.published

        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        mPutAssignmentCall = weave {
            try {
                saveButton?.setGone()
                savingProgressBar.announceForAccessibility(getString(R.string.saving))
                savingProgressBar.setVisible()
                mAssignment = awaitApi { AssignmentManager.editAssignment(mAssignment.courseId, mAssignment.id, postData, it, false) }
                AssignmentUpdatedEvent(mAssignment.id).post() // Post bus event
                toast(R.string.successfully_updated_assignment) // let the user know the assignment was saved
                editAssignmentName.hideKeyboard() // close the keyboard
                requireActivity().onBackPressed() // close this fragment
            } catch (e: Throwable) {
                saveButton?.setVisible()
                savingProgressBar.setGone()
                if (mAssignment.inClosedGradingPeriod) {
                    toast(R.string.error_saving_assignment_closed_grading_period)
                } else {
                    toast(R.string.error_saving_assignment)
                }
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssigneesChanged(event: AssigneesUpdatedEvent) {
        // Update grouped due dates (EditDateGroups)
        event.once(javaClass.simpleName) { dates ->
            mEditDateGroups = dates
            setupOverrides()
            //remove it so when we go to another assignment it won't show up there too
            EventBus.getDefault().removeStickyEvent(event)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EDIT_DATE_GROUPS, ArrayList<DueDateGroup>(mEditDateGroups))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDueDateApiCalls?.cancel()
        mPutAssignmentCall?.cancel()
        mScrollHandler.removeCallbacks(mScrollToRunnable)
        mSessionAuthJob?.cancel()
        rceImageUploadJob?.cancel()
    }

    companion object {
        @JvmStatic val ASSIGNMENT = "assignment"
        private const val SHOULD_SCROLL_TO_DATES = "shouldScrollToDates"
        const val EDIT_DATE_GROUPS = "editDateGroups"

        fun newInstance(course: Course, args: Bundle) = EditAssignmentDetailsFragment().withArgs(args).apply {
            mCourse = course
        }

        fun makeBundle(assignment: Assignment, scrollToDates: Boolean): Bundle {
            val args = Bundle()
            args.putBoolean(EditAssignmentDetailsFragment.SHOULD_SCROLL_TO_DATES, scrollToDates)
            args.putParcelable(EditAssignmentDetailsFragment.ASSIGNMENT, assignment)
            return args
        }
    }
}
