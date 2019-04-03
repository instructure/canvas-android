/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.student.R
import com.instructure.student.util.FileDownloadJobIntentService
import com.instructure.student.util.LoggingUtility
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.fragment_submission_view_online_url.*

class SubmissionViewOnlineURLFragment : ParentFragment(), Bookmarkable {

    private var submission: Submission by ParcelableArg(key = Const.SUBMISSION)

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override fun title(): String = getString(R.string.urlSubmission)

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_submission_view_online_url, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingLayout.setGone()
        setupPopupMenu()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        urlButton.text = "${getString(R.string.visitPage)} ${submission.url}"
        urlButton.onClick {
            InternalWebviewFragment.loadInternalWebView(
                activity,
                InternalWebviewFragment.makeRoute(canvasContext, submission.url.orEmpty(), false)
            )
        }
        if (submission.attachments.size > 0) {
            loadImage()
        } else {
            // The image snapshot isn't there (yet, or possibly ever), change the description to match the web
            submittedImage.setText(R.string.urlSubmissionNoPreview)
        }
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeButton(urlButton)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    private fun setupPopupMenu() {
        previewImage.onLongClick {
            PopupMenu(requireContext(), previewImage).apply {
                menu.add(resources.getString(R.string.open)).setOnMenuItemClickListener {
                    with (submission.attachments[0]) { openMedia(contentType, url, displayName, canvasContext) }
                    true
                }
                menu.add(resources.getString(R.string.download)).setOnMenuItemClickListener {
                    if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                        downloadFile()
                    } else {
                        requestPermissions(
                            PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE),
                            PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE
                        )
                    }
                    true
                }
                show()
            }
            true
        }
    }

    private fun loadImage() {
        loadingLayout.setVisible()
        Glide.with(previewImage)
            .load(submission.attachments[0].url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    LoggingUtility.LogException(requireActivity(), e)
                    toast(R.string.errorLoadingFiles)
                    loadingLayout.setGone()
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    loadingLayout.setGone()
                    return false
                }
            })
            .into(previewImage)
    }

    private fun downloadFile() {
        FileDownloadJobIntentService.scheduleDownloadJob(requireContext(), null, submission.attachments[0])
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            }
        }
    }

    companion object {

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, submission: Submission): Route {
            val bundle = Bundle().apply { putParcelable(Const.SUBMISSION, submission) }
            return Route(null, SubmissionViewOnlineURLFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.getParcelable<Submission>(Const.SUBMISSION) != null
        }

        fun newInstance(route: Route): SubmissionViewOnlineURLFragment? {
            if (!validateRoute(route)) return null
            return SubmissionViewOnlineURLFragment().withArgs(route.argsWithContext)
        }

    }
}
