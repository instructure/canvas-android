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
package com.instructure.student.widget.grades.courseselector

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.ScreenState
import com.instructure.student.R
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetUpdater.updateWidgets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseSelectorActivity : BaseCanvasActivity() {

    private val viewModel: CourseSelectorViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let { extras ->
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    CanvasThemedAppBar(
                        title = stringResource(R.string.selectCourse),
                        backgroundColor = colorResource(R.color.backgroundDark),
                        contentColor = colorResource(id = R.color.textLightest),
                        navigationActionClick = { finish() }
                    )
                },
                content = { paddingValues ->
                    when (uiState.state) {
                        ScreenState.Content -> CourseSelectorContentScreen(uiState, paddingValues, ::courseSelected)
                        ScreenState.Empty -> EmptyContent(
                            emptyMessage = stringResource(R.string.edit_dashboard_empty_title),
                            imageRes = R.drawable.ic_panda_nocourses,
                            modifier = Modifier.fillMaxSize(),
                        )
                        ScreenState.Error -> ErrorContent(
                            errorMessage = stringResource(R.string.widgetGradesErrorLoadingCourses),
                            modifier = Modifier.fillMaxSize()
                        )
                        ScreenState.Loading -> Loading(modifier = Modifier.fillMaxSize())
                    }
                }
            )
        }
    }

    private fun courseSelected(course: Course) {
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        StudentPrefs.putLong(WIDGET_COURSE_ID_PREFIX + appWidgetId, course.id)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
        updateWidgets()
    }

    companion object {
        const val WIDGET_COURSE_ID_PREFIX = "widgetCourseId__"
    }
}
