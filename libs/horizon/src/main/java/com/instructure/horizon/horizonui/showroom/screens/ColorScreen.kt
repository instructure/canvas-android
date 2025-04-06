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
@file:OptIn(ExperimentalLayoutApi::class)

package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun ColorScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        val colorsList = listOf(
            "PrimitivesBlue" to listOf(
                "blue12" to HorizonColors.PrimitivesBlue.blue12(),
                "blue45" to HorizonColors.PrimitivesBlue.blue45(),
                "blue57" to HorizonColors.PrimitivesBlue.blue57(),
                "blue70" to HorizonColors.PrimitivesBlue.blue70(),
                "blue82" to HorizonColors.PrimitivesBlue.blue82()
            ),
            "PrimitivesGreen" to listOf(
                "green12" to HorizonColors.PrimitivesGreen.green12(),
                "green45" to HorizonColors.PrimitivesGreen.green45(),
                "green57" to HorizonColors.PrimitivesGreen.green57(),
                "green70" to HorizonColors.PrimitivesGreen.green70(),
                "green82" to HorizonColors.PrimitivesGreen.green82()
            ),
            "PrimitivesOrange" to listOf(
                "orange12" to HorizonColors.PrimitivesOrange.orange12(),
                "orange30" to HorizonColors.PrimitivesOrange.orange30(),
                "orange45" to HorizonColors.PrimitivesOrange.orange45(),
                "orange57" to HorizonColors.PrimitivesOrange.orange57(),
                "orange70" to HorizonColors.PrimitivesOrange.orange70(),
                "orange82" to HorizonColors.PrimitivesOrange.orange82()
            ),
            "PrimitivesRed" to listOf(
                "red12" to HorizonColors.PrimitivesRed.red12(),
                "red45" to HorizonColors.PrimitivesRed.red45(),
                "red57" to HorizonColors.PrimitivesRed.red57(),
                "red70" to HorizonColors.PrimitivesRed.red70(),
                "red82" to HorizonColors.PrimitivesRed.red82()
            ),
            "PrimitivesWhite" to listOf(
                "white10" to HorizonColors.PrimitivesWhite.white10()
            ),
            "PrimitivesGrey" to listOf(
                "grey11" to HorizonColors.PrimitivesGrey.grey11(),
                "grey12" to HorizonColors.PrimitivesGrey.grey12(),
                "grey14" to HorizonColors.PrimitivesGrey.grey14(),
                "grey24" to HorizonColors.PrimitivesGrey.grey24(),
                "grey45" to HorizonColors.PrimitivesGrey.grey45(),
                "grey57" to HorizonColors.PrimitivesGrey.grey57(),
                "grey70" to HorizonColors.PrimitivesGrey.grey70(),
                "grey82" to HorizonColors.PrimitivesGrey.grey82(),
                "grey100" to HorizonColors.PrimitivesGrey.grey100(),
                "grey125" to HorizonColors.PrimitivesGrey.grey125()
            ),
            "PrimitivesBlack" to listOf(
                "black174" to HorizonColors.PrimitivesBlack.black174()
            ),
            "PrimitivesBeige" to listOf(
                "beige10" to HorizonColors.PrimitivesBeige.beige10(),
                "beige11" to HorizonColors.PrimitivesBeige.beige11(),
                "beige12" to HorizonColors.PrimitivesBeige.beige12(),
                "beige15" to HorizonColors.PrimitivesBeige.beige15(),
                "beige19" to HorizonColors.PrimitivesBeige.beige19(),
                "beige26" to HorizonColors.PrimitivesBeige.beige26(),
                "beige35" to HorizonColors.PrimitivesBeige.beige35(),
                "beige49" to HorizonColors.PrimitivesBeige.beige49(),
                "beige69" to HorizonColors.PrimitivesBeige.beige69(),
                "beige100" to HorizonColors.PrimitivesBeige.beige100(),
                "beige147" to HorizonColors.PrimitivesBeige.beige147()
            ),
            "PrimitivesRose" to listOf(
                "rose30" to HorizonColors.PrimitivesRose.rose30(),
                "rose35" to HorizonColors.PrimitivesRose.rose35(),
                "rose40" to HorizonColors.PrimitivesRose.rose40(),
                "rose45" to HorizonColors.PrimitivesRose.rose45(),
                "rose50" to HorizonColors.PrimitivesRose.rose50(),
                "rose57" to HorizonColors.PrimitivesRose.rose57(),
                "rose70" to HorizonColors.PrimitivesRose.rose70(),
                "rose90" to HorizonColors.PrimitivesRose.rose90(),
                "rose110" to HorizonColors.PrimitivesRose.rose110()
            ),
            "PrimitivesCopper" to listOf(
                "copper30" to HorizonColors.PrimitivesCopper.copper30(),
                "copper35" to HorizonColors.PrimitivesCopper.copper35(),
                "copper40" to HorizonColors.PrimitivesCopper.copper40(),
                "copper45" to HorizonColors.PrimitivesCopper.copper45(),
                "copper50" to HorizonColors.PrimitivesCopper.copper50(),
                "copper57" to HorizonColors.PrimitivesCopper.copper57(),
                "copper70" to HorizonColors.PrimitivesCopper.copper70(),
                "copper90" to HorizonColors.PrimitivesCopper.copper90(),
                "copper110" to HorizonColors.PrimitivesCopper.copper110()
            ),
            "PrimitivesHoney" to listOf(
                "honey30" to HorizonColors.PrimitivesHoney.honey30(),
                "honey35" to HorizonColors.PrimitivesHoney.honey35(),
                "honey40" to HorizonColors.PrimitivesHoney.honey40(),
                "honey45" to HorizonColors.PrimitivesHoney.honey45(),
                "honey50" to HorizonColors.PrimitivesHoney.honey50(),
                "honey57" to HorizonColors.PrimitivesHoney.honey57(),
                "honey70" to HorizonColors.PrimitivesHoney.honey70(),
                "honey90" to HorizonColors.PrimitivesHoney.honey90(),
                "honey110" to HorizonColors.PrimitivesHoney.honey110()
            ),
            "PrimitivesForest" to listOf(
                "forest30" to HorizonColors.PrimitivesForest.forest30(),
                "forest35" to HorizonColors.PrimitivesForest.forest35(),
                "forest40" to HorizonColors.PrimitivesForest.forest40(),
                "forest45" to HorizonColors.PrimitivesForest.forest45(),
                "forest50" to HorizonColors.PrimitivesForest.forest50(),
                "forest57" to HorizonColors.PrimitivesForest.forest57(),
                "forest70" to HorizonColors.PrimitivesForest.forest70(),
                "forest90" to HorizonColors.PrimitivesForest.forest90(),
                "forest110" to HorizonColors.PrimitivesForest.forest110()
            ),
            "PrimitivesAurora" to listOf(
                "aurora30" to HorizonColors.PrimitivesAurora.aurora30(),
                "aurora35" to HorizonColors.PrimitivesAurora.aurora35(),
                "aurora40" to HorizonColors.PrimitivesAurora.aurora40(),
                "aurora45" to HorizonColors.PrimitivesAurora.aurora45(),
                "aurora50" to HorizonColors.PrimitivesAurora.aurora50(),
                "aurora57" to HorizonColors.PrimitivesAurora.aurora57(),
                "aurora70" to HorizonColors.PrimitivesAurora.aurora70(),
                "aurora90" to HorizonColors.PrimitivesAurora.aurora90(),
                "aurora110" to HorizonColors.PrimitivesAurora.aurora110()
            ),
            "PrimitivesSea" to listOf(
                "sea30" to HorizonColors.PrimitivesSea.sea30(),
                "sea35" to HorizonColors.PrimitivesSea.sea35(),
                "sea40" to HorizonColors.PrimitivesSea.sea40(),
                "sea45" to HorizonColors.PrimitivesSea.sea45(),
                "sea50" to HorizonColors.PrimitivesSea.sea50(),
                "sea57" to HorizonColors.PrimitivesSea.sea57(),
                "sea70" to HorizonColors.PrimitivesSea.sea70(),
                "sea90" to HorizonColors.PrimitivesSea.sea90(),
                "sea110" to HorizonColors.PrimitivesSea.sea110()
            ),
            "PrimitivesSky" to listOf(
                "sky30" to HorizonColors.PrimitivesSky.sky30(),
                "sky35" to HorizonColors.PrimitivesSky.sky35(),
                "sky40" to HorizonColors.PrimitivesSky.sky40(),
                "sky45" to HorizonColors.PrimitivesSky.sky45(),
                "sky50" to HorizonColors.PrimitivesSky.sky50(),
                "sky57" to HorizonColors.PrimitivesSky.sky57(),
                "sky70" to HorizonColors.PrimitivesSky.sky70(),
                "sky90" to HorizonColors.PrimitivesSky.sky90(),
                "sky110" to HorizonColors.PrimitivesSky.sky110()
            ),
            "PrimitivesOcean" to listOf(
                "ocean57" to HorizonColors.PrimitivesOcean.ocean57(),
                "ocean70" to HorizonColors.PrimitivesOcean.ocean70(),
                "ocean90" to HorizonColors.PrimitivesOcean.ocean90(),
                "ocean110" to HorizonColors.PrimitivesOcean.ocean110()
            ),
            "PrimitivesViolet" to listOf(
                "violet30" to HorizonColors.PrimitivesViolet.violet30(),
                "violet35" to HorizonColors.PrimitivesViolet.violet35(),
                "violet40" to HorizonColors.PrimitivesViolet.violet40(),
                "violet45" to HorizonColors.PrimitivesViolet.violet45(),
                "violet50" to HorizonColors.PrimitivesViolet.violet50(),
                "violet57" to HorizonColors.PrimitivesViolet.violet57(),
                "violet70" to HorizonColors.PrimitivesViolet.violet70(),
                "violet90" to HorizonColors.PrimitivesViolet.violet90(),
                "violet110" to HorizonColors.PrimitivesViolet.violet110()
            ),
            "PrimitivesPlum" to listOf(
                "plum30" to HorizonColors.PrimitivesPlum.plum30(),
                "plum35" to HorizonColors.PrimitivesPlum.plum35(),
                "plum40" to HorizonColors.PrimitivesPlum.plum40(),
                "plum45" to HorizonColors.PrimitivesPlum.plum45(),
                "plum50" to HorizonColors.PrimitivesPlum.plum50(),
                "plum57" to HorizonColors.PrimitivesPlum.plum57(),
                "plum70" to HorizonColors.PrimitivesPlum.plum70(),
                "plum90" to HorizonColors.PrimitivesPlum.plum90(),
                "plum110" to HorizonColors.PrimitivesPlum.plum110()
            ),
            "PrimitivesStone" to listOf(
                "stone30" to HorizonColors.PrimitivesStone.stone30(),
                "stone35" to HorizonColors.PrimitivesStone.stone35(),
                "stone40" to HorizonColors.PrimitivesStone.stone40(),
                "stone45" to HorizonColors.PrimitivesStone.stone45(),
                "stone50" to HorizonColors.PrimitivesStone.stone50(),
                "stone57" to HorizonColors.PrimitivesStone.stone57(),
                "stone70" to HorizonColors.PrimitivesStone.stone70(),
                "stone90" to HorizonColors.PrimitivesStone.stone90(),
                "stone110" to HorizonColors.PrimitivesStone.stone110()
            ),
            "Surface" to listOf(
                "attention" to HorizonColors.Surface.attention(),
                "attentionSecondary" to HorizonColors.Surface.attentionSecondary(),
                "cardPrimary" to HorizonColors.Surface.cardPrimary(),
                "cardSecondary" to HorizonColors.Surface.cardSecondary(),
                "divider" to HorizonColors.Surface.divider(),
                "error" to HorizonColors.Surface.error(),
                "inversePrimary" to HorizonColors.Surface.inversePrimary(),
                "inverseSecondary" to HorizonColors.Surface.inverseSecondary(),
                "overlayGrey" to HorizonColors.Surface.overlayGrey(),
                "overlayWhite" to HorizonColors.Surface.overlayWhite(),
                "pagePrimary" to HorizonColors.Surface.pagePrimary(),
                "pageSecondary" to HorizonColors.Surface.pageSecondary(),
                "pageTertiary" to HorizonColors.Surface.pageTertiary(),
                "warning" to HorizonColors.Surface.warning(),
                "success" to HorizonColors.Surface.success(),
                "aiGradient" to HorizonColors.Surface.aiGradient(),
                "institution" to HorizonColors.Surface.institution(),
                "aiGradientStart" to HorizonColors.Surface.aiGradientStart(),
                "aiGradientEnd" to HorizonColors.Surface.aiGradientEnd()
            ),
            "Text" to listOf(
                "author" to HorizonColors.Text.author(),
                "beigePrimary" to HorizonColors.Text.beigePrimary(),
                "beigeSecondary" to HorizonColors.Text.beigeSecondary(),
                "body" to HorizonColors.Text.body(),
                "dataPoint" to HorizonColors.Text.dataPoint(),
                "link" to HorizonColors.Text.link(),
                "linkSecondary" to HorizonColors.Text.linkSecondary(),
                "placeholder" to HorizonColors.Text.placeholder(),
                "surfaceColored" to HorizonColors.Text.surfaceColored(),
                "surfaceInverseSecondary" to HorizonColors.Text.surfaceInverseSecondary(),
                "error" to HorizonColors.Text.error(),
                "success" to HorizonColors.Text.success(),
                "warning" to HorizonColors.Text.warning(),
                "timestamp" to HorizonColors.Text.timestamp(),
                "title" to HorizonColors.Text.title()
            ),
            "Icon" to listOf(
                "action" to HorizonColors.Icon.action(),
                "actionSecondary" to HorizonColors.Icon.actionSecondary(),
                "beigePrimary" to HorizonColors.Icon.beigePrimary(),
                "beigeSecondary" to HorizonColors.Icon.beigeSecondary(),
                "default" to HorizonColors.Icon.default(),
                "error" to HorizonColors.Icon.error(),
                "light" to HorizonColors.Icon.light(),
                "medium" to HorizonColors.Icon.medium(),
                "success" to HorizonColors.Icon.success(),
                "surfaceColored" to HorizonColors.Icon.surfaceColored(),
                "surfaceInverseSecondary" to HorizonColors.Icon.surfaceInverseSecondary(),
                "warning" to HorizonColors.Icon.warning()
            ),
            "LineAndBorder" to listOf(
                "containerStroke" to HorizonColors.LineAndBorder.containerStroke(),
                "lineConnector" to HorizonColors.LineAndBorder.lineConnector(),
                "lineDivider" to HorizonColors.LineAndBorder.lineDivider(),
                "lineStroke" to HorizonColors.LineAndBorder.lineStroke()
            )
        ).forEach { colorCategory ->
            Text(text = colorCategory.first, style = HorizonTypography.p2)
            FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colorCategory.second.forEach { color ->
                    if (color.second is Color) {
                        ColorCircle(color = color.second as Color, name = color.first)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(color: Color, name: String) {
    Column {
        Text(
            text = name,
            style = HorizonTypography.p3,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}