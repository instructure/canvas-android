/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.androidfoosball.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val mContext: Context, private val mRawUsers: List<User>) : BaseAdapter() {
    private val mAvatarSize = mContext.resources.getDimension(R.dimen.avatar_size_small).toInt()

    private var mUsers = mRawUsers

    override fun getCount() = mUsers.size

    override fun getItem(position: Int) = mUsers[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

    var searchQuery = ""
        set(value) {
            field = value
            mUsers = if (value.isBlank()) {
                mRawUsers
            } else {
                mRawUsers.filter { it.name.contains(searchQuery, true) }
            }
            notifyDataSetChanged()
        }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: UserHolder
        if (view == null) {
            view = View.inflate(mContext, R.layout.adapter_user_dialog, null)
            holder = UserHolder()
            holder.userName = view.findViewById(R.id.userName)
            holder.avatar = view.findViewById(R.id.avatar)
            view.tag = holder

        } else {
            holder = view.tag as UserHolder
        }
        holder.avatar?.setAvatar(mUsers[position], mAvatarSize)
        holder.userName?.text = mUsers[position].name
        return view!!
    }

    internal class UserHolder {
        var userName: TextView? = null
        var avatar: CircleImageView? = null
    }
}
