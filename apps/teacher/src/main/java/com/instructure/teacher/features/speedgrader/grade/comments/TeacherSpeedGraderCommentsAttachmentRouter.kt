/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.teacher.features.speedgrader.grade.comments

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentAttachment
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentsAttachmentRouter
import com.instructure.pandautils.utils.iconRes
import com.instructure.teacher.utils.viewMedia


class TeacherSpeedGraderCommentsAttachmentRouter : SpeedGraderCommentsAttachmentRouter {

    override fun openAttachment(activity: FragmentActivity, attachment: SpeedGraderCommentAttachment) {
        viewMedia(
            activity = activity,
            filename = attachment.displayName,
            contentType = attachment.contentType,
            url = attachment.url,
            thumbnailUrl = attachment.thumbnailUrl,
            displayName = attachment.displayName,
            iconRes = attachment.iconRes,
            fullScreen = true
        )
    }
}
