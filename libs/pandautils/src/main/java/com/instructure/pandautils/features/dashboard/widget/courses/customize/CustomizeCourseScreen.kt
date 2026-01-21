/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses.customize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ColorPicker
import com.instructure.pandautils.utils.ColorKeeper

@Composable
fun CustomizeCourseScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: CustomizeCourseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            uiState.onNavigationHandled()
            onNavigateBack()
        }
    }

    CustomizeCourseScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CustomizeCourseScreenContent(
    uiState: CustomizeCourseUiState,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            uiState.onErrorHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.backgroundLight))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) {
            item {
                CourseHeader(
                    imageUrl = uiState.imageUrl,
                    color = uiState.selectedColor,
                    showColorOverlay = uiState.showColorOverlay
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                NicknameCard(
                    nickname = uiState.nickname,
                    onNicknameChanged = uiState.onNicknameChanged,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.backgroundLightest)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    ColorPicker(
                        label = stringResource(R.string.dashboardCourseColor),
                        selectedColor = uiState.selectedColor,
                        colors = uiState.availableColors.map { ColorKeeper.createThemedColor(it) },
                        onColorSelected = uiState.onColorSelected,
                        isCollapsible = false,
                        expandedBackgroundColor = Color.Transparent,
                        titleModifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CanvasThemedAppBar(
            title = uiState.courseName,
            subtitle = uiState.courseCode,
            navIconRes = R.drawable.ic_close_lined,
            navIconContentDescription = stringResource(R.string.close),
            navigationActionClick = onNavigateBack,
            backgroundColor = if (uiState.showColorOverlay) Color.Transparent else Color.Black.copy(alpha = 0.5f),
            contentColor = colorResource(R.color.textLightest),
            actions = {
                TextButton(
                    onClick = uiState.onDone,
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseHeader(
    imageUrl: String?,
    color: Int,
    showColorOverlay: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color))
        )

        if (imageUrl != null) {
            GlideImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (showColorOverlay) 0.4f else 1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun NicknameCard(
    nickname: String,
    onNicknameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .padding(top = 12.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboardCourseNickname),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 21.sp,
                color = colorResource(R.color.textDarkest)
            )

            BasicTextField(
                value = nickname,
                onValueChange = onNicknameChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 21.sp,
                    color = colorResource(R.color.textDarkest),
                    textAlign = TextAlign.End
                ),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (nickname.isEmpty() && !isFocused) {
                            Text(
                                text = stringResource(R.string.dashboardCourseNickname),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 21.sp,
                                color = colorResource(R.color.textDark),
                                textAlign = TextAlign.End
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomizeCourseScreenPreview() {
    CanvasTheme {
        CustomizeCourseScreenContent(
            uiState = CustomizeCourseUiState(
                courseId = 1,
                courseName = "Introduction to Computer Science",
                courseCode = "CS101",
                imageUrl = null,
                nickname = "My CS Course",
                selectedColor = 0xFF2573DF.toInt(),
                availableColors = listOf(
                    0xFF2573DF.toInt(),
                    0xFFE71F63.toInt(),
                    0xFF0B874B.toInt(),
                    0xFFFC5E13.toInt(),
                    0xFF8F3E97.toInt(),
                    0xFF00AC18.toInt()
                ),
                onNicknameChanged = {},
                onColorSelected = {},
                onDone = {},
                onNavigationHandled = {},
                onErrorHandled = {}
            ),
            onNavigateBack = {}
        )
    }
}
