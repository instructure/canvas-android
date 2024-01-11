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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

private const val MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR = 500

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR)
@PageView(url = "calendar")
class ComposeCalendarFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CalendarViewModel by viewModels()

    @Inject
    lateinit var calendarRouter: CalendarRouter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                CalendarScreen(title(), viewModel) {
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

@Composable
fun CalendarScreen(title: String, viewModel: CalendarViewModel, navigationActionClick: () -> Unit) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                TopAppBar(title = {
                    Text(text = title)
                },
                    actions = {
                        val selectedDay = viewModel.selectedDay.collectAsState().value
                        if (selectedDay != LocalDate.now()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable {
                                    viewModel.jumpToToday()
                                }) {
                                Icon(
                                    painterResource(id = R.drawable.ic_calendar_day),
                                    contentDescription = "",
                                    tint = Color(ThemePrefs.primaryTextColor)
                                )
                                Text(
                                    text = LocalDate.now().dayOfMonth.toString(),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Color(ThemePrefs.primaryTextColor)
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
                    CalendarView(viewModel)
                }
            })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(viewModel: CalendarViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val calendarExpanded = viewModel.expanded.collectAsState()
        var centerIndex by remember { mutableIntStateOf(Int.MAX_VALUE / 2) }
        val currentDay = viewModel.selectedDay.collectAsState()
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            initialPageOffsetFraction = 0f
        ) {
            Int.MAX_VALUE
        }

        LaunchedEffect(pagerState) {
            // Collect from the a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.currentPage }.collect { page ->
                // Do something with each page change, for example:
                val monthOffset = page - centerIndex
                centerIndex = page
                // TODO Maybe move this logic to the ViewModel
                val dateFieldToAdd = if (calendarExpanded.value) ChronoUnit.MONTHS else ChronoUnit.WEEKS
                viewModel.dayChanged(currentDay.value.plus(monthOffset.toLong(), dateFieldToAdd))
            }
        }

        val month = currentDay.value.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = currentDay.value.year

        Spacer(modifier = Modifier.height(8.dp))
        CalendarHeader(month, year.toString(), calendarExpanded.value) { viewModel.expandChanged(it) }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalPager(
            state = pagerState,
            reverseLayout = false,
            pageSize = PageSize.Fill,
            pageContent = { page ->
                val monthOffset = page - centerIndex
                val calendarData = viewModel.calendarData.collectAsState().value
                val monthData = when (monthOffset) {
                    -1 -> calendarData.previous
                    1 -> calendarData.next
                    else -> calendarData.current
                }
                CalendarBody(monthData.calendarRows,
                    Day(currentDay.value.dayOfMonth, currentDay.value),
                    selectedDayChanged = { viewModel.dayChanged(it.date) })
            }
        )
    }
}

@Composable
fun CalendarHeader(
    calendarTitle: String,
    year: String,
    calendarOpen: Boolean,
    calendarOpenChanged: (Boolean) -> Unit
) {
    val iconRotation: Float by animateFloatAsState(targetValue = if (calendarOpen) 0f else 180f)

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    if (screenHeightDp <= MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) calendarOpenChanged(false)
    val clickableModifier = if (screenHeightDp <= MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        Modifier
    } else {
        Modifier.clickable { calendarOpenChanged(!calendarOpen) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = clickableModifier) {
            Text(
                text = year,
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Row {
                Text(
                    text = calendarTitle,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.height(30.dp)
                )
                if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
                    Icon(
                        painterResource(id = R.drawable.ic_chevron_down),
                        tint = colorResource(id = R.color.textDarkest),
                        contentDescription = "",
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
    month: List<CalendarRow>,
    selectedDay: Day,
    selectedDayChanged: (Day) -> Unit
) {
    Column(
        Modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .height(260.dp)) {
        DayHeaders()
        Spacer(modifier = Modifier.height(4.dp))
        CalendarExpanded(month, selectedDay, selectedDayChanged)
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
fun CalendarExpanded(
    monthDays: List<CalendarRow>,
    selectedDay: Day,
    selectedDayChanged: (Day) -> Unit
) {
    Column {
        monthDays.forEach {
            DaysOfWeekRow(days = it.days, selectedDay, selectedDayChanged)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DaysOfWeekRow(
    days: List<Day>,
    selectedDay: Day,
    selectedDayChanged: (Day) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            val textColor = when {
                    day.date == selectedDay.date -> Color(ThemePrefs.buttonTextColor)
                    day.today -> Color(ThemePrefs.textButtonColor)
                    day.enabled -> colorResource(id = R.color.textDarkest)
                    else -> colorResource(id = R.color.textDark)
                }
            var dayModifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .clickable { selectedDayChanged(day) }
            if (day.date == selectedDay.date) {
                dayModifier = dayModifier
                    .background(
                        color = Color(ThemePrefs.buttonColor),
                        shape = RoundedCornerShape(500.dp),
                    )
            }
            dayModifier = dayModifier.wrapContentHeight(align = Alignment.CenterVertically)
            Text(
                text = day.dayNumber.toString(),
                fontSize = 16.sp,
                color = textColor,
                modifier = dayModifier,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    CalendarScreen("Calendar", CalendarViewModel()) {}
}