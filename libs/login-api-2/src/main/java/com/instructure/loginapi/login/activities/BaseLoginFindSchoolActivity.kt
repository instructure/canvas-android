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

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AccountDomainManager
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.adapter.DomainAdapter
import com.instructure.loginapi.login.databinding.ActivityFindSchoolBinding
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.loginapi.login.util.Const
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setupAsBackButton
import retrofit2.Response
import java.util.Locale
import java.util.regex.Pattern

abstract class BaseLoginFindSchoolActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityFindSchoolBinding::inflate)

    private var mDomainAdapter: DomainAdapter? = null
    private var mNextActionButton: TextView? = null
    private val mDelayFetchAccountHandler = Handler()
    protected var mWhatsYourSchoolName: TextView? = null
    private var mLoginFlowLogout: TextView? = null

    /**
     * Worker thread for fetching account domains.
     */
    private val mFetchAccountsWorker = Runnable {
        val query = binding.domainInput.text.toString()
        AccountDomainManager.searchAccounts(query, mAccountDomainCallback)
    }

    private val mAccountDomainCallback = object : StatusCallback<List<AccountDomain>>() {

        override fun onResponse(response: Response<List<AccountDomain>>, linkHeaders: LinkHeaders, type: ApiType) {
            if (type.isCache) return

            val domains = response.body()?.toMutableList() ?: mutableListOf()

            val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

            if (isDebuggable) {
                // Put these domains first
                domains.add(0, createAccountForDebugging("mobiledev.instructure.com"))
                domains.add(1, createAccountForDebugging("mobiledev.beta.instructure.com"))
                domains.add(2, createAccountForDebugging("mobileqa.instructure.com"))
                domains.add(3, createAccountForDebugging("mobileqat.instructure.com"))
                domains.add(4, createAccountForDebugging("clare.instructure.com"))
                domains.add(5, createAccountForDebugging("mobileqa.beta.instructure.com"))
            }

            if (mDomainAdapter != null) {
                mDomainAdapter!!.setItems(domains)
                mDomainAdapter!!.filter.filter(binding.domainInput!!.text.toString())
            }
        }
    }

    @ColorInt
    protected abstract fun themeColor(): Int

    protected abstract fun signInActivityIntent(accountDomain: AccountDomain): Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupWindowInsets()
        bindViews()
        applyTheme()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                0
            )
            insets
        }
    }

    private fun bindViews() = with(binding) {
        mWhatsYourSchoolName = findViewById(R.id.whatsYourSchoolName)
        mLoginFlowLogout = findViewById(R.id.loginFlowLogout)
        toolbar.apply {
            applyTopSystemBarInsets()
            navigationIcon?.isAutoMirrored = true
            setupAsBackButton { finish() }
            inflateMenu(R.menu.menu_next)
            setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
                if (item.itemId == R.id.next) {
                    if (APIHelper.hasNetworkConnection()) {
                        validateDomain(AccountDomain(domainInput.text.toString()))
                        return@OnMenuItemClickListener true
                    } else {
                        NoInternetConnectionDialog.show(supportFragmentManager)
                        return@OnMenuItemClickListener true
                    }
                }
                false
            })
        }

        val a11yManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (a11yManager != null && (a11yManager.isEnabled || a11yManager.isTouchExplorationEnabled)) {
            toolbar.isFocusable = true
            toolbar.isFocusableInTouchMode = true
            toolbar.postDelayed({
                toolbar.requestFocus()
                toolbar.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }, 500)
        }

        mNextActionButton = findViewById(R.id.next)
        mNextActionButton!!.isEnabled = false
        mNextActionButton!!.setTextColor(ContextCompat.getColor(this@BaseLoginFindSchoolActivity, R.color.backgroundMedium))

        domainInput.requestFocus()
        domainInput.setOnEditorActionListener { _, _, _ ->
            validateDomain(AccountDomain(domainInput.text.toString()))
            true
        }

        domainInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (mDomainAdapter != null) {
                    mDomainAdapter!!.filter.filter(s)
                    fetchAccountDomains()
                }

                if (mNextActionButton != null) {
                    if (TextUtils.isEmpty(s.toString())) {
                        mNextActionButton!!.isEnabled = false
                        mNextActionButton!!.setTextColor(ContextCompat.getColor(
                                this@BaseLoginFindSchoolActivity, R.color.backgroundMedium))
                    } else {
                        mNextActionButton!!.isEnabled = true
                        mNextActionButton!!.setTextColor(ContextCompat.getColor(
                                this@BaseLoginFindSchoolActivity, R.color.textInfo))
                    }
                }
            }
        })

        mDomainAdapter = DomainAdapter(object : DomainAdapter.DomainEvents {
            override fun onDomainClick(account: AccountDomain) {
                domainInput.setText(account.domain)
                domainInput.setSelection(domainInput.text.length)
                validateDomain(account)
            }

            override fun onHelpClick() {
                val webHelpIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.FIND_SCHOOL_HELP_URL))
                startActivity(webHelpIntent)
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.findSchoolRecyclerView)
        recyclerView.addItemDecoration(DividerItemDecoration(this@BaseLoginFindSchoolActivity, RecyclerView.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this@BaseLoginFindSchoolActivity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = mDomainAdapter
        recyclerView.applyBottomSystemBarInsets()
    }

    /**
     * Override to handle a logout click event. Use the showLogout() method to enable the buttons visibility
     */
    protected fun logout() {}

    /**
     * Shows a logout button. Calls from click return to a logout()
     * @param show a value to indicate if the logout button should be shown.
     */
    protected fun showLogout(show: Boolean) {
        mLoginFlowLogout!!.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            mLoginFlowLogout!!.setOnClickListener { logout() }
        }
    }

    private fun validateDomain(accountDomain: AccountDomain) {
        var url: String? = accountDomain.domain!!.lowercase(Locale.getDefault()).replace(" ", "")

        //if the user enters nothing, try to connect to canvas.instructure.com
        if (url!!.trim { it <= ' ' }.isEmpty()) {
            url = "canvas.instructure.com"
        }

        //remove invalid characters at the end of the domain
        val pattern = Pattern.compile("(.*)([a-zA-Z0-9]){1}")
        val matcher = pattern.matcher(url)
        if (matcher.find()) {
            url = matcher.group()
        }

        //if there are no periods, append .instructure.com
        if (!url!!.contains(".") || url.endsWith(".beta")) {
            url += ".instructure.com"
        }

        //URIs need to to start with a scheme.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        //Get just the host.
        val uri = Uri.parse(url)
        url = uri.host

        //Strip off www. if they typed it.
        if (url!!.startsWith("www.")) {
            url = url.substring(4)
        }

        accountDomain.domain = url

        val intent = signInActivityIntent(accountDomain)
        intent.putExtra(Const.CANVAS_LOGIN, getIntent().extras!!.getInt(Const.CANVAS_LOGIN, 0))
        startActivity(intent)
    }

    protected open fun applyTheme() {
        val color = themeColor()

        val view = LayoutInflater.from(this).inflate(R.layout.login_toolbar_icon, null, false)
        val icon = view.findViewById<ImageView>(R.id.loginLogo)
        icon.setImageDrawable(ColorUtils.colorIt(color, icon.drawable))

        binding.toolbar.addView(view)

        ViewStyler.themeStatusBar(this)
    }

    /**
     * Handles fetching account domains. Uses a worker runnable and handler to cancel fetching too often.
     */
    private fun fetchAccountDomains() {
        mDelayFetchAccountHandler.removeCallbacks(mFetchAccountsWorker)
        mDelayFetchAccountHandler.postDelayed(mFetchAccountsWorker, 500)
    }

    private fun createAccountForDebugging(domain: String): AccountDomain {
        val account = AccountDomain()
        account.domain = domain
        account.name = "@ $domain"
        account.distance = 0.0
        return account
    }

    override fun onDestroy() {
        if (mDelayFetchAccountHandler != null && mFetchAccountsWorker != null) {
            mDelayFetchAccountHandler.removeCallbacks(mFetchAccountsWorker)
        }
        super.onDestroy()
    }

    //region Help & Support

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
