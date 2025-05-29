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

package com.instructure.student.widget.grades.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
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
import com.instructure.student.widget.glance.Error
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.glance.WidgetTextStyles
import com.instructure.student.widget.grades.WidgetCourseItem
import com.jakewharton.threetenabp.AndroidThreeTen

class GradesWidget : GlanceAppWidget() {

    companion object {
        private val NARROW = DpSize(100.dp, 110.dp)
        private val WIDE = DpSize(250.dp, 110.dp)
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
                prefs[GradesWidgetReceiver.gradesWidgetUiStateKey]?.fromJson<GradesWidgetUiState>()
                    ?: GradesWidgetUiState(WidgetState.Loading)
            Content(state)
        }
    }

    @Composable
    private fun Content(state: GradesWidgetUiState) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.backgroundLightest)
        ) {
            when (state.state) {
                WidgetState.Loading -> Loading()

                WidgetState.Error, WidgetState.Empty -> ClickHandlerBox {
                    Error(
                        imageRes = R.drawable.ic_panda_notsupported,
                        titleRes = R.string.widgetErrorTitle,
                        subtitleRes = R.string.widgetGradesErrorSubtitle
                    )
                }

                WidgetState.NotLoggedIn -> ClickHandlerBox {
                    Error(
                        imageRes = R.drawable.ic_smart_search_empty,
                        titleRes = R.string.widgetNotLoggedInTitle,
                        subtitleRes = R.string.widgetGradesNotLoggedInSubtitle
                    )
                }

                WidgetState.Content -> CourseList(state.courses)
            }
        }
    }

    @Composable
    private fun CourseList(courses: List<WidgetCourseItem>) {
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            items(courses) { item ->
                Row(
                    modifier = GlanceModifier.fillMaxSize()
                        .padding(8.dp, 8.dp)
                        .clickable(
                            actionRunCallback<LoggingStartActivityAction>(
                                LoggingStartActivityAction.createActionParams(
                                    InterwebsToApplication.createIntent(
                                        ContextKeeper.appContext,
                                        Uri.parse(item.url)
                                    ),
                                    AnalyticsEventConstants.WIDGET_GRADES_OPEN_ITEM_ACTION
                                )
                            )
                        ),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        modifier = GlanceModifier.defaultWeight().padding(end = 8.dp),
                        text = if (LocalSize.current.width < WIDE.width) item.courseCode else item.name,
                        style = WidgetTextStyles.mediumDarkest.copy(
                            color = androidx.glance.color.ColorProvider(
                                Color(item.courseColorLight),
                                Color(item.courseColorDark)
                            )
                        ),
                        maxLines = 2,
                    )
                    GradeLayout(item)
                }
            }
        }
    }

    @Composable
    private fun GradeLayout(
        widgetCourseItem: WidgetCourseItem,
        modifier: GlanceModifier = GlanceModifier
    ) {
        if (widgetCourseItem.isLocked) {
            LockIcon(modifier)
        } else if (widgetCourseItem.gradeText.isNullOrBlank()) {
            NoGradesLabel(modifier)
        } else {
            TextLabel(label = widgetCourseItem.gradeText, modifier = modifier)
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

    @Composable
    private fun ClickHandlerBox(
        modifier: GlanceModifier = GlanceModifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .clickable(
                    actionRunCallback<LoggingStartActivityAction>(
                        LoggingStartActivityAction.createActionParams(
                            Intent(ContextKeeper.appContext, LoginActivity::class.java),
                            AnalyticsEventConstants.WIDGET_GRADES_OPEN_APP_ACTION
                        )
                    )
            ),
            content = content
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun GradesWidgetPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            GradesWidgetUiState(
                state = WidgetState.Content,
                courses = listOf(
                    WidgetCourseItem(
                        name = "Biology 101",
                        courseCode = "BIO 101",
                        isLocked = false,
                        gradeText = "82%",
                        courseColorLight = 0xFF2573DF.toInt(),
                        courseColorDark = 0xFF2573DF.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "Mathematics 904 2024/25",
                        courseCode = "MAT 904",
                        isLocked = false,
                        gradeText = "Good",
                        courseColorLight = 0xFF9E58BD.toInt(),
                        courseColorDark = 0xFF9E58BD.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "Music Test Course something longer than a line or two for sh...",
                        courseCode = "MTC",
                        isLocked = false,
                        gradeText = "A+",
                        courseColorLight = 0xFF197EAB.toInt(),
                        courseColorDark = 0xFF197EAB.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "English Literature 101",
                        courseCode = "EL 101",
                        isLocked = true,
                        gradeText = "",
                        courseColorLight = 0xFF27872B.toInt(),
                        courseColorDark = 0xFF27872B.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "General Astrology",
                        courseCode = "GA",
                        isLocked = false,
                        gradeText = "",
                        courseColorLight = 0xFF00828E.toInt(),
                        courseColorDark = 0xFF00828E.toInt(),
                        ""
                    ),
                )
            )
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 150, heightDp = 200)
    @Composable
    private fun GradesWidgetNarrowPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            GradesWidgetUiState(
                state = WidgetState.Content,
                courses = listOf(
                    WidgetCourseItem(
                        name = "Biology 101",
                        courseCode = "BIO 101",
                        isLocked = false,
                        gradeText = "82%",
                        courseColorLight = 0xFF2573DF.toInt(),
                        courseColorDark = 0xFF2573DF.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "Mathematics 904 2024/25",
                        courseCode = "MAT 904",
                        isLocked = false,
                        gradeText = "Good",
                        courseColorLight = 0xFF9E58BD.toInt(),
                        courseColorDark = 0xFF9E58BD.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "Music Test Course something longer than a line or two for sh...",
                        courseCode = "MTC",
                        isLocked = false,
                        gradeText = "A+",
                        courseColorLight = 0xFF197EAB.toInt(),
                        courseColorDark = 0xFF197EAB.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "English Literature 101",
                        courseCode = "EL 101",
                        isLocked = true,
                        gradeText = "",
                        courseColorLight = 0xFF27872B.toInt(),
                        courseColorDark = 0xFF27872B.toInt(),
                        ""
                    ),
                    WidgetCourseItem(
                        name = "General Astrology",
                        courseCode = "GA",
                        isLocked = false,
                        gradeText = "",
                        courseColorLight = 0xFF00828E.toInt(),
                        courseColorDark = 0xFF00828E.toInt(),
                        ""
                    ),
                )
            )
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun GradesWidgetErrorPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            GradesWidgetUiState(WidgetState.Error)
        )
    }

    companion object {
        private val NARROW = DpSize(100.dp, 110.dp)
        private val WIDE = DpSize(250.dp, 110.dp)
    }
}
