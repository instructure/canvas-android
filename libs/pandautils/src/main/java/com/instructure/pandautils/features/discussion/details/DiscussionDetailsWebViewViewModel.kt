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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscussionDetailsWebViewViewModel @Inject constructor(
        private val oauthManager: OAuthManager,
        private val apiPrefs: ApiPrefs
) : ViewModel() {

    val data: LiveData<DiscussionDetailsWebViewViewData>
        get() = _data
    private val _data = MutableLiveData<DiscussionDetailsWebViewViewData>()

    fun loadData(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader) {
        viewModelScope.launch {
            try {
                val url = "${apiPrefs.fullDomain}/${canvasContext.apiContext()}/${canvasContext.id}/discussion_topics/${discussionTopicHeader.id}"
                val authenticatedUrl = "${oauthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow.sessionUrl}&embed=true"

                _data.postValue(DiscussionDetailsWebViewViewData(authenticatedUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}