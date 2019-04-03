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

package com.instructure.speedgrader.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.instructure.canvasapi.api.compatibility_synchronous.APIHttpResponse;
import com.instructure.canvasapi.api.compatibility_synchronous.HttpHelpers;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.util.HtmlMediaHelper;

public class InternalWebviewActivity extends ParentActivity {

    //logic variables
    private String url;
    private String html;

    //view variables
    private WebView webView;

    //async task
    private CheckURLAsyncTask checkUrl;

    private boolean authenticate;
    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        //Hacky way to pause audio in webview
        HtmlMediaHelper.pauseWebviewVideo(webView, InternalWebviewActivity.this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayout());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        //WebChromeClient required to play inline video
        webView.setWebChromeClient(new WebChromeClient() {
        });

        //clear the cookies to verify that we're working with a fresh session
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        //needs to be here due to calling getSherlockActivity()
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {

            }
        });

        loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        }
        else {
            finish();
            overridePendingTransition(0, R.anim.slide_up);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.slide_up);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides for ParentActivity
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getRootLayout() {
        return R.layout.fragment_internal_webview;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Logic
    ///////////////////////////////////////////////////////////////////////////

    public boolean canGoBack() {
        if(webView == null) {
            return false;
        }
        else return webView.canGoBack();
    }

    protected void loadUrl(String url) {
        if(html != null){
            loadHtml(html);
            return;
        }
        this.url = url;
        if(!TextUtils.isEmpty(url)) {
            if (authenticate) {
                new CheckURLAsyncTask(url).execute();
            } else {
                webView.loadUrl(url, Utils.getReferer(getContext()));
            }
        }
    }
    public void loadHtml(String html) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        webView.loadDataWithBaseURL(APIHelpers.getFullDomain(getContext()), APIHelpers.getAssetsFile(getContext(), "html_text_submission_wrapper.html").replace("{$CONTENT$}", html), "text/html", "UTF-8", null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AsyncTask
    ///////////////////////////////////////////////////////////////////////////

    public class CheckURLAsyncTask extends AsyncTask<Void, Void, Integer> {


        private String urlString = "";

        public CheckURLAsyncTask(String urlString) {
            this.urlString = urlString;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            APIHttpResponse response = HttpHelpers.externalHttpGet(getContext(), urlString, false);
            return response.responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if(responseCode != null && responseCode > 401) {
                webView.loadUrl(url, CanvasAPI.getAuthenticatedURL(getContext()));
            } else {
                webView.loadUrl(url, Utils.getReferer(getContext()));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void handleIntent(Intent intent) {
        url = intent.getExtras().getString(Const.INTERNAL_URL);
        authenticate = intent.getExtras().getBoolean(Const.AUTHENTICATE);
        html = intent.getExtras().getString(Const.HTML);
    }

    public static Bundle createBundle(String url, boolean authenticate, String html) {
        Bundle extras = new Bundle();
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.HTML, html);
        return extras;
    }

    public static Intent createIntent(Context context, String url, boolean authenticate) {
        Bundle bundle = createBundle(url, authenticate, null);
        Intent intent = new Intent(context, InternalWebviewActivity.class);
        intent.putExtras(bundle);
        return intent;
    }


}
