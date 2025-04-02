/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ProfileUtils


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserAvatar(
    imageUrl: String?,
    name: String,
    anonymous: Boolean = false,
    modifier: Modifier = Modifier
) {
    val model: Any? = when {
        anonymous -> R.drawable.ic_user_avatar
        ProfileUtils.shouldLoadAltAvatarImage(imageUrl) -> ProfileUtils.createAvatarDrawable(
            context = LocalContext.current,
            userName = name,
            borderWidth = with(LocalDensity.current) {
                dimensionResource(id = R.dimen.avatar_border_width_thin).toPx().toInt()
            }
        ).toBitmap()
        else -> imageUrl
    }

    GlideImage(
        model = model,
        contentDescription = null,
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    ) {
        it.placeholder(R.drawable.recipient_avatar_placeholder)
    }
}

@Preview(showBackground = true)
@Composable
fun UserAvatarPreview() {
    UserAvatar(
        imageUrl = null,
        name = "Test User",
        modifier = Modifier.size(100.dp)
    )
}
