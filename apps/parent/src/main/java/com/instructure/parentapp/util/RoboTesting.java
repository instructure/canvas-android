package com.instructure.parentapp.util;

import android.content.Context;

import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;

class RoboTesting {

    static void setAppStatePrefs(Context context) {
        //save parent id and user name
        Prefs prefs = new Prefs(context, context.getString(R.string.app_name_parent));
        prefs.save(com.instructure.pandautils.utils.Const.ID, BuildConfig.ROBO_TEST_USER_ID);
        prefs.save(com.instructure.pandautils.utils.Const.NAME, BuildConfig.ROBO_TEST_USERNAME);

        //save token
        ApiPrefs.setToken(BuildConfig.ROBO_TEST_API_KEY);

        //Set domain to gamma
        ApiPrefs.setAirwolfDomain(BuildConfig.GAMMA_DOMAIN);
    }

}
