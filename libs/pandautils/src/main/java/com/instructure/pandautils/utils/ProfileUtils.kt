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

package com.instructure.pandautils.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.R
import java.util.Locale


object ProfileUtils {

    private val noPictureUrls = listOf(
            "images/dotted_pic.png",
            "images%2Fmessages%2Favatar-50.png",
            "images/messages/avatar-50.png",
            "images/messages/avatar-group-50.png"
    )

    fun shouldLoadAltAvatarImage(avatarUrl: String?): Boolean {
        return avatarUrl.isNullOrBlank() || avatarUrl.indexOfAny(noPictureUrls) >= 0
    }

    fun getUserInitials(username: String?): String {
        val name: String = username.takeUnless { it.isNullOrBlank() } ?: return "?"
        val initials = name.trim().split(Regex("\\s+")).map { it.uppercase(Locale.getDefault())[0] }
        return if (initials.size == 2) {
            initials.joinToString("")
        } else {
            initials[0].toString()
        }
    }

    fun loadAvatarForUser(
        imageView: ImageView,
        name: String?,
        url: String?,
        @ColorInt avatarTint: Int? = null,
        @Dimension altAvatarBorderWidth: Int = imageView.context.resources.getDimension(R.dimen.avatar_border_width_thin).toInt()
    ) {
        if (shouldLoadAltAvatarImage(url)) {
            imageView.setImageDrawable(createAvatarDrawable(imageView.context, name.orEmpty(), altAvatarBorderWidth, avatarTint))
        } else {
            imageView.loadCircularImage(url, R.drawable.recipient_avatar_placeholder) {
                imageView.setImageDrawable(createAvatarDrawable(imageView.context, name.orEmpty(), altAvatarBorderWidth))
            }
        }
    }

    fun createAvatarDrawable(context: Context, userName: String, @Dimension borderWidth: Int, @ColorInt backgroundColor: Int? = null): Drawable {
        val initials = getUserInitials(userName)
        val color = ContextCompat.getColor(context, R.color.textDark)
        return TextDrawable.builder()
            .beginConfig()
            .height(context.resources.getDimensionPixelSize(R.dimen.avatar_size))
            .width(context.resources.getDimensionPixelSize(R.dimen.avatar_size))
            .toUpperCase()
            .useFont(Typeface.DEFAULT_BOLD)
            .textColor(color)
            .withBorder(borderWidth)
            .withBorderColor(color)
            .endConfig()
            .buildRound(initials, backgroundColor ?: Color.WHITE)
    }

    /**
     * Sets up the provided [avatar] for the given [conversation].
     *
     * If there are either one or two participants in the conversation, the profile image for the first participant
     * will be loaded into the the avatar view. The [onClick] callback may be provided to take action when the
     * avatar is clicked.
     *
     * If [onClick] is non-null then accessibility behavior will be set up on the avatar as well. As such, it is
     * important to provide this callback only when tapping on the avatar would lead to an actionable outcome. For
     * example, in the Teacher app a teacher can view a student's summary, but only in the context of a course. In this
     * case a callback should not be provided if there is no course associated with the conversation.
     *
     * If there are more than two participants, a group avatar will be shown and accessibility will be disabled.
     */
    fun configureAvatarForConversation(
        avatar: ImageView,
        conversation: Conversation,
        onClick: ((BasicUser) -> Unit)? = null
    ) {
        // Clear click listener and A11y
        avatar.setOnClickListener(null)
        avatar.clearAvatarA11y()
        avatar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

        val users = conversation.participants

        when (users.size) {
            0 -> return
            1, 2 -> {
                val firstUser = users[0]
                firstUser.avatarUrl = conversation.avatarUrl
                loadAvatarForUser(avatar, firstUser.name, firstUser.avatarUrl)

                // Set click listener and A11y
                if (onClick != null) {
                    avatar.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    avatar.setupAvatarA11y(firstUser.name)
                    avatar.onClick { onClick(firstUser) }
                }
            }
            else -> avatar.setImageResource(R.drawable.ic_group)
        }
    }

    fun getInitialsAvatarDrawable(
        context: Context,
        username: String,
        backgroundColor: Int = Color.WHITE,
        textColor: Int = Color.GRAY,
        borderColor: Int = Color.GRAY
    ): Drawable {
        return BitmapDrawable(
            context.resources,
            getInitialsAvatarBitMap(context, username, backgroundColor, textColor, borderColor)
        )
    }

    @JvmOverloads
    fun getInitialsAvatarBitMap(
            context: Context,
            username: String,
            backgroundColor: Int = Color.WHITE,
            textColor: Int = Color.GRAY,
            borderColor: Int = Color.GRAY
    ): Bitmap {
        val initials = ProfileUtils.getUserInitials(username)
        val drawable = TextDrawable.builder()
                .beginConfig()
                .height(context.resources.getDimensionPixelSize(R.dimen.avatar_size))
                .width(context.resources.getDimensionPixelSize(R.dimen.avatar_size))
                .toUpperCase()
                .textColor(textColor)
                .useFont(Typeface.DEFAULT_BOLD)
                .withBorderColor(borderColor)
                .withBorder(context.DP(1).toInt())
                .endConfig()
                .buildRound(initials, backgroundColor)

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }
}
