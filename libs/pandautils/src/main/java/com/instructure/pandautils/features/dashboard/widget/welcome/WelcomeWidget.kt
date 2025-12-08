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

package com.instructure.pandautils.features.dashboard.widget.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.pandautils.R
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun WelcomeWidget(
    refreshSignal: SharedFlow<Unit>,
    modifier: Modifier = Modifier
) {
    val viewModel: WelcomeWidgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshSignal) {
        refreshSignal.collect {
            viewModel.refresh()
        }
    }

    WelcomeContent(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
private fun WelcomeContent(
    modifier: Modifier = Modifier,
    uiState: WelcomeWidgetUiState
) {
    val contentDescriptionText = stringResource(
        R.string.welcomeWidgetContentDescription,
        uiState.greeting,
        uiState.message
    )

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = contentDescriptionText }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = uiState.greeting,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest),
            lineHeight = 29.sp
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            text = uiState.message,
            fontSize = 14.sp,
            color = colorResource(R.color.textDarkest),
            lineHeight = 19.sp
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeContentPreview() {
    WelcomeContent(
        uiState = WelcomeWidgetUiState(
            greeting = "Good morning, Riley!",
            message = "Every small step you take is progress. Keep going!"
        )
    )
}
