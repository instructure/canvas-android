/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.instructure.annotations.PdfSubmissionView
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.instructure.student.AnnotationComments.AnnotationCommentListFragment
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import com.pspdfkit.preferences.PSPDFKitPreferences
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import kotlinx.android.synthetic.main.view_pdf_student_submission.view.*
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList

@SuppressLint("ViewConstructor")
class PdfStudentSubmissionView(
        context: Context,
        private val pdfUrl: String,
        private val studentAnnotation: Boolean = false
) : PdfSubmissionView(context), AnnotationManager.OnAnnotationCreationModeChangeListener, AnnotationManager.OnAnnotationEditingModeChangeListener {

    private var initJob: Job? = null
    private var deleteJob: Job? = null

    override val annotationToolbarLayout: ToolbarCoordinatorLayout
        get() = findViewById(R.id.annotationToolbarLayout)
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
        get() = findViewById(R.id.inspectorCoordinatorLayout)
    override val commentsButton: ImageView
        get() = findViewById(R.id.commentsButton)
    override val loadingContainer: FrameLayout
        get() = findViewById(R.id.loadingContainer)
    override val progressBar: ProgressiveCanvasLoadingView
        get() = findViewById(R.id.progressBar)
    override val progressColor: Int
        get() = R.color.login_studentAppTheme

    override fun disableViewPager() {}
    override fun enableViewPager() {}
    override fun setIsCurrentlyAnnotating(boolean: Boolean) {}

    override fun showAnnotationComments(commentList: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues) {
        if (isAttachedToWindow) RouteMatcher.route(context, AnnotationCommentListFragment.makeRoute(commentList, headAnnotationId, docSession, apiValues, ApiPrefs.user!!.id))
    }

    override fun showFileError() {
        loadingView.setGone()
        retryLoadingContainer.setVisible()
        retryLoadingButton.onClick {
            setLoading(true)
            setup()
        }
    }

    override fun configureCommentView(commentsButton: ImageView) {
        //we want to offset the comment button by the height of the action bar
        val marginDp = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics)
        val layoutParams = commentsButton.layoutParams as LayoutParams
        commentsButton.drawable.setTint(Color.WHITE)
        layoutParams.gravity = Gravity.END or Gravity.TOP
        layoutParams.topMargin = marginDp.toInt()
        layoutParams.rightMargin = marginDp.toInt()

        commentsButton.onClick {
            openComments()
        }
    }

    override fun logOnAnnotationSelectedAnalytics() {
        Analytics.logEvent(AnalyticsEventConstants.SUBMISSION_ANNOTATION_SELECTED)
    }

    override fun showNoInternetDialog() {
        NoInternetConnectionDialog.show(supportFragmentManager)
    }

    init {
        if (!PSPDFKitPreferences.get(getContext()).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(getContext()).setAnnotationCreator(ApiPrefs.user?.name)
        }

        View.inflate(context, R.layout.view_pdf_student_submission, this)

        setLoading(true)
    }

    private fun setLoading(isLoading: Boolean) {
        loadingView?.setVisible(isLoading)
        contentRoot?.setVisible(!isLoading)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //we must set up the sliding panel prior to registering to the event
        EventBus.getDefault().register(this)

        setup()
    }

    override fun attachDocListener() {
        // We need to add this flag, because we want to show the toolbar in the student annotation, but hide when
        // we open an already submitted file submission with a teacher's annotations.
        if (!studentAnnotation) {
            // Modify the session data permissions to make sure students can't annotate already submitted assignments
            if (docSession.annotationMetadata?.canWrite() == true) {
                docSession.annotationMetadata?.permissions = "read"
            }
            // Default is to have top inset, remove this since there will be no toolbar
            pdfFragment?.setInsets(0, 0, 0, 0)
        }
        super.attachDocListener()
    }

    fun setup() {
        handlePdfContent(pdfUrl)
        setLoading(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        initJob?.cancel()
        EventBus.getDefault().unregister(this)
    }


    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (isAttachedToWindow) supportFragmentManager.beginTransaction().replace(content.id, fragment).commitNowAllowingStateLoss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentAdded(event: AnnotationCommentAdded) {
        if (event.assigneeId == ApiPrefs.user!!.id) {
            //add the comment to the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.add(event.annotation)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentEdited(event: AnnotationCommentEdited) {
        if (event.assigneeId == ApiPrefs.user!!.id) {
            //update the annotation in the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.find { it.annotationId == event.annotation.annotationId }?.contents = event.annotation.contents
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleted(event: AnnotationCommentDeleted) {
        if (event.assigneeId == ApiPrefs.user!!.id) {
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
        if (event.assigneeId == ApiPrefs.user!!.id) {
            deleteJob = tryWeave {
                for (annotation in event.annotationList) {
                    awaitApi<ResponseBody> { CanvaDocsManager.deleteAnnotation(apiValues.sessionId, annotation.annotationId, apiValues.canvaDocsDomain, it) }
                    commentRepliesHashMap[annotation.inReplyTo]?.remove(annotation)
                }
            } catch {
                Logger.d("There was an error acknowledging the delete!")
            }
        }
    }

    class AnnotationCommentAdded(val annotation: CanvaDocAnnotation, val assigneeId: Long)
    class AnnotationCommentEdited(val annotation: CanvaDocAnnotation, val assigneeId: Long)
    class AnnotationCommentDeleted(val annotation: CanvaDocAnnotation, val isHeadAnnotation: Boolean, val assigneeId: Long)
    class AnnotationCommentDeleteAcknowledged(val annotationList: List<CanvaDocAnnotation>, val assigneeId: Long)
}