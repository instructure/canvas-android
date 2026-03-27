/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.features.ngc.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.domain.usecase.splash.LoadSplashDataUseCase
import com.instructure.pandautils.domain.usecase.splash.SetupPendoTrackingUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.LocaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loadSplashDataUseCase: LoadSplashDataUseCase,
    private val setupPendoTrackingUseCase: SetupPendoTrackingUseCase,
    private val apiPrefs: ApiPrefs,
    private val colorKeeper: ColorKeeper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.tryLaunch {
            val splashData = loadSplashDataUseCase(LoadSplashDataUseCase.Params())

            splashData.user?.let { saveUserInfo(it) }
            splashData.colors?.let { colorKeeper.addToCache(it) }

            if (splashData.theme != null) {
                _uiState.update { it.copy(themeToApply = splashData.theme) }
            }

            if (apiPrefs.canBecomeUser == null) {
                if (apiPrefs.domain.startsWith("siteadmin", true)) {
                    apiPrefs.canBecomeUser = true
                } else {
                    apiPrefs.canBecomeUser = splashData.canBecomeUser ?: false
                }
            }

            setupPendoTrackingUseCase(Unit)

            // TODO: Fetch terms of service for pairing code capability
            // TODO: Fetch user settings (e.g., hideCourseColorOverlay)
            // TODO: Fetch launch definitions
            // TODO: Fetch unread message count
            // TODO: Fetch unread notification count
            // TODO: Fetch to-do count

            _uiState.update { it.copy(loading = false, initialDataLoaded = true) }
        } catch {
            _uiState.update { it.copy(loading = false, error = true, initialDataLoaded = true) }
        }
    }

    fun onThemeApplied() {
        _uiState.update { it.copy(themeToApply = null) }
    }

    private fun saveUserInfo(user: User) {
        val oldLocale = apiPrefs.effectiveLocale
        apiPrefs.user = user
        if (apiPrefs.effectiveLocale != oldLocale) {
            LocaleUtils.restartApp(context)
        }
    }
}