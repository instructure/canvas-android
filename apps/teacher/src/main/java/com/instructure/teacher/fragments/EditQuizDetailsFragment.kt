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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.QuizPostBody
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.interactions.Identity
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_QUIZ_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.DatePickerDialogFragment
import com.instructure.pandautils.dialogs.TimePickerDialogFragment
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.LongArg
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
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentEditQuizDetailsBinding
import com.instructure.teacher.dialog.ConfirmRemoveAssignmentOverrideDialog
import com.instructure.teacher.events.AssigneesUpdatedEvent
import com.instructure.teacher.factory.EditQuizDetailsPresenterFactory
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.presenters.EditQuizDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.groupedDueDates
import com.instructure.teacher.utils.setGroupedDueDates
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.view.AssignmentOverrideView
import com.instructure.teacher.viewinterface.EditQuizDetailsView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date

@ScreenView(SCREEN_VIEW_EDIT_QUIZ_DETAILS)
class EditQuizDetailsFragment : BasePresenterFragment<
        EditQuizDetailsPresenter,
        EditQuizDetailsView,
        FragmentEditQuizDetailsBinding>(),
    EditQuizDetailsView, Identity {

    private var mCourse: Course by ParcelableArg(Course())

    private var mQuizId: Long by LongArg(0L, QUIZ_ID)
    private var mQuiz: Quiz by ParcelableArg(Quiz(), QUIZ)
    private var mAssignment: Assignment by ParcelableArg(Assignment().apply { id = -1L }, ASSIGNMENT)

    private var mNeedToForceNetwork = false

    private var mIsPublished = true
    private var mHasAccessCode = false
    private var mQuizType: String? = null
    private var mScrollToDates by BooleanArg(false, SHOULD_SCROLL_TO_DATES)

    private val saveButton: TextView? get() = view?.findViewById(R.id.menu_save)

    private var placeHolderList: ArrayList<Placeholder> = ArrayList()

    // Keeps track of which override we were editing so we can scroll back to it when the user returns from editing assignees
    private var scrollBackToOverride: AssignmentOverrideView? = null

    private var mScrollHandler: Handler = Handler()

    private var mScrollToRunnable: Runnable = Runnable {
        if (isAdded) binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
    }

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
            if (presenter.mEditDateGroups.contains(callback)) presenter.mEditDateGroups.remove(callback)
            setupOverrides()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentEditQuizDetailsBinding = FragmentEditQuizDetailsBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide Keyboard
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setupToolbar()
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        root.applyDisplayCutoutInsets()
        toolbar.applyTopSystemBarInsets()
        scrollView.applyBottomSystemBarInsets()
    }

    override fun populateQuizDetails() {
        setupViews()
    }

    override fun onReadySetGo(presenter: EditQuizDetailsPresenter) {
        if (mQuizId == 0L) {
            // We have a Quiz object, possibly no assignment object though - Check it
            when {
                // This quiz does not have an assignment (non-graded) Load like normal
                mQuiz.assignmentId == 0L -> presenter.loadData(mNeedToForceNetwork)
                // Else this quiz has an assignment and we don't have the assignment
                presenter.mAssignment.id == -1L -> presenter.getAssignment(mQuiz.assignmentId, mCourse.id)
                // We have all that we need, make sure we set up the views. This is needed when we come back to this fragment from assignees
                else -> setupViews()
            }
        } else {
            presenter.getQuiz(mQuizId, mCourse.id)
        }
    }

    override fun getPresenterFactory() = EditQuizDetailsPresenterFactory(mQuiz, mAssignment, mCourse)

    override fun onPresenterPrepared(presenter: EditQuizDetailsPresenter) {}
    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}

    override fun scrollCheck() {
        if (mScrollToDates) {
            mScrollToDates = false
            // We came from the Dates page, scroll to the dates for editing
            mScrollHandler.postDelayed(mScrollToRunnable, 300)
        } else {
            scrollBackToOverride?.let {
                binding.scrollView.post {
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            scrollBackToOverride = null
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupCloseButton(this@EditQuizDetailsFragment)
        toolbar.title = getString(R.string.editQuiz)
        toolbar.setupMenu(R.menu.menu_save_generic) { saveQuiz() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    private fun setupPublishSwitch() = with(binding) {
        //if a student has submitted something, we can't let the teacher unpublish the quiz
        if (!presenter.mQuiz.unpublishable) {
            publishWrapper.setGone()
            mIsPublished = true
            return
        }
        // Publish status
        publishSwitch.applyTheme()
        publishSwitch.isChecked = presenter.mQuiz.published
        mIsPublished = presenter.mQuiz.published

        publishSwitch.setOnCheckedChangeListener { _, isChecked -> mIsPublished = isChecked }
    }

    private fun setupAccessCodeSwitch() = with(binding) {
        accessCodeSwitch.isChecked = mQuiz.hasAccessCode
        mHasAccessCode = mQuiz.hasAccessCode
        accessCodeTextInput.setVisible(mQuiz.hasAccessCode)
        ViewStyler.themeEditText(requireContext(), editAccessCode, ThemePrefs.brandColor)

        if (mQuiz.accessCode != null) {
            editAccessCode.setText(mQuiz.accessCode)
        }

        accessCodeSwitch.applyTheme()

        accessCodeSwitch.setOnCheckedChangeListener { _, isChecked ->
            mHasAccessCode = isChecked
            accessCodeTextInput.setVisible(isChecked)
        }
    }

    private fun setupQuizTypeSpinner() = with(binding) {
        // Filters spinner
        val filtersAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.quizTypes, android.R.layout.simple_spinner_item)
        filtersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quizTypeFilter.adapter = filtersAdapter
        quizTypeFilter.onItemSelectedListener = null

        when(mQuizType) {
            Quiz.TYPE_ASSIGNMENT -> quizTypeFilter.setSelection(filtersAdapter.getPosition(getString(R.string.gradedQuiz)))
            Quiz.TYPE_PRACTICE -> quizTypeFilter.setSelection(filtersAdapter.getPosition(getString(R.string.practiceQuiz)))
            Quiz.TYPE_SURVEY -> quizTypeFilter.setSelection(filtersAdapter.getPosition(getString(R.string.ungradedSurvey)))
            Quiz.TYPE_GRADED_SURVEY-> quizTypeFilter.setSelection(filtersAdapter.getPosition(getString(R.string.gradedSurvey)))
        }

        quizTypeFilter.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(view == null) return
                val mPrevQuizType = mQuizType
                when((view as TextView).text.toString()) {
                    getString(R.string.gradedSurvey) -> mQuizType = Quiz.TYPE_GRADED_SURVEY
                    getString(R.string.practiceQuiz) -> mQuizType = Quiz.TYPE_PRACTICE
                    getString(R.string.ungradedSurvey) -> mQuizType = Quiz.TYPE_SURVEY
                    getString(R.string.gradedQuiz) -> mQuizType = Quiz.TYPE_ASSIGNMENT
                }

                if (mPrevQuizType != mQuizType)
                    updateOverridesForQuizType(true)
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        })
    }

    /**
     * We can only support overrides for quizzes with an assignment.
     * If the quiz does not have an assignment, hide the override stuff.
     */
    private fun updateOverridesForQuizType(setupOverrides: Boolean = false) = with(binding) {
        when (mQuizType) {
            Quiz.TYPE_GRADED_SURVEY, Quiz.TYPE_ASSIGNMENT -> {
                overrideContainer.setVisible()
                addOverride.setVisible()
            }
            Quiz.TYPE_SURVEY, Quiz.TYPE_PRACTICE -> {
                overrideContainer.setGone()
                addOverride.setGone()
            }
        }
        if (setupOverrides) setupOverrides()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupViews() = with(binding) {
        (view as? ViewGroup)?.descendants<TextInputLayout>()?.forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        // Quiz name
        editQuizTitle.setText(presenter.mQuiz.title)
        // set the underline to be the brand color
        ViewStyler.themeEditText(requireContext(), editQuizTitle, ThemePrefs.brandColor)

        editQuizTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editedString: Editable) {
                if (editedString.isBlank()) {
                    quizTitleTextInput.error = getString(R.string.assignment_name_must_be_set)
                } else {
                    quizTitleTextInput.isErrorEnabled = false
                }
            }
        })

        editAccessCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editedString: Editable) {
                if (editedString.isBlank()) {
                    accessCodeTextInput.error = getString(R.string.mustHaveAccessCode)
                } else {
                    accessCodeTextInput.isErrorEnabled = false
                }
            }
        })

        if(mQuizType == null) {
            mQuizType = presenter.mQuiz.quizType
        }

        setupPublishSwitch()
        setupAccessCodeSwitch()
        setupQuizTypeSpinner()

        ViewStyler.themeInputTextLayout(quizTitleTextInput, requireContext().getColorCompat(R.color.textDark))
        ViewStyler.setToolbarElevation(requireContext(), toolbar, R.dimen.toolbar_elevation_small)

        // Description
        setupDescription(presenter.mQuiz)

        with(presenter) {if (mEditDateGroups.isEmpty()) mEditDateGroups.addAll(mAssignment.groupedDueDates)}
        presenter.getStudentsGroupsAndSections()
    }

    private fun setupDescription(quiz: Quiz) = with(binding) {
        // Show progress bar while loading description
        descriptionProgressBar.announceForAccessibility(getString(R.string.loading))
        descriptionProgressBar.setVisible()

        // Load description
        val description = quiz.description ?: ""
        if (CanvasWebView.containsLTI(description, "UTF-8")) {
            descriptionWebView.setHtml(DiscussionUtils.createLTIPlaceHolders(requireContext(), description) { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                    getString(R.string.quizDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        } else {
            descriptionWebView.setHtml(description,
                    getString(R.string.quizDescriptionContentDescription),
                    getString(R.string.rce_empty_description),
                    ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        }


        // When the RCE editor has focus we want the label to be darker so it matches the title's functionality
        descriptionWebView.setLabel(quizDescLabel, R.color.textDarkest, R.color.textDark)

        descriptionWebView.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this@EditQuizDetailsFragment) }

        // Dismiss the progress bar
        descriptionProgressBar.setGone()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // Get the image Uri
            when (requestCode) {
                RequestCodes.PICK_IMAGE_GALLERY -> data?.data
                RequestCodes.CAMERA_PIC_REQUEST -> MediaUploadUtils.handleCameraPicResult(requireActivity(), null)
                else -> null
            }?.let { imageUri ->
                MediaUploadUtils.uploadRceImageJob(imageUri, mCourse, requireActivity()) { imageUrl -> binding.descriptionWebView.insertImage(requireActivity(), imageUrl) }
            }
        }
    }

    override fun setupOverrides() {
        binding.overrideContainer.removeAllViews()
        // Load in overrides
        with(presenter) {
            mEditDateGroups.forEachIndexed { index, dueDateGroup ->
                val assignees = ArrayList<CharSequence>()
                val v = AssignmentOverrideView(requireActivity())
                if (dueDateGroup.isEveryone) {
                    // && (dueDateGroup.coreDates.dueDate != null || dueDateGroup.coreDates.lockDate != null || dueDateGroup.coreDates.unlockDate != null)
                    assignees += getString(if (mEditDateGroups.any { it.hasOverrideAssignees }) R.string.everyone_else else R.string.everyone)
                }
                dueDateGroup.groupIds.forEach { assignees.add(groupsMapped[it]?.name ?: "") }
                dueDateGroup.sectionIds.forEach { assignees.add(sectionsMapped[it]?.name ?: "") }
                dueDateGroup.studentIds.forEach {
                    val student = studentsMapped[it]
                    if (student != null) {
                        assignees.add(student.let { user -> Pronouns.span(user.name, user.pronouns) })
                    }
                }

                v.setupOverride(index, dueDateGroup, true, assignees, datePickerOnClick, timePickerOnClick, removeOverrideClick) {
                    val args = AssigneeListFragment.makeBundle(
                            mEditDateGroups,
                            index,
                            sectionsMapped.values.toList(),
                            groupsMapped.values.toList(),
                            studentsMapped.values.toList())
                    RouteMatcher.route(requireActivity(), Route(AssigneeListFragment::class.java, mCourse, args))
                    scrollBackToOverride = v
                }

                binding.overrideContainer.addView(v)
            }
        }

        binding.overrideContainer.descendants<TextInputLayout>().forEach {
            it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
    }

    private fun saveQuiz() = with(binding) {
        // Title is required
        if (editQuizTitle.text.isNullOrBlank()) return

        // Check due, unlock, and lock dates
        if (overrideContainer.children<AssignmentOverrideView>().any { it.validateInput() }) return


        // If a quiz requires an access code it has to have an access code entered
        if (mHasAccessCode && editAccessCode.text.isNullOrBlank()) {
            // setting the text blank will trigger the error
            editAccessCode.setText("")
            return
        }

        val quizPostData = assembleQuizPostData()
        val assignmentPostData = assembleAssignmentPostData()
        presenter.saveQuiz(quizPostData, assignmentPostData)
    }

    private fun assembleQuizPostData(): QuizPostBody = QuizPostBody().apply {
        title = binding.editQuizTitle.text.toString()
        description = handleLTIPlaceHolders(placeHolderList, binding.descriptionWebView.html)
        notifyOfUpdate = false
        accessCode = if (mHasAccessCode) binding.editAccessCode.text.toString() else null
        quizType = mQuizType

        // Only set the published flag if we can unpublish/publish the assignment
        published = if (mQuiz.unpublishable) mIsPublished else presenter.mQuiz.published
    }

    /**
     * Assembles the data we need to update the quiz's assignment overrides
     */
    private fun assembleAssignmentPostData(): AssignmentPostBody {
        return AssignmentPostBody().apply {
            setGroupedDueDates(presenter.mEditDateGroups)
            notifyOfUpdate = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mScrollHandler.removeCallbacks(mScrollToRunnable)
    }

    override fun startSavingQuiz() {
        saveButton?.setGone()
        binding.savingProgressBar.announceForAccessibility(getString(R.string.saving))
        binding.savingProgressBar.setVisible()
    }

    override fun quizSavedSuccessfully() {
        toast(R.string.successfully_updated_quiz) // Let the user know the quiz was saved
        binding.editQuizTitle.hideKeyboard() // Close the keyboard
        requireActivity().onBackPressed() // Close this fragment
    }

    override fun errorSavingQuiz() {
        saveButton?.setVisible()
        binding.savingProgressBar.setGone()
        toast(R.string.error_saving_quiz)
    }

    override fun setupAddOverridesButton() = with(binding) {
        updateOverridesForQuizType()

        // Theme add button and plus image
        addOverrideText.setTextColor(ThemePrefs.textButtonColor)
        plus.setColorFilter(ThemePrefs.textButtonColor)

        addOverride.setOnClickListener {
            presenter.mEditDateGroups.add(DueDateGroup())
            setupOverrides()
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
            // This opens the assignees page to save the user a click.
            overrideContainer.descendants<AssignmentOverrideView>().last().findViewById<AppCompatEditText>(R.id.assignTo).performClick()
        }
    }

    override val identity: Long? get() = if(mQuizId != 0L) mQuizId else mQuiz.id
    override val skipCheck: Boolean get() = false

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssigneesChanged(event: AssigneesUpdatedEvent) {
        // Update grouped due dates (EditDateGroups)
        event.once(javaClass.simpleName) { dates ->
            presenter.mEditDateGroups = dates
            setupOverrides()
            // Remove it so when we go to another assignment it won't show up there too
            EventBus.getDefault().removeStickyEvent(event)
        }
    }

    companion object {
        @JvmStatic val QUIZ = "quiz"
        @JvmStatic val QUIZ_ID = "quiz_id"
        @JvmStatic val ASSIGNMENT = "assignment"
        @JvmStatic val SHOULD_SCROLL_TO_DATES = "shouldScrollToDates"

        fun newInstance(course: Course, args: Bundle) = EditQuizDetailsFragment().withArgs(args).apply {
            mCourse = course
        }

        fun makeBundle(quiz: Quiz, scrollToDates: Boolean): Bundle {
            val args = Bundle()
            args.putBoolean(EditQuizDetailsFragment.SHOULD_SCROLL_TO_DATES, scrollToDates)
            args.putParcelable(EditQuizDetailsFragment.QUIZ, quiz)
            return args
        }

        fun makeBundle(quiz: Quiz): Bundle {
            return Bundle().apply {
                putParcelable(QUIZ, quiz)
            }
        }

        fun makeBundle(quizId: Long): Bundle {
            return Bundle().apply {
                putLong(QUIZ_ID, quizId)
                putBoolean(EditQuizDetailsFragment.SHOULD_SCROLL_TO_DATES, true)
            }
        }
    }
}
