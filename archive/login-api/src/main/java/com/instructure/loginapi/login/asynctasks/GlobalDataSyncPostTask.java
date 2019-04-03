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

public class GlobalDataSyncPostTask extends AsyncTask<GlobalDataSync,Void,Void>{

    private GlobalDataSyncAPI.NAMESPACE mNamespace;
    private Context mContext;

    public GlobalDataSyncPostTask(Context context, GlobalDataSyncAPI.NAMESPACE namespace) {
        mContext = context;
        mNamespace = namespace;
    }

    @Override
    protected Void doInBackground(GlobalDataSync... params) {

        GlobalDataSyncAPI.setGlobalData(mContext, mNamespace, params[0]);
        GlobalDataSync.setCachedGlobalData(mContext, params[0], mNamespace);

        return null;
    }
}
