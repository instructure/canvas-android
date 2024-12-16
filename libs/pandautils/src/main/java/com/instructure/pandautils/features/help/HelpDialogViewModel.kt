/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.help

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.PackageInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HelpDialogViewModel @Inject constructor(
    private val helpLinksManager: HelpLinksManager,
    private val courseManager: CourseManager,
    @ApplicationContext private val context: Context,
    private val helpLinkFilter: HelpLinkFilter) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HelpDialogViewData>
        get() = _data
    private val _data = MutableLiveData<HelpDialogViewData>()

    val events: LiveData<Event<HelpDialogAction>>
        get() = _events
    private val _events = MutableLiveData<Event<HelpDialogAction>>()

    init {
        loadHelpLinks()
    }

    private fun loadHelpLinks() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)

            try {
                val helpLinks = helpLinksManager.getHelpLinksAsync(true).await()
                    .dataOrThrow

                val helpLinksViewData = if (helpLinks.customHelpLinks.isNotEmpty()) {
                    // We have custom links, let's use those
                    createLinks(helpLinks.customHelpLinks)
                } else {
                    // Default links
                    createLinks(helpLinks.defaultHelpLinks)
                }

                _data.postValue(HelpDialogViewData(helpLinksViewData))
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
                Logger.d("Failed to grab help links: ${e.printStackTrace()}")
            }
        }
    }

    // Maps links to views and then adds them to the container
    private suspend fun createLinks(list: List<HelpLink>): List<HelpLinkItemViewModel> {

        // Share love link is specific to Android - Add it to the list returned from the API
        val rateLink = HelpLinkViewData(context.getString(R.string.shareYourLove), context.getString(R.string.shareYourLoveDetails), HelpDialogAction.RateTheApp)

        val favoriteCourses = courseManager.getAllFavoriteCoursesAsync(false).await()
            .dataOrThrow

        return list
            // Only want links for students
            .filter { helpLinkFilter.isLinkAllowed(it, favoriteCourses) }
            .filter { it.text != null && it.url != null }
            .map { HelpLinkItemViewModel(HelpLinkViewData(it.text.orEmpty(), it.subtext.orEmpty(), mapAction(it)), ::onLinkClicked) }
            .plus(HelpLinkItemViewModel(rateLink, ::onLinkClicked))
    }

    private fun mapAction(link: HelpLink): HelpDialogAction {
        return when {
            // Internal routes
            link.url == "#create_ticket" -> HelpDialogAction.ReportProblem
            link.url == "#teacher_feedback" -> HelpDialogAction.AskInstructor
            link.url.orEmpty().startsWith("tel:") -> HelpDialogAction.Phone(link.url.orEmpty())
            link.url.orEmpty().startsWith("mailto:") -> HelpDialogAction.SendMail(link.url.orEmpty())
            link.url.orEmpty().contains("cases.canvaslms.com/liveagentchat") -> HelpDialogAction.OpenExternalBrowser(link.url.orEmpty())
            // External URL
            else -> HelpDialogAction.OpenWebView(link.url.orEmpty(), link.text.orEmpty())
        }
    }

    private fun onLinkClicked(action: HelpDialogAction) {
        _events.value = Event(action)
    }
}