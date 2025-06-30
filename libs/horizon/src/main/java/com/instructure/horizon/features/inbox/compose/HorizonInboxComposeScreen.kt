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
package com.instructure.horizon.features.inbox.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor

@Composable
fun HorizonInboxComposeScreen(
    state: HorizonInboxComposeUiState,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            HorizonInboxComposeTopBar(navController)
        }
    ) { innerPadding ->
        HorizonInboxComposeContent(
            state,
            navController,
            Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonInboxComposeTopBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                "Create Message",
                style = HorizonTypography.h2,
                color = HorizonColors.Text.title()
            )
        },
        actions = {
            IconButton(
                iconRes = R.drawable.close,
                contentDescription = null,
                color = IconButtonColor.Inverse,
                elevation = HorizonElevation.level4,
                onClick = {
                    navController.popBackStack()
                },
            )
        },
        modifier = modifier.padding(24.dp)
    )
}

@Composable
private fun HorizonInboxComposeContent(
    state: HorizonInboxComposeUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {

    }
}