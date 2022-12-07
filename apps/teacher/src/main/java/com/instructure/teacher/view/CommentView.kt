/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.view_comment.view.*


enum class CommentDirection { INCOMING, OUTGOING }

class CommentView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** The direction (incoming or outgoing) of this comment item */
    var direction = CommentDirection.INCOMING
        set(value) {
            field = value
            val deviceRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            layoutDirection = if (deviceRtl xor (value == CommentDirection.OUTGOING)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            extrasContainer.layoutDirection = resources.configuration.layoutDirection
        }

    fun setCommentBubbleColor(@ColorInt color: Int) = commentTextView.setBubbleColor(color)

    fun setAvatar(avatarUrl: String?, userName: String) {
        ProfileUtils.loadAvatarForUser(avatarView, userName, avatarUrl.orEmpty())
    }

    var commentTextColor: Int
        get() = commentTextView.textColors.defaultColor
        set(value) = commentTextView.setTextColor(value)

    var commentText: String?
        get() = commentTextView.text.toString()
        set(value) {
            if (value.isNullOrBlank()) commentTextView.setGone() else commentTextView.setVisible().text = value
        }

    var usernameText: CharSequence?
        get() = userNameTextView.text
        set(value) {
            if (value == null) userNameTextView.setGone() else userNameTextView.setVisible().text = value
        }

    var dateText: String?
        get() = commentDateTextView.text.toString()
        set(value) {
            if (value == null) commentDateTextView.setGone() else commentDateTextView.setVisible().text = value
        }

    fun setExtraView(view: View?) {
        extrasContainer.removeAllViews()
        view?.let { extrasContainer.addView(it) }
    }

    init {
        inflate(context, R.layout.view_comment, this)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CommentView)
            (0 until a.indexCount).map { a.getIndex(it) }.forEach {
                when (it) {
                    R.styleable.CommentView_comment_bubbleColor -> setCommentBubbleColor(a.getColor(it, Color.GRAY))
                    R.styleable.CommentView_comment_direction -> direction = CommentDirection.values()[a.getInt(it, 0)]
                    R.styleable.CommentView_comment_textColor -> commentTextColor = a.getColor(it, Color.WHITE)
                    R.styleable.CommentView_comment_previewAvatar -> if (isInEditMode) avatarView.setImageResource(a.getResourceId(it, 0))
                    R.styleable.CommentView_comment_previewName -> if (isInEditMode) usernameText = a.getString(it)
                    R.styleable.CommentView_comment_previewText -> if (isInEditMode) commentText = a.getString(it)
                }
            }
            a.recycle()
        } else {
            direction = CommentDirection.INCOMING
        }
    }
}
