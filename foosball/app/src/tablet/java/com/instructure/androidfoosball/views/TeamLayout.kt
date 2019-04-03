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
import android.widget.FrameLayout
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.ITeam
import com.instructure.androidfoosball.ktmodels.Team
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.setAvatar
import com.instructure.androidfoosball.utils.setVisible
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.tablet.view_team_layout.view.*
import org.jetbrains.anko.sdk21.listeners.onClick

class TeamLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleRes) {

    var team: ITeam = Team()
        set(value) {
            field = value; refresh()
        }

    private var canEdit: Boolean = true
    private val rect = RectF()
    private var radius = 0f
    private var isVertical = false

    var onAddPlayerClicked: () -> Unit = {}
    var onAddTeamClicked: () -> Unit = {}
    var onTeamChanged: () -> Unit = {}

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }

    init {
        setWillNotDraw(false)
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TeamLayout)
            canEdit = a.getBoolean(R.styleable.TeamLayout_canEdit, canEdit)
            backgroundPaint.color = a.getColor(R.styleable.TeamLayout_teamColor, Color.GRAY)
            isVertical = a.getBoolean(R.styleable.TeamLayout_isVertical, false)
            a.recycle()
        }
        View.inflate(context, if (isVertical) R.layout.view_team_layout_vertical else R.layout.view_team_layout, this)
        addPlayerButton.onClick { onAddPlayerClicked() }
        addTeamButton.onClick { onAddTeamClicked() }
        refresh()
        if (isInEditMode) {
            avatar1.visibility = View.VISIBLE
            avatar2.visibility = View.VISIBLE
            avatar1.setImageResource(R.drawable.sadpanda)
            avatar2.setImageResource(R.drawable.sadpanda)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        super.onDraw(canvas)
    }

    fun addUser(user: User): Boolean {
        if (team.users.contains(user) || team.users.size >= 2) return false
        team.users.add(user)
        refresh()
        onTeamChanged()
        return true
    }

    fun removeUser(user: User): Boolean {
        if (!team.users.contains(user)) return false
        team.users.remove(user)
        refresh()
        onTeamChanged()
        return true
    }

    fun hasUsers() = team.users.isNotEmpty()

    fun setCanEdit(value: Boolean) {
        canEdit = value
        refresh()
    }

    fun setTeamColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    private fun refresh() {
        addTeamButton.setVisible(canEdit && team.users.isEmpty())
        addPlayerButton.visibility = if (canEdit && team.users.size < 2) View.VISIBLE else View.GONE
        attemptPlayerAssignment(0, avatar1, removeAvatar1)
        attemptPlayerAssignment(1, avatar2, removeAvatar2)
    }

    private fun attemptPlayerAssignment(idx: Int, avatar: CircleImageView, removeView: View) {
        if (team.users.size > idx) {
            avatar.visibility = View.VISIBLE
            avatar.setAvatar(team.users[idx], resources.getDimension(R.dimen.avatar_size_large).toInt())
            if (canEdit) {
                removeView.visibility = View.VISIBLE
                removeView.setOnClickListener {
                    team.users.remove(team.users[idx])
                    refresh()
                    onTeamChanged()
                }
            }
        } else {
            avatar.visibility = View.GONE
            removeView.visibility = View.GONE
        }
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
