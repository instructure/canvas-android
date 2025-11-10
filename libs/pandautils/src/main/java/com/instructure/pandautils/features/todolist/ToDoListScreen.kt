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
package com.instructure.pandautils.features.todolist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.features.todolist.filter.ToDoFilterScreen
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.courseOrUserColor
import com.instructure.pandautils.utils.performGestureHapticFeedback
import com.instructure.pandautils.utils.performToggleHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_DP = 150

private data class StickyHeaderState(
    val item: ToDoItemUiState?,
    val yOffset: Float,
    val isVisible: Boolean
)

private data class DateBadgeData(
    val dayOfWeek: String,
    val day: Int,
    val month: String,
    val isToday: Boolean,
    val dateTextColor: Color
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ToDoListScreen(
    navigationIconClick: () -> Unit,
    openToDoItem: (String) -> Unit,
    onToDoCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onFilterScreenVisibilityChanged: (isShowing: Boolean, closeCallback: () -> Unit) -> Unit = { _, _ -> }
) {
    val viewModel = hiltViewModel<ToDoListViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showFilterScreen by rememberSaveable { mutableStateOf(false) }

    // Notify fragment when filter screen visibility changes
    // Use SideEffect for synchronous state updates (runs after every successful composition)
    SideEffect {
        onFilterScreenVisibilityChanged(showFilterScreen) {
            showFilterScreen = false
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            uiState.onSnackbarDismissed()
        }
    }

    LaunchedEffect(uiState.confirmationSnackbarData) {
        uiState.confirmationSnackbarData?.let { item ->
            val messageRes = if (item.markedAsDone) {
                R.string.todoMarkedAsDone
            } else {
                R.string.todoMarkedAsNotDone
            }
            val message = context.getString(messageRes, item.title)
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.todoMarkedAsDoneSnackbarUndo),
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                uiState.onUndoMarkAsDoneUndoneAction()
            } else {
                uiState.onMarkedAsDoneSnackbarDismissed()
            }
        }
    }

    LaunchedEffect(uiState.toDoCount) {
        uiState.toDoCount?.let { count ->
            onToDoCountChanged(count)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = uiState.onRefresh
    )

    Scaffold(
        backgroundColor = colorResource(R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.Todo),
                navIconRes = R.drawable.ic_hamburger,
                navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                navigationActionClick = navigationIconClick,
                actions = {
                    IconButton(onClick = { showFilterScreen = true }) {
                        Icon(
                            painter = painterResource(
                                id = if (uiState.isFilterApplied) R.drawable.ic_filter_filled else R.drawable.ic_filter_outline
                            ),
                            contentDescription = stringResource(id = R.string.a11y_contentDescriptionToDoFilter)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = Color(ThemePrefs.textButtonColor)
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            ToDoListContent(
                uiState = uiState,
                onOpenToDoItem = openToDoItem
            )

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = colorResource(id = R.color.backgroundLightest),
                contentColor = Color(ThemePrefs.brandColor)
            )
        }
    }

    if (showFilterScreen) {
        Dialog(
            onDismissRequest = { showFilterScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            ToDoFilterScreen(
                onFiltersChanged = { dateFiltersChanged ->
                    showFilterScreen = false
                    uiState.onFiltersChanged(dateFiltersChanged)
                },
                onDismiss = {
                    showFilterScreen = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ToDoListContent(
    uiState: ToDoListUiState,
    onOpenToDoItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Loading(modifier = modifier.fillMaxSize())
        }

        uiState.isError -> {
            ErrorContent(
                errorMessage = stringResource(id = R.string.errorLoadingToDos),
                retryClick = uiState.onRefresh,
                modifier = modifier.fillMaxSize()
            )
        }

        uiState.itemsByDate.isEmpty() -> {
            EmptyContent(
                emptyTitle = stringResource(id = R.string.noToDosForNow),
                emptyMessage = stringResource(id = R.string.noToDosForNowSubtext),
                imageRes = R.drawable.ic_no_events,
                modifier = modifier.fillMaxSize()
            )
        }

        else -> {
            ToDoItemsList(
                itemsByDate = uiState.itemsByDate,
                onItemClicked = onOpenToDoItem,
                removingItemIds = uiState.removingItemIds,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ToDoItemsList(
    itemsByDate: Map<Date, List<ToDoItemUiState>>,
    onItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    removingItemIds: Set<String> = emptySet()
) {
    // Filter out items that are being removed
    val filteredItemsByDate = itemsByDate.mapValues { (_, items) ->
        items.filter { it.id !in removingItemIds }
    }.filterValues { it.isNotEmpty() }

    val dateGroups = filteredItemsByDate.entries.toList()
    val listState = rememberLazyListState()
    val itemPositions = remember { mutableStateMapOf<String, Float>() }
    val itemSizes = remember { mutableStateMapOf<String, Int>() }
    val density = LocalDensity.current
    var listHeight by remember { mutableIntStateOf(0) }

    val stickyHeaderState = rememberStickyHeaderState(
        dateGroups = dateGroups,
        listState = listState,
        itemPositions = itemPositions,
        density = density
    )

    // Calculate content height from last item's position + size
    val listContentHeight by remember {
        derivedStateOf {
            if (dateGroups.isEmpty()) return@derivedStateOf 0
            val lastGroup = dateGroups.last()
            val lastItem = lastGroup.value.last()
            val lastItemPosition = itemPositions[lastItem.id] ?: return@derivedStateOf 0
            val lastItemSize = itemSizes[lastItem.id] ?: return@derivedStateOf 0
            (lastItemPosition + lastItemSize).toInt()
        }
    }

    // Calculate if there's enough space for pandas (at least 140dp)
    val availableSpacePx = listHeight - listContentHeight
    val minSpaceForPandasPx = with(density) { 140.dp.toPx() }
    val showPandas = listHeight > 0 && listContentHeight > 0 &&
                     availableSpacePx >= minSpaceForPandasPx &&
                     itemsByDate.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    listHeight = coordinates.size.height
                }
        ) {
            dateGroups.forEachIndexed { groupIndex, (date, items) ->
                items.forEachIndexed { index, item ->
                    item(key = item.id) {
                        ToDoItem(
                            item = item,
                            showDateBadge = index == 0,
                            hideDate = index == 0 && stickyHeaderState.isVisible && stickyHeaderState.item?.id == item.id,
                            onCheckedChange = { item.onCheckboxToggle(!item.isChecked) },
                            onClick = { onItemClicked(item.id) },
                            modifier = Modifier
                                .animateItem()
                                .onGloballyPositioned { coordinates ->
                                    itemPositions[item.id] = coordinates.positionInParent().y
                                    itemSizes[item.id] = coordinates.size.height
                                }
                        )
                    }
                }

                // Add divider between date groups, or after last group if pandas are showing
                if (groupIndex < dateGroups.size - 1 || showPandas) {
                    item(key = "divider_$date") {
                        val isLastDivider = groupIndex == dateGroups.size - 1
                        CanvasDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .conditional(!isLastDivider) {
                                    padding(horizontal = 16.dp)
                                }
                        )
                    }
                }
            }
        }

        // Panda illustrations
        if (showPandas) {
            PandaIllustrations(contentHeightPx = listContentHeight.toFloat())
        }

        // Sticky header overlay
        stickyHeaderState.item?.let { item ->
            if (stickyHeaderState.isVisible) {
                StickyDateBadge(
                    item = item,
                    yOffset = stickyHeaderState.yOffset
                )
            }
        }
    }
}

@Composable
private fun StickyDateBadge(
    item: ToDoItemUiState,
    yOffset: Float
) {
    val dateBadgeData = rememberDateBadgeData(item.date)

    Box(
        modifier = Modifier
            .offset { IntOffset(0, yOffset.roundToInt()) }
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .padding(end = 12.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.TopCenter
        ) {
            DateBadge(dateBadgeData)
        }
    }
}

@Composable
private fun ToDoItem(
    item: ToDoItemUiState,
    showDateBadge: Boolean,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hideDate: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedOffsetX = remember { Animatable(0f) }
    var itemWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val view = LocalView.current

    // Track the isChecked state that SwipeBackground should display
    // Only update when item has settled back (offsetX is 0)
    var swipeBackgroundIsChecked by remember { mutableStateOf(item.isChecked) }

    // Update swipeBackgroundIsChecked only when offset is 0 and item.isChecked has changed
    LaunchedEffect(animatedOffsetX.value, item.isChecked) {
        if (animatedOffsetX.value == 0f && swipeBackgroundIsChecked != item.isChecked) {
            swipeBackgroundIsChecked = item.isChecked
        }
    }

    val swipeThreshold = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }

    fun animateToCenter() {
        coroutineScope.launch {
            animatedOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    fun handleSwipeEnd() {
        coroutineScope.launch {
            val currentOffset = animatedOffsetX.value
            val absOffset = abs(currentOffset)
            if (absOffset >= swipeThreshold) {
                val targetOffset = if (currentOffset > 0) itemWidth else -itemWidth
                animatedOffsetX.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(durationMillis = 200)
                )

                // Gesture end haptic feedback
                view.performGestureHapticFeedback(isStart = false)
                delay(300)

                // Trigger the action
                item.onSwipeToDone()

                // Animate back to center - if item gets removed, it will disappear during/after animation
                animateToCenter()
            } else {
                animateToCenter()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                itemWidth = coordinates.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        // Gesture start haptic feedback when user begins dragging
                        view.performGestureHapticFeedback(isStart = true)
                    },
                    onDragEnd = { handleSwipeEnd() },
                    onDragCancel = {
                        animateToCenter()
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            val newOffset = (animatedOffsetX.value + dragAmount).coerceIn(-itemWidth, itemWidth)
                            animatedOffsetX.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        SwipeBackground(
            isChecked = swipeBackgroundIsChecked,
            offsetX = animatedOffsetX.value
        )

        ToDoItemContent(
            item = item,
            showDateBadge = showDateBadge,
            hideDate = hideDate,
            onCheckedChange = onCheckedChange,
            onClick = onClick,
            modifier = Modifier.offset { IntOffset(animatedOffsetX.value.roundToInt(), 0) }
        )
    }
}

@Composable
private fun BoxScope.SwipeBackground(isChecked: Boolean, offsetX: Float) {
    val backgroundColor = if (isChecked) {
        colorResource(R.color.backgroundDark)
    } else {
        colorResource(R.color.backgroundSuccess)
    }

    val text = if (isChecked) {
        stringResource(id = R.string.todoSwipeUndo)
    } else {
        stringResource(id = R.string.todoSwipeDone)
    }

    val icon = if (isChecked) {
        R.drawable.ic_reply
    } else {
        R.drawable.ic_checkmark_lined
    }

    // Calculate alpha based on swipe progress with ease-in curve
    val density = LocalDensity.current
    val swipeThreshold = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }
    val progress = (abs(offsetX) / swipeThreshold).coerceIn(0f, 1f)
    // Apply ease-in cubic easing for gradual fade-in that accelerates near threshold
    val alpha = progress * progress * progress

    Box(
        modifier = Modifier
            .matchParentSize()
            .background(backgroundColor)
    ) {
        if (offsetX > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .alpha(alpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = colorResource(R.color.textLightest),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textLightest)
                )
            }
        }

        if (offsetX < 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .alpha(alpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textLightest)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = colorResource(R.color.textLightest),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ToDoItemContent(
    item: ToDoItemUiState,
    showDateBadge: Boolean,
    hideDate: Boolean,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateBadgeData = rememberDateBadgeData(item.date)
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.backgroundLightest))
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .padding(end = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (showDateBadge && !hideDate) {
                DateBadge(dateBadgeData)
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val contextColor = Color(item.canvasContext.courseOrUserColor)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        tint = contextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CanvasDivider(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.contextLabel,
                        fontSize = 14.sp,
                        color = contextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                item.tag?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.textDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                item.dateLabel?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.textDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Checkbox(
                checked = item.isChecked,
                onCheckedChange = {
                    // Determine if marking as done or undone based on the new checked state
                    view.performToggleHapticFeedback(it)
                    onCheckedChange()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(ThemePrefs.brandColor),
                    uncheckedColor = colorResource(id = R.color.textDark)
                )
            )
        }
    }
}

@Composable
private fun rememberDateBadgeData(date: Date): DateBadgeData {
    val calendar = remember(date) {
        Calendar.getInstance().apply { time = date }
    }

    val dayOfWeek = remember(date) {
        SimpleDateFormat("EEE", Locale.getDefault()).format(date)
    }

    val day = remember(date) {
        calendar.get(Calendar.DAY_OF_MONTH)
    }

    val month = remember(date) {
        SimpleDateFormat("MMM", Locale.getDefault()).format(date)
    }

    val isToday = remember(date) {
        val today = Calendar.getInstance()
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    val dateTextColor = if (isToday) {
        Color(ThemePrefs.brandColor)
    } else {
        colorResource(id = R.color.textDark)
    }

    return DateBadgeData(dayOfWeek, day, month, isToday, dateTextColor)
}

@Composable
private fun DateBadge(dateBadgeData: DateBadgeData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dateBadgeData.dayOfWeek,
            fontSize = 12.sp,
            color = dateBadgeData.dateTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (dateBadgeData.isToday) {
                Modifier
                    .size(32.dp)
                    .border(width = 1.dp, color = dateBadgeData.dateTextColor, shape = CircleShape)
            } else {
                Modifier
            }
        ) {
            Text(
                text = dateBadgeData.day.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = dateBadgeData.dateTextColor
            )
        }
        Text(
            text = dateBadgeData.month,
            fontSize = 10.sp,
            color = dateBadgeData.dateTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun rememberStickyHeaderState(
    dateGroups: List<Map.Entry<Date, List<ToDoItemUiState>>>,
    listState: LazyListState,
    itemPositions: Map<String, Float>,
    density: Density
): StickyHeaderState {
    return remember(dateGroups) {
        derivedStateOf {
            calculateStickyHeaderState(dateGroups, listState, itemPositions, density)
        }
    }.value
}

private fun calculateStickyHeaderState(
    dateGroups: List<Map.Entry<Date, List<ToDoItemUiState>>>,
    listState: LazyListState,
    itemPositions: Map<String, Float>,
    density: Density
): StickyHeaderState {
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

    // Find which date group's first item has been scrolled past
    var currentGroupIndex = -1
    var itemCount = 0

    for ((groupIndex, group) in dateGroups.withIndex()) {
        val groupItemCount = group.value.size
        if (firstVisibleItemIndex < itemCount + groupItemCount) {
            currentGroupIndex = groupIndex
            break
        }
        itemCount += groupItemCount + if (groupIndex < dateGroups.size - 1) 1 else 0 // +1 for divider
    }

    if (currentGroupIndex == -1 || currentGroupIndex >= dateGroups.size) {
        return StickyHeaderState(null, 0f, false)
    }

    val currentGroup = dateGroups[currentGroupIndex]
    val firstItemOfCurrentGroup = currentGroup.value.first()

    // Check if the first item has scrolled up even slightly
    val shouldShowSticky = if (firstVisibleItemIndex > 0) {
        true
    } else {
        firstVisibleItemScrollOffset > 0
    }

    // Calculate offset for animation when next group approaches
    var yOffset = 0f
    if (currentGroupIndex < dateGroups.size - 1) {
        val nextGroup = dateGroups[currentGroupIndex + 1]
        val nextGroupFirstItem = nextGroup.value.first()
        val nextItemPosition = itemPositions[nextGroupFirstItem.id] ?: Float.MAX_VALUE

        // Calculate date badge height by converting sp and dp values to pixels
        // Date badge components:
        // - dayOfWeek text: 12.sp
        // - day text (in 32.dp box): 12.sp (bold)
        // - month text: 10.sp
        // - All text heights together: 22.sp
        // - item bottom padding: 8.dp
        val textHeightPx = with(density) { 22.sp.toPx() }
        val circleHeightPx = with(density) { 32.dp.toPx() }
        val paddingPx = with(density) { 8.dp.toPx() }
        val stickyHeaderHeightPx = textHeightPx + circleHeightPx + paddingPx

        if (nextItemPosition < stickyHeaderHeightPx && nextItemPosition > 0) {
            yOffset = nextItemPosition - stickyHeaderHeightPx
        }
    }

    return StickyHeaderState(
        item = if (shouldShowSticky) firstItemOfCurrentGroup else null,
        yOffset = yOffset,
        isVisible = shouldShowSticky
    )
}

@Composable
private fun PandaIllustrations(contentHeightPx: Float) {
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right panda - positioned at top-right, below content
        Icon(
            painter = painterResource(id = R.drawable.ic_panda_top),
            contentDescription = null,
            modifier = Modifier
                .width(180.dp)
                .height(137.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = with(density) { (contentHeightPx - 2.5.dp.toPx()).toDp() }),
            tint = Color.Unspecified
        )

        // Bottom-left panda - positioned at bottom-left
        Icon(
            painter = painterResource(id = R.drawable.ic_panda_bottom),
            contentDescription = null,
            modifier = Modifier
                .width(114.dp)
                .height(137.dp)
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = 30.5.dp),
            tint = Color.Unspecified
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ToDoListScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val calendar = Calendar.getInstance()
    CanvasTheme {
        ToDoListContent(
            uiState = ToDoListUiState(
                itemsByDate = mapOf(
                    Date(10) to listOf(
                        ToDoItemUiState(
                            id = "1",
                            title = "Short title",
                            date = calendar.apply { set(2024, 9, 22, 7, 59) }.time,
                            dateLabel = "7:59 AM",
                            contextLabel = "COURSE",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.ASSIGNMENT,
                            iconRes = R.drawable.ic_assignment,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "2",
                            title = "Levitate an object without crushing it, bonus points if you don't scratch the paint",
                            date = calendar.apply { set(2024, 9, 22, 11, 59) }.time,
                            dateLabel = "11:59 AM",
                            contextLabel = "Introduction to Advanced Galactic Force Manipulation and Control Techniques for Beginners",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.QUIZ,
                            iconRes = R.drawable.ic_quiz,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "3",
                            title = "Identify which emotions lead to Jedi calmness vs. a full Darth Vader office meltdown situation",
                            date = calendar.apply { set(2024, 9, 22, 14, 30) }.time,
                            dateLabel = "2:30 PM",
                            contextLabel = "FORC 101",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.ASSIGNMENT,
                            iconRes = R.drawable.ic_assignment,
                            isChecked = true
                        ),
                        ToDoItemUiState(
                            id = "4",
                            title = "Peer review discussion post",
                            date = calendar.apply { set(2024, 9, 22, 16, 0) }.time,
                            dateLabel = "4:00 PM",
                            tag = "Peer Reviews for Exploring Emotional Mastery",
                            contextLabel = "Advanced Force Psychology",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.SUB_ASSIGNMENT,
                            iconRes = R.drawable.ic_discussion,
                            isChecked = false
                        )
                    ),
                    Date(1000) to listOf(
                        ToDoItemUiState(
                            id = "5",
                            title = "Essay - Why Force-choking co-workers is frowned upon in most galactic workplaces",
                            date = calendar.apply { set(2024, 9, 23, 19, 0) }.time,
                            dateLabel = "7:00 PM",
                            contextLabel = "Professional Jedi Ethics and Workplace Communication",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.DISCUSSION,
                            iconRes = R.drawable.ic_discussion,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "6",
                            title = "Personal meditation practice",
                            date = calendar.apply { set(2024, 9, 23, 20, 0) }.time,
                            dateLabel = "8:00 PM",
                            contextLabel = "My Notes",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.PLANNER_NOTE,
                            iconRes = R.drawable.ic_todo,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "7",
                            title = "Q",
                            date = calendar.apply { set(2024, 9, 23, 23, 59) }.time,
                            dateLabel = "11:59 PM",
                            contextLabel = "PHY",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.PLANNER_NOTE,
                            iconRes = R.drawable.ic_todo,
                            isChecked = false
                        )
                    ),
                    Date(2000) to listOf(
                        ToDoItemUiState(
                            id = "9",
                            title = "Lightsaber maintenance workshop",
                            date = calendar.apply { set(2024, 9, 24, 10, 0) }.time,
                            dateLabel = "10:00 AM",
                            contextLabel = "Equipment & Safety",
                            canvasContext = CanvasContext.defaultCanvasContext(),
                            itemType = ToDoItemType.CALENDAR_EVENT,
                            iconRes = R.drawable.ic_calendar,
                            isChecked = false
                        )
                    )
                )
            ),
            onOpenToDoItem = {}
        )
    }
}

@Preview(name = "Empty Light Mode", showBackground = true)
@Composable
fun ToDoListScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme {
        ToDoListContent(
            uiState = ToDoListUiState(),
            onOpenToDoItem = {}
        )
    }
}
