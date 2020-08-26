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
package com.instructure.espresso.page

import android.Manifest
import android.app.Activity
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.gson.Gson
import com.instructure.espresso.BuildConfig
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ScreenshotTestRule
import com.instructure.espresso.UiControllerSingleton
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import java.io.File

abstract class InstructureTest(overrideDittoModeString: String? = null) : InstructureTestingContract {

    abstract val activityRule: InstructureActivityTestRule<out Activity>

    abstract val isTesting: Boolean


    @Rule
    override fun chain(): TestRule {
        return RuleChain
            .outerRule(ScreenshotTestRule())
            .around(activityRule)
    }

    @Before
    override fun preLaunchSetup() {
        if (!configChecked) {
            checkBuildConfig()
            configChecked = true
        }
        setupCoverageFolder()
        UiControllerSingleton.get()
    }

    // Creates an /sdcard/coverage folder if it does not already exist.
    // This is necessary for us to generate/process code coverage data.
    private fun setupCoverageFolder() {
        val dir = File(Environment.getExternalStorageDirectory(), "coverage")
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun checkBuildConfig() {
        if (!isTesting) throw RuntimeException("Build config must be IS_TESTING! (qaDebug)")
    }

    companion object {

        /* Both read & write permission are required for saving screenshots
         * otherwise the code will error with permission denied.
         * Read also required due to a bug specifically with full read/write external storage not being granted
         * https://issuetracker.google.com/issues/64389280 */
        @ClassRule
        @JvmField
        val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private var configChecked = false

    }

}
