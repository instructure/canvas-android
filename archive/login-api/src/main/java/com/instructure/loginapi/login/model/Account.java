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

package com.instructure.loginapi.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.instructure.canvasapi.model.AccountDomain;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.util.Const;

import java.util.ArrayList;

public class Account {

    public String name = "";
    public String domain = "";
    public Locations[] locations = null;
    public android.location.Location currentLocation = null;
    public Locations closestSchoolLocation = null;
    public float distanceInMeters = 0;
    public double distanceInMiles = 0;

    public String getDistanceString(Context context) {

        if(domain != null && domain.equals(Const.URL_CANVAS_NETWORK)) {
            return context.getString(R.string.loginRightBehindYou);
        } else {
            final String distance = String.valueOf(distanceInMiles);
            if(TextUtils.isEmpty(distance)) {
                return "";
            }

            int dec = distance.indexOf('.');
            String subString = distance.substring(0, Math.min(distance.length(), dec + 2));
            return String.format(context.getString(R.string.loginMiles), subString);
        }
    }

    public static ArrayList<AccountDomain> scrubList(ArrayList<AccountDomain> accounts) {

        if(accounts == null) {
            return new ArrayList<>();
        }

        final ArrayList<AccountDomain> alteredAccounts = new ArrayList<>();

        for(AccountDomain account : accounts) {

            if(account.getDistance() != null && account.getDistance()* 0.000621371192237334 < 51.0) {
                alteredAccounts.add(account);
            }
        }
        return alteredAccounts;
    }
}
