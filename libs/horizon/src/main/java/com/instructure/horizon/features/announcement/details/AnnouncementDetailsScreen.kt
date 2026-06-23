/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.announcement.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.organisms.scaffolds.EdgeToEdgeScaffold
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.utils.localisedFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailsScreen(
    state: AnnouncementDetailsUiState,
    navController: NavHostController,
) {
    EdgeToEdgeScaffold(
        statusBarColor = null,
        navigationBarColor = HorizonColors.Surface.cardPrimary(),
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { AnnouncementDetailsHeader(state.title, navController) },
    ) { innerPadding ->
        LoadingStateWrapper(
            state.loadingState,
            modifier = Modifier.padding(innerPadding),
        ) {
            AnnouncementDetailsContent(state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementDetailsHeader(title: String, navController: NavHostController) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.campaign),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier.size(16.dp),
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = title,
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Surface.institution(),
                    maxLines = 2,
                )
            }
        },
        navigationIcon = {
            IconButton(
                iconRes = R.drawable.arrow_back,
                contentDescription = stringResource(R.string.a11yNavigateBack),
                color = IconButtonColor.Ghost,
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(horizontal = 10.dp),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default(),
        ),
    )
}

@Composable
private fun AnnouncementDetailsContent(state: AnnouncementDetailsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level4Top)
            .background(HorizonColors.Surface.pageSecondary()),
    ) {
        LazyColumn(contentPadding = PaddingValues(top = 16.dp)) {
            itemsIndexed(state.items) { _, item ->
                AnnouncementDetailsItemView(item)
                if (item != state.items.lastOrNull()) {
                    HorizonDivider()
                }
            }
        }
    }
}

@Composable
private fun AnnouncementDetailsItemView(item: AnnouncementDetailsItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pageSecondary())
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        ) {
            Text(
                text = item.author,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.body(),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.date.localisedFormat("MMM d yyyy, hh mm a", LocalContext.current),
                style = HorizonTypography.p3,
                color = HorizonColors.Text.timestamp(),
            )
        }

        HorizonSpace(SpaceSize.SPACE_8)

        if (item.isHtmlContent) {
            val activity = LocalContext.current.getActivityOrNull()
            ComposeCanvasWebViewWrapper(
                item.content,
                modifier = Modifier.padding(horizontal = 16.dp),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                    shouldLaunchInternalWebViewFragment = { _ -> true },
                    launchInternalWebViewFragment = { url ->
                        activity?.launchCustomTab(url, ThemePrefs.brandColor)
                    },
                ),
            )
        } else {
            Text(
                text = item.content,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}
