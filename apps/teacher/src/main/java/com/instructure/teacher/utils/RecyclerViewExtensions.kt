/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.utils

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible

fun RecyclerView.setHeaderVisibilityListener(divider: View) {
    this.addOnScrollListener(object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val position = (this@setHeaderVisibilityListener.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
            if(position <= 0) {
                // Started at the bottom now we here
                divider.setInvisible()
            } else {
                divider.setVisible()
            }
        }
    })
}

