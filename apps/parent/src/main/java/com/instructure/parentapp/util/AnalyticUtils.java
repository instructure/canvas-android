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
 *
 */
package com.instructure.parentapp.util;


import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

public class AnalyticUtils {

    //Button presses / Events
    public static final String DISMISS_ALERT = "Dismiss alert";
    public static final String REMOVE_STUDENT = "Remove student";
    public static final String LOG_OUT = "Log out";
    public static final String MODIFY_THRESHOLD = "Modify threshold";
    public static final String HELP = "Help";
    public static final String ADD_STUDENT = "Add student";
    public static final String WEEK_NAV_NEXT = "Week navigation next";
    public static final String WEEK_NAV_PREVIOUS = "Week navigation previous";
    public static final String SWIPE_STUDENT = "Swipe student";
    public static final String REMINDER_ASSIGNMENT = "Reminder assignment set";
    public static final String REMINDER_EVENT = "Reminder event set";
    public static final String COURSE_SELECTED = "Course selected";
    public static final String WEEK_VIEW_SELECTED = "Week event selected";
    public static final String ALERT_ITEM_SELECTED = "Alert item selected";

    //Flows
    public static final String COURSE_FLOW = "Courses";
    public static final String WEEK_FLOW = "Week";
    public static final String ALERT_FLOW = "Alert";


    /**
     * Use Fabric to track a button press
     * @param buttonName Name of the button or event
     */
    public static void trackButtonPressed(String buttonName) {
        Answers.getInstance().logCustom(new CustomEvent("Button Pressed")
                .putCustomAttribute("Name", buttonName));
    }


    /**
     * Use Fabric to try to keep track of different flows through the app to see how people get to places
     *
     * @param flow Courses, Week, or Alert
     * @param event
     */
    public static void trackFlow(String flow, String event) {
        Answers.getInstance().logCustom(new CustomEvent(flow)
                .putCustomAttribute("Event", event));
    }
}
