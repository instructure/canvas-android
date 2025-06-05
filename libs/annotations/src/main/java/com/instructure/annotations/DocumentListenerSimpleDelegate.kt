/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.annotations

import android.graphics.PointF
import android.view.MotionEvent
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.document.DocumentSaveOptions
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.listeners.DocumentListener

/**
 * Class delegate that gives us default implementations for everything, since
 * we're only using a few of these.
 *
 * Override specific methods in delegate
 */
class DocumentListenerSimpleDelegate : DocumentListener {
    override fun onPageUpdated(p0: PdfDocument, p1: Int) = Unit
    override fun onDocumentSaveCancelled(p0: PdfDocument?) = Unit
    override fun onPageChanged(p0: PdfDocument, p1: Int) = Unit
    override fun onDocumentSaved(p0: PdfDocument) = Unit
    override fun onDocumentZoomed(p0: PdfDocument, p1: Int, p2: Float) = Unit
    override fun onPageClick(p0: PdfDocument, p1: Int, p2: MotionEvent?, p3: PointF?, p4: Annotation?) = false
    override fun onDocumentSave(p0: PdfDocument, p1: DocumentSaveOptions) = true
    override fun onDocumentLoaded(p0: PdfDocument) = Unit
    override fun onDocumentLoadFailed(p0: Throwable) = Unit
    override fun onDocumentClick(): Boolean = false
    override fun onDocumentSaveFailed(p0: PdfDocument, p1: Throwable) = Unit
}