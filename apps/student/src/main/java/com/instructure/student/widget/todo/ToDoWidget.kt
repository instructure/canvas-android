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

package com.instructure.student.widget.todo

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.widget.glance.Empty
import com.instructure.student.widget.glance.Error
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.NotLoggedIn
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState


class ToDoWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val state = WidgetState.valueOf(prefs[ToDoWidgetReceiver.stateKey] ?: WidgetState.Loading.name)
            val plannerItems = prefs.get(ToDoWidgetReceiver.plannerItemsKey)?.map {
                it.fromJson<PlannerItem>()
            }.orEmpty()

            Scaffold(
                backgroundColor = WidgetColors.backgroundLightest,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                when (state) {
                    WidgetState.Loading -> Loading()
                    WidgetState.Error -> Error()
                    WidgetState.Empty -> Empty()
                    WidgetState.NotLoggedIn -> NotLoggedIn()
                    WidgetState.Content -> Content(plannerItems)
                }
            }
        }
    }

    @Composable
    private fun Content(
        plannerItems: List<PlannerItem>
    ) {
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            items(items = plannerItems) {
                Text(
                    text = it.plannable.title,
                    style = TextStyle(
                        color = WidgetColors.textDarkest,
                    )
                )
            }
        }
    }
}
