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
package com.instructure.horizon.horizonui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.instructure.horizon.R
import com.instructure.pandautils.utils.ThemePrefs

object HorizonColors {
    object PrimitivesBlue {
        @Composable
        fun blue12() = colorResource(R.color.primitives_blue12)
        @Composable
        fun blue45() = colorResource(R.color.primitives_blue45)
        @Composable
        fun blue57() = colorResource(R.color.primitives_blue57)
        @Composable
        fun blue70() = colorResource(R.color.primitives_blue70)
        @Composable
        fun blue82() = colorResource(R.color.primitives_blue82)
    }

    object PrimitivesGreen {
        @Composable
        fun green12() = colorResource(R.color.primitives_green12)
        @Composable
        fun green45() = colorResource(R.color.primitives_green45)
        @Composable
        fun green57() = colorResource(R.color.primitives_green57)
        @Composable
        fun green70() = colorResource(R.color.primitives_green70)
        @Composable
        fun green82() = colorResource(R.color.primitives_green82)
    }

    object PrimitivesOrange {
        @Composable
        fun orange12() = colorResource(R.color.primitives_orange12)
        @Composable
        fun orange30() = colorResource(R.color.primitives_orange30)
        @Composable
        fun orange45() = colorResource(R.color.primitives_orange45)
        @Composable
        fun orange57() = colorResource(R.color.primitives_orange57)
        @Composable
        fun orange70() = colorResource(R.color.primitives_orange70)
        @Composable
        fun orange82() = colorResource(R.color.primitives_orange82)
    }

    object PrimitivesRed {
        @Composable
        fun red12() = colorResource(R.color.primitives_red12)
        @Composable
        fun red45() = colorResource(R.color.primitives_red45)
        @Composable
        fun red57() = colorResource(R.color.primitives_red57)
        @Composable
        fun red70() = colorResource(R.color.primitives_red70)
        @Composable
        fun red82() = colorResource(R.color.primitives_red82)
    }

    object PrimitivesWhite {
        @Composable
        fun white10() = colorResource(R.color.primitives_white10)
    }

    object PrimitivesGrey {
        @Composable
        fun grey11() = colorResource(R.color.primitives_grey11)
        @Composable
        fun grey12() = colorResource(R.color.primitives_grey12)
        @Composable
        fun grey14() = colorResource(R.color.primitives_grey14)
        @Composable
        fun grey24() = colorResource(R.color.primitives_grey24)
        @Composable
        fun grey45() = colorResource(R.color.primitives_grey45)
        @Composable
        fun grey57() = colorResource(R.color.primitives_grey57)
        @Composable
        fun grey70() = colorResource(R.color.primitives_grey70)
        @Composable
        fun grey82() = colorResource(R.color.primitives_grey82)
        @Composable
        fun grey100() = colorResource(R.color.primitives_grey100)
        @Composable
        fun grey125() = colorResource(R.color.primitives_grey125)
    }

    object PrimitivesBlack {
        @Composable
        fun black174() = colorResource(R.color.primitives_black174)
    }

    object PrimitivesBeige {
        @Composable
        fun beige10() = colorResource(R.color.primitives_beige10)
        @Composable
        fun beige11() = colorResource(R.color.primitives_beige11)
        @Composable
        fun beige12() = colorResource(R.color.primitives_beige12)
        @Composable
        fun beige15() = colorResource(R.color.primitives_beige15)
        @Composable
        fun beige19() = colorResource(R.color.primitives_beige19)
        @Composable
        fun beige26() = colorResource(R.color.primitives_beige26)
        @Composable
        fun beige35() = colorResource(R.color.primitives_beige35)
        @Composable
        fun beige49() = colorResource(R.color.primitives_beige49)
        @Composable
        fun beige69() = colorResource(R.color.primitives_beige69)
        @Composable
        fun beige100() = colorResource(R.color.primitives_beige100)
        @Composable
        fun beige147() = colorResource(R.color.primitives_beige147)
    }

    object PrimitivesRose {
        @Composable
        fun rose30() = colorResource(R.color.primitives_rose30)
        @Composable
        fun rose35() = colorResource(R.color.primitives_rose35)
        @Composable
        fun rose40() = colorResource(R.color.primitives_rose40)
        @Composable
        fun rose45() = colorResource(R.color.primitives_rose45)
        @Composable
        fun rose50() = colorResource(R.color.primitives_rose50)
        @Composable
        fun rose57() = colorResource(R.color.primitives_rose57)
        @Composable
        fun rose70() = colorResource(R.color.primitives_rose70)
        @Composable
        fun rose90() = colorResource(R.color.primitives_rose90)
        @Composable
        fun rose110() = colorResource(R.color.primitives_rose110)
    }

    object PrimitivesCopper {
        @Composable
        fun copper30() = colorResource(R.color.primitives_copper30)
        @Composable
        fun copper35() = colorResource(R.color.primitives_copper35)
        @Composable
        fun copper40() = colorResource(R.color.primitives_copper40)
        @Composable
        fun copper45() = colorResource(R.color.primitives_copper45)
        @Composable
        fun copper50() = colorResource(R.color.primitives_copper50)
        @Composable
        fun copper57() = colorResource(R.color.primitives_copper57)
        @Composable
        fun copper70() = colorResource(R.color.primitives_copper70)
        @Composable
        fun copper90() = colorResource(R.color.primitives_copper90)
        @Composable
        fun copper110() = colorResource(R.color.primitives_copper110)
    }

    object PrimitivesHoney {
        @Composable
        fun honey30() = colorResource(R.color.primitives_honey30)
        @Composable
        fun honey35() = colorResource(R.color.primitives_honey35)
        @Composable
        fun honey40() = colorResource(R.color.primitives_honey40)
        @Composable
        fun honey45() = colorResource(R.color.primitives_honey45)
        @Composable
        fun honey50() = colorResource(R.color.primitives_honey50)
        @Composable
        fun honey57() = colorResource(R.color.primitives_honey57)
        @Composable
        fun honey70() = colorResource(R.color.primitives_honey70)
        @Composable
        fun honey90() = colorResource(R.color.primitives_honey90)
        @Composable
        fun honey110() = colorResource(R.color.primitives_honey110)
    }

    object PrimitivesForest {
        @Composable
        fun forest30() = colorResource(R.color.primitives_forest30)
        @Composable
        fun forest35() = colorResource(R.color.primitives_forest35)
        @Composable
        fun forest40() = colorResource(R.color.primitives_forest40)
        @Composable
        fun forest45() = colorResource(R.color.primitives_forest45)
        @Composable
        fun forest50() = colorResource(R.color.primitives_forest50)
        @Composable
        fun forest57() = colorResource(R.color.primitives_forest57)
        @Composable
        fun forest70() = colorResource(R.color.primitives_forest70)
        @Composable
        fun forest90() = colorResource(R.color.primitives_forest90)
        @Composable
        fun forest110() = colorResource(R.color.primitives_forest110)
    }

    object PrimitivesAurora {
        @Composable
        fun aurora30() = colorResource(R.color.primitives_aurora30)
        @Composable
        fun aurora35() = colorResource(R.color.primitives_aurora35)
        @Composable
        fun aurora40() = colorResource(R.color.primitives_aurora40)
        @Composable
        fun aurora45() = colorResource(R.color.primitives_aurora45)
        @Composable
        fun aurora50() = colorResource(R.color.primitives_aurora50)
        @Composable
        fun aurora57() = colorResource(R.color.primitives_aurora57)
        @Composable
        fun aurora70() = colorResource(R.color.primitives_aurora70)
        @Composable
        fun aurora90() = colorResource(R.color.primitives_aurora90)
        @Composable
        fun aurora110() = colorResource(R.color.primitives_aurora110)
    }

    object PrimitivesSea {
        @Composable
        fun sea30() = colorResource(R.color.primitives_sea30)
        @Composable
        fun sea35() = colorResource(R.color.primitives_sea35)
        @Composable
        fun sea40() = colorResource(R.color.primitives_sea40)
        @Composable
        fun sea45() = colorResource(R.color.primitives_sea45)
        @Composable
        fun sea50() = colorResource(R.color.primitives_sea50)
        @Composable
        fun sea57() = colorResource(R.color.primitives_sea57)
        @Composable
        fun sea70() = colorResource(R.color.primitives_sea70)
        @Composable
        fun sea90() = colorResource(R.color.primitives_sea90)
        @Composable
        fun sea110() = colorResource(R.color.primitives_sea110)
    }

    object PrimitivesSky {
        @Composable
        fun sky30() = colorResource(R.color.primitives_sky30)
        @Composable
        fun sky35() = colorResource(R.color.primitives_sky35)
        @Composable
        fun sky40() = colorResource(R.color.primitives_sky40)
        @Composable
        fun sky45() = colorResource(R.color.primitives_sky45)
        @Composable
        fun sky50() = colorResource(R.color.primitives_sky50)
        @Composable
        fun sky57() = colorResource(R.color.primitives_sky57)
        @Composable
        fun sky70() = colorResource(R.color.primitives_sky70)
        @Composable
        fun sky90() = colorResource(R.color.primitives_sky90)
        @Composable
        fun sky110() = colorResource(R.color.primitives_sky110)
    }

    object PrimitivesOcean {
        @Composable
        fun ocean57() = colorResource(R.color.primitives_ocean57)
        @Composable
        fun ocean70() = colorResource(R.color.primitives_ocean70)
        @Composable
        fun ocean90() = colorResource(R.color.primitives_ocean90)
        @Composable
        fun ocean110() = colorResource(R.color.primitives_ocean110)
    }

    object PrimitivesViolet {
        @Composable
        fun violet30() = colorResource(R.color.primitives_violet30)
        @Composable
        fun violet35() = colorResource(R.color.primitives_violet35)
        @Composable
        fun violet40() = colorResource(R.color.primitives_violet40)
        @Composable
        fun violet45() = colorResource(R.color.primitives_violet45)
        @Composable
        fun violet50() = colorResource(R.color.primitives_violet50)
        @Composable
        fun violet57() = colorResource(R.color.primitives_violet57)
        @Composable
        fun violet70() = colorResource(R.color.primitives_violet70)
        @Composable
        fun violet90() = colorResource(R.color.primitives_violet90)
        @Composable
        fun violet110() = colorResource(R.color.primitives_violet110)
    }

    object PrimitivesPlum {
        @Composable
        fun plum30() = colorResource(R.color.primitives_plum30)
        @Composable
        fun plum35() = colorResource(R.color.primitives_plum35)
        @Composable
        fun plum40() = colorResource(R.color.primitives_plum40)
        @Composable
        fun plum45() = colorResource(R.color.primitives_plum45)
        @Composable
        fun plum50() = colorResource(R.color.primitives_plum50)
        @Composable
        fun plum57() = colorResource(R.color.primitives_plum57)
        @Composable
        fun plum70() = colorResource(R.color.primitives_plum70)
        @Composable
        fun plum90() = colorResource(R.color.primitives_plum90)
        @Composable
        fun plum110() = colorResource(R.color.primitives_plum110)
    }

    object PrimitivesStone {
        @Composable
        fun stone30() = colorResource(R.color.primitives_stone30)
        @Composable
        fun stone35() = colorResource(R.color.primitives_stone35)
        @Composable
        fun stone40() = colorResource(R.color.primitives_stone40)
        @Composable
        fun stone45() = colorResource(R.color.primitives_stone45)
        @Composable
        fun stone50() = colorResource(R.color.primitives_stone50)
        @Composable
        fun stone57() = colorResource(R.color.primitives_stone57)
        @Composable
        fun stone70() = colorResource(R.color.primitives_stone70)
        @Composable
        fun stone90() = colorResource(R.color.primitives_stone90)
        @Composable
        fun stone110() = colorResource(R.color.primitives_stone110)
    }

    object Surface {
        @Composable
        fun attention() = colorResource(R.color.surface_attention)
        @Composable
        fun attentionSecondary() = colorResource(R.color.surface_attentionSecondary)
        @Composable
        fun cardPrimary() = colorResource(R.color.surface_cardPrimary)
        @Composable
        fun cardSecondary() = colorResource(R.color.surface_cardSecondary)
        @Composable
        fun divider() = colorResource(R.color.surface_divider)
        fun error() = Color(0xFFC71F23)
        fun inversePrimary() = Color(0xFF273540)
        @Composable
        fun inverseSecondary() = colorResource(R.color.surface_inverseSecondary)
        @Composable
        fun overlayGrey() = colorResource(R.color.surface_overlayGrey)
        @Composable
        fun overlayWhite() = colorResource(R.color.surface_overlayWhite)
        @Composable
        fun pagePrimary() = colorResource(R.color.surface_pagePrimary)
        fun pageSecondary() = Color(0xFFFFFFFF)
        @Composable
        fun pageTertiary() = colorResource(R.color.surface_pageTertiary)
        @Composable
        fun warning() = colorResource(R.color.surface_warning)
        @Composable
        fun success() = colorResource(R.color.surface_success)
        @Composable
        fun aiGradient() = Brush.verticalGradient(
            colors = listOf(
                colorResource(R.color.ai_gradient_start),
                colorResource(R.color.ai_gradient_end)
            )
        )
        fun institution() = Color(ThemePrefs.brandColor)
    }

    object Text {
        @Composable
        fun author() = colorResource(R.color.text_author)
        @Composable
        fun beigePrimary() = colorResource(R.color.text_beigePrimary)
        @Composable
        fun beigeSecondary() = colorResource(R.color.text_beigeSecondary)
        fun body() = Color(0xFF273540)
        @Composable
        fun dataPoint() = colorResource(R.color.text_dataPoint)
        @Composable
        fun link() = colorResource(R.color.text_link)
        @Composable
        fun linkSecondary() = colorResource(R.color.text_linkSecondary)
        @Composable
        fun placeholder() = colorResource(R.color.text_placeholder)
        fun surfaceColored() = Color(0xFFFFFFFF)
        @Composable
        fun surfaceInverseSecondary() = colorResource(R.color.text_surfaceInverseSecondary)
        fun error() = Color(0xFFC71F23)
        @Composable
        fun success() = colorResource(R.color.text_success)
        @Composable
        fun warning() = colorResource(R.color.text_warning)
        @Composable
        fun timestamp() = colorResource(R.color.text_timestamp)
        fun title() = Color(0xFF273540)
    }

    object Icon {
        @Composable
        fun action() = colorResource(R.color.icon_action)
        @Composable
        fun actionSecondary() = colorResource(R.color.icon_actionSecondary)
        @Composable
        fun beigePrimary() = colorResource(R.color.icon_beigePrimary)
        @Composable
        fun beigeSecondary() = colorResource(R.color.icon_beigeSecondary)
        fun default() = Color(0xFF273540)
        @Composable
        fun error() = colorResource(R.color.icon_error)
        @Composable
        fun light() = colorResource(R.color.icon_light)
        @Composable
        fun medium() = colorResource(R.color.icon_medium)
        @Composable
        fun success() = colorResource(R.color.icon_success)
        @Composable
        fun surfaceColored() = colorResource(R.color.icon_surfaceColored)
        @Composable
        fun surfaceInverseSecondary() = colorResource(R.color.icon_surfaceInverseSecondary)
        @Composable
        fun warning() = colorResource(R.color.icon_warning)
    }

    object LineAndBorder {
        @Composable
        fun containerStroke() = colorResource(R.color.lineAndBorder_containerStroke)
        @Composable
        fun lineConnector() = colorResource(R.color.lineAndBorder_lineConnector)
        @Composable
        fun lineDivider() = colorResource(R.color.lineAndBorder_lineDivider)
        @Composable
        fun lineStroke() = colorResource(R.color.lineAndBorder_lineStroke)
    }
}