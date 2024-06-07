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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.LocaleUtils
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.collapse
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.expand
import com.instructure.pandautils.utils.hide
import com.instructure.pandautils.utils.show
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.ActivityMainBinding
import com.instructure.parentapp.databinding.NavigationDrawerHeaderLayoutBinding
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.util.ParentLogoutTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var headerLayoutBinding: NavigationDrawerHeaderLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        setupNavigation()
        handleDeeplink()

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        lifecycleScope.launch {
            viewModel.data.collectLatest {
                setupNavigationDrawerHeader(it.userViewData)
                setupAppColors(it.selectedStudent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    private fun handleAction(action: MainAction) = when (action) {
        is MainAction.ShowToast -> Toast.makeText(this, action.message, Toast.LENGTH_LONG).show()
        is MainAction.LocaleChanged -> LocaleUtils.restartApp(this, LoginActivity::class.java)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = binding.drawerLayout
        appBarConfiguration = AppBarConfiguration(setOf(R.id.courses, R.id.calendar, R.id.alerts), drawerLayout)

        val toolbar = binding.toolbar
        toolbar.setNavigationIcon(R.drawable.ic_hamburger)
        toolbar.navigationContentDescription = getString(R.string.navigation_drawer_open)
        toolbar.setNavigationOnClickListener {
            openNavigationDrawer()
        }

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
        headerLayoutBinding = NavigationDrawerHeaderLayoutBinding.bind(navView.getHeaderView(0))

        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.setupWithNavController(navController)

        // Hide bottom nav on screens which don't require it
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.coursesFragment, R.id.calendarFragment, R.id.alertsFragment, R.id.help -> showMainNavigation()
                else -> hideMainNavigation()
            }
        }
    }

    private fun showMainNavigation() {
        binding.bottomNav.show()
        binding.toolbar.expand()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun hideMainNavigation() {
        binding.bottomNav.hide()
        binding.toolbar.collapse(200L)
        viewModel.closeStudentSelector()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setupAppColors(student: User?) {
        val color = ColorKeeper.getOrGenerateUserColor(student).backgroundColor()
        binding.toolbar.setBackgroundColor(color)
        binding.bottomNav.applyTheme(color, getColor(R.color.textDarkest))
        ViewStyler.setStatusBarDark(this, color)
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

    private fun setupNavigationDrawerHeader(userViewData: UserViewData?) {
        headerLayoutBinding.userViewData = userViewData
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
