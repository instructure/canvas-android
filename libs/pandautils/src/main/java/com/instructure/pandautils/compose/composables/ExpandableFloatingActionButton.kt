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

package com.instructure.pandautils.compose.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen


@Composable
fun ExpandableFloatingActionButton(
    icon: @Composable () -> Unit,
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    backgroundColor: Color = Color(color = ThemePrefs.buttonColor),
    contentColor: Color = Color(color = ThemePrefs.buttonTextColor),
    expandedItems: List<@Composable ColumnScope.() -> Unit>
) {
    val rotationState by animateFloatAsState(targetValue = if (expanded.value) 45f else 0f, label = "fabRotation")

    Column(
        modifier = modifier.semantics { isTraversalGroup = true },
        horizontalAlignment = Alignment.End
    ) {
        expandedItems.forEach {
            AnimatedVisibility(
                visible = expanded.value,
                enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(.80f, .5f)),
                exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(.80f, .5f)),
                modifier = Modifier.animateContentSize()
            ) {
                it()
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        val labelRes = if (expanded.value) R.string.a11y_calendarCloseAddActions else R.string.a11y_calendarOpenAddActions
        val onClickLabel = stringResource(labelRes)

        val announceRes = if (expanded.value) R.string.a11y_calendarActionsClosed else R.string.a11y_calendarActionsOpen
        val onClickAnnounce = stringResource(announceRes)
        val localView = LocalView.current
        FloatingActionButton(
            containerColor = backgroundColor,
            contentColor = contentColor,
            onClick = { expanded.value = !expanded.value },
            modifier = Modifier
                .size(56.dp)
                .rotate(rotationState)
                .semantics {
                    traversalIndex = 0f
                    onClick(label = onClickLabel) {
                        localView.announceForAccessibility(onClickAnnounce)
                        expanded.value = !expanded.value
                        true
                    }
                }
        ) {
            icon()
        }
    }
}

@Composable
fun ExpandableFabItem(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    labelBackgroundColor: Color = colorResource(id = R.color.backgroundLight),
    labelTextColor: Color = colorResource(id = R.color.textDarkest),
    iconBackgroundColor: Color = Color(ThemePrefs.buttonColor),
    iconContentColor: Color = Color(ThemePrefs.buttonTextColor)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 9.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = labelBackgroundColor,
                    shape = RoundedCornerShape(4.dp)
                ),
        ) {
            Text(
                text = text,
                color = labelTextColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape
                )
                .background(
                    color = iconBackgroundColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconContentColor
            )
        }
    }
}

@Preview
@Composable
fun ExpandableFloatingActionButtonPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    ExpandableFloatingActionButton(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                tint = Color.White,
                contentDescription = null
            )
        },
        expanded = remember { mutableStateOf(false) },
        expandedItems = listOf(
            { ExpandableFabItem(icon = painterResource(id = R.drawable.ic_todo), text = "Add To Do") },
            { ExpandableFabItem(icon = painterResource(id = R.drawable.ic_calendar_month_24), text = "Add Event") }
        )
    )
}