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

package com.instructure.loginapi.login;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.instructure.canvasapi.api.OAuthAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.OAuthToken;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.canvasapi.utilities.UserCallback;
import com.instructure.loginapi.login.adapter.SnickerDoodleAdapter;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.loginapi.login.api.MobileVerifyAPI;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.snicker.SnickerDoodle;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.SavedDomains;
import com.instructure.loginapi.login.util.Utils;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class OAuthWebLogin extends AppCompatActivity {

    private String url;
    private String authenticationURL;

    private FrameLayout mContainer;
    private TextView mDomainText;

    private WebView web;
    private static boolean factoryInit = false;

    private String successURL = "/login/oauth2/auth?code=";
    private String errorURL = "/login/oauth2/auth?error=access_denied";

    private String client_id;
    private String client_secret;
    private String api_protocol;

	private int canvas_login = 0;

    boolean specialCase = false;

    private HttpAuthHandler httpAuthHandler;

    private CanvasCallback<DomainVerificationResult> mobileVerifyCallback;
    private CanvasCallback<OAuthToken>getToken;

    public final static String OAUTH_URL = "OAuthWebLogin-url";
    public final static String OAUTH_CANVAS_LOGIN = "OAuthWebLogin-canvas_login";

    private static String prefFileName;
    private static String prefNamePreviousDomain;
    private static String prefNameOtherSignedInUsers;
    private static String prefMultiSignedInUsers;
    public final static int SIGNED_IN = 5000;

	private Uri passedURI;

    //region Navigation Drawer

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecyclerView;

    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauthweblogin);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.drawerRecyclerView);
        boolean isDebuggable =  (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        mDrawerLayout.setDrawerLockMode(isDebuggable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDomainText = (TextView) findViewById(R.id.domain);
        mContainer = (FrameLayout) findViewById(R.id.container);

        handleIntent();

        mContainer.addView(createWebView());

        if(!TextUtils.isEmpty(url)) {
            mDomainText.setText(url);
            mDomainText.setVisibility(View.VISIBLE);
        } else {
            mDomainText.setVisibility(View.INVISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        ((TextView)findViewById(R.id.domain)).setText(url);

        setupCallback();
        MobileVerifyAPI.mobileVerify(url, mobileVerifyCallback);

        if(isDebuggable) {
            eatSnickerDoodles();
        }
    }

    void clearCookies() {
        CookieSyncManager.createInstance(OAuthWebLogin.this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

	@Override
	public void onPause() {
		super.onPause();

		//save the intent information in case we get booted from memory.
		SharedPreferences settings = getSharedPreferences(prefFileName, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(OAUTH_URL, url);
		editor.putInt(OAUTH_CANVAS_LOGIN, canvas_login);

		editor.apply();

		//we don't want the onPostExecute to be called in mobileVerifyAT if we're leaving this function. Without cancelling this
		//it would try to create a fragment and not have anything to attach it to and it would crash. (insert frowny face)
		if(mobileVerifyCallback != null) {
			mobileVerifyCallback.cancel();
		}
	}

    private WebView createWebView() {
        web =  new WebView(this);
        clearCookies();
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setSavePassword(false);
        web.getSettings().setSaveFormData(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.getSettings().setAppCacheEnabled(false);
        web.getSettings().setUserAgentString(CanvasAPI.getCandroidUserAgent("candroid", OAuthWebLogin.this));

        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(url.contains(successURL)) {
                    String oAuthRequest = url.substring(url.indexOf(successURL)+successURL.length());
                    OAuthAPI.getToken(client_id, client_secret, oAuthRequest, getToken);
                }
                else if (url.contains(errorURL)) {
                    clearCookies();
                    view.loadUrl(authenticationURL);
                }
                else {
                    view.loadUrl(url);
                }

                return true; // then it is not handled by default action
            }

            public void onPageFinished(WebView view, String url) {
                setProgressBarIndeterminateVisibility(false);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                        specialCase = true;
                        String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                        OAuthAPI.getToken(client_id, client_secret, oAuthRequest, getToken);
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uriUrl = request.getUrl();
                    String url = uriUrl.toString();
                    Utils.d("url: " + url);
                    if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                        specialCase = true;
                        String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                        OAuthAPI.getToken(client_id, client_secret, oAuthRequest, getToken);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                httpAuthHandler = handler;
                showAuthenticationDialog();
            }
        });
        return web;
    }

    private void showAuthenticationDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(OAuthWebLogin.this);
        builder.title(R.string.authenticationRequired);
        builder.customView(R.layout.auth_dialog, true);
        builder.cancelable(true);
        builder.positiveText(R.string.done);
        builder.negativeText(R.string.cancel);
        builder.positiveColor(Color.BLACK);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                View dialogView = dialog.getCustomView();
                if (dialogView != null) {
                    EditText username = (EditText) dialogView.findViewById(R.id.username);
                    EditText password = (EditText) dialogView.findViewById(R.id.password);
                    if (!TextUtils.isEmpty(username.getText()) && !TextUtils.isEmpty(password.getText())) {
                        if (httpAuthHandler != null) {
                            httpAuthHandler.proceed(username.getText().toString(), password.getText().toString());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show();
                    }
                }
                super.onPositive(dialog);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                onBackPressed();
                super.onNegative(dialog);
            }
        });
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void setupCallback() {
        mobileVerifyCallback = new CanvasCallback<DomainVerificationResult>(APIHelpers.statusDelegateWithContext(OAuthWebLogin.this)) {
            @Override
            public void cache(DomainVerificationResult domainVerificationResult) {
            }

            @Override
            public void firstPage(DomainVerificationResult domainVerificationResult, LinkHeaders linkHeaders, Response response) {
                if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.Success) {
                    //Domain is now verified.
                    //save domain to the preferences.
                    String domain = "";

                    //mobile verify can change the hostname we need to use
                    if (domainVerificationResult.getBase_url() != null && !domainVerificationResult.getBase_url().equals("")) {
                        domain = domainVerificationResult.getBase_url();
                    } else {
                        domain = url;
                    }

                    //The domain gets set afterwards in SetUpInstance, but domain is required a bit before that works.
                    APIHelpers.setDomain(OAuthWebLogin.this, domain);

                    client_id = domainVerificationResult.getClient_id();
                    client_secret = domainVerificationResult.getClient_secret();

                    //Get the protocol
                    api_protocol = domainVerificationResult.getProtocol();

                    //Set the protocol
                    APIHelpers.setProtocol(domainVerificationResult.getProtocol(), OAuthWebLogin.this);

                    //Get device name for the login request.
                    String deviceName = Build.MODEL;
                    if(deviceName == null || deviceName.equals("")){
                        deviceName = getString(R.string.unknownDevice);
                    }

                    //Remove spaces
                    deviceName = deviceName.replace(" ", "_");

                    //changed for the online update to have an actual formatted login page
                    authenticationURL = api_protocol + "://" + domain + "/login/oauth2/auth?client_id=" +
                            client_id + "&response_type=code&redirect_uri=urn:ietf:wg:oauth:2.0:oob&mobile=1";
                    authenticationURL += "&purpose="+ deviceName;

                    if (canvas_login == 1) {
                        authenticationURL += "&canvas_login=1";
                    } else if (canvas_login == 2) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setCookie(api_protocol + "://" + domain, "canvas_sa_delegated=1");
                    }

                    web.loadUrl(authenticationURL);
                }
                else{
                    //Error message
                    int errorId;

                    if( domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.GeneralError){
                        errorId = R.string.mobileVerifyGeneral;
                    }
                    else if ( domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized){
                        errorId = R.string.mobileVerifyDomainUnauthorized;
                    }
                    else if ( domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.UnknownUserAgent){
                        errorId = R.string.mobileVerifyUserAgentUnauthorized;
                    }
                    else{
                        errorId = R.string.mobileVerifyUnknownError;
                    }

                    MaterialDialog.Builder builder = new MaterialDialog.Builder(OAuthWebLogin.this);
                    builder.title(R.string.errorOccurred);
                    builder.content(errorId);
                    builder.cancelable(true);
                    builder.positiveColor(Color.BLACK);
                    MaterialDialog dialog = builder.build();
                    dialog.show();
                }
            }
        };

        getToken = new CanvasCallback<OAuthToken>(APIHelpers.statusDelegateWithContext(OAuthWebLogin.this)) {
            @Override
            public void cache(OAuthToken oAuthToken, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(OAuthToken oAuthToken, LinkHeaders linkHeaders, Response response) {
                //Set up the rest adapter and such.
                APIHelpers.setToken(getContext(),oAuthToken.getAccess_token());
                CanvasRestAdapter.setupInstance(getContext(), oAuthToken.getAccess_token(),
                        APIHelpers.loadProtocol(getContext()) + "://" + APIHelpers.getDomain(getContext()));

                //save the successful domain to be remembered for later
                JSONArray domains = SavedDomains.getSavedDomains(OAuthWebLogin.this, prefNamePreviousDomain );

                String domain = APIHelpers.getDomain(OAuthWebLogin.this);
                domains.put(domain);
                SavedDomains.setSavedDomains(OAuthWebLogin.this, domains, prefNamePreviousDomain); //save the new domain

                //Set the last used domain.
                setLastSignedInDomain(domain, OAuthWebLogin.this);

                //We now need to get the cache user
                UserAPI.getSelf(new UserCallback(APIHelpers.statusDelegateWithContext(OAuthWebLogin.this)) {
                    @Override
                    public void cachedUser(User user) {
                    }

                    @Override
                    public void user(User user, Response response) {
                        Intent intent = OAuthWebLogin.this.getIntent();
                        intent.putExtra(URLSignIn.loggedInIntent, true);
                        if (passedURI != null){
                            intent.putExtra(Const.PASSED_URI, passedURI);
                        }

                        OAuthWebLogin.this.setResult(RESULT_OK, intent);
                        OAuthWebLogin.this.finish();
                    }
                });
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                if(!specialCase) {
                    Toast.makeText(OAuthWebLogin.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
                } else {
                    specialCase = false;
                }

                web.loadUrl(authenticationURL);

                return true;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Multi User Sign In
    ///////////////////////////////////////////////////////////////////////////

    //Used for MultipleUserSignIn
    public static String getGlobalUserId(String domain, User user) {
        if(user == null) {
            return "";
        }
        return domain + "-" + user.getId();
    }

    public static ArrayList<SignedInUser> getPreviouslySignedInUsers(Context context) {
        return getPreviouslySignedInUsers(context, prefNameOtherSignedInUsers);
    }

    //Does the CURRENT user support Multiple Users.
    public static ArrayList<SignedInUser> getPreviouslySignedInUsers(Context context, String preferenceKey) {

        if(TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        Gson gson = CanvasRestAdapter.getGSONParser();
        ArrayList<SignedInUser> signedInUsers = new ArrayList<SignedInUser>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            SignedInUser signedInUser = null;

            try {
                signedInUser = gson.fromJson(entry.getValue().toString(), SignedInUser.class);
            } catch (IllegalStateException e) {
            } catch (JsonSyntaxException e) {
                //Once in a great while some bad formatted json get stored, if that happens we end up here.
            }

            if(signedInUser != null) {
                signedInUsers.add(signedInUser);
            }
        }

        //Sort by last signed in date.
        Collections.sort(signedInUsers);
        return signedInUsers;
    }

    //Remove user from PreviouslySignedInUsers
    public static boolean removeFromPreviouslySignedInUsers(SignedInUser signedInUser, Context context) {
        return removeFromPreviouslySignedInUsers(signedInUser, context, prefNameOtherSignedInUsers);
    }

    public static boolean removeFromPreviouslySignedInUsers(SignedInUser signedInUser, final Context context, String preferenceKey) {

        if(TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        // Delete Access Token. We don't care about the result.
        APIStatusDelegate apiStatusDelegate = APIHelpers.statusDelegateWithContext(context);
        OAuthAPI.deleteToken(signedInUser.token, signedInUser.protocol, signedInUser.domain, new CanvasCallback<Response>(apiStatusDelegate) {
            @Override public void cache(Response response) { }
            @Override public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) { }
        });

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getGlobalUserId(signedInUser.domain, signedInUser.user));
        return editor.commit();
    }

    //Add user to PreviouslySignedInUsers
    public static boolean addToPreviouslySignedInUsers(SignedInUser signedInUser, Context context){
        return addToPreviouslySignedInUsers(signedInUser, context, prefNameOtherSignedInUsers);
    }

    public static boolean addToPreviouslySignedInUsers(SignedInUser signedInUser, Context context, String preferenceKey){

        if(TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        //Get the JSON.
        Gson gson = CanvasRestAdapter.getGSONParser();
        String signedInUserJSON = gson.toJson(signedInUser);

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getGlobalUserId(APIHelpers.getDomain(context), APIHelpers.getCacheUser(context)), signedInUserJSON);
        return editor.commit();
    }

    public static boolean setIsMultipleUsersSupported(boolean multipleUserSupported, Context context) {
        return setIsMultipleUsersSupported(multipleUserSupported, context, prefMultiSignedInUsers);
    }

    public static boolean setIsMultipleUsersSupported(boolean multipleUserSupported, Context context, String preferenceKey) {

        if(TextUtils.isEmpty(preferenceKey)) {
            prefMultiSignedInUsers = Const.KEY_MULTI_SIGN_IN_PREF_NAME;
            preferenceKey = prefMultiSignedInUsers;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String globalID = getGlobalUserId(APIHelpers.getDomain(context), APIHelpers.getCacheUser(context));

        if (multipleUserSupported) {
            editor.putBoolean(globalID, multipleUserSupported);
        } else {
            editor.remove(globalID);
        }
        return editor.commit();
    }

    public static boolean isMultipleUsersSupported(Context context) {
        return isMultipleUsersSupported(context, prefMultiSignedInUsers);
    }

    public static boolean isMultipleUsersSupported(Context context, String preferenceKey) {

        if(TextUtils.isEmpty(preferenceKey)) {
            prefMultiSignedInUsers = Const.KEY_MULTI_SIGN_IN_PREF_NAME;
            preferenceKey = prefMultiSignedInUsers;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        return sharedPreferences.getBoolean(getGlobalUserId(APIHelpers.getDomain(context), APIHelpers.getCacheUser(context)), false);
    }

    public static boolean isUserLoggedIn(Context context) {
        String token = APIHelpers.getToken(context);
        return (token != null && token.length() != 0);
    }

    /**
     * Helper method to retrieve a users shared prefs for calendar
     *
     * @return
     */
    public static ArrayList<String> getCalendarFilterPrefs(Context context) {
        if(TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(com.instructure.pandautils.utils.Const.FILTER_PREFS_KEY, new HashSet<String>());
        ArrayList<String> prefs = new ArrayList<>();
        if (set.size() != 0) {
            for (String s : set) {
                if (s != null) {
                    prefs.add(s);
                }
            }
        }
        return prefs;
    }

    /**
     * Helper method to set a users shared prefs for calendar
     *
     * @return
     */
    public static void setCalendarFilterPrefs(ArrayList<String> filterPrefs, Context context) {
        Set<String> set = new HashSet<>();

        if(filterPrefs == null) {
            filterPrefs = new ArrayList<>();
        }
        for (String s : filterPrefs) {
            if (s != null) {
                set.add(s);
            }
        }

        if(TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(prefFileName, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(com.instructure.pandautils.utils.Const.FILTER_PREFS_KEY, set);
        editor.apply();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Domain
    ///////////////////////////////////////////////////////////////////////////

    public static String getLastSignedInDomain(Context context) {
        //get the Domain
        if(TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }
        SharedPreferences settings = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        return settings.getString(Const.LAST_DOMAIN, "");
    }

    public static void setLastSignedInDomain(String domain, Context context) {
        if (Masquerading.isMasquerading(context)) {
            return;
        }

        if(TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        //save the OAuthToken
        SharedPreferences settings = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Const.LAST_DOMAIN, domain);
        editor.apply();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

	protected void handleIntent() {
		Intent intent = getIntent();
		//Make sure we weren't booted from memory
        if(intent != null && getIntent().hasExtra(Const.PREF_FILE_NAME) &&
                getIntent().hasExtra(Const.PREF_NAME_PREVIOUS_DOMAIN) &&
                getIntent().hasExtra(Const.PREF_OTHER_SIGNED_IN_USERS)){

            prefFileName = getIntent().getStringExtra(Const.PREF_FILE_NAME);
            prefNamePreviousDomain = getIntent().getStringExtra(Const.PREF_NAME_PREVIOUS_DOMAIN);
            prefNameOtherSignedInUsers = getIntent().getStringExtra(Const.PREF_OTHER_SIGNED_IN_USERS);
            prefMultiSignedInUsers = getIntent().getStringExtra(Const.PREF_MULTI_SIGN_IN);
        }

		if (intent == null || !intent.hasExtra(Const.HOST)) {
			SharedPreferences settings = getSharedPreferences(prefFileName, MODE_PRIVATE);
			url = settings.getString(OAUTH_URL, "");
		}
		else{
			url = intent.getStringExtra(Const.HOST);
		}

		if (intent == null || !intent.hasExtra(Const.CANVAS_LOGIN)) {
			SharedPreferences settings = getSharedPreferences(prefFileName, MODE_PRIVATE);
			canvas_login = settings.getInt(OAUTH_CANVAS_LOGIN, 0);
		}
		else {
			canvas_login = intent.getIntExtra(Const.CANVAS_LOGIN, 0);
		}

		if(intent != null && getIntent().hasExtra(Const.URI)){
            passedURI = getIntent().getParcelableExtra(Const.URI);
        }
	}

	public static Intent createIntent(
            Context context,
            String host,
            int canvasLogin,
            String preferenceName,
            String preferenceNamePreviousDomain,
            String preferenceOtherSignedInUsers,
            String preferenceMultiSignedInUsers,
            Uri uri) {

		Intent intent = new Intent(context, OAuthWebLogin.class);
		intent.putExtra(Const.URI, uri);
		intent.putExtra(Const.CANVAS_LOGIN, canvasLogin);
		intent.putExtra(Const.HOST,host);
        intent.putExtra(Const.PREF_FILE_NAME, preferenceName);
        intent.putExtra(Const.PREF_NAME_PREVIOUS_DOMAIN, preferenceNamePreviousDomain);
        intent.putExtra(Const.PREF_OTHER_SIGNED_IN_USERS, preferenceOtherSignedInUsers);
        intent.putExtra(Const.PREF_MULTI_SIGN_IN, preferenceMultiSignedInUsers);
		return intent;
	}

	public static Intent createIntent(
            Context context,
            String host,
            int canvasLogin,
            String preferenceName,
            String preferenceNamePreviousDomain,
            String preferenceOtherSignedInUsers,
            String preferenceMultiSignedInUsers) {

		Intent intent = new Intent(context, OAuthWebLogin.class);
		intent.putExtra(Const.HOST, host);
		intent.putExtra(Const.CANVAS_LOGIN, canvasLogin);
        intent.putExtra(Const.PREF_FILE_NAME, preferenceName);
        intent.putExtra(Const.PREF_NAME_PREVIOUS_DOMAIN, preferenceNamePreviousDomain);
        intent.putExtra(Const.PREF_OTHER_SIGNED_IN_USERS, preferenceOtherSignedInUsers);
        intent.putExtra(Const.PREF_MULTI_SIGN_IN, preferenceMultiSignedInUsers);
		return intent;
	}

    /**
     * Adds a simple login method for devs. To add credentials add your snickers (credentials) to the snickers.json
     * Slide the drawer out from the right to have a handy one click login. FYI: Only works on Debug.
     * Sample Format is:

     [
         {
         "password":"password",
         "subtitle":"subtitle",
         "title":"title",
         "username":"username"
         },
         ...
     ]

     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void eatSnickerDoodles() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        Writer writer = new StringWriter();
        try {
            InputStream is = getResources().openRawResource(getResources().getIdentifier("snickers", "raw", getPackageName()));
            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (Exception e) {
            //Do Nothing
        }

        String jsonString = writer.toString();
        if (jsonString != null && jsonString.length() > 0) {
            ArrayList<SnickerDoodle> snickerDoodles = new Gson().fromJson(jsonString, new TypeToken<ArrayList<SnickerDoodle>>() {
            }.getType());

            if (snickerDoodles.size() == 0) {
                findViewById(R.id.drawerEmptyView).setVisibility(View.VISIBLE);
                findViewById(R.id.drawerEmptyText).setVisibility(View.VISIBLE);
                return;
            }

            web.getSettings().setDomStorageEnabled(true);
            web.setWebChromeClient(new WebChromeClient());

            mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
            mDrawerRecyclerView.setAdapter(new SnickerDoodleAdapter(snickerDoodles, new SnickerDoodleAdapter.SnickerCallback() {
                @Override
                public void onClick(SnickerDoodle snickerDoodle) {
                    mDrawerLayout.closeDrawers();

                    final String js = "javascript: { " +
                            "document.getElementsByName('pseudonym_session[unique_id]')[0].value = '" + snickerDoodle.username + "'; " +
                            "document.getElementsByName('pseudonym_session[password]')[0].value = '" + snickerDoodle.password + "'; " +
                            "document.getElementsByClassName('btn')[0].click(); " +
                            "};";

                    web.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {}
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final String js = "javascript: { " +
                                            "document.getElementsByClassName('btn')[0].click();" +
                                            "};";

                                    web.evaluateJavascript(js, new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {}
                                    });
                                }
                            });
                        }
                    }, 750);
                }
            }));
        } else {
            findViewById(R.id.drawerEmptyView).setVisibility(View.VISIBLE);
            findViewById(R.id.drawerEmptyText).setVisibility(View.VISIBLE);
        }
    }
}
