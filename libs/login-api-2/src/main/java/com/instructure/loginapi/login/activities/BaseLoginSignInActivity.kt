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
package com.instructure.loginapi.login.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.instructure.canvasapi2.RequestInterceptor.Companion.acceptedLanguageString
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.TokenRefreshState
import com.instructure.canvasapi2.TokenRefresher
import com.instructure.canvasapi2.managers.OAuthManager.getToken
import com.instructure.canvasapi2.managers.UserManager.getSelf
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics.logEvent
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiPrefs.accessToken
import com.instructure.canvasapi2.utils.ApiPrefs.clientId
import com.instructure.canvasapi2.utils.ApiPrefs.clientSecret
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.instructure.canvasapi2.utils.ApiPrefs.protocol
import com.instructure.canvasapi2.utils.ApiPrefs.refreshToken
import com.instructure.canvasapi2.utils.ApiPrefs.user
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger.d
import com.instructure.canvasapi2.utils.isValid
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.api.MobileVerifyAPI.mobileVerify
import com.instructure.loginapi.login.databinding.ActivitySignInBinding
import com.instructure.loginapi.login.dialog.AuthenticationDialog
import com.instructure.loginapi.login.dialog.AuthenticationDialog.Companion.newInstance
import com.instructure.loginapi.login.dialog.AuthenticationDialog.OnAuthenticationSet
import com.instructure.loginapi.login.model.DomainVerificationResult
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.loginapi.login.util.Const
import com.instructure.loginapi.login.util.Const.CANVAS_LOGIN_FLOW
import com.instructure.loginapi.login.util.Const.MASQUERADE_FLOW
import com.instructure.loginapi.login.util.Const.MOBILE_VERIFY_FLOW
import com.instructure.loginapi.login.util.Const.SNICKER_DOODLES
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.loginapi.login.util.PreviousUsersUtils.add
import com.instructure.loginapi.login.util.SavedLoginInfo
import com.instructure.loginapi.login.viewmodel.LoginViewModel
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.ViewStyler.themeStatusBar
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import retrofit2.Call
import retrofit2.Response
import java.util.Locale
import javax.inject.Inject

abstract class BaseLoginSignInActivity : BaseCanvasActivity(), OnAuthenticationSet {

    companion object {
        const val ACCOUNT_DOMAIN = "accountDomain"
        const val SUCCESS_URL = "/login/oauth2/auth?code="
        const val ERROR_URL = "/login/oauth2/auth?error=access_denied"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    protected abstract fun refreshWidgets()
    protected abstract fun userAgent(): String

    private val binding by viewBinding(ActivitySignInBinding::inflate)

    private lateinit var webView: WebView
    private var canvasLogin = 0
    private var specialCase = false
    private var authenticationURL: String? = null
    private var httpAuthHandler: HttpAuthHandler? = null
    private var shouldShowProgressBar = false

    private val accountDomain: AccountDomain by lazy { intent.getParcelableExtra<AccountDomain>(ACCOUNT_DOMAIN) ?: AccountDomain() }
    private val progressBarHandler = Handler()

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var navigation: LoginNavigation

    @Inject
    lateinit var tokenRefresher: TokenRefresher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        setContentView(binding.root)
        setupWindowInsets()
        canvasLogin = intent!!.extras!!.getInt(Const.CANVAS_LOGIN, 0)
        setupViews()
        applyTheme()
        beginSignIn(accountDomain)

        onBackPressedDispatcher.addCallback(this) {
            tokenRefresher.refreshState = TokenRefreshState.Failed
            finish()
        }

        if (intent.hasExtra(TokenRefresher.TOKEN_REFRESH)) {
            AlertDialog.Builder(this, R.style.AccessibleAlertDialog)
                .setTitle(R.string.loginRequired)
                .setMessage(R.string.loginRequiredMessage)
                .setPositiveButton(R.string.login) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.logout) { dialog, _ ->
                    tokenRefresher.refreshState = TokenRefreshState.Failed
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                insets.left,
                0,
                insets.right,
                insets.bottom
            )
            windowInsets
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.applyTopSystemBarInsets()
        toolbar.title = accountDomain.domain
        toolbar.navigationIcon?.isAutoMirrored = true
        toolbar.setupAsBackButton {
            tokenRefresher.refreshState = TokenRefreshState.Failed
            finish()
        }
        webView = findViewById(R.id.webView)
        clearCookies()
        CookieManager.getInstance().setAcceptCookie(true)
        webView.settings.loadWithOverviewMode = true
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.useWideViewPort = true
        @Suppress("DEPRECATION")
        webView.settings.saveFormData = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString = Utils.generateUserAgent(this, userAgent())
        webView.webViewClient = mWebViewClient
        if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    private fun applyTheme() {
        themeStatusBar(this)
    }

    /**
     * Override to handle the shouldOverrideUrlLoading() method.
     * @param view WebView
     * @param url Url String
     * @return If overriding this method it is expected to return true, if false the default behavior
     * of the BaseLoginSignInActivity will handle the override.
     */
    @Suppress("UNUSED_PARAMETER")
    protected fun overrideUrlLoading(view: WebView?, url: String?): Boolean {
        return false
    }

    private val mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return handleShouldOverrideUrlLoading(view, request.url.toString())
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleShouldOverrideUrlLoading(view, url)
        }

        private fun handleShouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (overrideUrlLoading(view, url)) return true
            return when {
                url.contains(SUCCESS_URL) -> {
                    domain = accountDomain.domain!!
                    val oAuthRequest = url.substring(url.indexOf(SUCCESS_URL) + SUCCESS_URL.length)
                    getToken(clientId, clientSecret, oAuthRequest, mGetTokenCallback)
                    true
                }
                url.contains(ERROR_URL) -> {
                    clearCookies()
                    loadUrl(view, authenticationURL, headers)
                    true
                }
                else -> {
                    false
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            handleShouldInterceptRequest(url)
            return super.shouldInterceptRequest(view, url)
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            handleShouldInterceptRequest(request.url.toString())
            return super.shouldInterceptRequest(view, request)
        }

        private fun handleShouldInterceptRequest(url: String) {
            if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                specialCase = true
                domain = accountDomain.domain!!
                val oAuthRequest = url.substringAfter("hash=")
                getToken(clientId, clientSecret, oAuthRequest, mGetTokenCallback)
            }
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            httpAuthHandler = handler
            newInstance(accountDomain.domain).show(supportFragmentManager, AuthenticationDialog::class.java.simpleName)
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest?, errorResponse: WebResourceResponse) {
            if (errorResponse.statusCode == 400 && authenticationURL != null && request != null && request.url != null && authenticationURL == request.url.toString()) {
                //If the institution does not support skipping the authentication screen this will catch that error and force the
                //rebuilding of the authentication url with the authorization screen flow. Example: canvas.sfu.ca
                buildAuthenticationUrl(protocol, accountDomain, clientId, true)
                loadAuthenticationUrl(protocol, accountDomain.domain)
            }
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // The intention of this delay is that we don't want to show/hide the progress bar multiple times
            // when loading multiple pages after each other.
            shouldShowProgressBar = false
            progressBarHandler.postDelayed({
                if (!shouldShowProgressBar) binding.webViewProgressBar.setGone()
            }, 50)
        }
    }

    private fun beginSignIn(accountDomain: AccountDomain) {
        val url = accountDomain.domain
        if (canvasLogin == MOBILE_VERIFY_FLOW) { //Skip Mobile Verify
            val view = LayoutInflater.from(this@BaseLoginSignInActivity).inflate(R.layout.dialog_skip_mobile_verify, null)
            val protocolEditText = view.findViewById<EditText>(R.id.mobileVerifyProtocol)
            val clientIdEditText = view.findViewById<EditText>(R.id.mobileVerifyClientId)
            val clientSecretEditText = view.findViewById<EditText>(R.id.mobileVerifyClientSecret)
            val dialog = AlertDialog.Builder(this@BaseLoginSignInActivity)
                .setTitle(R.string.mobileVerifyDialogTitle)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    protocol = protocolEditText.text.toString()
                    domain = url!!
                    clientId = clientIdEditText.text.toString()
                    clientSecret = clientSecretEditText.text.toString()
                    buildAuthenticationUrl(
                        protocolEditText.text.toString(),
                        accountDomain,
                        clientId,
                        false
                    )
                    loadUrl(webView, authenticationURL, headers)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    mobileVerify(url, mobileVerifyCallback)
                    showLoading()
                }
                .create()
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            }
            dialog.show()
        } else {
            mobileVerify(url, mobileVerifyCallback)
            showLoading()
        }
    }

    override fun onRetrieveCredentials(username: String?, password: String?) {
        if (username.isValid() && password.isValid()) {
            httpAuthHandler?.proceed(username, password)
        } else {
            Toast.makeText(applicationContext, R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }

    val headers: Map<String, String>
        get() = mapOf(
            "accept-language" to acceptedLanguageString,
            "user-agent" to Utils.generateUserAgent(this, userAgent()),
            "session_locale" to Locale.getDefault().language
        )

    private var mobileVerifyCallback: StatusCallback<DomainVerificationResult> =
        object : StatusCallback<DomainVerificationResult>() {
            override fun onResponse(response: Response<DomainVerificationResult>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type.isCache) return
                val domainVerificationResult = response.body()
                if (domainVerificationResult!!.result === DomainVerificationResult.DomainVerificationCode.Success) {
                    //Domain is now verified.
                    //save domain to the preferences.
                    var domain: String?

                    //mobile verify can change the hostname we need to use
                    domainVerificationResult!!.baseUrl
                    domain = if (domainVerificationResult.baseUrl != "") {
                        domainVerificationResult.baseUrl
                    } else {
                        accountDomain.domain
                    }
                    if (domain!!.endsWith("/")) {
                        domain = domain.substring(0, domain.length - 1)
                    }
                    accountDomain.domain = domain
                    clientId = domainVerificationResult.clientId
                    clientSecret = domainVerificationResult.clientSecret

                    //Get the protocol
                    val apiProtocol = domainVerificationResult.protocol

                    //Set the protocol
                    protocol = domainVerificationResult.protocol
                    buildAuthenticationUrl(apiProtocol, accountDomain, clientId, false)
                    loadAuthenticationUrl(apiProtocol, domain)
                } else {
                    //Error message
                    val errorId: Int = when (domainVerificationResult?.result) {
                        DomainVerificationResult.DomainVerificationCode.GeneralError -> R.string.mobileVerifyGeneral
                        DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized -> R.string.mobileVerifyDomainUnauthorized
                        DomainVerificationResult.DomainVerificationCode.UnknownUserAgent -> R.string.mobileVerifyUserAgentUnauthorized
                        else -> R.string.mobileVerifyUnknownError
                    }
                    showErrorDialog(errorId)
                }
            }

            override fun onFail(call: Call<DomainVerificationResult>?, error: Throwable, response: Response<*>?) {
                showErrorDialog(R.string.mobileVerifyUnknownError)
            }
        }

    private fun showErrorDialog(@StringRes resId: Int) {
        shouldShowProgressBar = false
        binding.webViewProgressBar.setGone()

        if (!isFinishing) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.errorOccurred)
            builder.setMessage(resId)
            builder.setCancelable(true)
            builder.setOnCancelListener { finish() }
            val dialog = builder.create()
            dialog.show()
        }
    }

    protected fun loadAuthenticationUrl(apiProtocol: String, domain: String?) {
        if (canvasLogin == CANVAS_LOGIN_FLOW) {
            authenticationURL += "&canvas_login=1"
        } else if (canvasLogin == MASQUERADE_FLOW) {
            // canvas_sa_delegated=1    identifies that we want to masquerade
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            if (domain!!.contains(".instructure.com")) {
                val cookie = "canvas_sa_delegated=1;domain=.instructure.com;path=/;"
                cookieManager.setCookie("$apiProtocol://$domain", cookie)
                cookieManager.setCookie(".instructure.com", cookie)
            } else {
                cookieManager.setCookie(domain, "canvas_sa_delegated=1")
            }
        }
        loadUrl(webView, authenticationURL, headers)
        if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            webView.postDelayed({
                if (intent.hasExtra(SNICKER_DOODLES)) {
                    val snickerDoodle: SnickerDoodle = intent.getParcelableExtra(SNICKER_DOODLES)!!
                    populateWithSnickerDoodle(snickerDoodle)
                }
            }, 1500)
        }
    }

    protected fun buildAuthenticationUrl(protocol: String?, accountDomain: AccountDomain?, clientId: String?, forceAuthRedirect: Boolean) {
        //Get device name for the login request.
        var deviceName = Build.MODEL
        if (deviceName == null || deviceName == "") {
            deviceName = getString(R.string.unknownDevice)
        }
        // Remove spaces
        deviceName = deviceName.replace(" ", "_")
        // Changed for the online update to have an actual formatted login page
        var domain = accountDomain!!.domain
        if (domain != null && domain.endsWith("/")) {
            domain = domain.substring(0, domain.length - 1)
        }
        val builder = Uri.Builder()
            .scheme(protocol)
            .authority(domain)
            .appendPath("login")
            .appendPath("oauth2")
            .appendPath("auth")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("mobile", "1")
            .appendQueryParameter("purpose", deviceName)
        if (forceAuthRedirect || canvasLogin == MOBILE_VERIFY_FLOW || domain != null && domain.contains(".test.")) {
            //Skip mobile verify
            builder.appendQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
        } else {
            builder.appendQueryParameter("redirect_uri", "https://canvas.instructure.com/login/oauth2/auth")
        }

        //If an authentication provider is supplied we need to pass that along. This should only be appended if one exists.
        val authenticationProvider = accountDomain.authenticationProvider
        if (authenticationProvider != null && authenticationProvider.isNotEmpty() && !authenticationProvider.equals("null", ignoreCase = true)) {
            d("authentication_provider=$authenticationProvider")
            builder.appendQueryParameter("authentication_provider", authenticationProvider)
        }
        val authUri = builder.build()
        if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            d("AUTH URL: $authUri")
        }
        authenticationURL = authUri.toString()
    }

    private val mGetTokenCallback: StatusCallback<OAuthTokenResponse> =
        object : StatusCallback<OAuthTokenResponse>() {
            override fun onResponse(response: Response<OAuthTokenResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type.isCache) return
                val token = response.body()
                if (intent.hasExtra(TokenRefresher.TOKEN_REFRESH) && user?.id != null && token?.user?.id != null && user?.id != token.user?.id) {
                    toast(R.string.loginRefreshSameUserError)
                    tokenRefresher.refreshState = TokenRefreshState.Restart
                    finish()
                    return
                }
                val bundle = Bundle()
                bundle.putString(AnalyticsParamConstants.DOMAIN_PARAM, domain)
                logEvent(AnalyticsEventConstants.LOGIN_SUCCESS, bundle)
                refreshToken = token!!.refreshToken!!
                accessToken = token.accessToken!!
                @Suppress("DEPRECATION")
                ApiPrefs.token = "" // TODO: Remove when we're 100% using refresh tokens

                if (intent.hasExtra(TokenRefresher.TOKEN_REFRESH)) {
                    tokenRefresher.refreshState = TokenRefreshState.Success(accessToken)
                }

                // We now need to get the cache user
                getSelf(true, object : StatusCallback<User>() {
                    override fun onResponse(response: Response<User>, linkHeaders: LinkHeaders, type: ApiType) {
                        if (type.isAPI) {
                            user = response.body()
                            val userResponse = response.body()
                            val domain = domain
                            val protocol = protocol
                            val user = SignedInUser(
                                userResponse!!,
                                domain,
                                protocol,
                                "",  // TODO - delete once we move over 100% to refresh tokens
                                token.accessToken!!,
                                token.refreshToken!!,
                                clientId,
                                clientSecret,
                                null,
                                null
                            )
                            add(this@BaseLoginSignInActivity, user)
                            refreshWidgets()

                            if (intent.hasExtra(TokenRefresher.TOKEN_REFRESH)) {
                                finish()
                                return
                            }
                            LoginPrefs.lastSavedLogin = SavedLoginInfo(accountDomain, canvasLogin)
                            navigation.startLogin(viewModel, false)
                            tokenRefresher.loggedOut = false
                        }
                    }
                })
            }

            override fun onFail(call: Call<OAuthTokenResponse>?, error: Throwable, response: Response<*>?) {
                val bundle = Bundle()
                bundle.putString(AnalyticsParamConstants.DOMAIN_PARAM, domain)
                logEvent(AnalyticsEventConstants.LOGIN_FAILURE, bundle)
                if (!specialCase) {
                    Toast.makeText(
                        this@BaseLoginSignInActivity,
                        R.string.errorOccurred,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    specialCase = false
                }
                loadUrl(webView, authenticationURL, headers)
            }
        }

    private fun loadUrl(webView: WebView, url: String?, headers: Map<String, String>) {
        webView.loadUrl(url ?: "", headers)
        // We need to delay this, because it can happen that this method is called a couple of milliseconds
        // before the onPageFinished triggered for the previous page resulting in hiding the progress bar while still loading.
        progressBarHandler.postDelayed({ showLoading() }, 50)
    }

    private fun showLoading() = with(binding) {
        shouldShowProgressBar = true
        webViewProgressBar.setVisible()
        webViewProgressBar.announceForAccessibility(getString(R.string.loading))
    }

    //region Snicker Doodles
    private fun populateWithSnickerDoodle(snickerDoodle: SnickerDoodle) {
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        val js = "javascript: { " +
                "document.getElementsByName('pseudonym_session[unique_id]')[0].value = '" + snickerDoodle.username + "'; " +
                "document.getElementsByName('pseudonym_session[password]')[0].value = '" + snickerDoodle.password + "'; " +
                "document.getElementsByClassName('Button')[0].click(); " +
                "};"
        webView.evaluateJavascript(js) { }
        Handler().postDelayed({
            runOnUiThread {
                val javascript = "javascript: { " +
                        "document.getElementsByClassName('btn')[0].click();" +
                        "};"
                webView.evaluateJavascript(javascript) { }
            }
        }, 750)
    } //endregion
}
