/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.ngc.features.submission.annotation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.instructure.annotations.PdfSubmissionView
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.ngc.R
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.ProgressiveCanvasLoadingView
import com.pspdfkit.preferences.PSPDFKitPreferences
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout

private const val COMMENTS_BUTTON_SIZE_DP = 48
private const val COMMENTS_BUTTON_PADDING_DP = 10
private const val COMMENTS_BUTTON_ELEVATION_DP = 2f

/**
 * NGC-specific concrete subclass of [PdfSubmissionView] that hosts the PSPDFKit `PdfFragment` INLINE
 * inside the NGC submission pane (see MBL-19937 — Android deliberately diverges from iOS here, which
 * opens a separate modal annotation window; iOS is not the template for this class).
 *
 * Mirrors the mechanics of the classic Student app's `PdfStudentSubmissionView` (canvadocs "draft"
 * session load + annotation creation/update/delete against the same session), but:
 *  - Is built entirely programmatically rather than via an XML layout / `ViewBinding`, since `:ngc`
 *    is Compose-only (no `res/layout`, no `viewBinding` build feature).
 *  - Is submission-only: always constructed with `studentAnnotationView = false` (write mode). Viewing
 *    an already-submitted annotation attempt is out of scope for MBL-19937 (see the plan's "Out of
 *    scope" section; that's the closed MBL-19938 "Submission preview" flow).
 *  - [commentsButton] IS a real child of this view (added after [fragmentContainer] — and elevated —
 *    so it draws/receives touches above the `PdfFragment` content, mirroring
 *    `view_pdf_student_submission.xml`'s `commentsButton` + `content` ordering). [configureCommentView]
 *    is intentionally NOT overridden: the base's default implementation already ports the classic
 *    action-bar-offset positioning (see `PdfStudentSubmissionView.configureCommentView`'s
 *    `studentAnnotationSubmit` branch, which just delegates to the same base default), and it wires the
 *    click listener that calls [openComments].
 *  - [showAnnotationComments] bubbles the "open the multi-reply thread" event up to Compose navigation
 *    via [onShowAnnotationComments] (a settable callback, mirroring [flushAnnotations]'s registration
 *    pattern) instead of routing itself — this View has no NavController and must not reach for
 *    RouteMatcher/EventBus. [onCommentThreadResult] applies the outcome on return — see both docs.
 *  - Uses a STABLE resource id ([R.id.ngc_pdf_annotation_fragment_container]) for the `PdfFragment`
 *    container, rather than [View.generateViewId], so the container id survives a config-change-driven
 *    Activity recreation (a freshly generated id would leave any FragmentManager-restored `PdfFragment`
 *    pointing at a container that no longer exists). [sessionUrl] is loaded from [onAttachedToWindow]
 *    (rather than via an external post-construction call) so that any attach of this view — including a
 *    detach/reattach of the same instance, or the fresh instance created after a config change — always
 *    (re)starts the document load, so the pane is never left blank.
 */
@SuppressLint("ViewConstructor")
class PdfAnnotationSubmissionView(
    context: Context,
    private val fragmentManager: FragmentManager,
    courseId: Long,
    private val sessionUrl: String,
) : PdfSubmissionView(context, studentAnnotationView = false, courseId = courseId) {

    private val fragmentContainer: FrameLayout = FrameLayout(context).apply {
        id = R.id.ngc_pdf_annotation_fragment_container
    }

    override val annotationToolbarLayout: ToolbarCoordinatorLayout = ToolbarCoordinatorLayout(context)
    override val inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout = PropertyInspectorCoordinatorLayout(context)

    // Real, attached ImageView (see class doc) mirroring the classic commentsButton: 48dp, discussion
    // icon tinted white, gone until the base's onPrepareAnnotationSelection shows it for a selected
    // annotation. Positioning (gravity/margins) is applied by the base's default configureCommentView.
    override val commentsButton: ImageView = ImageView(context).apply {
        setImageResource(R.drawable.ic_discussion)
        setBackgroundResource(R.drawable.bg_annotation_comments)
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
        contentDescription = context.getString(R.string.comments)
        val paddingPx = context.DP(COMMENTS_BUTTON_PADDING_DP).toInt()
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        elevation = context.DP(COMMENTS_BUTTON_ELEVATION_DP)
        isClickable = true
        isFocusable = true
        setGone()
    }

    override val loadingContainer: FrameLayout = FrameLayout(context).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundLightest))
        setGone()
    }
    override val progressBar: ProgressiveCanvasLoadingView = ProgressiveCanvasLoadingView(context)

    override val progressColor: Int
        get() = R.color.textDarkest

    init {
        // Mirrors PdfStudentSubmissionView/PdfTeacherSubmissionView: must be set before the PdfFragment
        // loads and starts creating annotations, otherwise PSPDFKit prompts the user to enter an author
        // name the first time an annotation is created.
        if (!PSPDFKitPreferences.get(context).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(context).setAnnotationCreator(ApiPrefs.user?.name)
        }

        val matchParent = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // Child order mirrors the classic view_pdf_student_submission.xml layout: toolbar/inspector
        // coordinator layouts first, then the fragment container, then commentsButton (elevated so it
        // stays interactive above the fragment content), then the loading overlay on top of everything.
        addView(annotationToolbarLayout, matchParent)
        addView(inspectorCoordinatorLayout, matchParent)
        addView(fragmentContainer, matchParent)
        addView(
            commentsButton,
            FrameLayout.LayoutParams(context.DP(COMMENTS_BUTTON_SIZE_DP).toInt(), context.DP(COMMENTS_BUTTON_SIZE_DP).toInt())
        )

        loadingContainer.addView(
            progressBar,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )
        addView(loadingContainer, matchParent)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Started here (rather than eagerly at construction time) so that every attach — including a
        // detach/reattach of this same instance, and the fresh instance built after a config change —
        // (re)starts the load. This also avoids racing setFragment()'s own isAttachedToWindow guard,
        // which would silently drop the PdfFragment transaction if the document happened to finish
        // loading before the view was attached.
        //
        // The reload is deliberately unconditional. It used to also fire on every comment-thread round
        // trip, which made it expensive, but the thread is now a Tray layered over the still-composed
        // pane (see StudentAnnotationContent), so the only remaining triggers are a genuine
        // detach/reattach or a config change — where re-fetching is the correct, safe behaviour. An
        // "already loaded" guard here would risk leaving the pane permanently blank whenever it
        // misjudged that state, which is exactly what loading on every attach was written to prevent.
        handlePdfContent(sessionUrl)
    }

    override fun disableViewPager() = Unit

    override fun enableViewPager() = Unit

    // No-op, matching PdfStudentSubmissionView's studentAnnotationSubmit flow (the equivalent classic
    // inline annotate-and-submit path), which also no-ops this: it only exists in the base to keep a
    // sliding panel out of the way while annotating, and neither classic's nor NGC's inline submission
    // host has one.
    override fun setIsCurrentlyAnnotating(boolean: Boolean) = Unit

    // Registered by the Compose layer (via StudentAnnotationUiState.onShowAnnotationComments, forwarded
    // through PdfAnnotationHostFragment.setOnShowAnnotationComments) so showAnnotationComments() below can
    // bubble the "open the thread" event up to Compose navigation instead of routing itself — this View
    // has no NavController and must not reach for RouteMatcher/EventBus (see MBL-19937 follow-on plan,
    // Phase B: "Out of scope — RouteMatcher/EventBus").
    var onShowAnnotationComments: ((CommentThreadRequest) -> Unit)? = null


    // Reachable once an annotation already has a comment reply (openComments()'s "else" branch) — adding
    // the FIRST comment on an annotation is unaffected: that goes through the (already-functional)
    // AnnotationCommentDialog path in openComments()'s "if" branch. commentList/docSession/apiValues carry
    // more than the destination screen needs over Compose nav args; only headAnnotationId plus a handful
    // of primitives are threaded through (see CommentThreadRequest) — the screen re-loads the current
    // reply list itself via GetAnnotationCommentRepliesUseCase, so this deliberately ignores commentList.
    override fun showAnnotationComments(
        commentList: ArrayList<CanvaDocAnnotation>,
        headAnnotationId: String,
        docSession: DocSession,
        apiValues: ApiValues
    ) {
        val metadata = docSession.annotationMetadata
        onShowAnnotationComments?.invoke(
            CommentThreadRequest(
                headAnnotationId = headAnnotationId,
                sessionId = apiValues.sessionId,
                canvaDocsDomain = apiValues.canvaDocsDomain,
                documentId = apiValues.documentId,
                canWrite = metadata?.canWrite() == true,
                canManage = metadata?.canManage() == true,
                canComment = metadata?.canRead() != false,
                currentUserId = metadata?.userId.orEmpty(),
            )
        )
    }

    /**
     * Applies the outcome of a Compose-native comment-thread visit for [CommentThreadResult.headAnnotationId]:
     * patches [commentRepliesHashMap] directly with [CommentThreadResult.replies] instead of calling the
     * base's `updateAnnotations()`/`loadAnnotations()` again. A second `loadAnnotations()` call on the same
     * live `PdfFragment`/document is NOT idempotent — it unconditionally re-`addAnnotationToPage`s every
     * non-reply annotation with no existence check, which would duplicate every already-rendered
     * annotation on the page — so this instead mirrors classic's own EventBus
     * `AnnotationCommentAdded/Edited/Deleted` handlers (`PdfStudentSubmissionView`), which likewise patch
     * `commentRepliesHashMap` precisely rather than re-fetching everything.
     *
     * Also best-effort refreshes the on-page "has comment" dot for the head annotation when it's still
     * the current selection (the common case right after returning from the thread screen without the
     * PdfFragment having been torn down/recreated in between). If the annotation isn't found (e.g. the
     * host Fragment/View was recreated while the thread screen was open), this is a no-op: a fresh
     * instance's own first `loadAnnotations()` will already compute the correct indicator from the
     * server's current state.
     */
    fun onCommentThreadResult(result: CommentThreadResult) {
        if (result.replies.isEmpty()) {
            commentRepliesHashMap.remove(result.headAnnotationId)
        } else {
            commentRepliesHashMap[result.headAnnotationId] = ArrayList(result.replies)
        }

        val selectedAnnotation = pdfFragment?.selectedAnnotations?.firstOrNull { it.name == result.headAnnotationId }
        if (selectedAnnotation != null) {
            selectedAnnotation.contents = if (result.replies.isEmpty()) null else "comment"
            pdfFragment?.notifyAnnotationHasChanged(selectedAnnotation)
        }
    }

    override fun showFileError() {
        // The assignment itself loaded fine at this point — it's the canvadocs document that failed,
        // so this must not reuse the assignment-level load error string.
        toast(R.string.ngc_annotation_documentLoadError)
    }

    override fun showNoInternetDialog() {
        toast(R.string.noInternetConnectionMessage)
    }

    @SuppressLint("CommitTransaction")
    override fun setFragment(fragment: Fragment) {
        if (isAttachedToWindow) {
            fragmentManager.beginTransaction().replace(fragmentContainer.id, fragment).commitNowAllowingStateLoss()
        }
    }

    override fun removeContentFragment() {
        val contentFragment = fragmentManager.findFragmentById(fragmentContainer.id)
        if (contentFragment != null) {
            fragmentManager.beginTransaction().remove(contentFragment).commitAllowingStateLoss()
        }
    }

    /**
     * Awaits every in-flight PSPDFKit annotation create/update/delete network write so they've had a
     * chance to reach the canvadocs draft session, then reports whether all of them (in-flight or
     * already completed) succeeded. Call this before finalizing a submission and abort if it returns
     * `false` — see [PdfSubmissionView.awaitPendingAnnotationWrites] for the failure-tracking details.
     */
    suspend fun flushAnnotations(): Boolean = try {
        awaitPendingAnnotationWrites()
    } catch (e: Exception) {
        false
    }
}
