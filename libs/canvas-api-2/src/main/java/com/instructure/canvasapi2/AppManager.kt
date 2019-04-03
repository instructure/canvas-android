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

package com.instructure.canvasapi2

import android.app.Application
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasAuthError
import com.instructure.canvasapi2.models.User
import com.jakewharton.threetenabp.AndroidThreeTen
import io.paperdb.Paper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Response

abstract class AppManager : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Paper.init(this)
        EventBus.getDefault().register(this)
    }

    override fun onTerminate() {
        EventBus.getDefault().unregister(this)
        super.onTerminate()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun authErrorEvent(event: CanvasAuthError) {
        validateAuthentication()
    }

    open fun validateAuthentication() {
        // Don't use weave/awaitApi; override onResponse to avoid an infinite loop of CanvasAuthError events
        UserManager.getSelf(true, object : StatusCallback<User>() {
            override fun onResponse(data: Call<User>, response: Response<User>) {
                if (!response.isSuccessful && response.code() == 401) performLogoutOnAuthError()
            }
        })
    }

    abstract fun performLogoutOnAuthError()

}
