/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
package com.instructure.pandautils.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import com.bumptech.glide.Glide
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.ActivityViewMediaBinding
import com.instructure.pandautils.dialogs.MobileDataWarningDialog
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.ExoAgent
import com.instructure.pandautils.utils.ExoAgentState
import com.instructure.pandautils.utils.ExoInfoListener
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.pandautils.utils.viewExternally
import org.greenrobot.eventbus.EventBus
import java.io.File

abstract class BaseViewMediaActivity : BaseCanvasActivity() {

    abstract fun allowEditing(): Boolean
    abstract fun handleEditing(editableFile: EditableFile)
    abstract fun allowCopyingUrl() :Boolean

    private val binding by viewBinding(ActivityViewMediaBinding::inflate)

    private val mUri: Uri by lazy { intent?.extras?.getParcelable(URI) ?: Uri.EMPTY }
    private val mContentType: String by lazy { intent?.extras?.getString(CONTENT_TYPE).orEmpty() }
    private val mThumbnailUrl: String? by lazy { intent?.extras?.getString(THUMB_URL) }
    private val mDisplayName: String? by lazy { intent?.extras?.getString(DISPLAY_NAME) }
    private val mDestroyOnExit: Boolean by lazy { intent?.extras?.getBoolean(DESTROY_ON_EXIT, true) ?: true }
    private val mEditableFile: EditableFile? by lazy { intent?.extras?.getParcelable<EditableFile>(EDITABLE_FILE) }
    private var mediaUri: Uri? = null

    private val mExoAgent by lazy { ExoAgent.getAgentForUri(mediaUri ?: mUri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.mediaPlayerView.findViewById<ImageButton>(R.id.fullscreenButton).setGone()
        fetchMediaUri()
    }

    private fun fetchMediaUri() {
        with(binding) {
            mediaPreviewContainer.setGone()
            mediaPlaybackErrorView.setGone()
            mediaPlayerView.setVisible()
            mediaProgressBar.announceForAccessibility(getString(R.string.loading))
            mediaProgressBar.setVisible()
        }
        mediaUri = mUri
        attachMediaPlayer()
    }

    private fun attachMediaPlayer() = with(binding) {
        mExoAgent.attach(mediaPlayerView, object : ExoInfoListener {
            override fun onStateChanged(newState: ExoAgentState) {
                when (newState) {
                    ExoAgentState.IDLE -> {
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        mediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }

                    ExoAgentState.PREPARING,
                    ExoAgentState.BUFFERING -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        mediaPlayerView.setVisible()
                        mediaProgressBar.announceForAccessibility(getString(R.string.loading))
                        mediaProgressBar.setVisible()
                    }

                    ExoAgentState.READY -> {
                        mediaPreviewContainer.setGone()
                        mediaPlaybackErrorView.setGone()
                        mediaPlayerView.setVisible()
                        mediaProgressBar.setGone()
                    }

                    ExoAgentState.ENDED -> {
                        mExoAgent.reset()
                        mediaPreviewContainer.setVisible()
                        mediaPlaybackErrorView.setGone()
                        mediaPlayerView.setGone()
                        mediaProgressBar.setGone()
                    }
                }
            }

            @OptIn(UnstableApi::class)
            override fun onError(cause: Throwable?) {
                mediaPlayerView.setGone()
                mediaProgressBar.setGone()
                mediaPlaybackErrorView.setVisible()
                errorTextView.setText(
                    when (cause) {
                        is HttpDataSource.HttpDataSourceException -> R.string.utils_no_data_connection
                        is UnrecognizedInputFormatException -> R.string.utils_couldNotPlayFormat
                        else -> R.string.errorOccurred
                    }
                )
                val isUnrecognizedFormat = cause is UnrecognizedInputFormatException
                openExternallyButton.setVisible(isUnrecognizedFormat)
                tryAgainButton.setVisible(!isUnrecognizedFormat)
            }

            override fun setAudioOnly() {
                audioIconView.setVisible()
            }

        })
        updateImmersivePadding()

        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = binding.mediaPlayerView.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = mDisplayName

        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)

            fileFolderUpdatedEvent?.let {
                //update the toolbar title if we've updated the file name by editing it
                toolbar.title = it.updatedFileFolder.displayName
            }
            if(allowEditing() && !allowCopyingUrl()) {
                toolbar.setMenu(R.menu.utils_menu_edit_generic) { _ ->
                    mExoAgent.flagForResume()
                    handleEditing(it)
                }
            } else if(allowEditing() && allowCopyingUrl()) {
                toolbar.setMenu(R.menu.utils_menu_file_options) { menu ->
                    when(menu.itemId) {
                        R.id.menu_edit -> {
                            mExoAgent.flagForResume()
                            handleEditing(it)
                        }
                        R.id.menu_copy -> {
                            if(it.file.url != null) {
                                Utils.copyToClipboard(this, it.file.url)
                            }
                        }
                    }
                }
            }
        }
        toolbar.setupAsCloseButton { onBackPressed() }
        ViewStyler.setToolbarElevationSmall(this, toolbar)
        ViewStyler.themeToolbarColored(this, toolbar, ContextCompat.getColor(this, R.color.translucentBlack), getColor(R.color.white))
    }

    override fun onStart() {
        super.onStart()
        binding.mediaPlayerView.findViewById<ImageButton>(R.id.fullscreenButton).setGone()
        Glide.with(this).load(mThumbnailUrl).into(binding.mediaThumbnailView)
        binding.prepareMediaButton.onClick {
            MobileDataWarningDialog.showIfNeeded(manager = supportFragmentManager, onProceed = this::prepare)
        }
        ViewStyler.themeButton(binding.tryAgainButton)
        binding.tryAgainButton.onClick { prepare() }
        binding.openExternallyButton.onClick { (mediaUri ?: mUri).viewExternally(this, mContentType) }
    }

    override fun onResume() {
        super.onResume()

        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null)
            finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateImmersivePadding()
    }

    private fun updateImmersivePadding() {
        val toolbar = binding.mediaPlayerView.findViewById<Toolbar>(R.id.toolbar)
        val fullWindowFrame = binding.mediaPlayerView.findViewById<FrameLayout>(R.id.fullWindowFrame)
        val controlsContainer = binding.mediaPlayerView.findViewById<LinearLayout>(R.id.controlsContainer)

        fullWindowFrame.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (fullWindowFrame.paddingTop == 0 && fullWindowFrame.paddingBottom == 0) return
                toolbar.setPadding(toolbar.paddingLeft, fullWindowFrame.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
                controlsContainer.setPadding(controlsContainer.paddingLeft, controlsContainer.paddingTop, controlsContainer.paddingRight, fullWindowFrame.paddingBottom)
                fullWindowFrame.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun prepare() = mExoAgent.prepare(binding.mediaPlayerView)

    override fun onPause() {
        mExoAgent.flagForResume()
        super.onPause()
    }

    override fun onDestroy() {
        if (mDestroyOnExit) mExoAgent.release()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    companion object {
        private const val URI = "uri"
        private const val THUMB_URL = "thumb_url"
        private const val CONTENT_TYPE = "content_type"
        private const val DISPLAY_NAME = "display_name"
        private const val DESTROY_ON_EXIT = "destroy_on_eit"
        private const val EDITABLE_FILE = "editable_file"

        @JvmOverloads
        fun makeBundle(
                url: String,
                thumbnailUrl: String?,
                contentType: String,
                displayName: String?,
                destroyOnExit: Boolean = false,
                editableFile: EditableFile? = null
        ) = Bundle().apply {
            putParcelable(URI, Uri.parse(url))
            putString(THUMB_URL, thumbnailUrl)
            putString(CONTENT_TYPE, contentType)
            putString(DISPLAY_NAME, displayName)
            putBoolean(DESTROY_ON_EXIT, destroyOnExit)
            putParcelable(EDITABLE_FILE, editableFile)
        }

        fun makeBundle(
                file: File?,
                contentType: String,
                displayName: String?,
                destroyOnExit: Boolean = false
        ) = Bundle().apply {
            putParcelable(URI, Uri.fromFile(file))
            putString(CONTENT_TYPE, contentType)
            putString(DISPLAY_NAME, displayName)
            putBoolean(DESTROY_ON_EXIT, destroyOnExit)
        }

        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, BaseViewMediaActivity::class.java)
            intent.putExtras(route.arguments)
            return intent
        }
    }
}
