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

import android.net.Uri;
import android.os.Bundle;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ModuleItem;
import com.instructure.canvasapi2.models.ModuleObject;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.interactions.router.Route;
import com.instructure.student.fragment.DiscussionDetailsFragment;
import com.instructure.student.fragment.FileDetailsFragment;
import com.instructure.student.fragment.InternalWebviewFragment;
import com.instructure.student.fragment.MasteryPathLockedFragment;
import com.instructure.student.fragment.MasteryPathSelectionFragment;
import com.instructure.student.fragment.ModuleQuizDecider;
import com.instructure.student.fragment.PageDetailsFragment;
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ModuleUtility {

    ///////////////////////////////////////////////////////////////////////////
    // Fragment Creation
    ///////////////////////////////////////////////////////////////////////////

    public static Fragment getFragment(ModuleItem moduleItem, final Course course, ModuleObject moduleObject) {
        if(moduleItem.getType() == null) {
            return null;
        }

        //deal with files
        if(moduleItem.getType().equals("File")) {
            String url = moduleItem.getUrl();
            //we just want the end of the api, not the domain

            Bundle bundle;
            url = removeDomain(url);

            if(moduleObject == null){
                return FileDetailsFragment.newInstance(FileDetailsFragment.makeRoute(course, url));
            } else {
                return FileDetailsFragment.newInstance(FileDetailsFragment.makeRoute(course, moduleObject, moduleItem.getId(), url));
            }
        }

        //deal with pages
        if (moduleItem.getType().equals("Page")) {
            return PageDetailsFragment.newInstance(PageDetailsFragment.makeRoute(course, moduleItem.getTitle(), moduleItem.getPageUrl()));
        }

        //deal with assignments
        if(moduleItem.getType().equals("Assignment")) {
            return AssignmentDetailsFragment.newInstance(AssignmentDetailsFragment.makeRoute(course, getAssignmentId(moduleItem, course)));
        }

        //deal with external urls
        if((moduleItem.getType().equals("ExternalUrl") || moduleItem.getType().equals("ExternalTool"))) {

            Uri uri =  Uri.parse(moduleItem.getHtmlUrl()).buildUpon().appendQueryParameter("display", "borderless").build();

            Route route = InternalWebviewFragment.Companion.makeRoute(course, uri.toString(), moduleItem.getTitle(), true, true, true);
            return InternalWebviewFragment.Companion.newInstance(route);
        }

        //don't do anything with headers, they're just dividers so we don't show them here.
        if((moduleItem.getType().equals("SubHeader"))) {
            return null;
        }

        // Quizzes
        if (moduleItem.getType().equals("Quiz")) {
            String apiURL = moduleItem.getUrl();
            apiURL = removeDomain(apiURL);
            return ModuleQuizDecider.newInstance(ModuleQuizDecider.makeRoute(course, moduleItem.getHtmlUrl(), apiURL));
        }

        //Discussions
        if(moduleItem.getType().equals("Discussion")) {
            Route route = getDiscussionRoute(moduleItem, course);
            return DiscussionDetailsFragment.newInstance(route);
        }

        if(moduleItem.getType().equals("Locked")) {
            Route route = MasteryPathLockedFragment.makeRoute(moduleItem.getTitle());
            return MasteryPathLockedFragment.newInstance(route);
        }

        if(moduleItem.getType().equals("ChooseAssignmentGroup")) {
            Route route = MasteryPathSelectionFragment.makeRoute(course, moduleItem.getMasteryPaths(), moduleObject.getId(), moduleItem.getMasteryPathsItemId());
            return MasteryPathSelectionFragment.newInstance(route);
        }
        //return null if there is a type we don't handle yet
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Bundle Creation
    ///////////////////////////////////////////////////////////////////////////

    @Nullable
    private static String getPageName(ModuleItem moduleItem) {
        //get the pageName from the url
        String url = moduleItem.getUrl();
        String pageName = null;
        if(url != null) {
            if(url.contains("wiki/")) {
                int index = url.indexOf("wiki/");
                if(index != -1) {
                    index += 5;
                    pageName = getPageName(url, index);
                }
            }
            else if(url.contains("pages/")) {
                int index = url.indexOf("pages/");
                if(index != -1) {
                    index += 6;
                    pageName = getPageName(url, index);
                }
            }
        }

        return pageName;
    }

    private static long getAssignmentId(ModuleItem moduleItem, Course course) {
        //get the assignment id from the url
        String url = moduleItem.getUrl();
        long assignmentId = 0;
        if(url.contains("assignments")) {
            int index = url.indexOf("assignments/");
            if(index != -1) {
                index += 12;
                assignmentId = getIdFromUrl(url, index);
            }
        }
        return assignmentId;
    }

    private static Route getDiscussionRoute(ModuleItem moduleItem, Course course) {
        // Get the topic id from the url
        String url = moduleItem.getUrl();
        long topicId = 0;
        if(url.contains("discussion_topics")) {
            int index = url.indexOf("discussion_topics/");
            if(index != -1) {
                index += 18;
                topicId = getIdFromUrl(url, index);
            }
        }
        return DiscussionDetailsFragment.makeRoute(course, topicId, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isGroupLocked(ModuleObject module) {
        // Currently the state for the group says "Locked" until the user visits the modules online, so we need
        // a different way to determine if it's Locked
        boolean isLocked = false;
        Date curDate = new Date();
        boolean isDatePassed;
        // Check if we're passed the unlock date
        if (module.getUnlockDate() != null && curDate.before(module.getUnlockDate())) return true;
        else isDatePassed = true;

        long[] ids = module.getPrerequisiteIds();
        // Is the unlock date is in the past or the state == Locked AND there are prerequisites
        if (ids != null && (isDatePassed || (ids.length > 0 && ids[0] != 0)) && module.getState() != null &&
                module.getState().equals(ModuleObject.State.Locked.getApiString())) {
            isLocked = true;
        }

        return isLocked;
    }

    private static String removeDomain(String url) {
        //strip off the domain and protocol
        int index = 0;
        String prefix = "/api/v1/";
        index = url.indexOf(prefix);
        if(index != -1) {
            url = url.substring(index + prefix.length());
        }
        return url;
    }

    private static long getIdFromUrl(String url, int index) {
        long assignmentId;
        int endIndex = url.indexOf("/", index);
        if(endIndex != -1) {
            assignmentId = Long.parseLong(APIHelper.INSTANCE.expandTildeId(url.substring(index, endIndex)));
        } else {
            assignmentId = Long.parseLong(APIHelper.INSTANCE.expandTildeId(url.substring(index)));
        }
        return assignmentId;
    }

    private static String getPageName(String url, int index) {
        String pageName;
        int endIndex = url.indexOf("/", index);
        if(endIndex != -1) {
            pageName = url.substring(index, endIndex);
        } else {
            pageName = url.substring(index);
        }

        //decode the page name in case there are special characters in the name (like {}<>|`)
        try {
            pageName = URLDecoder.decode(pageName, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LoggingUtility.LogConsole(e.getMessage());
        }

        return pageName;
    }
}
