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
package com.instructure.pandautils.features.modules.externalurl

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.utils.ThemePrefs

/**
 * Shown for `ExternalUrl` module items.
 *
 * An `ExternalUrl` item's `htmlUrl` is an `/api/v1/.../module_item_redirect/:id` endpoint that only
 * redirects onward to an arbitrary third-party URL, so it cannot be rendered reliably in an embedded
 * WebView. Mirroring iOS, we show the item title and offer to open `external_url` in a browser.
 *
 * [onOpenClick] is null when the module item has no external URL to open, which hides the button.
 *
 * [navIconRes] lets a host override the nav button, e.g. Teacher's module pager uses the master/detail
 * expand/collapse icon on tablets rather than a back arrow.
 *
 * Bottom system bar insets are deliberately not applied here: Student's host consumes them for the
 * fragment container, and Teacher's module pager reserves that space for its own paging bar.
 */
@Composable
fun ExternalUrlScreen(
    title: String,
    onNavigationClick: () -> Unit,
    courseColor: Color = Color(ThemePrefs.primaryColor),
    onOpenClick: (() -> Unit)? = null,
    @DrawableRes navIconRes: Int = R.drawable.ic_back_arrow,
    navIconContentDescription: String = stringResource(R.string.back)
) {
    Scaffold(
        topBar = {
            CanvasThemedAppBar(
                title = title,
                navigationActionClick = onNavigationClick,
                backgroundColor = courseColor,
                contentColor = colorResource(R.color.textLightest),
                navIconRes = navIconRes,
                navIconContentDescription = navIconContentDescription
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.backgroundLightest))
                .padding(padding)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // EmptyContent centres itself vertically, so give it at least the viewport height to
                // keep it centred when it fits, and let it overflow into the scroll when it doesn't
                // (e.g. landscape, where the illustration alone is taller than the viewport).
                val minContentHeight = maxHeight
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    EmptyContent(
                        emptyMessage = stringResource(R.string.externalUrlBrowserOnly),
                        imageRes = R.drawable.ic_panda_unsupported_content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minContentHeight)
                    )
                }
            }
            onOpenClick?.let { OpenInBrowserButton(onClick = it) }
        }
    }
}

@Composable
private fun OpenInBrowserButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(ThemePrefs.buttonColor),
            contentColor = Color(ThemePrefs.buttonTextColor)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("openInBrowserButton")
    ) {
        Text(
            text = stringResource(R.string.openInBrowser),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExternalUrlScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ExternalUrlScreen(
        title = "Course Intro Video",
        onNavigationClick = {},
        onOpenClick = {}
    )
}
