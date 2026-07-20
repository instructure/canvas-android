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
package com.instructure.annotations.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.instructure.pandautils.utils.getActivityOrNull
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.jetpack.compose.interactors.AnnotationListener
import com.pspdfkit.jetpack.compose.interactors.DocumentListener
import com.pspdfkit.jetpack.compose.interactors.getDefaultDocumentManager
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView

/**
 * Read-only PSPDFKit preview of a local PDF document. The toolbar is hidden so the document renders
 * as a plain, scrollable preview. Lives in the `:annotations` module (the PSPDFKit owner) so it can
 * be shared by any Compose feature that needs an inline PDF preview.
 *
 * [annotations] are drawn onto the document once it loads — used for CanvaDoc submissions whose
 * annotations aren't baked into the PDF and must be layered on. Empty for plain PDF previews.
 */
@Composable
fun PdfPreview(
    documentUri: Uri,
    modifier: Modifier = Modifier,
    annotations: List<Annotation> = emptyList(),
) {
    val activity = LocalContext.current.getActivityOrNull()
    activity?.let {
        val userInterfaceViewMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_HIDDEN
        val pdfActivityConfiguration =
            PdfActivityConfiguration
                .Builder(it)
                .setUserInterfaceViewMode(userInterfaceViewMode)
                .immersiveModeEnabled(false)
                .scrollMode(PageScrollMode.CONTINUOUS)
                .scrollDirection(PageScrollDirection.VERTICAL)
                // Read-only preview: no annotation editing or text selection.
                .annotationEditingEnabled(false)
                .textSelectionEnabled(false)
                // Lock zoom to the fit scale so the preview can't be zoomed (incl. double tap).
                .maxZoomScale(1.0f)
                .build()

        val documentState = rememberDocumentState(documentUri, pdfActivityConfiguration)
        val documentManager = getDefaultDocumentManager(
            documentListener = DocumentListener(
                onDocumentLoaded = {
                    // Draw via the compose DocumentConnection (synchronous) rather than the
                    // document's AnnotationProvider, whose addAnnotationToPage is suspend.
                    annotations.forEach { annotation ->
                        documentState.documentConnection.addAnnotationToPage(annotation, false)
                    }
                },
            ),
            // Block annotation selection entirely so annotations can't be tapped/moved in a preview.
            annotationListener = AnnotationListener(
                onPrepareAnnotationSelection = { _, _, _ -> false },
            ),
        )
        DocumentView(documentState, modifier = modifier, documentManager = documentManager)
    }
}
