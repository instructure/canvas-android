/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.teacher.fragments

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
import com.instructure.teacher.R
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.view.MediaContent
import kotlinx.android.synthetic.main.exo_player_control_view.view.*
import kotlinx.android.synthetic.main.fragment_speed_grader_media.*

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
class ViewMediaFragment : Fragment(), ShareableFile {

    private var mUri by ParcelableArg(Uri.EMPTY)
    private var mContentType by StringArg()
    private var mThumbnailUrl by NullableStringArg()
    private var mDisplayName by NullableStringArg()

    private val mExoAgent get() = ExoAgent.getAgentForUri(mUri)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_speed_grader_media, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        speedGraderMediaPlayerView.toolbar.setGone()
    }

    override fun onStart() {
        super.onStart()

        Glide.with(requireContext()).load(mThumbnailUrl).into(mediaThumbnailView)

        prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(
                    manager = requireActivity().supportFragmentManager,
                    onProceed = this::prepare
            )
        }

        ViewStyler.themeButton(tryAgainButton)
        tryAgainButton.onClick { prepare() }

        ViewStyler.themeButton(openExternallyButton)
        openExternallyButton.onClick { mUri.viewExternally(requireContext(), mContentType) }

        speedGraderMediaPlayerView.fullscreenButton.onClick {
            mExoAgent.flagForResume()
            val bundle = BaseViewMediaActivity.makeBundle(mUri.toString(), mThumbnailUrl, mContentType, mDisplayName, false)
            RouteMatcher.route(requireContext(), Route(bundle, RouteContext.MEDIA))
        }
    }

    override fun viewExternally() {
        mUri.viewExternally(requireContext(), mContentType)
    }

    override fun onResume() {
        super.onResume()

        mExoAgent.attach(speedGraderMediaPlayerView, object : ExoInfoListener {
            override fun onStateChanged(newState: ExoAgentState) {
                when (newState) {
                    ExoAgentState.IDLE -> {
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                    ExoAgentState.PREPARING,
                    ExoAgentState.BUFFERING -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setVisible()
                        mediaProgressBar.announceForAccessibility(getString(R.string.loading))
                        mediaProgressBar.setVisible()
                    }
                    ExoAgentState.READY -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setVisible()
                        mediaProgressBar.setGone()
                    }
                    ExoAgentState.ENDED -> {
                        mExoAgent.reset()
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                }
            }

            override fun onError(cause: Throwable?) {
                speedGraderMediaPlayerView.setGone()
                mediaProgressBar.setGone()
                mediaPlaybackErrorView.setVisible()
                errorTextView.setText(when (cause) {
                    is HttpDataSource.HttpDataSourceException -> R.string.no_data_connection
                    is UnrecognizedInputFormatException -> R.string.couldNotPlayFormat
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

    private fun prepare() = mExoAgent.prepare(speedGraderMediaPlayerView)

    override fun onDetach() {
        mExoAgent.release()
        super.onDetach()
    }

    companion object {

        fun newInstance(media: MediaContent) = ViewMediaFragment().apply {
            mUri = media.uri
            mThumbnailUrl = media.thumbnailUrl
            mContentType = media.contentType!!
            mDisplayName = media.displayName
        }

        fun newInstance(bundle: Bundle) = ViewMediaFragment().apply { arguments = bundle }
    }
}
