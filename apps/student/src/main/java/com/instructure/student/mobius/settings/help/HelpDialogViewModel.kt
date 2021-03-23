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
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.PackageInfoProvider
import com.instructure.student.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HelpDialogViewModel @Inject constructor(
    private val helpLinksManager: HelpLinksManager,
    private val courseManager: CourseManager,
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val packageInfoProvider: PackageInfoProvider) : ViewModel() {

    val state = MutableLiveData<ViewState>()
    val data = MutableLiveData<HelpDialogViewData>()
    val events = MutableLiveData<Event<HelpDialogAction>>()

    init {
        loadHelpLinks()
    }

    private fun loadHelpLinks() {
        viewModelScope.launch {
            state.postValue(ViewState.Loading)

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

                data.postValue(HelpDialogViewData(helpLinksViewData))
                state.postValue(ViewState.Success)
            } catch (e: Exception) {
                state.postValue(ViewState.Error(e.message ?: ""))
                Logger.d("Failed to grab help links: ${e.printStackTrace()}")
            }
        }
    }

    // Maps links to views and then adds them to the container
    private suspend fun createLinks(list: List<HelpLink>): List<HelpLinkSubViewModel> {

        // Share love link is specific to Android - Add it to the list returned from the API
        val rateLink = HelpLinkViewData(context.getString(R.string.shareYourLove), context.getString(R.string.shareYourLoveDetails), "", HelpDialogAction.RateTheApp)

        val favoriteCourses = courseManager.getAllFavoriteCoursesAsync(false).await()
            .dataOrThrow

        return list
            // Only want links for students
            .filter { link ->
                (link.availableTo.contains("student") || link.availableTo.contains("user"))
                    && (link.url != "#teacher_feedback" || favoriteCourses.filter { !it.isTeacher }.count() > 0)
            }
            .map { HelpLinkSubViewModel(HelpLinkViewData(it.text, it.subtext, it.url, mapAction(it)), this) }
            .plus(HelpLinkSubViewModel(rateLink, this))
    }

    private fun mapAction(link: HelpLink): HelpDialogAction {
        return when {
            // Internal routes
            link.url == "#create_ticket" -> HelpDialogAction.ReportProblem
            link.url == "#teacher_feedback" -> HelpDialogAction.AskInstructor
            // External URL, but we handle within the app
            link.id.contains("submit_feature_idea") -> createSubmitFeatureIdea()
            link.url.startsWith("tel:") -> HelpDialogAction.Phone(link.url)
            link.url.startsWith("mailto:") -> HelpDialogAction.SendMail(link.url)
            link.url.contains("cases.canvaslms.com/liveagentchat") -> HelpDialogAction.OpenExternalBrowser(link.url)
            // External URL
            else -> HelpDialogAction.OpenWebView(link.url, link.text)
        }
    }

    private fun createSubmitFeatureIdea(): HelpDialogAction.SubmitFeatureIdea {
        val recipient = context.getString(R.string.utils_mobileSupportEmailAddress)

        // Try to get the version number and version code
        val packageInfo = packageInfoProvider.getPackageInfo()
        val versionName = packageInfo?.versionName
        val versionCode = packageInfo?.versionCode

        val subject = "[${context.getString(R.string.featureSubject)}] Issue with Canvas [Android] $versionName"

        val installDateString = if (packageInfo != null) {
            DateHelper.dayMonthYearFormat.format(Date(packageInfo.firstInstallTime))
        } else {
            ""
        }

        val user = apiPrefs.user
        // Populate the email body with information about the user
        var emailBody = ""
        emailBody += context.getString(R.string.understandRequest) + "\n"
        emailBody += context.getString(R.string.help_userId) + " " + user?.id + "\n"
        emailBody += context.getString(R.string.help_email) + " " + user?.email + "\n"
        emailBody += context.getString(R.string.help_domain) + " " + apiPrefs.domain + "\n"
        emailBody += context.getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n"
        emailBody += context.getString(R.string.help_locale) + " " + Locale.getDefault() + "\n"
        emailBody += context.getString(R.string.installDate) + " " + installDateString + "\n"
        emailBody += "----------------------------------------------\n"

        return HelpDialogAction.SubmitFeatureIdea(recipient, subject, emailBody)
    }

    fun onLinkClicked(action: HelpDialogAction) {
        events.value = Event(action)
    }
}