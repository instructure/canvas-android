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

package com.instructure.student.util;

import android.app.Activity;
import android.app.Application;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.utils.ApiPrefs;

import java.util.List;

public class Analytics {

    public static void trackEnrollment(Activity context, List<Course> courseList) {

        if(context == null || courseList == null){
            return;
        }

        String enrollment = "Unknown";

        //used to track how many teacher/student enrollments
        int teacherCount = 0;
        int studentCount = 0;
        int observerCount = 0;
        for(Course course : courseList) {
            if(course.isTeacher()) {
                teacherCount++;
            }
            else if(course.isStudent()) {
                studentCount++;
            }
            else if(course.isObserver()) {
                observerCount++;
            }
        }
        //Google Analytics will only track the last custom dimension set because it is a user based
        //variable. This will set the custom dimension as whatever role is set the most
        if(studentCount > teacherCount && studentCount > observerCount) {
            // Set the dimension value for index 1 (Enrollment in GA, it is a 1 based list).
            enrollment = "Student";
        }
        else if(teacherCount >= studentCount && teacherCount >= observerCount) {
            // Set the dimension value for index 1.(Enrollment in GA, it is a 1 based list).
            enrollment = "Teacher";
        }
        else {
            // Set the dimension value for index 1.(Enrollment in GA, it is a 1 based list).
            enrollment = "Observer";

        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackEnrollment(enrollment);
        }
    }
    public static void trackDomain(Activity context){
        if(context == null){ 
            return;
        }

        //Get Domain
        String domain = ApiPrefs.getDomain();

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackDomain(domain);
        }
    }

    public static void trackCanvasPollData(Activity context, String dataToTrack, boolean isTablet) {

        if(context == null || TextUtils.isEmpty(dataToTrack)) {
            return;
        }

        String deviceType = (isTablet ? "TABLET" : "PHONE");

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackEvent("CanvasPoll_1", deviceType, dataToTrack, 0);
        }
    }

    public static void trackUIDuration(Activity context, String pageName, long duration){

        if(context == null || pageName == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackTiming("UI Duration", pageName, null, duration);
        }
    }

    public static void trackBookmarkCreated(Activity context) {
        if(context == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackUIEvent("Bookmarker Created", "Bookmarker", 512512L);
        }
    }

    public static void trackButtonPressed(Activity context, String buttonName, Long buttonValue){

        if(context == null || buttonName == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackButtonPressed(buttonName, buttonValue);
        }
    }

    public static void trackLandingPage(Activity context, String buttonName, Long buttonValue){

        if(context == null || buttonName == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackUIEvent("Landing Page", buttonName, buttonValue);
        }
    }

    private static void trackAppFlow(Activity context, String pageName){

        if(context == null || pageName == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackScreen(pageName);
        }
    }

    public static void trackWidgetFlow(Activity context, String widgetName){
        trackAppFlow(context, widgetName);
    }


    public static void trackAppFlow(Activity currentActivity){
        trackAppFlow(currentActivity, currentActivity.getClass().getSimpleName());
    }

    public static void trackAppFlow(Fragment currentFragment){
        if(currentFragment == null || currentFragment.getActivity() == null) {
            return;
        }

        trackAppFlow(currentFragment.getActivity(), currentFragment.getClass().getSimpleName());
    }

    public static void trackAppFlow(Activity context, Fragment currentFragment){
        if(currentFragment == null || context == null) {
            return;
        }

        trackAppFlow(context, currentFragment.getClass().getSimpleName());
    }

    public static void trackAppFlow(Activity context, Class cls){
        if(cls == null || context == null) {
            return;
        }

        trackAppFlow(context, cls.getSimpleName());
    }

    public static void trackColorSelected(Activity context, int courseColor, boolean isCourse) {
        if(context == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackUIEvent("Course color selected", Integer.toString(courseColor), isCourse ? 1L : 0);
        }
    }

    public static void trackBookmarkSelected(Activity context, String className) {
        if(context == null || className == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackUIEvent("Bookmarker selected", className, 0);
        }
    }

    public static void trackUnsupportedFeature(Activity context, String featureName) {
        if(context == null || featureName == null){
            return;
        }

        Application application = context.getApplication();
        if(application instanceof AnalyticsEventHandling) {
            ((AnalyticsEventHandling)application).trackUIEvent("Unsupported feature link selected", featureName, 0);
        }
    }
}
