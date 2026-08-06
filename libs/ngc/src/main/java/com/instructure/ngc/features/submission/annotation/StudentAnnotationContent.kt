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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.fragment.compose.AndroidFragment
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.StudentAnnotationUiState
import com.instructure.ngc.features.submission.annotation.comments.AnnotationCommentsBottomSheet
import com.instructure.pandautils.utils.getFragmentActivityOrNull

/**
 * Renders the `STUDENT_ANNOTATION` teacher-provided document INLINE in the NGC submission pane —
 * the student views and annotates the PSPDFKit surface directly within the pane, with no separate
 * modal/activity (see MBL-19937; this deliberately diverges from the iOS clone MBL-19915, which opens
 * a modal annotation window).
 *
 * The embedded [PdfAnnotationSubmissionView] hosts a PSPDFKit `PdfFragment`, which requires a
 * `FragmentManager` from a `FragmentActivity` host. NGC's host activity (`NGCActivity`) extends
 * `BaseCanvasActivity` -> `AppCompatActivity`, so a `FragmentManager` is always available in practice;
 * this composable still fails loud into the error state (rather than crashing inside PSPDFKit) if that
 * ever stops being true.
 *
 * The `PdfFragment` itself is NOT committed into the Activity's `supportFragmentManager` directly.
 * Instead, [PdfAnnotationHostFragment] is embedded via [AndroidFragment] (`androidx.fragment:fragment-compose`)
 * and owns a `childFragmentManager` used to host the `PdfFragment` (see [PdfAnnotationHostFragment] and
 * [PdfAnnotationSubmissionView] for why: it isolates config-change fragment restoration from the
 * Compose-hosted container's layout timing).
 *
 * [AndroidFragment]'s `onUpdate` callback fires once the hosted [PdfAnnotationHostFragment] instance is
 * added/found, which is used here to hand the ViewModel a reference to that instance's
 * [PdfAnnotationHostFragment.flushAnnotations] via [StudentAnnotationUiState.registerAnnotationFlush], so
 * `onSubmit` can await in-flight annotation writes before finalizing the attempt (see MBL-19937 Phase 4).
 * The registration is cleared on dispose so the ViewModel never holds a reference into a torn-down
 * fragment/view.
 *
 * The same `onUpdate` callback also registers [PdfAnnotationHostFragment.setOnShowAnnotationComments], so
 * that when the user re-taps the comments button on an annotation that already has replies, the resulting
 * [PdfAnnotationSubmissionView.onShowAnnotationComments] event opens [AnnotationCommentsBottomSheet] as a
 * Tray LAYERED OVER this composable (see MBL-19937 follow-on plan, Phase B).
 *
 * The thread is deliberately NOT a Navigation-Compose destination. Navigating would have disposed the
 * submission destination — and with it this composable, the [AndroidFragment], the host fragment and the
 * whole PSPDFKit surface — so returning re-ran `handlePdfContent(sessionUrl)`, re-downloading the document
 * and resetting scroll/zoom to page 1 every time. It also made the result round trip pointless (the patch
 * landed on an instance about to be replaced) and let `flushAnnotations()` report success while the
 * previous view instance's annotation writes were still in flight. As a Tray the pane stays composed, so
 * the result can be handed straight to [PdfAnnotationHostFragment.deliverCommentThreadResult] with no
 * `SavedStateHandle` round trip, no `RouteMatcher` and no `EventBus`.
 */
@Composable
fun StudentAnnotationContent(
    uiState: StudentAnnotationUiState,
    courseId: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val courseColor = LocalCourseColor.current
    val fragmentActivity = context.getFragmentActivityOrNull()

    var hostFragment by remember { mutableStateOf<PdfAnnotationHostFragment?>(null) }
    var commentThreadRequest by remember { mutableStateOf<CommentThreadRequest?>(null) }

    commentThreadRequest?.let { request ->
        AnnotationCommentsBottomSheet(
            request = request,
            onDismiss = { result ->
                hostFragment?.deliverCommentThreadResult(result)
                commentThreadRequest = null
            },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.sessionLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("studentAnnotation_loading"),
                    contentAlignment = Alignment.Center,
                ) {
                    Spinner(color = courseColor)
                }
            }
            // A blank sessionUrl is treated the same as an explicit sessionLoadError (the Phase-2
            // domain layer already errors on a blank canvadocs session URL, so in practice this only
            // covers the initial/default StudentAnnotationUiState before a load has ever run).
            uiState.sessionLoadError != null || uiState.sessionUrl.isBlank() || fragmentActivity == null -> {
                StudentAnnotationErrorState(
                    message = uiState.sessionLoadError ?: stringResource(R.string.ngc_submission_loadError),
                    onRetry = uiState.onRetrySessionLoad,
                    modifier = Modifier.testTag("studentAnnotation_error"),
                )
            }
            else -> {
                // Re-key on the session URL so a genuinely new draft session (e.g. after a retry)
                // gets a fresh PdfAnnotationHostFragment/PdfAnnotationSubmissionView/PdfFragment
                // instead of mutating the old one.
                key(uiState.sessionUrl) {
                    // The document load is kicked off internally by PdfAnnotationSubmissionView's
                    // own onAttachedToWindow() override, not here — see its class doc.
                    AndroidFragment<PdfAnnotationHostFragment>(
                        modifier = Modifier.fillMaxSize(),
                        arguments = PdfAnnotationHostFragment.createArguments(courseId, uiState.sessionUrl),
                        onUpdate = { fragment ->
                            hostFragment = fragment
                            uiState.registerAnnotationFlush(fragment::flushAnnotations)
                            fragment.setOnShowAnnotationComments { request ->
                                commentThreadRequest = request
                            }
                        },
                    )
                    DisposableEffect(Unit) {
                        onDispose {
                            uiState.registerAnnotationFlush(null)
                            hostFragment?.setOnShowAnnotationComments(null)
                            hostFragment = null
                            // The Tray must not outlive the surface it reports back into.
                            commentThreadRequest = null
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentAnnotationErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = InstUISemanticColors.Text.error(),
            )
            Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceMd.spaceMd))
            TextButton(
                text = stringResource(R.string.ngc_submission_retry),
                onClick = onRetry,
            )
        }
    }
}
