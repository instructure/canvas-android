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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.teacher.R
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.viewinterface.CanvasContextView
import instructure.androidblueprint.SyncPresenter
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Response
import java.util.*

class EditFavoritesPresenter(filter: (Course) -> Boolean) : SyncPresenter<CanvasContext, CanvasContextView>(CanvasContext::class.java) {

    override fun loadData(forceNetwork: Boolean) {
        if(isEmpty) {
            onRefreshStarted()
            CourseManager.getCoursesTeacher(forceNetwork, mCoursesCallback)
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        mCoursesCallback.reset()
        clearData()
        loadData(forceNetwork)
    }

    //region FavoriteApiModel Courses Request

    private val mCoursesCallback = object :  StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { data.addOrUpdate(it.filter(filter).filter { it.hasActiveEnrollment() }) }
        }

        override fun onFinished(type: ApiType) {
            viewCallback?.checkIfEmpty()
            viewCallback?.onRefreshFinished()
        }
    }

    fun setFavorite(canvasContext: CanvasContext, isFavorite: Boolean) {

        val mFavoriteCallback = object : StatusCallback<Favorite>() {
            override fun onResponse(response: Response<Favorite>, linkHeaders: LinkHeaders, type: ApiType) {
                data.addOrUpdate(canvasContext)
                EventBus.getDefault().postSticky(CourseUpdatedEvent(canvasContext as Course))
            }

            override fun onFail(call: Call<Favorite>?, error: Throwable, response: Response<*>?) {
                // We already set the canvasContext to be favorited before we made the api call. Because it
                // failed we need to flip it
                (canvasContext as Course).isFavorite = !canvasContext.isFavorite
            }
        }

        if (canvasContext is Course) {
            if (isFavorite) {
                canvasContext.isFavorite = true
                mFavoriteCallback.reset()
                CourseManager.addCourseToFavorites(canvasContext.id, mFavoriteCallback, true)
            } else {
                canvasContext.isFavorite = false
                mFavoriteCallback.reset()
                CourseManager.removeCourseFromFavorites(canvasContext.id, mFavoriteCallback, true)
            }
        }
    }


    //endregion

    //region Comparison checks - Favorites API is not returning in the default ABC order as other apis
    override fun compare(o1: CanvasContext, o2: CanvasContext): Int = when {
        o1.name == null && o2.name == null -> 0 // If both are null, we'll consider them equal
        o1.name == null -> -1 // If the first name is null, but not the second, consider it less than the second
        o2.name == null -> 1 // If the second name is null, but not the first, consider it greater than the first
        else -> o1.name!!.lowercase(Locale.getDefault())
            .compareTo(o2.name!!.lowercase(Locale.getDefault())) // Normal comparison
    }
    override fun areItemsTheSame(item1: CanvasContext, item2: CanvasContext): Boolean = item1.contextId.hashCode() == item2.contextId.hashCode()
    override fun areContentsTheSame(item1: CanvasContext, item2: CanvasContext): Boolean = if (item1 is Course && item2 is Course) item1.isFavorite == item2.isFavorite else false
    //endregion
}
