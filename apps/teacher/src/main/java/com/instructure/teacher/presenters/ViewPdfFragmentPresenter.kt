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
package com.instructure.teacher.presenters

import android.net.Uri
import com.instructure.annotations.FileCaching.FileCache
import com.instructure.annotations.awaitFileDownload
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.ViewPdfFragmentView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job
import java.io.File

class ViewPdfFragmentPresenter(val pdfUrl: String) : FragmentPresenter<ViewPdfFragmentView>() {

    private var job: Job? = null
    private var localPdfUri: Uri? = null

    override fun loadData(forceNetwork: Boolean) {
        localPdfUri?.let { viewCallback?.onLoadingFinished(it); return }
        viewCallback?.onLoadingStarted()
        if (job?.isActive ?: false) return
        job = weave {
            val tempFile: File? = FileCache.awaitFileDownload(pdfUrl) {
                onUI { viewCallback?.onLoadingProgress(it) }
            }
            viewCallback?.let {
                if (tempFile != null) it.onLoadingFinished(Uri.fromFile(tempFile)) else it.onLoadingError()
            }
        }
    }

    override fun refresh(forceNetwork: Boolean) = Unit

    override fun onDestroyed() {
        super.onDestroyed()
        job?.cancel()
    }

}
