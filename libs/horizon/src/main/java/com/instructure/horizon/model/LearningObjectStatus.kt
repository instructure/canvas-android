/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.model

import androidx.annotation.StringRes
import com.instructure.horizon.R

enum class LearningObjectStatus(@StringRes val stringRes: Int, val completed: Boolean = false) {
    OPTIONAL(R.string.learningObjectStatus_optional),
    REQUIRED(R.string.learningObjectStatus_required),
    VIEWED(R.string.learningObjectStatus_viewed, completed = true),
    SUBMITTED(R.string.learningObjectStatus_submitted, completed = true),
}