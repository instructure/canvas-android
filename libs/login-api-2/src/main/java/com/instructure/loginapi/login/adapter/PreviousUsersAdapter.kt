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
package com.instructure.loginapi.login.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.Pronouns.span
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.pandautils.utils.ProfileUtils
import kotlinx.android.synthetic.main.adapter_previous_users.view.*

class PreviousUsersAdapter(
    private val previousUsers: ArrayList<SignedInUser>,
    private val callback: PreviousUsersEvents
) : RecyclerView.Adapter<PreviousUserHolder>() {
    interface PreviousUsersEvents {
        fun onPreviousUserClick(user: SignedInUser)
        fun onRemovePreviousUserClick(user: SignedInUser, position: Int)
        fun onNowEmpty()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousUserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_previous_users, parent, false)
        return PreviousUserHolder(view)
    }

    override fun onBindViewHolder(holder: PreviousUserHolder, position: Int) {
        val user = previousUsers[position]
        holder.bind(
            user = user,
            onUserClick = { callback.onPreviousUserClick(user) },
            onUserRemove = {
                callback.onRemovePreviousUserClick(user, position)
                previousUsers.removeAt(position)
                notifyItemRemoved(position)
                if (previousUsers.size == 0) {
                    callback.onNowEmpty()
                }
            }
        )
    }

    override fun getItemCount(): Int = previousUsers.size
}

class PreviousUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user: SignedInUser, onUserClick: () -> Unit, onUserRemove: () -> Unit) = with(itemView) {
        ProfileUtils.loadAvatarForUser(usersAvatar, user.user.name, user.user.avatarUrl, 0)
        userName.text = span(user.user.name, user.user.pronouns)
        schoolDomain.text = user.domain
        setOnClickListener { onUserClick() }
        removePreviousUser.setOnClickListener { onUserRemove() }
    }
}
