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
package com.instructure.student.util

import android.content.Context
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.instructure.canvasapi2.utils.ApiPrefs.protocol
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.student.R
import java.util.*

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
