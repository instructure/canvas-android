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
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen

private const val ANIMATION_DURATION = 400

@Composable
fun ExpandableFloatingActionButton(
    icon: @Composable () -> Unit,
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    backgroundColor: Color = Color(color = ThemePrefs.buttonColor),
    contentColor: Color = Color(color = ThemePrefs.buttonTextColor),
    expandedItems: List<@Composable () -> Unit>
) {
    val rotationState by animateFloatAsState(targetValue = if (expanded.value) 45f else 0f, label = "fabRotation")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded.value,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = TweenSpec(ANIMATION_DURATION)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = TweenSpec(ANIMATION_DURATION)
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .animateContentSize()
        ) {
            LazyColumn(horizontalAlignment = Alignment.End) {
                items(expandedItems) {
                    it()
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        FloatingActionButton(
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            onClick = { expanded.value = !expanded.value },
            modifier = Modifier.rotate(rotationState)
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
        modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = labelTextColor,
            modifier = Modifier
                .background(
                    color = labelBackgroundColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(
                    color = iconBackgroundColor,
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
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
        expandedItems = listOf {
            ExpandableFabItem(icon = painterResource(id = R.drawable.ic_todo), text = "Add To Do")
            ExpandableFabItem(icon = painterResource(id = R.drawable.ic_calendar_month), text = "Add Event")
        }
    )
}