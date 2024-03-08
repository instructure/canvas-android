/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.backgroundColor
import com.jakewharton.threetenabp.AndroidThreeTen

@Composable
fun SelectCalendarScreen(
    uiState: SelectCalendarUiState,
    onCalendarSelected: (CanvasContext) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasAppBar(
                title = stringResource(id = R.string.selectCalendarScreenTitle),
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_back_arrow,
                navIconContentDescription = stringResource(id = R.string.back)
            )
        },
        content = { padding ->
            SelectCalendarContent(
                uiState = uiState,
                onCalendarSelected = onCalendarSelected,
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    )
}

@Composable
private fun SelectCalendarContent(
    uiState: SelectCalendarUiState,
    onCalendarSelected: (CanvasContext) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                content = {
                    items(uiState.canvasContexts) { canvasContext ->
                        Column(
                            modifier = modifier.clickable {
                                onCalendarSelected(canvasContext)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = Color(
                                                if (canvasContext is User) {
                                                    ThemePrefs.brandColor
                                                } else {
                                                    canvasContext.backgroundColor
                                                }
                                            )
                                        )
                                )
                                Text(
                                    text = canvasContext.name.orEmpty(),
                                    fontSize = 16.sp,
                                    color = colorResource(id = R.color.textDarkest),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                if (uiState.selectedCanvasContext?.id == canvasContext.id) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_checkmark),
                                        contentDescription = null,
                                        tint = colorResource(id = R.color.textDarkest)
                                    )
                                }
                            }
                            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                        }
                    }
                }
            )
        }
    }
}

data class SelectCalendarUiState(
    val show: Boolean = false,
    val selectedCanvasContext: CanvasContext? = null,
    val canvasContexts: List<CanvasContext> = emptyList()
)

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun SelectCalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    SelectCalendarScreen(
        uiState = SelectCalendarUiState(
            show = true,
            selectedCanvasContext = Course(id = 2),
            canvasContexts = listOf(
                Course(id = 1, name = "Black Holes"),
                Course(id = 2, name = "Cosmology"),
                Course(id = 3, name = "Life in the Universe"),
            )
        ),
        onCalendarSelected = {},
        navigationActionClick = {}
    )
}