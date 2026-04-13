/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandautils.domain.usecase.splash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.data.repository.theme.ThemeRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LoadSplashDataUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val themeRepository: ThemeRepository,
    private val featureFlagProvider: FeatureFlagProvider,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : BaseUseCase<LoadSplashDataUseCase.Params, SplashData>() {

    data class Params(
        val forceRefresh: Boolean = false
    )

    override suspend fun execute(params: Params): SplashData = coroutineScope {
        val userDeferred = async { userRepository.getSelf(forceRefresh = params.forceRefresh).dataOrNull }
        val colorsDeferred = async { userRepository.getColors(forceRefresh = params.forceRefresh).dataOrNull }
        val themeDeferred = async { themeRepository.getTheme(forceRefresh = params.forceRefresh).dataOrNull }
        val canBecomeUserDeferred = async { userRepository.getBecomeUserPermission(forceRefresh = params.forceRefresh).dataOrNull?.becomeUser }
        val featureFlagsDeferred = async {
            try {
                featureFlagProvider.fetchEnvironmentFeatureFlags()
            } catch (e: Exception) {
                // Log error but don't block app startup
                firebaseCrashlytics.recordException(e)
            }
        }

        featureFlagsDeferred.await()

        SplashData(
            user = userDeferred.await(),
            colors = colorsDeferred.await(),
            theme = themeDeferred.await(),
            canBecomeUser = canBecomeUserDeferred.await()
        )
    }
}

data class SplashData(
    val user: User?,
    val colors: CanvasColor?,
    val theme: CanvasTheme?,
    val canBecomeUser: Boolean?
)