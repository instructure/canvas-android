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

package com.instructure.loginapi.login.api.zendesk.utilities;

import android.content.Context;

import com.instructure.canvasapi.utilities.APIStatusDelegate;

import retrofit.Callback;

public abstract class ZendeskCallback<T> implements Callback<T> {    protected APIStatusDelegate statusDelegate;

    /**
     * @param apiStatusDelegate Delegate to get the context
     */

    public ZendeskCallback(APIStatusDelegate apiStatusDelegate) {
        statusDelegate = apiStatusDelegate;
    }

    /**
     * @return Current context, can be null
     */
    public Context getContext(){
        return statusDelegate.getContext();
    }
}
