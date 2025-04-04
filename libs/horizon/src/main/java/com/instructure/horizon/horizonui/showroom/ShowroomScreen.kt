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
@file:OptIn(ExperimentalFoundationApi::class)

package com.instructure.horizon.horizonui.showroom

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.showroom.screens.AvatarScreen
import com.instructure.horizon.horizonui.showroom.screens.BorderScreen
import com.instructure.horizon.horizonui.showroom.screens.ButtonsScreen
import com.instructure.horizon.horizonui.showroom.screens.CardsScreen
import com.instructure.horizon.horizonui.showroom.screens.ColorScreen
import com.instructure.horizon.horizonui.showroom.screens.ControlsScreen
import com.instructure.horizon.horizonui.showroom.screens.CornerRadiusScreen
import com.instructure.horizon.horizonui.showroom.screens.ElevationScreen
import com.instructure.horizon.horizonui.showroom.screens.FileDropScreen
import com.instructure.horizon.horizonui.showroom.screens.IconButtonScreen
import com.instructure.horizon.horizonui.showroom.screens.IconographyScreen
import com.instructure.horizon.horizonui.showroom.screens.InputsScreen
import com.instructure.horizon.horizonui.showroom.screens.ModalScreen
import com.instructure.horizon.horizonui.showroom.screens.PillScreen
import com.instructure.horizon.horizonui.showroom.screens.ProgressBarScreen
import com.instructure.horizon.horizonui.showroom.screens.SegmentedControlScreen
import com.instructure.horizon.horizonui.showroom.screens.SpinnerScreen
import com.instructure.horizon.horizonui.showroom.screens.TagScreen
import com.instructure.horizon.horizonui.showroom.screens.TypographyScreen

sealed class ShowroomItem(val contentType: String) {
    data class Header(val title: String) : ShowroomItem(contentType = "header")
    data class Item(val title: String, val route: String) : ShowroomItem(contentType = "item")
}

val showroomItems = listOf(
    ShowroomItem.Header("Foundations: Atoms"),
    ShowroomItem.Item("Colors", "colors"),
    ShowroomItem.Item("Typography", "typography"),
    ShowroomItem.Item("Corner Radius", "cornerradius"),
    ShowroomItem.Item("Border", "border"),
    ShowroomItem.Item("Elevation", "elevation"),
    ShowroomItem.Item("Iconography", "iconography"),
    ShowroomItem.Header("Components: Molecules"),
    ShowroomItem.Item("Avatar", "avatar"),
    ShowroomItem.Item("Buttons", "buttons"),
    ShowroomItem.Item("Icon Buttons", "iconbuttons"),
    ShowroomItem.Item("File Drop", "filedrop"),
    ShowroomItem.Item("Pill", "pill"),
    ShowroomItem.Item("Progress Bar", "progressbar"),
    ShowroomItem.Item("Segmented Control", "segmentedcontrol"),
    ShowroomItem.Item("Spinner", "spinner"),
    ShowroomItem.Item("Tag", "tag"),
    ShowroomItem.Header("Components: Organisms"),
    ShowroomItem.Item("Cards", "cards"),
    ShowroomItem.Item("Modal", "modal"),
    ShowroomItem.Item("Controls", "controls"),
    ShowroomItem.Item("Inputs", "inputs"),
)

@Composable
fun ShowroomScreen() {
    var subScreen by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
        Box {
            if (subScreen != null) {
                IconButton(iconRes = R.drawable.arrow_back, color = IconButtonColor.GHOST, onClick = {
                    subScreen = null
                })
            }
            Column {
                Text(
                    text = "Design system",
                    style = HorizonTypography.h1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                subScreen?.let { subScreen ->
                    val showroomItem = showroomItems.find { (it as? ShowroomItem.Item)?.route == subScreen }
                    Text(
                        text = (showroomItem as ShowroomItem.Item).title,
                        style = HorizonTypography.p2,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        textAlign = TextAlign.Center)
                }
            }
        }
        subScreen?.let {
            ShowroomContent(it)
        } ?: LazyColumn {
            items(showroomItems.size, contentType = {
                showroomItems[it].contentType
            }) { index ->
                when (val showroomItem = showroomItems[index]) {
                    is ShowroomItem.Header -> ShowroomHeader(showroomItem.title)
                    is ShowroomItem.Item -> ShowroomItem(showroomItem.title) {
                        subScreen = showroomItem.route
                    }
                }
            }
        }


    }
}

@Composable
private fun ShowroomHeader(title: String) {
    Text(
        text = title,
        style = HorizonTypography.p2,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun ColumnScope.ShowroomItem(title: String, onClick: () -> Unit = {}) {
    Text(
        text = title,
        style = HorizonTypography.h4,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
            .clip(HorizonCornerRadius.level3)
            .clickable { onClick() }
            .padding(16.dp)
    )
    HorizonSpace(SpaceSize.SPACE_8)
}

@Composable
private fun ShowroomContent(route: String) {
    when (route) {
        "colors" -> ColorScreen()
        "typography" -> TypographyScreen()
        "cornerradius" -> CornerRadiusScreen()
        "border" -> BorderScreen()
        "elevation" -> ElevationScreen()
        "iconography" -> IconographyScreen()
        "avatar" -> AvatarScreen()
        "buttons" -> ButtonsScreen()
        "iconbuttons" -> IconButtonScreen()
        "filedrop" -> FileDropScreen()
        "pill" -> PillScreen()
        "progressbar" -> ProgressBarScreen()
        "segmentedcontrol" -> SegmentedControlScreen()
        "spinner" -> SpinnerScreen()
        "tag" -> TagScreen()
        "cards" -> CardsScreen()
        "modal" -> ModalScreen()
        "controls" -> ControlsScreen()
        "inputs" -> InputsScreen()
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
fun ShowroomScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ShowroomScreen()
}