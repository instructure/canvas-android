/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.horizon.horizonui.organisms.controls

import android.annotation.SuppressLint
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.utils.toPx

private const val ANIMATION_DURATION = 250

data class SwitchItemState(
    val controlsContentState: ControlsContentState,
    val checked: Boolean,
    val onCheckedChanged: ((Boolean) -> Unit)? = null,
    val enabled: Boolean = true
)

@Composable
fun SwitchItem(state: SwitchItemState, modifier: Modifier = Modifier) {
    val alphaModifier = if (state.enabled) modifier else modifier.alpha(0.5f)
    Row(modifier = alphaModifier) {
        HorizonSwitch(
            checked = state.checked,
            onCheckedChange = state.onCheckedChanged,
            enabled = state.enabled
        )
        HorizonSpace(SpaceSize.SPACE_8)
        ControlsContent(state = state.controlsContentState, modifier = Modifier.padding(top = 2.dp))
    }
}

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
private fun HorizonSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val switchWidth = 40.dp
    val switchHeight = 24.dp
    val thumbSize = 18.dp
    val thumbOffsetInPx = 16.toPx.toFloat()

    val transition = updateTransition(targetState = checked, label = "Switch Transition")

    val thumbOffset by transition.animateFloat(
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        label = "Thumb Offset"
    ) { if (it) thumbOffsetInPx else 0f }

    val color by transition.animateColor(
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        label = "Background Color"
    ) { if (it) HorizonColors.Icon.default() else HorizonColors.Icon.medium() }

    val iconAlpha by transition.animateFloat(
        transitionSpec = {
            keyframes {
                durationMillis = ANIMATION_DURATION
                1f at 0
                0f at ANIMATION_DURATION / 2
                1f at ANIMATION_DURATION
            }
        },
        label = "Icon Alpha"
    ) { 1f }

    val progress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION, easing = LinearEasing) },
        label = "Progress"
    ) { if (it) 1f else 0f }

    val currentIcon by remember(progress, checked) {
        derivedStateOf {
            if (progress < 0.5f) {
                R.drawable.close
            } else {
                R.drawable.check
            }
        }
    }

    var switchModifier = modifier
        .size(width = switchWidth, height = switchHeight)
        .clip(CircleShape)
        .background(color)
    if (enabled && onCheckedChange != null) switchModifier = switchModifier.clickable { onCheckedChange(!checked) }
    Box(
        modifier = switchModifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 3.dp)
                .size(thumbSize)
                .offset { IntOffset(thumbOffset.toInt(), 0) }
                .background(HorizonColors.Surface.pagePrimary(), CircleShape)
        ) {
            Icon(
                painter = painterResource(currentIcon),
                tint = color,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .alpha(iconAlpha),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SwitchItemPreview() {
    SwitchItem(SwitchItemState(ControlsContentState("Content", "Description"), checked = true))
}

@Composable
@Preview(showBackground = true)
fun SwitchItemOffPreview() {
    SwitchItem(SwitchItemState(ControlsContentState("Content", "Description"), checked = false))
}

@Composable
@Preview(showBackground = true)
fun SwitchItemDisabledPreview() {
    SwitchItem(SwitchItemState(ControlsContentState("Content", "Description"), checked = true, enabled = false))
}