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
package com.instructure.horizon.features.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.utils.HorizonScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(state: NotificationUiState, mainNavController: NavController) {
    HorizonScaffold(
        title = stringResource(R.string.notificationsTitle),
        onBackPressed = { mainNavController.popBackStack() },
    ) {
        LoadingStateWrapper(state.screenState) {
            NotificationContent(state)
        }
    }
}

@Composable
private fun NotificationContent(state: NotificationUiState) {
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(HorizonColors.Surface.pageSecondary())
    ) {
        if (state.notificationItems.isEmpty()) {
            item {
                EmptyNotificationItemContent()
            }
        } else {
            itemsIndexed(state.notificationItems) { index, item ->
                NotificationItemContent(
                    categoryLabel = item.categoryLabel,
                    title = item.title,
                    date = item.date
                )

                HorizonDivider()
            }
        }
    }
}

@Composable
private fun EmptyNotificationItemContent() {
    NotificationItemContent(
        categoryLabel = "",
        title = stringResource(R.string.notificationsEmptyMessage),
        date = ""
    )
}

@Composable
private fun NotificationItemContent(
    categoryLabel: String,
    title: String,
    date: String,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        HorizonSpace(SpaceSize.SPACE_16)

        Text(
            text = categoryLabel,
            style = HorizonTypography.labelSmallBold,
            color = HorizonColors.Text.timestamp()
        )

        HorizonSpace(SpaceSize.SPACE_4)

        Text(
            text = title,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_4)

        Text(
            text = date,
            style = HorizonTypography.labelSmall,
            color = HorizonColors.Text.timestamp()
        )
    }


}