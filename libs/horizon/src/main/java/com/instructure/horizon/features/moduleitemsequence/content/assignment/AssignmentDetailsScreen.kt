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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.moduleitemsequence.content.assignment

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.TextSubmissionContent
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.SegmentedControl
import com.instructure.horizon.horizonui.molecules.SegmentedControlIconPosition
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.CanvasWebView

@Composable
fun AssignmentDetailsScreen(uiState: AssignmentDetailsUiState, scrollState: ScrollState, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.getActivityOrNull()
    LoadingStateWrapper(loadingState = uiState.loadingState, containerColor = Color.Transparent) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.cardPrimary())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                HorizonSpace(SpaceSize.SPACE_24)
                Text(
                    stringResource(R.string.assignmentDetails_instructions),
                    style = HorizonTypography.h3,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizonSpace(SpaceSize.SPACE_8)
                ComposeCanvasWebViewWrapper(
                    html = uiState.instructions,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        canvasEmbeddedWebViewCallback = embeddedWebViewCallback
                        canvasWebViewClientCallback = webViewClientCallback
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                    }
                )
                if (uiState.ltiUrl.isNotEmpty()) {
                    HorizonSpace(SpaceSize.SPACE_24)
                    ComposeCanvasWebView(
                        uiState.ltiUrl,
                        modifier = modifier
                            .height(400.dp)
                            .padding(horizontal = 16.dp),
                        embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                            shouldLaunchInternalWebViewFragment = { _ -> true },
                            launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                        ),
                        applyOnWebView = {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setInitialScale(100)
                            setZoomSettings(false)
                            activity?.let { addVideoClient(it) }
                        })
                    HorizonSpace(SpaceSize.SPACE_24)
                }
                HorizonSpace(SpaceSize.SPACE_40)
                if (uiState.showSubmissionDetails) {
                    SubmissionDetailsContent(uiState.submissionDetailsUiState)
                }
                if (uiState.showAddSubmission) {
                    AddSubmissionContent(uiState.addSubmissionUiState)
                }
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}

@Composable
private fun ColumnScope.SubmissionDetailsContent(uiState: SubmissionDetailsUiState, modifier: Modifier = Modifier) {
    SubmissionContent(uiState.submissions.find { it.submissionAttempt == uiState.currentSubmissionAttempt }
        ?: uiState.submissions.first(), modifier = modifier)
    HorizonSpace(SpaceSize.SPACE_40)
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Button(
            label = stringResource(R.string.assignmentDetails_newAttempt),
            color = ButtonColor.Institution,
            onClick = uiState.onNewAttemptClick
        )
    }
}

@Composable
fun SubmissionContent(uiState: SubmissionUiState, modifier: Modifier = Modifier) {
    when (uiState.submissionContent) {
        is SubmissionContent.TextSubmission -> TextSubmissionContent(text = uiState.submissionContent.text, modifier = modifier)
        is SubmissionContent.FileSubmission -> {}
    }
}

@Composable
fun ColumnScope.AddSubmissionContent(uiState: AddSubmissionUiState, modifier: Modifier = Modifier) {
    if (uiState.submissionTypes.size > 1) {
        Text(stringResource(R.string.assignmentDetails_selectSubmissionType), style = HorizonTypography.h3)
        HorizonSpace(SpaceSize.SPACE_16)
        val options = uiState.submissionTypes.map { stringResource(it.labelRes) }
        SegmentedControl(
            options = options,
            onItemSelected = uiState.onSubmissionTypeSelected,
            selectedIndex = uiState.selectedSubmissionTypeIndex,
            iconPosition = SegmentedControlIconPosition.Start(checkmark = true),
            modifier = modifier
        )
        HorizonSpace(SpaceSize.SPACE_24)
    }
    if (uiState.submissionTypes.isNotEmpty()) {
        val selectedSubmissionType = uiState.submissionTypes[uiState.selectedSubmissionTypeIndex]
        when (selectedSubmissionType) {
            is AddSubmissionTypeUiState.File -> Text(text = "File Submission") // TODO Submission ticket
            is AddSubmissionTypeUiState.Text -> Text(text = "Text Submission") // TODO Submission ticket
        }
    }
}

private val embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
    override fun launchInternalWebViewFragment(url: String) = Unit

    override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
}

private val webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
    override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit

    override fun onPageStartedCallback(webView: WebView, url: String) = Unit

    override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

    override fun canRouteInternallyDelegate(url: String) = false

    override fun routeInternallyCallback(url: String) = Unit
}

@Preview
@Composable
fun AssignmentDetailsScreenPreview() {
    AssignmentDetailsScreen(
        uiState = AssignmentDetailsUiState(
            instructions = "This is a test",
            ltiUrl = "",
            submissionDetailsUiState = SubmissionDetailsUiState(
                submissions = listOf(
                    SubmissionUiState(
                        submissionAttempt = 1L,
                        submissionContent = SubmissionContent.TextSubmission("This is a test"),
                        date = "2023-10-01"
                    )
                ),
                currentSubmissionAttempt = 1L
            ),
            showSubmissionDetails = true
        ),
        scrollState = ScrollState(0)
    )
}