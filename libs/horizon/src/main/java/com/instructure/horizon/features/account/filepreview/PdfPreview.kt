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
package com.instructure.horizon.features.account.filepreview

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.instructure.pandautils.utils.getActivityOrNull
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView

@Composable
fun PdfPreview(
    documentUri: Uri,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivityOrNull()
    activity?.let {
        val userInterfaceViewMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_HIDDEN
        val pdfActivityConfiguration =
            PdfActivityConfiguration
                .Builder(it)
                .setUserInterfaceViewMode(userInterfaceViewMode)
                .scrollMode(PageScrollMode.CONTINUOUS)
                .scrollDirection(PageScrollDirection.VERTICAL)
                .build()

        val documentState = rememberDocumentState(documentUri, pdfActivityConfiguration)
        DocumentView(documentState, modifier = modifier)
    }
}