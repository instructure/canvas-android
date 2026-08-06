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
 */
package com.instructure.pandautils.utils

import android.content.Context
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.instructure.canvasapi2.utils.ApiPrefs.protocol
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import java.util.Date

/**
 * Moved here from the Student app's `com.instructure.student.util.LockInfoHTMLHelper` so it can be
 * shared by ViewModels living in `:pandautils` (e.g. `PageDetailsViewModel`), which can't depend on
 * app-module classes. Behavior is unchanged.
 */
object LockInfoHTMLHelper {
    fun getLockedInfoHTML(lockInfo: LockInfo, context: Context, explanationFirstLine: Int, addModulesLink: Boolean = true): String {
        /* Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
        like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
        not look as good. The blue button will just be a link. */

        // Get the Locked message and make the module name bold
        var lockedMessage = ""
        lockInfo.lockedModuleName?.let { name ->
            lockedMessage = "<p>" + context.getString(explanationFirstLine, "<b>$name</b>") + "</p>"
        }

        if (lockInfo.modulePrerequisiteNames!!.size > 0) {
            // We only want to add this text if there are module completion requirements
            lockedMessage += context.getString(R.string.mustComplete) + "<ul>"
            lockInfo.modulePrerequisiteNames?.forEach { lockedMessage += "<li>$it</li>" }
            lockedMessage += "</ul>"
        }

        // Check to see if there is an unlocked date
        if (lockInfo.unlockDate != null && lockInfo.unlockDate!!.after(Date())) {
            lockedMessage = " "
            val unlocked = DateHelper.getDateTimeString(context, lockInfo.unlockDate)
            // If there is an unlock date but no module then the assignment is Locked
            if (lockInfo.contextModule == null) {
                lockedMessage = "<p>" + context.getString(R.string.lockedAssignmentNotModule) + "</p>"
            }
            lockedMessage += "${context.getString(R.string.unlockedAt)}<ul><li>$unlocked</li></ul>"
        }

        // Make sure we know what the protocol is (http or https)
        if (addModulesLink) {
            lockInfo.contextModule?.let { module ->
                // Create the url to modules for this course
                val url = "$protocol://$domain/courses/${module.contextId}/modules"
                // Create the button and link it to modules
                lockedMessage += """<center><a href="$url" class="button blue">${context.resources.getString(R.string.goToModules)}</a></center>"""
            }
        }
        return lockedMessage
    }
}
