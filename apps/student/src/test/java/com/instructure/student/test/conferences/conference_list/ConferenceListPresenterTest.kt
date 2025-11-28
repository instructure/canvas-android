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
package com.instructure.student.test.conferences.conference_list

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.mobius.conferences.conference_list.ConferenceHeaderType
import com.instructure.student.mobius.conferences.conference_list.ConferenceListModel
import com.instructure.student.mobius.conferences.conference_list.ConferenceListPresenter
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListItemViewState
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ConferenceListPresenterTest : Assert() {
    private lateinit var context: Context
    private lateinit var canvasContext: CanvasContext

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        canvasContext = Course(id = 123L)
    }

    @Test
    fun `Returns loading state if loading`() {
        // Set up model with loading state
        val model = ConferenceListModel(canvasContext, isLoading = true)

        // Generate state
        val state = ConferenceListPresenter.present(model, context)

        // Stat should be an instance of ConferenceListViewState.Loading
        assert(state is ConferenceListViewState.Loading)
    }

    @Test
    fun `Returns single failed item state if loading failed`() {
        // Set up model with failed result
        val result = DataResult.Fail()
        val model = ConferenceListModel(canvasContext, listResult = result)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        // Stat should be an instance of ConferenceListViewState.Loading
        assertEquals(state.itemStates.size, 1)
        assert(state.itemStates[0] is ConferenceListItemViewState.Error)
    }

    @Test
    fun `Returns single empty item state if results are empty`() {
        // Set up model with empty result
        val result = DataResult.Success(emptyList<Conference>())
        val model = ConferenceListModel(canvasContext, listResult = result)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        // Stat should be an instance of ConferenceListViewState.Loading
        assertEquals(state.itemStates.size, 1)
        assert(state.itemStates[0] is ConferenceListItemViewState.Empty)
    }

    @Test
    fun `Returns only 'new' header and items if there are no concluded conferences`() {
        // Set up model with a dateless (i.e. not-started) Conference
        val conference = Conference()
        val result = DataResult.Success(listOf(conference))
        val model = ConferenceListModel(canvasContext, listResult = result)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val expectedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.newConferences),
            ConferenceHeaderType.NEW_CONFERENCES,
            true
        )
        val expectedConferenceItem = ConferenceListPresenter.mapItemState(canvasContext.color, conference, context)

        // Should have two list items - one header and one conference
        assertEquals(state.itemStates.size, 2)
        assertEquals(state.itemStates[0], expectedHeader)
        assertEquals(state.itemStates[1], expectedConferenceItem)
    }

    @Test
    fun `Returns only 'concluded' header and items if there are no new conferences`() {
        // Set up model with a concluded Conference
        val conference = Conference(startedAt = Date(), endedAt = Date())
        val result = DataResult.Success(listOf(conference))
        val model = ConferenceListModel(canvasContext, listResult = result)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val expectedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.concludedConferences),
            ConferenceHeaderType.CONCLUDED_CONFERENCES,
            true
        )
        val expectedConferenceItem = ConferenceListPresenter.mapItemState(canvasContext.color, conference, context)

        // Should have two list items - one header and one conference
        assertEquals(state.itemStates.size, 2)
        assertEquals(state.itemStates[0], expectedHeader)
        assertEquals(state.itemStates[1], expectedConferenceItem)
    }

    @Test
    fun `Correctly sorts conference items`() {
        // Set up model with all three types
        val notStarted = Conference(startedAt = null, endedAt = null)
        val concluded = Conference(startedAt = Date(), endedAt = Date())
        val inProgress = Conference(startedAt = Date(), endedAt = null)
        val result = DataResult.Success(listOf(notStarted, concluded, inProgress))
        val model = ConferenceListModel(canvasContext, listResult = result)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val newHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.newConferences),
            ConferenceHeaderType.NEW_CONFERENCES,
            true
        )
        val concludedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.concludedConferences),
            ConferenceHeaderType.CONCLUDED_CONFERENCES,
            true
        )
        val inProgressItem = ConferenceListPresenter.mapItemState(canvasContext.color, inProgress, context)
        val notStartedItem = ConferenceListPresenter.mapItemState(canvasContext.color, notStarted, context)
        val concludedItem = ConferenceListPresenter.mapItemState(canvasContext.color, concluded, context)

        // Should sort by in-progress, then not-started, then concluded, with appropriate headers
        // Should have five list items - two headers and three conferences
        assertEquals(state.itemStates.size, 5)
        assertEquals(state.itemStates[0], newHeader)
        assertEquals(state.itemStates[1], inProgressItem)
        assertEquals(state.itemStates[2], notStartedItem)
        assertEquals(state.itemStates[3], concludedHeader)
        assertEquals(state.itemStates[4], concludedItem)
    }

    @Test
    fun `mapItemState returns correct state for in-progress conference`() {
        // Set up in-progress conference
        val conference = Conference(
            id = 123L,
            title = "Conference Title",
            description = "Conference Description",
            startedAt = Date(),
            endedAt = null
        )

        // Generate state
        val state = ConferenceListPresenter.mapItemState(canvasContext.color, conference, context)

        // Expected state. Notably, label tint is green and isJoinable is true.
        val expected = ConferenceListItemViewState.ConferenceItem(
            tint = canvasContext.color,
            title  = conference.title!!,
            subtitle = conference.description!!,
            label = context.getString(R.string.inProgress),
            labelTint = context.getColor(R.color.textSuccess),
            conferenceId = conference.id,
            isJoinable = true
        )

        assertEquals(state, expected)
    }

    @Test
    fun `mapItemState returns correct state for not-started conference`() {
        // Set up in-progress conference
        val conference = Conference(
            id = 123L,
            title = "Conference Title",
            description = "Conference Description",
            startedAt = null,
            endedAt = null
        )

        // Generate state
        val state = ConferenceListPresenter.mapItemState(canvasContext.color, conference, context)

        // Expected state. Notably, label tint is grey and isJoinable is false.
        val expected = ConferenceListItemViewState.ConferenceItem(
            tint = canvasContext.color,
            title  = conference.title!!,
            subtitle = conference.description!!,
            label = context.getString(R.string.notStarted),
            labelTint = context.getColor(R.color.textDark),
            conferenceId = conference.id,
            isJoinable = false
        )

        assertEquals(state, expected)
    }

    @Test
    fun `mapItemState returns correct state for concluded conference`() {
        // Set up in-progress conference
        val conference = Conference(
            id = 123L,
            title = "Conference Title",
            description = "Conference Description",
            startedAt = Date(),
            endedAt = Calendar.getInstance().apply { set(2000, 0, 1, 8, 57) }.time
        )

        // Generate state
        val state = ConferenceListPresenter.mapItemState(canvasContext.color, conference, context)

        // Expected state. Notably, label tint is grey and isJoinable is false.
        val expected = ConferenceListItemViewState.ConferenceItem(
            tint = canvasContext.color,
            title  = conference.title!!,
            subtitle = conference.description!!,
            label = "Concluded Jan 1 at 8:57 AM",
            labelTint = context.getColor(R.color.textDark),
            conferenceId = conference.id,
            isJoinable = false
        )

        assertEquals(state, expected)
    }

    @Test
    fun `Returns only header when new conferences section is collapsed`() {
        // Set up model with a not-started conference but collapsed state
        val conference = Conference()
        val result = DataResult.Success(listOf(conference))
        val model = ConferenceListModel(canvasContext, listResult = result, isNewConferencesExpanded = false)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val expectedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.newConferences),
            ConferenceHeaderType.NEW_CONFERENCES,
            false
        )

        // Should have only the header, no conference items
        assertEquals(state.itemStates.size, 1)
        assertEquals(state.itemStates[0], expectedHeader)
    }

    @Test
    fun `Returns only header when concluded conferences section is collapsed`() {
        // Set up model with a concluded conference but collapsed state
        val conference = Conference(startedAt = Date(), endedAt = Date())
        val result = DataResult.Success(listOf(conference))
        val model = ConferenceListModel(canvasContext, listResult = result, isConcludedConferencesExpanded = false)

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val expectedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.concludedConferences),
            ConferenceHeaderType.CONCLUDED_CONFERENCES,
            false
        )

        // Should have only the header, no conference items
        assertEquals(state.itemStates.size, 1)
        assertEquals(state.itemStates[0], expectedHeader)
    }

    @Test
    fun `Sections can be collapsed independently`() {
        // Set up model with both types but new conferences collapsed
        val notStarted = Conference(startedAt = null, endedAt = null)
        val concluded = Conference(startedAt = Date(), endedAt = Date())
        val result = DataResult.Success(listOf(notStarted, concluded))
        val model = ConferenceListModel(
            canvasContext,
            listResult = result,
            isNewConferencesExpanded = false,
            isConcludedConferencesExpanded = true
        )

        // Generate state
        val state = ConferenceListPresenter.present(model, context) as ConferenceListViewState.Loaded

        val newHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.newConferences),
            ConferenceHeaderType.NEW_CONFERENCES,
            false
        )
        val concludedHeader = ConferenceListItemViewState.ConferenceHeader(
            context.getString(R.string.concludedConferences),
            ConferenceHeaderType.CONCLUDED_CONFERENCES,
            true
        )
        val concludedItem = ConferenceListPresenter.mapItemState(canvasContext.color, concluded, context)

        // Should have collapsed new header (no items), expanded concluded header with item
        assertEquals(state.itemStates.size, 3)
        assertEquals(state.itemStates[0], newHeader)
        assertEquals(state.itemStates[1], concludedHeader)
        assertEquals(state.itemStates[2], concludedItem)
    }
}
