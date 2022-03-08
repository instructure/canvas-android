/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.documentscanning

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.instructure.student.R
import com.instructure.student.databinding.ActivityDocumentScanningBinding
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_document_scanning.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DocumentScanningActivity : ScanActivity() {

    private val viewModel: DocumentScanningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityDocumentScanningBinding>(this, R.layout.activity_document_scanning)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        addFragmentContentLayout()

        setupToolbar()

        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: DocumentScanningAction) {
        when (action) {
            is DocumentScanningAction.SaveBitmapAction -> {
                val file = File(filesDir, "scanned_${SimpleDateFormat("yyyyMMddkkmmss", Locale.getDefault()).format(Date())}.jpg")
                var fileOutputStream: FileOutputStream? = null
                try {
                    fileOutputStream = FileOutputStream(file.absolutePath)
                    action.bitmap.compress(Bitmap.CompressFormat.JPEG, action.quality, fileOutputStream)
                    intent.data = file.toUri()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } finally {
                    fileOutputStream?.run {
                        flush()
                        close()
                    }
                }
            }
        }
    }

    override fun onClose() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onError(error: DocumentScannerErrorModel) {

    }

    override fun onSuccess(scannerResults: ScannerResults) {
        viewModel.setScannerResults(scannerResults)
    }

    private fun setupToolbar() {
        toolbar.apply {
            setTitle(R.string.documentScanningTitle)
            navigationIcon = ContextCompat.getDrawable(this@DocumentScanningActivity, R.drawable.ic_back_arrow)
            navigationIcon?.isAutoMirrored = true
            setNavigationContentDescription(R.string.close)
            setNavigationOnClickListener { onClose() }
        }
    }
}