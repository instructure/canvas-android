/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.features.modules.progression

import android.net.Uri
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.features.discussion.DiscussionsDetailsFragment
import com.instructure.teacher.fragments.AssignmentDetailsFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModuleProgressionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ModuleProgressionRepository,
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ModuleProgressionViewData>
        get() = _data
    private val _data = MutableLiveData<ModuleProgressionViewData>()

    private val canvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)!!
    private val moduleItemId = savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID).orDefault()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _state.postValue(ViewState.Loading)

            val isDiscussionRedesignEnabled = discussionRouteHelperRepository.getEnabledFeaturesForCourse(canvasContext, true)

            val modules = repository.getModulesWithItems(canvasContext)

            val items = modules
                .flatMap { it.items }
                .map { createFragment(it, isDiscussionRedesignEnabled) to it }
                .filter { it.first != null }

            val initialPosition = items
                .indexOfFirst { it.second.id == moduleItemId }
                .takeIf { it != -1 }

            val moduleNames = items.map { item ->
                modules.find { item.second.moduleId == it.id }?.name.orEmpty()
            }

            if (initialPosition != null) {
                _data.postValue(
                    ModuleProgressionViewData(
                        items.map { it.first!! },
                        moduleNames,
                        initialPosition
                    )
                )
                _state.postValue(ViewState.Success)
            } else {
                _state.postValue(ViewState.Error())
            }
        } catch {
            _state.postValue(ViewState.Error())
        }
    }

    private fun createFragment(item: ModuleItem, isDiscussionRedesignEnabled: Boolean) = when (item.type) {
        "Page" -> PageDetailsFragment.newInstance(canvasContext, PageDetailsFragment.makeBundle(item.pageUrl.orEmpty()))
        "Assignment" -> AssignmentDetailsFragment.newInstance(canvasContext as Course, AssignmentDetailsFragment.makeBundle(item.contentId))
        "Discussion" -> if (isDiscussionRedesignEnabled) {
            DiscussionDetailsWebViewFragment.newInstance(DiscussionDetailsWebViewFragment.makeRoute(canvasContext, item.contentId))
        } else {
            DiscussionsDetailsFragment.newInstance(canvasContext, DiscussionsDetailsFragment.makeBundle(item.contentId))
        }
        "Quiz" -> QuizDetailsFragment.newInstance(canvasContext as Course, QuizDetailsFragment.makeBundle(item.contentId))
        "ExternalUrl", "ExternalTool" -> {
            val url = Uri.parse(item.htmlUrl).buildUpon().appendQueryParameter("display", "borderless").build()
            InternalWebViewFragment.newInstance(
                InternalWebViewFragment.makeBundle(
                    url.toString(),
                    item.title.orEmpty(),
                    darkToolbar = true,
                    shouldAuthenticate = true,
                    navButtonClose = false,
                    allowRoutingTheSameUrlInternally = false
                )
            )
        }
        else -> null
    }
}
