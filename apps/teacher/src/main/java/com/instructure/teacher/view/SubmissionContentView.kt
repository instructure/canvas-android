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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.instructure.annotations.PdfSubmissionView
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.binding.BindableSpinnerAdapter
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.speedgrader.content.AnonymousSubmissionContent
import com.instructure.pandautils.features.speedgrader.content.DiscussionContent
import com.instructure.pandautils.features.speedgrader.content.ExternalToolContent
import com.instructure.pandautils.features.speedgrader.content.GradeableContent
import com.instructure.pandautils.features.speedgrader.content.ImageContent
import com.instructure.pandautils.features.speedgrader.content.MediaContent
import com.instructure.pandautils.features.speedgrader.content.NoSubmissionContent
import com.instructure.pandautils.features.speedgrader.content.NoneContent
import com.instructure.pandautils.features.speedgrader.content.OnPaperContent
import com.instructure.pandautils.features.speedgrader.content.OtherAttachmentContent
import com.instructure.pandautils.features.speedgrader.content.PdfContent
import com.instructure.pandautils.features.speedgrader.content.QuizContent
import com.instructure.pandautils.features.speedgrader.content.StudentAnnotationContent
import com.instructure.pandautils.features.speedgrader.content.TextContent
import com.instructure.pandautils.features.speedgrader.content.UnsupportedContent
import com.instructure.pandautils.features.speedgrader.content.UrlContent
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAvatarA11y
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.viewExternally
import com.instructure.pandautils.views.ExpandCollapseAnimation
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.pandautils.views.ViewPagerNonSwipeable
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.databinding.ViewSubmissionContentBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.RationedBusEvent
import com.instructure.teacher.fragments.SimpleWebViewFragment
import com.instructure.teacher.fragments.SpeedGraderCommentsFragment
import com.instructure.teacher.fragments.SpeedGraderEmptyFragment
import com.instructure.teacher.fragments.SpeedGraderFilesFragment
import com.instructure.teacher.fragments.SpeedGraderGradeFragment
import com.instructure.teacher.fragments.SpeedGraderLtiSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderQuizSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderTextSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderUrlSubmissionFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewMediaFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.Const
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.getResForSubmission
import com.instructure.teacher.utils.getState
import com.instructure.teacher.utils.iconRes
import com.instructure.teacher.utils.isTablet
import com.instructure.teacher.utils.setAnonymousAvatar
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.transformForQuizGrading
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ViewConstructor")
class SubmissionContentView(
    context: Context,
    private val studentSubmission: GradeableStudentSubmission,
    private val assignment: Assignment,
    private val course: Course,
    var initialTabIndex: Int = 0
) : PdfSubmissionView(context, courseId = course.id), AnnotationManager.OnAnnotationCreationModeChangeListener, AnnotationManager.OnAnnotationEditingModeChangeListener {

    private val binding: ViewSubmissionContentBinding

    override val annotationToolbarLayout: ToolbarCoordinatorLayout
        get() = binding.annotationToolbarLayout
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
        get() = binding.inspectorCoordinatorLayout
    override val commentsButton: ImageView
        get() = binding.commentsButton
    override val loadingContainer: FrameLayout
        get() = binding.loadingContainer
    override val progressBar: ProgressiveCanvasLoadingView
        get() = binding.speedGraderProgressBar
    override val progressColor: Int
        get() = R.color.login_teacherAppTheme

    private var containerId: Int = 0
    private val assignee: Assignee get() = studentSubmission.assignee
    private val rootSubmission: Submission? get() = studentSubmission.submission
    private val bottomViewPager: ViewPagerNonSwipeable

    private var initJob: Job? = null
    private var deleteJob: Job? = null
    private var studentAnnotationJob: Job? = null

    private var isCleanedUp = false
    private val activity: SpeedGraderActivity get() = context as SpeedGraderActivity
    private val gradeFragment by lazy { SpeedGraderGradeFragment.newInstance(rootSubmission, assignment, course, assignee) }

    val hasUnsavedChanges: Boolean
        get() = gradeFragment.hasUnsavedChanges

    private var selectedSubmission: Submission? = null
    private var assignmentEnhancementsEnabled = false

    override fun showNoInternetDialog() {
        NoInternetConnectionDialog.show(supportFragmentManager)
    }

    override fun disableViewPager() {
        activity.disableViewPager()
    }

    override fun enableViewPager() {
        activity.enableViewPager()
    }

    override fun setIsCurrentlyAnnotating(boolean: Boolean) {
        activity.isCurrentlyAnnotating = boolean
    }

    override fun showFileError() {
        showMessageFragment(R.string.error_loading_files)
    }

    override fun showAnnotationComments(commentList: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues) {
        val bundle = AnnotationCommentListFragment.makeBundle(commentList, headAnnotationId, docSession, apiValues, assignee.id)
        //if isTablet, we need to prevent the sliding panel from moving opening all the way with the keyboard
        if(context.isTablet) {
            setIsCurrentlyAnnotating(true)
        }
        RouteMatcher.route(activity as FragmentActivity, Route(AnnotationCommentListFragment::class.java, null, bundle))
    }

    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (!isCleanedUp && isAttachedToWindow) supportFragmentManager.beginTransaction().replace(containerId, fragment).commitNowAllowingStateLoss()

        //if we can share the content with another app, show the share icon
        binding.speedGraderToolbar.menu.findItem(R.id.menu_share).apply {
            isVisible = fragment is ShareableFile || fragment is PdfFragment
            iconTintList = ContextCompat.getColorStateList(context, R.color.textDarkest)
        }
    }

    override fun removeContentFragment() {
        val contentFragment = supportFragmentManager.findFragmentById(containerId)
        if (contentFragment != null) {
            supportFragmentManager.beginTransaction().remove(contentFragment).commitAllowingStateLoss()
        }
    }

    //region view lifecycle
    init {
        binding = ViewSubmissionContentBinding.inflate(LayoutInflater.from(context), this, true)

        setLoading(true)

        //if we have anonymous peer reviews we don't want the teacher to be able to annotate
        if (assignment.isPeerReviews && assignment.anonymousPeerReviews) {
            annotationToolbarLayout.setGone()
            inspectorCoordinatorLayout.setGone()
        }

        containerId = View.generateViewId()
        binding.content.id = containerId
        bottomViewPager = binding.bottomViewPager.apply { id = View.generateViewId() }

        initializeSubmissionView()

        if (isAccessibilityEnabled(context)) {
            binding.slidingUpPanelLayout?.anchorPoint = 1.0f
        }
        setupExpandCollapseToggle()
    }

    private fun setupExpandCollapseToggle() {
        binding.toggleImageView?.let { toggle ->
            binding.panelContent?.let { panel ->
                val panelWidth = resources.getDimensionPixelOffset(R.dimen.speedgraderPanelWidth)
                val animation = ExpandCollapseAnimation(
                    panel,
                    panelWidth,
                    0
                ) {
                    if (panel.width > 50) {
                        binding.toggleImageView.setImageResource(R.drawable.ic_collapse_horizontal)
                        binding.toggleImageView.contentDescription =
                            context.getString(R.string.collapseGradePanel)
                    } else {
                        binding.toggleImageView.setImageResource(R.drawable.ic_expand_horizontal)
                        binding.toggleImageView.contentDescription =
                            context.getString(R.string.expandGradePanel)
                    }
                }
                animation.duration = 500
                toggle.onClick {
                    if (!animation.hasStarted() || animation.hasEnded()) {
                        panel.clearAnimation()
                        if (panel.width > 0) {
                            animation.updateValues(panelWidth, 0)
                        } else {
                            animation.updateValues(0, panelWidth)
                        }
                        panel.startAnimation(animation)
                    }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupToolbar(assignee)
        obtainSubmissionData()
    }

    private fun setLoading(isLoading: Boolean) = with(binding) {
        retryLoadingContainer.setGone()
        loadingView.setVisible(isLoading)
        slidingUpPanelLayout?.setVisible(!isLoading)
        panelContent?.setVisible(!isLoading)
        contentRoot.setVisible(!isLoading)
        divider?.setVisible(!isLoading)
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun obtainSubmissionData() {
        initJob = tryWeave {
            if (!studentSubmission.isCached) {
                // Determine if the logged in user is an Observer
                val enrollments = awaitApi<List<Enrollment>> { EnrollmentManager.getObserveeEnrollments(true, it) }
                val isObserver = enrollments.any { it.isObserver }
                if (isObserver) {
                    // Get the first observee associated with this course
                    val observee = enrollments.first { it.courseId == course.id }
                    studentSubmission.submission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(course.id, assignment.id, studentSubmission.assigneeId, it, true) }
                } else {
                    // Get the user's submission normally
                    studentSubmission.submission = awaitApi<Submission> {
                        SubmissionManager.getSingleSubmission(
                            course.id,
                            assignment.id,
                            studentSubmission.assigneeId,
                            it,
                            true
                        )
                    }
                }
                val featureFlags = FeaturesManager.getEnabledFeaturesForCourseAsync(course.id, true).await().dataOrNull
                assignmentEnhancementsEnabled = featureFlags?.contains("assignments_2_student").orDefault()
                studentSubmission.isCached = true
            }
            setup()
        } catch {
            with(binding) {
                loadingView.setGone()
                retryLoadingContainer.setVisible()
                retryLoadingButton.onClick {
                    setLoading(true)
                    obtainSubmissionData()
                }
            }
        }
    }

    fun setup() {
        if (SubmissionType.ONLINE_QUIZ in assignment.getSubmissionTypes()) rootSubmission?.transformForQuizGrading()
        setupToolbar(assignee)
        setupDueDate(assignment)
        setupSubmissionVersions(rootSubmission?.submissionHistory?.filterNotNull()?.filter { it.attempt > 0 })
        setSubmission(rootSubmission)
        setupBottomSheetViewPager(course)
        setupSlidingPanel()
        //we must set up the sliding panel prior to registering to the event
        EventBus.getDefault().register(this)
        setLoading(false)
    }

    private fun setupDueDate(assignment: Assignment) = with(binding) {
        if (assignment.dueDate != null) {
            dueDateTextView?.setVisible(true)
            dueDateTextView?.text = DateHelper.getDateAtTimeString(context, R.string.due_dateTime, assignment.dueDate)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        initJob?.cancel()
        studentAnnotationJob?.cancel()
        bottomViewPager.adapter = null
        EventBus.getDefault().unregister(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = with(binding) {
        // Resize sliding panel and content, don't if keyboard based annotations are active or selected
        if (oldh > 0 && oldh != h && !activity.isCurrentlyAnnotating && pdfFragment?.selectedAnnotations?.isEmpty() != false) {
            val newState = when {
                context.isTablet -> slidingUpPanelLayout?.panelState ?: SlidingUpPanelLayout.PanelState.ANCHORED
                h < oldh -> SlidingUpPanelLayout.PanelState.EXPANDED
                else -> SlidingUpPanelLayout.PanelState.ANCHORED
            }

            if (newState != SlidingUpPanelLayout.PanelState.DRAGGING) {
                slidingUpPanelLayout?.panelState = newState
            }

            // Have to post here as we wait for contentRoot height to settle
            contentRoot.post {
                val slideOffset = when (newState) {
                    SlidingUpPanelLayout.PanelState.EXPANDED -> 1f
                    SlidingUpPanelLayout.PanelState.ANCHORED -> 0.5f
                    else -> 0f
                }

                val maxHeight = contentRoot.height
                val adjustedHeight = Math.abs(maxHeight * slideOffset).toInt()

                if (slideOffset >= 0.50F) { //Prevents resizing views when not necessary
                    this@SubmissionContentView.bottomViewPager.layoutParams?.height = adjustedHeight
                    this@SubmissionContentView.bottomViewPager.requestLayout()
                } else if (slideOffset <= 0.50F) {
                    contentWrapper?.layoutParams?.height = Math.abs(maxHeight - adjustedHeight)
                    contentWrapper?.requestLayout()
                }
            }
        }
    }

    @SuppressLint("CommitTransaction")
    fun performCleanup() {
        isCleanedUp = true
        getCurrentFragment()?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
    }
    //endregion

    //region private helpers
    private fun setSubmission(submission: Submission?) {
        selectedSubmission = submission
        val content = when {
            SubmissionType.NONE.apiString in assignment.submissionTypesRaw -> NoneContent
            SubmissionType.ON_PAPER.apiString in assignment.submissionTypesRaw -> OnPaperContent
            submission?.submissionType == null -> NoSubmissionContent
            assignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_MISSING ||
                    assignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING -> NoSubmissionContent
            else -> when (Assignment.getSubmissionTypeFromAPIString(submission.submissionType!!)) {

            // LTI submission
                SubmissionType.BASIC_LTI_LAUNCH -> ExternalToolContent(
                        submission.previewUrl.validOrNull() ?: assignment.url.validOrNull()
                        ?: assignment.htmlUrl ?: ""
                )

            // Text submission
                SubmissionType.ONLINE_TEXT_ENTRY -> TextContent(submission.body ?: "")

            // Media submission
                SubmissionType.MEDIA_RECORDING -> submission.mediaComment?.let {
                    MediaContent(
                            uri = Uri.parse(it.url),
                            contentType = it.contentType ?: "",
                        displayName = it.displayName
                    )
                } ?: UnsupportedContent

                // File uploads
                SubmissionType.ONLINE_UPLOAD -> getAttachmentContent(submission.attachments[0])

                // URL Submission
                SubmissionType.ONLINE_URL -> UrlContent(submission.url!!, submission.attachments.firstOrNull()?.url)

                // Quiz Submission
                SubmissionType.ONLINE_QUIZ -> handleQuizSubmissionType(submission)

                // Discussion Submission
                SubmissionType.DISCUSSION_TOPIC -> DiscussionContent(submission.previewUrl)

                SubmissionType.STUDENT_ANNOTATION -> {
                    StudentAnnotationContent(submission.id, submission.attempt)
                }

                // Unsupported type
                else -> UnsupportedContent
            }
        }
        (bottomViewPager.adapter as? BottomSheetPagerAdapter)?.refreshFilesTabCount(submission?.attachments?.size
            ?: 0)
        setGradeableContent(content)
    }

    private fun handleQuizSubmissionType(submission: Submission): GradeableContent {
        return if (assignment.anonymousGrading) {
            AnonymousSubmissionContent
        } else {
            QuizContent(
                course.id,
                assignment.id,
                submission.userId,
                submission.previewUrl ?: "",
                QuizSubmission.parseWorkflowState(submission.workflowState!!) == QuizSubmission.WorkflowState.PENDING_REVIEW
            )
        }
    }

    private fun getAttachmentContent(attachment: Attachment): GradeableContent {
        var type = attachment.contentType ?: return OtherAttachmentContent(attachment)
        if (type == "*/*") {
            val fileExtension = attachment.filename?.substringAfterLast(".") ?: ""
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                ?: MimeTypeMap.getFileExtensionFromUrl(attachment.url)
                    ?: type
        }
        return when {
            type == "application/pdf" || (attachment.previewUrl?.contains(Const.CANVADOC) ?: false) -> {
                if (attachment.previewUrl?.contains(Const.CANVADOC) == true) {
                    PdfContent(attachment.previewUrl ?: "")
                } else {
                    PdfContent(attachment.url ?: "")
                }
            }
            type.startsWith("audio") || type.startsWith("video") -> with(attachment) {
                MediaContent(
                        uri = Uri.parse(url),
                        thumbnailUrl = thumbnailUrl,
                        contentType = contentType,
                        displayName = displayName
                )
            }
            type.startsWith("image") -> ImageContent(attachment.url ?: "", attachment.contentType!!)
            else -> OtherAttachmentContent(attachment)
        }
    }

    private fun setAttachmentContent(attachment: Attachment) {
        setGradeableContent(getAttachmentContent(attachment))
    }

    private fun setupSubmissionVersions(unsortedSubmissions: List<Submission>?) = with(binding.submissionVersionsSpinner) {
        if (unsortedSubmissions.isNullOrEmpty()) {
            setGone()
        } else {
            unsortedSubmissions.sortedByDescending { it.submittedAt }.let { submissions ->
                val itemViewModels = submissions.mapIndexed { index, submission ->
                    AssignmentDetailsAttemptItemViewModel(
                        AssignmentDetailsAttemptViewData(
                            context.getString(R.string.attempt, unsortedSubmissions.size - index),
                            submission.submittedAt?.let { getFormattedAttemptDate(it) }.orEmpty()
                        )
                    )
                }
                adapter = BindableSpinnerAdapter(context, R.layout.item_submission_attempt_spinner, itemViewModels)
                setSelection(0, false)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        EventBus.getDefault().post(SubmissionSelectedEvent(submissions[position]))
                    }
                }

                if (submissions.size > 1) {
                    setVisible()
                } else {
                    setGone()
                    binding.attemptView?.apply {
                        itemViewModel = itemViewModels.firstOrNull()
                        root.setVisible()
                    }
                }
            }
        }
    }

    private fun getFormattedAttemptDate(date: Date): String = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
        Locale.getDefault()
    ).format(date)

    private fun setupToolbar(assignee: Assignee) = with(binding) {
        val assigneeName = if (assignment.anonymousGrading) {
            resources.getString(R.string.anonymousStudentLabel)
        } else {
            Pronouns.span(assignee.name, assignee.pronouns)
        }
        titleTextView.text = assigneeName

        if (studentSubmission.isCached) {
            // get string/color resources for assignment status
            val (stringRes, colorRes) = assignment.getResForSubmission(rootSubmission)
            if (stringRes == -1 || colorRes == -1) {
                contentDescription = titleTextView.text
                subtitleTextView.setGone()
            } else {
                contentDescription = "${titleTextView.text}, ${resources.getString(stringRes)}"
                subtitleTextView.setText(stringRes)
                subtitleTextView.setTextColor(context.getColorCompat(colorRes))
            }
        }

        speedGraderToolbar.setupMenu(R.menu.menu_share_file, menuItemCallback)
        speedGraderToolbar.foregroundGravity = Gravity.CENTER_VERTICAL
        ViewStyler.setToolbarElevationSmall(context, speedGraderToolbar)

        when {
            assignment.anonymousGrading -> userImageView.setAnonymousAvatar()
            assignee is GroupAssignee -> userImageView.setImageResource(assignee.iconRes)
            assignee is StudentAssignee -> {
                ProfileUtils.loadAvatarForUser(userImageView, assignee.student.name, assignee.student.avatarUrl)
                userImageView.setupAvatarA11y(assignee.name)
                userImageView.onClick {
                    val bundle = StudentContextFragment.makeBundle(assignee.id, course.id)
                    RouteMatcher.route(activity as FragmentActivity, Route(StudentContextFragment::class.java, null, bundle))
                }
            }
        }

        if (assignee is GroupAssignee && !assignment.anonymousGrading) setupGroupMemberList(assignee)
    }

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_share -> {
                (getCurrentFragment() as? ShareableFile)?.viewExternally()

                //pdfs are a different type of fragment
                if (pdfFragment != null) {
                    pdfFragment?.document?.documentSource?.fileUri?.viewExternally(context, "application/pdf")
                }
            }
        }
    }

    private fun setupGroupMemberList(assignee: GroupAssignee) = with(binding) {
        assigneeWrapperView.onClick {
            val popup = ListPopupWindow(context)
            popup.anchorView = it
            popup.setAdapter(object : ArrayAdapter<User>(context, 0, assignee.students) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val user = getItem(position)
                    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.adapter_speed_grader_group_member, parent, false)
                    val memberAvatarView = view.findViewById<ImageView>(R.id.memberAvatarView)
                    ProfileUtils.loadAvatarForUser(memberAvatarView, user?.name, user?.avatarUrl)
                    val memberNameView = view.findViewById<TextView>(R.id.memberNameView)
                    memberNameView.text = Pronouns.span(user?.name, user?.pronouns)
                    return view
                }
            })
            popup.setContentWidth(resources.getDimensionPixelSize(R.dimen.speedgraderGroupMemberListWidth))
            popup.verticalOffset = -assigneeWrapperView.height
            popup.isModal = true // For a11y
            popup.setOnItemClickListener { _, _, position, _ ->
                val bundle = StudentContextFragment.makeBundle(assignee.students[position].id, course.id)
                RouteMatcher.route(activity as FragmentActivity, Route(StudentContextFragment::class.java, null, bundle))
                popup.dismiss()
            }
            popup.show()
        }
    }

    private fun setGradeableContent(content: GradeableContent) = with(binding) {
        // Handle the existing PdfFragment if there is one
        val currentFragment = getCurrentFragment()
        if (currentFragment is PdfFragment) {
            // Unregister listeners for the existing fragment
            unregisterPdfFragmentListeners()
        }

        topDivider?.setVisible(!(content is PdfContent))

        when (content) {
            is PdfContent -> {
                if(content.url.contains("canvadoc")) {
                    if(slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        // Attempt to reset the sliding panel to collapsed, so we don't render the pdf at anchored size
                        slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    }
                    handlePdfContent(content.url)
                } else {
                    showMessageFragment(R.string.pdfError)
                }
            }
            is NoSubmissionContent -> when (assignee) {
                is StudentAssignee -> showMessageFragment(R.string.noSubmission, R.string.noSubmissionTeacher)
                is GroupAssignee -> showMessageFragment(R.string.speedgrader_group_no_submissions)
            }
            is UnsupportedContent -> showMessageFragment(R.string.speedgrader_unsupported_type)
            is UrlContent -> setFragment(SpeedGraderUrlSubmissionFragment.newInstance(content.url, content.previewUrl))
            is QuizContent -> setFragment(SpeedGraderQuizSubmissionFragment.newInstance(content))
            is OtherAttachmentContent -> with(content.attachment) {
                setFragment(ViewUnsupportedFileFragment.newInstance(
                        uri = Uri.parse(url),
                        displayName = displayName ?: filename ?: "",
                        contentType = contentType ?: "",
                        previewUri = thumbnailUrl?.let { Uri.parse(it) },
                        fallbackIcon = iconRes
                ))
            }
            is TextContent -> setFragment(SpeedGraderTextSubmissionFragment.newInstance(content.text))
            is MediaContent -> setFragment(ViewMediaFragment.newInstance(content))
            is ImageContent -> load(content.url) { setFragment(ViewImageFragment.newInstance(content.url, it, content.contentType, false)) }
            is NoneContent -> showMessageFragment(R.string.speedGraderNoneMessage)
            is ExternalToolContent -> setFragment(SpeedGraderLtiSubmissionFragment.newInstance(content))
            is OnPaperContent -> showMessageFragment(R.string.speedGraderOnPaperMessage)
            is DiscussionContent -> setFragment(SimpleWebViewFragment.newInstance(content.previewUrl!!))
            is AnonymousSubmissionContent -> showMessageFragment(R.string.speedGraderAnonymousSubmissionMessage)
            is StudentAnnotationContent -> {
                studentAnnotationJob = tryWeave {
                    val canvaDocSession = CanvaDocsManager.createCanvaDocSessionAsync(
                        content.submissionId,
                        content.attempt.toString()
                    ).await().dataOrThrow
                    handlePdfContent(canvaDocSession.canvadocsSessionUrl ?: "")
                } catch {
                    toast(R.string.errorLoadingSubmission)
                }
            }
        }.exhaustive
    }

    private fun showMessageFragment(@StringRes stringRes: Int) = showMessageFragment(resources.getString(stringRes))

    private fun showMessageFragment(@StringRes titleRes: Int, @StringRes messageRes: Int) =
            showMessageFragment(resources.getString(titleRes), resources.getString(messageRes))

    private fun showMessageFragment(message: String) {
        val fragment = SpeedGraderEmptyFragment.newInstance(message = message)
        setFragment(fragment)
    }

    private fun showMessageFragment(title: String, message: String) {
        val fragment = SpeedGraderEmptyFragment.newInstance(title = title, message = message)
        setFragment(fragment)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(containerId)
    }

    override fun attachDocListener() {
        if (!(assignment.anonymousPeerReviews && assignment.isPeerReviews)) {
            // We don't need to do annotations if there are anonymous peer reviews
            if(docSession.annotationMetadata?.canWrite() == true) {
                if ((context as Activity).isTablet)
                    pdfFragment?.setInsets(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68f, context.resources.displayMetrics).toInt(), 0, 0)
                else
                    pdfFragment?.setInsets(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics).toInt(), 0, 0)
            }
            super.attachDocListener()
        } else {
            pdfFragment?.setInsets(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics).toInt(), 0, 0)
        }
    }

    private fun setupSlidingPanel() = with(binding) {

        slidingUpPanelLayout?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {

            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                adjustPanelHeights(slideOffset)
            }

            override fun onPanelStateChanged(panel: View?,
                                             previousState: SlidingUpPanelLayout.PanelState?,
                                             newState: SlidingUpPanelLayout.PanelState?) {
                if (newState != previousState) {
                    // We don't want to update for all states, just these three
                    when (newState) {
                        SlidingUpPanelLayout.PanelState.ANCHORED -> {
                            submissionVersionsSpinner.isClickable = true
                            postPanelEvent(newState, 0.5f)
                            contentRoot.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        }
                        SlidingUpPanelLayout.PanelState.EXPANDED -> {
                            submissionVersionsSpinner.isClickable = false
                            postPanelEvent(newState, 1.0f)
                            contentRoot.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        }
                        SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                            submissionVersionsSpinner.isClickable = true
                            //fix for rotating when the panel is collapsed
                            pdfFragment?.notifyLayoutChanged()
                            postPanelEvent(newState, 0.0f)
                            contentRoot.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        }
                        else -> {}
                    }
                }
            }
        })
    }

    private fun postPanelEvent(panelState: SlidingUpPanelLayout.PanelState, offset: Float) {
        val event = SlidingPanelAnchorEvent(panelState, offset)
        EventBus.getDefault().postSticky(event)
    }

    private fun adjustPanelHeights(offset: Float) {
        //Adjusts the panel content sizes based on the position of the sliding portion of the view
        val maxHeight = binding.contentRoot.height
        if (offset < 0 || maxHeight == 0) return

        val adjustedHeight = Math.abs(maxHeight * offset)

        if (offset >= 0.50F) { //Prevents resizing views when not necessary
            bottomViewPager.layoutParams?.height = adjustedHeight.toInt()
            bottomViewPager.requestLayout()
        }
    }

    private fun setupBottomSheetViewPager(course: Course) = with(binding) {
        this@SubmissionContentView.bottomViewPager.offscreenPageLimit = 2
        this@SubmissionContentView.bottomViewPager.adapter = BottomSheetPagerAdapter.Holder(supportFragmentManager)
                .add(gradeFragment)
                .add(SpeedGraderCommentsFragment.newInstance(
                        rootSubmission,
                        assignee,
                        this@SubmissionContentView.course.id,
                        assignment.id,
                        assignment.groupCategoryId > 0 && assignee is GroupAssignee,
                        assignment.anonymousGrading,
                        assignmentEnhancementsEnabled
                ))
                .add(SpeedGraderFilesFragment.newInstance(rootSubmission))
                .setFileCount(rootSubmission?.attachments?.size ?: 0)
                .set()

        this@SubmissionContentView.bottomViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {

                }
            }
        })

        bottomTabLayout.setupWithViewPager(this@SubmissionContentView.bottomViewPager)
        bottomTabLayout.setSelectedTabIndicatorColor(course.color)
        bottomTabLayout.setTabTextColors(ContextCompat.getColor(context, R.color.textDarkest), course.color)
        bottomTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                EventBus.getDefault().post(TabSelectedEvent(tab?.position ?: 0))
                if (slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                }
            }
        })

        val spacing = resources.getDimensionPixelOffset(R.dimen.speedgrader_tab_spacing)

        for (i in 0 until bottomTabLayout.tabCount) {
            val tab = (bottomTabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val params = tab.layoutParams as MarginLayoutParams
            params.setMargins(spacing, 0, spacing, 0)
            tab.requestLayout()
        }

        this@SubmissionContentView.bottomViewPager.currentItem = initialTabIndex
    }

    private fun showVideoCommentDialog() = with(binding) {
        activity.disableViewPager()
        floatingRecordingView.setContentType(RecordingMediaType.Video)
        floatingRecordingView.startVideoView()
        floatingRecordingView.recordingCallback = {
            it?.let {
                EventBus.getDefault().post(UploadMediaCommentEvent(it, assignment.id, assignment.courseId, assignee.id, selectedSubmission?.attempt))
            }
        }
        floatingRecordingView.stoppedCallback = {
            activity.enableViewPager()
            EventBus.getDefault().post(MediaCommentDialogClosedEvent())
        }
        floatingRecordingView.replayCallback = {
            val bundle = BaseViewMediaActivity.makeBundle(it, "video", context.getString(R.string.videoCommentReplay), true)
            RouteMatcher.route(activity as FragmentActivity, Route(bundle, RouteContext.MEDIA))
        }
    }

    private fun showAudioCommentDialog() = with(binding) {
        activity.disableViewPager()
        floatingRecordingView.setContentType(RecordingMediaType.Audio)
        floatingRecordingView.setVisible()
        floatingRecordingView.stoppedCallback = {
            activity.enableViewPager()
            EventBus.getDefault().post(MediaCommentDialogClosedEvent())
        }
        floatingRecordingView.recordingCallback = {
            it?.let {
                EventBus.getDefault().post(UploadMediaCommentEvent(it, assignment.id, assignment.courseId, assignee.id, selectedSubmission?.attempt))
            }
        }
    }

    private fun getAudioPermission() {
        ActivityCompat.requestPermissions((context as SpeedGraderActivity), arrayOf(PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
    }

    private fun getVideoPermission() {
        ActivityCompat.requestPermissions((context as SpeedGraderActivity), arrayOf(PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
    }
    //endregion

    private class BottomSheetPagerAdapter internal constructor(fm: FragmentManager, fragments: ArrayList<Fragment>, var fileCount: Int = 0) : FragmentPagerAdapter(fm) {

        private var fragments = ArrayList<Fragment>()

        init {
            this.fragments = fragments
        }

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int) = when (position) {
            0 -> ContextKeeper.appContext.getString(R.string.sg_tab_grade).uppercase(Locale.getDefault())
            1 -> ContextKeeper.appContext.getString(R.string.sg_tab_comments).uppercase(Locale.getDefault())
            2 -> ContextKeeper.appContext.getString(R.string.sg_tab_files_w_counter, fileCount).uppercase(Locale.getDefault())
            else -> ""
        }

        fun refreshFilesTabCount(fileCount: Int) {
            this.fileCount = fileCount
            notifyDataSetChanged()
        }

        internal class Holder(private val manager: FragmentManager) {

            private val fragments = ArrayList<Fragment>()
            private var fileCount: Int = 0

            fun add(f: Fragment): Holder {
                fragments.add(f)
                return this
            }

            fun setFileCount(fileCount: Int): Holder {
                this.fileCount = fileCount
                return this
            }

            fun set() = BottomSheetPagerAdapter(manager, fragments, fileCount)
        }

        override fun finishUpdate(container: ViewGroup) {
            // Workaround for known issue in the support library
            try {
                super.finishUpdate(container)
            } catch (nullPointerException: NullPointerException) {
            }
        }
    }

    //endregion

    //region event bus subscriptions
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchSubmission(event: SubmissionSelectedEvent) {
        //close the annotations toolbar so it can be associated with new document
        pdfFragment?.exitCurrentlyActiveMode()
        if (event.submission?.id == rootSubmission?.id) setSubmission(event.submission)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchAttachment(event: SubmissionFileSelectedEvent) {
        if (event.submissionId == rootSubmission?.id) {
            //close the annotations toolbar so it can be associated with new document
            pdfFragment?.exitCurrentlyActiveMode()
            setAttachmentContent(event.attachment)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAnchorChanged(event: SlidingPanelAnchorEvent) {
        binding.slidingUpPanelLayout?.panelState = event.anchorPosition
        //If we try to adjust the panels before contentRoot's height is determined, things don't work
        //This post works because we setup the panel before registering to the event
        binding.contentRoot.post { adjustPanelHeights(event.offset) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentTextFocused(event: CommentTextFocusedEvent) {
        if (event.assigneeId == assignee.id) {
            activity.isCurrentlyAnnotating = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentAdded(event: AnnotationCommentAdded) {
        if (event.assigneeId == assignee.id) {
            //add the comment to the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.add(event.annotation)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentEdited(event: AnnotationCommentEdited) {
        if (event.assigneeId == assignee.id) {
            //update the annotation in the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.find { it.annotationId == event.annotation.annotationId }?.contents = event.annotation.contents
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleted(event: AnnotationCommentDeleted) {
        if (event.assigneeId == assignee.id) {
            if (event.isHeadAnnotation) {
                //we need to delete the entire list of comments from the hashmap
                commentRepliesHashMap.remove(event.annotation.inReplyTo)
                pdfFragment?.selectedAnnotations?.get(0)?.contents = ""
                noteHinter?.notifyDrawablesChanged()
            } else {
                //otherwise just remove the comment
                commentRepliesHashMap[event.annotation.inReplyTo]?.remove(event.annotation)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleteAcknowledged(event: AnnotationCommentDeleteAcknowledged) {
        if (event.assigneeId == assignee.id) {
            deleteJob = tryWeave {
                for(annotation in event.annotationList) {
                    awaitApi<ResponseBody> { CanvaDocsManager.deleteAnnotation(apiValues.sessionId, annotation.annotationId, apiValues.canvaDocsDomain, it) }
                    commentRepliesHashMap[annotation.inReplyTo]?.remove(annotation)
                }
            } catch {
                Logger.d("There was an error acknowledging the delete!")
            }
        }
    }

    @Subscribe
    fun onTabSelected(event: TabSelectedEvent) {
        bottomViewPager.currentItem = event.selectedTabIdx
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAudioPermissionGranted(event: AudioPermissionGrantedEvent) {
        if (compareAssignees(event.assigneeId))
            showAudioCommentDialog()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoPermissionGranted(event: VideoPermissionGrantedEvent) {
        if (compareAssignees(event.assigneeId))
            showVideoCommentDialog()
    }

    private fun compareAssignees(eventAssigneeId: Long): Boolean{
        return if(assignee is GroupAssignee) {
            (assignee as GroupAssignee).group.users.any { it.id == eventAssigneeId }
        } else {
            return eventAssigneeId == assignee.id
        }
    }
    //endregion
}

class SubmissionSelectedEvent(val submission: Submission?)
class SubmissionFileSelectedEvent(val submissionId: Long, val attachment: Attachment)
class QuizSubmissionGradedEvent(submission: Submission) : RationedBusEvent<Submission>(submission)
class SlidingPanelAnchorEvent(val anchorPosition: SlidingUpPanelLayout.PanelState, val offset: Float)
class CommentTextFocusedEvent(val assigneeId: Long)
class AnnotationCommentAdded(val annotation: CanvaDocAnnotation, val assigneeId: Long)
class AnnotationCommentEdited(val annotation: CanvaDocAnnotation, val assigneeId: Long)
class AnnotationCommentDeleted(val annotation: CanvaDocAnnotation, val isHeadAnnotation: Boolean, val assigneeId: Long)
class AnnotationCommentDeleteAcknowledged(val annotationList: List<CanvaDocAnnotation>, val assigneeId: Long)
class TabSelectedEvent(val selectedTabIdx: Int)
class UploadMediaCommentEvent(val file: File, val assignmentId: Long, val courseId: Long, val assigneeId: Long, val attemptId: Long?)

class MediaCommentDialogClosedEvent
class AudioPermissionGrantedEvent(val assigneeId: Long)
class VideoPermissionGrantedEvent(val assigneeId: Long)