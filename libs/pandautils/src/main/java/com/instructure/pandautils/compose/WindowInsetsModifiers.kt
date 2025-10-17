/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Apply system bars (status bar + navigation bar) insets as padding.
 * Use this for edge-to-edge screens where content should not draw behind system bars.
 */
fun Modifier.systemBarsPadding() = composed {
    this.padding(WindowInsets.systemBars.asPaddingValues())
}

/**
 * Apply status bar insets as padding.
 * Use this for components that should be pushed below the status bar.
 */
fun Modifier.statusBarsPadding() = composed {
    this.padding(WindowInsets.statusBars.asPaddingValues())
}