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
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

enum class PillStyle {
    OUTLINE,
    SOLID,
    INLINE
}

enum class PillType(val shapeColor: Color, val textColor: Color, val filledTextColor: Color, val iconColor: Color) {
    DEFAULT(
        HorizonColors.Surface.inversePrimary(),
        HorizonColors.Text.body(),
        HorizonColors.Text.surfaceColored(),
        HorizonColors.Icon.default()
    ),
    DANGER(
        HorizonColors.Surface.error(),
        HorizonColors.Text.error(),
        HorizonColors.Text.surfaceColored(),
        HorizonColors.Surface.error()
    ),
    INVERSE(
        HorizonColors.Surface.pageSecondary(),
        HorizonColors.Text.surfaceColored(),
        HorizonColors.Text.title(),
        HorizonColors.Surface.pageSecondary()
    ),
    INSTITUTION(
        HorizonColors.Surface.institution(),
        HorizonColors.Surface.institution(),
        HorizonColors.Text.surfaceColored(),
        HorizonColors.Surface.institution()
    ),
    LEARNING_OBJECT_TYPE(
        HorizonColors.Surface.institution(),
        HorizonColors.Text.body(),
        HorizonColors.Text.surfaceColored(),
        HorizonColors.Surface.institution()
    )
}

enum class PillCase {
    UPPERCASE,
    TITLE
}

enum class PillSize(val height: Dp, val verticalPadding: Dp, val horizontalPadding: Dp) {
    REGULAR(34.dp, 8.dp, 12.dp),
    SMALL(26.dp, 4.dp, 8.dp)
}

@Composable
fun Pill(
    label: String,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.OUTLINE,
    type: PillType = PillType.DEFAULT,
    case: PillCase = PillCase.UPPERCASE,
    size: PillSize = PillSize.REGULAR,
    @DrawableRes iconRes: Int? = null
) {
    val finalModifier = when (style) {
        PillStyle.OUTLINE -> modifier
            .border(width = 1.dp, shape = HorizonCornerRadius.level4, color = type.shapeColor)
            .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding)

        PillStyle.SOLID -> modifier
            .background(shape = HorizonCornerRadius.level4, color = type.shapeColor)
            .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding)

        PillStyle.INLINE -> modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = finalModifier
    ) {
        if (iconRes != null) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (style == PillStyle.SOLID) type.filledTextColor else type.iconColor,
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        val text = if (case == PillCase.UPPERCASE) label.uppercase() else label
        val textStyle = if (case == PillCase.UPPERCASE) HorizonTypography.tag else HorizonTypography.labelSmall
        Text(
            text = text,
            style = textStyle,
            color = if (style == PillStyle.SOLID) type.filledTextColor else type.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PillDefaultOutlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.OUTLINE,
        label = "Label",
        type = PillType.DEFAULT,
        case = PillCase.UPPERCASE,
        iconRes = R.drawable.calendar_today
    )
}

@Composable
@Preview(showBackground = true)
private fun PillDangerOutlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.OUTLINE, label = "Label", type = PillType.DANGER, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun PillInverseOutlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.OUTLINE,
        label = "Label",
        type = PillType.INVERSE,
        case = PillCase.UPPERCASE,
        iconRes = R.drawable.calendar_today
    )
}

@Composable
@Preview(showBackground = true)
private fun PillInstitutionOutlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.OUTLINE,
        label = "Label",
        type = PillType.INSTITUTION,
        case = PillCase.UPPERCASE,
        iconRes = R.drawable.calendar_today
    )
}

@Composable
@Preview(showBackground = true)
private fun PillDefaultSolidPreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.SOLID, label = "Label", type = PillType.DEFAULT, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true)
private fun PillDangerSolidPreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.SOLID, label = "Label", type = PillType.DANGER, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun PillInverseSolidPreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.SOLID, label = "Label", type = PillType.INVERSE, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true)
private fun PillInstitutionSolidPreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.SOLID,
        label = "Label",
        type = PillType.INSTITUTION,
        case = PillCase.UPPERCASE,
        iconRes = R.drawable.calendar_today
    )
}

@Composable
@Preview(showBackground = true)
private fun PillDefaultInlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.INLINE, label = "Label", type = PillType.DEFAULT, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true)
private fun PillDangerInlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.INLINE, label = "Label", type = PillType.DANGER, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
private fun PillInverseInlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.INLINE, label = "Label", type = PillType.INVERSE, case = PillCase.UPPERCASE, iconRes = R.drawable.calendar_today)
}

@Composable
@Preview(showBackground = true)
private fun PillInstitutionInlinePreview() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.INLINE,
        label = "Label",
        type = PillType.INSTITUTION,
        case = PillCase.UPPERCASE,
        iconRes = R.drawable.calendar_today
    )
}

@Composable
@Preview(showBackground = true)
private fun PillWithoutIcon() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.OUTLINE, label = "Label", type = PillType.DEFAULT, case = PillCase.UPPERCASE)
}

@Composable
@Preview(showBackground = true)
private fun PillNoCaps() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.OUTLINE, label = "Label", type = PillType.DEFAULT, case = PillCase.TITLE)
}

@Composable
@Preview(showBackground = true)
private fun PillLearningObjectCard() {
    ContextKeeper.appContext = LocalContext.current
    Pill(
        style = PillStyle.INLINE,
        label = "Label",
        type = PillType.LEARNING_OBJECT_TYPE,
        case = PillCase.TITLE,
        iconRes = R.drawable.schedule
    )
}

@Composable
@Preview(showBackground = true)
private fun PillSmall() {
    ContextKeeper.appContext = LocalContext.current
    Pill(style = PillStyle.OUTLINE, label = "Label", type = PillType.INSTITUTION, case = PillCase.TITLE, size = PillSize.SMALL)
}