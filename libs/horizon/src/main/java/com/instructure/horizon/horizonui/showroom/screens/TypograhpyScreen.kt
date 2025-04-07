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
package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun TypographyScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(text = "h1", style = HorizonTypography.h1)
        Text(text = "This is an example text", style = HorizonTypography.h1, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "h2", style = HorizonTypography.h2)
        Text(text = "This is an example text", style = HorizonTypography.h2, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "h3", style = HorizonTypography.h3)
        Text(text = "This is an example text", style = HorizonTypography.h3, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "h4", style = HorizonTypography.h4)
        Text(text = "This is an example text", style = HorizonTypography.h4, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "sh1", style = HorizonTypography.sh1)
        Text(text = "This is an example text", style = HorizonTypography.sh1, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "sh2", style = HorizonTypography.sh2)
        Text(text = "This is an example text", style = HorizonTypography.sh2, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "sh3", style = HorizonTypography.sh3)
        Text(text = "This is an example text", style = HorizonTypography.sh3, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "sh4", style = HorizonTypography.sh4)
        Text(text = "This is an example text", style = HorizonTypography.sh4, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "p1", style = HorizonTypography.p1)
        Text(text = "This is an example text", style = HorizonTypography.p1, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "p2", style = HorizonTypography.p2)
        Text(text = "This is an example text", style = HorizonTypography.p2, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "p3", style = HorizonTypography.p3)
        Text(text = "This is an example text", style = HorizonTypography.p3, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "tag", style = HorizonTypography.tag)
        Text(text = "This is an example text", style = HorizonTypography.tag, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "labelLargeBold", style = HorizonTypography.labelLargeBold)
        Text(text = "This is an example text", style = HorizonTypography.labelLargeBold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "labelMediumBold", style = HorizonTypography.labelMediumBold)
        Text(text = "This is an example text", style = HorizonTypography.labelMediumBold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "labelSmallBold", style = HorizonTypography.labelSmallBold)
        Text(text = "This is an example text", style = HorizonTypography.labelSmallBold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "labelSmall", style = HorizonTypography.labelSmall)
        Text(text = "This is an example text", style = HorizonTypography.labelSmall, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "buttonTextLarge", style = HorizonTypography.buttonTextLarge)
        Text(text = "This is an example text", style = HorizonTypography.buttonTextLarge, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "buttonTextMedium", style = HorizonTypography.buttonTextMedium)
        Text(text = "This is an example text", style = HorizonTypography.buttonTextMedium, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
@Preview(showBackground = true)
fun TypographyScreenPreview() {
    TypographyScreen()
}