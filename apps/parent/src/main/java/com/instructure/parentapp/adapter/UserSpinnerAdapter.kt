/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */    package com.instructure.parentapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.GlideApp
import com.instructure.parentapp.R
import kotlinx.android.synthetic.main.spinner_user.view.*

class UserSpinnerAdapter(context: Context, users: Array<User>) : ArrayAdapter<User>(context, 0, users) {

    private val inflater = LayoutInflater.from(context)

    private val placeholder by lazy {
        ColorUtils.colorIt(context, Color.WHITE, R.drawable.ic_cv_user)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            (convertView ?: inflater.inflate(R.layout.spinner_user, parent, false)).apply {
                val user = getItem(position)
                GlideApp.with(avatar).load(user?.avatarUrl).placeholder(placeholder).error(placeholder).into(avatar)
                userName.text = user?.shortName ?: ""
            }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView as? TextView ?: inflater.inflate(DROPDOWN_RES_ID, parent, false) as TextView).apply {
            text = getItem(position)?.shortName ?: ""
        }
    }

    companion object {
        private const val DROPDOWN_RES_ID = R.layout.support_simple_spinner_dropdown_item
    }
}