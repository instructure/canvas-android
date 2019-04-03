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

import com.instructure.androidpolling.app.BuildConfig;
import com.instructure.androidpolling.app.R;
import com.instructure.loginapi.login.activities.BaseLoginInitActivity;

import androidx.core.content.ContextCompat;

public class InitLoginActivity extends BaseLoginInitActivity {

    @Override
    protected Intent launchApplicationMainActivityIntent() {
        return StartingActivity.createIntent(getApplicationContext());
    }

    @Override
    protected Intent beginLoginFlowIntent() {
        return LoginLandingPageActivity.createIntent(this);
    }

    @Override
    protected int themeColor() {
        return ContextCompat.getColor(this, R.color.canvaspollingtheme_color);
    }

    @Override
    protected String userAgent() {
        return "androidPolling";
    }

    @Override
    protected boolean isTesting() {
        return false;
    }

    @Override
    public void finish() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.finish();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, InitLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
}
