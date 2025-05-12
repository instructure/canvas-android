/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */package com.instructure.teacher.features.speedgrader

import android.annotation.SuppressLint
import android.app.Activity
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.instructure.annotations.PdfSubmissionView
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ViewPdfTeacherSubmissionBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.fragments.SpeedGraderEmptyFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.isTablet
import com.instructure.teacher.view.AnnotationCommentAdded
import com.instructure.teacher.view.AnnotationCommentDeleteAcknowledged
import com.instructure.teacher.view.AnnotationCommentDeleted
import com.instructure.teacher.view.AnnotationCommentEdited
import com.pspdfkit.preferences.PSPDFKitPreferences
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PdfTeacherSubmissionView(private val activity: FragmentActivity,
                               private val pdfUrl: String,
                               private val courseId: Long,
                               private val assigneeId: Long,
                               private val fragmentManager: FragmentManager,
                               private val peerReviews: Boolean = false,
                               private val studentAnnotationSubmit: Boolean = false,
                               private val studentAnnotationView: Boolean = false,) : PdfSubmissionView(activity, studentAnnotationView, courseId) {

    private val binding: ViewPdfTeacherSubmissionBinding

    private var initJob: Job? = null
    private var deleteJob: Job? = null

    override val annotationToolbarLayout: ToolbarCoordinatorLayout
        get() = binding.annotationToolbarLayout
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
        get() = binding.inspectorCoordinatorLayout
    override val commentsButton: ImageView
        get() = binding.commentsButton
    override val loadingContainer: FrameLayout
        get() = binding.loadingContainer
    override val progressBar: ProgressiveCanvasLoadingView
        get() = binding.progressBar
    override val progressColor: Int
        get() = R.color.login_studentAppTheme

    init {
        if (!PSPDFKitPreferences.get(getContext()).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(getContext()).setAnnotationCreator(ApiPrefs.user?.name)
        }

        binding = ViewPdfTeacherSubmissionBinding.inflate(LayoutInflater.from(context), this, true)

        setLoading(true)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingView.setVisible(isLoading)
        binding.contentRoot.setVisible(!isLoading)
    }

    fun setup() {
        handlePdfContent(pdfUrl)
        setLoading(false)
    }

    override fun attachDocListener() {
        if (peerReviews.not()) {
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)

        setup()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        initJob?.cancel()
        EventBus.getDefault().unregister(this)
    }

    override fun disableViewPager() {}
    override fun enableViewPager() {}
    override fun setIsCurrentlyAnnotating(boolean: Boolean) {}

    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (isAttachedToWindow) fragmentManager.beginTransaction().replace(binding.content.id, fragment).commitNowAllowingStateLoss()
    }

    override fun removeContentFragment() {
        val contentFragment = fragmentManager.findFragmentById(binding.content.id)
        if (contentFragment != null) {
            fragmentManager.beginTransaction().remove(contentFragment).commitAllowingStateLoss()
        }
    }

    override fun showNoInternetDialog() {
        NoInternetConnectionDialog.show(fragmentManager)
    }

    override fun showAnnotationComments(commentList: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues) {
        val bundle = AnnotationCommentListFragment.makeBundle(commentList, headAnnotationId, docSession, apiValues, assigneeId)
        //if isTablet, we need to prevent the sliding panel from moving opening all the way with the keyboard
        if(context.isTablet) {
            setIsCurrentlyAnnotating(true)
        }
        RouteMatcher.route(activity as FragmentActivity, Route(AnnotationCommentListFragment::class.java, null, bundle))
    }

    override fun showFileError() {
        setFragment(SpeedGraderEmptyFragment.newInstance(message = context.getString(R.string.error_loading_files)))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentAdded(event: AnnotationCommentAdded) {
        if (event.assigneeId == assigneeId) {
            //add the comment to the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.add(event.annotation)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentEdited(event: AnnotationCommentEdited) {
        if (event.assigneeId == assigneeId) {
            //update the annotation in the hashmap
            commentRepliesHashMap[event.annotation.inReplyTo]?.find { it.annotationId == event.annotation.annotationId }?.contents = event.annotation.contents
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnnotationCommentDeleted(event: AnnotationCommentDeleted) {
        if (event.assigneeId == assigneeId) {
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
        if (event.assigneeId == assigneeId) {
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
}