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
import com.instructure.androidfoosball.ktmodels.CutThroatPlayer
import com.instructure.androidfoosball.ktmodels.User
import kotlinx.android.synthetic.tablet.view_team_layout.view.*
import org.jetbrains.anko.sdk21.listeners.onClick
import java.util.*

class CutThroatPlayersLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleRes) {

    var players: MutableList<CutThroatPlayer> = ArrayList()
        set(value) {
            field = value
            refresh()
            onPlayersChanged()
        }

    private var canEdit: Boolean = true
    private val rect = RectF()
    private var radius = 0f

    var onAddPlayerClicked: () -> Unit = {}
    var onPlayersChanged: () -> Unit = {}

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }

    init {
        orientation = LinearLayout.HORIZONTAL
        setWillNotDraw(false)
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CutThroatPlayersLayout)
            canEdit = a.getBoolean(R.styleable.CutThroatPlayersLayout_ctpl_canEdit, canEdit)
            backgroundPaint.color = a.getColor(R.styleable.CutThroatPlayersLayout_ctpl_bgColor, Color.GRAY)
            a.recycle()
        }
        View.inflate(context, R.layout.layout_cut_throat_players, this)
        addPlayerButton.onClick { onAddPlayerClicked() }
        refresh()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        super.onDraw(canvas)
    }

    fun addUser(user: User) = addPlayer(CutThroatPlayer(user))

    fun addPlayer(player: CutThroatPlayer) {
        removePlayer(player)
        players.add(player)
        addPlayerView(player)
        onPlayersChanged()
    }

    fun removePlayer(player: CutThroatPlayer) {
        val idx = players.indexOfFirst { it.user == player.user }
        if (idx < 0) return
        players.removeAt(idx)
        root.removeViewAt(idx)
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
        addPlayerButton.visibility = if (canEdit && players.size < 2) View.VISIBLE else View.GONE
        kotlin.repeat(root.childCount - 1) { root.removeViewAt(0) }
        players.forEach { addPlayerView(it) }
    }

    private fun addPlayerView(player: CutThroatPlayer) {
        val view = CutThroatPlayerView(context)
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
