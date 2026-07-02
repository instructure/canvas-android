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
 */

package com.instructure.ngc.navigation

import com.instructure.pandautils.navigation.WebViewRouter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Qualifier

/**
 * Qualifier for the Activity-scoped [WebViewRouter] used by NGC. It keeps this binding distinct from
 * the unqualified FragmentComponent binding so the two don't collide (FragmentComponent inherits
 * ActivityComponent bindings).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NgcWebViewRouter

/**
 * Hilt entry point that exposes the app-specific [WebViewRouter] to NGC Compose screens, which are
 * hosted directly by an Activity (not a Fragment) and therefore cannot inject the FragmentComponent
 * binding. The hosting app provides the [WebViewRouter] in [ActivityComponent].
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
interface NGCWebViewRouterEntryPoint {
    @NgcWebViewRouter
    fun webViewRouter(): WebViewRouter
}
