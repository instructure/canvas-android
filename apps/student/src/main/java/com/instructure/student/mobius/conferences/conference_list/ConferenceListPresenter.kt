/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.mobius.conferences.conference_list

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListItemViewState
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListViewState

object ConferenceListPresenter : Presenter<ConferenceListModel, ConferenceListViewState> {
    override fun present(model: ConferenceListModel, context: Context): ConferenceListViewState {
        // Loading state
        if (model.isLoading) return ConferenceListViewState.Loading(model.isLaunchingInBrowser)

        // Failed state
        if (model.listResult?.isFail == true) {
            return ConferenceListViewState.Loaded(
                model.isLaunchingInBrowser,
                listOf(ConferenceListItemViewState.Error)
            )
        }

        // Success state
        if (model.listResult?.dataOrNull?.isNotEmpty() == true) {
            val color = model.canvasContext.color

            // Sort conferences by in-progress, then not-started, then concluded
            val conferences = model.listResult.dataOrThrow.sortedBy {
                if (it.endedAt != null) 2 else if (it.startedAt != null) 0 else 1
            }

            // Partition into 'new' and 'concluded' lists
            val (new, concluded) = conferences.partition { it.endedAt == null }

            val itemStates: MutableList<ConferenceListItemViewState> = mutableListOf()

            // Set up 'New Conferences' list items
            if (new.isNotEmpty()) {
                // Header
                itemStates.add(
                    ConferenceListItemViewState.ConferenceHeader(
                        context.getString(R.string.newConferences),
                        ConferenceHeaderType.NEW_CONFERENCES,
                        model.isNewConferencesExpanded
                    )
                )

                // Conferences - only add if expanded
                if (model.isNewConferencesExpanded) {
                    itemStates.addAll(new.map {
                        mapItemState(color, it, context)
                    })
                }
            }

            // Set up 'Concluded Conferences' list items
            if (concluded.isNotEmpty()) {
                // Header
                itemStates.add(
                    ConferenceListItemViewState.ConferenceHeader(
                        context.getString(R.string.concludedConferences),
                        ConferenceHeaderType.CONCLUDED_CONFERENCES,
                        model.isConcludedConferencesExpanded
                    )
                )

                // Conferences - only add if expanded
                if (model.isConcludedConferencesExpanded) {
                    itemStates.addAll(concluded.map {
                        mapItemState(color, it, context)
                    })
                }
            }

            return ConferenceListViewState.Loaded(model.isLaunchingInBrowser, itemStates)
        }

        // Empty state
        return ConferenceListViewState.Loaded(
            model.isLaunchingInBrowser,
            listOf(ConferenceListItemViewState.Empty)
        )
    }

    @VisibleForTesting
    fun mapItemState(color: Int, item: Conference, context: Context): ConferenceListItemViewState {
        val title = item.title.orEmpty()
        val subtitle = item.description.orEmpty()

        var isJoinable = false
        val label: String
        val labelTint: Int

        when {
            item.endedAt != null -> {
                val date = DateHelper.dayMonthDateFormat.format(item.endedAt)
                val time = DateHelper.getFormattedTime(context, item.endedAt)
                label = context.getString(R.string.conferenceConcludedDateAtTime, date, time)
                labelTint = ContextCompat.getColor(context, R.color.textDark)
            }
            item.startedAt != null -> {
                isJoinable = true
                label = context.getString(R.string.inProgress)
                labelTint = ContextCompat.getColor(context, R.color.textSuccess)
            }
            else -> {
                label = context.getString(R.string.notStarted)
                labelTint = ContextCompat.getColor(context, R.color.textDark)
            }
        }

        return ConferenceListItemViewState.ConferenceItem(color, title, subtitle, label, labelTint, item.id, isJoinable)
    }
}
