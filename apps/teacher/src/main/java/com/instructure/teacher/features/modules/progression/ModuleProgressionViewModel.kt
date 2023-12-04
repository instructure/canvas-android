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

import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.teacher.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModuleProgressionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: ModuleProgressionRepository,
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ModuleProgressionViewData>
        get() = _data
    private val _data = MutableLiveData<ModuleProgressionViewData>()

    val events: LiveData<Event<ModuleProgressionAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ModuleProgressionAction>>()

    val canvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)!!
    private var moduleItemId = savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID).orDefault(-1)
    private val assetType = savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_TYPE).orEmpty()
    private val assetId = savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_ID).orEmpty()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _state.postValue(ViewState.Loading)

            if (moduleItemId == -1L) {
                val asset = ModuleItemAsset.valueOf(assetType)
                try {
                    moduleItemId = if (asset == ModuleItemAsset.MODULE_ITEM) {
                        assetId.toLong()
                    } else {
                        repository.getModuleItemSequence(canvasContext, asset.assetType, assetId).items!!.first().current!!.id
                    }
                } catch (e: Exception) {
                    _events.postValue(Event(ModuleProgressionAction.RedirectToAsset(asset)))
                    return@tryLaunch
                }
            }

            val isDiscussionRedesignEnabled = discussionRouteHelperRepository.getEnabledFeaturesForCourse(canvasContext, true)

            val modules = repository.getModulesWithItems(canvasContext)

            val items = modules
                .flatMap { it.items }
                .map { createModuleItemViewData(it, isDiscussionRedesignEnabled) to it }
                .filter { it.first != null }

            val initialPosition = items.indexOfFirst { it.second.id == moduleItemId }

            val moduleNames = items.map { item ->
                modules.find { item.second.moduleId == it.id }?.name.orEmpty()
            }

            _data.postValue(
                ModuleProgressionViewData(
                    items.map { it.first!! },
                    moduleNames,
                    initialPosition,
                    canvasContext.textAndIconColor
                )
            )
            _state.postValue(ViewState.Success)
        } catch {
            _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
        }
    }

    private fun createModuleItemViewData(item: ModuleItem, isDiscussionRedesignEnabled: Boolean) = when (item.type) {
        "Page" -> ModuleItemViewData.Page(item.pageUrl.orEmpty())
        "Assignment" -> ModuleItemViewData.Assignment(item.contentId)
        "Discussion" -> ModuleItemViewData.Discussion(isDiscussionRedesignEnabled, item.contentId)
        "Quiz" -> ModuleItemViewData.Quiz(item.contentId)
        "ExternalUrl", "ExternalTool" -> {
            val url = Uri.parse(item.htmlUrl).buildUpon().appendQueryParameter("display", "borderless").build().toString()
            ModuleItemViewData.External(url, item.title.orEmpty())
        }
        "File" -> ModuleItemViewData.File(item.url.orEmpty())
        else -> null
    }
}
