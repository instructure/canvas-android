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
 */
package com.instructure.loginapi.login.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.instructure.canvasapi2.RequestInterceptor;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.canvasapi2.models.OAuthTokenResponse;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.Analytics;
import com.instructure.canvasapi2.utils.AnalyticsEventConstants;
import com.instructure.canvasapi2.utils.AnalyticsParamConstants;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.api.MobileVerifyAPI;
import com.instructure.loginapi.login.dialog.AuthenticationDialog;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.snicker.SnickerDoodle;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.PreviousUsersUtils;
import com.instructure.pandautils.utils.ViewStyler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

import static com.instructure.loginapi.login.util.Const.CANVAS_LOGIN_FLOW;
import static com.instructure.loginapi.login.util.Const.MASQUERADE_FLOW;
import static com.instructure.loginapi.login.util.Const.MOBILE_VERIFY_FLOW;
import static com.instructure.loginapi.login.util.Const.SNICKER_DOODLES;

public abstract class BaseLoginSignInActivity extends AppCompatActivity implements AuthenticationDialog.OnAuthenticationSet {

    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    protected abstract Intent launchApplicationMainActivityIntent();
    protected abstract void refreshWidgets();
    protected abstract String userAgent();

    protected static final String ACCOUNT_DOMAIN = "accountDomain";

    protected static final String SUCCESS_URL = "/login/oauth2/auth?code=";
    protected static final String ERROR_URL = "/login/oauth2/auth?error=access_denied";

    private WebView mWebView;

    private int mCanvasLogin = 0;
    boolean mSpecialCase = false;
    private String mAuthenticationURL;
    private HttpAuthHandler mHttpAuthHandler;
    private AccountDomain mAccountDomain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mCanvasLogin = getIntent().getExtras().getInt(Const.CANVAS_LOGIN, 0);
        setupViews();
        applyTheme();
        beginSignIn(getAccountDomain());
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getAccountDomain().getDomain());
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setAutoMirrored(true);
        }
        toolbar.setNavigationContentDescription(R.string.close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mWebView = findViewById(R.id.webView);

        clearCookies();
        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setUserAgentString(com.instructure.pandautils.utils.Utils.generateUserAgent(this, userAgent()));
        mWebView.setWebViewClient(mWebViewClient);

        if((0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    private void applyTheme() {
        ViewStyler.setStatusBarLight(this);
    }

    /**
     * Override to handle the shouldOverrideUrlLoading() method.
     * @param view WebView
     * @param url Url String
     * @return If overriding this method it is expected to return true, if false the default behavior
     * of the BaseLoginSignInActivity will handle the override.
     */
    protected boolean overrideUrlLoading(WebView view, String url) {
        return false;
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
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
            if(overrideUrlLoading(view, url)) return true;

            if (url.contains(SUCCESS_URL)) {
                ApiPrefs.setDomain(getAccountDomain().getDomain());
                String oAuthRequest = url.substring(url.indexOf(SUCCESS_URL) + SUCCESS_URL.length());
                OAuthManager.getToken(ApiPrefs.getClientId(), ApiPrefs.getClientSecret(), oAuthRequest, mGetTokenCallback);
            } else if (url.contains(ERROR_URL)) {
                clearCookies();
                view.loadUrl(mAuthenticationURL, getHeaders());
            } else {
                view.loadUrl(url, getHeaders());
            }

            return true; // then it is not handled by default action
        }

        @SuppressWarnings("deprecation")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            handleShouldInterceptRequest(url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                handleShouldInterceptRequest(request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }

        private void handleShouldInterceptRequest(String url) {
            if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                mSpecialCase = true;
                ApiPrefs.setDomain(getAccountDomain().getDomain());
                String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                OAuthManager.getToken(ApiPrefs.getClientId(), ApiPrefs.getClientSecret(), oAuthRequest, mGetTokenCallback);
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            mHttpAuthHandler = handler;
            AuthenticationDialog.newInstance(getAccountDomain().getDomain()).show(getSupportFragmentManager(), AuthenticationDialog.class.getSimpleName());
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (errorResponse.getStatusCode() == 400 &&
                        mAuthenticationURL != null && request != null && request.getUrl() != null &&
                        mAuthenticationURL.equals(request.getUrl().toString())) {
                    //If the institution does not support skipping the authentication screen this will catch that error and force the
                    //rebuilding of the authentication url with the authorization screen flow. Example: canvas.sfu.ca
                    buildAuthenticationUrl(ApiPrefs.getProtocol(), getAccountDomain(), ApiPrefs.getClientId(), true);
                    loadAuthenticationUrl(ApiPrefs.getProtocol(), getAccountDomain().getDomain());
                }
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if(mAuthenticationURL != null && url != null && mAuthenticationURL.equals(url)){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
                    //If the institution does not support skipping the authentication screen this will catch that error and force the
                    //rebuilding of the authentication url with the authorization screen flow. Example: canvas.sfu.ca
                    view.evaluateJavascript(
                            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    if (value != null && value.contains("redirect_uri does not match client settings")) {
                                        buildAuthenticationUrl(ApiPrefs.getProtocol(), getAccountDomain(), ApiPrefs.getClientId(), true);
                                        mWebView.loadUrl("about:blank");
                                        loadAuthenticationUrl(ApiPrefs.getProtocol(), getAccountDomain().getDomain());
                                    }
                                }
                            });
                }
            }
        }
    };

    public AccountDomain getAccountDomain() {
        if (mAccountDomain == null) {
            mAccountDomain = getIntent().getParcelableExtra(ACCOUNT_DOMAIN);
        }
        return mAccountDomain;
    }

    protected void beginSignIn(final AccountDomain accountDomain) {
        final String url = accountDomain.getDomain();
        if(mCanvasLogin == MOBILE_VERIFY_FLOW) { //Skip Mobile Verify
            final View view = LayoutInflater.from(BaseLoginSignInActivity.this).inflate(R.layout.dialog_skip_mobile_verify, null);
            final EditText protocolEditText = view.findViewById(R.id.mobileVerifyProtocol);
            final EditText clientIdEditText = view.findViewById(R.id.mobileVerifyClientId);
            final EditText clientSecretEditText = view.findViewById(R.id.mobileVerifyClientSecret);

            final AlertDialog dialog = new AlertDialog.Builder(BaseLoginSignInActivity.this)
                    .setTitle(R.string.mobileVerifyDialogTitle)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiPrefs.setProtocol(protocolEditText.getText().toString());
                            ApiPrefs.setDomain(url);
                            ApiPrefs.setClientId(clientIdEditText.getText().toString());
                            ApiPrefs.setClientSecret(clientSecretEditText.getText().toString());
                            buildAuthenticationUrl(protocolEditText.getText().toString(), accountDomain, ApiPrefs.getClientId(), false);
                            mWebView.loadUrl(mAuthenticationURL, getHeaders());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobileVerifyAPI.mobileVerify(url, mMobileVerifyCallback);
                        }
                    })
                    .create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                }
            });
            dialog.show();
        } else {
            MobileVerifyAPI.mobileVerify(url, mMobileVerifyCallback);
        }
    }

    @Override
    public void onRetrieveCredentials(String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            if (mHttpAuthHandler != null) {
                mHttpAuthHandler.proceed(username, password);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show();
        }
    }

    protected void clearCookies() {
        CookieManager.getInstance().removeAllCookies(null);
    }

    public Map<String, String> getHeaders() {
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("accept-language", RequestInterceptor.Companion.getAcceptedLanguageString());
        extraHeaders.put("user-agent", com.instructure.pandautils.utils.Utils.generateUserAgent(this, userAgent()));
        extraHeaders.put("session_locale", Locale.getDefault().getLanguage());
        return extraHeaders;
    }

    //region Callbacks

    StatusCallback<DomainVerificationResult> mMobileVerifyCallback = new StatusCallback<DomainVerificationResult>() {

        @Override
        public void onResponse(@NonNull Response<DomainVerificationResult> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
            if(type.isCache()) return;

            DomainVerificationResult domainVerificationResult = response.body();

            if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.Success) {
                //Domain is now verified.
                //save domain to the preferences.
                String domain = "";

                //mobile verify can change the hostname we need to use
                if (domainVerificationResult.getBase_url() != null && !domainVerificationResult.getBase_url().equals("")) {
                    domain = domainVerificationResult.getBase_url();
                } else {
                    domain = getAccountDomain().getDomain();
                }

                if (domain.endsWith("/")) {
                    domain = domain.substring(0, domain.length() - 1);
                }

                mAccountDomain.setDomain(domain);

                ApiPrefs.setClientId(domainVerificationResult.getClient_id());
                ApiPrefs.setClientSecret(domainVerificationResult.getClient_secret());


                //Get the protocol
                final String apiProtocol = domainVerificationResult.getProtocol();

                //Set the protocol
                ApiPrefs.setProtocol(domainVerificationResult.getProtocol());

                buildAuthenticationUrl(apiProtocol, getAccountDomain(), ApiPrefs.getClientId(), false);
                loadAuthenticationUrl(apiProtocol, domain);
            } else {
                //Error message
                int errorId;

                if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.GeneralError) {
                    errorId = R.string.mobileVerifyGeneral;
                } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized) {
                    errorId = R.string.mobileVerifyDomainUnauthorized;
                } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.UnknownUserAgent) {
                    errorId = R.string.mobileVerifyUserAgentUnauthorized;
                } else {
                    errorId = R.string.mobileVerifyUnknownError;
                }

                if(!(BaseLoginSignInActivity.this).isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BaseLoginSignInActivity.this);
                    builder.setTitle(R.string.errorOccurred);
                    builder.setMessage(errorId);
                    builder.setCancelable(true);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    };

    final protected void loadAuthenticationUrl(final String apiProtocol, final String domain, final String clientId, final String clientSecret) {
        ApiPrefs.setClientId(clientId);
        ApiPrefs.setClientSecret(clientSecret);
        ApiPrefs.setToken(""); // TODO: Remove when we're 100% using refresh tokens
        loadAuthenticationUrl(apiProtocol, domain);
    }

    final protected void loadAuthenticationUrl(final String apiProtocol, final String domain) {
        if (mCanvasLogin == CANVAS_LOGIN_FLOW) {
            mAuthenticationURL += "&canvas_login=1";
        } else if (mCanvasLogin == MASQUERADE_FLOW) {
            // canvas_sa_delegated=1    identifies that we want to masquerade
            CookieManager cookieManager = CookieManager.getInstance();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebView, true);
            }
            if (domain.contains(".instructure.com")) {
                String cookie = "canvas_sa_delegated=1;domain=.instructure.com;path=/;";
                cookieManager.setCookie(apiProtocol + "://" + domain, cookie);
                cookieManager.setCookie(".instructure.com", cookie);
            } else {
                cookieManager.setCookie(domain, "canvas_sa_delegated=1");
            }
        }
        mWebView.loadUrl(mAuthenticationURL, getHeaders());

        if((0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
            mWebView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(getIntent().hasExtra(SNICKER_DOODLES)) {
                        SnickerDoodle snickerDoodle = getIntent().getParcelableExtra(SNICKER_DOODLES);
                        populateWithSnickerDoodle(snickerDoodle);
                    }
                }
            }, 1500);
        }
    }

    final protected void setAuthenticationUrl(String authenticationUrl) {
        mAuthenticationURL = authenticationUrl;
    }

    final protected void buildAuthenticationUrl(String protocol, AccountDomain accountDomain, String clientId, boolean forceAuthRedirect) {
        //Get device name for the login request.
        String deviceName = Build.MODEL;
        if (deviceName == null || deviceName.equals("")) {
            deviceName = getString(R.string.unknownDevice);
        }
        // Remove spaces
        deviceName = deviceName.replace(" ", "_");
        // Changed for the online update to have an actual formatted login page

        String domain = accountDomain.getDomain();

        if (domain != null && domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        Uri.Builder builder = new Uri.Builder()
                .scheme(protocol)
                .authority(domain)
                .appendPath("login")
                .appendPath("oauth2")
                .appendPath("auth")
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("mobile", "1")
                .appendQueryParameter("purpose", deviceName);

        if(forceAuthRedirect || mCanvasLogin == MOBILE_VERIFY_FLOW || (domain != null && domain.contains(".test."))) {
            //Skip mobile verify
            builder.appendQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
        } else {
            builder.appendQueryParameter("redirect_uri", "https://canvas.instructure.com/login/oauth2/auth");
        }

        //If an authentication provider is supplied we need to pass that along. This should only be appended if one exists.
        String authenticationProvider = accountDomain.getAuthenticationProvider();
        if(authenticationProvider != null && authenticationProvider.length() > 0 && !authenticationProvider.equalsIgnoreCase("null")) {
            Logger.d("authentication_provider=" + authenticationProvider);
            builder.appendQueryParameter("authentication_provider", authenticationProvider);
        }

        Uri authUri = builder.build();

        if((0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
            Logger.d("AUTH URL: " + authUri.toString());
        }
        mAuthenticationURL = authUri.toString();
    }

    private StatusCallback<OAuthTokenResponse> mGetTokenCallback = new StatusCallback<OAuthTokenResponse>() {

        @Override
        public void onResponse(@NonNull Response<OAuthTokenResponse> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
            if (type.isCache()) return;

            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParamConstants.DOMAIN_PARAM, ApiPrefs.getDomain());

            Analytics.logEvent(AnalyticsEventConstants.LOGIN_SUCCESS, bundle);

            final OAuthTokenResponse token = response.body();
            ApiPrefs.setRefreshToken(token.getRefreshToken());
            ApiPrefs.setAccessToken(token.getAccessToken());
            ApiPrefs.setToken(""); // TODO: Remove when we're 100% using refresh tokens

            // We now need to get the cache user
            UserManager.getSelf(new StatusCallback<User>() {

                @Override
                public void onResponse(@NonNull Response<User> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                    if(type.isAPI()) {
                        ApiPrefs.setUser(response.body());
                        User userResponse = response.body();
                        String domain = ApiPrefs.getDomain();
                        String protocol = ApiPrefs.getProtocol();

                        SignedInUser user = new SignedInUser(
                            userResponse,
                            domain,
                            protocol,
                            "", // TODO - delete once we move over 100% to refresh tokens
                            token.getAccessToken(),
                            token.getRefreshToken(),
                            null,
                            null
                        );
                        PreviousUsersUtils.add(BaseLoginSignInActivity.this, user);

                        refreshWidgets();

                        handleLaunchApplicationMainActivityIntent();
                    }
                }
            });
        }

        @Override
        public void onFail(@Nullable Call<OAuthTokenResponse> call, @NonNull Throwable error, @Nullable Response<?> response) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParamConstants.DOMAIN_PARAM, ApiPrefs.getDomain());

            Analytics.logEvent(AnalyticsEventConstants.LOGIN_FAILURE, bundle);

            if (!mSpecialCase) {
                Toast.makeText(BaseLoginSignInActivity.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
            } else {
                mSpecialCase = false;
            }

            mWebView.loadUrl(mAuthenticationURL, getHeaders());
        }
    };

    /**
     * Override and do not call super if you need additional logic before launching the main activity intent.
     * It is expected that the class overriding will launch an intent.
     */
    protected void handleLaunchApplicationMainActivityIntent() {
        Intent intent = launchApplicationMainActivityIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //endregion

    //region Snicker Doodles

    private void populateWithSnickerDoodle(SnickerDoodle snickerDoodle) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());

        final String js = "javascript: { " +
                    "document.getElementsByName('pseudonym_session[unique_id]')[0].value = '" + snickerDoodle.getUsername() + "'; " +
                    "document.getElementsByName('pseudonym_session[password]')[0].value = '" + snickerDoodle.getPassword() + "'; " +
                    "document.getElementsByClassName('Button')[0].click(); " +
                "};";

        mWebView.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {}
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String js =
                                "javascript: { " +
                                    "document.getElementsByClassName('btn')[0].click();" +
                                "};";

                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
                        mWebView.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {}
                        });
                    }
                });
            }
        }, 750);
    }

    //endregion
}
