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
package com.instructure.teacher.adapters

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.teacher.holders.EditFavoritesViewHolder
import com.instructure.teacher.interfaces.AdapterToEditFavoriteCoursesCallback
import com.instructure.teacher.presenters.EditFavoritesPresenter
import com.instructure.teacher.viewinterface.CanvasContextView
import instructure.androidblueprint.SyncPresenter
import instructure.androidblueprint.SyncRecyclerAdapter

class EditFavoritesAdapter(
        context: Context,
        presenter: EditFavoritesPresenter,
        private val mCallback: AdapterToEditFavoriteCoursesCallback
) : SyncRecyclerAdapter<CanvasContext, EditFavoritesViewHolder, CanvasContextView>(context, presenter) {

    override fun bindHolder(canvasContext: CanvasContext, holder: EditFavoritesViewHolder, position: Int) {
        context?.let { holder.bind(it, canvasContext, mCallback)}
    }

    override fun createViewHolder(v: View, viewType: Int): EditFavoritesViewHolder {
        return EditFavoritesViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return EditFavoritesViewHolder.HOLDER_RES_ID
    }
}
