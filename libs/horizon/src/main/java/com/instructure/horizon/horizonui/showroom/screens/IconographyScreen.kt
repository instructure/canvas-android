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
@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun IconographyScreen() {
    FlowRow {
        Icon(
            painterResource(R.drawable.accessibility_new),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.account_circle),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.add),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.alarm),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.archive),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.arrow_back),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.arrow_forward),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.attach_file),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.autorenew),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.backup),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.block),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.bookmark),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.bug_report),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.build),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.cached),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.camera),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.change_history),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.check),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.check_circle),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.code),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.compare_arrows),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.contacts),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.content_copy),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.content_cut),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.content_paste),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.delete),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.description),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.done_all),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.error),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.event),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.exit_to_app),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.extension),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
        Icon(
            painterResource(R.drawable.favorite),
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(8.dp),
            tint = HorizonColors.Icon.default()
        )
    }
}

@Composable
@Preview(showBackground = true)
fun IconographyScreenPreview() {
    IconographyScreen()
}
