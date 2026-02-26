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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.GroupCategoriesManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.Companion.GPA_SCALE_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.LETTER_GRADE_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.NOT_GRADED_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.PASS_FAIL_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.PERCENT_TYPE
import com.instructure.canvasapi2.models.Assignment.Companion.POINTS_TYPE
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.Placeholder
import com.instructure.pandautils.utils.RequestCodes
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyDisplayCutoutInsets
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.handleLTIPlaceHolders
import com.instructure.pandautils.utils.hideKeyboard
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentEditAssignmentDetailsBinding
import com.instructure.teacher.dialog.ConfirmRemoveAssignmentOverrideDialog
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.EditDateGroups
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.groupedDueDates
import com.instructure.teacher.utils.setGroupedDueDates
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.view.AssignmentOverrideView
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat
import java.text.ParseException
import java.util.Date

@PageView(url = "{canvasContext}/assignments/{assignmentId}")
@ScreenView(SCREEN_VIEW_EDIT_ASSIGNMENT_DETAILS)
class EditAssignmentDetailsFragment : BaseFragment() {

    private val binding by viewBinding(FragmentEditAssignmentDetailsBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var course: Course by ParcelableArg(Course())
    private var assignment: Assignment by ParcelableArg(key = ASSIGNMENT)
    private var isPublished: Boolean = true
    private var scrollToDates: Boolean by BooleanArg(key = SHOULD_SCROLL_TO_DATES)
    private var displayGradeAs: String? = null
    private var sessionAuthJob: Job? = null
    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    private var rceImageUploadJob: Job? = null

    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)

    private val groupsMapped = hashMapOf<Long, Group>()
    private val sectionsMapped = hashMapOf<Long, Section>()
    private val studentsMapped = hashMapOf<Long, User>()

    // Keeps track of which override we were editing so we can scroll back to it when the user returns from editing assignees
    private var scrollBackToOverride: AssignmentOverrideView? = null

    private var dueDateApiCalls: Job? = null
    private var putAssignmentCall: Job? = null

    private var scrollHandler: Handler = Handler()

    private var scrollToRunnable: Runnable = Runnable {
        if(isAdded) binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
    }

    // We maintain a copy of the groupedDueDates to manipulate and use to display
    // overrides. When pushing changes, we update the original assignment object
    // with the changes in the copy.
    private var editDateGroups: EditDateGroups = arrayListOf()

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
            if (editDateGroups.contains(callback)) editDateGroups.remove(callback)
            setupOverrides()
        }
    }

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}/${course.contextId.replace("_", "s/")}/${assignment.id}"

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
            editDateGroups = (savedInstanceState.getSerializable(EDIT_DATE_GROUPS) as ArrayList<DueDateGroup>)
        }

        // Hide Keyboard
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setupViews()
        setupToolbar()
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        root.applyDisplayCutoutInsets()
        scrollView.applyBottomSystemBarInsets()
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
                rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, course, requireActivity()) { imageUrl -> binding.descriptionEditor.insertImage(requireActivity(), imageUrl) }
            }
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.applyTopSystemBarInsets()
        toolbar.setupCloseButton(this@EditAssignmentDetailsFragment)
        toolbar.title = getString(R.string.edit_assignment)
        toolbar.setupMenu(R.menu.menu_save_generic) { saveAssignment() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPublishSwitch() = with(assignment) {
        // If a student has submitted something, we can't let the teacher unpublish the assignment
        // Publish status
        val publishSwitch = binding.publishSwitch
        publishSwitch.applyTheme()
        publishSwitch.isChecked = published
        isPublished = published
        publishSwitch.setOnCheckedChangeListener { _, isChecked -> isPublished = isChecked }
        if (published && !unpublishable) {
            publishSwitch.alpha = 0.5f
            publishSwitch.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) toast(getString(R.string.unpublish_assignment_error))
                true
            }
        }
    }

    private fun setupDisplayGradeAs() = with(binding) {
        // Filters spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.display_grade_as_types, R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        displayGradeAsSpinner.adapter = spinnerAdapter
        ViewStyler.themeSpinner(requireContext(), displayGradeAsSpinner, ThemePrefs.brandColor)
        displayGradeAsSpinner.onItemSelectedListener = null

        when(displayGradeAs) {
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
                    getString(R.string.points) -> displayGradeAs = POINTS_TYPE
                    getString(R.string.gpa_scale) -> displayGradeAs = GPA_SCALE_TYPE
                    getString(R.string.letter_grade) -> displayGradeAs = LETTER_GRADE_TYPE
                    getString(R.string.complete_incomplete) -> displayGradeAs = PASS_FAIL_TYPE
                    getString(R.string.percentage) -> displayGradeAs = PERCENT_TYPE
                    getString(R.string.not_graded) -> displayGradeAs = NOT_GRADED_TYPE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupViews() = with(binding) {

        descriptionEditor.hideEditorToolbar()
        descriptionEditor.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@EditAssignmentDetailsFragment) }

        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        // Assignment name
        editAssignmentName.setText(assignment.name)
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
        editGradePoints.setText(NumberHelper.formatDecimal(assignment.pointsPossible, 2, true))
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

        if(displayGradeAs == null) {
            displayGradeAs = assignment.gradingType
        }
        setupDisplayGradeAs()

        ViewStyler.themeInputTextLayout(assignmentNameTextInput, requireContext().getColorCompat(R.color.textDark))
        ViewStyler.themeInputTextLayout(gradeTotalTextInput, requireContext().getColorCompat(R.color.textDark))
        ViewStyler.setToolbarElevation(requireContext(), toolbar, R.dimen.toolbar_elevation_small)

        // Description
        setupDescription()

        if (editDateGroups.isEmpty()) editDateGroups.addAll(assignment.groupedDueDates)

        dueDateApiCalls = weave {
            try {
                if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {
                    val sections = awaitApi<List<Section>> { SectionManager.getAllSectionsForCourse(assignment.courseId, it, false) }
                    val groups = if (assignment.groupCategoryId > 0L) awaitApi<List<Group>> { GroupCategoriesManager.getAllGroupsForCategory(assignment.groupCategoryId, it, false) } else emptyList()
                    val students = awaitApi<List<User>> { UserManager.getAllPeopleList(course, it, false) }
                    groupsMapped += groups.associateBy { it.id }
                    sectionsMapped += sections.associateBy { it.id }
                    studentsMapped += students.associateBy { it.id }
                }
                setupAddOverrideButton()
                setupOverrides()

                if (scrollToDates) {
                    scrollToDates = false
                    // We came from the Dates page, scroll to the dates for editing
                    scrollHandler.postDelayed(scrollToRunnable, 300)
                }

                scrollBackToOverride?.let {
                    if (!scrollToDates)
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

    private fun setupAddOverrideButton() = with(binding) {
        addOverride.setVisible(true)

        // Theme add button and plus image
        addOverrideText.setTextColor(ThemePrefs.textButtonColor)
        plus.setColorFilter(ThemePrefs.textButtonColor)

        addOverride.setOnClickListener {
            editDateGroups.add(DueDateGroup())
            setupOverrides()
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            // This opens the assignees page to save the user a click.
            overrideContainer.descendants<AssignmentOverrideView>().last().findViewById<AppCompatEditText>(R.id.assignTo).performClick()
        }
    }

    private fun setupOverrides() = with(binding) {
        overrideContainer.removeAllViews()
        // Load in overrides
        editDateGroups.forEachIndexed { index, dueDateGroup ->
            val assignees = ArrayList<CharSequence>()
            val v = AssignmentOverrideView(requireActivity())
            if (dueDateGroup.isEveryone) {
                assignees += getString(if (editDateGroups.any { it.hasOverrideAssignees }) R.string.everyone_else else R.string.everyone)
            }
            dueDateGroup.groupIds.forEach {
                val group = groupsMapped[it]
                if (group?.name != null) {
                    assignees.add(group.name!!)
                }
            }
            dueDateGroup.sectionIds.forEach {
                val section = sectionsMapped[it]
                if (section != null) {
                    assignees.add(section.name)
                }
            }
            dueDateGroup.studentIds.forEach {
                val student = studentsMapped[it]
                if (student != null) {
                    assignees.add(student.let { user -> Pronouns.span(user.name, user.pronouns) })
                }
            }

            v.setupOverride(index, dueDateGroup, true, assignees, datePickerOnClick, timePickerOnClick, removeOverrideClick) {
                val args = AssigneeListFragment.makeBundle(
                        editDateGroups,
                        index,
                        sectionsMapped.values.toList(),
                        groupsMapped.values.toList(),
                        studentsMapped.values.toList())
                RouteMatcher.route(requireActivity(), Route(AssigneeListFragment::class.java, course, args))
                scrollBackToOverride = v
            }

            overrideContainer.addView(v)
        }

        overrideContainer.descendants<TextInputLayout>().forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    private fun setupDescription() = with(binding) {
        // Show progress bar while loading description
        descriptionProgressBar.announceForAccessibility(getString(R.string.loading))
        descriptionProgressBar.setVisible()

        // Load description
        // If the html has a Studio LTI url, we want to authenticate so the user doesn't have to login again
        if (CanvasWebView.containsLTI(assignment.description.orEmpty(), "UTF-8")) {
            descriptionEditor.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), assignment.description ?: "") { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.assignmentDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)

        } else {
            descriptionEditor.setHtml(assignment.description,
                    getString(R.string.assignmentDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        }
        // when the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionEditor.setLabel(assignmentDescLabel, R.color.textDarkest, R.color.textDark)

        // Take down progress bar
        descriptionProgressBar.setGone()
    }

    private fun saveAssignment() = with(binding) {
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
        postData.setGroupedDueDates(editDateGroups)
        postData.description = handleLTIPlaceHolders(placeHolderList, descriptionEditor.html)
        postData.notifyOfUpdate = false
        postData.gradingType = displayGradeAs

        // TODO: remove this section when we support editing submission types
        // There is some weirdness with the API dealing with not graded stuff. When you change it from not graded you also
        // need to set the submission type to be something. When we implement submission type editing we won't need this here
        if(assignment.gradingType == NOT_GRADED_TYPE && displayGradeAs != NOT_GRADED_TYPE) {
            val type = "none"
            val submissionList = listOf(type)
            postData.submissionTypes = submissionList
        }

        // if we want to set the type as not graded, we don't want a submission type or points possible
        if(displayGradeAs == NOT_GRADED_TYPE) {
            //set points to 0 if we aren't grading it
            postData.pointsPossible = null
            val type = NOT_GRADED_TYPE
            val submissionList = listOf(type)
            postData.submissionTypes = submissionList
        }

        // only set the published flag if we can unpublish/publish the assignment
        if (assignment.unpublishable) postData.published = isPublished
        else postData.published = assignment.published

        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        putAssignmentCall = weave {
            try {
                saveButton?.setGone()
                savingProgressBar.announceForAccessibility(getString(R.string.saving))
                savingProgressBar.setVisible()
                assignment = awaitApi { AssignmentManager.editAssignment(assignment.courseId, assignment.id, postData, it, true) }
                AssignmentUpdatedEvent(assignment.id).post() // Post bus event
                toast(R.string.successfully_updated_assignment) // let the user know the assignment was saved
                editAssignmentName.hideKeyboard() // close the keyboard
                requireActivity().onBackPressed() // close this fragment
            } catch (e: Throwable) {
                saveButton?.setVisible()
                savingProgressBar.setGone()
                if (assignment.inClosedGradingPeriod) {
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
            editDateGroups = dates
            setupOverrides()
            //remove it so when we go to another assignment it won't show up there too
            EventBus.getDefault().removeStickyEvent(event)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EDIT_DATE_GROUPS, ArrayList<DueDateGroup>(editDateGroups))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dueDateApiCalls?.cancel()
        putAssignmentCall?.cancel()
        scrollHandler.removeCallbacks(scrollToRunnable)
        sessionAuthJob?.cancel()
        rceImageUploadJob?.cancel()
    }

    companion object {
        @JvmStatic val ASSIGNMENT = "assignment"
        private const val SHOULD_SCROLL_TO_DATES = "shouldScrollToDates"
        const val EDIT_DATE_GROUPS = "editDateGroups"

        fun newInstance(course: Course, args: Bundle) = EditAssignmentDetailsFragment().withArgs(args).apply {
            this.course = course
        }

        fun makeBundle(assignment: Assignment, scrollToDates: Boolean): Bundle {
            val args = Bundle()
            args.putBoolean(EditAssignmentDetailsFragment.SHOULD_SCROLL_TO_DATES, scrollToDates)
            args.putParcelable(EditAssignmentDetailsFragment.ASSIGNMENT, assignment)
            return args
        }
    }
}
