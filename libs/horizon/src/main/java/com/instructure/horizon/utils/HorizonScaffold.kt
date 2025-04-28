/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.organisms.topappbar.HorizonTopAppBar

@Composable
fun HorizonScaffold(
    title: String,
    onBackPressed: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { HorizonTopAppBar(title, onBackPressed) },
        contentColor = HorizonColors.Surface.pagePrimary()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(HorizonColors.Surface.pagePrimary())
                .fillMaxSize()
                .padding(innerPadding)
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.pageSecondary())

        ) {
            content()
        }
    }
}