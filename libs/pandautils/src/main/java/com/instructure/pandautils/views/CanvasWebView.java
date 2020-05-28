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

package com.instructure.pandautils.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.FileUploadUtils;
import com.instructure.pandautils.utils.PermissionUtilsKt;
import com.instructure.pandautils.utils.Utils;
import com.instructure.pandautils.video.VideoWebChromeClient;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CanvasWebView extends WebView implements NestedScrollingChild {

    private static final int VIDEO_PICKER_RESULT_CODE = 1202;
    private static final int COPY_LINK_ID = 9357;
    private static final int SHARE_LINK_ID = 9358;

    private final String encoding = "UTF-8";

    private int mLastY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    private boolean firstScroll = true;
    private NestedScrollingChildHelper mChildHelper;
    private boolean addedJavascriptInterface;

    public interface CanvasWebViewClientCallback {
        void openMediaFromWebView(String mime, String url, String filename);
        void onPageStartedCallback(WebView webView, String url);
        void onPageFinishedCallback(WebView webView, String url);
        void routeInternallyCallback(String url);
        boolean canRouteInternallyDelegate(String url);
    }

    public interface CanvasEmbeddedWebViewCallback {
        boolean shouldLaunchInternalWebViewFragment(String url);
        void launchInternalWebViewFragment(String url);
    }

    public interface CanvasWebChromeClientCallback {
        void onProgressChangedCallback(WebView view, final int newProgress);
    }

    public interface VideoPickerCallback {
        void requestStartActivityForResult(Intent intent, int requestCode);
        boolean permissionsGranted();
    }

    public interface MediaDownloadCallback {
        void downloadMedia(String mime, String url, String filename);
    }

    private CanvasWebViewClientCallback mCanvasWebViewClientCallback;
    private CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback;
    private CanvasWebChromeClientCallback mCanvasWebChromeClientCallback;
    private VideoPickerCallback mVideoPickerCallback;
    private MediaDownloadCallback mMediaDownloadCallback;

    private Context mContext;
    private CanvasWebChromeClient mWebChromeClient;
    private ValueCallback<Uri[]> mFilePathCallback;

    public CanvasWebView(Context context) {
        super(context);
        addedJavascriptInterface = false;
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addedJavascriptInterface = false;
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addedJavascriptInterface = false;
        init(context);
    }

    public class JavascriptInterface {
        @android.webkit.JavascriptInterface
        @SuppressWarnings("unused")
        // Must match Javascript interface method of VideoWebChromeClient
        public void notifyVideoEnd() {
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mWebChromeClient != null) mWebChromeClient.onHideCustomView();
                }
            });
        }
    }

    /**
     * @param content the content disposition that contains the file name
     * @param url used to generate a unique hashcode for the file
     * @return the parsed filename, or "file" if missing from the content disposition
     */
    public String parseFileNameFromContentDisposition(String content, String url) {
        String filename = "file";
        String temp = "filename=";
        int index = content.indexOf(temp);

        if (index > -1) {
            int end = content.indexOf(";", index);
            if (end < 0) {
                end = content.length();
            }
            // Remove the quotes
            filename = content.substring(index + temp.length(), end).replaceAll("\"", "");

            try {
                filename = URLDecoder.decode(filename, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Make the filename unique
            if (url != null) {
                filename = String.format(Locale.getDefault(), "%d_%s", url.hashCode(), filename);
            }
        }

        return filename;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        mContext = context;

        // Enabled to allow better support for http content
        this.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
        // Hide the zoom controls
        this.getSettings().setDisplayZoomControls(false);

        this.getSettings().setUseWideViewPort(true);
        this.setWebViewClient(new CanvasWebViewClient());

        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setMediaPlaybackRequiresUserGesture(false); // Disabled to allow videos to be played

        // Increase text size based on the devices accessibility setting
        // fontScale comes back as a float
        int scalePercent = (int)(getResources().getConfiguration().fontScale * 100);
        this.getSettings().setTextZoom(scalePercent);

        this.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (contentDisposition != null) {
                    if (mCanvasWebViewClientCallback != null) {
                        mCanvasWebViewClientCallback.openMediaFromWebView(mimetype, url, parseFileNameFromContentDisposition(contentDisposition, url));
                    }
                }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }

        addViewTreeObserver();
    }

    private void addViewTreeObserver() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int newVis = getVisibility();
            }
        });
    }

    /**
     * Builds a video enabled WebChromeClient.
     **/
    public void addVideoClient(final Activity activity) {
        mWebChromeClient = new CanvasWebChromeClient(activity, this);
        this.setWebChromeClient(mWebChromeClient);
    }

    private void addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            addJavascriptInterface(new JavascriptInterface(), VideoWebChromeClient.JsInterfaceName);
            addedJavascriptInterface = true;
        }
    }

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    @SuppressWarnings("unused")
    public boolean isVideoFullscreen() {
        return mWebChromeClient != null && mWebChromeClient.isVideoFullscreen();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(getParent() instanceof CoordinatorLayout) {
                mChildHelper = new NestedScrollingChildHelper(this);
                setNestedScrollingEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        // Calling onPause will stop Video's sound, but onResume must be called if resumed, otherwise the second time onPause is called it won't work
        try {
            super.onPause();
        } catch (NullPointerException npe) {
            // Catch for API 16 devices (and perhaps others) and webkit
            Logger.e(npe.getMessage());
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * Create a context menu to copy the link that was pressed and then copy that link to the clipboard
     *
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        final HitTestResult result = this.getHitTestResult();

        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // do the menu action
                if(item.getItemId() == COPY_LINK_ID) {
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText(mContext.getString(R.string.link), result.getExtra());
                        clipboard.setPrimaryClip(clip);

                        // Let the user know
                        Toast.makeText(mContext, mContext.getString(R.string.linkCopied), Toast.LENGTH_SHORT).show();
                    }
                } else if(item.getItemId() == SHARE_LINK_ID) {
                    // Share the link with other apps
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(result.getExtra()));
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    } else {
                        // No apps can do anything with this link, let the user know
                        Toast.makeText(mContext, mContext.getString(R.string.noApps), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        };

        if (result.getType() == HitTestResult.ANCHOR_TYPE ||
                result.getType() == HitTestResult.SRC_ANCHOR_TYPE) {

            // Title of the link, use a custom view so we can show the entire link in the style we want
            TextView title = new TextView(mContext);
            title.setText(result.getExtra());
            title.setTextColor(mContext.getResources().getColor(R.color.canvasTextDark));
            int padding = (int)Utils.dpToPx(mContext, 8);
            title.setPadding(padding*2, padding, padding*2, 0);

            menu.setHeaderView(title);

            // Menu options for a hyperlink.
            // Copy
            menu.add(0, COPY_LINK_ID, 0, mContext.getString(R.string.copyLinkAddress)).setOnMenuItemClickListener(handler);
            // Share with a different app
            menu.add(0, SHARE_LINK_ID, 1, mContext.getString(R.string.shareLink)).setOnMenuItemClickListener(handler);
        }
    }

    /**
     * Handles back presses for the CanvasWebView and the lifecycle of the ActivityContentVideoViewClient
     *
     * Use instead of goBack and canGoBack
     *
     * @return true if handled; false otherwise
     */
    public boolean handleGoBack() {
        if (mWebChromeClient != null && mWebChromeClient.isVideoFullscreen()) {
            return mWebChromeClient.onBackPressed();
        } else if (super.canGoBack()) {
            super.goBack();
            return true;
        }
        return false;
    }

    private void popBackStack() {
        ((AppCompatActivity) getContext()).getSupportFragmentManager().popBackStack();
    }

    @Deprecated
    public static String getRefererDomain(Context context) {
        // Mainly for embedded content such as vimeo, youtube, video tags, iframes, etc
        return ApiPrefs.getFullDomain();
    }

    public static String getReferrer() {
        return ApiPrefs.getDomain();
    }

    public static String getReferrer(boolean shouldIncludeProtocol) {
        if(shouldIncludeProtocol) {
            return ApiPrefs.getFullDomain();
        }
        return getReferrer();
    }

    public static String applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
        if(TextUtils.isEmpty(html)) return "";
        // Fix for embedded videos that have // instead of http://
        html = html.replaceAll("href=\"//", "href=\"https://");
        html = html.replaceAll("href='//", "href='https://");
        html = html.replaceAll("src=\"//", "src=\"https://");
        html = html.replaceAll("src='//", "src='https://");
        return html;
    }


    /**
     * When we parse the HTML if the links don't have a protocol we aren't able to handle them. This
     * will add http:// to any link that doesn't have one
     *
     * @param html
     * @return HTML with protocol added
     */
    public static String addProtocolToLinks(String html) {
        if(TextUtils.isEmpty(html)) return "";

        html = html.replaceAll("href=\"www.", "href=\"https://www.");
        html = html.replaceAll("href='www.", "href='https://www.");
        html = html.replaceAll("src=\"www.", "src=\"https://www.");
        html = html.replaceAll("src='www.", "src='https://www.");
        return html;
    }

    public class CanvasWebChromeClient extends VideoWebChromeClient {

        CanvasWebChromeClient(@NonNull Activity activity, @NonNull CanvasWebView webView) { super(activity, webView); }

        @Override
        public boolean onShowFileChooser(@Nullable WebView webView, @Nullable ValueCallback<Uri[]> filePathCallback, @Nullable FileChooserParams fileChooserParams) {
            return showFileChooser(webView, filePathCallback, fileChooserParams);
        }

        @Override
        public void onProgressChanged(@Nullable WebView view, int newProgress) {
            if(mCanvasWebChromeClientCallback != null) mCanvasWebChromeClientCallback.onProgressChangedCallback(view, newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            PermissionUtilsKt.requestWebPermissions((Activity) getContext(), request);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            // This allows us to handle links in an iFrame when they have a target of '_blank', which tells the WebView
            // to launch the URL in a new window.
            //
            // Without this, Chrome will launch, but doesn't appear to get the URL. This launches the browser and
            // explicitly sends it the URL.
            String url = view.getHitTestResult().getExtra();
            if (url != null && !url.isEmpty()) {
                Context context = view.getContext();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
                return false;
            } else return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
    }

    public class CanvasWebViewClient extends WebViewClient {

        public CanvasWebViewClient() {}

        @androidx.annotation.Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (isStudioDownload(url) && mMediaDownloadCallback != null) {
                String extensionSegment = request.getUrl().getLastPathSegment();
                String extension = "";
                int index = extensionSegment.lastIndexOf(".");
                if (index > 0) {
                    extension = extensionSegment.substring(index);
                }

                mMediaDownloadCallback.downloadMedia("", url, parseFileNameFromContentDisposition(url, null) + extension);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        CanvasWebView.this.stopLoading(); // Hack to stop loading the file in the WebView, since returning an empty response breaks what's being shown
                    }
                });
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return handleShouldOverrideUrlLoading(view, request.getUrl().toString());
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleShouldOverrideUrlLoading(view, url);
        }

        private boolean handleShouldOverrideUrlLoading(WebView view, String url) {
            // Check to see if we need to do anything with the link that was clicked

            // Default headers
            Map<String, String> extraHeaders = Utils.getReferer(getContext());

            if (url.contains("yellowdig") && yellowdigInstalled()) {
                // Pertaining to the Yellowdig LTI:
                //  This is a yellowdig URL, they have a special condition on their end
                //  to not send us a yellowdig URI scheme link if they detect anything
                //  in the 'X-Requested-With' header, so we're putting in a special
                //  case here to intercept and remove the value in that header. The WebView
                //  automatically adds this in and we can't remove it so we just blank it out
                extraHeaders.put("X-Requested-With", "");
            }

            // Check if the URL has a scheme that we aren't handling
            Uri uri = Uri.parse(url);
            if (uri != null && uri.getScheme() != null && !uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                // Special scheme, send URL to app that can handle it
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Verify that the intent will resolve to an activity
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    if (uri.getScheme().equals("yellowdig")) {
                        // Pop off the LTI page so it doesn't try to reload the yellowdig app when going back to our app
                        popBackStack();
                    }

                    getContext().startActivity(intent);
                    return true;
                }
                if (url.startsWith("intent:")) {
                    try {
                        Intent appIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (appIntent.resolveActivity(getContext().getPackageManager()) != null) {
                            getContext().startActivity(appIntent);
                            popBackStack();
                            return true;
                        }
                        //try to find fallback url
                        String fallbackUrl = appIntent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl, extraHeaders);
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        //not an intent uri
                    }
                }
            }

            if (mCanvasWebViewClientCallback != null) {
                //Is the URL something we can link to inside our application?
                if (mCanvasWebViewClientCallback.canRouteInternallyDelegate(url)) {
                    mCanvasWebViewClientCallback.routeInternallyCallback(url);
                    return true;
                }
            }

            // Handle the embedded webview case (Its not within the InternalWebViewFragment)
            if (mCanvasEmbeddedWebViewCallback != null && mCanvasEmbeddedWebViewCallback.shouldLaunchInternalWebViewFragment(url)) {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    String contentTypeGuess = null;
                    try {
                        contentTypeGuess = URLConnection.guessContentTypeFromName(url);
                    } catch (StringIndexOutOfBoundsException e) {
                        Crashlytics.logException(new MalformedURLException("Unable to parse content type of url: " + url));
                    }
                    // null when type can't be determined, launchInternalWebView anyway
                    // When contentType has 'application', it typically means it's a pdf or some type of document that needs to be downloaded,
                    //   so allow the embedded webview to open the url, which will trigger the DownloadListener. If for some reason the content can
                    //   be loaded in the webview, the content will just load in the embedded webview (which isn't ideal, but in majority of cases it won't happen).
                    if (contentTypeGuess == null || !contentTypeGuess.contains("application")) {
                        mCanvasEmbeddedWebViewCallback.launchInternalWebViewFragment(url);
                        return true;
                    }
                } else return true;
            }

            if (url.startsWith("blob:")) return false; // MBL-9546 (Don't remove me, I'll break an LTI tool if you do.)

            view.loadUrl(url, extraHeaders);
            //we're handling the url ourselves, so return true.
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageStartedCallback(view, url);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            // Clear the history if formatHtml was called more than once. Refer to formatHtml's NOTE
            if (url.startsWith(getHtmlAsUrl("", encoding))) {
                view.clearHistory();
            }
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageFinishedCallback(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl != null && failingUrl.startsWith("file://")) {
                failingUrl = failingUrl.replaceFirst("file://", "https://");
                view.loadUrl(failingUrl, Utils.getReferer(getContext()));
            }
        }
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        addJavascriptInterface();
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        addJavascriptInterface();
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(String url) {
        addJavascriptInterface();
        super.loadUrl(url);
    }

    /**
     * Makes html content somewhat suitable for mobile
     *
     * NOTE: The web history is cleared when formatHtml is called. Only the loaded page will appear in the webView.copyBackForwardList()
     *       Back history will not work with multiple pages. This allows for formatHtml to be called several times without causing the user to
     *          press back 2 or 3 times.
     *
     * @param html
     * @param contentDescription
     * @return
     */
    public String loadHtml(String html, String contentDescription) {
        String result = formatHtml(html);
        this.loadDataWithBaseURL(CanvasWebView.getReferrer(true), result, "text/html", encoding, getHtmlAsUrl(result, encoding));
        setupAccessibilityContentDescription(result, contentDescription);
        return result;
    }

    /**
     * Helper function that makes html content somewhat suitable for mobile
     */
    public String formatHtml(String html) {
        String htmlWrapper = FileUtils.getAssetsFile(mContext, "html_wrapper.html");
        html = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(html);
        html = CanvasWebView.addProtocolToLinks(html);
        html = checkForMathTags(html);
        return htmlWrapper.replace("{$CONTENT$}", html);
    }

    /**
     * Loads the provided HTML string without modification
     * @param html The raw HTML to load
     * @param contentDescription The content description of the HTML
     */
    public void loadRawHtml(String html, String contentDescription) {
        this.loadDataWithBaseURL(CanvasWebView.getReferrer(true), html, "text/html", encoding, getHtmlAsUrl(html, encoding));
        setupAccessibilityContentDescription(html, contentDescription);
    }

    /*
     *  Work around for API 16 devices (and perhaps others). When pressing back the webview was loading 'about:blank' instead of the custom html
     */
    private String getHtmlAsUrl(String html, String encoding) {
        return String.format("data:text/html; charset=%s, %s", encoding, html);
    }

    @NonNull
    private String checkForMathTags(String content) {
        // If this html that we're about to load has a math tag and isn't just an image we want to parse it with MathJax.
        // This is the version that web currently uses (the 2.7.1 is the version number) and this is the check that they do to
        // decide if they'll run the MathJax script on the webview
        if(content.contains("<math") && !content.contains("<img class='equation_image'")) {
            content = "<script type=\"text/javascript\"\n" +
                    "                src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n" +
                    "        </script>" + content;
        }
        return content;
    }


    public static boolean isStudioDownload(String url) {
        return url.contains("instructuremedia.com/fetch/") && url.contains("disposition=download");
    }

    public static boolean containsStudioLTI(@NonNull String html, String encoding) {
        // BaseURL is set as Referer. Referer needed for some Vimeo videos to play
        // Studio needs the protocol attached to the referrer, so use that if we're using Studio
        try {
            //sanitize the html
            String sanitized = html.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            if (URLDecoder.decode(sanitized, encoding).contains("instructuremedia.com/lti/launch")) return true;
        } catch (UnsupportedEncodingException e) { /* do nothing */ }
        return false;
    }

    public static boolean containsLTI(@NonNull String html, String encoding) {
        // BaseURL is set as Referer. Referer needed for some Vimeo videos to play
        // Studio needs the protocol attached to the referrer, so use that if we're using Studio
        try {
            //sanitize the html
            String sanitized = html.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            if (URLDecoder.decode(sanitized, encoding).contains("/external_tools/retrieve")) return true;
        } catch (UnsupportedEncodingException e) { /* do nothing */ }
        return false;
    }

    /**
     * Check if the Yellowdig app is installed by checking
     * to see if there is an app that handles the yellowdig URI scheme.
     * @return True if installed, false if not
     */
    private boolean yellowdigInstalled() {
        Uri uri = Uri.parse("yellowdig://");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return intent.resolveActivity(getContext().getPackageManager()) != null;
    }

    private void setupAccessibilityContentDescription(String formattedHtml, String title) {
        //Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        String contentDescription = formattedHtml;
        if (title != null) {
            contentDescription = title + " " + formattedHtml;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.setContentDescription(APIHelper.INSTANCE.simplifyHTML(Html.fromHtml(contentDescription, Html.FROM_HTML_MODE_LEGACY)));
        } else {
            this.setContentDescription(APIHelper.INSTANCE.simplifyHTML(Html.fromHtml(contentDescription)));
        }
    }

    // region Getter & Setters

    public CanvasEmbeddedWebViewCallback getCanvasEmbeddedWebViewCallback() {
        return mCanvasEmbeddedWebViewCallback;
    }

    public void setCanvasEmbeddedWebViewCallback(CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback) {
        this.mCanvasEmbeddedWebViewCallback = mCanvasEmbeddedWebViewCallback;
    }

    public CanvasWebViewClientCallback getCanvasWebViewClientCallback() {
        return mCanvasWebViewClientCallback;
    }

    public void setCanvasWebViewClientCallback(CanvasWebViewClientCallback canvasWebViewClientCallback) {
        this.mCanvasWebViewClientCallback = canvasWebViewClientCallback;
    }

    public CanvasWebChromeClientCallback getCanvasWebChromeClientCallback() {
        return mCanvasWebChromeClientCallback;
    }

    public void setCanvasWebChromeClientCallback(CanvasWebChromeClientCallback mCanvasWebChromeClientCallback) {
        this.mCanvasWebChromeClientCallback = mCanvasWebChromeClientCallback;
    }

    public void setCanvasWebChromeClientShowFilePickerCallback(VideoPickerCallback callback) {
        this.getSettings().setAllowFileAccess(true);
        this.mVideoPickerCallback = callback;
    }

    public void setMediaDownloadCallback(MediaDownloadCallback callback) {
        this.mMediaDownloadCallback = callback;
    }

    // endregion


    //region Video Picking for WebView (Used for Studio)

    public void clearPickerCallback() {
        mFilePathCallback = null;
    }

    // For Android 5.0+
    private boolean showFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

        // make sure we have permissions first

        if(mVideoPickerCallback != null && mVideoPickerCallback.permissionsGranted()) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;
            startFileChooser(VIDEO_PICKER_RESULT_CODE, fileChooserParams);
            return true;
        }

        return false;
    }

    // Allow users to pick files for webview upload. Most common usage is for Studio uploads. Modified to allow file
    // chooser params so other LTI's can also be used (like turnitin).
    private void startFileChooser(final int requestCode, WebChromeClient.FileChooserParams fileChooserParams) {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);

        String[] extraMimeTypes = fileChooserParams.getAcceptTypes();
        if (extraMimeTypes.length > 0 && !extraMimeTypes[0].trim().isEmpty()) {
            // An array with one blank element will clear out the allowed mime types,
            // so we only want to add extra mime types if we are given any valid types
            fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
        }

        fileIntent.setType("*/*");

        // Determine if we can upload video files so we can add the camera to the list of choices
        boolean allowRecording = false;
        for (String type : fileChooserParams.getAcceptTypes()) {
            if (type.equalsIgnoreCase("video/*") || type.equalsIgnoreCase("video/mp4")) {
                allowRecording = true;
                break;
            }
        }

        final List<Intent> cameraIntents = new ArrayList<>();
        if (allowRecording) {
            String fileName = "vid_" + System.currentTimeMillis() + ".mp4";
            File file = new File(FileUploadUtils.getExternalCacheDir(mContext), fileName);

            Uri cameraImageUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file);
            // Camera.
            final Intent captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            final PackageManager packageManager = mContext.getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                cameraIntents.add(intent);
            }
        }
        // Chooser of filesystem options.
        final String title = TextUtils.isEmpty(fileChooserParams.getTitle())
                ? getContext().getString(R.string.pickFile)
                : fileChooserParams.getTitle().toString();
        final Intent chooserIntent = Intent.createChooser(fileIntent, title);

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[0]));

        mVideoPickerCallback.requestStartActivityForResult(chooserIntent, requestCode);
    }

    public boolean handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VIDEO_PICKER_RESULT_CODE && mFilePathCallback != null) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri[] results = new Uri[]{data.getData()};
                mFilePathCallback.onReceiveValue(results);
            } else {
                mFilePathCallback.onReceiveValue(null);
            }
        }

        clearPickerCallback();

        return true;
    }

    //endregion

    //region Nested Scrolling Child

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mChildHelper == null) return super.onTouchEvent(ev);

        boolean returnValue = false;

        MotionEvent event = MotionEvent.obtain(ev);
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }
        int eventY = (int) event.getY();
        event.offsetLocation(0, mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastY - eventY;
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    mLastY = eventY - mScrollOffset[1];
                    event.offsetLocation(0, -mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                }
                returnValue = super.onTouchEvent(event);

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0, mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                    mLastY -= mScrollOffset[1];
                }
                break;
            case MotionEvent.ACTION_DOWN:
                returnValue = super.onTouchEvent(event);
                if (firstScroll) {
                    // dispatching first down scrolling properly by making sure that first deltaY will be -ve
                    mLastY = eventY - 5;
                    firstScroll = false;
                } else {
                    mLastY = eventY;
                }
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            default:
                returnValue = super.onTouchEvent(event);
                // end NestedScroll
                stopNestedScroll();
                break;
        }
        return returnValue;
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        if(mChildHelper != null) mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper != null && mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper != null && mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        if(mChildHelper != null) mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper != null && mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper != null && mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper != null && mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper != null && mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    //endregion
}

