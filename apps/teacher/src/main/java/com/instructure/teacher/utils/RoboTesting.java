package com.instructure.teacher.utils;

import com.google.gson.Gson;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.teacher.BuildConfig;

class RoboTesting {

    static void setAppStatePrefs() {
        //save token
        ApiPrefs.setToken(BuildConfig.ROBO_USER_TOKEN);
        ApiPrefs.setDomain(BuildConfig.ROBO_USER_DOMAIN);
        User user = new Gson().fromJson(BuildConfig.ROBO_USER_JSON, User.class);
        ApiPrefs.setUser(user);
    }

}