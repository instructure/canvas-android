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
package com.instructure.parentapp.features.login.createaccount

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.ActivityCreateAccountBinding
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateAccountActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityCreateAccountBinding::inflate)

    @Inject
    lateinit var navigation: Navigation

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        setContentView(binding.root)
        setupWindowInsets()
        setupNavigation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            // Apply both navigation bar and display cutout insets
            // This ensures content is not hidden behind the navigation bar OR the hole punch camera
            val leftPadding = maxOf(navigationBars.left, displayCutout.left)
            val rightPadding = maxOf(navigationBars.right, displayCutout.right)

            view.setPadding(
                leftPadding,
                0,
                rightPadding,
                0
            )
            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.graph = navigation.createAccountCreationNavGraph(navController)
    }

    override fun onBackPressed() {
        // supportFragmentManager.fragments.last() is always the NavHostFragment
        val topFragment =
            supportFragmentManager.fragments.last().childFragmentManager.fragments.last()
        if (topFragment is NavigationCallbacks && topFragment.onHandleBackPressed()) {
            return
        } else {
            super.onBackPressed()
        }
    }
}
