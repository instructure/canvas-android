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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedGraderMediaBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.view.MediaContent

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
class ViewMediaFragment : Fragment(), ShareableFile {

    private val binding by viewBinding(FragmentSpeedGraderMediaBinding::bind)

    private var mUri by ParcelableArg(Uri.EMPTY)
    private var mContentType by StringArg()
    private var mThumbnailUrl by NullableStringArg()
    private var mDisplayName by NullableStringArg()
    private var isInModulesPager by BooleanArg()
    private var toolbarColor by IntArg()

    private val mExoAgent get() = ExoAgent.getAgentForUri(mUri)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_speed_grader_media, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.speedGraderMediaPlayerView.findViewById<Toolbar>(R.id.toolbar).setGone()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() = with(binding) {
        if (isInModulesPager) {
            toolbar.title = mDisplayName
            toolbar.setupBackButton { requireActivity().onBackPressed() }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, requireContext().getColor(R.color.white))
        } else {
            toolbar.setGone()
        }
    }

    override fun onStart() = with(binding) {
        super.onStart()

        Glide.with(requireContext()).load(mThumbnailUrl).into(mediaThumbnailView)

        prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(
                    manager = requireActivity().supportFragmentManager,
                    onProceed = this@ViewMediaFragment::prepare
            )
        }

        ViewStyler.themeButton(tryAgainButton)
        tryAgainButton.onClick { prepare() }

        ViewStyler.themeButton(openExternallyButton)
        openExternallyButton.onClick { mUri.viewExternally(requireContext(), mContentType) }

        speedGraderMediaPlayerView.findViewById<ImageButton>(R.id.fullscreenButton).onClick {
            mExoAgent.flagForResume()
            val bundle = BaseViewMediaActivity.makeBundle(mUri.toString(), mThumbnailUrl, mContentType, mDisplayName, false)
            RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.MEDIA))
        }
    }

    override fun viewExternally() {
        mUri.viewExternally(requireContext(), mContentType)
    }

    override fun onResume() = with(binding) {
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

    private fun prepare() = mExoAgent.prepare(binding.speedGraderMediaPlayerView)

    override fun onDetach() {
        mExoAgent.release()
        super.onDetach()
    }

    companion object {

        fun newInstance(media: MediaContent) = newInstance(media.uri, media.thumbnailUrl, media.contentType!!, media.displayName)

        fun newInstance(
            uri: Uri,
            thumbnailUrl: String?,
            contentType: String,
            displayName: String?,
            isInModulesPager: Boolean = false,
            toolbarColor: Int = 0
        ) = ViewMediaFragment().apply {
            mUri = uri
            mThumbnailUrl = thumbnailUrl
            mContentType = contentType
            mDisplayName = displayName
            this.isInModulesPager = isInModulesPager
            this.toolbarColor = toolbarColor
        }

        fun newInstance(bundle: Bundle) = ViewMediaFragment().apply { arguments = bundle }
    }
}
