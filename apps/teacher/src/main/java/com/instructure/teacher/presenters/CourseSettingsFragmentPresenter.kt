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
import com.instructure.pandautils.blueprint.FragmentPresenter
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.viewinterface.CourseSettingsFragmentView
import org.greenrobot.eventbus.EventBus
import retrofit2.Response

class CourseSettingsFragmentPresenter : FragmentPresenter<CourseSettingsFragmentView>() {

    private var hasFrontPage: Boolean? = null
    private var shouldShowDialogAfterFetch = false

    override fun loadData(forceNetwork: Boolean) {
        // TODO: Load course data?
    }

    override fun refresh(forceNetwork: Boolean) {
        // TODO: Load course data?
    }

    fun prefetchFrontPageStatus(course: Course) {
        shouldShowDialogAfterFetch = false
        PageManager.getFrontPage(course, true, object : StatusCallback<Page>() {
            override fun onResponse(response: Response<Page>, linkHeaders: LinkHeaders, type: ApiType) {
                hasFrontPage = response.isSuccessful && response.body() != null
                // If no front page exists but course home is set to wiki, automatically set it to course activity stream
                if (!hasFrontPage!! && course.homePage?.apiString == "wiki") {
                    editCourseHomePage("feed", course)
                }
            }

            override fun onFail(call: retrofit2.Call<Page>?, error: Throwable, response: Response<*>?) {
                hasFrontPage = false
                // If no front page exists but course home is set to wiki, automatically set it to course activity stream
                if (course.homePage?.apiString == "wiki") {
                    editCourseHomePage("feed", course)
                }
            }
        })
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
        val cachedValue = hasFrontPage
        if (cachedValue != null) {
            // Data already loaded, show dialog immediately
            viewCallback?.showEditCourseHomePageDialog(cachedValue)
        } else {
            // Fallback: fetch if not already loaded (e.g., prefetch failed or was skipped)
            shouldShowDialogAfterFetch = true
            PageManager.getFrontPage(course, true, mCheckFrontPageCallback)
        }
    }

    private val mCheckFrontPageCallback = object : StatusCallback<Page>() {
        override fun onResponse(response: Response<Page>, linkHeaders: LinkHeaders, type: ApiType) {
            hasFrontPage = response.isSuccessful && response.body() != null
            if (shouldShowDialogAfterFetch) {
                viewCallback?.showEditCourseHomePageDialog(hasFrontPage!!)
                shouldShowDialogAfterFetch = false
            }
        }

        override fun onFail(call: retrofit2.Call<Page>?, error: Throwable, response: Response<*>?) {
            // If the API call fails (e.g., 404 means no front page), cache as false
            hasFrontPage = false
            if (shouldShowDialogAfterFetch) {
                viewCallback?.showEditCourseHomePageDialog(false)
                shouldShowDialogAfterFetch = false
            }
        }
    }
}
