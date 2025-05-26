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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.drawableId
import dagger.hilt.android.EarlyEntryPoints
import java.util.Date

@Composable
fun SpeedGraderContentScreen() {
    val viewModel: SpeedGraderContentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current.applicationContext

    val router: SpeedGraderContentRouter by lazy {
        EarlyEntryPoints.get(context, SpeedGraderContentRouterEntryPoint::class.java).speedGraderContentRouter()
    }

    SpeedGraderContentScreen(uiState, router)
}

@Composable
private fun SpeedGraderContentScreen(
    uiState: SpeedGraderContentUiState,
    router: SpeedGraderContentRouter
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
                dueDate = uiState.dueDate
            )
            CanvasDivider()
            if (uiState.attachmentSelectorUiState.items.isNotEmpty()) {
                SelectorContent(
                    attemptSelectorUiState = SelectorUiState(),
                    attachmentSelectorUiState = uiState.attachmentSelectorUiState
                )
                CanvasDivider()
            }
            uiState.content?.let { content ->
                key(content) {
                    val route = router.getRouteForContent(content)
                    AndroidFragment(
                        clazz = route.clazz,
                        arguments = route.bundle,
                        modifier = Modifier.fillMaxSize()
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
    dueDate: Date?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
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
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        if (attemptSelectorUiState.items.isNotEmpty()) {
            Selector(
                selectorUiState = attemptSelectorUiState,
                modifier = Modifier.weight(1f)
            )
        }
        if (attemptSelectorUiState.items.isNotEmpty() && attachmentSelectorUiState.items.isNotEmpty()) {
            VerticalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        if (attachmentSelectorUiState.items.isNotEmpty()) {
            Selector(
                selectorUiState = attachmentSelectorUiState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Selector(
    selectorUiState: SelectorUiState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val color = Color(color = selectorUiState.color)

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
        Spacer(modifier = Modifier.width(18.dp))
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectorUiState.onItemSelected(item.id)
                    expanded = false
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            color = colorResource(R.color.textDarkest),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
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
        submissionStatus = SubmissionStateLabel.GRADED
    )
}

@Preview(showBackground = true)
@Composable
fun SelectorContentPreview() {
    SelectorContent(
        attemptSelectorUiState = SelectorUiState(
            items = listOf(
                SelectorItem(1, "Attempt 1")
            ),
            selectedItemId = 1,
            color = android.graphics.Color.BLUE
        ),
        attachmentSelectorUiState = SelectorUiState(
            items = listOf(
                SelectorItem(1, "Attachment 1"),
                SelectorItem(2, "Attachment 2"),
                SelectorItem(3, "Attachment 3")
            ),
            selectedItemId = 1,
            color = android.graphics.Color.BLUE
        )
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
            color = android.graphics.Color.RED,
            items = listOf(
                SelectorItem(1, "Item 1"),
                SelectorItem(2, "Item 2"),
                SelectorItem(3, "Item 3")
            ),
            selectedItemId = 2
        )
    )

    SpeedGraderContentScreen(uiState = uiState, router = object : SpeedGraderContentRouter {
        override fun getRouteForContent(content: GradeableContent): SpeedGraderContentRoute {
            throw NotImplementedError("No need for Preview")
        }
    })
}
