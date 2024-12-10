/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.loginapi.login.dialog.MasqueradingDialog
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.inbox.list.OnUnreadCountInvalidated
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.loadUrlIntoHeadlessWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.ActivityMainBinding
import com.instructure.parentapp.features.dashboard.InboxCountUpdater
import com.instructure.parentapp.features.splash.SplashFragment
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseCanvasActivity(), OnUnreadCountInvalidated, MasqueradingDialog.OnMasqueradingSet {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    @Inject
    lateinit var navigation: Navigation

    @Inject
    lateinit var inboxCountUpdater: InboxCountUpdater

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var oAuthApi: OAuthAPI.OAuthInterface

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupTheme()
        setupNavigation()
        handleQrMasquerading()
        scheduleAlarms()

        if (ApiPrefs.isFirstMasqueradingStart) {
            loadAuthenticatedSession()
            ApiPrefs.isFirstMasqueradingStart = false
        }
    }

    private fun loadAuthenticatedSession() {
        lifecycleScope.launch {
            oAuthApi.getAuthenticatedSession(
                ApiPrefs.fullDomain,
                RestParams(isForceReadFromNetwork = true)
            ).dataOrNull?.sessionUrl?.let {
                loadUrlIntoHeadlessWebView(this@MainActivity, it)
            }
        }
    }

    private fun handleQrMasquerading() {
        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)
        if (masqueradingUserId != 0L) {
            MasqueradeHelper.startMasquerading(masqueradingUserId, ApiPrefs.domain, MainActivity::class.java)
            finish()
        }
    }

    private fun setupTheme() {
        ThemePrefs.reapplyCanvasTheme(this)
        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeeplink(intent.data)
        showMessageExtra(intent)
    }

    private fun setupNavigation() {
        val deeplinkUri = intent.data
        intent.data = null

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)
        navController.graph = navigation.crateMainNavGraph(navController, masqueradingUserId)

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(SplashFragment.INITIAL_DATA_LOADED_KEY)?.observe(this) {
            // If the initial data has been loaded, we can navigate to courses, remove splash from backstack
            navController.popBackStack()
            navController.navigate(navigation.courses)
            navController.graph.setStartDestination(navigation.courses)

            handleDeeplink(deeplinkUri)
            showMessageExtra(intent)
        }
    }

    private fun handleDeeplink(uri: Uri?) {
        try {
            navController.navigate(uri ?: return)
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.message.orEmpty())
        }
    }

    private fun showMessageExtra(intent: Intent) {
        val message = intent.getStringExtra(Const.MESSAGE)
        if (!message.isNullOrBlank()) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        // supportFragmentManager.fragments.last() is always the NavHostFragment
        val topFragment = supportFragmentManager.fragments.last().childFragmentManager.fragments.last()
        if (topFragment is NavigationCallbacks && topFragment.onHandleBackPressed()) {
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun invalidateUnreadCount() {
        lifecycleScope.launch {
            inboxCountUpdater.updateShouldRefreshInboxCount(true)
        }
    }

    override fun onStartMasquerading(domain: String, userId: Long) {
        MasqueradeHelper.startMasquerading(userId, domain, MainActivity::class.java)
    }

    override fun onStopMasquerading() {
        MasqueradeHelper.stopMasquerading(MainActivity::class.java)
    }

    private fun scheduleAlarms() {
        lifecycleScope.launch {
            alarmScheduler.scheduleAllAlarmsForCurrentUser()
        }
    }

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = uri
            return intent
        }

        fun createIntent(context: Context, masqueradingUserId: Long): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(Const.QR_CODE_MASQUERADE_ID, masqueradingUserId)
            return intent
        }
    }
}
