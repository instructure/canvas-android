/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.model.GlobalDataSync;
import com.instructure.loginapi.login.util.Utils;

public class GlobalDataSyncGetTask extends AsyncTask<Void,Void,GlobalDataSync> {

    private Context mContext;
    private GlobalDataSyncCallbacks mCallback;
    private GlobalDataSyncAPI.NAMESPACE mNamespace;

    public interface GlobalDataSyncCallbacks {
        public void globalDataResults(GlobalDataSync data);
    }

    public GlobalDataSyncGetTask(Context context, GlobalDataSyncCallbacks callback, GlobalDataSyncAPI.NAMESPACE namespace) {
        mContext = context;
        mCallback = callback;
        mNamespace = namespace;
    }

    @Override
    protected GlobalDataSync doInBackground(Void... params) {

        GlobalDataSync data = GlobalDataSyncAPI.getGlobalData(mContext, mNamespace);

        if(data != null) {
            Utils.d(data.toString());
            //Evey time we get the data cache it.
            GlobalDataSync.setCachedGlobalData(mContext, data, mNamespace);
        }

        return data;
    }

    @Override
    protected void onPostExecute(GlobalDataSync data) {
        super.onPostExecute(data);

        if(mCallback != null) {
            mCallback.globalDataResults(data);
        }
    }
}
