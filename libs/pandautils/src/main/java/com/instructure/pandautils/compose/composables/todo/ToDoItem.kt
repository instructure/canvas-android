/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables.todo

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.courseOrUserColor
import com.instructure.pandautils.utils.performGestureHapticFeedback
import com.instructure.pandautils.utils.performToggleHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_DP = 150

@Composable
fun ToDoItem(
    item: ToDoItemUiState,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dateBadge: @Composable (() -> Unit)? = null
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
            .testTag("todoItem_${item.id}")
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
            onCheckedChange = onCheckedChange,
            onClick = onClick,
            modifier = Modifier.offset { IntOffset(animatedOffsetX.value.roundToInt(), 0) },
            dateBadge = dateBadge
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
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dateBadge: @Composable (() -> Unit)? = null
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.backgroundLightest))
            .clickable(enabled = item.isClickable, onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        dateBadge?.let { it() }

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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("todoItemTitle")
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
                ),
                modifier = Modifier.testTag("todoCheckbox_${item.id}")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "1",
            title = "Complete Assignment: Introduction to Algorithms",
            date = java.util.Date(),
            dateLabel = "Due Today at 11:59 PM",
            contextLabel = "Introduction to Computer Science",
            canvasContext = CanvasContext.emptyCourseContext(1),
            itemType = ToDoItemType.ASSIGNMENT,
            isChecked = false,
            iconRes = R.drawable.ic_assignment,
            tag = "100 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemCheckedPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "2",
            title = "Submit Lab Report",
            date = java.util.Date(),
            dateLabel = "Due Yesterday at 11:59 PM",
            contextLabel = "Physics 101",
            canvasContext = CanvasContext.emptyCourseContext(2),
            itemType = ToDoItemType.ASSIGNMENT,
            isChecked = true,
            iconRes = R.drawable.ic_assignment,
            tag = "50 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemQuizPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "3",
            title = "Chapter 5 Quiz: Data Structures",
            date = java.util.Date(),
            dateLabel = "Due Tomorrow at 3:00 PM",
            contextLabel = "Advanced Programming",
            canvasContext = CanvasContext.emptyCourseContext(3),
            itemType = ToDoItemType.QUIZ,
            isChecked = false,
            iconRes = R.drawable.ic_quiz,
            tag = "25 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemDiscussionPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "4",
            title = "Weekly Discussion: Climate Change Impact",
            date = java.util.Date(),
            dateLabel = "Due in 2 days at 11:59 PM",
            contextLabel = "Environmental Science",
            canvasContext = CanvasContext.emptyCourseContext(4),
            itemType = ToDoItemType.DISCUSSION,
            isChecked = false,
            iconRes = R.drawable.ic_discussion,
            tag = "10 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemCalendarEventPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "5",
            title = "Office Hours with Professor Smith",
            date = java.util.Date(),
            dateLabel = "Today at 2:00 PM - 4:00 PM",
            contextLabel = "Mathematics",
            canvasContext = CanvasContext.emptyCourseContext(5),
            itemType = ToDoItemType.CALENDAR_EVENT,
            isChecked = false,
            iconRes = R.drawable.ic_calendar,
            tag = null
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemPlannerNotePreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "6",
            title = "Review lecture notes and prepare for midterm exam",
            date = java.util.Date(),
            dateLabel = "Today at 6:00 PM",
            contextLabel = "Personal To Do",
            canvasContext = CanvasContext.emptyCourseContext(6),
            itemType = ToDoItemType.PLANNER_NOTE,
            isChecked = false,
            iconRes = R.drawable.ic_todo,
            tag = null
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ToDoItemLongTitlePreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "7",
            title = "Complete the comprehensive research paper on the effects of social media on mental health in adolescents including literature review and data analysis",
            date = java.util.Date(),
            dateLabel = "Due in 3 days at 11:59 PM",
            contextLabel = "Psychology Research Methods",
            canvasContext = CanvasContext.emptyCourseContext(7),
            itemType = ToDoItemType.ASSIGNMENT,
            isChecked = false,
            iconRes = R.drawable.ic_assignment,
            tag = "200 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun ToDoItemDarkModePreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context

    ToDoItem(
        item = ToDoItemUiState(
            id = "8",
            title = "Complete Assignment: Web Development Project",
            date = java.util.Date(),
            dateLabel = "Due Tomorrow at 11:59 PM",
            contextLabel = "Full Stack Development",
            canvasContext = CanvasContext.emptyCourseContext(8),
            itemType = ToDoItemType.ASSIGNMENT,
            isChecked = false,
            iconRes = R.drawable.ic_assignment,
            tag = "150 pts"
        ),
        onCheckedChange = {},
        onClick = {}
    )
}