/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assessment

import android.graphics.Color
import android.os.Handler
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.instructure.canvasapi2.models.LtiType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.getActivityOrNull

private const val JS_BRIDGE_NAME = "QuizJsBridge"
private const val QUIZ_SUBMITTED_MESSAGE = "quiz.submitted"

private val removeReturnButtonCss = """
    a[data-automation="sdk-return-button"] {
        display: none;
    }
""".trimIndent()

private val removeReturnButtonJs = """
    (function() {
        var style = document.createElement('style');
        style.type = 'text/css';
        style.innerHTML = `${removeReturnButtonCss}`;
        document.head.appendChild(style);
    })();
""".trimIndent()

val quizSubmittedListener = """
    (function() {
        window.addEventListener("message", function(event) {
            if (event.data && event.data.type === "quiz.resultContentRendered") {
                $JS_BRIDGE_NAME.postMessage("$QUIZ_SUBMITTED_MESSAGE");
            }
        });
    })();
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentContentScreen(
    uiState: AssessmentUiState,
    modifier: Modifier = Modifier,
    onAssessmentSubmitted: () -> Unit = {}
) {
    val activity = LocalContext.current.getActivityOrNull()
    if (uiState.showAssessmentDialog) {
        Dialog(
            onDismissRequest = {
                if (!uiState.assessmentCompletionLoading) {
                    uiState.onAssessmentClosed()
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .background(
                        HorizonColors.Surface.pageSecondary(),
                        shape = HorizonCornerRadius.level5
                    ).testTag("AssessmentDialog")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = uiState.assessmentName,
                        style = HorizonTypography.h3,
                        modifier = Modifier
                            .padding(horizontal = 52.dp)
                            .align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                    if (uiState.assessmentCompletionLoading) {
                        Spinner(
                            modifier = Modifier
                                .align(Alignment.CenterEnd),
                            size = SpinnerSize.EXTRA_SMALL,
                            color = HorizonColors.Surface.institution(),
                            progress = null
                        )
                    } else {
                        val context = LocalContext.current
                        IconButton(
                            iconRes = R.drawable.close,
                            contentDescription = context.getString(R.string.a11y_close),
                            color = IconButtonColor.Inverse,
                            modifier = Modifier
                                .align(Alignment.CenterEnd),
                            elevation = HorizonElevation.level4,
                            onClick = uiState.onAssessmentClosed,
                            size = IconButtonSize.SMALL
                        )
                    }
                }
                HorizonSpace(SpaceSize.SPACE_8)
                Box {
                    if (uiState.urlToLoad != null) {
                        ComposeCanvasWebView(
                            uiState.urlToLoad, webViewCallbacks = ComposeWebViewCallbacks(
                                onPageFinished = { webView, url ->
                                    Handler().post {
                                        webView.evaluateJavascript(removeReturnButtonJs, null)
                                        webView.evaluateJavascript(quizSubmittedListener, null)
                                    }
                                    if (url.contains(LtiType.NEW_QUIZZES_LTI.id.orEmpty())) {
                                        uiState.onAssessmentLoaded()
                                    }
                                }
                            ), embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                                shouldLaunchInternalWebViewFragment = { _ -> false },
                            ), applyOnWebView = {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(Color.TRANSPARENT)
                                setInitialScale(100)
                                activity?.let { addVideoClient(it) }
                                addJavascriptInterface(JSBridge {
                                    onAssessmentSubmitted()
                                    uiState.onAssessmentCompletion()
                                }, JS_BRIDGE_NAME)
                            }, modifier = Modifier.alpha(if (uiState.assessmentLoading) 0f else 1f)
                        )
                    }
                    if (uiState.assessmentLoading) {
                        Spinner(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    LoadingStateWrapper(
        loadingState = uiState.loadingState,
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level5),
        containerColor = HorizonColors.Surface.pageSecondary()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.TopCenter
        ) {
            Button(
                label = stringResource(R.string.assessment_startQuiz),
                color = ButtonColor.Institution,
                onClick = uiState.onStartQuizClicked
            )
        }
    }
}

@Composable
@Preview
private fun AssessmentContentScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AssessmentContentScreen(AssessmentUiState())
}

class JSBridge(val onEventReceived: () -> Unit) {
    @JavascriptInterface
    fun postMessage(message: String) {
        if (message == QUIZ_SUBMITTED_MESSAGE) {
            onEventReceived()
        }
    }
}