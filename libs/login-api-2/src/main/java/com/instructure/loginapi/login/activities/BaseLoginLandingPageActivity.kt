/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.adapter.PreviousUsersAdapter
import com.instructure.loginapi.login.adapter.SnickerDoodleAdapter
import com.instructure.loginapi.login.databinding.ActivityLoginLandingPageBinding
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.loginapi.login.util.Const.CANVAS_LOGIN_FLOW
import com.instructure.loginapi.login.util.Const.MASQUERADE_FLOW
import com.instructure.loginapi.login.util.Const.MOBILE_VERIFY_FLOW
import com.instructure.loginapi.login.util.Const.NORMAL_FLOW
import com.instructure.loginapi.login.util.Const.SNICKER_DOODLES
import com.instructure.loginapi.login.util.Const.URL_CANVAS_NETWORK
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.loginapi.login.util.SavedLoginInfo
import com.instructure.loginapi.login.viewmodel.LoginViewModel
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.util.*
import javax.inject.Inject

abstract class BaseLoginLandingPageActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityLoginLandingPageBinding::inflate)

    private lateinit var gestureDetector: GestureDetector
    private var gestureFirstFree = true
    private var gestureFirst: Long = 0
    private var gestureSecond: Long = 0
    private var canvasLogin = NORMAL_FLOW

    protected abstract fun beginFindSchoolFlow(): Intent

    protected abstract fun signInActivityIntent(accountDomain: AccountDomain): Intent

    protected abstract fun beginCanvasNetworkFlow(url: String): Intent

    @ColorInt
    protected abstract fun themeColor(): Int

    protected abstract fun appTypeName(): String

    protected open fun appChangesLink(): String? = null

    protected open fun loginWithQRCodeEnabled(): Boolean = false

    protected abstract fun loginWithQRIntent(): Intent?

    @Inject
    lateinit var navigation: LoginNavigation

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        bindViews()
        applyTheme()
        loadPreviousUsers()
        setupGesture()
        setupSnickerDoodles()
        setupButtons()
    }

    private fun bindViews() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        canvasNetwork.onClick {
            if (APIHelper.hasNetworkConnection()) {
                val intent = beginCanvasNetworkFlow(URL_CANVAS_NETWORK)
                intent.putExtra(Const.CANVAS_LOGIN, canvasLogin)
                startActivity(intent)
            } else {
                NoInternetConnectionDialog.show(supportFragmentManager)
            }
        }

        if(loginWithQRCodeEnabled()) {
            qrLogin.setVisible()
            qrDivider.setVisible()
            qrLogin.onClickWithRequireNetwork {
                qrLoginClicked()
            }
        } else {
            qrLogin.setGone()
            qrDivider.setGone()
        }
    }

    protected open fun qrLoginClicked() {
        Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_CLICKED)
        startActivity(loginWithQRIntent())
    }

    private fun loadPreviousUsers() = with(binding) {
        val previousUsers = PreviousUsersUtils.get(this@BaseLoginLandingPageActivity)
        resizePreviousUsersRecyclerView(previousUsers)

        previousLoginRecyclerView.layoutManager = LinearLayoutManager(this@BaseLoginLandingPageActivity, RecyclerView.VERTICAL, false)
        previousLoginRecyclerView.adapter =
                PreviousUsersAdapter(previousUsers, object : PreviousUsersAdapter.PreviousUsersEvents {
                    override fun onPreviousUserClick(user: SignedInUser) {
                        ApiPrefs.protocol = user.protocol
                        ApiPrefs.user = user.user
                        ApiPrefs.domain = user.domain
                        ApiPrefs.clientId = user.clientId.orEmpty()
                        ApiPrefs.clientSecret = user.clientSecret.orEmpty()
                        user.accessToken?.let { accessToken ->
                            ApiPrefs.refreshToken = user.refreshToken
                            ApiPrefs.accessToken = accessToken
                        }

                        ApiPrefs.token = user.token
                        ApiPrefs.canvasForElementary = user.canvasForElementary

                        navigation.startLogin(viewModel, true)
                    }

                    override fun onRemovePreviousUserClick(user: SignedInUser) {
                        removePreviousUser(user)
                    }

                    override fun onNowEmpty() {
                        val fade = ObjectAnimator.ofFloat(previousLoginWrapper, View.ALPHA, 1f, 0f)
                        val move = ObjectAnimator.ofFloat(
                            previousLoginWrapper,
                            View.TRANSLATION_Y,
                            0f,
                            previousLoginWrapper.top.toFloat()
                        )
                        val set = AnimatorSet()
                        set.playTogether(fade, move)
                        set.duration = 430
                        set.start()
                    }
                })
        previousLoginWrapper.visibility = if (previousUsers.size > 0) View.VISIBLE else View.GONE

    }

    open fun removePreviousUser(user: SignedInUser) {
        PreviousUsersUtils.remove(this@BaseLoginLandingPageActivity, user)
    }

    private fun resizePreviousUsersRecyclerView(previousUsers: ArrayList<SignedInUser>) = with(binding) {
        val maxUsersToShow = resources.getInteger(R.integer.login_previousMaxVisible)
        if (previousUsers.size == 1 && maxUsersToShow > 1) {
            //Resize the view to only show one previous user
            val params = previousLoginRecyclerView.layoutParams
            params.height = resources.getDimensionPixelOffset(R.dimen.login_previousLoginHeight_1x)
            previousLoginRecyclerView.layoutParams = params
        }
    }

    private fun applyTheme() = with(binding) {
        // Colors
        val color = themeColor()
        val buttonColor = ContextCompat.getColor(this@BaseLoginLandingPageActivity, R.color.textInfo)

        // Button
        val wrapDrawable = DrawableCompat.wrap(findMySchool.background)
        DrawableCompat.setTint(wrapDrawable, buttonColor)
        findMySchool.background = DrawableCompat.unwrap(wrapDrawable)

        // Icon
        ColorUtils.colorIt(color, canvasLogo)

        ViewStyler.themeStatusBar(this@BaseLoginLandingPageActivity)
    }

    private fun setupGesture() {
        gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true
        })
        binding.rootView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            val action = event.action and MotionEvent.ACTION_MASK
            // Capture the event when the user lifts their fingers, not on the down press to make sure they're not long pressing
            if (action == MotionEvent.ACTION_POINTER_UP) {
                // Timer to get difference between clicks
                val now = Calendar.getInstance()

                // Detect number of fingers, change to 1 for a single-finger double-click, 3 for a triple-finger double-click!
                if (event.pointerCount == 2) {
                    gestureFirstFree = !gestureFirstFree

                    if (gestureFirstFree) {
                        // If this is the first click, then there hasn't been a second click yet, also record the time
                        gestureFirst = now.timeInMillis
                    } else {
                        // If this is the second click, record its time
                        gestureSecond = now.timeInMillis
                    }

                    /* If the difference between the 2 clicks is less than 500 ms (1/2 second) Math.abs() is used because
                    you need to be able to detect any sequence of clicks, rather than just in pairs of two (e.g. click1
                    could be registered as a second click if the difference between click1 and click2 > 500 but click2
                    and the next click1 is < 500) */
                    if (Math.abs(gestureSecond - gestureFirst) < 500) {
                        canvasLogin++

                        /* Cycle between 0, 1, 2, and 3
                         *
                         * 0 == no special login
                         * 1 == canvas login
                         * 2 == site admin
                         * 3 == No mobile verify check
                         */
                        if (canvasLogin > MOBILE_VERIFY_FLOW) {
                            canvasLogin = NORMAL_FLOW
                        }

                        toast(
                            when (canvasLogin) {
                                CANVAS_LOGIN_FLOW -> R.string.canvasLoginOn
                                MASQUERADE_FLOW -> R.string.siteAdminLogin
                                MOBILE_VERIFY_FLOW -> R.string.mobileVerifyOff
                                else -> R.string.canvasLoginOff
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            //Do Nothing
        }

        return true
    }


    /**
     * Adds a simple login method for devs. To add credentials add your snickers (credentials) to the snickers.json
     * Slide the drawer out from the right to have a handy one click login. FYI: Only works on Debug.
     * Sample Format is:
     * ```
     * [
     *   {
     *     "password":"password",
     *     "subtitle":"subtitle",
     *     "title":"title",
     *     "username":"username",
     *     "domain":"about.blank"
     *   },
     *   ...
     * ]
     * ```
     */
    private fun setupSnickerDoodles() = with(binding) {
        val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        drawerLayout.setDrawerLockMode(if (isDebuggable) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        if (isDebuggable) try {
            val snickersRes = resources.getIdentifier("snickers", "raw", packageName)
            val jsonString = resources.openRawResource(snickersRes).bufferedReader().use { it.readText() }
            val snickerDoodles = Gson().fromJson(jsonString, Array<SnickerDoodle>::class.java).toList()

            if (snickerDoodles.isEmpty()) {
                drawerEmptyView.setVisible()
                drawerEmptyText.setVisible()
                return
            }

            drawerRecyclerView.layoutManager = LinearLayoutManager(this@BaseLoginLandingPageActivity, RecyclerView.VERTICAL, true)
            drawerRecyclerView.adapter = SnickerDoodleAdapter(snickerDoodles) { snickerDoodle ->
                drawerLayout.closeDrawers()
                val intent = signInActivityIntent(AccountDomain(snickerDoodle.domain))
                intent.putExtra(SNICKER_DOODLES, snickerDoodle)
                startActivity(intent)
                finish()
            }
        } catch (e: Throwable) {
            drawerEmptyView.setVisible()
            drawerEmptyText.setVisible()
        }
    }

    private fun setupButtons() = with(binding) {
        val lastSavedLogin = LoginPrefs.lastSavedLogin
        if (lastSavedLogin != null) {
            openRecentSchool.visibility = View.VISIBLE
            findAnotherSchool.visibility = View.VISIBLE
            findMySchool.visibility = View.GONE

            openRecentSchool.text = if (lastSavedLogin.accountDomain.name.isNullOrEmpty()) {
                lastSavedLogin.accountDomain.domain
            } else {
                lastSavedLogin.accountDomain.name
            }
            openRecentSchool.onClick { openRecentSchool(lastSavedLogin) }

            findAnotherSchool.onClick { findSchool() }
        } else {
            openRecentSchool.visibility = View.GONE
            findAnotherSchool.visibility = View.GONE
            findMySchool.visibility = View.VISIBLE

            findMySchool.onClick { findSchool() }
        }
    }

    private fun openRecentSchool(lastSavedLogin: SavedLoginInfo) {
        if (APIHelper.hasNetworkConnection()) {
            val intent = signInActivityIntent(lastSavedLogin.accountDomain)
            intent.putExtra(Const.CANVAS_LOGIN, lastSavedLogin.canvasLogin)
            startActivity(intent)
        } else {
            NoInternetConnectionDialog.show(supportFragmentManager)
        }
    }

    private fun findSchool() {
        if (APIHelper.hasNetworkConnection()) {
            val intent = beginFindSchoolFlow()
            intent.putExtra(Const.CANVAS_LOGIN, canvasLogin)
            startActivity(intent)
        } else {
            NoInternetConnectionDialog.show(supportFragmentManager)
        }
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}

private fun View.onClickPopupMenu(vararg options: Pair<String, () -> Unit>) {
    onClick {
        val popup = PopupMenu(context, this)
        options.forEachIndexed { idx, option -> popup.menu.add(0, idx, idx, option.first) }
        popup.setOnMenuItemClickListener { item ->
            options[item.itemId].second()
            true
        }
        popup.show()
    }
}
