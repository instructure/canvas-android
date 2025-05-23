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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.compose.AndroidFragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.drawableId
import dagger.hilt.android.EarlyEntryPoints
import java.util.Date

@Composable
fun SpeedGraderContentScreen(
    expanded: Boolean,
    onExpandClick: (() -> Unit)?
) {

    val viewModel: SpeedGraderContentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current.applicationContext

    val router: SpeedGraderContentRouter by lazy {
        EarlyEntryPoints.get(context, SpeedGraderContentRouterEntryPoint::class.java)
            .speedGraderContentRouter()
    }

    Column {
        UserHeader(
            uiState.userUrl,
            uiState.userName,
            uiState.submissionState,
            uiState.dueDate,
            expanded,
            onExpandClick
        )
        CanvasDivider()
        uiState.content?.let {
            val route = router.getRouteForContent(it)
            AndroidFragment(
                clazz = route.clazz,
                arguments = route.bundle,
                modifier = Modifier.fillMaxSize()
            )
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
    onExpandClick: (() -> Unit)?
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
                    tint = colorResource(id = R.color.textInfo),
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

@Preview
@Composable
fun UserHeaderPreview() {
    UserHeader(
        userUrl = null,
        userName = "John Doe",
        dueDate = Date(),
        submissionStatus = SubmissionStateLabel.GRADED,
        expanded = false,
        onExpandClick = null
    )
}