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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.inbox.list.OnUnreadCountInvalidated
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.ActivityMainBinding
import com.instructure.parentapp.features.dashboard.InboxCountUpdater
import com.instructure.parentapp.features.splash.SplashFragment
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnUnreadCountInvalidated {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    @Inject
    lateinit var navigation: Navigation

    @Inject
    lateinit var inboxCountUpdater: InboxCountUpdater

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupTheme()
        setupNavigation()
    }

    private fun setupTheme() {
        ThemePrefs.reapplyCanvasTheme(this)
        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeeplink(intent.data)
    }

    private fun setupNavigation() {
        val deeplinkUri = intent.data
        intent.data = null

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.graph = navigation.crateMainNavGraph(navController)

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(SplashFragment.INITIAL_DATA_LOADED_KEY)?.observe(this) {
            // If the initial data has been loaded, we can navigate to courses, remove splash from backstack
            navController.popBackStack()
            navController.navigate(navigation.courses)
            navController.graph.setStartDestination(navigation.courses)

            handleDeeplink(deeplinkUri)
        }
    }

    private fun handleDeeplink(uri: Uri?) {
        try {
            navController.navigate(uri ?: return)
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.message.orEmpty())
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

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = uri
            return intent
        }

        fun createIntent(context: Context, masqueradingUserId: Long): Intent {
            val intent = Intent(context, MainActivity::class.java)
            // TODO: Implement masquerading
            intent.putExtra(Const.QR_CODE_MASQUERADE_ID, masqueradingUserId)
            return intent
        }
    }
}
