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

package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.LayoutDirection
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.text.TextUtilsCompat
import com.instructure.annotations.CanvasPdfMenuGrouping
import com.instructure.pandautils.analytics.SCREEN_VIEW_PSPDFKIT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.student.R
import com.pspdfkit.document.processor.PdfProcessorTask
import com.pspdfkit.document.sharing.DefaultDocumentSharingController
import com.pspdfkit.document.sharing.DocumentSharingIntentHelper
import com.pspdfkit.document.sharing.DocumentSharingManager
import com.pspdfkit.document.sharing.SharingOptions
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.ContextualToolbar
import com.pspdfkit.ui.toolbar.ContextualToolbarMenuItem
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import java.util.*

@ScreenView(SCREEN_VIEW_PSPDFKIT)
class CandroidPSPDFActivity : PdfActivity(), ToolbarCoordinatorLayout.OnContextualToolbarLifecycleListener {
    override fun onDisplayContextualToolbar(p0: ContextualToolbar<*>) {}
    override fun onRemoveContextualToolbar(p0: ContextualToolbar<*>) {}

    private var menuItems: List<ContextualToolbarMenuItem>? = null

    private val submissionTarget by lazy { intent?.extras?.getParcelable<ShareFileSubmissionTarget>(Const.SUBMISSION_TARGET) }

    override fun onPrepareContextualToolbar(toolbar: ContextualToolbar<*>) {
        if(toolbar is AnnotationCreationToolbar) {
            toolbar.setMenuItemGroupingRule(CanvasPdfMenuGrouping(this))
            toolbar.layoutParams = ToolbarCoordinatorLayout.LayoutParams(
                    ToolbarCoordinatorLayout.LayoutParams.Position.TOP, EnumSet.of(ToolbarCoordinatorLayout.LayoutParams.Position.TOP))
        }

        // We need to reverse the order of the menu items for RTL, once PSPDFKit supports RTL this can be deleted
        if (menuItems == null) {
                menuItems = toolbar.menuItems
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                for (item in menuItems!!) {
                    if (item.position == ContextualToolbarMenuItem.Position.START) {
                        item.position = ContextualToolbarMenuItem.Position.END
                    } else {
                        item.position = ContextualToolbarMenuItem.Position.START
                    }
                }
                if(toolbar.closeButton.position == ContextualToolbarMenuItem.Position.START) {
                    toolbar.closeButton.position = ContextualToolbarMenuItem.Position.END
                } else {
                    toolbar.closeButton.position = ContextualToolbarMenuItem.Position.START
                }
            }
        } else {
            toolbar.menuItems = menuItems as List<ContextualToolbarMenuItem>
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnContextualToolbarLifecycleListener(this)

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        @ColorInt val color = typedValue.data
        ViewStyler.setStatusBarDark(this, color)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menuInflater.inflate(R.menu.pspdf_activity_menu, menu)
        if (submissionTarget != null) {
            // If targeted for submission, change the menu item title from "Upload to Canvas" to "Submit Assignment"
            val item = menu.findItem(R.id.upload_item)
            item.title = getString(R.string.submitAssignment)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return if (item.itemId == R.id.upload_item) {
            uploadDocumentToCanvas()
            true
        } else {
            false
        }
    }

    private fun uploadDocumentToCanvas() {
        if (document != null) {
            DocumentSharingManager.shareDocument(
                    CandroidDocumentSharingController(this, submissionTarget),
                    document!!,
                    SharingOptions(PdfProcessorTask.AnnotationProcessingMode.FLATTEN))
        }
    }


    private inner class CandroidDocumentSharingController(
        private val mContext: Context,
        private val submissionTarget: ShareFileSubmissionTarget?
    ) : DefaultDocumentSharingController(mContext) {

        override fun onDocumentPrepared(shareUri: Uri) {
            val intent = Intent(mContext, ShareFileUploadActivity::class.java)
            intent.type = DocumentSharingIntentHelper.MIME_TYPE_PDF
            intent.putExtra(Intent.EXTRA_STREAM, shareUri)
            intent.putExtra(Const.SUBMISSION_TARGET, submissionTarget)
            intent.action = Intent.ACTION_SEND
            mContext.startActivity(intent)
        }
    }
}
