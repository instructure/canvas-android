/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.webview

import android.webkit.CookieManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimpleWebViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apiPrefs: ApiPrefs,
    private val repository: SimpleWebViewRepository,
    private val cookieManager: CookieManager,
    private val fileDownloader: FileDownloader
) : ViewModel() {

    private val url: String = savedStateHandle.get<String>(Const.URL).orEmpty()
    private val initialCookies: String? = savedStateHandle.get<String>(Navigation.INITIAL_COOKIES)

    private val _events = Channel<SimpleWebViewAction>()
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    init {
        loadUrl()
    }

    fun downloadFile(mime: String, url: String, filename: String) {
        viewModelScope.launch {
            fileDownloader.downloadFileToDevice(url, filename, mime)
        }
    }

    private fun loadUrl() {
        viewModelScope.tryLaunch {
            val url = getAuthUrl()
            val cookies = getCookies()
            val limitWebAccess = apiPrefs.user?.limitParentAppWebAccess().orDefault()

            if (limitWebAccess || !cookies.isNullOrEmpty()) {
                cookies?.let { setCookies(url, it) }
                _events.send(SimpleWebViewAction.LoadWebView(url, limitWebAccess))
            } else {
                _events.send(SimpleWebViewAction.LaunchCustomTab(url))
            }
            _state.value = ViewState.Success
        } catch {
            showError()
        }
    }

    private suspend fun getAuthUrl(): String {
        return if (url.contains(apiPrefs.domain)) {
            repository.getAuthenticatedSession(url)
        } else {
            url
        }
    }

    private fun getCookies(): Map<String, String>? = initialCookies?.fromJson<Map<String, String>>()

    private fun setCookies(url: String, cookies: Map<String, String>) {
        cookieManager.setAcceptCookie(true)
        cookies.forEach { (key, value) -> cookieManager.setCookie(url, "$key=$value") }
        cookieManager.flush()
    }

    private fun showError() {
        viewModelScope.launch {
            _events.send(SimpleWebViewAction.ShowError)
        }
    }
}
