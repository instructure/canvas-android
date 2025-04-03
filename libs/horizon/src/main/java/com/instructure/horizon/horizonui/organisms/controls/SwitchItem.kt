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
@file:OptIn(ExperimentalAnimationGraphicsApi::class)

package com.instructure.horizon.horizonui.organisms.controls

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
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
import androidx.compose.runtime.getValue
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

private const val ANIMATION_DURATION = 200

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

//    val vector = AnimatedImageVector.animatedVectorResource(R.drawable.avd_anim)
//    val animatedVectorPainter = rememberAnimatedVectorPainter(
//        vector, // Your AVD XML
//        !checked
//    )

    Box(
        modifier = modifier
            .size(width = switchWidth, height = switchHeight)
            .clip(CircleShape)
            .background(color)
            .clickable { onCheckedChange!!(!checked) },
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
                painter = painterResource(if (checked) R.drawable.check else R.drawable.close),
                tint = color,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SwitchItemPreview() {
    SwitchItem(SwitchItemState(ControlsContentState("Content", "Description"), checked = true))
}