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
 */    package com.instructure.student.AnnotationComments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.annotations.AnnotationDialogs.AnnotationCommentDialog
import com.instructure.annotations.createCommentReplyAnnotation
import com.instructure.annotations.generateAnnotationId
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_ANNOTATION_COMMENT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.PdfStudentSubmissionView
import kotlinx.android.synthetic.main.fragment_annotation_comment_list.*
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.util.Locale

@ScreenView(SCREEN_VIEW_ANNOTATION_COMMENT_LIST)
class AnnotationCommentListFragment : ParentFragment() {

    private var annotations by ParcelableArrayListArg<CanvaDocAnnotation>()
    private var assigneeId by LongArg()
    private var docSession by ParcelableArg<DocSession>()
    private var apiValues by ParcelableArg<ApiValues>()
    private var headAnnotationId by StringArg()

    private var recyclerAdapter: AnnotationCommentListRecyclerAdapter? = null

    private var sendCommentJob: Job? = null
    private var editCommentJob: Job? = null
    private var deleteCommentJob: Job? = null

    override fun title() = getString(R.string.comments)

    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsCloseButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_annotation_comment_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerAdapter = AnnotationCommentListRecyclerAdapter(requireContext(), docSession, { annotation, position ->
            AnnotationCommentDialog.getInstance(requireFragmentManager(), annotation.contents ?: "", requireContext().getString(R.string.editComment)) { cancelled, text ->
                if(!cancelled) {
                    annotation.contents = text
                    editComment(annotation, position)
                }
            }.show(requireFragmentManager(), AnnotationCommentDialog::class.java.simpleName)
        }, { annotation, position ->
            val builder = AlertDialog.Builder(requireContext())
            //we want to show a different title for the root comment
            builder.setTitle(R.string.deleteComment)
            builder.setMessage(if(position == 0) R.string.deleteHeadCommentConfirmation else R.string.deleteCommentConfirmation)
            builder.setPositiveButton(getString(R.string.delete).uppercase(Locale.getDefault())) { _, _ ->
                deleteComment(annotation, position)
            }
            builder.setNegativeButton(getString(R.string.cancel).uppercase(Locale.getDefault()), null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            }
            dialog.show()
        })

        configureRecyclerView()
        applyTheme()
        setupCommentInput()

        if(recyclerAdapter?.size() == 0) {
            recyclerAdapter?.addAll(annotations)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sendCommentJob?.cancel()
        editCommentJob?.cancel()
        deleteCommentJob?.cancel()
        EventBus.getDefault().post(
            PdfStudentSubmissionView.AnnotationCommentDeleteAcknowledged(
                annotations.filter { it.deleted && it.deleteAcknowledged.isNullOrEmpty() },
                assigneeId))
    }

    fun configureRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        annotationCommentsRecyclerView.layoutManager = layoutManager
        annotationCommentsRecyclerView.itemAnimator = DefaultItemAnimator()
        annotationCommentsRecyclerView.adapter = recyclerAdapter
    }

    private fun setupCommentInput() {
        // We want users with read permission to still be able to create and respond to comments.
        if(docSession.annotationMetadata?.canRead() == false) {
            commentInputContainer.setVisible(false)
        } else {
            sendCommentButton.imageTintList = ViewStyler.generateColorStateList(
                    intArrayOf(-android.R.attr.state_enabled) to ContextCompat.getColor(requireContext(), R.color.defaultTextGray),
                    intArrayOf() to ThemePrefs.buttonColor
            )

            sendCommentButton.isEnabled = false
            commentEditText.onTextChanged { sendCommentButton.isEnabled = it.isNotBlank() }
            sendCommentButton.onClickWithRequireNetwork {
                sendComment(commentEditText.text.toString())
            }
        }
    }

    private fun showSendingStatus() {
        sendCommentButton.setInvisible()
        sendingProgressBar.setVisible()
        sendingProgressBar.announceForAccessibility(getString(R.string.sendingSimple))
        sendingErrorTextView.setGone()
        commentEditText.isEnabled = false
    }

    private fun hideSendingStatus(success: Boolean) {
        sendingProgressBar.setGone()
        sendCommentButton.setVisible()
        commentEditText.isEnabled = true
        if (success) {
            commentEditText.setText("")
            commentEditText.hideKeyboard()
        } else {
            sendingErrorTextView.setVisible()
        }
    }


    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun sendComment(comment: String) {
        sendCommentJob = weave {
            try {
                showSendingStatus()
                //first we need to find the root comment
                val rootComment = annotations.firstOrNull()
                if (rootComment != null) {
                    val newCommentReply = awaitApi<CanvaDocAnnotation> { CanvaDocsManager.putAnnotation(apiValues.sessionId, generateAnnotationId(), createCommentReplyAnnotation(comment, headAnnotationId, apiValues.documentId, ApiPrefs.user?.id.toString(), rootComment.page), apiValues.canvaDocsDomain, it) }
                    EventBus.getDefault().post(PdfStudentSubmissionView.AnnotationCommentAdded(newCommentReply, assigneeId))
                    // The put request doesn't return this property, so we need to set it to true
                    newCommentReply.isEditable = true
                    recyclerAdapter?.add(newCommentReply) //ALSO, add it to the UI
                    hideSendingStatus(true)
                } else {
                    hideSendingStatus(false)
                }
            } catch (e: Throwable) {
                hideSendingStatus(false)
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun editComment(annotation: CanvaDocAnnotation, position: Int) {
        editCommentJob = tryWeave {
            awaitApi<CanvaDocAnnotation> { CanvaDocsManager.putAnnotation(apiValues.sessionId, annotation.annotationId, annotation, apiValues.canvaDocsDomain, it) }
            EventBus.getDefault().post(PdfStudentSubmissionView.AnnotationCommentEdited(annotation, assigneeId))
            // Update the UI
            recyclerAdapter?.add(annotation)
            recyclerAdapter?.notifyItemChanged(position)
        } catch {
            hideSendingStatus(false)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun deleteComment(annotation: CanvaDocAnnotation, position: Int) {
        deleteCommentJob = tryWeave {
            awaitApi<ResponseBody> { CanvaDocsManager.deleteAnnotation(apiValues.sessionId, annotation.annotationId, apiValues.canvaDocsDomain, it) }
            if(annotation.annotationId == annotations.firstOrNull()?.annotationId) {
                //this is the root comment, deleting this deletes the entire thread
                EventBus.getDefault().post(PdfStudentSubmissionView.AnnotationCommentDeleted(annotation, true, assigneeId))
                headAnnotationDeleted()
            } else {
                EventBus.getDefault().post(PdfStudentSubmissionView.AnnotationCommentDeleted(annotation, false, assigneeId))
                recyclerAdapter?.remove(annotation)
                recyclerAdapter?.notifyItemChanged(position)
            }
        } catch {
            hideSendingStatus(false)
        }
    }

    private fun headAnnotationDeleted() {
        activity?.onBackPressed()
    }

    companion object {
        private const val ANNOTATIONS = "annotations"
        private const val ASSIGNEE_ID = "assigneeId"
        private const val DOC_SESSION = "docSession"
        private const val API_VALUES = "apiValues"
        private const val HEAD_ANNOTATION_ID = "headAnnotationId"

        fun newInstance(bundle: Bundle) = AnnotationCommentListFragment().apply { arguments = bundle }

        fun makeRoute(annotations: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues, assigneeId: Long): Route {
            val args = makeBundle(annotations, headAnnotationId, docSession, apiValues, assigneeId)

            return Route(null, AnnotationCommentListFragment::class.java, null, args)
        }

        fun validRoute(route: Route): Boolean {
            return route.arguments.containsKey(ANNOTATIONS)
                    && route.arguments.containsKey(HEAD_ANNOTATION_ID)
                    && route.arguments.containsKey(DOC_SESSION)
                    && route.arguments.containsKey(API_VALUES)
                    && route.arguments.containsKey(ASSIGNEE_ID)
        }

        fun newInstance(route: Route): AnnotationCommentListFragment? {
            if (!validRoute(route)) return null
            return AnnotationCommentListFragment().withArgs(route.arguments)
        }

        fun makeBundle(annotations: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues, assigneeId: Long): Bundle {
            val args = Bundle()
            args.putParcelableArrayList(ANNOTATIONS, annotations)
            args.putLong(ASSIGNEE_ID, assigneeId)
            args.putParcelable(DOC_SESSION, docSession)
            args.putParcelable(API_VALUES, apiValues)
            args.putString(HEAD_ANNOTATION_ID, headAnnotationId)
            return args
        }
    }
}
