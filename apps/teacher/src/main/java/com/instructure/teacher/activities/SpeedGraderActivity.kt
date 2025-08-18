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
package com.instructure.teacher.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.coerceAtLeast
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.rangeWithin
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.activities.BasePresenterActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.UnsavedChangesContinueDialog
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ExoAgent
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.RequestCodes.CAMERA_PIC_REQUEST
import com.instructure.pandautils.utils.RequestCodes.PICK_FILE_FROM_DEVICE
import com.instructure.pandautils.utils.RequestCodes.PICK_IMAGE_GALLERY
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.hideKeyboard
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.postSticky
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.adapters.SubmissionContentAdapter
import com.instructure.teacher.databinding.ActivitySpeedgraderBinding
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.factory.SpeedGraderPresenterFactory
import com.instructure.pandautils.features.speedgrader.AssignmentSubmissionRepository
import com.instructure.pandautils.features.speedgrader.SubmissionListFilter
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.features.speedgrader.commentlibrary.CommentLibraryAction
import com.instructure.teacher.features.speedgrader.commentlibrary.CommentLibraryFragment
import com.instructure.teacher.features.speedgrader.commentlibrary.CommentLibraryViewModel
import com.instructure.teacher.presenters.SpeedGraderPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.isTalkbackEnabled
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.toast
import com.instructure.teacher.view.AudioPermissionGrantedEvent
import com.instructure.teacher.view.TabSelectedEvent
import com.instructure.teacher.view.VideoPermissionGrantedEvent
import com.instructure.teacher.viewinterface.SpeedGraderView
import com.pspdfkit.preferences.PSPDFKitPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale
import javax.inject.Inject

@PageView("courses/{courseId}/gradebook/speed_grader?assignment_id={assignmentId}")
@ScreenView(SCREEN_VIEW_SPEED_GRADER)
@AndroidEntryPoint
class SpeedGraderActivity : BasePresenterActivity<SpeedGraderPresenter, SpeedGraderView>(), SpeedGraderView {

    @Inject
    lateinit var repository: AssignmentSubmissionRepository

    private val binding by viewBinding(ActivitySpeedgraderBinding::inflate)

    /* These should be passed to the presenter factory and should not be directly referenced otherwise */
    @get:PageViewUrlParam("courseId")
    val courseId: Long by lazy { intent.extras!!.getLong(Const.COURSE_ID) }
    @get:PageViewUrlParam("assignmentId")
    val assignmentId: Long by lazy { intent.extras!!.getLong(Const.ASSIGNMENT_ID) }
    private val submissionId: Long by lazy { intent.extras!!.getLong(RouterParams.SUBMISSION_ID) }
    private val discussionTopicHeader: DiscussionTopicHeader? by lazy { intent.extras!!.getParcelable(Const.DISCUSSION_HEADER) }
    private val discussionEntryId: Long? by lazy { intent.extras?.getLong(DISCUSSION_ENTRY_ID, -1) }
    private val anonymousGrading: Boolean? by lazy { intent.extras?.getBoolean(Const.ANONYMOUS_GRADING) }
    private val filteredSubmissionIds: LongArray by lazy { intent.extras?.getLongArray(FILTERED_SUBMISSION_IDS) ?: longArrayOf() }
    private val filter: SubmissionListFilter by lazy {
        intent.extras!!.getSerializable(
            FILTER
        ) as? SubmissionListFilter
            ?: SubmissionListFilter.ALL
    }
    private val filterValue: Double by lazy { intent.extras!!.getDouble(FILTER_VALUE) }

    private val initialSelection: Int by lazy { intent.extras!!.getInt(Const.SELECTED_ITEM, 0) }
    private var currentSelection = 0
    private var previousSelection = 0

    // Used for keeping track of the page that is asking for media permissions from SubmissionContentView
    private var assigneeId: Long = -1L

    private val viewModel: CommentLibraryViewModel by viewModels()

    // Used in the SubmissionViewFragments in the ViewPager to handle issues with sliding panel
    var isCurrentlyAnnotating = false

    private lateinit var adapter: SubmissionContentAdapter

    var hasCommentLibrarySuggestions = false

    override fun unBundle(extras: Bundle) = Unit

    override fun onPresenterPrepared(presenter: SpeedGraderPresenter) = Unit

    override fun onReadySetGo(presenter: SpeedGraderPresenter) {
        presenter.setupData()
    }

    // This is here to prevent transaction too large exceptions, possibly caused by pdfFragment's saving.
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {}

    override fun getPresenterFactory() = SpeedGraderPresenterFactory(
        courseId,
        assignmentId,
        submissionId,
        discussionTopicHeader,
        repository,
        filteredSubmissionIds,
        filter,
        filterValue
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the PDF author, but only if it hasn't been set yet
        if (!PSPDFKitPreferences.get(this).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(this).setAnnotationCreator(ApiPrefs.user?.name)
        }

        setContentView(binding.root)

        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.data.observe(this) { data ->
            hasCommentLibrarySuggestions = !data.isEmpty()
        }
    }

    private fun handleAction(action: CommentLibraryAction) {
        if (action is CommentLibraryAction.CommentLibraryClosed) {
            closeCommentLibrary()
        }
    }

    override fun onDataSet(assignment: Assignment, submissions: List<GradeableStudentSubmission>): Unit = with(binding) {
        val assignmentWithAnonymousGrading = if (anonymousGrading != null) {
            assignment.copy(anonymousGrading = anonymousGrading!!)
        } else {
            assignment
        }
        val selection = if (discussionEntryId != null && discussionEntryId != -1L) {
            submissions.indexOfFirst { it.submission?.discussionEntries?.map{ it.id }?.contains(discussionEntryId).orDefault() }
        } else { initialSelection }
        adapter = SubmissionContentAdapter(assignmentWithAnonymousGrading, presenter!!.course, submissions)
        submissionContentPager.offscreenPageLimit = 1
        submissionContentPager.pageMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics))
        submissionContentPager.setPageMarginDrawable(R.color.backgroundMedium)
        submissionContentPager.adapter = adapter
        submissionContentPager.setCurrentItem(selection, false)
        submissionContentPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                adapter.updateAnnotations(position)
                previousSelection = currentSelection
                currentSelection = position
                adapter.onPageSelected(previousSelection, currentSelection)
                if (adapter.hasUnsavedChanges(previousSelection)) {
                    UnsavedChangesContinueDialog.show(supportFragmentManager) {
                        submissionContentPager.setCurrentItem(previousSelection, true)
                    }
                }
            }
        })
        setupTutorialView()

        setupToolbar(presenter!!.course, assignment)
    }

    private fun setupToolbar(course: Course, assignment: Assignment) = with(binding) {
        gradingToolbar.setupBackButton(this@SpeedGraderActivity)

        gradingToolbar.setupMenu(R.menu.menu_post_policies) {
            when (it.itemId) {
                R.id.menuPostPolicies -> {
                    RouteMatcher.route(
                        this@SpeedGraderActivity,
                        PostPolicyFragment.makeRoute(presenter!!.course.id, assignment)
                    )
                }
            }
        }

        gradingToolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        gradingToolbar.title = assignment.name
        gradingToolbar.subtitle = course.name

        ViewStyler.themeToolbarColored(this@SpeedGraderActivity, gradingToolbar, course.color, getColor(R.color.textLightest))
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupTutorialView() = weave {
        if (BuildConfig.IS_TESTING || TeacherPrefs.hasViewedSwipeTutorial || adapter.count < 2 || isTalkbackEnabled()) return@weave

        delay(TUTORIAL_DELAY)
        binding.swipeTutorialView.setVisible().onClick {
            if (it.alpha != 1f) return@onClick
            ObjectAnimator.ofFloat(it, "alpha", 1f, 0f).apply {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) = Unit
                    override fun onAnimationCancel(animation: Animator) = Unit
                    override fun onAnimationStart(animation: Animator) = Unit
                    override fun onAnimationEnd(animation: Animator) {
                        it.setGone()
                        TeacherPrefs.hasViewedSwipeTutorial = true
                    }
                })
                duration = TUTORIAL_DELAY
            }.start()
        }
        ObjectAnimator.ofFloat(binding.swipeTutorialView, "alpha", 0f, 1f).apply {
            duration = TUTORIAL_DELAY
        }.start()
    }

    override fun onErrorSettingData() {
        toast(R.string.errorOccurred)
        finish()
    }

    fun enableViewPager() {
        binding.submissionContentPager.isPagingEnabled = true
    }

    fun disableViewPager() {
        binding.submissionContentPager.isPagingEnabled = false
    }

    @Suppress("unused")
    fun lockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    fun unlockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoAgent.releaseAllAgents() // Stop any media playback
    }

    @Suppress("unused")
    @Subscribe
    fun onTabSelected(event: TabSelectedEvent) {
        binding.submissionContentPager.hideKeyboard()
        adapter.initialTabIdx = event.selectedTabIdx
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            // Invalidate submission cache for post/hide grades (this covers submissions not currently visible in the pager)
            if(assignmentId == it) adapter.invalidateSubmissionCache()
        }
    }

    fun requestAudioPermissions(assigneeId: Long) {
        if (checkAudioPermission()) {
            // We have the permission
            EventBus.getDefault().post(AudioPermissionGrantedEvent(assigneeId))
        } else {
            this.assigneeId = assigneeId
            ActivityCompat.requestPermissions(this, arrayOf(PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
        }
    }

    fun requestVideoPermissions(assigneeId: Long) {
        if (checkVideoPermission()) {
            // We have the permissions
            EventBus.getDefault().post(VideoPermissionGrantedEvent(assigneeId))
        } else {
            this.assigneeId = assigneeId
            ActivityCompat.requestPermissions(this, arrayOf(PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkAudioPermission(): Boolean = ContextCompat.checkSelfPermission(this, PermissionUtils.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    private fun checkVideoPermission(): Boolean = ContextCompat.checkSelfPermission(this, PermissionUtils.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE && grantResults.size >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when {
                permissions.contains(PermissionUtils.CAMERA) && permissions.contains(PermissionUtils.RECORD_AUDIO) -> {
                    if(grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                        EventBus.getDefault().post(VideoPermissionGrantedEvent(assigneeId))
                    }
                }
                permissions.contains(PermissionUtils.RECORD_AUDIO) ->  EventBus.getDefault().post(AudioPermissionGrantedEvent(assigneeId))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_PIC_REQUEST ||
                requestCode == PICK_FILE_FROM_DEVICE ||
                requestCode == PICK_IMAGE_GALLERY) {
            //File Dialog Fragment will not be notified of onActivityResult(), alert manually
            OnActivityResults(ActivityResult(requestCode, resultCode, data), null).postSticky()
        }
    }

    fun openCommentLibrary(submissionId: Long) {
        viewModel.currentSubmissionId = submissionId
        if (!isCommentLibraryOpen() && hasCommentLibrarySuggestions) {
            binding.submissionContentPager.isCommentLibraryOpen = true

            val commentLibraryFragment = CommentLibraryFragment.newInstance(submissionId)
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.commentLibraryFragmentContainer, commentLibraryFragment, commentLibraryFragment::class.java.name)
            fragmentTransaction.addToBackStack(commentLibraryFragment::class.java.name)
            fragmentTransaction.commitAllowingStateLoss()

            binding.submissionContentPager.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
    }

    fun closeCommentLibrary() {
        if (isCommentLibraryOpen()) {
            binding.submissionContentPager.isCommentLibraryOpen = false
            supportFragmentManager.popBackStackImmediate()

            binding.submissionContentPager.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }

    override fun onBackPressed() {
        if (isCommentLibraryOpen()) {
            closeCommentLibrary()
        } else {
            super.onBackPressed()
        }
    }

    private fun isCommentLibraryOpen(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(CommentLibraryFragment::class.java.name)
        return fragment != null
    }

    companion object {

        private const val TUTORIAL_DELAY = 400L

        /**
         * The number of submissions to be bundled (pre-cached) to either side of the selected
         * submission. If this value is too high it may result in Android throwing a
         * TransactionTooLargeException when creating SpeedGraderActivity.
         */
        private const val MAX_CACHED_ADJACENT = 6

        /**
         * The maximum submission history depth allowed for a submission to be eligible for
         * pre-caching. If this value is too high it may result in Android throwing a
         * TransactionTooLargeException when creating SpeedGraderActivity.
         */
        private const val MAX_HISTORY_THRESHOLD = 8

        const val FILTER = "filter"
        const val FILTER_VALUE = "filter_value"
        const val FILTERED_SUBMISSION_IDS = "filtered_submission_ids"
        const val DISCUSSION_ENTRY_ID = "discussion_entry_id"

        fun makeBundle(
            courseId: Long,
            assignmentId: Long,
            selectedIdx: Int,
            anonymousGrading: Boolean? = null,
            filteredSubmissionIds: LongArray = longArrayOf(),
            filter: SubmissionListFilter? = null,
            filterValue: Double = 0.0
        ): Bundle {
            return Bundle().apply {
                putLong(Const.COURSE_ID, courseId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putInt(Const.SELECTED_ITEM, selectedIdx)
                putBoolean(Const.ANONYMOUS_GRADING, anonymousGrading ?: false)
                putSerializable(FILTER, filter)
                putDouble(FILTER_VALUE, filterValue)
                putLongArray(FILTERED_SUBMISSION_IDS, filteredSubmissionIds)
            }
        }

        fun makeBundle(courseId: Long, assignmentId: Long, submissions: List<GradeableStudentSubmission>, selectedIdx: Int, anonymousGrading: Boolean? = null): Bundle {
            return Bundle().apply {
                putLong(Const.COURSE_ID, courseId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)

                // Avoid TransactionTooLargeException by only bundling submissions in the cached range with shallow submission histories
                val cachedRange = selectedIdx.rangeWithin(MAX_CACHED_ADJACENT).coerceAtLeast(0)
                val compactSubmissions = submissions.mapIndexed { index, submission ->
                    val inRange = index in cachedRange
                    val smallHistory = submission.submission?.submissionHistory?.size ?: 0 <= MAX_HISTORY_THRESHOLD
                    val smallBodies = submission.submission?.submissionHistory?.none { it?.body?.length ?: 0 > 2048 } ?: true
                    if (inRange && smallHistory && smallBodies && submission.submission != null) {
                        submission.copy(isCached = true)
                    } else {
                        submission.copy(submission = null, isCached = false)
                    }
                }

                // Only sort when anon grading is off
                val anonymousGradingOn = anonymousGrading ?: (submissions.firstOrNull()?.submission?.assignment?.anonymousGrading == true)

                if(!anonymousGradingOn) {
                    // We need to sort the submissions so they appear in the same order as the submissions list
                    putParcelableArrayList(Const.SUBMISSION, ArrayList(compactSubmissions.sortedBy {
                        (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(Locale.getDefault())
                    }))
                } else {
                    putParcelableArrayList(Const.SUBMISSION, ArrayList(compactSubmissions))
                }

                putInt(Const.SELECTED_ITEM, selectedIdx)

                putBoolean(Const.ANONYMOUS_GRADING, anonymousGradingOn)
            }
        }

        fun createIntent(context: Context, route: Route): Intent {
            return Intent(context, SpeedGraderActivity::class.java).apply {
                if(!route.arguments.isEmpty) {
                    putExtras(route.arguments)
                } else {
                    // Try to get the information from the route that this activity needs. This happens
                    // when we come from a push notification
                    putExtras(Bundle().apply {
                        putLong(Const.COURSE_ID, route.paramsHash[RouterParams.COURSE_ID]?.toLong() ?: 0)
                        putLong(Const.ASSIGNMENT_ID, route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong() ?: 0)
                        putLong(RouterParams.SUBMISSION_ID, route.paramsHash[RouterParams.SUBMISSION_ID]?.toLong() ?: 0)
                    })
                }
            }
        }

        fun createIntent(context: Context, courseId: Long, assignmentId: Long, submissionId: Long) = Intent(context, SpeedGraderActivity::class.java).apply {
            // We've come from a push notification, we'll use these ids to grab the data we need later
            putExtras(Bundle().apply {
                putLong(Const.COURSE_ID, courseId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putLong(RouterParams.SUBMISSION_ID, submissionId)
            })
        }
    }
}
