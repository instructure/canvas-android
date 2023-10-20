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
package com.instructure.loginapi.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Currently we are using this class to handle login flow specific checks.
 * The only reason this is shared (only the code, but both have it's own instance) between different Activities of the login process is that we don't have proper MVVM in the login screens.
 * If we would have all the logic in MVVM we could have 3 separate ViewModels for both 3 Activities.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val featureFlagProvider: FeatureFlagProvider,
    private val userManager: UserManager,
    private val oauthManager: OAuthManager,
    private val apiPrefs: ApiPrefs,
    private val networkStateProvider: NetworkStateProvider) : ViewModel() {

    private val loginResultAction = MutableLiveData<Event<LoginResultAction>>()

    fun checkLogin(checkToken: Boolean, checkElementary: Boolean): LiveData<Event<LoginResultAction>> {
        viewModelScope.launch {
            try {
                val offlineEnabled = featureFlagProvider.offlineEnabled()
                val offlineLogin = offlineEnabled && !networkStateProvider.isOnline()
                if (checkToken && !offlineLogin) {
                    val selfResult = userManager.getSelfAsync(true).await()
                    if (selfResult.isSuccess) {
                        val canvasForElementary = checkCanvasElementary(checkElementary)
                        checkTermsAcceptance(canvasForElementary)
                    } else {
                        loginResultAction.value = Event(LoginResultAction.TokenNotValid)
                    }
                } else {
                    val canvasForElementary = checkCanvasElementary(checkElementary)
                    checkTermsAcceptance(canvasForElementary, offlineLogin)
                }
            } catch (e: Exception) {
                loginResultAction.value = Event(LoginResultAction.TokenNotValid)
            }
        }
        return loginResultAction
    }

    private suspend fun checkTermsAcceptance(canvasForElementary: Boolean, offlineLogin: Boolean = false) {
        val authenticatedSession = oauthManager.getAuthenticatedSessionAsync("${apiPrefs.fullDomain}/users/self").await()
        val requiresTermsAcceptance = authenticatedSession.dataOrNull?.requiresTermsAcceptance ?: false
        if (requiresTermsAcceptance) {
            loginResultAction.value = Event(LoginResultAction.ShouldAcceptPolicy(canvasForElementary))
        } else {
            apiPrefs.checkTokenAfterOfflineLogin = offlineLogin
            loginResultAction.value = Event(LoginResultAction.Login(canvasForElementary))
        }
    }

    private suspend fun checkCanvasElementary(shouldCheckElementary: Boolean): Boolean {
        if (!shouldCheckElementary) return false

        return featureFlagProvider.getCanvasForElementaryFlag()
    }
}

sealed class LoginResultAction() {
    object TokenNotValid : LoginResultAction()
    data class ShouldAcceptPolicy(val elementary: Boolean) : LoginResultAction()
    data class Login(val elementary: Boolean) : LoginResultAction()
}