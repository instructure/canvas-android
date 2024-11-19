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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import com.bumptech.glide.Glide
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.pandautils.utils.ExoAgent
import com.instructure.pandautils.utils.ExoAgentState
import com.instructure.pandautils.utils.ExoInfoListener
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.viewExternally
import com.instructure.student.R
import com.instructure.student.databinding.FragmentMediaSubmissionViewBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.router.RouteMatcher

@OptIn(UnstableApi::class)
class MediaSubmissionViewFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentMediaSubmissionViewBinding::bind)

    private var uri by ParcelableArg(Uri.EMPTY)
    private var contentType by StringArg()
    private var thumbnailUrl by NullableStringArg()
    private var displayName by NullableStringArg()

    private val exoAgent get() = ExoAgent.getAgentForUri(uri)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_media_submission_view, container, false)

    override fun onStart() = with(binding) {
        super.onStart()

        Glide.with(requireContext()).load(thumbnailUrl).into(mediaThumbnailView)

        prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(
                manager = requireActivity().supportFragmentManager,
                onProceed = this@MediaSubmissionViewFragment::prepare
            )
        }

        ViewStyler.themeButton(tryAgainButton)
        tryAgainButton.onClick { prepare() }

        ViewStyler.themeButton(openExternallyButton)
        openExternallyButton.onClick { uri.viewExternally(requireContext(), contentType) }

        submissionMediaPlayerView.findViewById<View>(R.id.fullscreenButton).onClick {
            exoAgent.flagForResume()
            val bundle = BaseViewMediaActivity.makeBundle(uri.toString(), thumbnailUrl, contentType, displayName, false)
            RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.MEDIA))
        }
    }

    override fun onResume() {
        super.onResume()

        exoAgent.attach(binding.submissionMediaPlayerView, object : ExoInfoListener {
            override fun onStateChanged(newState: ExoAgentState) {
                when (newState) {
                    ExoAgentState.IDLE -> {
                        binding.mediaPreviewContainer.setVisible()
                        binding.mediaPlaybackErrorView.setGone()
                        binding.submissionMediaPlayerView.setGone()
                        binding.mediaProgressBar.setGone()
                    }
                    ExoAgentState.PREPARING,
                    ExoAgentState.BUFFERING -> {
                        binding.mediaPreviewContainer.setGone()
                        binding.mediaPlaybackErrorView.setGone()
                        binding.submissionMediaPlayerView.setVisible()
                        binding.mediaProgressBar.announceForAccessibility(getString(R.string.loading))
                        binding.mediaProgressBar.setVisible()
                    }
                    ExoAgentState.READY -> {
                        binding.mediaPreviewContainer.setGone()
                        binding.mediaPlaybackErrorView.setGone()
                        binding.submissionMediaPlayerView.setVisible()
                        binding.mediaProgressBar.setGone()
                    }
                    ExoAgentState.ENDED -> {
                        exoAgent.reset()
                        binding.mediaPreviewContainer.setVisible()
                        binding.mediaPlaybackErrorView.setGone()
                        binding.submissionMediaPlayerView.setGone()
                        binding.mediaProgressBar.setGone()
                    }
                }
            }

            override fun onError(cause: Throwable?) {
                binding.submissionMediaPlayerView.setGone()
                binding.mediaProgressBar.setGone()
                binding.mediaPlaybackErrorView.setVisible()
                binding.errorTextView.setText(when (cause) {
                    is HttpDataSource.HttpDataSourceException -> R.string.no_data_connection
                    is UnrecognizedInputFormatException -> R.string.utils_couldNotPlayFormat
                    else -> R.string.errorOccurred
                })
                val isUnrecognizedFormat = cause is UnrecognizedInputFormatException
                binding.openExternallyButton.setVisible(isUnrecognizedFormat)
                binding.tryAgainButton.setVisible(!isUnrecognizedFormat)
            }

            override fun setAudioOnly() {
                binding.audioIconView.setVisible()
            }
        })
    }

    private fun prepare() {
        exoAgent.prepare(binding.submissionMediaPlayerView)
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
