@file:OptIn(ExperimentalFoundationApi::class)

package com.instructure.pandautils.features.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.textAndIconColor
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale
import javax.inject.Inject

private const val MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR = 500
private const val HEADER_HEIGHT = 20
private const val CALENDAR_ROW_HEIGHT = 46

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR)
@PageView(url = "calendar")
class ComposeCalendarFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CalendarViewModel by viewModels()

    @Inject
    lateinit var calendarRouter: CalendarRouter

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CalendarScreen(title(), uiState, actionHandler = {
                    viewModel.handleAction(it)
                }) {
                    calendarRouter.openNavigationDrawer()
                }
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.calendar)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment? {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    companion object {
        fun newInstance(route: Route) = ComposeCalendarFragment()

        fun makeRoute() = Route(ComposeCalendarFragment::class.java, null)
    }
}

@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
    title: String,
    calendarUiState: CalendarUiState,
    actionHandler: (CalendarAction) -> Unit,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                TopAppBar(title = {
                    Text(text = title)
                },
                    actions = {
                        if (calendarUiState.selectedDay != LocalDate.now()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable {
                                    actionHandler(CalendarAction.TodayTapped)
                                }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_calendar_day),
                                    contentDescription = stringResource(id = R.string.a11y_contentDescriptionCalendarJumpToToday),
                                    tint = Color(ThemePrefs.primaryTextColor)
                                )
                                Text(
                                    text = LocalDate.now().dayOfMonth.toString(),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Color(ThemePrefs.primaryTextColor),
                                )
                            }
                        }
                    },
                    backgroundColor = Color(ThemePrefs.primaryColor),
                    contentColor = Color(ThemePrefs.primaryTextColor),
                    navigationIcon = {
                        IconButton(onClick = navigationActionClick) {
                            Icon(
                                painterResource(id = R.drawable.ic_hamburger),
                                contentDescription = stringResource(id = R.string.navigation_drawer_open)
                            )
                        }

                    })
            },
            content = { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingValues(0.dp, 8.dp, 0.dp, 16.dp)),
                    color = colorResource(id = R.color.backgroundLightest),
                ) {
                    Column {
                        CalendarView(calendarUiState, actionHandler)
                        CalendarEventsView(calendarUiState.calendarEventsUiState, actionHandler)
                    }
                }
            })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(calendarUiState: CalendarUiState, actionHandler: (CalendarAction) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var centerIndex by remember { mutableIntStateOf(Int.MAX_VALUE / 2) }
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            initialPageOffsetFraction = 0f
        ) {
            Int.MAX_VALUE
        }

        LaunchedEffect(pagerState) {
            // Collect from the a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.settledPage }.collect { page ->
                // Do something with each page change, for example:
                val monthOffset = page - centerIndex
                centerIndex = page
                actionHandler(CalendarAction.PageChanged(monthOffset))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        CalendarHeader(calendarUiState.headerUiState, calendarUiState.expanded, actionHandler)
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            reverseLayout = false,
            pageSize = PageSize.Fill,
            pageContent = { page ->
                val settledPage = pagerState.settledPage

                val monthOffset = page - centerIndex
                val calendarBodyUiState = calendarUiState.bodyUiState
                val calendarPageUiState = when (monthOffset) {
                    -1 -> calendarBodyUiState.previousPage
                    1 -> calendarBodyUiState.nextPage
                    else -> calendarBodyUiState.currentPage
                }

                val rowsHeight =
                    if (calendarUiState.expanded) CALENDAR_ROW_HEIGHT * calendarBodyUiState.currentPage.calendarRows.size else CALENDAR_ROW_HEIGHT
                val height = rowsHeight + HEADER_HEIGHT

                if (page >= settledPage - 1 && page <= settledPage + 1) {
                    CalendarBody(calendarPageUiState.calendarRows,
                        calendarUiState.selectedDay,
                        height = height,
                        selectedDayChanged = { actionHandler(CalendarAction.DaySelected(it)) })
                } else {
                    Box(
                        Modifier
                            .height(height.dp)
                            .fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(ThemePrefs.buttonColor))
                    }
                }
            }
        )
    }
}

@Composable
fun CalendarHeader(
    headerUiState: CalendarHeaderUiState,
    calendarOpen: Boolean,
    actionHandler: (CalendarAction) -> Unit
) {
    val iconRotation: Float by animateFloatAsState(targetValue = if (calendarOpen) 0f else 180f)

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    if (screenHeightDp <= MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) actionHandler(CalendarAction.ExpandDisabled)

    var monthRowModifier = Modifier.semantics(mergeDescendants = true){}
    if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        monthRowModifier = monthRowModifier.clickable(
            onClick = { actionHandler(CalendarAction.ExpandChanged) },
            onClickLabel = stringResource(id = if (calendarOpen) R.string.a11y_calendarSwitchToWeekView else R.string.a11y_calendarSwitchToMonthView)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = monthRowModifier) {
            Text(
                text = headerUiState.yearTitle,
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Row {
                Text(
                    text = headerUiState.monthTitle,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.height(30.dp)
                )
                if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
                    Icon(
                        painterResource(id = R.drawable.ic_chevron_down),
                        tint = colorResource(id = R.color.textDarkest),
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(iconRotation + 180)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarBody(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    height: Int,
    selectedDayChanged: (LocalDate) -> Unit
) {
    Column(
        Modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .height(height.dp)) {
        DayHeaders()
        Spacer(modifier = Modifier.height(4.dp))
        CalendarPage(calendarRows, selectedDay, selectedDayChanged)
    }
}

@Composable
fun DayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = DayOfWeek.values()
        // Shift the starting point to Sunday
        val shiftedDaysOfWeek = Array(7) { daysOfWeek[(it + 6) % 7] }

        for (day in shiftedDaysOfWeek) {
            val headerText = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val colorResource =
                if (day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY) R.color.textDark else R.color.textDarkest
            Text(
                text = headerText,
                fontSize = 12.sp,
                color = colorResource(id = colorResource),
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarPage(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit
) {
    Column {
        calendarRows.forEach {
            DaysOfWeekRow(days = it.days, selectedDay, selectedDayChanged)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun DaysOfWeekRow(
    days: List<CalendarDayUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { dayState ->
            val textColor = when {
                dayState.date == selectedDay -> Color(ThemePrefs.buttonTextColor)
                dayState.today -> Color(ThemePrefs.textButtonColor)
                dayState.enabled -> colorResource(id = R.color.textDarkest)
                else -> colorResource(id = R.color.textDark)
            }
            var dayModifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .clickable { selectedDayChanged(dayState.date) }
            if (dayState.date == selectedDay) {
                dayModifier = dayModifier
                    .background(
                        color = Color(ThemePrefs.buttonColor),
                        shape = RoundedCornerShape(500.dp),
                    )
            }
            dayModifier = dayModifier.wrapContentHeight(align = Alignment.CenterVertically)
            Column(
                Modifier
                    .width(32.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = dayState.dayNumber.toString(),
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = dayModifier,
                    textAlign = TextAlign.Center
                )
                Row(
                    Modifier
                        .height(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(dayState.indicatorCount) {
                        EventIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun EventIndicator() {
    Box(
        Modifier
            .padding(horizontal = 3.dp)
            .graphicsLayer()
            .clip(CircleShape)
            .size(4.dp)
            .background(Color(ThemePrefs.buttonColor))
    )
}

@ExperimentalFoundationApi
@Composable
fun CalendarEventsView(
    calendarEventsUiState: CalendarEventsUiState,
    actionHandler: (CalendarAction) -> Unit
) {
    var centerIndex by remember { mutableIntStateOf(Int.MAX_VALUE / 2) }
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        initialPageOffsetFraction = 0f
    ) {
        Int.MAX_VALUE
    }

    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.settledPage }.collect { page ->
            // Do something with each page change, for example:
            val monthOffset = page - centerIndex
            centerIndex = page
            actionHandler(CalendarAction.EventPageChanged(monthOffset))
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondBoundsPageCount = 2,
        reverseLayout = false,
        pageSize = PageSize.Fill,
        pageContent = { page ->
            val settledPage = pagerState.settledPage

            val monthOffset = page - centerIndex
            val calendarEventsPageUiState = when (monthOffset) {
                -1 -> calendarEventsUiState.previousPage
                1 -> calendarEventsUiState.nextPage
                else -> calendarEventsUiState.currentPage
            }

            if (page >= settledPage - 1 && page <= settledPage + 1 && !calendarEventsPageUiState.loading) {
                CalendarEventsPage(calendarEventsPageUiState = calendarEventsPageUiState)
            } else {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(ThemePrefs.buttonColor))
                }
            }
        }
    )
}

@Composable
fun CalendarEventsPage(calendarEventsPageUiState: CalendarEventsPageUiState) {
    if (calendarEventsPageUiState.events.isNotEmpty()) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(), verticalArrangement = Arrangement.Top
        ) {
            items(calendarEventsPageUiState.events) {
                CalendarEventItem(eventUiState = it)
            }
        }
    } else {
        CalendarEmptyView()
    }
}

@Composable
fun CalendarEventItem(eventUiState: EventUiState) {
    Row {
        Icon(
            painter = painterResource(id = R.drawable.ic_assignment),
            contentDescription = null,
            tint = Color(eventUiState.canvasContext.textAndIconColor)
        )
        Column {
            Text(text = eventUiState.contextName)
            Text(text = eventUiState.name)
            Text(text = eventUiState.dueDate.toString())
            Text(text = eventUiState.status)
        }
    }
}

@Composable
fun CalendarEmptyView() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_events),
            tint = Color.Unspecified,
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.calendarNoEvents),
            fontSize = 22.sp,
            color = colorResource(
                id = R.color.textDarkest
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.calendarNoEventsDescription),
            fontSize = 16.sp,
            color = colorResource(
                id = R.color.textDarkest
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CalendarScreen("Calendar", CalendarUiState(
        LocalDate.now().plusDays(1), true, CalendarEventsUiState(
            currentPage = CalendarEventsPageUiState(
                events = listOf(
                    EventUiState(
                        "Course To Do",
                        CanvasContext.defaultCanvasContext(),
                        "Todo 1"
                    ),
                    EventUiState(
                        "Course",
                        CanvasContext.defaultCanvasContext(),
                        "Assignment 1",
                        LocalDate.now().plusDays(1),
                        "Missing"
                    )
                )
            )
        )
    ), {}) {}
}