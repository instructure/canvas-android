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

package com.instructure.pandautils.features.grades.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.clipboardList
import com.instructure.instui.token.icon.fileText
import com.instructure.instui.token.icon.helpCircle
import com.instructure.instui.token.icon.megaphone
import com.instructure.instui.token.icon.messageCircle
import com.instructure.instui.token.icon.plug
import com.instructure.pandautils.R

fun mapAssignmentIcon(iconRes: Int): ImageVector = when (iconRes) {
    R.drawable.ic_assignment -> InstUIIcons.clipboardList
    R.drawable.ic_quiz -> InstUIIcons.helpCircle
    R.drawable.ic_discussion -> InstUIIcons.messageCircle
    R.drawable.ic_announcement -> InstUIIcons.megaphone
    R.drawable.ic_lti -> InstUIIcons.plug
    R.drawable.ic_document -> InstUIIcons.fileText
    else -> InstUIIcons.clipboardList
}
