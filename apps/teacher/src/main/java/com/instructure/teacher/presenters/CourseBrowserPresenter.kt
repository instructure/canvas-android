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

import com.instructure.canvasapi2.apis.AttendanceAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.LaunchDefinitionsManager
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.teacher.viewinterface.CourseBrowserView
import kotlinx.coroutines.Job

class CourseBrowserPresenter(val canvasContext: CanvasContext, val filter: (Tab, Long) -> Boolean) : SyncPresenter<Tab, CourseBrowserView>(Tab::class.java) {

    var mApiCalls: Job? = null

    override fun loadData(forceNetwork: Boolean) {
        if(!forceNetwork && data.size() > 0)  return

        onRefreshStarted()

        mApiCalls = tryWeave {
            // Check to see if we should show the student view tab - skip if Student View isn't enabled
            val canUseStudentView = awaitApiResponse<CanvasContextPermission> {
                CourseManager.getCoursePermissions(
                    canvasContext.id,
                    emptyList(),
                    it,
                    true
                )
            }

            val tabs = awaitApi<List<Tab>> { TabManager.getTabs(canvasContext, it, forceNetwork) }
                .filter { !(it.isExternal && it.isHidden) } // We don't want to list external tools that are hidden
                .toMutableList().apply {
                    if (canUseStudentView.isSuccessful && canUseStudentView.body()?.canUseStudentView == true)
                        // Add extra tab for the student view and make sure it's at the very end of the list
                        add(Tab(tabId = Tab.STUDENT_VIEW, position = 1000))
                }.toList() // Turn back into a non-mutable list
            val launchDefinitions = awaitApi<List<LaunchDefinition>> {
                LaunchDefinitionsManager.getLaunchDefinitionsForCourse((canvasContext as? Group)?.courseId ?: canvasContext.id, it, forceNetwork)
            }

            var attendanceId: Long = 0

            launchDefinitions.forEach {
                val ltiDefinitionUrl = it.placements?.courseNavigation?.url
                if (ltiDefinitionUrl != null && (
                        ltiDefinitionUrl.contains(AttendanceAPI.BASE_DOMAIN) ||
                                ltiDefinitionUrl.contains(AttendanceAPI.BASE_TEST_DOMAIN))) {
                    // Has rollcall (Attendance) installed, show tool
                    attendanceId = it.definitionId ?: 0L
                }
            }

            data.addOrUpdate(tabs.filter {
                filter(it, attendanceId)
            })

            viewCallback?.let {
                it.onRefreshFinished()
                it.checkIfEmpty()
            }
        } catch {
            it.cause?.printStackTrace()
            viewCallback?.let {
                it.onRefreshFinished()
                it.checkIfEmpty()
            }
        }
    }

    fun handleStudentViewClick() {
        if (viewCallback?.isStudentInstalled() == true)
            viewCallback?.showStudentView()
        else
            viewCallback?.gotoStudentPlayStoreListing()
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        mApiCalls?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun compare(item1: Tab, item2: Tab) = item1.position.compareTo(item2.position)
    override fun areItemsTheSame(item1: Tab, item2: Tab) = item1.id == item2.id

    override fun onDestroyed() {
        super.onDestroyed()
        mApiCalls?.cancel()
    }
}
