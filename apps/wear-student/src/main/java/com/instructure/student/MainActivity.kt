/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import com.instructure.student.features.grades.GradesScreen
import com.instructure.student.features.todo.TodoScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Box(Modifier.safeDrawingPadding()) {
                WearApp()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WearApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState = authViewModel.authState.collectAsState()
    val pagerState = rememberPagerState()
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageCount: Int
                get() = 2
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = pagerState.currentPage

        }
    }

    MaterialTheme {
        if (uiState.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                HorizontalPager(pageCount = 2, state = pagerState) {
                    when (it) {
                        0 -> GradesScreen()
                        1 -> TodoScreen()
                        else -> throw IllegalStateException("Unexpected page index $it")
                    }
                }
                HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Text(text = "Not Authenticated", modifier = Modifier.align(Alignment.Center))
            }
        }


    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}