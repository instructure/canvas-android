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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.Avatar
import com.instructure.horizon.horizonui.molecules.AvatarColor
import com.instructure.horizon.horizonui.molecules.AvatarSize
import com.instructure.horizon.horizonui.molecules.AvatarType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvatarScreen() {
    FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val avatarTypes = listOf(
            AvatarType.Icon(R.drawable.person),
            AvatarType.Initials("AW"),
            AvatarType.Picture("https://s3-alpha-sig.figma.com/img/08f6/d6f4/422874cef8ee99541ba32e4b43857339?Expires=1743984000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=KOa8QMvgfbcohr~4BY-7Jamcu-dO0Ct7YBP0Ey8MZ7nRqnUo3IEeKEValLglo1qglLKI2lH2Vg0CgVxRDCBlCjB9cq4wxfZ8HfDA3Q-CkZvjofcuRa02ZkJSPvF8pRyPPL4CCgQZUG2QMoMeslc2gEWNQtmsWGEVDcOZfj9RwQUm~Eo9lJ3aYhQxNKggwlb7nxM5EuipFUIZdRCDgYyRwgzO42fJzq9TqP~-2zsunB8u2-nAwtOyNz2SXakOXhS~0qqz8q53ThG6QLc1Ce-kEqPoFmjj20riFukx53NOomG9E8t4UTKiDmeEVR~5zByeLEghJwOcl3D7bt0Pz5pl~w__"),
        )
        avatarTypes.forEach { type ->
            AvatarColor.entries.forEach { color ->
                AvatarSize.entries.forEach { size ->
                    if (color != AvatarColor.INVERSE || type !is AvatarType.Picture) {
                        Avatar(
                            type = type,
                            color = color,
                            size = size,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AvatarScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AvatarScreen()
}