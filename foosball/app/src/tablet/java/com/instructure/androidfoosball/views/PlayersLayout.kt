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
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setVisible
import kotlinx.android.synthetic.tablet.layout_players.view.*
import org.jetbrains.anko.sdk21.listeners.onClick

class PlayersLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleRes) {

    var players: MutableList<User> = mutableListOf()
        set(value) {
            field = value
            refresh()
            onPlayersChanged()
        }

    private var canEdit: Boolean = true
    private val rect = RectF()
    private var radius = 0f

    private var maxPlayerCount = 16

    var onAddPlayerClicked: () -> Unit = {}
    var onPlayersChanged: () -> Unit = {}

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }

    init {
        orientation = HORIZONTAL
        setWillNotDraw(false)
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayersLayout)
            canEdit = a.getBoolean(R.styleable.PlayersLayout_pl_canEdit, canEdit)
            backgroundPaint.color = a.getColor(R.styleable.PlayersLayout_pl_bgColor, Color.GRAY)
            maxPlayerCount = a.getInt(R.styleable.PlayersLayout_pl_maxPlayers, maxPlayerCount)
            a.recycle()
        }
        View.inflate(context, R.layout.layout_players, this)
        addPlayerButton.onClick { onAddPlayerClicked() }
        refresh()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        super.onDraw(canvas)
    }

    fun addUser(user: User) = addPlayer(user)

    fun addPlayer(player: User) {
        removePlayer(player)
        players.add(player)
        addPlayerView(player)
        addPlayerButton.setVisible(canEdit && players.size < maxPlayerCount)
        onPlayersChanged()
    }

    fun removePlayer(player: User) {
        val idx = players.indexOf(player)
        if (idx < 0) return
        players.removeAt(idx)
        root.removeViewAt(idx)
        addPlayerButton.setVisible(canEdit && players.size < maxPlayerCount)
        onPlayersChanged()
    }

    @Suppress("unused")
    fun setCanEdit(value: Boolean) {
        canEdit = value
        refresh()
    }

    fun setBgColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    fun refresh() {
        addPlayerButton.setVisible(canEdit && players.size < maxPlayerCount)
        kotlin.repeat(root.childCount - 1) { root.removeViewAt(0) }
        players.forEach { addPlayerView(it) }
    }

    private fun addPlayerView(player: User) {
        val view = PlayerView(context)
        if (canEdit) {
            view.setEditablePlayer(player) { removePlayer(it) }
        } else {
            view.setPlayer(player)
        }
        root.addView(view, root.childCount - 1)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.left = 1f
        rect.top = 1f
        rect.right = w.toFloat() - 1
        rect.bottom = h.toFloat() - 1
        radius = Math.min(w, h) / 2f
    }

}
