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
package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.hls.DefaultHlsDataSourceFactory
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.extractor.DefaultExtractorsFactory
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIDEO_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applySystemBarInsets
import com.instructure.student.databinding.ActivityVideoViewBinding
import com.instructure.student.util.Const
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@ScreenView(SCREEN_VIEW_VIDEO_VIEW)
class VideoViewActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityVideoViewBinding::inflate)

    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var mainHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.playerView.requestFocus()
        binding.playerView.applySystemBarInsets(bottom = true)
        mediaDataSourceFactory = buildDataSourceFactory(true)
        mainHandler = Handler()
        val videoTrackSelectionFactory: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(applicationContext, videoTrackSelectionFactory)
        fetchMediaUri(Uri.parse(intent?.extras?.getString(Const.URL)))
        ViewStyler.setStatusBarDark(this, ThemePrefs.primaryColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    private fun fetchMediaUri(uri: Uri) {
        lifecycleScope.launch {
            val mediaUri = RouteUtils.getMediaUri(uri)
            player = ExoPlayer.Builder(this@VideoViewActivity)
                .setTrackSelector(trackSelector)
                .setLoadControl(DefaultLoadControl())
                .build()
            binding.playerView.player = player
            player?.playWhenReady = true
            player?.setMediaSource(buildMediaSource(mediaUri))
            player?.prepare()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)
        return when (val type = Util.inferContentType(uri)) {
            C.CONTENT_TYPE_SS -> SsMediaSource.Factory(DefaultSsChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(mediaItem)
            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(DefaultDashChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(mediaItem)
            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(DefaultHlsDataSourceFactory(buildDataSourceFactory(false))).createMediaSource(mediaItem)
            C.CONTENT_TYPE_OTHER -> ProgressiveMediaSource.Factory(mediaDataSourceFactory, DefaultExtractorsFactory()).createMediaSource(mediaItem)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set DefaultBandwidthMeter as a listener to the new DataSource factory.
     * @return A new DataSource factory.
     */
    private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
        val meter = if (useBandwidthMeter) DefaultBandwidthMeter.Builder(this).build() else null
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("candroid")
            .setTransferListener(meter)
        return DefaultDataSource.Factory(this, httpDataSourceFactory)
            .setTransferListener(meter)
    }

    companion object {
        fun createIntent(context: Context?, url: String?): Intent {
            val bundle = Bundle()
            bundle.putString(Const.URL, url)
            val intent = Intent(context, VideoViewActivity::class.java)
            intent.putExtras(bundle)
            return intent
        }
    }
}
