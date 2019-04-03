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
import android.location.Location;
import android.location.LocationManager;

import com.instructure.loginapi.login.util.Const;

public class Locations {

    public double latitude;
    public double longitude;

    /**
     * Gets the last known location of a user, starting with GPS, if fails, NETWORK, if fails, OTHER PROVIDER if one exists
     * @param context
     * @return the users last location or null
     */
    public static Location getCurrentLocation(Context context) throws SecurityException {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        if(manager == null) {
            return null;
        }

        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if(location == null && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location =  manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(location == null && manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location =  manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        return location;
    }

    public float calculateDistanceBetweenPoints(Account account) {

        if(account.currentLocation == null) {
            return Const.NO_LOCATION_INDICATOR_INT;
        }

        Location location = new Location("School");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return account.currentLocation.distanceTo(location);
    }
}
