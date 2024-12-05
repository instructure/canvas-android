/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
 */
package com.instructure.student.activity

import android.os.Bundle
import com.instructure.pandautils.base.BaseCanvasActivity
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.pandautils.analytics.SCREEN_VIEW_STUDENT_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.student.R
import com.instructure.student.databinding.ActivityStudentViewStarterBinding

// The sole purpose of this activity is to capture the intent from the Teacher app that signals the Student app
// to start the Student view
@ScreenView(SCREEN_VIEW_STUDENT_VIEW)
class StudentViewStarterActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityStudentViewStarterBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.loadingView.setOverrideColor(ContextCompat.getColor(this, R.color.login_studentAppTheme))

        val extras = intent.extras!!

        val domain: String = extras.getString(Const.DOMAIN, "")
        val token: String = extras.getString(Const.TOKEN, "")
        val clientId = extras.getString(Const.CLIENT_ID, ApiPrefs.clientId)
        val clientSecret = extras.getString(Const.CLIENT_SECRET, ApiPrefs.clientSecret)
        val courseId = extras.getLong(Const.COURSE_ID)
        val isElementary = extras.getBoolean(Const.IS_ELEMENTARY, false)

        MasqueradeHelper.startMasquerading(
            masqueradingUserId = -1, // This will be retrieved when we get the test user
            masqueradingDomain = domain,
            startingClass = NavigationActivity::class.java,
            masqueradeToken = token,
            masqueradeClientId = clientId,
            masqueradeClientSecret = clientSecret,
            courseId = courseId,
            isElementary = isElementary
        )
    }
}