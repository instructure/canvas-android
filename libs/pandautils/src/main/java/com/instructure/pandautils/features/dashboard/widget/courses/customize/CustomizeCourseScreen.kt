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

package com.instructure.pandautils.features.dashboard.customize.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.ColorPicker
import com.instructure.pandautils.utils.ColorKeeper

@Composable
fun CustomizeCourseScreen(
    onNavigateBack: () -> Unit,
    onShowError: () -> Unit
) {
    val viewModel: CustomizeCourseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()

    LaunchedEffect(events) {
        when (events) {
            CustomizeCourseEvent.NavigateBack -> onNavigateBack()
            CustomizeCourseEvent.ShowError -> onShowError()
            null -> {}
        }
    }

    CustomizeCourseScreenContent(
        uiState = uiState,
        onNicknameChanged = viewModel::onNicknameChanged,
        onColorSelected = viewModel::onColorSelected,
        onDone = viewModel::onDone,
        onBack = onNavigateBack
    )
}

@Composable
fun CustomizeCourseScreenContent(
    uiState: CustomizeCourseUiState,
    onNicknameChanged: (String) -> Unit,
    onColorSelected: (Int) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(0.dp)
        ) {
            item {
                CourseHeader(
                    courseName = uiState.courseName,
                    courseCode = uiState.courseCode,
                    imageUrl = uiState.imageUrl,
                    onBack = onBack,
                    onDone = onDone,
                    isLoading = uiState.isLoading
                )
            }

            item {
                NicknameCard(
                    nickname = uiState.nickname,
                    onNicknameChanged = onNicknameChanged,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                ColorCard(
                    selectedColor = uiState.selectedColor,
                    availableColors = uiState.availableColors,
                    onColorSelected = onColorSelected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseHeader(
    courseName: String,
    courseCode: String,
    imageUrl: String?,
    onBack: () -> Unit,
    onDone: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(308.dp)
    ) {
        GlideImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_lined),
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White
                )
            }

            TextButton(
                onClick = onDone,
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.done),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = courseName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )

            if (courseCode.isNotEmpty()) {
                Text(
                    text = courseCode,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NicknameCard(
    nickname: String,
    onNicknameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.courseNickname),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                color = colorResource(R.color.textDarkest),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.courseNicknameHint),
                        color = colorResource(R.color.textDark)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorResource(R.color.textDarkest),
                    unfocusedTextColor = colorResource(R.color.textDarkest),
                    focusedBorderColor = colorResource(R.color.borderMedium),
                    unfocusedBorderColor = colorResource(R.color.borderMedium),
                    focusedContainerColor = colorResource(R.color.backgroundLightest),
                    unfocusedContainerColor = colorResource(R.color.backgroundLightest)
                )
            )
        }
    }
}

@Composable
private fun ColorCard(
    selectedColor: Int,
    availableColors: List<Int>,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val themedColors = availableColors.map { ColorKeeper.createThemedColor(it) }

            ColorPicker(
                label = stringResource(R.string.courseColor),
                selectedColor = selectedColor,
                colors = themedColors,
                onColorSelected = onColorSelected,
                titleModifier = Modifier.padding(bottom = 8.dp)
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
                )
            ),
            onNicknameChanged = {},
            onColorSelected = {},
            onDone = {},
            onBack = {}
        )
    }
}
