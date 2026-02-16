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
package com.instructure.horizon.horizonui.molecules

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadingImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = { ImagePlaceholder() }
) {
    var isImageLoading by rememberSaveable { mutableStateOf(true) }
    if (!imageUrl.isNullOrEmpty()) {
        GlideImage(
            imageUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            requestBuilderTransform = {
                it.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        return false
                    }

                })
            },
            modifier = modifier
                .aspectRatio(1.69f)
                .shimmerEffect(isImageLoading)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1.69f)
                .background(HorizonColors.Surface.institution().copy(alpha = 0.1f))
        ) {
            placeholder?.invoke()
        }
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(
        Modifier
            .fillMaxSize()
            .background(HorizonColors.Surface.pagePrimary())
    )
}