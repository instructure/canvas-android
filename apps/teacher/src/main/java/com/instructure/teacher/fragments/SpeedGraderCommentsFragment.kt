/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_COMMENTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.fragments.BaseListFragment
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.services.NotoriousUploadWorker
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.SpeedGraderCommentsAdapter
import com.instructure.teacher.databinding.FragmentSpeedgraderCommentsBinding
import com.instructure.teacher.decorations.SpacesItemDecoration
import com.instructure.teacher.dialog.SGAddMediaCommentDialog
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.UploadMediaCommentUpdateEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.SpeedGraderCommentsPresenterFactory
import com.instructure.teacher.features.speedgrader.commentlibrary.CommentLibraryViewModel
import com.instructure.teacher.holders.SpeedGraderCommentHolder
import com.instructure.teacher.models.SubmissionCommentWrapper
import com.instructure.teacher.presenters.SpeedGraderCommentsPresenter
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.view
import com.instructure.teacher.view.CommentTextFocusedEvent
import com.instructure.teacher.view.MediaCommentDialogClosedEvent
import com.instructure.teacher.view.SubmissionSelectedEvent
import com.instructure.teacher.view.UploadMediaCommentEvent
import com.instructure.teacher.viewinterface.SpeedGraderCommentsView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.UUID
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_SPEED_GRADER_COMMENTS)
@AndroidEntryPoint
class SpeedGraderCommentsFragment : BaseListFragment<SubmissionCommentWrapper, SpeedGraderCommentsPresenter, SpeedGraderCommentsView, SpeedGraderCommentHolder, SpeedGraderCommentsAdapter>(), SpeedGraderCommentsView, FileUploadDialogParent {

    private val binding by viewBinding(FragmentSpeedgraderCommentsBinding::bind)

    @Inject
    lateinit var fileUploadInputDao: FileUploadInputDao

    @Inject
    lateinit var submissionCommentDao: SubmissionCommentDao

    @Inject
    lateinit var attachmentDao: AttachmentDao

    @Inject
    lateinit var authorDao: AuthorDao

    @Inject
    lateinit var mediaCommentDao: MediaCommentDao

    @Inject
    lateinit var pendingSubmissionCommentDao: PendingSubmissionCommentDao

    var mRawComments by ParcelableArrayListArg<SubmissionComment>()
    var mSubmissionId by LongArg()
    var mSubmission by ParcelableArg<Submission>()
    var mSubmissionHistory by ParcelableArrayListArg<Submission>()
    var mAssignee by ParcelableArg<Assignee>(StudentAssignee(User()))
    var mCourseId by LongArg()
    var mAssignmentId by LongArg()
    var mIsGroupMessage by BooleanArg()
    var mGradeAnonymously by BooleanArg()
    var assignmentEnhancementsEnabled by BooleanArg()

    var changeCommentFieldExternallyFlag = false

    override fun createAdapter(): SpeedGraderCommentsAdapter {
        return SpeedGraderCommentsAdapter(requireContext(), presenter, mCourseId, presenter.assignee, mGradeAnonymously, onAttachmentClicked)
    }

    private val mLayoutManager by lazy { LinearLayoutManager(requireContext())
        .apply { this.stackFromEnd = true }}

    override fun layoutResId() = R.layout.fragment_speedgrader_comments
    override val recyclerView: RecyclerView get() = binding.speedGraderCommentsRecyclerView
    override fun getPresenterFactory() = SpeedGraderCommentsPresenterFactory(
        mRawComments,
        mSubmissionHistory,
        mAssignee,
        mCourseId,
        mAssignmentId,
        mIsGroupMessage,
        submissionCommentDao,
        attachmentDao,
        authorDao,
        mediaCommentDao,
        pendingSubmissionCommentDao,
        fileUploadInputDao,
        mSubmission.attempt,
        assignmentEnhancementsEnabled
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommentInput()
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(speedGraderCommentsRecyclerView) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(commentInputContainer.commentInputRoot) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }
        if (speedGraderCommentsRecyclerView.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(speedGraderCommentsRecyclerView)
        }
        if (commentInputContainer.commentInputRoot.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(commentInputContainer.commentInputRoot)
        }
    }

    override fun onRefreshStarted() {}
    override fun onRefreshFinished() {}

    private val onAttachmentClicked = { attachment: Attachment -> attachment.view(requireActivity()) }

    private val commentLibraryViewModel: CommentLibraryViewModel by activityViewModels()

    override fun onPresenterPrepared(presenter: SpeedGraderCommentsPresenter) = with(binding) {
        RecyclerViewUtils.buildRecyclerView(requireContext(), adapter, presenter, swipeRefreshLayout, speedGraderCommentsRecyclerView, speedGraderCommentsEmptyView, getString(R.string.no_submission_comments))
        speedGraderCommentsRecyclerView.addItemDecoration(SpacesItemDecoration(requireContext(), R.dimen.speedgrader_comment_margins))
        speedGraderCommentsRecyclerView.layoutManager = mLayoutManager
        swipeRefreshLayout.isEnabled = false
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onReadySetGo(presenter: SpeedGraderCommentsPresenter) {
        presenter.loadData(false)

        // Check for any media comment updates
        val event = EventBus.getDefault().getStickyEvent(UploadMediaCommentUpdateEvent::class.java)
        event?.get {
            checkMediaCommentsAndUpdate(it)
        }
    }

    private fun setupCommentInput() = with(binding.commentInputContainer) {
        commentLibraryViewModel.getCommentBySubmission(mSubmissionId).observe(viewLifecycleOwner) {
            if (commentEditText.text.toString() != it.comment) {
                commentEditText.setText(it.comment)
                if (it.selectedFromSuggestion) {
                    commentEditText.setSelection(it.comment.length)
                }
            }
        }

        sendCommentButton.imageTintList = ViewStyler.generateColorStateList(
                intArrayOf(-android.R.attr.state_enabled) to requireContext().getColorCompat(R.color.textDark),
                intArrayOf() to ThemePrefs.textButtonColor
        )
        sendCommentButton.isEnabled = false
        sendCommentButton.setGone()
        commentEditText.onTextChanged {
            sendCommentButton.isEnabled = it.isNotBlank()
            sendCommentButton.setVisible(it.isNotBlank())
            commentLibraryViewModel.setCommentBySubmission(mSubmissionId, it)
            if (!changeCommentFieldExternallyFlag) {
                (requireActivity() as SpeedGraderActivity).openCommentLibrary(mSubmissionId)
            }
        }
        sendCommentButton.onClickWithRequireNetwork {
            (requireActivity() as SpeedGraderActivity).closeCommentLibrary()
            presenter.sendComment(commentEditText.text.toString())
            val errorLayout = binding.root.findViewById<LinearLayout>(R.id.errorLayout)
            errorLayout?.announceForAccessibility(getString(R.string.sendingSimple))
        }

        commentEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                (requireActivity() as SpeedGraderActivity).openCommentLibrary(mSubmissionId)
                EventBus.getDefault().post(CommentTextFocusedEvent(mAssignee.id))
            }
        }

        addAttachment.onClick {
            (requireActivity() as SpeedGraderActivity).closeCommentLibrary()
            SGAddMediaCommentDialog.show(requireActivity().supportFragmentManager,
                    presenter.assignmentId, presenter.courseId,
                    when (presenter.assignee) {
                        is StudentAssignee -> (presenter.assignee as StudentAssignee).student.id
                        is GroupAssignee -> (presenter.assignee as GroupAssignee).students.firstOrNull()?.id ?: presenter.assignee.id
                    }, when (presenter.assignee) {
                is GroupAssignee -> true
                else -> false
            }, ::showFileUploadDialog)

            addAttachment.isEnabled = false
        }
    }

    override fun scrollToBottom() {
        mLayoutManager.scrollToPosition(presenter.data.size() - 1)
    }

    override fun setDraftText(comment: String?) {
        // We use this flag to avoid opening the comment library, when text is set externally.
        // Cases like sending a comment and clearing the comment field or populating the comment field with cached data
        changeCommentFieldExternallyFlag = true
        binding.commentInputContainer.commentEditText.setText(comment.orEmpty())
        changeCommentFieldExternallyFlag = false
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        presenter.saveDraft(binding.commentInputContainer.commentEditText.text.toString())
        super.onStop()
    }

    override fun checkIfEmpty() = with(binding) {
         RecyclerViewUtils.checkIfEmpty(speedGraderCommentsEmptyView, speedGraderCommentsRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    @Suppress("unused")
    @Subscribe
    fun onUploadMediaComment(event: UploadMediaCommentEvent) {
        lifecycleScope.launch {
            if (mAssignee.id == event.assigneeId) {
                val id = presenter.createPendingMediaComment(event.file.absolutePath)
                uploadSGMediaComment(event.file, event.assignmentId, event.courseId, id, event.attemptId)
                binding.commentInputContainer.addAttachment.isEnabled = true
            }
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onCommentsUpdated(event: SubmissionCommentsUpdated) {
        view?.announceForAccessibility(getString(R.string.a11y_commentSentSuccess))
    }

    private fun showFileUploadDialog() {
        val bundle = FileUploadDialogFragment.createTeacherSubmissionCommentBundle(
            presenter.courseId,
            presenter.assignmentId,
            presenter.assignee.id,
            presenter.selectedAttemptId
        )

        FileUploadDialogFragment.newInstance(bundle).show(
            childFragmentManager, FileUploadDialogFragment.TAG
        )
    }

    override fun selectedUriStringsCallback(filePaths: List<String>) {
        presenter.selectedFilePaths = filePaths
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo?>) {
        workInfoLiveData.observe(this) {
            if (it == null) return@observe
            presenter.onFileUploadWorkInfoChanged(it)
        }
    }

    override fun subscribePendingWorkers(workerIds: List<UUID>) {
        workerIds.forEach {
            workInfoLiveDataCallback(null, WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(it))
        }
    }

    override fun restartWorker(fileUploadWorkerData: FileUploadWorkerData) {
        lifecycleScope.launch {
            val worker = OneTimeWorkRequestBuilder<FileUploadWorker>()
                .build()

            val inputData = FileUploadInputEntity(
                workerId = worker.id.toString(),
                filePaths = fileUploadWorkerData.filePaths,
                courseId = fileUploadWorkerData.courseId,
                assignmentId = fileUploadWorkerData.assignmentId,
                userId = fileUploadWorkerData.userId,
                action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT
            )

            fileUploadInputDao.insert(inputData)

            WorkManager.getInstance(requireContext()).apply {
                workInfoLiveDataCallback(null, getWorkInfoByIdLiveData(worker.id))
                enqueue(worker)
            }
        }
    }

    override fun onDestroy() {
        presenter.removeFailedFileUploads()
        super.onDestroy()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @Subscribe
    fun onMediaCommentDialogClosed(event: MediaCommentDialogClosedEvent) {
        binding.commentInputContainer.addAttachment.isEnabled = true
    }

    @Suppress("unused")
    @Subscribe
    fun onUploadMediaCommentSuccess(event: UploadMediaCommentUpdateEvent) {
        event.get {
            checkMediaCommentsAndUpdate(it)
        }
    }

    private fun checkMediaCommentsAndUpdate(comments: MutableMap<String, MutableList<Pair<PendingSubmissionComment, SubmissionComment?>>>) {
        if (comments.containsKey(presenter.mPageId)) {
            presenter.updatePendingComments(comments[presenter.mPageId]!!)

            SubmissionCommentsUpdated().post()

            // Update the event
            EventBus.getDefault().getStickyEvent(UploadMediaCommentUpdateEvent::class.java)?.get {
                comments.remove(presenter.mPageId)
                UploadMediaCommentUpdateEvent(comments).post()
            }
        }
    }

    /**
     * Takes an Audio/Video file and uploads it using the NotoriousUploadService
     *
     * @param mediaFile File pointing to the media to upload
     */
    private fun uploadSGMediaComment(mediaFile: File, assignmentId: Long, courseID: Long, dbId: Long, attemptId: Long?) {
        NotoriousUploadWorker.enqueueUpload(
            context = requireActivity(),
            mediaFilePath = Uri.fromFile(mediaFile).path,
            assignment = Assignment(id = assignmentId, courseId = courseID),
            studentId = mAssignee.id,
            isGroupComment = mAssignee is GroupAssignee,
            pageId = presenter.mPageId,
            attemptId = attemptId.takeIf { assignmentEnhancementsEnabled },
            mediaCommentId = dbId
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchSubmission(event: SubmissionSelectedEvent) {
        presenter.selectedAttemptId = event.submission?.attempt
        presenter.refresh(false)
    }

    companion object {
        fun newInstance(
                submission: Submission?,
                assignee: Assignee,
                courseId: Long,
                assignmentId: Long,
                isGroupMessage: Boolean,
                gradeAnonymously: Boolean,
                assignmentEnhancementsEnabled: Boolean
        ) = SpeedGraderCommentsFragment().apply {
            mSubmission = submission ?: Submission()
            mRawComments = ArrayList(submission?.submissionComments ?: emptyList())
            mSubmissionId = submission?.id ?: -1
            mSubmissionHistory = ArrayList(submission?.submissionHistory?.filterNotNull()?.filter { it.submissionType != null && it.workflowState != "unsubmitted" } ?: emptyList())
            mAssignee = assignee
            mCourseId = courseId
            mAssignmentId = assignmentId
            mIsGroupMessage = isGroupMessage
            mGradeAnonymously = gradeAnonymously
            this.assignmentEnhancementsEnabled = assignmentEnhancementsEnabled
        }
    }
}
