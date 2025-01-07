/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.discussion.create

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CreateDiscussionWebViewViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val oauthManager: OAuthManager,
    private val resources: Resources,
): ViewModel() {
    val data: StateFlow<CreateDiscussionWebViewViewData>
        get() = _data
    private val _data = MutableStateFlow(CreateDiscussionWebViewViewData(""))

    val state: StateFlow<ViewState>
        get() = _state
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)

    fun loadData(canvasContext: CanvasContext, isAnnouncement: Boolean, editDiscussionTopicId: Long?) {
        viewModelScope.launch {
            try {
                _state.tryEmit(ViewState.Loading)
                val locale = Locale.getDefault().language
                val timezone = TimeZone.getDefault().id
                val isAnnouncementString = if (isAnnouncement) "?is_announcement=true" else ""
                val url = if (editDiscussionTopicId != null) {
                    "\"${apiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/discussion_topics/${editDiscussionTopicId}/edit"
                } else {
                    "${apiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/discussion_topics/new${isAnnouncementString}"
                }
                val sessionUrl = oauthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
                val authenticatedUrl = "$sessionUrl&embed=true&session_locale=$locale&session_timezone=$timezone"

                _data.tryEmit(CreateDiscussionWebViewViewData(authenticatedUrl))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.tryEmit(ViewState.Error(resources.getString(R.string.errorOccurred)))
            }
        }
    }

    fun setLoading(loading: Boolean) {
        if (loading) _state.tryEmit(ViewState.Loading) else _state.tryEmit(ViewState.Success)
    }
}