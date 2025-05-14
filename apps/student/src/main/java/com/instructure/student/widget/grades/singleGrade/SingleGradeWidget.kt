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
 */package com.instructure.student.widget.grades.singleGrade

import android.content.Context
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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.Column
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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.glance.WidgetTextStyles
import com.instructure.student.widget.grades.WidgetCourseItem
import com.jakewharton.threetenabp.AndroidThreeTen

class SingleGradeWidget : GlanceAppWidget() {

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
                prefs[SingleGradeWidgetReceiver.singleGradeWidgetUiStateKey]?.fromJson<SingleGradeWidgetUiState>()
                    ?: SingleGradeWidgetUiState(WidgetState.Loading)
            Content(state)
        }
    }

    @Composable
    private fun Content(state: SingleGradeWidgetUiState) {
        state.course?.let {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.backgroundLightest)
                    .clickable(
                        actionStartActivity(
                            InterwebsToApplication.createIntent(
                                ContextKeeper.appContext,
                                Uri.parse(it.url)
                            )
                        )
                    )
            ) {
                Column {
                    Text(
                        modifier = GlanceModifier.defaultWeight().padding(end = 8.dp),
                        text = if (LocalSize.current.width < WIDE.width) it.courseCode else it.name,
                        style = WidgetTextStyles.mediumDarkest.copy(
                            color = androidx.glance.color.ColorProvider(
                                Color(it.courseColorLight),
                                Color(it.courseColorDark)
                            )
                        )
                    )
                    GradeLayout(it)
                }
            }
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
                TextLabel(label = it, modifier = modifier)
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
    @Preview(widthDp = 110, heightDp = 100)
    @Composable
    private fun GradesWidgetPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            getPreviewSampleData()
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 50, heightDp = 100)
    @Composable
    private fun GradesWidgetNarrowPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            getPreviewSampleData()
        )
    }
}