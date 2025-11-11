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

package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.viewinterface.CourseSettingsFragmentView
import com.instructure.pandautils.blueprint.FragmentPresenter
import org.greenrobot.eventbus.EventBus
import retrofit2.Response

class CourseSettingsFragmentPresenter : FragmentPresenter<CourseSettingsFragmentView>() {

    override fun loadData(forceNetwork: Boolean) {
        // TODO: Load course data?
    }

    override fun refresh(forceNetwork: Boolean) {
        // TODO: Load course data?
    }

    fun editCourseName(newName: String, course: Course) {
        CourseManager.editCourseName(course.id, newName, mEditCourseNameCallback, true)
    }

    private val mEditCourseNameCallback = object : StatusCallback<Course>() {
        override fun onResponse(response: Response<Course>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let {
                EventBus.getDefault().postSticky(CourseUpdatedEvent(it))
                viewCallback?.updateCourseName(it)
            }
        }

        override fun onFinished(type: ApiType) {
            viewCallback?.onRefreshFinished()
        }
    }

    fun editCourseHomePage(newPage: String, course: Course) {
        CourseManager.editCourseHomePage(course.id, newPage, true, mEditCourseHomePageCallback)
    }

    private val mEditCourseHomePageCallback = object : StatusCallback<Course>() {
        override fun onResponse(response: Response<Course>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let {
                EventBus.getDefault().postSticky(CourseUpdatedEvent(it))
                viewCallback?.updateCourseHomePage(it.homePage)
            }
        }

        override fun onFinished(type: ApiType) {
            viewCallback?.onRefreshFinished()
        }
    }

    fun editCourseNameClicked() {
        viewCallback?.showEditCourseNameDialog()
    }

    fun editCourseHomePageClicked(course: Course) {
        PageManager.getFrontPage(course, true, mCheckFrontPageCallback)
    }

    private val mCheckFrontPageCallback = object : StatusCallback<Page>() {
        override fun onResponse(response: Response<Page>, linkHeaders: LinkHeaders, type: ApiType) {
            val hasFrontPage = response.isSuccessful && response.body() != null
            viewCallback?.showEditCourseHomePageDialog(hasFrontPage)
        }

        override fun onFail(call: retrofit2.Call<Page>?, error: Throwable, response: Response<*>?) {
            // If the API call fails (e.g., 404 means no front page), show dialog with disabled state
            viewCallback?.showEditCourseHomePageDialog(false)
        }
    }
}
