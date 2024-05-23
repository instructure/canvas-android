/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.binders

import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.teacher.holders.UserViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback

object UserBinder {
    fun bind(user: User, adapterToFragmentCallback: AdapterToFragmentCallback<User>, position: Int, holder: UserViewHolder) = with(holder.binding) {
        // Set student avatar
        val basicUser = BasicUser()
        basicUser.name = user.name
        basicUser.pronouns = user.pronouns
        basicUser.avatarUrl = user.avatarUrl
        ProfileUtils.loadAvatarForUser(studentAvatar, basicUser.name, basicUser.avatarUrl)

        // Set student name
        userName.text = Pronouns.span(user.name, user.pronouns)

        root.setOnClickListener { adapterToFragmentCallback.onRowClicked(user, position) }

        // List enrollmentApiModel type(s)
        // Get a list of strings of the enrollments
        // Use hashSet to prevent duplicate enrollments
        val enrollments = user.enrollments
            .map { it.type }
            .toSet()

        userRole.text = android.text.TextUtils.join(", ", enrollments)
    }
}
