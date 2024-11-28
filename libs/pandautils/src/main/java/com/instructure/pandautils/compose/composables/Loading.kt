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
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun Loading(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    @DrawableRes icon: Int? = null,
    @RawRes animation: Int? = null,
    color: Color = Color(ThemePrefs.buttonColor)
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                icon != null -> {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null
                    )
                }
                animation != null -> {
                    val preloaderLottieComposition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(
                            animation
                        )
                    )

                    val preloaderProgress by animateLottieCompositionAsState(
                        preloaderLottieComposition,
                        iterations = LottieConstants.IterateForever,
                        isPlaying = true
                    )

                    LottieAnimation(
                        composition = preloaderLottieComposition,
                        progress = { preloaderProgress },
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
                else -> {
                    CircularProgressIndicator(color = color)
                }
            }

            title?.let {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = title,
                    style = MaterialTheme.typography.h5,
                    color = colorResource(R.color.textDarkest),
                    textAlign = TextAlign.Center
                )
            }

            message?.let {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = colorResource(R.color.textDarkest),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    Loading()
}

@Preview(showBackground = true)
@Composable
fun LoadingWithTextPreview() {
    ContextKeeper.appContext = LocalContext.current
    Loading(
        title = stringResource(id = R.string.loading),
        message = stringResource(id = R.string.loadingAssignments),
        icon = R.drawable.ic_smart_search_loading
    )
}

@Preview
@Composable
fun LoadingWithAnimationPreview() {
    ContextKeeper.appContext = LocalContext.current
    Loading(
        title = stringResource(id = R.string.loading),
        message = stringResource(id = R.string.loadingAssignments),
        animation = R.raw.panda_reading
    )
}