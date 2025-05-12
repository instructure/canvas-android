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

package com.instructure.student.widget.grades

import android.content.Context
import android.content.res.Configuration
import android.util.Log
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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandares.R
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetTextStyles


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
            val courses = prefs[GradesWidgetReceiver.coursesKey]?.map {
                it.fromJson<Course>()
            }.orEmpty()

            val isDarkMode = isDarkMode(context)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.backgroundLightest)
            ) {
                Content(courses, isDarkMode)
            }

        }
    }

    @Composable
    private fun Content(courses: List<Course>, isDarkMode: Boolean) {
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            itemsIndexed(items = courses) { index, item ->
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp, 8.dp)
                ) {
                    val name = if (LocalSize.current.width < WIDE.width) item.courseCode else item.name
                    val size = LocalSize.current
                    Log.d("ASDF", size.width.toString())
                    Text(
                        text = (if (LocalSize.current.width < WIDE.width) item.courseCode else item.name) ?: item.name,
                        style = WidgetTextStyles.mediumDarkest.copy(
                            color = ColorProvider(
                                Color(
                                    getCanvasContextTextColor(item, isDarkMode)
                                )
                            )
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    // TODO implement logic
                    when (index) {
                        0 -> RatioLabel(81, 100)
                        1 -> TextLabel("Good")
                        2 -> TextLabel("No Grades", WidgetColors.textDark, FontWeight.Normal)
                        3 -> LockComponent()
                        else -> TextLabel("72%")
                    }

                }

            }
        }
    }

    @Composable
    private fun RatioLabel(points: Int, max: Int) {
        Row {
            Text(
                text = points.toString(),
                style = WidgetTextStyles.mediumDarkest
            )
            Text(
                text = " / $max",
                style = WidgetTextStyles.normalDark
            )
        }
    }

    @Composable
    private fun TextLabel(
        label: String,
        textColor: ColorProvider = WidgetColors.textDarkest,
        fontWeight: FontWeight = FontWeight.Medium
    ) {
        Text(
            text = label,
            style = WidgetTextStyles.mediumDarkest.copy(color = textColor, fontWeight = fontWeight)
        )
    }

    @Composable
    private fun LockComponent() {
        Image(
            modifier = GlanceModifier.size(16.dp),
            provider = ImageProvider(R.drawable.ic_lock),
            contentDescription = LocalContext.current.getString(R.string.locked),
            colorFilter = ColorFilter.tint(WidgetColors.textDark)
        )
    }

    private fun isDarkMode(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun getCanvasContextTextColor(canvasContext: CanvasContext?, isDarkMode: Boolean): Int {
        val themedColor = ColorKeeper.getOrGenerateColor(canvasContext)
        return if (isDarkMode) themedColor.dark else themedColor.light
    }
}
