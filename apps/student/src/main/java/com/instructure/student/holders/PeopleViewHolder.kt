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
package com.instructure.student.holders

import android.content.res.ColorStateList
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.displayType
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.util.BinderUtils
import kotlinx.android.synthetic.main.viewholder_people.view.*

class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        item: User,
        adapterToFragmentCallback: AdapterToFragmentCallback<User>,
        courseColor: Int,
        isFirstItem: Boolean,
        isLastItem: Boolean
    ) = with(itemView) {
        ProfileUtils.loadAvatarForUser(icon, item.name, item.avatarUrl, 0)
        icon.backgroundTintList = ColorStateList.valueOf(courseColor)

        itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(item, adapterPosition, true) }

        title.text = Pronouns.span(item.name, item.pronouns)

        val enrollmentIndex = item.enrollmentIndex
        if (enrollmentIndex >= 0 && enrollmentIndex < item.enrollments.size) {
            role.text = item.enrollments[item.enrollmentIndex].displayType
            role.setVisible()
        } else {
            role.text = ""
            role.setGone()
        }

        BinderUtils.updateShadows(isFirstItem, isLastItem, shadowTop, shadowBottom)
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_people
    }
}
