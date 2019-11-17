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

package com.instructure.student.binders

import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.displayType
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.student.holders.PeopleViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class PeopleBinder : BaseBinder() {
    companion object {

        fun bind(
                item: User,
                holder: PeopleViewHolder,
                adapterToFragmentCallback: AdapterToFragmentCallback<User>,
                courseColor: Int,
                isFirstItem: Boolean,
                isLastItem: Boolean) {

            ProfileUtils.loadAvatarForUser(holder.icon, item)

            holder.itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(item, holder.adapterPosition, true) }

            holder.icon.borderColor = courseColor
            holder.title.text = Pronouns.span(item.name, item.pronouns)


            val enrollmentIndex = item.enrollmentIndex
            if (enrollmentIndex >= 0 && enrollmentIndex < item.enrollments.size) {
                holder.role.text = item.enrollments[item.enrollmentIndex].displayType
                BaseBinder.setVisible(holder.role)
            } else {
                holder.role.text = ""
                BaseBinder.setGone(holder.role)
            }

            BaseBinder.updateShadows(isFirstItem, isLastItem, holder.shadowTop, holder.shadowBottom)
        }
    }
}
