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
package com.instructure.pandautils.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.blueprint.Presenter
import com.instructure.pandautils.blueprint.PresenterActivity

abstract class BasePresenterActivity<PRESENTER : Presenter<VIEW>, VIEW> : PresenterActivity<PRESENTER, VIEW>() {
    private var mCanvasContext: CanvasContext? = null

    // Only gets called if not null
    abstract override fun unBundle(extras: Bundle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.let { unBundle(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if ((topFragment as? NavigationCallbacks)?.onHandleBackPressed() == true) return
        super.onBackPressed()
    }

    /**
     * Only returns a fragment if one exists and if it was added to the backStack via FragmentManager.addToBackStack()
     * @return The top fragment in the back stack or null
     */
    private val topFragment: Fragment?
        get() {
            if (supportFragmentManager.backStackEntryCount > 0) {
                val fragments = supportFragmentManager.fragments
                if (fragments.isNotEmpty()) {
                    return fragments.getOrNull(supportFragmentManager.backStackEntryCount - 1)
                }
            } else {
                return supportFragmentManager.fragments.lastOrNull()
            }
            return null
        }
}
