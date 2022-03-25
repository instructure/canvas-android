/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.instructure.pandautils.utils.*
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.exo_player_control_view.view.*
import kotlinx.android.synthetic.main.fragment_media_submission_view.*

class MediaSubmissionViewFragment : Fragment() {

    private var uri by ParcelableArg(Uri.EMPTY)
    private var contentType by StringArg()
    private var thumbnailUrl by NullableStringArg()
    private var displayName by NullableStringArg()

    private val exoAgent get() = ExoAgent.getAgentForUri(uri)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_media_submission_view, container, false)

    override fun onStart() {
        super.onStart()

        Glide.with(requireContext()).load(thumbnailUrl).into(mediaThumbnailView)

        prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(
                manager = requireActivity().supportFragmentManager,
                onProceed = this::prepare
            )
        }

        ViewStyler.themeButton(tryAgainButton)
        tryAgainButton.onClick { prepare() }

        ViewStyler.themeButton(openExternallyButton)
        openExternallyButton.onClick { uri.viewExternally(requireContext(), contentType) }

        submissionMediaPlayerView.fullscreenButton.onClick {
            exoAgent.flagForResume()
            val bundle = BaseViewMediaActivity.makeBundle(uri.toString(), thumbnailUrl, contentType, displayName, false)
            RouteMatcher.route(requireContext(), Route(bundle, RouteContext.MEDIA))
        }
    }

    override fun onResume() {
        super.onResume()

        exoAgent.attach(submissionMediaPlayerView, object : ExoInfoListener {
            override fun onStateChanged(newState: ExoAgentState) {
                when (newState) {
                    ExoAgentState.IDLE -> {
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        submissionMediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                    ExoAgentState.PREPARING,
                    ExoAgentState.BUFFERING -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        submissionMediaPlayerView.setVisible()
                        mediaProgressBar.announceForAccessibility(getString(R.string.loading))
                        mediaProgressBar.setVisible()
                    }
                    ExoAgentState.READY -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        submissionMediaPlayerView.setVisible()
                        mediaProgressBar.setGone()
                    }
                    ExoAgentState.ENDED -> {
                        exoAgent.reset()
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        submissionMediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                }
            }

            override fun onError(cause: Throwable?) {
                submissionMediaPlayerView.setGone()
                mediaProgressBar.setGone()
                mediaPlaybackErrorView.setVisible()
                errorTextView.setText(when (cause) {
                    is HttpDataSource.HttpDataSourceException -> R.string.no_data_connection
                    is UnrecognizedInputFormatException -> R.string.utils_couldNotPlayFormat
                    else -> R.string.errorOccurred
                })
                val isUnrecognizedFormat = cause is UnrecognizedInputFormatException
                openExternallyButton.setVisible(isUnrecognizedFormat)
                tryAgainButton.setVisible(!isUnrecognizedFormat)
            }

            override fun setAudioOnly() {
                audioIconView.setVisible()
            }
        })
    }

    private fun prepare() {
        exoAgent.prepare(submissionMediaPlayerView)
    }

    override fun onDetach() {
        exoAgent.release()
        super.onDetach()
    }

    companion object {

        fun newInstance(media: SubmissionDetailsContentType.MediaContent) = MediaSubmissionViewFragment().apply {
            uri = media.uri
            thumbnailUrl = media.thumbnailUrl
            contentType = media.contentType!!
            displayName = media.displayName
        }
    }
}
