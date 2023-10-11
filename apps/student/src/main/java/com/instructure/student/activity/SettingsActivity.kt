/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.instructure.pandautils.analytics.SCREEN_VIEW_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_SETTINGS)
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(){

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    private val binding by viewBinding(ActivitySettingsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        networkStateProvider.isOnlineLiveData.observe(this) { isOnline ->
            binding.offlineIndicator.root.setVisible(!isOnline)
        }
    }

    private val currentFragment: Fragment? get() = supportFragmentManager.fragments.last()

    fun addFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        currentFragment?.let { ft.hide(it) }
        ft.add(R.id.fragmentContainer, fragment, fragment.javaClass.name)
        ft.addToBackStack(fragment.javaClass.name)
        ft.commitAllowingStateLoss()
    }

    companion object {
         fun createIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
