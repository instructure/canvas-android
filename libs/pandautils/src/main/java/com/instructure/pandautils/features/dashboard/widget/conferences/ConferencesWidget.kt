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

package com.instructure.pandautils.features.dashboard.widget.conferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ConferencesWidget(
    refreshSignal: SharedFlow<Unit>,
    columns: Int,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ConferencesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            uiState.onRefresh()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarMessage ->
            onShowSnackbar(snackbarMessage.message, snackbarMessage.actionLabel, snackbarMessage.action)
            uiState.onClearSnackbar()
        }
    }

    ConferencesWidgetContent(
        modifier = modifier,
        uiState = uiState,
        columns = columns
    )
}

@Composable
fun ConferencesWidgetContent(
    modifier: Modifier = Modifier,
    uiState: ConferencesUiState,
    columns: Int
) {
    if (uiState.loading || uiState.error || uiState.conferences.isEmpty()) {
        return
    }

    val activity = LocalActivity.current as? FragmentActivity ?: return
    val conferencePages = uiState.conferences.chunked(columns)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            text = pluralStringResource(R.plurals.conferencesWidgetTitle, uiState.conferences.size, uiState.conferences.size),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.textDarkest)
        )

        val pagerState = rememberPagerState(pageCount = { conferencePages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            beyondViewportPageCount = 1
        ) { page ->
            val conferencesInPage = conferencePages[page]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conferencesInPage.forEach { conference ->
                    ConferenceCard(
                        conference = conference,
                        isJoining = uiState.joiningConferenceId == conference.id,
                        onJoin = { uiState.onJoinConference(activity, conference) },
                        onDismiss = { uiState.onDismissConference(conference) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - conferencesInPage.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (conferencePages.size > 1) {
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
private fun ConferenceCard(
    conference: ConferenceItem,
    isJoining: Boolean,
    onJoin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isJoining, onClick = onJoin),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(colorResource(R.color.borderInfo))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(colorResource(R.color.backgroundInfo)),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info_solid),
                    contentDescription = null,
                    tint = colorResource(R.color.textLightest),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.conferencesWidgetConferenceInProgress),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.textDarkest),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = conference.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.textDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isJoining) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(R.color.backgroundDark)
                )
            } else {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.dismiss),
                        tint = colorResource(R.color.textDark)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    backgroundColor = 0x1F2124,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ConferencesWidgetContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    ConferencesWidgetContent(
        uiState = ConferencesUiState(
            loading = false,
            error = false,
            conferences = listOf(
                ConferenceItem(
                    id = 1,
                    subtitle = "Biology 101",
                    joinUrl = "https://example.com/join",
                    canvasContext = Course(id = 1, name = "Biology 101")
                ),
                ConferenceItem(
                    id = 2,
                    subtitle = "Chemistry 201",
                    joinUrl = "https://example.com/join",
                    canvasContext = Course(id = 2, name = "Chemistry 201")
                ),
                ConferenceItem(
                    id = 3,
                    subtitle = "Physics 301",
                    joinUrl = "https://example.com/join",
                    canvasContext = Course(id = 3, name = "Physics 301")
                )
            )
        ),
        columns = 1
    )
}