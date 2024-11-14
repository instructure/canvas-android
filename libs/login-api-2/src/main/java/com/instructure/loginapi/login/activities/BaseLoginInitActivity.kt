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
import com.instructure.canvasapi2.utils.ApiPrefs.getValidToken
import com.instructure.canvasapi2.utils.ApiPrefs.userAgent
import com.instructure.loginapi.login.BuildConfig
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.R
import com.instructure.pandautils.views.CanvasLoadingView
import com.instructure.loginapi.login.viewmodel.LoginViewModel
import com.instructure.pandautils.utils.Utils
import javax.inject.Inject

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

    @ColorInt
    protected abstract fun themeColor(): Int

    protected abstract fun userAgent(): String

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var navigation: LoginNavigation

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
                navigation.startLogin(viewModel, true)
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
                        navigation.startLogin(viewModel, true)
                    }
                }
            }, 1750) // This delay allows the animation to finish.
        }
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
