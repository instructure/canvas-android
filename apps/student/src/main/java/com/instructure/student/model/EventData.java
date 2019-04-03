/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.model;

import hirondelle.date4j.DateTime;

/**
 * Small helper data class to encapsulate our "ExtraData" for the above methods
 * <p/>
 * Used by getExtraData()
 */
public class EventData {

    private DateTime dateTime;
    private EventCount eventCount;

    public enum EventCount {
        NONE, MIN, MID, MAX
    }

    public EventData(DateTime dateTime, EventCount eventCount) {
        this.eventCount = eventCount;
        this.dateTime = dateTime;
    }

    public EventCount getEventCount(){
        return eventCount;
    }

    public DateTime getDateTime(){
        return dateTime;
    }
}
