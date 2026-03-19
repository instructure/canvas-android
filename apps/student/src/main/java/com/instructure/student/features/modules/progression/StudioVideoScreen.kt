/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.student.features.modules.progression

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StudioVideoScreen(
    title: String,
    posterUri: String?,
    courseColor: Color = Color(ThemePrefs.primaryColor),
    onOpenClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CanvasThemedAppBar(
                title = title,
                navigationActionClick = onBackClick,
                backgroundColor = courseColor,
                contentColor = colorResource(R.color.textLightest)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.backgroundLightest))
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (posterUri != null) {
                GlideImage(
                    model = posterUri,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = colorResource(id = R.color.backgroundMedium),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_media),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colorResource(id = R.color.textDarkest)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.textDarkest)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "video/mp4",
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(ThemePrefs.brandColor)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.open),
                    color = colorResource(R.color.textLightest)
                )
            }
        }
    }
}

@Preview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioVideoScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    StudioVideoScreen(
        title = "Module Video",
        posterUri = null,
        onOpenClick = {},
        onBackClick = {}
    )
}
