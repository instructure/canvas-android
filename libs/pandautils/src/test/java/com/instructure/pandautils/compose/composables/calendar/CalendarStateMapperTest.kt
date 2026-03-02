package com.instructure.pandautils.compose.composables.calendar

import com.instructure.pandautils.utils.getSystemLocaleCalendar
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Calendar
import java.util.Locale

class CalendarStateMapperTest {

    private val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())

    private val calendarStateMapper = CalendarStateMapper(clock)

    private val locale = Locale.getDefault()

    @Before
    fun setup() {
        Locale.setDefault(Locale.US)

        // Mock getSystemLocaleCalendar to return a simple Calendar instance for testing
        mockkStatic(::getSystemLocaleCalendar)
        every { getSystemLocaleCalendar() } returns Calendar.getInstance()
    }

    @After
    fun tearDown() {
        Locale.setDefault(locale)
        unmockkStatic(::getSystemLocaleCalendar)
    }

    @Test
    fun `Format header UI state for selected date`() {
        val headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.of(2023, 4, 20), null, loading = true)

        Assert.assertEquals("2023", headerUiState.yearTitle)
        Assert.assertEquals("April", headerUiState.monthTitle)
        Assert.assertTrue(headerUiState.loadingMonths)
    }

    @Test
    fun `Format header UI state for pending selected date when it's not null`() {
        val headerUiState = calendarStateMapper.createHeaderUiState(
            LocalDate.of(2023, 4, 20),
            LocalDate.of(2024, 2, 21))

        Assert.assertEquals("2024", headerUiState.yearTitle)
        Assert.assertEquals("February", headerUiState.monthTitle)
    }

    @Test
    fun `Return one row body for calendar when it's not expanded with weekends disabled`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(false, LocalDate.of(2023, 4, 20))

        Assert.assertEquals(1, bodyUiState.currentPage.calendarRows.size)

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false),
            )
        )

        Assert.assertEquals(expectedCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }

    @Test
    fun `Return all rows for the month for calendar when it's expanded with disabled days from previous month`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(true, LocalDate.of(2023, 4, 20))

        Assert.assertEquals(6, bodyUiState.currentPage.calendarRows.size)

        val expectedFirstCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(26, LocalDate.of(2023, 3, 26), false),
                CalendarDayUiState(27, LocalDate.of(2023, 3, 27), false),
                CalendarDayUiState(28, LocalDate.of(2023, 3, 28), false),
                CalendarDayUiState(29, LocalDate.of(2023, 3, 29), false),
                CalendarDayUiState(30, LocalDate.of(2023, 3, 30), false),
                CalendarDayUiState(31, LocalDate.of(2023, 3, 31), false),
                CalendarDayUiState(1, LocalDate.of(2023, 4, 1), false),
            )
        )

        Assert.assertEquals(expectedFirstCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }

    @Test
    fun `Add correct indicator count for days`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = false, LocalDate.of(2023, 4, 20), eventIndicators = mapOf(
                LocalDate.of(2023, 4, 20) to 1,
                LocalDate.of(2023, 4, 22) to 2,
                LocalDate.of(2023, 4, 18) to 3,
            )
        )

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true, indicatorCount = 3),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true, indicatorCount = 1),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false, indicatorCount = 2),
            )
        )

        Assert.assertEquals(expectedCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }

    @Test
    fun `Create previous page with today if jumping to today and today is in a previous week`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = false, selectedDay = LocalDate.of(2023, 5, 20), jumpToToday = true, scrollToPageOffset = -1
        )

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false),
            )
        )

        Assert.assertEquals(expectedCalendarRow, bodyUiState.previousPage.calendarRows.first())
    }

    @Test
    fun `Create next page with today if jumping to today and today is in a next week`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = false, selectedDay = LocalDate.of(2023, 3, 20), jumpToToday = true, scrollToPageOffset = 1
        )

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false),
            )
        )

        Assert.assertEquals(expectedCalendarRow, bodyUiState.nextPage.calendarRows.first())
    }

    @Test
    fun `Create previous expanded page with today if jumping to today and today is in a previous month`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = true, selectedDay = LocalDate.of(2023, 5, 20), jumpToToday = true, scrollToPageOffset = -1
        )

        Assert.assertEquals(6, bodyUiState.previousPage.calendarRows.size)

        val expectedFirstCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(26, LocalDate.of(2023, 3, 26), false),
                CalendarDayUiState(27, LocalDate.of(2023, 3, 27), false),
                CalendarDayUiState(28, LocalDate.of(2023, 3, 28), false),
                CalendarDayUiState(29, LocalDate.of(2023, 3, 29), false),
                CalendarDayUiState(30, LocalDate.of(2023, 3, 30), false),
                CalendarDayUiState(31, LocalDate.of(2023, 3, 31), false),
                CalendarDayUiState(1, LocalDate.of(2023, 4, 1), false),
            )
        )

        Assert.assertEquals(expectedFirstCalendarRow, bodyUiState.previousPage.calendarRows.first())
    }

    @Test
    fun `Create next expanded page with today if jumping to today and today is in a next month`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = true, selectedDay = LocalDate.of(2023, 3, 20), jumpToToday = true, scrollToPageOffset = 1
        )

        Assert.assertEquals(6, bodyUiState.nextPage.calendarRows.size)

        val expectedFirstCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(26, LocalDate.of(2023, 3, 26), false),
                CalendarDayUiState(27, LocalDate.of(2023, 3, 27), false),
                CalendarDayUiState(28, LocalDate.of(2023, 3, 28), false),
                CalendarDayUiState(29, LocalDate.of(2023, 3, 29), false),
                CalendarDayUiState(30, LocalDate.of(2023, 3, 30), false),
                CalendarDayUiState(31, LocalDate.of(2023, 3, 31), false),
                CalendarDayUiState(1, LocalDate.of(2023, 4, 1), false),
            )
        )

        Assert.assertEquals(expectedFirstCalendarRow, bodyUiState.nextPage.calendarRows.first())
    }
}