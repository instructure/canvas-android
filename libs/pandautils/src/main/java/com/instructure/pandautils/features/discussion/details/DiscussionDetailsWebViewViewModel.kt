/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.discussion.details

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class DiscussionDetailsWebViewViewModel @Inject constructor(
        private val oauthManager: OAuthManager,
        private val apiPrefs: ApiPrefs,
        private val discussionManager: DiscussionManager,
        private val resources: Resources,
        private val locale: Locale = Locale.getDefault(),
        private val timezone: TimeZone = TimeZone.getDefault()
) : ViewModel() {

    val data: LiveData<DiscussionDetailsWebViewViewData>
        get() = _data
    private val _data = MutableLiveData<DiscussionDetailsWebViewViewData>()

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    fun loadData(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader?, id: Long) {
        viewModelScope.launch {
            try {
                val header = discussionTopicHeader ?: discussionManager.getDiscussionTopicHeaderAsync(canvasContext, id, false).await().dataOrNull
                _state.postValue(ViewState.Loading)
                val locale = locale.language
                val timezone = timezone.id
                val url = "${apiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/discussion_topics/$id"
                val sessionUrl = oauthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl
                val authenticatedUrl = "$sessionUrl&embed=true&session_locale=$locale&session_timezone=$timezone"

                _data.postValue(DiscussionDetailsWebViewViewData(authenticatedUrl, header?.title ?: resources.getString(R.string.discussion)))
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