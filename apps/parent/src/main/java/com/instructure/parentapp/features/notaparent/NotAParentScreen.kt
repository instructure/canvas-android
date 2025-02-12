/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.notaparent

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.parentapp.R


@Composable
internal fun NotAParentScreen(
    returnToLoginClick: () -> Unit,
    onStudentClick: () -> Unit,
    onTeacherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = colorResource(id = R.color.backgroundLightest),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        var bottomExpanded by remember { mutableStateOf(false) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            EmptyContent(
                emptyTitle = stringResource(id = R.string.notAParentTitle),
                emptyMessage = stringResource(id = R.string.notAParentSubtitle),
                imageRes = R.drawable.ic_panda_book,
                buttonText = stringResource(id = R.string.returnToLogin),
                buttonClick = returnToLoginClick
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            if (bottomExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    color = colorResource(id = R.color.backgroundMedium),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = stringResource(id = R.string.studentOrTeacherTitle),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 12.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    bottomExpanded = !bottomExpanded
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppOptions(bottomExpanded, onStudentClick, onTeacherClick)
        }
    }
}

@Composable
private fun AppOptions(
    bottomExpanded: Boolean,
    onStudentClick: () -> Unit,
    onTeacherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .animateContentSize()
            .height(if (bottomExpanded) 180.dp else 0.dp)
    ) {
        Text(
            text = stringResource(id = R.string.studentOrTeacherSubtitle),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppOption(
            stringResource(id = R.string.studentApp),
            stringResource(id = R.string.canvasStudentApp),
            colorResource(id = R.color.login_studentAppTheme),
            R.drawable.ic_canvas_logo_student,
            { onStudentClick() }
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppOption(
            stringResource(id = R.string.teacherApp),
            stringResource(id = R.string.canvasTeacherApp),
            colorResource(id = R.color.login_teacherAppTheme),
            R.drawable.ic_canvas_logo_teacher,
            { onTeacherClick() }
        )
    }
}

@Composable
private fun AppOption(
    name: String,
    label: String,
    color: Color,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = label
            }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            tint = color,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Icon(
                painter = painterResource(id = R.drawable.ic_canvas_wordmark),
                tint = colorResource(id = R.color.backgroundMedium),
                contentDescription = null,
                modifier = Modifier.height(24.dp)
            )
            Text(
                text = name,
                color = color,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun NotAParentScreenPreview() {
    NotAParentScreen(
        returnToLoginClick = {},
        onStudentClick = {},
        onTeacherClick = {}
    )
}