/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.flurgle.camerakit.CameraListener
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.dialog.SGMediaCommentType
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.requestAccessibilityFocus
import kotlinx.android.synthetic.main.view_floating_media_recorder.view.*
import kotlinx.android.synthetic.main.view_floating_media_recorder_audio.view.*
import kotlinx.android.synthetic.main.view_floating_media_recorder_video.view.*
import java.io.File
import java.util.*


class FloatingRecordingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0

) : CardView(context, attrs, defStyleAttr) {

    private val timerHandler = Handler()
    private var startTime = 0L

    @Suppress("JoinDeclarationAndAssignment")
    private var recordingView: ViewGroup

    private var videoFile: File? = null
    private var isRecording = false

    lateinit var stoppedCallback: () -> Unit
    lateinit var replayCallback: (File?) -> Unit

    // Audio related
    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null
    private val TEMP_FILENAME = "audio.3gp"

    private var mediaType: SGMediaCommentType? = null

    lateinit var recordingCallback: (File?) -> Unit

    init {
        recordingView = View.inflate(context, R.layout.view_floating_media_recorder, this) as ViewGroup
        setupFloatingAction()

        this.elevation = context.DP(8.0f)
        this.useCompatPadding = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timerHandler.removeCallbacks(timerRunnable)
        when(mediaType) {
            is SGMediaCommentType.Video -> stopVideoView()
            is SGMediaCommentType.Audio -> cleanupMediaObjects()
        }
    }

    fun startVideoView() {
        recordingView.setVisible()
        resetVideoViews()
        recordingView.camera.start()
    }

    private fun stopVideoView() {
        videoFile?.delete()
        recordingView.setGone()
        if(isRecording) {
            isRecording = false
            recordingView.camera.stopRecordingVideo()
        }
        recordingView.camera.stop()
        timerHandler.removeCallbacks(timerRunnable)
        stoppedCallback()
    }

    fun setContentType(type: SGMediaCommentType) = when (type) {
        is SGMediaCommentType.Video -> {
            mediaType = SGMediaCommentType.Video()
            setupVideo()
        }
        is SGMediaCommentType.Audio -> {
            mediaType = SGMediaCommentType.Audio()
            setupAudio()
        }
    }

    private fun setupFloatingAction() {
        recordingView.dragView.setOnTouchListener(object : View.OnTouchListener {
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

                        //remember the initial position.
                        initialX = recordingView.x
                        initialY = recordingView.y

                        //remember the initial position.
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            return false
                        } else {
                            lastAction = event.action
                            return true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        display.getSize(size)
                        val newX = (initialX + (event.rawX - initialTouchX)).coerceIn(0f, size.x - v.width.toFloat())
                        val newY = (initialY + (event.rawY - initialTouchY)).coerceIn(0f, size.y - v.height.toFloat())
                        recordingView.x = newX
                        recordingView.y = newY
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

            //Mod the seconds by 60 so they reset every minute
            recordingView.toolbarTitle?.text = "%02d:%02d:%02d".format(hours, minutes, seconds % 60)
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

            //Mod the seconds by 60 so they reset every minute
            recordingView.toolbarTitle?.text = "%02d:%02d:%02d / %02d:%02d:%02d".format(hours, minutes, seconds % 60, totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
            setA11yStringForAudioReplayTitle(hours, minutes, seconds % 60, totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
            if (seconds <= totalDurationSeconds)
                timerHandler.postDelayed(this, 500)
        }
    }



    private fun setupVideo() {
        recordingView.video.setVisible()
        recordingView.audio.setGone()

        recordingView.startRecordingButton.requestAccessibilityFocus()

        recordingView.camera.setCameraListener(object : CameraListener() {
            override fun onVideoTaken(video: File?) {
                super.onVideoTaken(video)
                //show toast
                timerHandler.removeCallbacks(timerRunnable)
                videoFile = video
            }
        })

        //Set the close button.
        recordingView.closeButton.onClick {
            stopVideoView()
        }

        recordingView.startRecordingButton.onClick {
            recordingView.camera.startRecordingVideo()
            isRecording = true
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
            setViewStateStartRecording()
        }

        recordingView.endRecordingButton.onClick {
            recordingView.camera.stopRecordingVideo()
            isRecording = false
            timerHandler.removeCallbacks(timerRunnable)
            setViewStateEndRecording()
        }

        recordingView.deleteButton.onClick {
            videoFile?.delete()
            resetVideoViews()
        }

        recordingView.replayButton.onClick {
            replayCallback(videoFile)
        }

        recordingView.sendButton.onClickWithRequireNetwork {
            val newFile = File(context.externalCacheDir, UUID.randomUUID().toString() + "video.mp4")
            videoFile?.renameTo(newFile)
            recordingCallback(newFile)
            recordingView.setGone()
            if(isRecording) {
                isRecording = false
                recordingView.camera.stopRecordingVideo()
            }
            recordingView.camera.stop()
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun resetVideoViews() {
        recordingView.toolbarTitle.setText(R.string.recordingTimerDefault)
        setA11yStringForTitle()
        recordingView.deleteButton.setGone()
        recordingView.replayButton.setGone()
        recordingView.sendButton.setGone()
        recordingView.startRecordingButton.setVisible()
        recordingView.endRecordingButton.setGone()
        recordingView.closeButton.setVisible()
    }

    private fun setViewStateEndRecording() {
        recordingView.endRecordingButton.setGone()
        recordingView.replayButton.setVisible()
        recordingView.sendButton.setVisible()
        recordingView.deleteButton.setVisible()
        recordingView.closeButton.setVisible()
        recordingView.sendButton.requestAccessibilityFocus()
    }

    private fun setViewStateStartRecording() {
        recordingView.startRecordingButton.setGone()
        // We need to prevent them from stopping this before its finished being started
        recordingView.endRecordingButton.postDelayed({
            recordingView.endRecordingButton.setVisible()
            recordingView.endRecordingButton.requestAccessibilityFocus()
        }, 500)
        recordingView.closeButton.setGone()
        recordingView.endRecordingButton.alpha = 0.5f
    }

    private fun setupAudio() {
        recordingView.audio.setVisible()
        setupAudioViews()

        recordingView.recordAudioButton.onClick {
            recordingView.recordAudioButton.setGone()
            recordingView.closeButton.setGone()
            recordingView.stopButton.setVisible()
            recordingView.stopButton.requestAccessibilityFocus()
            val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
            if (audioFile.exists())
            // File existed previously - delete old file
                audioFile.delete()
            audioFile.createNewFile()

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            timerHandler.post(timerRunnable)
        }

        recordingView.stopButton.onClick {
            cleanupMediaObjects()

            // Stop the timer
            timerHandler.removeCallbacks(timerRunnable)

            recordingView.closeButton.setVisible()
            recordingView.stopButton.setGone()

            recordingView.deleteButton.setVisible()
            recordingView.deleteButton.onClick {
                cleanupMediaObjects()
                val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
                if (audioFile.exists()) {
                    audioFile.delete()
                }
                setupAudioViews()
            }

            recordingView.sendAudioButton.setVisible()
            recordingView.sendAudioButton.requestAccessibilityFocus()
            recordingView.sendAudioButton.onClickWithRequireNetwork {
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

            recordingView.replayAudioButton.setVisible()
            recordingView.replayAudioButton.onClick { // This button flips between Replay and Stop
                if(recordingView.replayAudioButton.text == context.getString(R.string.replay)) {
                    recordingView.dragIcon.setGone()

                    val audioFile = File(context.externalCacheDir, TEMP_FILENAME)
                    if (audioFile.exists()) {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer()
                            mediaPlayer?.setDataSource(audioFile.absolutePath)
                            mediaPlayer?.prepare()
                        }
                        mediaPlayer?.start()
                        recordingView.replayAudioButton.text = context.getString(R.string.stop)
                        timerHandler.post(playbackTimerRunnable)
                    }
                } else {
                    if(mediaPlayer != null) {
                        mediaPlayer?.pause()
                        mediaPlayer?.seekTo(0)
                    }
                    // Reset view to "done" state (replay/send)
                    recordingView.replayAudioButton.text = context.getString(R.string.replay)
                    timerHandler.removeCallbacks(playbackTimerRunnable)
                    setTimeForAudioRecording() // Resets the title to show the total duration of recording
                    recordingView.dragIcon.setVisible()
                }

            }
        }

        recordingView.closeButton.onClick {
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
        recordingView.toolbarTitle.setText(R.string.recordingTimerDefault)
        setA11yStringForTitle()
        recordingView.recordAudioButton.setVisible()
        recordingView.video.setGone()
        recordingView.deleteButton.setGone()
        recordingView.stopButton.setGone()
        recordingView.replayAudioButton.setGone()
        recordingView.sendAudioButton.setGone()
        recordingView.dragIcon.setVisible()

        recordingView.recordAudioButton.requestAccessibilityFocus()
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

    //Sets the toolbar to the total duration of the audio recording.
    private fun setTimeForAudioRecording() {
        val duration = mediaPlayer?.duration ?: 0

        val totalDurationSeconds = duration / 1000
        val totalDurationMinutes = totalDurationSeconds / 60
        val totalDurationHours = totalDurationMinutes / 60

        recordingView.toolbarTitle?.text = "%02d:%02d:%02d".format(totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
        setA11yStringForTitle(totalDurationHours, totalDurationMinutes, totalDurationSeconds % 60)
    }

    private fun setA11yStringForTitle(hours: Int = 0, minutes: Int = 0, seconds: Int = 0) {
        recordingView.toolbarTitle?.contentDescription = context.getString(R.string.recordingTimerContentDescription, hours, minutes, seconds)
    }

    private fun setA11yStringForAudioReplayTitle(hours: Int, minutes: Int, seconds: Int, totalHours: Int, totalMinutes: Int, totalSeconds: Int) {
        recordingView.toolbarTitle?.contentDescription = context.getString(R.string.recordingTimerSpeechDivider,
                context.getString(R.string.recordingTimerContentDescription, hours, minutes, seconds),
                context.getString(R.string.recordingTimerContentDescription, totalHours, totalMinutes, totalSeconds))
    }
}


