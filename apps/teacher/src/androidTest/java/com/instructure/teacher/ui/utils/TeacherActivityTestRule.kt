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
package com.instructure.teacher.ui.utils

import android.app.Activity
import android.content.Context
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.utils.PandaAppResetter
import com.instructure.teacher.utils.TeacherPrefs

class TeacherActivityTestRule<T: Activity>(activityClass: Class<T>) : InstructureActivityTestRule<T>(activityClass) {

    override fun performReset(context: Context) {
        PandaAppResetter.reset(context)
        TeacherPrefs.safeClearPrefs()
        PreviousUsersUtils.clear(context)
    }

}
