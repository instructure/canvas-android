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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val data: LiveData<CreateDiscussionWebViewViewData>
        get() = _data
    private val _data = MutableLiveData<CreateDiscussionWebViewViewData>()

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    fun loadData(canvasContext: CanvasContext, isAnnouncement: Boolean) {
        viewModelScope.launch {
            try {
                _state.postValue(ViewState.Loading)
                val locale = Locale.getDefault().language
                val timezone = TimeZone.getDefault().id
                val isAnnouncementString = if (isAnnouncement) "?is_announcement=true" else ""
                val url = "${apiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/discussion_topics/new${isAnnouncementString}"
                val sessionUrl = oauthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
                val authenticatedUrl = "$sessionUrl&embed=true&session_locale=$locale&session_timezone=$timezone"

                _data.postValue(CreateDiscussionWebViewViewData(authenticatedUrl))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
            }
        }
    }

    fun setLoading(loading: Boolean) {
        if (loading) _state.postValue(ViewState.Loading) else _state.postValue(ViewState.Success)
    }
}