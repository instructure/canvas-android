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

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.utils.ApiPrefs.getValidToken
import com.instructure.canvasapi2.utils.ApiPrefs.userAgent
import com.instructure.loginapi.login.BuildConfig
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.view.CanvasLoadingView
import com.instructure.loginapi.login.viewmodel.LoginViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.Utils

abstract class BaseLoginInitActivity : AppCompatActivity() {

    /**** Login Flow:
     *
     * InitLoginActivity ->
     * LoginLandingPage
     * ** OR **
     * StartApplication
     *
     * LoginLandingPage ->
     * FindSchoolActivity
     * ** OR **
     * StartApplication
     *
     * FindSchoolActivity ->
     * SignInActivity
     *
     */
    protected abstract fun beginLoginFlowIntent(): Intent?

    protected abstract fun launchApplicationMainActivityIntent(): Intent

    @ColorInt
    protected abstract fun themeColor(): Int

    protected abstract fun userAgent(): String

    protected abstract val isTesting: Boolean

    protected abstract fun logout()

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_login)
        applyTheme()
        userAgent = Utils.generateUserAgent(this, userAgent())
    }

    override fun onResume() {
        super.onResume()
        checkLoginState()
    }

    /**
     * This function checks whether or not the current user is signed in.
     */
    private fun checkLoginState() {
        val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        if (BuildConfig.IS_TESTING || isDebuggable) {
            val token = getValidToken()
            if (token.isEmpty()) {
                // Start Login Flow
                startActivity(beginLoginFlowIntent())
            } else {
                // Start App
                viewModel.checkIfTokenIsValid().observe(this, Observer { event ->
                    event.getContentIfNotHandled()?.let {
                        if (it) {
                            startApp()
                            // We only want to finish here on debug builds, our login bypass for UI testing depends
                            // on a function called by this class, which then finishes the activity.
                            // See loginWithToken() in Teacher's InitLoginActivity.
                            if (!isTesting) finish()
                        } else {
                            logout()
                        }
                    }
                })
            }
        } else {
            Handler().postDelayed({
                runOnUiThread {
                    val token = getValidToken()
                    if (token.isEmpty()) {
                        //Start Login Flow
                        startActivity(beginLoginFlowIntent())
                    } else {
                        //Start App
                        viewModel.checkIfTokenIsValid().observe(this, Observer { event ->
                            event.getContentIfNotHandled()?.let {
                                if (it) {
                                    startApp()
                                    finish()
                                } else {
                                    logout()
                                }
                            }
                        })
                    }
                }
            }, 1750) // This delay allows the animation to finish.
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
            }
        })
    }

    private fun applyTheme() {
        val loadingView = findViewById<CanvasLoadingView>(R.id.progress_bar)
        loadingView?.setOverrideColor(themeColor())
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
