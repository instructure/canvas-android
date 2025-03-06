/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun EmptyContent(
    emptyMessage: String,
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    emptyTitle: String? = null,
    buttonText: String? = null,
    buttonClick: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("EmptyContent")
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(250.dp)
                .testTag(imageRes.toString())
        )
        Spacer(modifier = Modifier.height(32.dp))
        emptyTitle?.let {
            Text(
                text = emptyTitle,
                fontSize = 20.sp,
                color = colorResource(
                    id = R.color.textDarkest
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = emptyMessage,
            fontSize = 16.sp,
            color = colorResource(
                id = R.color.textDarkest
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        buttonClick?.let {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { it() },
                border = BorderStroke(1.dp, colorResource(id = R.color.textDark)),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.backgroundLightest))
            ) {
                Text(
                    text = buttonText.orEmpty(),
                    fontSize = 16.sp,
                    color = colorResource(
                        id = R.color.textDark
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EmptyContentPreview() {
    EmptyContent(
        emptyTitle = "Empty Title",
        emptyMessage = "Empty Message",
        imageRes = R.drawable.ic_panda_book,
        buttonText = "Button Text",
        buttonClick = {}
    )
}