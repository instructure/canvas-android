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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.utils.ThemedColor
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
            text = stringResource(R.string.institutionalAnnouncementsTitle, uiState.announcements.size),
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
                        color = Color(uiState.color.color()),
                        onDismiss = { uiState.onDismiss(announcement.id) },
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
                    .padding(top = 12.dp),
                activeColor = colorResource(R.color.backgroundDarkest),
                inactiveColor = colorResource(R.color.backgroundDarkest).copy(alpha = 0.4f)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AnnouncementCard(
    announcement: InstitutionalAnnouncement,
    onClick: () -> Unit,
    color: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box {
                if (announcement.logoUrl.isNotEmpty()) {
                    GlideImage(
                        model = announcement.logoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Inside
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = color,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {}
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(colorResource(R.color.backgroundLightest), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = getIconResource(announcement.icon)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Center),
                        tint = colorResource(
                            when (announcement.icon) {
                                AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.color.textWarning
                                AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> R.color.textDanger
                                else -> R.color.textInfo
                            }
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_globalAnnouncement),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                announcement.startDate?.let { startDate ->
                    Text(
                        text = formatDateTime(startDate),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.textDark),
                        maxLines = 1
                    )
                }

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = announcement.subject,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(R.color.textDarkest),
                    maxLines = 2
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onDismiss),
                tint = colorResource(R.color.textDark)
            )
        }
    }
}

@Composable
private fun getIconResource(icon: String): Int {
    return when (icon) {
        AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.drawable.ic_warning_solid
        AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> R.drawable.ic_warning_solid
        AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR -> R.drawable.ic_calendar_solid
        AccountNotification.ACCOUNT_NOTIFICATION_QUESTION -> R.drawable.ic_question_solid
        else -> R.drawable.ic_info_solid
    }
}

private fun formatDateTime(date: Date): String {
    val formatter = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    backgroundColor = 0x1F2124,
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
                    subject = "Back to School Ceremony Dress Code",
                    message = "Canvas will be offline for maintenance...",
                    startDate = Date(),
                    icon = AccountNotification.ACCOUNT_NOTIFICATION_WARNING,
                    logoUrl = ""
                ),
                InstitutionalAnnouncement(
                    id = 2,
                    subject = "New Feature Release",
                    message = "We're excited to announce...",
                    startDate = Date(),
                    icon = AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR,
                    logoUrl = ""
                )
            ),
            color = ThemedColor(GlobalConfig.DEFAULT_COLOR)
        ),
        columns = 1,
        onAnnouncementClick = { _, _ -> }
    )
}