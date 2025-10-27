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
import androidx.annotation.OptIn
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import com.bumptech.glide.Glide
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_MEDIA
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.pandautils.features.speedgrader.content.MediaContent
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ExoAgent
import com.instructure.pandautils.utils.ExoAgentState
import com.instructure.pandautils.utils.ExoInfoListener
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.viewExternally
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedGraderMediaBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_VIEW_MEDIA)
class ViewMediaFragment : BaseCanvasFragment(), ShareableFile {

    private val binding by viewBinding(FragmentSpeedGraderMediaBinding::bind)

    private var uri by ParcelableArg(Uri.EMPTY)
    private var contentType by StringArg()
    private var thumbnailUrl by NullableStringArg()
    private var displayName by NullableStringArg()
    private var isInModulesPager by BooleanArg()
    private var toolbarColor by IntArg()
    private var editableFile: EditableFile? by NullableParcelableArg()
    private var mediaUri: Uri? = null

    private val exoAgent get() = ExoAgent.getAgentForUri(mediaUri ?: uri)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_speed_grader_media, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.root.applyHorizontalSystemBarInsets()
        binding.speedGraderMediaPlayerView.findViewById<Toolbar>(R.id.toolbar).setGone()
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        if (isInModulesPager) {
            toolbar.applyTopSystemBarInsets()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchMediaUri()
    }

    override fun onStart() = with(binding) {
        super.onStart()

        Glide.with(requireContext()).load(thumbnailUrl).into(mediaThumbnailView)

        prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(
                    manager = requireActivity().supportFragmentManager,
                    onProceed = this@ViewMediaFragment::prepare
            )
        }

        ViewStyler.themeButton(tryAgainButton)
        tryAgainButton.onClick { prepare() }

        ViewStyler.themeButton(openExternallyButton)
        openExternallyButton.onClick { uri.viewExternally(requireContext(), contentType) }

        speedGraderMediaPlayerView.findViewById<ImageButton>(R.id.fullscreenButton).onClick {
            exoAgent.flagForResume()
            val bundle = BaseViewMediaActivity.makeBundle(uri.toString(), thumbnailUrl, contentType, displayName, false)
            RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.MEDIA))
        }
    }

    override fun viewExternally() {
        uri.viewExternally(requireContext(), contentType)
    }

    override fun onResume()  {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null && fileFolderDeletedEvent.deletedFileFolder.id == editableFile?.file?.id) {
            requireActivity().finish()
        }

        setupToolbar()

        if (mediaUri != null) {
            attachMediaPlayer()
        }
    }

    private fun fetchMediaUri() {
        lifecycleScope.launch {
            mediaUri = RouteUtils.getMediaUri(uri)
            if (isResumed) {
                attachMediaPlayer()
            }
        }
    }

    private fun attachMediaPlayer() = with(binding){
        exoAgent.attach(speedGraderMediaPlayerView, object : ExoInfoListener {
            override fun onStateChanged(newState: ExoAgentState) {
                when (newState) {
                    ExoAgentState.IDLE -> {
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setGone()
                        prepareMediaButton.setVisible()
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
                        exoAgent.reset()
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        speedGraderMediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                }
            }

            @OptIn(UnstableApi::class) override fun onError(cause: Throwable?) {
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

    private fun prepare() = exoAgent.prepare(binding.speedGraderMediaPlayerView)

    private fun setupToolbar() = with(binding) {
        editableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                if (it.file.id == event.updatedFileFolder.id) {
                    it.file = event.updatedFileFolder
                }
            }

            toolbar.title = it.file.displayName
            toolbar.setupMenu(R.menu.menu_file_details) { menu ->
                when (menu.itemId) {
                    R.id.edit -> {
                        val args = EditFileFolderFragment.makeBundle(it.file, it.usageRights, it.licenses, it.canvasContext!!.id)
                        RouteMatcher.route(requireActivity(), Route(EditFileFolderFragment::class.java, it.canvasContext, args))
                    }

                    R.id.copyLink -> {
                        if (it.file.url != null) {
                            Utils.copyToClipboard(requireContext(), it.file.url!!)
                        }
                    }
                }
            }
        }

        if (isInModulesPager) {
            toolbar.setVisible()
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@ViewMediaFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@ViewMediaFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, requireContext().getColor(R.color.textLightest))
        }
    }

    override fun onDetach() {
        exoAgent.release()
        super.onDetach()
    }

    fun pauseVideo() {
        binding.speedGraderMediaPlayerView.player?.pause()
    }

    companion object {

        fun newInstance(media: MediaContent) = newInstance(media.uri, media.thumbnailUrl, media.contentType!!, media.displayName)

        fun newInstance(
            uri: Uri,
            thumbnailUrl: String?,
            contentType: String,
            displayName: String?,
            isInModulesPager: Boolean = false,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null
        ) = ViewMediaFragment().apply {
            this.uri = uri
            this.thumbnailUrl = thumbnailUrl
            this.contentType = contentType
            this.displayName = displayName
            this.isInModulesPager = isInModulesPager
            this.toolbarColor = toolbarColor
            this.editableFile = editableFile
        }

        fun newInstance(bundle: Bundle) = ViewMediaFragment().apply { arguments = bundle }

        fun createBundle(media: MediaContent) = Bundle().apply {
            putParcelable("uri", media.uri)
            putString("thumbnailUrl", media.thumbnailUrl)
            putString("contentType", media.contentType)
            putString("displayName", media.displayName)
        }
    }
}
