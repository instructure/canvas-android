/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
 */
package com.emeritus.student.holders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import kotlinx.android.synthetic.main.viewholder_recipient.view.*

class RecipientViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_recipient
    }

    private val selectionTransparencyMask = 0x08FFFFFF

    fun bind(
        context: Context,
        holder: RecipientViewHolder,
        recipient: Recipient,
        adapterCallback: (Recipient, Int, Boolean) -> Unit,
        isSelected: Boolean,
        canMessageAll: Boolean
    ) = with(itemView) {

        fun setChecked(isChecked: Boolean = true) {
            if (isChecked) {
                val selectionColor = context.getColor(R.color.backgroundInfo)
                setBackgroundColor(selectionColor and selectionTransparencyMask)
                avatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle)?.apply {
                    mutate().setTintList(ColorStateList.valueOf(selectionColor))
                })
                checkMarkImageView.setVisible()
                ColorUtils.colorIt(Color.WHITE, checkMarkImageView)
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                checkMarkImageView.setGone()
            }
        }

        setChecked(false)

        // Clear checkbox listener so we don't trigger unwanted events as we recycle
        checkBox.setOnCheckedChangeListener(null)

        // Set recipient name
        title.text = Pronouns.span(recipient.name, recipient.pronouns)

        when (recipient.recipientType) {
        // Show user count if group, load avatars
            Recipient.Type.Group -> {
                checkBox.setVisible(canMessageAll)
                if (isSelected) {
                    setChecked(true)
                } else {
                    ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                }
                userCount.setVisible()
                userCount.text = context.resources.getQuantityString(
                    R.plurals.people_count,
                    recipient.userCount,
                    recipient.userCount
                )
            }
            Recipient.Type.Metagroup -> {
                checkBox.setGone()
                ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                userCount.setVisible()
                userCount.text = context.resources.getQuantityString( R.plurals.group_count, recipient.itemCount, recipient.itemCount)
            }
            else -> {
                checkBox.setGone()
                userCount.setGone()
                if (isSelected) {
                    setChecked(true)
                } else {
                    userCount.text = ""
                    ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                }
            }
        }

        // Set checked if recipient is selected
        checkBox.isChecked = isSelected
        ViewStyler.themeCheckBox(context, checkBox, ThemePrefs.brandColor)
        title.contentDescription = if (isSelected) context.getString(R.string.selectedListItem, recipient.name) else recipient.name

        // Set whole item listener
        itemView.setOnClickListener { adapterCallback(recipient, holder.adapterPosition, false) }

        // Set checkbox listener
        checkBox.setOnCheckedChangeListener { _, _ ->
            adapterCallback(recipient, holder.adapterPosition, true)
        }
    }
}
