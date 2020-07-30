package com.instructure.teacher.utils

import com.google.gson.Gson
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiPrefs.accessToken
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.instructure.teacher.BuildConfig

internal object RoboTesting {
    fun setAppStatePrefs() {
        //save token
        accessToken = BuildConfig.ROBO_USER_TOKEN
        domain = BuildConfig.ROBO_USER_DOMAIN
        val user: User = Gson().fromJson(BuildConfig.ROBO_USER_JSON, User::class.java)
        ApiPrefs.user = user
    }
}
