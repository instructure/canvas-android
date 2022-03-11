package com.instructure.pandautils.video

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.view.*
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import com.instructure.pandautils.R
import com.instructure.pandautils.views.CanvasWebView
import java.lang.ref.WeakReference

/**
 * This class serves as a WebChromeClient to be set to a WebView, allowing it to play video.
 * Video will play differently depending on target API level (in-line, fullscreen, or both).
 *
 *
 * It has been tested with the following video classes:
 * - android.widget.VideoView (typically API level <11)
 * - android.webkit.HTML5VideoFullScreen$VideoSurfaceView/VideoTextureView (typically API level 11-18)
 * - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView (typically API level 19+)
 *
 *
 * Important notes:
 * - For API level 11+, android:hardwareAccelerated="true" must be set in the application manifest.
 * - The invoking activity must call VideoWebChromeClient's onBackPressed() inside of its own onBackPressed().
 * - Tested in Android API levels 8-19. Only tested on http://m.youtube.com.
 *
 * @author Cristian Perez (http://cpr.name)
 *
 *
 *
 * Modifications by Instructure
 */
abstract class VideoWebChromeClient : WebChromeClient, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var webView: CanvasWebView? = null
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    var isVideoFullscreen: Boolean = false
        private set// Indicates if the video is being displayed using a custom view (typically full-screen)
    private var videoViewContainer: FrameLayout? = null
    private var videoViewCallback: WebChromeClient.CustomViewCallback? = null

    private var toggledFullscreenCallback: ToggledFullscreenCallback? = null

    interface ToggledFullscreenCallback {
        fun toggledFullscreen(fullscreen: Boolean)
    }

    /**
     * Never use this constructor alone.
     * This constructor allows this class to be defined as an inline inner class in which the user can override methods
     */
    constructor() {}

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityVideoView    A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     */
    constructor(activity: Activity, webView: CanvasWebView) {
        activityRef = WeakReference(activity)
        this.webView = webView
        this.isVideoFullscreen = false
    }

    /**
     * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
     *
     * @param callback A VideoWebChromeClient.ToggledFullscreenCallback callback
     */
    fun setOnToggledFullscreen(callback: ToggledFullscreenCallback) {
        this.toggledFullscreenCallback = callback
    }

    override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (view is FrameLayout) {

            // A video wants to be shown
            val focusedChild = view.focusedChild

            // Save video related variables
            this.isVideoFullscreen = true
            this.videoViewContainer = view
            this.videoViewCallback = callback

            videoViewContainer?.id = R.id.videoFullScreenView
            videoViewContainer?.setBackgroundColor(Color.BLACK)
            videoViewContainer?.isClickable = true

            val window = activityRef?.get()?.window
            if(window != null) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.addContentView(videoViewContainer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER))
            }

            if (focusedChild is android.widget.VideoView) {
                // android.widget.VideoView (typically API level <11)

                // Handle all the required events
                focusedChild.setOnPreparedListener(this)
                focusedChild.setOnCompletionListener(this)
                focusedChild.setOnErrorListener(this)
            } else {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below
                if (webView != null && webView!!.settings.javaScriptEnabled && focusedChild is SurfaceView) {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    var js = "javascript:"
                    js += "var _ytrp_html5_video_last;"
                    js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];"
                    js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {"
                    run {
                        js += "_ytrp_html5_video_last = _ytrp_html5_video;"
                        js += "function _ytrp_html5_video_ended() {"
                        run {
                            js += "$JsInterfaceName.notifyVideoEnd();"
                        }
                        js += "}"
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);"
                    }
                    js += "}"
                    webView!!.loadUrl(js)
                }
            }

            // Notify full-screen change
            toggledFullscreenCallback?.toggledFullscreen(true)
        }
    }

    override fun onHideCustomView() {
        // This method should be manually called on video end in all cases because it's not always called automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).

        if (isVideoFullscreen) {
            val window = activityRef?.get()?.window
            if(window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.decorView.let {
                    if (it.findViewById<View>(R.id.videoFullScreenView) != null) {
                        val container = it.findViewById<View>(R.id.videoFullScreenView).parent as? ViewGroup
                        container?.removeView(videoViewContainer)
                    }
                }
            }

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback!!.javaClass.name.contains(".chromium.")) {
                videoViewCallback!!.onCustomViewHidden()
            }

            // Reset video related variables
            isVideoFullscreen = false
            videoViewContainer = null
            videoViewCallback = null

            // Notify full-screen change
            toggledFullscreenCallback?.toggledFullscreen(false)
        }
    }

    override fun onPrepared(mp: MediaPlayer) { // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
    }

    override fun onCompletion(mp: MediaPlayer) { // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
        onHideCustomView()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean { // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
        return false // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     *
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    fun onBackPressed(): Boolean {
        if (isVideoFullscreen) {
            onHideCustomView()
            return true
        } else {
            return false
        }
    }

    companion object {
        const val JsInterfaceName = "_VideoEnabledWebView"
    }
}