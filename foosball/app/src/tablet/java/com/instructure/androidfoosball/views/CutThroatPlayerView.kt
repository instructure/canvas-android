/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.androidfoosball.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.CutThroatPlayer
import com.instructure.androidfoosball.utils.setAvatar
import kotlinx.android.synthetic.tablet.view_cut_throat_player.view.*
import org.jetbrains.anko.sdk21.listeners.onClick

class CutThroatPlayerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleRes) {

    init {
        View.inflate(context, R.layout.view_cut_throat_player, this)
    }

    fun setEditablePlayer(player: CutThroatPlayer, onClear: (CutThroatPlayer) -> Unit) {
        scoreView.visibility = View.GONE
        avatarView.setAvatar(player.user, context.resources.getDimension(R.dimen.avatar_size_large).toInt())
        removeButton.onClick { onClear(player) }
    }

    fun setPlayer(player: CutThroatPlayer) {
        removeButton.visibility = View.GONE
        scoreView.text = player.score.toString()
        avatarView.setAvatar(player.user, context.resources.getDimension(R.dimen.avatar_size_large).toInt())
    }

}
