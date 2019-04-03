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

import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.BuildConfig;
import com.instructure.loginapi.login.api.zendesk.model.ZendeskTicket;
import com.instructure.loginapi.login.api.zendesk.model.ZendeskTicketData;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.POST;

public class ZendeskAPI {

    interface ZendeskInterface{
        @POST("/tickets.json")
        void postZendeskTicket(@Body ZendeskTicket ticket, ZendeskCallback<ZendeskTicketData> callback);
    }

    private static ZendeskInterface buildInterface(ZendeskCallback<?> callback){
        RestAdapter restAdapter = ZendeskRestAdapter.buildAdapter(callback);
        return restAdapter.create(ZendeskInterface.class);
    }

    public static void postZendeskTicket(Context context, ZendeskTicket zendeskTicket, final ZendeskCallback<ZendeskTicketData> callback) {
        //Append user info to ticket body
        String ticketBody = zendeskTicket.getTicket().getComment().getBody();
        zendeskTicket.getTicket().getComment().setBody(ticketBody + getUserDataString(context));
        buildInterface(callback).postZendeskTicket(zendeskTicket, callback);
    }

    private static String getUserDataString(Context context){
        String res = "\n\n\n";
        User signedInUser = APIHelpers.getCacheUser(context);
        boolean isAnonymousDomain = APIHelpers.getDomain(context).endsWith(BuildConfig.ANONYMOUS_SCHOOL_DOMAIN);

        String versionNumber = "";
        try{
            versionNumber = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName + " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        }catch(Exception e){}

        if (!isAnonymousDomain && signedInUser != null) {
            res += "User ID   : " + Long.toString(signedInUser.getId()) +"\n";
            res += "User Name : " + signedInUser.getName() +"\n";
            res += "Email     : " + signedInUser.getEmail() +"\n";
            res += "Hostname  : " + APIHelpers.getDomain(context) +"\n";
            res += "Version   : " + versionNumber;
        } else {
            res += "Hostname  : " + APIHelpers.getDomain(context)+"\n";
            res += "Version   : " + versionNumber;
        }

        return res;
    }
}
