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

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Hilt entry point that exposes the app-specific [NgcAttachmentRouter] to NGC Compose screens, which
 * are hosted directly by an Activity (not a Fragment) and therefore cannot inject bindings through a
 * FragmentComponent. The hosting app provides the [NgcAttachmentRouter] in [ActivityComponent].
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
interface NGCAttachmentRouterEntryPoint {
    fun attachmentRouter(): NgcAttachmentRouter
}
