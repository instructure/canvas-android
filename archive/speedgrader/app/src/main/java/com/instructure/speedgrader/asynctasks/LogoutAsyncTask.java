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

package com.instructure.speedgrader.asynctasks;

import android.content.Intent;
import android.os.AsyncTask;
import com.instructure.canvasapi.utilities.FileUtilities;
import com.instructure.speedgrader.activities.LoginActivity;
import com.instructure.speedgrader.activities.ParentActivity;
import com.instructure.speedgrader.util.App;

import java.io.File;

public class LogoutAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private ParentActivity parentActivity;
    private String messageToUser;


    public LogoutAsyncTask(ParentActivity parentActivity, String messageToUser) {
        this.parentActivity = parentActivity;
        this.messageToUser = messageToUser;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //start indeterminate progress circle
        if(parentActivity != null){
//            parentActivity.showProgress();
        }
    }

    @Override

    protected Boolean doInBackground(Void... params) {
        if (parentActivity == null) return false;

        return ((App) parentActivity.getApplication()).logoutUser();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (parentActivity == null) return;

//        parentActivity.hideProgress();

        if (result) {
            File cacheDir = new File(parentActivity.getFilesDir(), "cache");
            File exCacheDir = App.getAttachmentsDirectory(parentActivity);
            //remove the cached stuff for masqueraded user
            File masqueradeCacheDir = new File(parentActivity.getFilesDir(), "cache_masquerade");
            //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
            //need to delete the contents of the external cache folder so previous user's results don't show up on incorrect user
            FileUtilities.deleteAllFilesInDirectory(masqueradeCacheDir);
            FileUtilities.deleteAllFilesInDirectory(cacheDir);
            FileUtilities.deleteAllFilesInDirectory(exCacheDir);

            //TODO: this MOST LIKELY won't work if there are items on the back-stack.
            Intent intent;
            if(messageToUser != null){
                intent = LoginActivity.createIntent(parentActivity, true, messageToUser);
            } else {
                intent = LoginActivity.createIntent(parentActivity);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            parentActivity.startActivity(intent);
            parentActivity.finish();
        } else {
//            AppMsg.makeText(parentActivity, parentActivity.getResources().getString(R.string.noDataConnection), AppMsg.STYLE_ERROR).show();
        }
    }
}