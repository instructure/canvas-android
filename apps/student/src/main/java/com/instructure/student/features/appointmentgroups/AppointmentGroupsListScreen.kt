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
@file:OptIn(ExperimentalMaterialApi::class)

package com.instructure.student.features.appointmentgroups

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent

@Composable
fun AppointmentGroupsListScreen(
    title: String,
    uiState: AppointmentGroupsListUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courseColor = LocalCourseColor.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                backgroundColor = courseColor,
                contentColor = colorResource(id = R.color.white)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.isError -> {
                    ErrorContent(
                        errorMessage = stringResource(id = R.string.errorOccurred),
                        retryClick = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.groups.isEmpty() -> {
                    EmptyContent(
                        emptyTitle = stringResource(id = R.string.noAppointmentGroups),
                        emptyMessage = stringResource(id = R.string.noAppointmentGroupsMessage),
                        imageRes = R.drawable.ic_panda_noannouncements,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    AppointmentGroupsList(
                        groups = uiState.groups,
                        onReserveSlot = uiState.onReserveSlot,
                        onCancelReservation = uiState.onCancelReservation,
                        onToggleGroupExpansion = uiState.onToggleGroupExpansion
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun AppointmentGroupsList(
    groups: List<AppointmentGroupUiState>,
    onReserveSlot: (Long, String?) -> Unit,
    onCancelReservation: (Long) -> Unit,
    onToggleGroupExpansion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize().background(colorResource(R.color.backgroundLightest))) {
        items(groups, key = { it.id }) { group ->
            AppointmentGroupCard(
                group = group,
                onReserveSlot = onReserveSlot,
                onCancelReservation = onCancelReservation,
                onToggleGroupExpansion = onToggleGroupExpansion,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun AppointmentGroupCard(
    group: AppointmentGroupUiState,
    onReserveSlot: (Long, String?) -> Unit,
    onCancelReservation: (Long) -> Unit,
    onToggleGroupExpansion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val reservedSlots = group.slots.filter { it.isReservedByMe }
    val availableSlots = group.slots.filter { !it.isReservedByMe }

    val rotationState by animateFloatAsState(
        targetValue = if (group.isExpanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300
        ),
        label = "arrow_rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = colorResource(R.color.backgroundLightest)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onToggleGroupExpansion(group.id)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.title,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.textDarkest)
                    )

                    if (!group.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = group.description,
                            color = colorResource(id = R.color.textDark)
                        )
                    }

                    if (!group.locationName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = group.locationName,
                            color = colorResource(id = R.color.textDarkest)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (group.isExpanded)
                        stringResource(id = R.string.collapse)
                    else
                        stringResource(id = R.string.expand),
                    tint = colorResource(id = R.color.textDark),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState)
                )
            }

            if (reservedSlots.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.myReservations),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.textDarkest)
                )
                reservedSlots.forEach { slot ->
                    AppointmentSlotItem(
                        slot = slot,
                        canReserveMore = group.canReserveMore,
                        onReserveSlot = onReserveSlot,
                        onCancelReservation = onCancelReservation,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            if (group.isExpanded && availableSlots.isNotEmpty()) {
                if (reservedSlots.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = colorResource(id = R.color.borderMedium))
                }
                Text(
                    text = stringResource(id = R.string.availableSlots),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.padding(16.dp),
                )
                availableSlots.forEach { slot ->
                    AppointmentSlotItem(
                        slot = slot,
                        canReserveMore = group.canReserveMore,
                        onReserveSlot = onReserveSlot,
                        onCancelReservation = onCancelReservation,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AppointmentSlotItem(
    slot: AppointmentSlotUiState,
    canReserveMore: Boolean,
    onReserveSlot: (Long, String?) -> Unit,
    onCancelReservation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReserveDialog by remember { mutableStateOf(false) }
    val courseColor = LocalCourseColor.current

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 1.dp,
        backgroundColor = colorResource(id = R.color.backgroundLightestElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = (slot.isAvailable && canReserveMore) || slot.isReservedByMe) {
                    if (slot.isReservedByMe) {
                        slot.myReservationId?.let { reservationId ->
                            onCancelReservation(reservationId)
                        }
                    } else if (slot.isAvailable && canReserveMore) {
                        showReserveDialog = true
                    }
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.timeRange,
                    style = MaterialTheme.typography.body1,
                    color = colorResource(id = R.color.textDarkest)
                )

                if (slot.isReservedByMe) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorResource(id = R.color.textSuccess),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.reservedByYou),
                            style = MaterialTheme.typography.caption,
                            color = colorResource(id = R.color.textSuccess)
                        )
                    }
                }

                if (slot.hasConflict && !slot.isReservedByMe) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colorResource(id = R.color.textWarning),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                id = R.string.conflictWithEvent,
                                slot.conflictEventTitle ?: ""
                            ),
                            style = MaterialTheme.typography.caption,
                            color = colorResource(id = R.color.textWarning)
                        )
                    }
                }

                if (!slot.isAvailable && !slot.isReservedByMe) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.noSlotsAvailable),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (slot.isReservedByMe) {
                TextButton(onClick = {
                    slot.myReservationId?.let { reservationId ->
                        onCancelReservation(reservationId)
                    }
                }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = courseColor
                    )
                }
            } else if (slot.isAvailable) {
                TextButton(
                    onClick = { showReserveDialog = true },
                    enabled = canReserveMore
                ) {
                    Text(
                        text = stringResource(id = R.string.reserve),
                        color = if (canReserveMore) courseColor else colorResource(id = R.color.textDark).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showReserveDialog) {
        ReserveSlotDialog(
            slot = slot,
            courseColor = courseColor,
            onConfirm = { comments ->
                onReserveSlot(slot.id, comments)
                showReserveDialog = false
            },
            onDismiss = { showReserveDialog = false }
        )
    }
}

@Composable
fun ReserveSlotDialog(
    slot: AppointmentSlotUiState,
    courseColor: Color,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        backgroundColor = colorResource(R.color.backgroundLightestElevated),
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.reserveAppointment))
        },
        text = {
            Column {
                Text(text = stringResource(id = R.string.reserveAppointmentMessage, slot.timeRange))
                if (slot.hasConflict) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colorResource(id = R.color.textWarning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                id = R.string.conflictWarning,
                                slot.conflictEventTitle ?: ""
                            ),
                            style = MaterialTheme.typography.caption,
                            color = colorResource(id = R.color.textWarning)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(null) }) {
                Text(
                    text = stringResource(id = R.string.reserve),
                    color = courseColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = android.R.string.cancel),
                    color = courseColor
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppointmentGroupsListScreenPreview() {
    val sampleState = AppointmentGroupsListUiState(
        isLoading = false,
        groups = listOf(
            AppointmentGroupUiState(
                id = 1,
                title = "Office Hours",
                description = "Weekly office hours with Professor Smith",
                locationName = "Room 301",
                locationAddress = null,
                participantCount = 5,
                maxAppointmentsPerParticipant = 2,
                currentReservationCount = 1,
                canReserveMore = true,
                isExpanded = true,
                slots = listOf(
                    AppointmentSlotUiState(
                        id = 1,
                        timeRange = "Oct 31, 2:00 PM - 3:00 PM",
                        availableSlots = 3,
                        isAvailable = true,
                        isReservedByMe = false,
                        myReservationId = null,
                        hasConflict = false,
                        conflictEventTitle = null
                    ),
                    AppointmentSlotUiState(
                        id = 2,
                        timeRange = "Nov 1, 10:00 AM - 11:00 AM",
                        availableSlots = 1,
                        isAvailable = false,
                        isReservedByMe = true,
                        myReservationId = 123,
                        hasConflict = false,
                        conflictEventTitle = null
                    ),
                    AppointmentSlotUiState(
                        id = 3,
                        timeRange = "Nov 2, 3:00 PM - 4:00 PM",
                        availableSlots = 2,
                        isAvailable = true,
                        isReservedByMe = false,
                        myReservationId = null,
                        hasConflict = true,
                        conflictEventTitle = "Math Assignment"
                    )
                )
            )
        )
    )

    AppointmentGroupsListScreen(
        title = "Appointments",
        uiState = sampleState,
        onRefresh = {},
        onBack = {}
    )
}
