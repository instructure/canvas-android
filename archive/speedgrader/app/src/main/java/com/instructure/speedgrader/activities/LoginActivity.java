/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.loginapi.login.URLSignIn;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.CanvasErrorDelegate;
import io.fabric.sdk.android.Fabric;


public class LoginActivity extends URLSignIn {

    Intent routingIntent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(com.instructure.loginapi.login.util.Const.ROUTING_CANVAS_CONEXT)
                    && bundle.containsKey(com.instructure.loginapi.login.util.Const.ROUTING_ASSINGMENT)) {
                routingIntent = intent;
            }
        }

        super.onCreate(savedInstanceState);
        final Drawable connectArrow = CanvasContextColor.getColoredDrawable(this, R.drawable.ic_cv_login_arrow, getResources().getColor(R.color.sg_defaultPrimary));
        ((ImageView)findViewById(com.instructure.loginapi.login.R.id.connect)).setImageDrawable(connectArrow);
    }

    public ErrorDelegate getErrorDelegate() {
        return new CanvasErrorDelegate();
    }

    public void startNextActivity() {
        if(((App)getApplication()).shouldShowTutorial()){
            startActivity(new Intent(LoginActivity.this, TutorialActivity.class));
            return;
        }
        if(routingIntent != null){
            startActivity(HomeActivity.createIntent(getApplicationContext(), routingIntent.getExtras()));
        } else{
            startActivity(HomeActivity.createIntent(getApplicationContext()));
        }

    }

    public void startNextActivity(Uri passedURI) {
        if(routingIntent != null){
            startActivity(HomeActivity.createIntent(getApplicationContext(), routingIntent.getExtras()));
        } else{
            startActivity(HomeActivity.createIntent(getApplicationContext(), passedURI));
        }
    }

    @Override
    public int getRootLayout() {
        return R.layout.sg_url_sign_in;
    }

    @Override
    public boolean shouldShowHelpButton() {
        return false;
    }

    public void initializeLoggingForUser(boolean isSimonFraser, User signedInUser) {
        if (!isSimonFraser) {
            Crashlytics.setUserIdentifier(Long.toString(signedInUser.getId()));
        } else {
            //Clear context for Crashlytics.
            Crashlytics.setUserIdentifier("");
        }
        Crashlytics.setString("domain", APIHelpers.getDomain(LoginActivity.this));

    }

    public void startCrashlytics() {
        Fabric.with(this, new Crashlytics());
    }

    public void startHelpShift() {}

    public void startGoogleAnalytics() {}

    public void showHelpShiftSupport() {}

    public void trackAppFlow(Activity activity) {}

    public void displayMessage(String message, int messageType) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void handleNightlyBuilds() {}

    public void refreshWidgets() {}

    public void deleteCachedFiles() {}

    @Override
    public String getPrefsFileName() {
        return App.PREF_FILE_NAME;
    }

    @Override
    public String getPrefsPreviousDomainKey() {
        return App.PREF_NAME_PREVIOUS_DOMAINS;
    }

    @Override
    public String getPrefsOtherSignedInUsersKey() {
        return App.OTHER_SIGNED_IN_USERS_PREF_NAME;
    }

    @Override
    public String getPrefsMultiUserKey() {
        return App.MULTI_SIGN_IN_PREF_NAME;
    }

    //Intent
    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    public static Intent createIntent(Context context, boolean showMessage, String message) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("showMessage", showMessage);
        intent.putExtra("messageToUser", message);
        return intent;
    }
}
