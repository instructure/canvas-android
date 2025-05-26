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
package com.instructure.student.widget.grades.singleGrade

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.unit.ColorProvider
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.student.activity.LoginActivity
import com.instructure.student.widget.LoggingStartActivityAction
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.glance.WidgetTextStyles
import com.instructure.student.widget.grades.WidgetCourseItem
import com.instructure.student.widget.grades.courseselector.CourseSelectorActivity
import com.jakewharton.threetenabp.AndroidThreeTen

class SingleGradeWidget : GlanceAppWidget() {

    companion object {
        private val NARROW = DpSize(70.dp, 70.dp)
        private val WIDE = DpSize(100.dp, 70.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            NARROW,
            WIDE
        )
    )

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val state =
                prefs[SingleGradeWidgetReceiver.singleGradeWidgetUiStateKey]?.fromJson<SingleGradeWidgetUiState>()
                    ?: SingleGradeWidgetUiState(WidgetState.Loading)
            Content(state, id)
        }
    }

    @Composable
    private fun Content(state: SingleGradeWidgetUiState, glanceId: GlanceId? = null) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.backgroundLightest)
        ) {
            when (state.state) {
                WidgetState.Loading -> Loading()

                WidgetState.Error -> SingleGradeError(
                    imageRes = com.instructure.student.R.drawable.ic_panda_notsupported,
                    titleRes = com.instructure.student.R.string.widgetErrorTitle,
                    subtitleRes = com.instructure.student.R.string.widgetSingleGradeErrorSubtitle
                )

                WidgetState.Empty -> {
                    glanceId?.let {
                        SingleGradeEmpty(
                            imageRes = com.instructure.student.R.drawable.ic_smart_search_empty,
                            titleRes = com.instructure.student.R.string.selectCourse,
                            it
                        )
                    }

                }

                WidgetState.NotLoggedIn -> SingleGradeError(
                    imageRes = com.instructure.student.R.drawable.ic_smart_search_empty,
                    titleRes = com.instructure.student.R.string.widgetNotLoggedInTitle,
                    subtitleRes = com.instructure.student.R.string.widgetSingleGradeNotLoggedInSubtitle
                )

                WidgetState.Content -> CourseContent(state.course)
            }
        }
    }

    @Composable
    private fun CourseContent(courseItem: WidgetCourseItem?) {
        courseItem?.let {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.backgroundLightest)
                    .clickable(
                        actionRunCallback<LoggingStartActivityAction>(
                            LoggingStartActivityAction.createActionParams(
                                InterwebsToApplication.createIntent(
                                    ContextKeeper.appContext,
                                    Uri.parse(it.url)
                                ),
                                AnalyticsEventConstants.WIDGET_SINGLE_GRADE_OPEN_ITEM_ACTION
                            )
                        )
                    )
            ) {
                if (LocalSize.current.width < WIDE.width) {
                    NarrowContent(it)
                } else {
                    WideContent(it)
                }
            }
        }
    }

    private fun getConfigurationIntent(glanceId: GlanceId): Intent {
        val widgetId = GlanceAppWidgetManager(ContextKeeper.appContext).getAppWidgetId(glanceId)
        return Intent(ContextKeeper.appContext, CourseSelectorActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
    }

    @Composable
    private fun NarrowContent(courseItem: WidgetCourseItem) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .padding(top = 12.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    modifier = GlanceModifier.size(16.dp),
                    provider = ImageProvider(R.drawable.ic_canvas_logo),
                    contentDescription = LocalContext.current.getString(R.string.locked)
                )

                Text(
                    modifier = GlanceModifier.padding(start = 4.dp),
                    text = "Grades",
                    style = WidgetTextStyles.mediumDarkest.copy(
                        fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            GradeLayout(courseItem)
            Text(
                modifier = GlanceModifier,
                text = courseItem.courseCode,
                style = WidgetTextStyles.mediumDarkest.copy(
                    color = androidx.glance.color.ColorProvider(
                        Color(courseItem.courseColorLight),
                        Color(courseItem.courseColorDark)
                    ),
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

        }
    }

    @Composable
    private fun WideContent(courseItem: WidgetCourseItem) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .padding(top = 12.dp, bottom = 18.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    modifier = GlanceModifier.size(24.dp),
                    provider = ImageProvider(R.drawable.ic_canvas_logo),
                    contentDescription = "",
                )

                Text(
                    modifier = GlanceModifier.padding(start = 4.dp),
                    text = "Grades",
                    style = WidgetTextStyles.mediumDarkest.copy(
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                modifier = GlanceModifier,
                text = courseItem.name,
                style = WidgetTextStyles.mediumDarkest.copy(
                    color = androidx.glance.color.ColorProvider(
                        Color(courseItem.courseColorLight),
                        Color(courseItem.courseColorDark)
                    ),
                    textAlign = TextAlign.Left
                ),
                maxLines = 2
            )
            GradeLayout(courseItem)
        }
    }

    @Composable
    fun SingleGradeError(
        @DrawableRes imageRes: Int,
        @StringRes titleRes: Int,
        @StringRes subtitleRes: Int,
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize().clickable(
                    actionRunCallback<LoggingStartActivityAction>(
                        LoggingStartActivityAction.createActionParams(
                            Intent(ContextKeeper.appContext, LoginActivity::class.java),
                            AnalyticsEventConstants.WIDGET_SINGLE_GRADE_OPEN_APP_ACTION
                        )
                    )
                )
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Image(
                provider = ImageProvider(imageRes),
                contentDescription = null,
                modifier = GlanceModifier.wrapContentWidth().defaultWeight()
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = LocalContext.current.getString(titleRes),
                style = WidgetTextStyles.mediumDarkest.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Left
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = LocalContext.current.getString(subtitleRes),
                style = WidgetTextStyles.normalDark.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Left
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    fun SingleGradeEmpty(
        @DrawableRes imageRes: Int,
        @StringRes titleRes: Int,
        glanceId: GlanceId
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(
                    actionRunCallback<LoggingStartActivityAction>(
                        LoggingStartActivityAction.createActionParams(
                            getConfigurationIntent(glanceId),
                            AnalyticsEventConstants.WIDGET_GRADES_OPEN_APP_ACTION
                        )
                    )
                )
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Image(
                provider = ImageProvider(imageRes),
                contentDescription = null,
                modifier = GlanceModifier.wrapContentWidth().defaultWeight()
            )
            Spacer(modifier = GlanceModifier.height(16.dp))
            Text(
                text = LocalContext.current.getString(titleRes),
                style = WidgetTextStyles.mediumDarkest.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Left
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun GradeLayout(
        widgetCourseItem: WidgetCourseItem,
        modifier: GlanceModifier = GlanceModifier
    ) {
        widgetCourseItem.gradeText?.let {
            if (widgetCourseItem.isLocked) {
                LockIcon(modifier)
            } else if (it == "") {
                NoGradesLabel(modifier)
            } else {
                TextLabel(label = it, modifier = modifier, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun TextLabel(
        label: String,
        textColor: ColorProvider = WidgetColors.textDarkest,
        fontWeight: FontWeight = FontWeight.Medium,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Text(
            modifier = modifier,
            text = label,
            style = WidgetTextStyles.mediumDarkest.copy(
                color = textColor,
                fontWeight = fontWeight,
                textAlign = TextAlign.End
            ),
            maxLines = 1,
        )
    }

    @Composable
    private fun NoGradesLabel(modifier: GlanceModifier = GlanceModifier) {
        TextLabel(
            LocalContext.current.getString(R.string.noGrades),
            WidgetColors.textDark,
            FontWeight.Normal,
            modifier
        )
    }

    @Composable
    private fun LockIcon(modifier: GlanceModifier = GlanceModifier) {
        Image(
            modifier = modifier.size(16.dp),
            provider = ImageProvider(R.drawable.ic_lock),
            contentDescription = LocalContext.current.getString(R.string.locked),
            colorFilter = ColorFilter.tint(WidgetColors.textDark)
        )
    }

    private fun getPreviewSampleData() = SingleGradeWidgetUiState(
        state = WidgetState.Content,
        course =
        WidgetCourseItem(
            name = "Biology 101",
            courseCode = "BIO 101",
            isLocked = false,
            gradeText = "82%",
            courseColorLight = 0xFF2573DF.toInt(),
            courseColorDark = 0xFF2573DF.toInt(),
            ""
        )
    )

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 140, heightDp = 100)
    @Composable
    private fun GradesWidgetPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            getPreviewSampleData()
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 70, heightDp = 100)
    @Composable
    private fun GradesWidgetNarrowPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            getPreviewSampleData()
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 140, heightDp = 100)
    @Composable
    private fun GradesWidgetErrorPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            SingleGradeWidgetUiState(WidgetState.Error)
        )
    }
}