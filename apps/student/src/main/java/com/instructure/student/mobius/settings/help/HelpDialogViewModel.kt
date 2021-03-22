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
package com.instructure.student.mobius.settings.help

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.HelpLinks
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.Event
import com.instructure.student.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class HelpDialogViewModel @Inject constructor(
    private val helpLinksManager: HelpLinksManager,
    private val courseManager: CourseManager,
    @ApplicationContext private val context: Context) : ViewModel() {

    private var helpLinksJob: Job? = null

    val state = MutableLiveData<HelpDialogViewState>()
    val data = MutableLiveData<HelpDialogViewData>()
    val events = MutableLiveData<Event<HelpDialogAction>>()

    init {
        loadHelpLinks()
    }

    private fun loadHelpLinks() {
        helpLinksJob = tryWeave {
//            with(layoutView) { emptyView.setLoading() }
            state.postValue(HelpDialogViewState.Loading)

            // TODO Can this be null if we have an error?
            val helpLinks = awaitApi<HelpLinks> { helpLinksManager.getHelpLinks(it, true) }

            val helpLinksViewData = if (helpLinks.customHelpLinks.isNotEmpty()) {
                // We have custom links, let's use those
                createLinks(helpLinks.customHelpLinks)
            } else {
                // Default links
                createLinks(helpLinks.defaultHelpLinks)
            }
//            emptyView.setGone()
            state.postValue(HelpDialogViewState.Success)
            data.postValue(HelpDialogViewData(helpLinksViewData))

        } catch {
            Logger.d("Failed to grab help links: ${it.printStackTrace()}")
        }
    }

    // Maps links to views and then adds them to the container
    private suspend fun createLinks(list: List<HelpLink>): List<HelpLinkViewData> {

        // Share love link is specific to Android - Add it to the list returned from the API
        val rateLink = HelpLinkViewData(context.getString(R.string.shareYourLove), context.getString(R.string.shareYourLoveDetails), "#share_the_love", HelpDialogAction.RateTheApp)

        return list
            // Only want links for students
            .filter { link ->
                (link.availableTo.contains("student") || link.availableTo.contains("user"))
                    && (link.url != "#teacher_feedback" || awaitApi<List<Course>> { courseManager.getAllFavoriteCourses(false, it) }.filter { !it.isTeacher }.count() > 0) }
            .map { HelpLinkViewData(it.text, it.subtext, it.url, mapAction(it)) }
            .plus(rateLink)
    }

    private fun mapAction(link: HelpLink): HelpDialogAction {
        return when {
            // Internal routes
            link.url[0] == '#' ->
                when (link.url) {
                    "#create_ticket" -> HelpDialogAction.ReportProblem
                    "#teacher_feedback" -> HelpDialogAction.AskInstructor
                    "#share_the_love" -> HelpDialogAction.RateTheApp
                    else -> { HelpDialogAction.EmptyAction  }
                }
            // External URL, but we handle within the app
            link.id.contains("submit_feature_idea") -> HelpDialogAction.SubmitFeatureIdea
            link.url.startsWith("tel:")-> HelpDialogAction.Phone(link.url)
            link.url.startsWith("mailto:") -> HelpDialogAction.SendMail(link.url)
            link.url.contains("cases.canvaslms.com/liveagentchat") -> HelpDialogAction.OpenExternalBrowser(link.url)
            // External URL
            else -> HelpDialogAction.OpenWebView(link.url, link.text)
        }
    }

    fun onLinkClicked(action: HelpDialogAction) {
        events.value = Event(action)
    }

    override fun onCleared() {
        helpLinksJob?.cancel()
        super.onCleared()
    }
}