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

import android.content.Context;

import com.instructure.student.R;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.models.LockInfo;
import com.instructure.canvasapi2.utils.ApiPrefs;

import java.util.Date;

public class LockInfoHTMLHelper {
    public static String getLockedInfoHTML(LockInfo lockInfo, Context context, int explanationFirstLine) {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        String buttonTemplate = "<a href = \"%s\" class=\"button blue\"> %s </a>";

        //get the Locked message and make the module name bold
        String lockedMessage = "";

        if(lockInfo.getLockedModuleName() != null) {
            lockedMessage = "<p>" + String.format(context.getString(explanationFirstLine), "<b>" + lockInfo.getLockedModuleName() + "</b>") + "</p>";
        }
        if(lockInfo.getModulePrerequisiteNames().size() > 0) {
            //we only want to add this text if there are module completion requirements
            lockedMessage += context.getString(R.string.mustComplete) + "<ul>";
            for(int i = 0; i < lockInfo.getModulePrerequisiteNames().size(); i++) {
                lockedMessage +=  "<li>" + lockInfo.getModulePrerequisiteNames().get(i) + "</li>";  //"&#8226; "
            }
            lockedMessage += "</ul>";
        }

        //check to see if there is an unlocked date
        if(lockInfo.getUnlockDate() != null && lockInfo.getUnlockDate().after(new Date())) {
            lockedMessage = " ";
            String unlocked = DateHelper.getDateTimeString(context, lockInfo.getUnlockDate());
            //If there is an unlock date but no module then the assignment is Locked
            if(lockInfo.getContextModule() == null){
                lockedMessage = "<p>" + context.getString(R.string.lockedAssignmentNotModule) + "</p>";
            }
            lockedMessage += context.getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>";
        }
        //add the second line telling user how to check requirements
        //lockedMessage += "<p>" + context.getResources().getString(explanationSecondLine) + "</p>";
        //make sure we know what the protocol is (http or https)

        if (lockInfo.getContextModule() != null) {
            //create the url to modules for this course
            String url = ApiPrefs.getProtocol() + "://" + ApiPrefs.getDomain() + "/courses/" + lockInfo.getContextModule().getContextId() + "/modules";
            //create the button and link it to modules
            String linkToModules = "<center>" + String.format(buttonTemplate, url, context.getResources().getString(R.string.goToModules)) + "</center>";

            lockedMessage += linkToModules;
        }
        return lockedMessage;
    }
}
