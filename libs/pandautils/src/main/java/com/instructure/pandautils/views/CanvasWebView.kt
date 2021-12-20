/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 *
 * Scrolling Behavior Modifications Taken From
 * Copyright (C) 2015 takahirom
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.instructure.pandautils.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Html
import android.util.AttributeSet
import android.util.Patterns
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.APIHelper.simplifyHTML
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils.getAssetsFile
import com.instructure.canvasapi2.utils.Logger.e
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.FileUploadUtils.getExternalCacheDir
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.requestWebPermissions
import com.instructure.pandautils.video.VideoWebChromeClient
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URLConnection
import java.net.URLDecoder
import java.util.*

class CanvasWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private val encoding = "UTF-8"
    private var lastY = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY = 0
    private var firstScroll = true
    private var childHelper: NestedScrollingChildHelper? = null
    private var addedJavascriptInterface: Boolean = false

    interface CanvasWebViewClientCallback {
        fun openMediaFromWebView(mime: String, url: String, filename: String)
        fun onPageStartedCallback(webView: WebView, url: String)
        fun onPageFinishedCallback(webView: WebView, url: String)
        fun routeInternallyCallback(url: String)
        fun canRouteInternallyDelegate(url: String): Boolean

    }

    interface CanvasEmbeddedWebViewCallback {
        fun shouldLaunchInternalWebViewFragment(url: String): Boolean
        fun launchInternalWebViewFragment(url: String)
    }

    interface CanvasWebChromeClientCallback {
        fun onProgressChangedCallback(view: WebView?, newProgress: Int)
    }

    interface VideoPickerCallback {
        fun requestStartActivityForResult(intent: Intent, requestCode: Int)
        fun permissionsGranted(): Boolean
    }

    interface MediaDownloadCallback {
        fun downloadMedia(mime: String?, url: String?, filename: String?)
    }

    var canvasWebViewClientCallback: CanvasWebViewClientCallback? = null

    var canvasEmbeddedWebViewCallback: CanvasEmbeddedWebViewCallback? = null
    var canvasWebChromeClientCallback: CanvasWebChromeClientCallback? = null
    private var videoPickerCallback: VideoPickerCallback? = null
    private var mediaDownloadCallback: MediaDownloadCallback? = null
    private var webChromeClient: CanvasWebChromeClient? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    inner class JavascriptInterface {
        @android.webkit.JavascriptInterface  // Must match Javascript interface method of VideoWebChromeClient
        fun notifyVideoEnd() {
            // This code is not executed in the UI thread, so we must force that to happen
            Handler(Looper.getMainLooper()).post { webChromeClient?.onHideCustomView() }
        }
    }

    /**
     * @param content the content disposition that contains the file name
     * @param url used to generate a unique hashcode for the file
     * @return the parsed filename, or "file" if missing from the content disposition
     */
    fun parseFileNameFromContentDisposition(content: String, url: String?): String {
        var filename = "file"
        val temp = "filename="
        val index = content.indexOf(temp)
        if (index > -1) {
            var end = content.indexOf(";", index)
            if (end < 0) end = content.length
            // Remove the quotes
            filename = content.substring(index + temp.length, end).replace("\"".toRegex(), "")
            try {
                filename = URLDecoder.decode(filename, encoding)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            // Make the filename unique
            if (url != null) {
                filename = String.format(Locale.getDefault(), "%d_%s", url.hashCode(), filename)
            }
        }
        return filename
    }

    init {
        initSettings()
        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            if (contentDisposition != null) {
                val fileName = parseFileNameFromContentDisposition(contentDisposition, url)
                canvasWebViewClientCallback?.openMediaFromWebView(mimetype, url, fileName)
            }
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        this.settings.javaScriptEnabled = true
        this.settings.builtInZoomControls = true
        // Hide the zoom controls
        this.settings.displayZoomControls = false
        this.settings.useWideViewPort = true
        this.webViewClient = CanvasWebViewClient()
        this.settings.domStorageEnabled = true
        this.settings.mediaPlaybackRequiresUserGesture = false // Disabled to allow videos to be played

        // Increase text size based on the devices accessibility setting
        // fontScale comes back as a float
        val scalePercent = (resources.configuration.fontScale * 100).toInt()
        this.settings.textZoom = scalePercent
    }

    /**
     * Builds a video enabled WebChromeClient.
     */
    fun addVideoClient(activity: Activity) {
        webChromeClient = CanvasWebChromeClient(activity, this)
        setWebChromeClient(webChromeClient)
    }

    fun setZoomSettings(enabled: Boolean) {
        this.settings.builtInZoomControls = enabled
    }

    private fun addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            addJavascriptInterface(JavascriptInterface(), VideoWebChromeClient.JsInterfaceName)
            addedJavascriptInterface = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent is CoordinatorLayout) {
            childHelper = NestedScrollingChildHelper(this)
            isNestedScrollingEnabled = true
        }
    }

    override fun onPause() {
        // Calling onPause will stop Video's sound, but onResume must be called if resumed, otherwise the second time onPause is called it won't work
        try {
            super.onPause()
        } catch (npe: NullPointerException) {
            // Catch for API 16 devices (and perhaps others) and webkit
            e(npe.message)
        } catch (e: Exception) {
            e(e.message)
        }
    }

    /**
     * Create a context menu to copy the link that was pressed and then copy that link to the clipboard
     *
     */
    override fun onCreateContextMenu(menu: ContextMenu) {
        super.onCreateContextMenu(menu)
        val result = this.hitTestResult
        val handler = MenuItem.OnMenuItemClickListener { item -> // do the menu action
            if (item.itemId == COPY_LINK_ID) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(context.getString(R.string.link), result.extra)
                clipboard.setPrimaryClip(clip)

                // Let the user know
                Toast.makeText(context, context.getString(R.string.linkCopied), Toast.LENGTH_SHORT).show()
            } else if (item.itemId == SHARE_LINK_ID) {
                // Share the link with other apps
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(result.extra)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // No apps can do anything with this link, let the user know
                    Toast.makeText(context, context.getString(R.string.noApps), Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        @Suppress("DEPRECATION")
        if (result.type == HitTestResult.ANCHOR_TYPE || result.type == HitTestResult.SRC_ANCHOR_TYPE) {
            // Title of the link, use a custom view so we can show the entire link in the style we want
            val title = TextView(context)
            title.text = result.extra
            title.setTextColor(context.resources.getColor(R.color.canvasTextDark))
            val padding = context.DP(8).toInt()
            title.setPadding(padding * 2, padding, padding * 2, 0)
            menu.setHeaderView(title)

            // Menu options for a hyperlink.
            // Copy
            menu.add(0, COPY_LINK_ID, 0, context.getString(R.string.copyLinkAddress))
                .setOnMenuItemClickListener(handler)
            // Share with a different app
            menu.add(0, SHARE_LINK_ID, 1, context.getString(R.string.shareLink)).setOnMenuItemClickListener(handler)
        }
    }

    /**
     * Handles back presses for the CanvasWebView and the lifecycle of the ActivityContentVideoViewClient
     *
     * Use instead of goBack and canGoBack
     *
     * @return true if handled; false otherwise
     */
    fun handleGoBack(): Boolean {
        if (webChromeClient?.isVideoFullscreen == true) {
            return webChromeClient?.onBackPressed() ?: false
        } else if (super.canGoBack()) {
            super.goBack()
            return true
        }
        return false
    }

    private fun popBackStack() {
        (context as AppCompatActivity).supportFragmentManager.popBackStack()
    }

    inner class CanvasWebChromeClient internal constructor(activity: Activity, webView: CanvasWebView) :
        VideoWebChromeClient(activity, webView) {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            return showFileChooser(filePathCallback, fileChooserParams)
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            canvasWebChromeClientCallback?.onProgressChangedCallback(view, newProgress)
            super.onProgressChanged(view, newProgress)
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            (context as? Activity)?.requestWebPermissions(request)
        }

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, msg: Message): Boolean {
            // This allows us to handle links in an iFrame when they have a target of '_blank', which tells the WebView
            // to launch the URL in a new window.
            //
            // Without this, Chrome will launch, but doesn't appear to get the URL. This launches the browser and
            // explicitly sends it the URL.
            val url = view.hitTestResult.extra
            return if (url != null && url.isNotEmpty()) {
                val context = view.context
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
                false
            } else super.onCreateWindow(view, isDialog, isUserGesture, msg)
        }
    }

    inner class CanvasWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()
            if (isStudioDownload(url) && mediaDownloadCallback != null) {
                val extensionSegment = request.url.lastPathSegment
                val extension = extensionSegment?.substringAfterLast('.', "") ?: ""
                val fileName = parseFileNameFromContentDisposition(url, null) + extension
                mediaDownloadCallback?.downloadMedia("", url, fileName)
                view.post { stopLoading() } // Hack to stop loading the file in the WebView, since returning an empty response breaks what's being shown
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return handleShouldOverrideUrlLoading(view, request.url.toString())
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleShouldOverrideUrlLoading(view, url)
        }

        private fun handleShouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            // Check to see if we need to do anything with the link that was clicked

            // Default headers
            val extraHeaders = Utils.referer.toMutableMap()
            if (url.contains("yellowdig") && yellowdigInstalled()) {
                // Pertaining to the Yellowdig LTI:
                //  This is a yellowdig URL, they have a special condition on their end
                //  to not send us a yellowdig URI scheme link if they detect anything
                //  in the 'X-Requested-With' header, so we're putting in a special
                //  case here to intercept and remove the value in that header. The WebView
                //  automatically adds this in and we can't remove it so we just blank it out
                extraHeaders["X-Requested-With"] = ""
            }

            // Check if the URL has a scheme that we aren't handling
            val uri = Uri.parse(url)
            if (uri != null && uri.scheme != null && uri.scheme != "http" && uri.scheme != "https") {
                // Special scheme, send URL to app that can handle it
                val intent = Intent(Intent.ACTION_VIEW, uri)
                // Verify that the intent will resolve to an activity
                if (intent.resolveActivity(context.packageManager) != null) {
                    if (uri.scheme == "yellowdig") {
                        // Pop off the LTI page so it doesn't try to reload the yellowdig app when going back to our app
                        popBackStack()
                    }
                    context.startActivity(intent)
                    return true
                }
                if (url.startsWith("intent:")) {
                    try {
                        val appIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (appIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(appIntent)
                            popBackStack()
                            return true
                        }
                        //try to find fallback url
                        val fallbackUrl = appIntent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl, extraHeaders)
                            return true
                        }
                    } catch (e: URISyntaxException) {
                        //not an intent uri
                    }
                }
            }
            // Is the URL something we can link to inside our application?
            if (canvasWebViewClientCallback?.canRouteInternallyDelegate(url) == true) {
                canvasWebViewClientCallback?.routeInternallyCallback(url)
                return true
            }

            // Handle the embedded webview case (Its not within the InternalWebViewFragment)
            if (canvasEmbeddedWebViewCallback?.shouldLaunchInternalWebViewFragment(url) == true) {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    var contentTypeGuess: String? = null
                    try {
                        contentTypeGuess = URLConnection.guessContentTypeFromName(url)
                    } catch (e: StringIndexOutOfBoundsException) {
                        FirebaseCrashlytics.getInstance().recordException(MalformedURLException("Unable to parse content type of url: $url"))
                    }
                    // null when type can't be determined, launchInternalWebView anyway
                    // When contentType has 'application', it typically means it's a pdf or some type of document that needs to be downloaded,
                    //   so allow the embedded webview to open the url, which will trigger the DownloadListener. If for some reason the content can
                    //   be loaded in the webview, the content will just load in the embedded webview (which isn't ideal, but in majority of cases it won't happen).
                    if (contentTypeGuess == null || !contentTypeGuess.contains("application")) {
                        canvasEmbeddedWebViewCallback?.launchInternalWebViewFragment(url)
                        return true
                    }
                } else return true
            }
            if (url.startsWith("blob:")) return false // MBL-9546 (Don't remove me, I'll break an LTI tool if you do.)
            view.loadUrl(url, extraHeaders)
            // we're handling the url ourselves, so return true.
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            canvasWebViewClientCallback?.onPageStartedCallback(view, url)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            // Clear the history if formatHtml was called more than once. Refer to formatHtml's NOTE
            if (url.startsWith(getHtmlAsUrl(""))) view.clearHistory()
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            canvasWebViewClientCallback?.onPageFinishedCallback(view, url)
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (failingUrl.startsWith("file://")) {
                view.loadUrl(failingUrl.replaceFirst("file://".toRegex(), "https://"), Utils.referer)
            }
        }
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        addJavascriptInterface()
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(url: String?, data: String, mimeType: String?, encoding: String?, history: String?) {
        addJavascriptInterface()
        super.loadDataWithBaseURL(url, data, mimeType, encoding, history)
    }

    override fun loadUrl(url: String) {
        addJavascriptInterface()
        super.loadUrl(url)
    }

    /**
     * Makes html content somewhat suitable for mobile
     *
     * NOTE: The web history is cleared when formatHtml is called. Only the loaded page will appear in the webView.copyBackForwardList()
     * Back history will not work with multiple pages. This allows for formatHtml to be called several times without causing the user to
     * press back 2 or 3 times.
     *
     * @param html
     * @param title
     * @return
     */
    fun loadHtml(html: String, title: String?): String {
        val result = formatHtml(html, title)
        loadDataWithBaseURL(getReferrer(true), result, "text/html", encoding, getHtmlAsUrl(result))
        return result
    }

    /**
     * Helper function that makes html content somewhat suitable for mobile
     */
    fun formatHtml(html: String, title: String? = ""): String {
        var formatted = applyWorkAroundForDoubleSlashesAsUrlSource(html)
        formatted = addProtocolToLinks(formatted)
        formatted = checkForMathTags(formatted)
        val htmlWrapperFileName = if (ApiPrefs.canvasForElementary) "html_wrapper_k5.html" else "html_wrapper.html"
        val htmlWrapper = getAssetsFile(context, htmlWrapperFileName)
        return htmlWrapper
            .replace("{\$CONTENT$}", formatted)
            .replace("{\$TITLE$}", title ?: "")
    }

    /**
     * Loads the provided HTML string without modification
     * @param html The raw HTML to load
     */
    fun loadRawHtml(html: String) {
        loadDataWithBaseURL(getReferrer(true), html, "text/html", encoding, getHtmlAsUrl(html))
    }

    /*
     *  Work around for API 16 devices (and perhaps others). When pressing back the webview was loading 'about:blank' instead of the custom html
     */
    private fun getHtmlAsUrl(html: String, encoding: String = this.encoding): String {
        return String.format("data:text/html; charset=%s, %s", encoding, html)
    }

    private fun checkForMathTags(content: String): String {
        // If this html that we're about to load has a math tag and isn't just an image we want to parse it with MathJax.
        // This is the version that web currently uses (the 2.7.1 is the version number) and this is the check that they do to
        // decide if they'll run the MathJax script on the webview
        if (content.contains("<math") && !content.contains("<img class='equation_image'")) {
            return """<script type="text/javascript"
                src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML">
        </script>$content"""
        }
        return content
    }

    /**
     * Check if the Yellowdig app is installed by checking
     * to see if there is an app that handles the yellowdig URI scheme.
     * @return True if installed, false if not
     */
    private fun yellowdigInstalled(): Boolean {
        val uri = Uri.parse("yellowdig://")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return intent.resolveActivity(context.packageManager) != null
    }

    @Suppress("DEPRECATION")
    private fun setupAccessibilityContentDescription(formattedHtml: String, title: String?) {
        // Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        val contentDescription = title?.let { "$it $formattedHtml" } ?: formattedHtml
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.contentDescription = simplifyHTML(Html.fromHtml(contentDescription, Html.FROM_HTML_MODE_LEGACY))
        } else {
            this.contentDescription = simplifyHTML(Html.fromHtml(contentDescription))
        }
    }

    fun setCanvasWebChromeClientShowFilePickerCallback(callback: VideoPickerCallback?) {
        this.settings.allowFileAccess = true
        videoPickerCallback = callback
    }

    fun setMediaDownloadCallback(callback: MediaDownloadCallback?) {
        mediaDownloadCallback = callback
    }

    fun clearPickerCallback() {
        filePathCallback = null
    }

    // For Android 5.0+
    private fun showFileChooser(
        filePath: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ): Boolean {
        // Make sure we have permissions first
        if (videoPickerCallback?.permissionsGranted() == true) {
            // Double check that we don't have any existing callbacks
            filePathCallback?.onReceiveValue(null)
            filePathCallback = filePath
            startFileChooser(VIDEO_PICKER_RESULT_CODE, fileChooserParams)
            return true
        }
        return false
    }

    // Allow users to pick files for webview upload. Most common usage is for Studio uploads. Modified to allow file
    // chooser params so other LTIs can also be used (like turnitin).
    @Suppress("SameParameterValue")
    private fun startFileChooser(requestCode: Int, fileChooserParams: WebChromeClient.FileChooserParams?) {
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
        val acceptTypes = fileChooserParams?.acceptTypes ?: emptyArray()
        if (acceptTypes.isNotEmpty() && acceptTypes[0].isNotBlank()) {
            // An array with one blank element will clear out the allowed mime types,
            // so we only want to add extra mime types if we are given any valid types
            fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes)
        }
        fileIntent.type = "*/*"

        // Determine if we can upload video files so we can add the camera to the list of choices
        var allowRecording = false
        for (type in acceptTypes) {
            if (type.equals("video/*", ignoreCase = true) || type.equals("video/mp4", ignoreCase = true)) {
                allowRecording = true
                break
            }
        }
        val cameraIntents: MutableList<Intent> = ArrayList()
        if (allowRecording) {
            val fileName = "vid_" + System.currentTimeMillis() + ".mp4"
            val file = File(getExternalCacheDir(context), fileName)
            val cameraImageUri = FileProvider.getUriForFile(
                context,
                context.packageName + Const.FILE_PROVIDER_AUTHORITY,
                file
            )
            // Camera.
            val captureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            val packageManager = context.packageManager
            val listCam = packageManager.queryIntentActivities(captureIntent, 0)
            for (res in listCam) {
                val packageName = res.activityInfo.packageName
                val intent = Intent(captureIntent)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(packageName)
                cameraIntents.add(intent)
            }
        }
        // Chooser of filesystem options.
        val title = fileChooserParams?.title.takeIf { !it.isNullOrBlank() } ?: context.getString(R.string.pickFile)
        val chooserIntent = Intent.createChooser(fileIntent, title)

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray<Parcelable>())
        videoPickerCallback?.requestStartActivityForResult(chooserIntent, requestCode)
    }

    fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == VIDEO_PICKER_RESULT_CODE && filePathCallback != null) {
            if (resultCode == Activity.RESULT_OK && data?.data != null) {
                val results = arrayOf(data.data!!)
                filePathCallback?.onReceiveValue(results)
            } else {
                filePathCallback?.onReceiveValue(null)
            }
        }
        clearPickerCallback()
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (childHelper == null) return super.onTouchEvent(ev)
        val returnValue: Boolean
        val event = MotionEvent.obtain(ev)
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    lastY = eventY - scrollOffset[1]
                    event.offsetLocation(0f, (-scrollOffset[1]).toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY, scrollOffset)) {
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                if (firstScroll) {
                    // dispatching first down scrolling properly by making sure that first deltaY will be -ve
                    lastY = eventY - 5
                    firstScroll = false
                } else {
                    lastY = eventY
                }
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            else -> {
                returnValue = super.onTouchEvent(event)
                // end NestedScroll
                stopNestedScroll()
            }
        }
        return returnValue
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper?.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper?.isNestedScrollingEnabled == true
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper?.startNestedScroll(axes) == true
    }

    override fun stopNestedScroll() {
        childHelper?.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper?.hasNestedScrollingParent() == true
    }

    override fun dispatchNestedScroll(dx1: Int, dy1: Int, dx2: Int, dy2: Int, offset: IntArray?): Boolean {
        return childHelper?.dispatchNestedScroll(dx1, dy1, dx2, dy2, offset) == true
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return childHelper?.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow) == true
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper?.dispatchNestedFling(velocityX, velocityY, consumed) == true
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper?.dispatchNestedPreFling(velocityX, velocityY) == true
    }

    companion object {
        private const val VIDEO_PICKER_RESULT_CODE = 1202
        private const val COPY_LINK_ID = 9357
        private const val SHARE_LINK_ID = 9358

        fun getReferrer(shouldIncludeProtocol: Boolean = false): String {
            return if (shouldIncludeProtocol) ApiPrefs.fullDomain else ApiPrefs.domain
        }

        fun applyWorkAroundForDoubleSlashesAsUrlSource(html: String): String {
            if (html.isBlank()) return ""
            // Fix for embedded videos that have // instead of http://
            return html.replace("href=\"//".toRegex(), "href=\"https://")
                .replace("href='//".toRegex(), "href='https://")
                .replace("src=\"//".toRegex(), "src=\"https://")
                .replace("src='//".toRegex(), "src='https://")
        }

        /**
         * When we parse the HTML if the links don't have a protocol we aren't able to handle them. This
         * will add http:// to any link that doesn't have one
         *
         * @return HTML with protocol added
         */
        fun addProtocolToLinks(html: String): String {
            if (html.isBlank()) return ""
            return html.replace("href=\"www.".toRegex(), "href=\"https://www.")
                .replace("href='www.".toRegex(), "href='https://www.")
                .replace("src=\"www.".toRegex(), "src=\"https://www.")
                .replace("src='www.".toRegex(), "src='https://www.")
        }

        fun isStudioDownload(url: String): Boolean {
            return url.contains("instructuremedia.com/fetch/") && url.contains("disposition=download")
        }

        fun containsLTI(html: String, encoding: String?): Boolean {
            // BaseURL is set as Referer. Referer needed for some Vimeo videos to play
            // Studio needs the protocol attached to the referrer, so use that if we're using Studio
            try {
                //sanitize the html
                val sanitized = html.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
                if (URLDecoder.decode(sanitized, encoding).contains("/external_tools/retrieve")) return true
            } catch (e: UnsupportedEncodingException) { /* do nothing */
            }
            return false
        }
    }
}
