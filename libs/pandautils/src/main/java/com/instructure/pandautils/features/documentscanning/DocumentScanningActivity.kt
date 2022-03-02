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

package com.instructure.pandautils.features.documentscanning

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ActivityDocumentScanningBinding
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_document_scanning.*

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