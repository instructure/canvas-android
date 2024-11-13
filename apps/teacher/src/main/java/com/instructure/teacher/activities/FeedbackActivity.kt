package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import com.instructure.pandautils.blueprint.BaseCanvasActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_FEEDBACK
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ActivityFeedbackBinding
import com.instructure.teacher.utils.setupBackButton

@ScreenView(SCREEN_VIEW_FEEDBACK)
class FeedbackActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityFeedbackBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupWebView()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setTitle(R.string.feedback_form)
        toolbar.setBackgroundColor(ThemePrefs.primaryColor)
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        toolbar.setupBackButton { finish() }

        ViewStyler.setStatusBarDark(this@FeedbackActivity, ThemePrefs.darkPrimaryColor)
        ViewStyler.colorToolbarIconsAndText(this@FeedbackActivity, toolbar, ThemePrefs.primaryTextColor)
    }

    private fun setupWebView() = with(binding) {
        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)
        webView.loadUrl(buildUrl())
    }

    private fun buildUrl(): String {

        val os = "Android"
        val osVersion = Build.VERSION.RELEASE
        val device = Build.MANUFACTURER + " " + Build.MODEL
        var releaseVersion = ""
        try {
            val info = packageManager.getPackageInfo(packageName, 0)
            releaseVersion = "${info.versionName}/${info.versionCode}"
        } catch (e: PackageManager.NameNotFoundException) { /* do nothing */ }
        val email = ApiPrefs.user?.primaryEmail ?: ""
        val builder = Uri.Builder()
        builder.scheme("https")
                .encodedAuthority(BASE_URL)
                .appendQueryParameter("entry.1679142653", "")
                .appendQueryParameter("entry.1079876773", os)
                .appendQueryParameter("entry.1763625541", osVersion)
                .appendQueryParameter("entry.50706604", device)
                .appendQueryParameter("entry.321299646", releaseVersion)
                .appendQueryParameter("entry.941918261", email)

        return builder.build().toString()
    }

    companion object {

        private const val BASE_URL = "docs.google.com/forms/d/e/1FAIpQLSeBW9mUTkwMUXpIr4LOE_jtAXzynjWExUDsfg98_ktBldq_6A/viewform"

        fun createIntent(context: Context): Intent {
            return Intent(context, FeedbackActivity::class.java)
        }
    }
}
