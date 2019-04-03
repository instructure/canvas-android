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
 *
 */

package com.instructure.androidpolling.app.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.instructure.androidpolling.app.interfaces.UpdatePoll
import com.instructure.canvasapi2.models.Poll

open class ParentFragment : Fragment(), UpdatePoll {

    private var callback: OnUpdatePollListener? = null

    // Container Activity must implement this interface
    interface OnUpdatePollListener {
        fun onUpdatePoll(poll: Poll, fragmentTag: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = context as OnUpdatePollListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement OnUpdatePollListener")
        }

    }

    open fun loadData() = Unit
    open fun reloadData() = Unit
    // Override this if we want to update the poll information
    override fun updatePoll(poll: Poll) = Unit
}
