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
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.models.ErrorReportPreFill
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.adapter.PreviousUsersAdapter
import com.instructure.loginapi.login.adapter.SnickerDoodleAdapter
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.loginapi.login.util.Const.CANVAS_LOGIN_FLOW
import com.instructure.loginapi.login.util.Const.MASQUERADE_FLOW
import com.instructure.loginapi.login.util.Const.MOBILE_VERIFY_FLOW
import com.instructure.loginapi.login.util.Const.NORMAL_FLOW
import com.instructure.loginapi.login.util.Const.SNICKER_DOODLES
import com.instructure.loginapi.login.util.Const.URL_CANVAS_NETWORK
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.loginapi.login.viewmodel.LoginViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.activity_login_landing_page.*
import java.util.*

abstract class BaseLoginLandingPageActivity : AppCompatActivity(), ErrorReportDialog.ErrorReportDialogResultListener {

    private lateinit var gestureDetector: GestureDetector
    private var gestureFirstFree = true
    private var gestureFirst: Long = 0
    private var gestureSecond: Long = 0
    private var canvasLogin = NORMAL_FLOW

    protected abstract fun beginFindSchoolFlow(): Intent

    protected abstract fun signInActivityIntent(snickerDoodle: SnickerDoodle): Intent

    protected abstract fun beginCanvasNetworkFlow(url: String): Intent

    @ColorInt
    protected abstract fun themeColor(): Int

    @StringRes
    protected abstract fun appTypeName(): Int

    protected abstract fun launchApplicationMainActivityIntent(): Intent

    protected open fun appChangesLink(): String? = null

    protected open fun loginWithQRCodeEnabled(): Boolean = false

    protected abstract fun loginWithQRIntent(): Intent?

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_landing_page)
        bindViews()
        applyTheme()
        loadPreviousUsers()
        setupGesture()
        setupSnickerDoodles()
    }

    private fun bindViews() {
        // Only show the what's new text if the app supports it
        changesLayout.visibility = if (appChangesLink() != null) View.VISIBLE else View.GONE

        findMySchool.onClick {
            if (APIHelper.hasNetworkConnection()) {
                val intent = beginFindSchoolFlow()
                intent.putExtra(Const.CANVAS_LOGIN, canvasLogin)
                startActivity(intent)
            } else {
                NoInternetConnectionDialog.show(supportFragmentManager)
            }
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

        whatsNew.onClick {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(appChangesLink())
            startActivity(i)
        }

        helpButton.setHidden(true) // hiding the help button until we make mobile login better
        helpButton.onClickPopupMenu(getString(R.string.requestLoginHelp) to { requestLoginHelp() })

        if(loginWithQRCodeEnabled()) {
            qrLogin.setVisible()
            qrDivider.setVisible()
            qrLogin.onClick {
                Analytics.logEvent(AnalyticsEventConstants.QR_CODE_LOGIN_CLICKED)
                startActivity(loginWithQRIntent())
            }
        } else {
            qrLogin.setGone()
            qrDivider.setGone()
        }
    }

    private fun requestLoginHelp() {
        ErrorReportDialog().also {
            it.arguments = ErrorReportDialog.createBundle(
                appName = getString(appTypeName()),
                fromLogin = true,
                useDefaultDomain = true,
                preFill = ErrorReportPreFill(
                    title = getString(R.string.requestLoginHelp),
                    subject = getString(R.string.loginHelpSubject),
                    severity = ErrorReportAPI.Severity.BLOCKING
                )
            )
        }.show(supportFragmentManager, ErrorReportDialog.TAG)
    }

    private fun loadPreviousUsers() {
        val previousUsers = PreviousUsersUtils.get(this)
        resizePreviousUsersRecyclerView(previousUsers)

        previousLoginRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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

                        startApp()
                    }

                    override fun onRemovePreviousUserClick(user: SignedInUser, position: Int) {
                        PreviousUsersUtils.remove(this@BaseLoginLandingPageActivity, user)
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
        // Don't show the new changes view if there are previous users, it will clutter the view
        if (appChangesLink() != null) {
            changesLayout.visibility = if (previousUsers.size > 0) View.GONE else View.VISIBLE
        }

    }

    /**
     * This should be private once we have the same functionality for the teacher app, but currently we don't want to check the feature flag in teacher.
     */
    protected open fun startApp() {
        viewModel.checkCanvasForElementaryFeature().observe(this, Observer { event: Event<Boolean>? ->
            event?.getContentIfNotHandled()?.let { result: Boolean ->
                val intent = launchApplicationMainActivityIntent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("canvas_for_elementary", result)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun resizePreviousUsersRecyclerView(previousUsers: ArrayList<SignedInUser>) {
        val maxUsersToShow = resources.getInteger(R.integer.login_previousMaxVisible)
        if (previousUsers.size == 1 && maxUsersToShow > 1) {
            //Resize the view to only show one previous user
            val params = previousLoginRecyclerView.layoutParams
            params.height = resources.getDimensionPixelOffset(R.dimen.login_previousLoginHeight_1x)
            previousLoginRecyclerView.layoutParams = params
        }
    }

    private fun applyTheme() {
        // Colors
        val color = themeColor()
        val buttonColor = ContextCompat.getColor(this, R.color.login_loginFlowBlue)

        // Button
        val wrapDrawable = DrawableCompat.wrap(findMySchool.background)
        DrawableCompat.setTint(wrapDrawable, buttonColor)
        findMySchool.background = DrawableCompat.unwrap(wrapDrawable)

        // Icon
        ColorUtils.colorIt(color, canvasLogo)

        // App Name/Type. Will not be present in all layout versions
        appDescriptionType?.setTextColor(color)
        appDescriptionType?.setText(appTypeName())

        ViewStyler.setStatusBarLight(this)
    }

    private fun setupGesture() {
        gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true
        })
        rootView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
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
    private fun setupSnickerDoodles() {
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

            drawerRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
            drawerRecyclerView.adapter = SnickerDoodleAdapter(snickerDoodles) { snickerDoodle ->
                drawerLayout.closeDrawers()
                val intent = signInActivityIntent(snickerDoodle)
                intent.putExtra(SNICKER_DOODLES, snickerDoodle)
                startActivity(intent)
                finish()
            }
        } catch (e: Throwable) {
            drawerEmptyView.setVisible()
            drawerEmptyText.setVisible()
        }
    }

    override fun onTicketPost() {
        toast(R.string.errorReportThankyou)
    }

    override fun onTicketError() {
        toast(R.string.errorOccurred)
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
