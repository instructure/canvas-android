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
package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object HorizonCornerRadius {
    val level0 = RoundedCornerShape(0.dp)
    val level1 = RoundedCornerShape(8.dp)
    val level1_5 = RoundedCornerShape(12.dp)
    val level2 = RoundedCornerShape(16.dp)
    val level3 = RoundedCornerShape(16.dp)
    val level3Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val level3Bottom = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    val level3_5 = RoundedCornerShape(24.dp)
    val level4 = RoundedCornerShape(32.dp)
    val level4Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val level4Bottom = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    val level5 = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val level6 = RoundedCornerShape(100.dp)
}