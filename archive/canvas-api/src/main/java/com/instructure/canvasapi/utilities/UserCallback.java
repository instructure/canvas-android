/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi.utilities;

import com.instructure.canvasapi.model.User;
import retrofit.client.Response;

/**
 * Class used to automatically cache users.
 * Sets the cacheUser to the user returned by this class.
 */
public abstract class UserCallback extends CanvasCallback<User> {

    public UserCallback(APIStatusDelegate statusDelegate) {
        super(statusDelegate);
    }

    public UserCallback(APIStatusDelegate statusDelegate, ErrorDelegate errorDelegate) {
        super(statusDelegate, errorDelegate);
    }

    public abstract void cachedUser(User user);
    public abstract void user(User user, Response response);

    @Override
    public void cache(User user, LinkHeaders linkHeaders, Response response) {
        cachedUser(user);
    }

    @Override
    public void firstPage(User user, LinkHeaders linkHeaders, Response response) {
        if (getContext() == null) return;
        user(user, response);
    }

    @Override
    public void success(User user, Response response) {

        // check if it's cancelled or detached
        if (getContext() == null) {
            return;
        }

        statusDelegate.onCallbackFinished(SOURCE.API);

        try {
            APIHelpers.setCacheUser(getContext(), user);
        } catch (Exception E) {}

        user(user, response);
    }
}
