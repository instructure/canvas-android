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

package com.instructure.pandautils.features.speedgrader.details.studentnotes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.utils.ScreenState


@Composable
internal fun StudentNotes(
    modifier: Modifier = Modifier,
    studentHolderViewModel: StudentNotesViewModel = hiltViewModel()
) {
    val uiState by studentHolderViewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.state != ScreenState.Empty) {
        StudentNotes(uiState, modifier)
    }
}

@Composable
private fun StudentNotes(
    uiState: StudentNotesUiState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = if (expanded) 16.dp else 0.dp)
    ) {
        GroupHeader(
            name = stringResource(R.string.speedGraderStudentNotes),
            expanded = expanded,
            onClick = {
                expanded = !expanded
            }
        )

        if (expanded) {
            when (uiState.state) {
                ScreenState.Loading -> {
                    Loading()
                }

                ScreenState.Error -> {
                    Error(uiState.onRefresh)
                }

                ScreenState.Content -> {
                    uiState.studentNotes.forEach {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 2.dp,
                            backgroundColor = colorResource(R.color.backgroundLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = it.title,
                                    color = colorResource(id = R.color.textDarkest),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it.description,
                                    color = colorResource(id = R.color.textDarkest),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun Loading() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.speedGraderStudentNotesLoading),
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun Error(onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_panda_notsupported),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.speedGraderStudentNotesErrorTitle),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.speedGraderStudentNotesErrorDescription),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRefresh,
            border = BorderStroke(1.dp, colorResource(id = R.color.textInfo)),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.backgroundLightest)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh_lined),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colorResource(id = R.color.textInfo)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.speedGraderStudentNotesErrorButtonText),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textInfo),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentNotesLoadingPreview() {
    StudentNotes(
        uiState = StudentNotesUiState(
            state = ScreenState.Loading,
            studentNotes = emptyList()
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun StudentNotesErrorPreview() {
    StudentNotes(
        uiState = StudentNotesUiState(
            state = ScreenState.Error,
            studentNotes = emptyList()
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun StudentNotesPreview() {
    StudentNotes(
        uiState = StudentNotesUiState(
            state = ScreenState.Content,
            studentNotes = listOf(
                StudentNote("Note 1", "This is the first note"),
                StudentNote("Note 2", "This is the second note"),
                StudentNote("Note 3", "This is the third note")
            )
        )
    )
}
