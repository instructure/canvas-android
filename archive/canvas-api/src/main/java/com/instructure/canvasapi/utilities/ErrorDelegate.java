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

import android.content.Context;

import com.instructure.canvasapi.model.CanvasError;

import retrofit.RetrofitError;

public interface ErrorDelegate {
    // No Network
    void noNetworkError(RetrofitError error, Context context);

    // HTTP 401
    void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context);

    // HTTP 400-500
    void invalidUrlError(RetrofitError error, Context context);

    // HTTP 500-600
    void serverError(RetrofitError error, Context context);

    // HTTP 200 OK but unknown error or an unexpected error in the retrofit client.
    void generalError(RetrofitError error, CanvasError canvasError, Context context);
}