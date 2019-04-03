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

package com.instructure.teacheraid.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.asynctask.LogoutAsyncTask;

import retrofit.RetrofitError;

public class CanvasErrorDelegate implements ErrorDelegate {

    @Override
    public void noNetworkError(RetrofitError error, Context context) {
        //Unsafe casts cause crashes.
        if(context instanceof Activity){
            Toast.makeText(context, context.getString(R.string.noDataConnection), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {
        //If the Access_token is Invalid then Log them out.
        if (canvasError != null && canvasError.getMessage().equals("Invalid access token.") && context instanceof Activity) {

            new LogoutAsyncTask((Activity) context, context.getString(R.string.invalidAccessToken)).execute();

            return;
        }

        //Unsafe casts cause crashes.
        if (context instanceof Activity) {
            Toast.makeText(context, context.getString(R.string.unauthorized), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void invalidUrlError(RetrofitError error, Context context) {
        //Unsafe casts cause crashes.
        if(context instanceof Activity){
            Toast.makeText(context, context.getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void serverError(RetrofitError error, Context context) {
        //Unsafe casts cause crashes.
        if(context instanceof Activity){
            Toast.makeText(context, context.getString(R.string.serverError), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void generalError(RetrofitError error,CanvasError canvasError, Context context) {
        //Unsafe casts cause crashes.

        if(context instanceof Activity){
            Toast.makeText((Activity) context, context.getString(R.string.errorOccurred), Toast.LENGTH_SHORT).show();
        }

    }
}
