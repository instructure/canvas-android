/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
@file:Suppress("PackageDirectoryMismatch")

// This must have same package as AppManager, but we only want it to exist in test code
package com.instructure.student.util

import android.app.Application
import android.content.Context
import com.instructure.canvasapi2.utils.ContextKeeper
import org.robolectric.TestLifecycleApplication
import java.lang.reflect.Method

@Suppress("unused")
class TestAppManager : Application(), TestLifecycleApplication {

    override fun attachBaseContext(base: Context) {
        ContextKeeper.appContext = base
        super.attachBaseContext(base)
    }

    override fun beforeTest(method: Method?) {

    }

    override fun prepareTest(test: Any?) {

    }

    override fun afterTest(method: Method?) {

    }


}
