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

import android.content.res.Configuration
import android.os.Bundle
import instructure.androidblueprint.BaseCanvasActivity
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.BundleSaver
import com.instructure.pandautils.utils.ColorKeeper

abstract class BaseActionBarActivity : BaseCanvasActivity() {

    var toolbar: Toolbar? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        BundleSaver.restoreBundleFromDisk(savedInstanceState)
        super.onCreate(savedInstanceState)
        overrideFont()

        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        if (contentResId() != 0) {
            setContentView(contentResId())
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(showHomeAsUp())
            supportActionBar?.setDisplayShowTitleEnabled(showTitleEnabled())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        BundleSaver.saveBundleToDisk(outState)
    }

    abstract fun contentResId(): Int
    abstract fun showHomeAsUp(): Boolean
    abstract fun showTitleEnabled(): Boolean
    abstract fun onUpPressed()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onUpPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> if (supportActionBar != null) {
                supportActionBar?.openOptionsMenu()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    open fun overrideFont() {

    }
}
