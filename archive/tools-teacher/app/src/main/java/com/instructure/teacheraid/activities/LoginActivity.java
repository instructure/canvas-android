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
 */

package com.instructure.teacheraid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.loginapi.login.URLSignIn;
import com.instructure.loginapi.login.util.Const;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.ApplicationManager;
import com.instructure.teacheraid.util.CanvasErrorDelegate;


public class LoginActivity extends URLSignIn {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //we don't want any help icons because we don't have any zendesk or web resources to link to
        getCanvasHelpIconView().setVisibility(View.GONE);
        getCanvasHelpView().setVisibility(View.GONE);
    }

    @Override
    public ErrorDelegate getErrorDelegate() {
        return new CanvasErrorDelegate();
    }

    @Override
    public void startNextActivity() {
        startActivity(BaseActivity.createIntent(LoginActivity.this));

        finish();
    }

    @Override
    public void startNextActivity(Uri passedURI) {
        startActivity(BaseActivity.createIntent(LoginActivity.this, passedURI));
        finish();
    }

    @Override
    public void initializeLoggingForUser(boolean isSimonFraser, User signedInUser) {



    }

    @Override
    public void startCrashlytics() {
    }

    @Override
    public void startHelpShift() {

    }

    @Override
    public void startGoogleAnalytics() {
       // EasyTracker easyTracker = EasyTracker.getInstance(LoginActivity.this);
       // easyTracker.send(MapBuilder.createEvent("app_flow", "User Signed In", "WelcomeScreen", null).build());
    }

    @Override
    public void showHelpShiftSupport() { }

    @Override
    public void trackAppFlow(Activity activity) {
        //Analytics.trackAppFlow(LoginActivity.this);
    }

    @Override
    public void displayMessage(String message, int messageType) {
        if(!TextUtils.isEmpty(message)){
           Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getRootLayout() {
        return R.layout.teacher_tools_url_sign_in;
    }

    @Override
    public String getPrefsFileName() {
        return ApplicationManager.PREF_FILE_NAME;
    }

    @Override
    public String getPrefsPreviousDomainKey() {
        return ApplicationManager.PREF_NAME_PREVIOUS_DOMAINS;
    }

    @Override
    public String getPrefsOtherSignedInUsersKey() {
        return ApplicationManager.OTHER_SIGNED_IN_USERS_PREF_NAME;
    }

    @Override
    public String getPrefsMultiUserKey() {
        return ApplicationManager.MULTI_SIGN_IN_PREF_NAME;
    }

    @Override
    public boolean shouldShowHelpButton() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intents
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Const.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }


    public static Intent createIntent(Context context, boolean showMessage, String message) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Const.SHOW_MESSAGE, showMessage);
        intent.putExtra(Const.MESSAGE_TO_USER, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
