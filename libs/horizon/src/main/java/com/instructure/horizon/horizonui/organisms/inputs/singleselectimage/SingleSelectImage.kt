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
package com.instructure.horizon.horizonui.organisms.inputs.singleselectimage

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.common.Input
import com.instructure.horizon.horizonui.organisms.inputs.common.InputContainer
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup

@Composable
fun SingleSelectImage(
    state: SingleSelectImageState,
    modifier: Modifier = Modifier
) {

    Input(
        label = state.label,
        helperText = state.helperText,
        errorText = state.errorText,
        required = state.required,
        modifier = modifier
            .onFocusChanged {
                state.onFocusChanged(it.isFocused)
            }
    ) {
        Column(
            modifier = Modifier
        ) {
            val localDensity = LocalDensity.current
            var heightInPx by remember { mutableIntStateOf(0) }
            var width by remember { mutableStateOf(0.dp) }
            InputContainer(
                isFocused = false,
                isError = state.errorText != null,
                enabled = state.enabled,
                onClick = { state.onMenuOpenChanged(!state.isMenuOpen) },
                modifier = Modifier
                    .onGloballyPositioned {
                        heightInPx = it.size.height
                        width = with(localDensity) { it.size.width.toDp() }
                    }
            ) {
                SingleSelectContent(state)
            }

            InputDropDownPopup(
                isMenuOpen = state.isMenuOpen,
                options = state.options,
                width = width,
                verticalOffsetPx = heightInPx,
                onMenuOpenChanged = state.onMenuOpenChanged,
                onOptionSelected = { selectedOption ->
                    state.onOptionSelected(selectedOption)
                    state.onMenuOpenChanged(false)
                },
                item = { item ->
                    SingleSelectImageItem(item)
                }
            )
        }
    }
}

@Composable
private fun SingleSelectImageItem(item: Pair<Drawable, String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Image(
            bitmap = item.first.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(32.dp)
        )
        Text(
            text = item.second,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier

        )
    }
}

@Composable
private fun SingleSelectContent(state: SingleSelectImageState) {
    val iconRotation = animateIntAsState(
        targetValue = if (state.isMenuOpen) 180 else 0,
        label = "iconRotation"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = state.size.verticalPadding, horizontal = state.size.horizontalPadding)
    ) {
        if (state.selectedOption != null) {
            Text(
                text = state.selectedOption,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
        } else if (state.placeHolderText != null) {
            Text(
                text = state.placeHolderText,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.placeholder(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_down),
            tint = HorizonColors.Icon.default(),
            contentDescription = null,
            modifier = Modifier
                .rotate(iconRotation.value.toFloat())
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSimpleCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 170)
fun SingleSelectSimpleExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = null,
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSimpleCollapsedFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSimpleCollapsedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = null,
            isFocused = true,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSelectedCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300, heightDp = 180)
fun SingleSelectSelectedExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSelectedErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun SingleSelectSelectedCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300, heightDp = 180)
fun SingleSelectSelectedExpandedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = true,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD , widthDp = 300)
fun SingleSelectSelectedErrorCollapsedWithHelperTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            helperText = "Helper text",
            isFocused = false,
            enabled = true,
            isMenuOpen = false,
            errorText = "Error",
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = null,
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD, widthDp = 300)
fun SingleSelectSelectedCollapsedDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    val icon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check)!!
    SingleSelectImage(
        state = SingleSelectImageState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            enabled = false,
            isMenuOpen = false,
            errorText = null,
            size = SingleSelectImageInputSize.Medium,
            options = listOf(Pair(icon, "Option 1"), Pair(icon, "Option 2"), Pair(icon, "Option 3")),
            selectedOption = "Option 1",
            onOptionSelected = {},
            onMenuOpenChanged = {},
        ),
        modifier = Modifier.padding(4.dp)
    )
}