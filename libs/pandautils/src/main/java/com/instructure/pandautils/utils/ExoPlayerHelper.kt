/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.net.Uri
import android.view.SurfaceView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper

enum class ExoAgentState {
    IDLE,
    PREPARING,
    BUFFERING,
    READY,
    ENDED
}

interface ExoInfoListener {
    fun onStateChanged(newState: ExoAgentState)
    fun onError(cause: Throwable?)
    fun setAudioOnly()
}

/**
 * Wraps an ExoPlayer instance and decouples playback management from the client view. Can be
 * detached from one client and reattached to another client, which is useful for transitioning
 * playback between different fragments/activities (e.g. switching from a player embedded in a
 * fragment to a player in a fullscreen activity)
 */
class ExoAgent private constructor(val uri: Uri) {

    /** Current ExoPlayer */
    private var mPlayer: ExoPlayer? = null

    /** Client's state/info listener */
    private var mInfoListener: ExoInfoListener? = null

    /** Whether this Agent should immediately resume playback when attached to a new client */
    private var mFlaggedForResume = false

    /** Whether the media track is audio only, or false if unknown */
    private var mIsAudioOnly = false

    /** The current state of this agent */
    private var currentState = ExoAgentState.IDLE
        set(value) {
            mInfoListener?.onStateChanged(value)
        }

    /** The media source that will feed data from the [uri] */
    private val mMediaSource by lazy {
        val mediaItem = MediaItem.fromUri(uri)
        when (Util.inferContentType(uri.lastPathSegment ?: "")) {
            C.TYPE_SS -> SsMediaSource.Factory(DefaultSsChunkSource.Factory(DATA_SOURCE_FACTORY), DATA_SOURCE_FACTORY).createMediaSource(mediaItem)
            C.TYPE_DASH -> DashMediaSource.Factory(DefaultDashChunkSource.Factory(DATA_SOURCE_FACTORY), DATA_SOURCE_FACTORY).createMediaSource(mediaItem)
            C.TYPE_HLS -> HlsMediaSource.Factory(DefaultHlsDataSourceFactory(DATA_SOURCE_FACTORY)).createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(DATA_SOURCE_FACTORY, DefaultExtractorsFactory()).createMediaSource(mediaItem)
        }
    }

    /**
     * Attaches a new client view to this agent. Any previous client will be detached. The passed
     * [ExoInfoListener] will be immediately called with the current state of this agent.
     * NOTE: This function MUST be called before [prepare] is called by the same client.
     */
    fun attach(playerView: PlayerView, listener: ExoInfoListener) {
        mInfoListener = listener
        mInfoListener?.onStateChanged(currentState)
        if (mIsAudioOnly) mInfoListener?.setAudioOnly()
        mPlayer?.switchSurface(playerView)
        resumeIfFlagged()
    }

    /**
     * Prepares the media and begins playback once prepared.
     * NOTE: The client MUST call [attach] prior to calling this function.
     */
    fun prepare(playerView: PlayerView) {
        if (mPlayer == null) preparePlayer()
        mPlayer?.switchSurface(playerView)
    }

    private fun ExoPlayer.switchSurface(playerView: PlayerView) {
        // Detach from current surface
        clearVideoSurface()

        // Attach to new surface
        setVideoSurfaceView(playerView.videoSurfaceView as SurfaceView)

        /* Seek to the current position + 1 to work around an issue where frames aren't
        rendered into the new surface until the next keyframe. This isn't ideal as it triggers a
        brief buffer cycle, but it's better than having no video at all. */
        if (!mIsAudioOnly) seekTo(currentPosition + 1)

        playerView.player = this
    }

    private fun preparePlayer() {
        currentState = ExoAgentState.PREPARING

        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector: TrackSelector = DefaultTrackSelector(ContextKeeper.appContext, trackSelectionFactory)
        mPlayer = ExoPlayer.Builder(ContextKeeper.appContext)
            .setTrackSelector(trackSelector)
            .build()

        mPlayer?.addListener(object : Player.Listener {
            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onSeekProcessed() {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
                mIsAudioOnly = trackSelections.all.none { (it as? ExoTrackSelection)?.selectedFormat?.sampleMimeType?.startsWith("video") == true }
                if (mIsAudioOnly) mInfoListener?.setAudioOnly()
            }

            override fun onPlayerError(exception: PlaybackException) {
                reset()
                mInfoListener?.onError(exception.cause)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, state: Int) {
                currentState = when (state) {
                    Player.STATE_BUFFERING -> ExoAgentState.BUFFERING
                    Player.STATE_ENDED -> {
                        mPlayer?.seekToDefaultPosition()
                        mPlayer?.stop()
                        mPlayer?.release()
                        ExoAgentState.ENDED
                    }
                    Player.STATE_IDLE -> ExoAgentState.IDLE
                    else -> ExoAgentState.READY
                }

            }
        })

        mPlayer?.playWhenReady = true
        mPlayer?.setMediaSource(mMediaSource)
        mPlayer?.prepare()
    }

    /** Resets the internal player but keeps this agent alive for reuse. */
    fun reset() {
        mPlayer?.release()
        mPlayer = null
    }

    /**
     * Releases this agent from life and resets the internal player.
     * This should only be called when we're confident that playback is no longer desired
     * for this Agent's [uri].
     */
    fun release() {
        reset()
        agentInstances.remove(uri.toString())
    }

    /**
     * Calling this before switching client views will cause this agent to automatically
     * resume playback after successfully attaching the new client.
     */
    fun flagForResume() {
        mPlayer?.let {
            mFlaggedForResume = it.playWhenReady
            it.playWhenReady = false
        }
    }

    private fun resumeIfFlagged() {
        mPlayer?.let { it.playWhenReady = mFlaggedForResume }
    }

    companion object {

        private const val CONNECT_TIMEOUT = 8000

        private const val READ_TIMEOUT = 8000

        private val BANDWIDTH_METER by lazy { DefaultBandwidthMeter.Builder(ContextKeeper.appContext).build() }

        private val DATA_SOURCE_FACTORY by lazy {
            val httpSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(ApiPrefs.userAgent)
                .setTransferListener(BANDWIDTH_METER)
                .setConnectTimeoutMs(CONNECT_TIMEOUT)
                .setReadTimeoutMs(READ_TIMEOUT)
                .setAllowCrossProtocolRedirects(true)
            DefaultDataSource.Factory(ContextKeeper.appContext, httpSourceFactory)
                .setTransferListener(BANDWIDTH_METER)
        }

        private var agentInstances: HashMap<String, ExoAgent> = hashMapOf()

        /** Creates or retrieves an agent associated with the specified Uri */
        fun getAgentForUri(uri: Uri): ExoAgent {
            return agentInstances.getOrPut(uri.toString()) { ExoAgent(uri) }
        }

        /** Kills all current agents */
        fun releaseAllAgents() {
            agentInstances.values.forEach { it.release() }
        }
    }
}
