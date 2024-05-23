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
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.hide
import com.instructure.pandautils.utils.show
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.ActivityMainBinding
import com.instructure.parentapp.databinding.NavigationDrawerHeaderLayoutBinding
import com.instructure.parentapp.util.ParentLogoutTask
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNavigation()
        handleDeeplink()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = binding.drawerLayout
        appBarConfiguration = AppBarConfiguration(setOf(R.id.courses, R.id.calendar, R.id.alerts), drawerLayout)

        val navView = binding.navView
        navView.setNavigationItemSelectedListener {
            closeNavigationDrawer()
            when (it.itemId) {
                R.id.log_out -> {
                    onLogout()
                    true
                }

                R.id.switch_users -> {
                    onSwitchUsers()
                    true
                }

                else -> {
                    navController.navigate(it.itemId)
                    true
                }
            }
        }
        val header = NavigationDrawerHeaderLayoutBinding.bind(navView.getHeaderView(0))
        setupNavigationDrawerHeader(header)

        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.setupWithNavController(navController)

        // Hide bottom nav on screens which don't require it
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.coursesFragment, R.id.calendarFragment, R.id.alertsFragment, R.id.help -> binding.bottomNav.show()
                else -> binding.bottomNav.hide()
            }
        }
    }

    private fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun closeNavigationDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun handleDeeplink() {
        try {
            navController.handleDeepLink(intent)
        } catch (e: Exception) {
            finish()
        }
    }

    private fun onLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_warning)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ParentLogoutTask(LogoutTask.Type.LOGOUT).execute()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun onSwitchUsers() {
        ParentLogoutTask(LogoutTask.Type.SWITCH_USERS).execute()
    }

    private fun setupNavigationDrawerHeader(header: NavigationDrawerHeaderLayoutBinding) {
        ApiPrefs.user?.let {
            header.navHeaderName.text = Pronouns.span(it.shortName, it.pronouns)
            header.navHeaderEmail.text = it.primaryEmail
            ProfileUtils.loadAvatarForUser(header.navHeaderImage, it.shortName, it.avatarUrl)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeNavigationDrawer()
            return
        }

        // supportFragmentManager.fragments.last() is always the NavHostFragment
        val topFragment = supportFragmentManager.fragments.last().childFragmentManager.fragments.last()
        if (topFragment is NavigationCallbacks && topFragment.onHandleBackPressed()) {
            return
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.setData(uri)
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
