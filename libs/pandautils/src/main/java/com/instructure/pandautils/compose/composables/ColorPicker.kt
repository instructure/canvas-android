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

package com.instructure.pandautils.compose.composables

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemedColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    label: String,
    selectedColor: Int,
    colors: List<ThemedColor>,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    // Find the matching ThemedColor and display the appropriate variant
    val selectedThemedColor =
        colors.find { (it.light and 0xFFFFFF) == (selectedColor and 0xFFFFFF) }
    val displaySelectedColor = if (selectedThemedColor != null) {
        if (isDarkTheme) selectedThemedColor.dark else selectedThemedColor.light
    } else {
        selectedColor or 0xFF000000.toInt()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp)
                .then(titleModifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                color = colorResource(R.color.textDarkest),
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outer box for border
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.borderLight)),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner box for color
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(displaySelectedColor))
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorResource(R.color.textDark),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.backgroundLight))
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    colors.forEach { themedColor ->
                        val displayColor = if (isDarkTheme) themedColor.dark else themedColor.light
                        ColorCircle(
                            displayColor = displayColor,
                            isSelected = (themedColor.light and 0xFFFFFF) == (selectedColor and 0xFFFFFF),
                            onClick = {
                                onColorSelected(themedColor.light)
                                isExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ColorCircle(
    displayColor: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer box for border
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.borderLight))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner box for color
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(displayColor or 0xFF000000.toInt())),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Preview(
    showBackground = true,
    backgroundColor = 0x1F2124,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WidgetColorPickerPreview() {
    var selectedColor by remember { mutableStateOf(0x2573DF) }
    val colors = listOf(
        ThemedColor(0xFFA31C73.toInt(), 0xFFA31C73.toInt()),
        ThemedColor(0xFFC54396.toInt(), 0xFFC54396.toInt()),
        ThemedColor(0xFF9E58BD.toInt(), 0xFF9E58BD.toInt()),
        ThemedColor(0xFF2573DF.toInt(), 0xFF2573DF.toInt()),
        ThemedColor(0xFF197EAB.toInt(), 0xFF197EAB.toInt()),
        ThemedColor(0xFF00828E.toInt(), 0xFF00828E.toInt()),
        ThemedColor(0xFF048660.toInt(), 0xFF048660.toInt()),
        ThemedColor(0xFF27872B.toInt(), 0xFF27872B.toInt()),
        ThemedColor(0xFF996E00.toInt(), 0xFF996E00.toInt()),
        ThemedColor(0xFFBF5811.toInt(), 0xFFBF5811.toInt()),
        ThemedColor(0xFFED0000.toInt(), 0xFFED0000.toInt()),
        ThemedColor(0xFF767676.toInt(), 0xFF767676.toInt()),
        ThemedColor(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()),
        ThemedColor(0xFF0F1316.toInt(), 0xFF0F1316.toInt())
    )

    ColorPicker(
        label = "Background color",
        selectedColor = selectedColor,
        colors = colors,
        onColorSelected = {
            selectedColor = it
        },
        titleModifier = Modifier.padding(horizontal = 16.dp)
    )
}