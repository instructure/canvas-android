/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.compose.composables.filedetails

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ExoAgent
import com.instructure.pandautils.utils.ExoAgentState
import com.instructure.pandautils.utils.ExoInfoListener
import com.instructure.pandautils.utils.onClick

@UnstableApi
@Composable
fun MediaFileContent(mediaUrl: String, contentType: String, onFullScreenClicked: (Uri, String) -> Unit, modifier: Modifier = Modifier) {
    MediaFileContent(mediaUrl.toUri(), contentType, onFullScreenClicked, modifier)
}

@UnstableApi
@Composable
fun MediaFileContent(uri: Uri, contentType: String, onFullScreenClicked: (Uri, String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoAgent = remember(uri) { ExoAgent.getAgentForUri(uri) }
    var playerViewInstance: PlayerView? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        key1 = exoAgent,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        playerViewInstance?.let { exoAgent.prepare(it) }
                    }

                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                exoAgent.release()
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    AndroidView(
        factory = {
            PlayerView(context).apply {
                playerViewInstance = this
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                controllerAutoShow = true
                setBackgroundColor(context.getColor(R.color.black))

                findViewById<View>(R.id.fullscreenButton).onClick {
                    exoAgent.flagForResume()
                    onFullScreenClicked(uri, contentType)
                }

                exoAgent.attach(this, object : ExoInfoListener {
                    override fun onStateChanged(newState: ExoAgentState) = Unit

                    override fun onError(cause: Throwable?) = Unit

                    override fun setAudioOnly() = Unit
                })

                exoAgent.prepare(this)
            }
        },
        update = { _ -> },
        modifier = modifier
    )
}

@UnstableApi
@Composable
@Preview
fun MediaFileContentPreview() {
    MediaFileContent(
        mediaUrl = "https://www.example.com/media.mp4",
        contentType = "video/mp4",
        onFullScreenClicked = { _, _ -> }
    )
}