/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.pandautils.views

import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.cardview.widget.CardView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.ViewFloatingMediaRecorderBinding
import com.instructure.pandautils.databinding.ViewFloatingMediaRecorderVideoBinding
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import java.io.File
import java.util.UUID


class FloatingRecordingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ViewFloatingMediaRecorderBinding
    /** The binding of the view used to record video. This must be inflated separately from the main layout in
     * order to capture exceptions during inflation and disable the video functionality */
    private var videoViewBinding: ViewFloatingMediaRecorderVideoBinding?

    private val timerHandler = Handler()
    private var startTime = 0L

    private var videoFile: File? = null
    private var isRecording = false

    lateinit var stoppedCallback: () -> Unit
    lateinit var replayCallback: (File?) -> Unit

    lateinit var videoCaptureCallback: CameraListener

    /** Whether there was an error when initializing the video view. If this is true then
     * all video functionality should be considered disabled. */
    private var hasVideoError = false

    // Audio related
    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null
    private val TEMP_FILENAME = "audio.amr"

    private var mediaType: RecordingMediaType? = null

    lateinit var recordingCallback: (File?) -> Unit

    init {
        val recordingView = View.inflate(context, R.layout.view_floating_media_recorder, this) as ViewGroup
        binding = ViewFloatingMediaRecorderBinding.bind(recordingView)

        /* CameraView will throw an exception during inflation on some devices. We capture
           that exception here and show an error view instead when we try to record video. */
        try {
            videoViewBinding = ViewFloatingMediaRecorderVideoBinding.inflate(LayoutInflater.from(context), binding.dragDetectLayout, false)
            binding.dragDetectLayout.addView(videoViewBinding?.root)
        } catch (e: InflateException) {
            hasVideoError = true
            videoViewBinding = null
            if (BuildConfig.DEBUG) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
        }

        setupFloatingAction()

        this.elevation = context.DP(8.0f)
        this.useCompatPadding = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timerHandler.removeCallbacks(timerRunnable)
        when(mediaType) {
            is RecordingMediaType.Video -> stopVideoView()
            is RecordingMediaType.Audio -> cleanupMediaObjects()
            else -> {}
        }
    }

    fun startVideoView() {
        binding.root.setVisible()
        if (hasVideoError) return
        resetVideoViews()
        videoViewBinding?.camera?.open()
    }

    private fun stopVideoView() {
        videoFile?.delete()
        binding.root.setGone()
        if(isRecording) {
            isRecording = false
            videoViewBinding?.camera?.stopVideo()
        }
        videoViewBinding?.camera?.close()
        timerHandler.removeCallbacks(timerRunnable)
        stoppedCallback()
    }

    fun setContentType(type: RecordingMediaType) {
        val actualType = when (type) {
            is RecordingMediaType.Video -> {
                if (hasVideoError) RecordingMediaType.Error else RecordingMediaType.Video
            }
            else  -> type
        }
        setupContentType(actualType)
    }

    private fun setupContentType(type: RecordingMediaType) {
        when (type) {
            RecordingMediaType.Video -> setupVideo()
            RecordingMediaType.Audio -> setupAudio()
            RecordingMediaType.Error -> setupError()
        }
    }

    private fun setupFloatingAction() {
        binding.dragDetectLayout.setOnTouchListener(object : View.OnTouchListener {
            private var lastAction: Int = 0
            var windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            var display = windowManager.defaultDisplay
            var size = Point()
            private var initialX = 0F
            private var initialY = 0F
            private var initialTouchX = 0F
            private var initialTouchY = 0F


            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {

                        // Remember the initial position.
                        initialX = binding.root.x
                        initialY = binding.root.y

                        // Remember the initial position.
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // As we implemented on touch listener with ACTION_MOVE,
                        // we have to check if the previous action was ACTION_DOWN
                        // to identify if the user clicked the view or not.
                        return if (lastAction == MotionEvent.ACTION_DOWN) {
                            false
                        } else {
                            lastAction = event.action
                            true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the X and Y coordinates of the view.
                        display.getSize(size)
                        val newX = (initialX + (event.rawX - initialTouchX)).coerceIn(0f, size.x - v.width.toFloat())
                        val newY = (initialY + (event.rawY - initialTouchY)).coerceIn(0f, size.y - v.height.toFloat())
                        binding.root.x = newX
                        binding.root.y = newY
                        lastAction = event.action
                        return true
                    }
                }
                return false
            }
        })
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            val seconds: Int = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60

            // Mod the seconds by 60 so they reset every minute
            binding.toolbarTitle.text = "%02d:%02d:%02d".format(hours, minutes, seconds % 60)
            setA11yStringForTitle(hours, minutes, seconds % 60)
            timerHandler.postDelayed(this, 500)
        }
    }

    private val playbackTimerRunnable = object : Runnable {
        override fun run() {

            val millis = mediaPlayer?.currentPosition ?: 0
            val seconds: Int = (millis / 1000)
            val minutes = seconds / 60
            val hours = minutes / 60

            val duration = mediaPlayer?.duration ?: 0

            val totalDurationSeconds = duration / 1000
            val totalDurationMinutes = totalDurationSeconds / 60
            val totalDurationHours = totalDurationMinutes / 60

            // Mod the seconds by 60 so they reset every minute
            binding.toolbarTitle.text = "%02d:%02d:%02d / %02d:%02d:%02d".format(hours, minutes, seconds % 60, totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
            setA11yStringForAudioReplayTitle(hours, minutes, seconds % 60, totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
            if (seconds <= totalDurationSeconds)
                timerHandler.postDelayed(this, 500)
        }
    }

    private fun setupError() {
        videoViewBinding?.root?.setGone()
        with(binding) {
            audio.root.setGone()
            errorView.setVisible()
            deleteButton.setGone()
            toolbarTitle.text = ""
            closeButton.onClick {
                setGone()
            }
        }
        return
    }

    private fun setupVideo() {
        videoViewBinding?.root?.setVisible()
        binding.audio.root.setGone()
        binding.errorView.setGone()
        videoViewBinding?.startRecordingButton?.requestAccessibilityFocus()

        videoFile = File(context.cacheDir, "temp.mp4")

        videoCaptureCallback = object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {
                timerHandler.removeCallbacks(timerRunnable)
                videoFile = result.file
            }
        }

        // Set the close button.
        binding.closeButton.onClick {
            stopVideoView()
        }

        videoViewBinding?.startRecordingButton?.onClick {
            videoFile?.let { file ->
                videoViewBinding?.camera?.takeVideo(file)
                isRecording = true
                startTime = System.currentTimeMillis()
                timerHandler.postDelayed(timerRunnable, 0)
                setViewStateStartRecording()
            }
        }

        videoViewBinding?.endRecordingButton?.onClick {
            videoViewBinding?.camera?.stopVideo()
            isRecording = false
            timerHandler.removeCallbacks(timerRunnable)
            setViewStateEndRecording()
        }

        binding.deleteButton.onClick {
            videoFile?.delete()
            resetVideoViews()
            videoViewBinding?.camera?.close()
            videoViewBinding?.camera?.open()
        }

        videoViewBinding?.replayButton?.onClick {
            replayCallback(videoFile)
        }

        videoViewBinding?.sendButton?.onClickWithRequireNetwork {
            val newFile = File(context.cacheDir, UUID.randomUUID().toString() + "video.mp4")
            newFile.createNewFile()
            videoFile?.renameTo(newFile)
            recordingCallback(newFile)
            binding.root.setGone()
            if (isRecording) {
                isRecording = false
                videoViewBinding?.camera?.stopVideo()
            }
            videoViewBinding?.camera?.close()
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun resetVideoViews() {
        binding.toolbarTitle.setText(R.string.recordingTimerDefault)
        setA11yStringForTitle()
        binding.deleteButton.setGone()
        videoViewBinding?.replayButton?.setGone()
        videoViewBinding?.sendButton?.setGone()
        videoViewBinding?.startRecordingButton?.setVisible()
        videoViewBinding?.endRecordingButton?.setGone()
        binding.closeButton.setVisible()
    }

    private fun setViewStateEndRecording() {
        videoViewBinding?.endRecordingButton?.setGone()
        videoViewBinding?.replayButton?.setVisible()
        videoViewBinding?.sendButton?.setVisible()
        binding.deleteButton.setVisible()
        binding.closeButton.setVisible()
        videoViewBinding?.sendButton?.requestAccessibilityFocus()
    }

    private fun setViewStateStartRecording() {
        videoViewBinding?.startRecordingButton?.setGone()
        // We need to prevent them from stopping this before its finished being started
        videoViewBinding?.endRecordingButton?.postDelayed({
            videoViewBinding?.endRecordingButton?.setVisible()
            videoViewBinding?.endRecordingButton?.requestAccessibilityFocus()
        }, 500)
        binding.closeButton.setGone()
        videoViewBinding?.endRecordingButton?.alpha = 0.5f
    }

    private fun setupAudio() {
        binding.audio.root.setVisible()
        setupAudioViews()

        binding.audio.recordAudioButton.onClick {
            binding.audio.recordAudioButton.setGone()
            binding.closeButton.setGone()
            binding.audio.stopButton.setVisible()
            binding.audio.stopButton.requestAccessibilityFocus()
            val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
            if (audioFile.exists())
            // File existed previously - delete old file
                audioFile.delete()
            audioFile.createNewFile()

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            timerHandler.post(timerRunnable)
        }

        binding.audio.stopButton.onClick {
            cleanupMediaObjects()

            // Stop the timer
            timerHandler.removeCallbacks(timerRunnable)

            binding.closeButton.setVisible()
            binding.audio.stopButton.setGone()

            binding.deleteButton.setVisible()
            binding.deleteButton.onClick {
                cleanupMediaObjects()
                val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
                if (audioFile.exists()) {
                    audioFile.delete()
                }
                setupAudioViews()
            }

            binding.audio.sendAudioButton.setVisible()
            binding.audio.sendAudioButton.requestAccessibilityFocus()
            binding.audio.sendAudioButton.onClickWithRequireNetwork {
                cleanupMediaObjects()
                val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
                if (audioFile.exists()) {
                    val newFile = File(context.externalCacheDir, UUID.randomUUID().toString() + TEMP_FILENAME)
                    audioFile.renameTo(newFile)
                    recordingCallback(newFile)
                }
                setGone()
                stoppedCallback()
            }

            binding.audio.replayAudioButton.setVisible()
            binding.audio.replayAudioButton.onClick { // This button flips between Replay and Stop
                if(binding.audio.replayAudioButton.text == context.getString(R.string.replay)) {
                    binding.dragIcon.setGone()

                    val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
                    if (audioFile.exists()) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer()
                            mediaPlayer?.setDataSource(audioFile.absolutePath)
                            mediaPlayer?.prepare()
                        }
                        mediaPlayer?.start()
                        binding.audio.replayAudioButton.text = context.getString(R.string.stop)
                        timerHandler.post(playbackTimerRunnable)
                    }
                } else {
                    if(mediaPlayer != null) {
                        mediaPlayer?.pause()
                        mediaPlayer?.seekTo(0)
                    }
                    // Reset view to "done" state (replay/send)
                    binding.audio.replayAudioButton.text = context.getString(R.string.replay)
                    timerHandler.removeCallbacks(playbackTimerRunnable)
                    setTimeForAudioRecording() // Resets the title to show the total duration of recording
                    binding.dragIcon.setVisible()
                }

            }
        }

        binding.closeButton.onClick {
            val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
            if (audioFile.exists()) {
                audioFile.delete()
            }

            cleanupMediaObjects()

            setGone()
            stoppedCallback()
        }
    }

    private fun setupAudioViews() {
        binding.toolbarTitle.setText(R.string.recordingTimerDefault)
        setA11yStringForTitle()
        binding.audio.recordAudioButton.setVisible()
        videoViewBinding?.root?.setGone()
        binding.errorView.setGone()
        binding.deleteButton.setGone()
        binding.audio.stopButton.setGone()
        binding.audio.replayAudioButton.setGone()
        binding.audio.sendAudioButton.setGone()
        binding.dragIcon.setVisible()

        binding.audio.recordAudioButton.requestAccessibilityFocus()
    }

    private fun cleanupMediaObjects() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        // Remove timer callbacks
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.removeCallbacks(playbackTimerRunnable)
    }

    // Sets the toolbar to the total duration of the audio recording.
    private fun setTimeForAudioRecording() {
        val duration = mediaPlayer?.duration ?: 0

        val totalDurationSeconds = duration / 1000
        val totalDurationMinutes = totalDurationSeconds / 60
        val totalDurationHours = totalDurationMinutes / 60

        binding.toolbarTitle.text = "%02d:%02d:%02d".format(totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
        setA11yStringForTitle(totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
    }

    private fun setA11yStringForTitle(hours: Int = 0, minutes: Int = 0, seconds: Int = 0) {
        binding.toolbarTitle.contentDescription = context.getString(R.string.recordingTimerContentDescription, hours, minutes, seconds)
    }

    private fun setA11yStringForAudioReplayTitle(hours: Int, minutes: Int, seconds: Int, totalHours: Int, totalMinutes: Int, totalSeconds: Int) {
        binding.toolbarTitle.contentDescription = context.getString(R.string.recordingTimerSpeechDivider,
                context.getString(R.string.recordingTimerContentDescription, hours, minutes, seconds),
                context.getString(R.string.recordingTimerContentDescription, totalHours, totalMinutes, totalSeconds))
    }
}

sealed class RecordingMediaType {
    object Video : RecordingMediaType()
    object Audio : RecordingMediaType()
    object Error : RecordingMediaType()
}


