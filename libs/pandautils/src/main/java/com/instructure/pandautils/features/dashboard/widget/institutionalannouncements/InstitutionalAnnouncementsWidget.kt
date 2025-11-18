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
package com.instructure.pandautils.features.dashboard.widget.institutionalannouncements

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstitutionalAnnouncementsWidget(
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    onAnnouncementClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: InstitutionalAnnouncementsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            uiState.onRefresh()
        }
    }

    InstitutionalAnnouncementsContent(
        modifier = modifier,
        uiState = uiState,
        columns = columns,
        onAnnouncementClick = onAnnouncementClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstitutionalAnnouncementsContent(
    modifier: Modifier = Modifier,
    uiState: InstitutionalAnnouncementsUiState,
    columns: Int,
    onAnnouncementClick: (String, String) -> Unit
) {
    if (uiState.loading || uiState.error || uiState.announcements.isEmpty()) {
        return
    }

    val announcementPages = uiState.announcements.chunked(columns)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            text = stringResource(R.string.institutionalAnnouncementsTitle),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.textDarkest)
        )

        val pagerState = rememberPagerState(pageCount = { announcementPages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            beyondViewportPageCount = 1
        ) { page ->
            val announcementsInPage = announcementPages[page]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                announcementsInPage.forEach { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        onClick = { onAnnouncementClick(announcement.subject, announcement.message) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - announcementsInPage.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (announcementPages.size > 1) {
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
}

@Composable
private fun AnnouncementCard(
    announcement: InstitutionalAnnouncement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightestElevated)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(id = getIconResource(announcement.icon)),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(R.color.textDark)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = announcement.subject,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        color = colorResource(R.color.textDarkest),
                        maxLines = 2
                    )

                    announcement.startDate?.let { startDate ->
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = formatDate(startDate),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.textDark)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getIconResource(icon: String): Int {
    return when (icon) {
        AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.drawable.ic_warning
        AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> R.drawable.ic_warning
        AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR -> R.drawable.ic_calendar_month
        else -> R.drawable.ic_info
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF0F1316,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun InstitutionalAnnouncementsContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    InstitutionalAnnouncementsContent(
        uiState = InstitutionalAnnouncementsUiState(
            loading = false,
            error = false,
            announcements = listOf(
                InstitutionalAnnouncement(
                    id = 1,
                    subject = "System Maintenance This Weekend",
                    message = "Canvas will be offline for maintenance...",
                    institutionName = "University",
                    startDate = Date(),
                    icon = AccountNotification.ACCOUNT_NOTIFICATION_WARNING
                ),
                InstitutionalAnnouncement(
                    id = 2,
                    subject = "New Feature Release",
                    message = "We're excited to announce...",
                    institutionName = "University",
                    startDate = Date(),
                    icon = AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR
                )
            )
        ),
        columns = 1,
        onAnnouncementClick = { _, _ -> }
    )
}