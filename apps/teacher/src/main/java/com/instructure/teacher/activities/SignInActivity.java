/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity;
import com.instructure.pandautils.services.PushNotificationRegistrationService;
import com.instructure.teacher.BuildConfig;

public class SignInActivity extends BaseLoginSignInActivity {

    @Override
    protected Intent launchApplicationMainActivityIntent() {
        PushNotificationRegistrationService.Companion.scheduleJob(this, ApiPrefs.isMasquerading(), BuildConfig.PUSH_SERVICE_PROJECT_ID);

        CookieManager.getInstance().flush();
        return SplashActivity.Companion.createIntent(this, null);
    }

    @Override
    protected String userAgent() {
        return "androidTeacher";
    }

    @Override
    protected void refreshWidgets() {
        //No Widgets in Teacher
    }

    public static Intent createIntent(Context context, AccountDomain accountDomain) {
        Intent intent = new Intent(context, SignInActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(ACCOUNT_DOMAIN, accountDomain);
        intent.putExtras(extras);
        return intent;
    }
}
