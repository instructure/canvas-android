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

package com.instructure.parentapp.features.splash

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SplashRepository,
    private val apiPrefs: ApiPrefs,
    private val colorKeeper: ColorKeeper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _events = Channel<SplashAction>()
    val events = _events.receiveAsFlow()

    private val qrMasqueradeId = savedStateHandle.get<Long>(Const.QR_CODE_MASQUERADE_ID) ?: 0L

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.tryLaunch {
            val user = repository.getSelf()
            user?.let { saveUserInfo(it) }

            val colors = repository.getColors()
            colors?.let { colorKeeper.addToCache(it) }

            val theme = repository.getTheme()
            theme?.let { _events.send(SplashAction.ApplyTheme(it)) }

            if (apiPrefs.canBecomeUser == null && qrMasqueradeId == 0L) {
                if (apiPrefs.domain.startsWith("siteadmin", true)) {
                    apiPrefs.canBecomeUser = true
                } else {
                    apiPrefs.canBecomeUser = repository.getBecomeUserPermission()
                }
            }

            val students = repository.getStudents()
            if (students.isEmpty() && apiPrefs.canBecomeUser == false) {
                _events.send(SplashAction.NavigateToNotAParentScreen)
            } else {
                _events.send(SplashAction.InitialDataLoadingFinished)
            }
        } catch {
            viewModelScope.launch {
                _events.send(SplashAction.InitialDataLoadingFinished)
            }
        }
    }

    private suspend fun saveUserInfo(user: User) {
        val oldLocale = apiPrefs.effectiveLocale
        apiPrefs.user = user
        if (apiPrefs.effectiveLocale != oldLocale) {
            _events.send(SplashAction.LocaleChanged)
        }
    }
}
