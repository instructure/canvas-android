/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.DocumentActivity;
import com.instructure.speedgrader.adapters.DocumentPagerAdapter;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.views.CircularProgressBar;
import com.instructure.speedgrader.views.DocumentWebView;
import com.instructure.speedgrader.views.HelveticaTextView;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import retrofit.client.Response;

public class SubmissionWebViewFragment extends BaseSubmissionView {

    public DocumentWebView webView;
    private View rootView;
    RelativeLayout loadingView;

    @Override
    public int getRootLayout() {
        return R.layout.fragment_document_view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflateLayout(inflater, container);

        initLoadingView(rootView);
        initWebView(rootView);
        setupCallbacks();
        if(currentSubmission != null) {
            String onlineUpload     = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD);
            String onlineTextEntry  = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_TEXT_ENTRY);
            String onlineQuiz       = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ);
            String submissionType   = currentSubmission.getSubmissionType();

            if(currentSubmission.getSubmissionType() != null && currentSubmission.getSubmissionType().equals(onlineTextEntry)){
                setURLText();
            }
            else if (currentSubmission.getSubmissionType() != null && submissionType.equals(onlineUpload) && currentSubmission.getAttachments().size() > 0) {
                displayAttachment(currentSubmission.getAttachments().get(0));
            }
            else if(!DocumentPagerAdapter.isEmptySubmission(currentSubmission) && submissionType.equals(onlineQuiz)) {
                webView.loadUrl(currentSubmission.getPreviewUrl());
            }
            else {
                webView.loadUrl(currentSubmission.getUrl());
            }
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        //zoom controls causing memory leak
        webView.getSettings().setBuiltInZoomControls(false);
        super.onDestroy();
    }

    //this gets called when the webview becomes visible in the viewpager
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && currentSubmission != null) {
            if(currentSubmission.getSubmissionType() != null && currentSubmission.getSubmissionType().equals(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ))) {
                if (webView != null) {
                    //if the webview is the same it means we're on the login page still, the getPreviewUrl redirects to another page
                    if(currentSubmission.getPreviewUrl().equals(webView.getUrl())) {
                        //check to see if the webview is authorized already
                        new CheckURLAsyncTask().execute(currentSubmission.getPreviewUrl());
                    }
                }
            }
        }
    }

    public class CheckURLAsyncTask extends AsyncTask<String,Void, Integer> {

        private String url;
        @Override
        protected void onPreExecute(){}

        @Override
        protected Integer doInBackground(String... params) {
            try {
                //check what the webpage that we're going to will display so we can figure out if
                //we need to authenticate it.

                url = params[0];

                URL url = new URL(this.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.addRequestProperty(getString(R.string.cookie), getCookieFromAppCookieManager());
                conn.setConnectTimeout(30000);

                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();

                return code;

            }
            catch(ProtocolException e) {
//                we can't even get to it due to authentication required, so catch the exception
//                and make the webview require authentication
                if(e.getMessage().equals(getString(R.string.authorizationRequired))) {
                    return 401;
                }
                else {
                    return null;
                }
            }
            catch(Exception E) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            //Null pointer check
            if(getActivity() == null){return;}

            if(result != null) {
                //if the value is 200 that means the cookies say we have been authenticated, so reload the webview so it doesn't show
                //the login screen when we've already logged in on one of the fragments in the viewpager
                if(result.intValue() == 200) {
                    webView.loadUrl(currentSubmission.getPreviewUrl());
                }
                else if(result.intValue() == 401) {
                    //if it's a 401, we want to remove the session cookie. If we don't remove it the cookie can sometimes be the cookie
                    //for a different webview in the view pager, which means it would load the url for the wrong user
                    CookieSyncManager.createInstance(getActivity());
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeSessionCookie();
                    CookieSyncManager.getInstance().sync();
                }
            }
        }
    }

    /**
     * For Crocodoc files, we use javascript to override some CSS styles. Since Javascript is so slow, it's a bit ugly since the user can see
     * the style changes taking place, and the view isn't scrollable until the changes take place. For Crocodoc files, we display a loading image
     * over the webview, then use a javascript interface to remove this loadingview when changes complete.
     * @param rootView
     */
    private void initLoadingView(View rootView) {
        loadingView = (RelativeLayout) rootView.findViewById(R.id.webViewLoading);
        ((HelveticaTextView)loadingView.findViewById(R.id.emptyViewText)).setText(getString(R.string.loadingIndeterminate));
        CircularProgressBar progressBar = (CircularProgressBar) loadingView.findViewById(R.id.circularProgressBar);
        progressBar.setColor(CanvasContextColor.getCachedColor(getContext(), getCanvasContext().getContextId()));
    }

    public void initWebView(View rootView){
        webView = (DocumentWebView) rootView.findViewById(R.id.documentWebView);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.requestFocusFromTouch();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.addJavascriptInterface(new JsObject(webView, loadingView), "CallToAnAndroidFunction");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("?login_success=1")) {
                    view.loadUrl(currentSubmission.getPreviewUrl());
                    return true;
                } else if(url.contains("score_updated=1")){
                    getSubmissionAndUpdateRubric();
                }
                else {
                    view.loadUrl(url);
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!isAdded()) {return;}
                if (!((App) getActivity().getApplication()).showStudentNames()) {
                    view.loadUrl("javascript:"
                            + "var css = document.createElement('style');"
                            + "css.type = 'text/css';"
                            + "css.innerHTML = '.quiz-header h2{ visibility: hidden !important; }';"
                            + "document.getElementById('content').appendChild(css);");
                }

            }
        });
    }

    private void getSubmissionAndUpdateRubric(){
        if(getActivity() != null && getActivity() instanceof DocumentActivity){
            SubmissionAPI.getSubmission(getCanvasContext(), submission.getAssignment_id(), submission.getUser_id(), new CanvasCallback<Submission>((DocumentActivity) getActivity()) {
                @Override
                public void cache(Submission submission) {
                }

                @Override
                public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                    ((DocumentActivity) getActivity()).updateRubricSubmissionInfo(submission);
                }
            });
        }
    }

    /**
     *  Called by our js function when our changes to Crocodoc are completed. Hides our loading view and shows the webview.
     * */
    public class JsObject {
        private View loadingView;
        private View view;
        JsObject(View view, View loadingView){this.view = view;this.loadingView = loadingView;}
        @JavascriptInterface
        public void setVisible(){
            if(isAdded()) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        view.setVisibility(View.VISIBLE);
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    public void setURL(String url){
        getActivity().setProgressBarIndeterminateVisibility(false);
        //load the html framework that we want to display
        String html = CanvasAPI.getAssetsFile(getActivity(), "submission_online_text.html");
        //replace the body of the webpage with the content of their assignment
        html = html.replace("__BODY__", url);
        if(!html.equals("")) {
            webView.loadDataWithBaseURL(null, html, null, "utf-8", null);
        }
    }

    public void setURLText(){
       getActivity().setProgressBarIndeterminateVisibility(false);
       setURL(currentSubmission.getBody());
    }

    public void displayAttachment(Attachment attachment){
        if(!isAdded()){return;}
        getActivity().setProgressBarIndeterminateVisibility(true);
        if(attachmentIsImage(attachment)){
            loadImage(attachment);
        }else{
            loadDocument(attachment);
        }
    }

    private void loadDocument(Attachment attachment){
        getActivity().setProgressBarIndeterminateVisibility(false);
        webView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        String attachmentDocumentURL = APIHelpers.getFullDomain(getContext()) + attachment.getPreviewURL();

        // add authorization header to webview. The url provided in the api will redirect to a box address and therefore needs an authorization token
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + String.valueOf(APIHelpers.getToken(getContext())));

        webView.loadUrl(attachmentDocumentURL, headers);
    }

    private void loadImage(Attachment attachment){
        getActivity().setProgressBarIndeterminateVisibility(false);
        String attachmentImageURL = "<img id=\"resizeImage\" src=\" " +attachment.getUrl() +"\" width=\"100%\" alt=\"\" />\n";
        setURL(attachmentImageURL);
    }

    public boolean attachmentIsImage(Attachment attachment){
        if(attachment.getMimeType().contains("image")){
            return true;
        }
        return false;
    }

    public String getCookieFromAppCookieManager() throws MalformedURLException {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null)
            return null;
        String rawCookieHeader = null;

        // Extract Set-Cookie header value from Android app CookieManager for this URL.
        // NOTE: we need the full domain (including the https://)
        rawCookieHeader = cookieManager.getCookie(APIHelpers.getFullDomain(getActivity()));
        if (rawCookieHeader == null)
            return null;
        return rawCookieHeader;
    }
}