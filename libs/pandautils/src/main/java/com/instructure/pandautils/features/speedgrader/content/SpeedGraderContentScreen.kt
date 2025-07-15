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
package com.instructure.pandautils.features.speedgrader.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.compose.AndroidFragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.features.speedgrader.SpeedGraderSharedViewModel
import com.instructure.pandautils.utils.drawableId
import com.instructure.pandautils.utils.getFragmentActivity
import dagger.hilt.android.EarlyEntryPoints
import java.util.Date

@Composable
fun SpeedGraderContentScreen(
    expanded: Boolean,
    onExpandClick: (() -> Unit)?
) {
    val activity = LocalContext.current.getFragmentActivity()
    val speedGraderSharedViewModel: SpeedGraderSharedViewModel = viewModel(viewModelStoreOwner = activity)

    val viewModel: SpeedGraderContentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current.applicationContext

    val router: SpeedGraderContentRouter by lazy {
        EarlyEntryPoints.get(context, SpeedGraderContentRouterEntryPoint::class.java)
            .speedGraderContentRouter()
    }

    SpeedGraderContentScreen(
        uiState = uiState,
        router = router,
        expanded = expanded,
        onExpandClick = onExpandClick,
        toggleViewPager = speedGraderSharedViewModel::enableViewPager
    )
}

@Composable
private fun SpeedGraderContentScreen(
    uiState: SpeedGraderContentUiState,
    router: SpeedGraderContentRouter,
    expanded: Boolean,
    onExpandClick: (() -> Unit)?,
    toggleViewPager: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = colorResource(id = R.color.backgroundLightest),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.padding(it)) {
            UserHeader(
                userUrl = uiState.userUrl,
                userName = uiState.userName,
                submissionStatus = uiState.submissionState,
                dueDate = uiState.dueDate,
                expanded = expanded,
                onExpandClick = onExpandClick,
                courseColor = LocalCourseColor.current
            )
            CanvasDivider()
            if (uiState.attemptSelectorUiState.items.size > 1 || uiState.attachmentSelectorUiState.items.isNotEmpty()) {
                SelectorContent(
                    attemptSelectorUiState = uiState.attemptSelectorUiState,
                    attachmentSelectorUiState = uiState.attachmentSelectorUiState,
                    courseColor = LocalCourseColor.current
                )
                CanvasDivider()
            }
            uiState.content?.let { content ->
                key(content) {
                    val route = router.getRouteForContent(content)
                    AndroidFragment(
                        clazz = route.clazz,
                        arguments = route.bundle,
                        modifier = Modifier
                            .fillMaxSize()
                            .conditional(content is PdfContent) {
                                pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (
                                                event.changes.any { pointerInputChange ->
                                                    pointerInputChange.pressed
                                                }
                                            ) {
                                                toggleViewPager(false)
                                                do {
                                                    val moveEvent = awaitPointerEvent()
                                                } while (
                                                    moveEvent.changes.any { pointerInputChange ->
                                                        pointerInputChange.pressed
                                                    }
                                                )
                                                toggleViewPager(true)
                                            }
                                        }
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserHeader(
    userUrl: String?,
    userName: String?,
    submissionStatus: SubmissionStateLabel,
    dueDate: Date?,
    expanded: Boolean,
    onExpandClick: (() -> Unit)?,
    courseColor: Color,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val horizontal = windowClass != WindowWidthSizeClass.COMPACT

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .requiredHeight(64.dp)
            .fillMaxWidth()
            .padding(start = 22.dp, top = 12.dp, bottom = 12.dp, end = 22.dp)
    ) {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            imageUrl = userUrl,
            name = userName.orEmpty(),
        )
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = userName.orEmpty(),
                color = colorResource(id = R.color.textDarkest),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            if (submissionStatus != SubmissionStateLabel.NONE) {
                Row(verticalAlignment = Alignment.Bottom) {
                    SubmissionStatus(
                        submissionStatus = submissionStatus,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                    if (dueDate != null) {
                        VerticalDivider(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(16.dp),
                            color = colorResource(id = R.color.borderMedium),
                        )
                        Text(
                            DateHelper.getDateAtTimeString(
                                LocalContext.current,
                                R.string.due_dateTime,
                                dueDate
                            ).orEmpty(),
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.textDark),
                        )
                    }
                }
            }
        }
        if (horizontal) {
            var expandedState by remember { mutableStateOf(expanded) }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    expandedState = !expandedState
                    onExpandClick?.invoke()
                },
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (expandedState) R.drawable.ic_collapse_bottomsheet else R.drawable.ic_expand_bottomsheet),
                    contentDescription = stringResource(if (expandedState) R.string.content_description_collapse_content else R.string.content_description_expand_content),
                    tint = courseColor,
                )
            }
        }
    }
}

@Composable
private fun SubmissionStatus(
    submissionStatus: SubmissionStateLabel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            painter = painterResource(id = submissionStatus.iconRes),
            contentDescription = null,
            tint = colorResource(id = submissionStatus.colorRes),
            modifier = Modifier
                .size(16.dp)
                .semantics {
                    drawableId = submissionStatus.iconRes
                }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(id = submissionStatus.labelRes),
            color = colorResource(id = submissionStatus.colorRes),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SelectorContent(
    attemptSelectorUiState: SelectorUiState,
    attachmentSelectorUiState: SelectorUiState,
    courseColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val showAttemptSelector = attemptSelectorUiState.items.size > 1
        val showAttachmentSelector = attachmentSelectorUiState.items.isNotEmpty()
        if (showAttemptSelector) {
            Selector(
                selectorUiState = attemptSelectorUiState,
                color = courseColor,
                showBadge = false,
                modifier = Modifier.weight(1f)
            )
        }
        if (showAttemptSelector && showAttachmentSelector) {
            VerticalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        if (showAttachmentSelector) {
            Selector(
                selectorUiState = attachmentSelectorUiState,
                color = courseColor,
                showBadge = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Selector(
    selectorUiState: SelectorUiState,
    color: Color,
    showBadge: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = true
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(18.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_reset_history),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-6).dp)
                        .size(16.dp)
                        .border(1.dp, colorResource(R.color.textLightest), CircleShape)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectorUiState.items.size.toString(),
                        color = colorResource(id = R.color.textLightest),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(if (showBadge) 18.dp else 8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = selectorUiState.items.find { it.id == selectorUiState.selectedItemId }?.title.orEmpty(),
                color = color,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = color
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            expanded = false
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLightest))
    ) {
        selectorUiState.items.forEach { item ->
            DropdownMenuItem(
                onClick = {
                    if (item.id != selectorUiState.selectedItemId) {
                        selectorUiState.onItemSelected(item.id)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    expanded = false
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                color = colorResource(R.color.textDarkest),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            item.subtitle?.let {
                                Text(
                                    text = it,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.textDark),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (item.id == selectorUiState.selectedItemId) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_checkmark_lined),
                                contentDescription = null,
                                tint = colorResource(R.color.textDarkest),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserHeaderPreview() {
    UserHeader(
        userUrl = null,
        userName = "John Doe",
        dueDate = Date(),
        submissionStatus = SubmissionStateLabel.GRADED,
        expanded = false,
        onExpandClick = null,
        courseColor = Color(color = android.graphics.Color.BLUE)
    )
}

@Preview(showBackground = true)
@Composable
fun SelectorContentPreview() {
    SelectorContent(
        attemptSelectorUiState = SelectorUiState(
            items = listOf(
                SelectorItem(1, "Attempt 1"),
                SelectorItem(2, "Attempt 2")
            ),
            selectedItemId = 1
        ),
        attachmentSelectorUiState = SelectorUiState(
            items = listOf(
                SelectorItem(1, "Attachment 1"),
                SelectorItem(2, "Attachment 2"),
                SelectorItem(3, "Attachment 3")
            ),
            selectedItemId = 1
        ),
        courseColor = Color(color = android.graphics.Color.BLUE)
    )
}

@Preview(showBackground = true)
@Composable
private fun SpeedGraderContentScreenPreview() {
    val uiState = SpeedGraderContentUiState(
        userUrl = null,
        userName = "John Doe",
        submissionState = SubmissionStateLabel.GRADED,
        dueDate = Date(),
        attachmentSelectorUiState = SelectorUiState(
            items = listOf(
                SelectorItem(1, "Item 1"),
                SelectorItem(2, "Item 2"),
                SelectorItem(3, "Item 3")
            ),
            selectedItemId = 2
        )
    )

    SpeedGraderContentScreen(
        uiState = uiState,
        router = object : SpeedGraderContentRouter {
            override fun getRouteForContent(content: GradeableContent): SpeedGraderContentRoute {
                throw NotImplementedError("No need for Preview")
            }
        },
        expanded = false,
        onExpandClick = {},
        toggleViewPager = {}
    )
}
