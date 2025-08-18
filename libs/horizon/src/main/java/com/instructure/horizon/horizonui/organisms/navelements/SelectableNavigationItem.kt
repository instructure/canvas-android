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
package com.instructure.horizon.horizonui.organisms.navelements

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.instructure.horizon.features.home.BottomNavItem
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun RowScope.SelectableNavigationItem(item: BottomNavItem, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(item.label)
    NavigationBarItem(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        icon = {
            Icon(painter = painterResource(if (selected) item.selectedIcon else item.icon), contentDescription = label)
        },
        label = {
            val color = if (selected) HorizonColors.Text.surfaceInverseSecondary() else HorizonColors.Text.body()
            val textStyle = if (selected) HorizonTypography.labelSmallBold else HorizonTypography.labelSmall
            Text(text = label, color = color, style = textStyle)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = HorizonColors.Icon.surfaceInverseSecondary(),
            unselectedIconColor = HorizonColors.Icon.medium(),
            selectedTextColor = HorizonColors.Text.surfaceInverseSecondary(),
            unselectedTextColor = HorizonColors.Text.body(),
            indicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}