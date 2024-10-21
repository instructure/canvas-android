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

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.LaunchDefinitionsManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.events.CourseColorOverlayToggledEvent
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.viewinterface.InitActivityView
import instructure.androidblueprint.Presenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class InitActivityPresenter : Presenter<InitActivityView> {

    // Before the view is ready to be used we have a few pieces of data we need to get
    // The data can be from cache or network

    private var view: InitActivityView? = null
    private var apiCall: WeaveJob? = null
    private var colorOverlayJob: Job? = null
    private var unreadCountJob: Job? = null

    override fun onViewAttached(view: InitActivityView): InitActivityPresenter {
        this.view = view
        return this
    }

    fun loadData(forceNetwork: Boolean) {
        // Get the to do count
        apiCall = tryWeave {
            // Get To dos
            val todos = awaitApi<List<ToDo>> { ToDoManager.getUserTodos(it, forceNetwork) }
            // Now count now students need grading
            val count = todos.sumOf { it.needsGradingCount }
            view?.updateTodoCount(count)

            val launchDefinitions = awaitApi { LaunchDefinitionsManager.getLaunchDefinitions(it, false) }
            launchDefinitions?.let {
                view?.gotLaunchDefinitions(it)
            }

            val inboxUnreadCount = awaitApi<UnreadConversationCount> { UnreadCountManager.getUnreadConversationCount(it, true) }
            val unreadCountInt = (inboxUnreadCount.unreadCount ?: "0").toInt()
            view?.updateInboxUnreadCount(unreadCountInt)
        } catch {
            it.printStackTrace()
        }
    }

    fun updateUnreadCount() {
        unreadCountJob = tryWeave {
            val inboxUnreadCount = awaitApi<UnreadConversationCount> { UnreadCountManager.getUnreadConversationCount(it, true) }
            val unreadCountInt = (inboxUnreadCount.unreadCount ?: "0").toInt()
            view?.updateInboxUnreadCount(unreadCountInt)
        } catch {
            it.printStackTrace()
        }
    }

    fun setHideColorOverlay(hide: Boolean) {
        colorOverlayJob?.cancel()
        colorOverlayJob = GlobalScope.launch(Dispatchers.Main) {
            UserManager.setHideColorOverlay(hide).await()
                .onSuccess {
                    TeacherPrefs.hideCourseColorOverlay = it.hideDashCardColorOverlays
                    CanvasRestAdapter.clearCacheUrls("""/users/self/settings""")
                    EventBus.getDefault().post(CourseColorOverlayToggledEvent())
                    view?.updateColorOverlaySwitch(!it.hideDashCardColorOverlays, false)
                }
                .onFailure {
                    view?.updateColorOverlaySwitch(hide, true)
                }
        }
    }

    override fun onViewDetached() {
        view = null
    }

    override fun onDestroyed() {
        apiCall?.cancel()
    }
}
