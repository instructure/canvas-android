/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseInvitationsWidget(
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: CourseInvitationsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            uiState.onRefresh()
        }
    }

    CourseInvitationsContent(
        modifier = modifier,
        uiState = uiState,
        columns = columns
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseInvitationsContent(
    modifier: Modifier = Modifier,
    uiState: CourseInvitationsUiState,
    columns: Int
) {
    if (uiState.loading || uiState.error || uiState.invitations.isEmpty()) {
        return
    }

    var invitationToDecline by remember { mutableStateOf<CourseInvitation?>(null) }

    val invitationPages = uiState.invitations.chunked(columns)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            text = stringResource(R.string.courseInvitationsTitle, uiState.invitations.size),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.textDarkest)
        )

        val pagerState = rememberPagerState(pageCount = { invitationPages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            beyondViewportPageCount = 1
        ) { page ->
            val invitationsInPage = invitationPages[page]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                invitationsInPage.forEach { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onAccept = { uiState.onAcceptInvitation(invitation) },
                        onDecline = { invitationToDecline = invitation },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add empty spaces to maintain card width when there are fewer cards than columns
                repeat(columns - invitationsInPage.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (invitationPages.size > 1) {
            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                activeColor = colorResource(R.color.backgroundDarkest),
                inactiveColor = colorResource(R.color.backgroundDarkest).copy(alpha = 0.4f)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    invitationToDecline?.let { invitation ->
        AlertDialog(
            onDismissRequest = { invitationToDecline = null },
            title = {
                Text(
                    text = stringResource(R.string.courseInvitationDeclineConfirmTitle),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest)
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.courseInvitationDeclineConfirmMessage,
                        invitation.courseName
                    ),
                    fontSize = 16.sp,
                    color = colorResource(R.color.textDark)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.onDeclineInvitation(invitation)
                        invitationToDecline = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.declineCourseInvitation),
                        color = colorResource(R.color.textDanger)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { invitationToDecline = null }) {
                    Text(
                        text = stringResource(android.R.string.cancel),
                        color = colorResource(R.color.textDark)
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            containerColor = colorResource(R.color.backgroundLightest)
        )
    }
}

@Composable
private fun InvitationCard(
    invitation: CourseInvitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightestElevated)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                text = invitation.courseName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(R.color.textDarkest),
                maxLines = 2
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accept button
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ThemePrefs.brandColor),
                        contentColor = colorResource(R.color.textLightest)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.acceptCourseInvitation),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // Decline button
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colorResource(R.color.backgroundLightest),
                        contentColor = colorResource(R.color.textDarkest)
                    ),
                    border = BorderStroke(
                        0.5.dp,
                        colorResource(R.color.borderMedium)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.declineCourseInvitation),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF0F1316,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CourseInvitationsContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseInvitationsContent(
        uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = listOf(
                CourseInvitation(
                    enrollmentId = 1,
                    courseId = 1,
                    courseName = "Introduction to Computer Science",
                    userId = 1
                ),
                CourseInvitation(
                    enrollmentId = 2,
                    courseId = 2,
                    courseName = "Advanced Mathematics",
                    userId = 1
                ),
                CourseInvitation(
                    enrollmentId = 3,
                    courseId = 3,
                    courseName = "Art History 101",
                    userId = 1
                )
            )
        ),
        columns = 1
    )
}