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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.instructure.annotations.PdfSubmissionView
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.dialog.RadioButtonDialog
import com.instructure.teacher.events.RationedBusEvent
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.fragments.*
import com.instructure.teacher.interfaces.SpeedGraderWebNavigator
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.utils.Const
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.adapter_speed_grader_group_member.view.*
import kotlinx.android.synthetic.main.view_submission_content.view.*
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.ArrayList
import java.util.Locale

@SuppressLint("ViewConstructor")
class SubmissionContentView(
        context: Context,
        private val mStudentSubmission: GradeableStudentSubmission,
        private val mAssignment: Assignment,
        private val mCourse: Course,
        var initialTabIndex: Int = 0
) : PdfSubmissionView(context), AnnotationManager.OnAnnotationCreationModeChangeListener, AnnotationManager.OnAnnotationEditingModeChangeListener {

    override val annotationToolbarLayout: ToolbarCoordinatorLayout
        get() = findViewById(R.id.annotationToolbarLayout)
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
        get() = findViewById(R.id.inspectorCoordinatorLayout)
    override val commentsButton: ImageView
        get() = findViewById(R.id.commentsButton)
    override val loadingContainer: FrameLayout
        get() = findViewById(R.id.loadingContainer)
    override val progressBar: ProgressiveCanvasLoadingView
        get() = findViewById(R.id.speedGraderProgressBar)
    override val progressColor: Int
        get() = R.color.login_teacherAppTheme

    private var mContainerId: Int = 0
    private val mAssignee: Assignee get() = mStudentSubmission.assignee
    private val mRootSubmission: Submission? get() = mStudentSubmission.submission
    private val mBottomViewPager: ViewPagerNoSwipe

    private var initJob: Job? = null
    private var deleteJob: Job? = null

    private var mIsCleanedUp = false
    private val activity: SpeedGraderActivity get() = context as SpeedGraderActivity
    private val mGradeFragment by lazy { SpeedGraderGradeFragment.newInstance(mRootSubmission, mAssignment, mCourse, mAssignee) }

    val hasUnsavedChanges: Boolean
        get() = mGradeFragment.hasUnsavedChanges

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
        val bundle = AnnotationCommentListFragment.makeBundle(commentList, headAnnotationId, docSession, apiValues, mAssignee.id)
        //if isTablet, we need to prevent the sliding panel from moving opening all the way with the keyboard
        if(context.isTablet) {
            setIsCurrentlyAnnotating(true)
        }
        RouteMatcher.route(context, Route(AnnotationCommentListFragment::class.java, null, bundle))
    }

    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (!mIsCleanedUp && isAttachedToWindow) supportFragmentManager.beginTransaction().replace(mContainerId, fragment).commitNowAllowingStateLoss()

        //if we can share the content with another app, show the share icon
        speedGraderToolbar.menu.findItem(R.id.menu_share)?.isVisible = fragment is ShareableFile || fragment is PdfFragment

        ViewStyler.colorToolbarIconsAndText(context as Activity, speedGraderToolbar, Color.BLACK)
    }

    //region view lifecycle
    init {
        View.inflate(context, R.layout.view_submission_content, this)

        setLoading(true)

        //if we have anonymous peer reviews we don't want the teacher to be able to annotate
        if (mAssignment.isPeerReviews && mAssignment.anonymousPeerReviews) {
            annotationToolbarLayout.setGone()
            inspectorCoordinatorLayout.setGone()
        }

        mContainerId = View.generateViewId()
        content.id = mContainerId
        mBottomViewPager = bottomViewPager.apply { id = View.generateViewId() }

        initializeSubmissionView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupToolbar(mAssignee)
        obtainSubmissionData()
    }

    private fun setLoading(isLoading: Boolean) {
        retryLoadingContainer.setGone()
        loadingView?.setVisible(isLoading)
        slidingUpPanelLayout?.setVisible(!isLoading)
        panelContent?.setVisible(!isLoading)
        contentRoot?.setVisible(!isLoading)
        divider?.setVisible(!isLoading)
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun obtainSubmissionData() {
        initJob = tryWeave {
            if (!mStudentSubmission.isCached) {
                // Determine if the logged in user is an Observer
                val enrollments = awaitApi<List<Enrollment>> { EnrollmentManager.getObserveeEnrollments(true, it) }
                val isObserver = enrollments.any { it.isObserver }
                if (isObserver) {
                    // Get the first observee associated with this course
                    val observee = enrollments.first { it.courseId == mCourse.id }
                    mStudentSubmission.submission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(mCourse.id, mAssignment.id, mStudentSubmission.assigneeId, it, true) }
                } else {
                    // Get the user's submission normally
                    mStudentSubmission.submission = awaitApi<Submission> {
                        SubmissionManager.getSingleSubmission(
                            mCourse.id,
                            mAssignment.id,
                            mStudentSubmission.assigneeId,
                            it,
                            true
                        )
                    }
                }
                mStudentSubmission.isCached = true
            }
            setup()
        } catch {
            loadingView.setGone()
            retryLoadingContainer.setVisible()
            retryLoadingButton.onClick {
                setLoading(true)
                obtainSubmissionData()
            }
        }
    }

    fun setup() {
        if (SubmissionType.ONLINE_QUIZ in mAssignment.getSubmissionTypes()) mRootSubmission?.transformForQuizGrading()
        setupToolbar(mAssignee)
        setupSubmissionVersions(mRootSubmission?.submissionHistory?.filterNotNull()?.filter { it.attempt > 0 })
        setSubmission(mRootSubmission)
        setupBottomSheetViewPager(mCourse)
        setupSlidingPanel()
        //we must set up the sliding panel prior to registering to the event
        EventBus.getDefault().register(this)
        setLoading(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        initJob?.cancel()
        mBottomViewPager.adapter = null
        EventBus.getDefault().unregister(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        if (context.isTablet) return

        // Resize sliding panel and content, don't if keyboard based annotations are active
        // we only do this if the oldw == w so we won't be resizing on rotation
        if (oldh > 0 && oldh != h && oldw == w && !activity.isCurrentlyAnnotating) {
            val newState = if (h < oldh) SlidingUpPanelLayout.PanelState.EXPANDED else SlidingUpPanelLayout.PanelState.ANCHORED
            slidingUpPanelLayout?.panelState = newState

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
                    mBottomViewPager.layoutParams?.height = adjustedHeight
                    mBottomViewPager.requestLayout()
                } else if (slideOffset <= 0.50F) {
                    contentWrapper?.layoutParams?.height = Math.abs(maxHeight - adjustedHeight)
                    contentWrapper?.requestLayout()
                }
            }
        }
    }

    @SuppressLint("CommitTransaction")
    fun performCleanup() {
        mIsCleanedUp = true
        getCurrentFragment()?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
    }
    //endregion

    //region private helpers
    private fun setSubmission(submission: Submission?) {
        if (submission != null) submissionVersionsButton.text = submission.submittedAt.getSubmissionFormattedDate(context)
        val content = when {
            SubmissionType.NONE.apiString in mAssignment.submissionTypesRaw -> NoneContent
            SubmissionType.ON_PAPER.apiString in mAssignment.submissionTypesRaw -> OnPaperContent
            submission?.submissionType == null -> NoSubmissionContent
            mAssignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_MISSING ||
                    mAssignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING -> NoSubmissionContent
            else -> when (Assignment.getSubmissionTypeFromAPIString(submission.submissionType!!)) {

            // LTI submission
                SubmissionType.BASIC_LTI_LAUNCH -> ExternalToolContent(
                        mCourse,
                        submission.previewUrl.validOrNull() ?: mAssignment.url.validOrNull()
                        ?: mAssignment.htmlUrl ?: ""
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
                SubmissionType.ONLINE_QUIZ -> QuizContent(
                        mCourse.id,
                        mAssignment.id,
                        submission.userId,
                        submission.previewUrl ?: "",
                        QuizSubmission.parseWorkflowState(submission.workflowState!!) == QuizSubmission.WorkflowState.PENDING_REVIEW
                )

            // Discussion Submission
                SubmissionType.DISCUSSION_TOPIC -> DiscussionContent(submission.previewUrl)

            // Unsupported type
                else -> UnsupportedContent
            }
        }
        setGradeableContent(content)
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

    private fun setupSubmissionVersions(unsortedSubmissions: List<Submission>?) {
        if (unsortedSubmissions == null) return
        when (unsortedSubmissions.size) {
            0 -> submissionVersionsButton.setGone()
            1 -> submissionVersionsButton.setVisible().background = ColorDrawable(Color.TRANSPARENT)
            else -> unsortedSubmissions.sortedByDescending { it.submittedAt }.let { submissions ->
                val submissionDates = submissions.map { it.submittedAt.getSubmissionFormattedDate(context) }
                submissionVersionsButton.onClickWithRequireNetwork {
                    val dialog = RadioButtonDialog.getInstance(supportFragmentManager, resources.getString(R.string.submission_versions), submissionDates as ArrayList,
                            submissionDates.indexOf(submissionVersionsButton.text.toString())) { selectedIdx ->
                        EventBus.getDefault().post(SubmissionSelectedEvent(submissions[selectedIdx]))
                    }
                    dialog.show(supportFragmentManager, RadioButtonDialog::class.java.simpleName)
                }
                submissionVersionsButton.setVisible()
            }
        }
    }

    private fun setupToolbar(assignee: Assignee) {
        speedGraderToolbar.setupBackButton {
            // Use back button for WebView if applicable
            (getCurrentFragment() as? SpeedGraderWebNavigator)?.let {
                if (it.canGoBack()) {
                    it.goBack()
                    return@setupBackButton
                }
            }

            // Notify of unsaved changes
            if (hasUnsavedChanges) {
                UnsavedChangesExitDialog.show(supportFragmentManager) { (context as? Activity)?.finish() }
            } else {
                (context as? Activity)?.finish()
            }
        }

        val assigneeName = if (mAssignment.anonymousGrading) {
            resources.getString(R.string.anonymousStudentLabel)
        } else {
            Pronouns.span(assignee.name, assignee.pronouns)
        }
        titleTextView.text = assigneeName

        if (mStudentSubmission.isCached) {
            // get string/color resources for assignment status
            val (stringRes, colorRes) = mAssignment.getResForSubmission(mRootSubmission)
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
        ViewStyler.colorToolbarIconsAndText(context as Activity, speedGraderToolbar, Color.BLACK)
        ViewStyler.setStatusBarLight(context as Activity)
        ViewStyler.setToolbarElevationSmall(context, speedGraderToolbar)

        when {
            mAssignment.anonymousGrading -> userImageView.setAnonymousAvatar()
            assignee is GroupAssignee -> userImageView.setImageResource(assignee.iconRes)
            assignee is StudentAssignee -> {
                ProfileUtils.loadAvatarForUser(userImageView, assignee.student.name, assignee.student.avatarUrl)
                userImageView.setupAvatarA11y(assignee.name)
                userImageView.onClick {
                    val bundle = StudentContextFragment.makeBundle(assignee.id, mCourse.id)
                    RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
                }
            }
        }

        if (assignee is GroupAssignee && !mAssignment.anonymousGrading) setupGroupMemberList(assignee)
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_share -> {
                (getCurrentFragment() as? ShareableFile)?.viewExternally()

                //pdfs are a different type of fragment
                if (pdfFragment != null) {
                    pdfFragment?.document?.documentSource?.fileUri?.viewExternally(context, "application/pdf")
                }
            }
            R.id.menuPostPolicies -> {
                RouteMatcher.route(context, PostPolicyFragment.makeRoute(mCourse, mAssignment))
            }
        }
    }


    private fun setupGroupMemberList(assignee: GroupAssignee) {
        assigneeWrapperView.onClick {
            val popup = ListPopupWindow(context)
            popup.anchorView = it
            popup.setAdapter(object : ArrayAdapter<User>(context, 0, assignee.students) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val user = getItem(position)
                    val view = convertView
                            ?: LayoutInflater.from(context).inflate(R.layout.adapter_speed_grader_group_member, parent, false)
                    ProfileUtils.loadAvatarForUser(view.memberAvatarView, user?.name, user?.avatarUrl)
                    view.memberNameView.text = Pronouns.span(user?.name, user?.pronouns)
                    return view
                }
            })
            popup.setContentWidth(resources.getDimensionPixelSize(R.dimen.speedgraderGroupMemberListWidth))
            popup.verticalOffset = -assigneeWrapperView.height
            popup.isModal = true // For a11y
            popup.setOnItemClickListener { _, _, position, _ ->
                val bundle = StudentContextFragment.makeBundle(assignee.students[position].id, mCourse.id)
                RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
                popup.dismiss()
            }
            popup.show()
        }
    }

    private fun setGradeableContent(content: GradeableContent) {
        // Handle the existing PdfFragment if there is one
        val currentFragment = getCurrentFragment()
        if (currentFragment is PdfFragment) {
            // Unregister listeners for the existing fragment
            unregisterPdfFragmentListeners()
        }

        when (content) {
            is PdfContent -> {
                if(content.url.contains("canvadoc")) {
                    if(slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        // Attempt to reset the sliding panel to collapsed, so we don't render the pdf at anchored size
                        slidingUpPanelLayout?.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    }
                    handlePdfContent(content.url)
                } else {
                    showMessageFragment(R.string.pdfError)
                }
            }
            is NoSubmissionContent -> when (mAssignee) {
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
        return supportFragmentManager.findFragmentById(mContainerId)
    }

    override fun attachDocListener() {
        if (!(mAssignment.anonymousPeerReviews && mAssignment.isPeerReviews)) {
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

    private fun setupSlidingPanel() {

        slidingUpPanelLayout?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {

            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                adjustPanelHeights(slideOffset)
            }

            override fun onPanelStateChanged(panel: View?,
                                             previousState: SlidingUpPanelLayout.PanelState?,
                                             newState: SlidingUpPanelLayout.PanelState?) {
                if (newState != previousState) {
                    @Suppress("NON_EXHAUSTIVE_WHEN") //we don't want to update for all states, just these three
                    when (newState) {
                        SlidingUpPanelLayout.PanelState.ANCHORED -> {
                            postPanelEvent(newState, 0.5f)
                        }
                        SlidingUpPanelLayout.PanelState.EXPANDED ->
                            postPanelEvent(newState, 1.0f)
                        SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                            //fix for rotating when the panel is collapsed
                            pdfFragment?.notifyLayoutChanged()
                            postPanelEvent(newState, 0.0f)
                        }
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
        val maxHeight = contentRoot.height
        if (offset < 0 || maxHeight == 0) return

        val adjustedHeight = Math.abs(maxHeight * offset)

        if (offset >= 0.50F) { //Prevents resizing views when not necessary
            mBottomViewPager.layoutParams?.height = adjustedHeight.toInt()
            mBottomViewPager.requestLayout()
        }
        if (offset <= 0.50F) {
            contentWrapper?.layoutParams?.height = Math.abs(maxHeight - adjustedHeight).toInt()
            contentWrapper?.requestLayout()
        }
    }

    private fun setupBottomSheetViewPager(course: Course) {
        mBottomViewPager.offscreenPageLimit = 2
        mBottomViewPager.adapter = BottomSheetPagerAdapter.Holder(supportFragmentManager)
                .add(mGradeFragment)
                .add(SpeedGraderCommentsFragment.newInstance(
                        mRootSubmission,
                        mAssignee,
                        mCourse.id,
                        mAssignment.id,
                        mAssignment.groupCategoryId > 0 && mAssignee is GroupAssignee,
                        mAssignment.anonymousGrading
                ))
                .add(SpeedGraderFilesFragment.newInstance(mRootSubmission))
                .set()

        mBottomViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {

                }
            }
        })

        bottomTabLayout.setupWithViewPager(mBottomViewPager)
        bottomTabLayout.setSelectedTabIndicatorColor(course.color)
        bottomTabLayout.setTabTextColors(Color.BLACK, course.color)
        bottomTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanelLayout?.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                EventBus.getDefault().post(TabSelectedEvent(tab?.position ?: 0))
                if (slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanelLayout?.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
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

        mBottomViewPager.currentItem = initialTabIndex
    }

    private fun showVideoCommentDialog() {
        activity.disableViewPager()
        floatingRecordingView.setContentType(RecordingMediaType.Video)
        floatingRecordingView.startVideoView()
        floatingRecordingView.recordingCallback = {
            it?.let {
                EventBus.getDefault().post(UploadMediaCommentEvent(it, mAssignment.id, mAssignment.courseId, mAssignee.id))
            }
        }
        floatingRecordingView.stoppedCallback = {
            activity.enableViewPager()
            EventBus.getDefault().post(MediaCommentDialogClosedEvent())
        }
        floatingRecordingView.replayCallback = {
            val bundle = BaseViewMediaActivity.makeBundle(it, "video", context.getString(R.string.videoCommentReplay), true)
            RouteMatcher.route(context, Route(bundle, RouteContext.MEDIA))
        }
    }

    private fun showAudioCommentDialog() {
        activity.disableViewPager()
        floatingRecordingView.setContentType(RecordingMediaType.Audio)
        floatingRecordingView.setVisible()
        floatingRecordingView.stoppedCallback = {
            activity.enableViewPager()
            EventBus.getDefault().post(MediaCommentDialogClosedEvent())
        }
        floatingRecordingView.recordingCallback = {
            it?.let {
                EventBus.getDefault().post(UploadMediaCommentEvent(it, mAssignment.id, mAssignment.courseId, mAssignee.id))
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

    private class BottomSheetPagerAdapter internal constructor(fm: FragmentManager, fragments: ArrayList<Fragment>) : FragmentPagerAdapter(fm) {

        private var fragments = ArrayList<Fragment>()

        init {
            this.fragments = fragments
        }

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int) = when (position) {
            0 -> ContextKeeper.appContext.getString(R.string.sg_tab_grade).toUpperCase(Locale.getDefault())
            1 -> ContextKeeper.appContext.getString(R.string.sg_tab_comments).toUpperCase(Locale.getDefault())
            2 -> ContextKeeper.appContext.getString(R.string.sg_tab_files).toUpperCase(Locale.getDefault())
            else -> ""
        }

        internal class Holder(private val manager: FragmentManager) {

            private val fragments = ArrayList<Fragment>()

            fun add(f: Fragment): Holder {
                fragments.add(f)
                return this
            }

            fun set() = BottomSheetPagerAdapter(manager, fragments)
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
        if (event.submission?.id == mRootSubmission?.id) setSubmission(event.submission)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchAttachment(event: SubmissionFileSelectedEvent) {
        if (event.submissionId == mRootSubmission?.id) {
            //close the annotations toolbar so it can be associated with new document
            pdfFragment?.exitCurrentlyActiveMode()
            setAttachmentContent(event.attachment)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAnchorChanged(event: SlidingPanelAnchorEvent) {
        slidingUpPanelLayout?.panelState = event.anchorPosition
        //If we try to adjust the panels before contentRoot's height is determined, things don't work
        //This post works because we setup the panel before registering to the event
        contentRoot.post { adjustPanelHeights(event.offset) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentTextFocused(event: CommentTextFocusedEvent) {
        if (event.assigneeId == mAssignee.id) {
            pdfFragment?.exitCurrentlyActiveMode()
            activity.isCurrentlyAnnotating = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentAdded(event: AnnotationCommentAdded) {
        if (event.assigneeId == mAssignee.id) {
            //add the comment to the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.add(event.annotation)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentEdited(event: AnnotationCommentEdited) {
        if (event.assigneeId == mAssignee.id) {
            //update the annotation in the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.find { it.annotationId == event.annotation.annotationId }?.contents = event.annotation.contents
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleted(event: AnnotationCommentDeleted) {
        if (event.assigneeId == mAssignee.id) {
            if (event.isHeadAnnotation) {
                //we need to delete the entire list of comments from the hashmap
                commentRepliesHashMap.remove(event.annotation.inReplyTo)
            } else {
                //otherwise just remove the comment
                commentRepliesHashMap[event.annotation.inReplyTo]?.remove(event.annotation)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleteAcknowledged(event: AnnotationCommentDeleteAcknowledged) {
        if (event.assigneeId == mAssignee.id) {
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
        mBottomViewPager.currentItem = event.selectedTabIdx
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
        return if(mAssignee is GroupAssignee) {
            (mAssignee as GroupAssignee).group.users.any { it.id == eventAssigneeId }
        } else {
            return eventAssigneeId == mAssignee.id
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
class UploadMediaCommentEvent(val file: File, val assignmentId: Long, val courseId: Long, val assigneeId: Long)


sealed class GradeableContent
object NoSubmissionContent : GradeableContent()
object NoneContent : GradeableContent()
class ExternalToolContent(val canvasContext: CanvasContext, val url: String) : GradeableContent()
object OnPaperContent : GradeableContent()
object UnsupportedContent : GradeableContent()
class OtherAttachmentContent(val attachment: Attachment) : GradeableContent()
class PdfContent(val url: String) : GradeableContent()
class TextContent(val text: String) : GradeableContent()
class ImageContent(val url: String, val contentType: String) : GradeableContent()
class UrlContent(val url: String, val previewUrl: String?) : GradeableContent()
class DiscussionContent(val previewUrl: String?) : GradeableContent()
class MediaCommentDialogClosedEvent
class AudioPermissionGrantedEvent(val assigneeId: Long)
class VideoPermissionGrantedEvent(val assigneeId: Long)


class QuizContent(
        val courseId: Long,
        val assignmentId: Long,
        val studentId: Long,
        val url: String,
        val pendingReview: Boolean) : GradeableContent()

class MediaContent(
        val uri: Uri,
        val contentType: String? = null,
        val thumbnailUrl: String? = null,
        val displayName: String? = null
) : GradeableContent()
