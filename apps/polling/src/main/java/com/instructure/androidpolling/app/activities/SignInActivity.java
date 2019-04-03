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

package com.instructure.androidpolling.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity;

public class SignInActivity extends BaseLoginSignInActivity {

    @Override
    protected Intent launchApplicationMainActivityIntent() {
        CookieManager.getInstance().flush();
        return StartingActivity.createIntent(getApplicationContext());
    }

    @Override
    protected String userAgent() {
        return "androidPolling";
    }

    @Override
    protected void refreshWidgets() {
        //No widgets, do nothing
    }

    public static Intent createIntent(Context context, AccountDomain accountDomain) {
        Intent intent = new Intent(context, SignInActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(ACCOUNT_DOMAIN, accountDomain);
        intent.putExtras(extras);
        return intent;
    }
}
