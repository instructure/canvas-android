/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.teacher.holders

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ViewholderRecipientBinding
import com.instructure.teacher.interfaces.RecipientAdapterCallback

class RecipientViewHolder(private val binding: ViewholderRecipientBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val SELECTION_TRANSPARENCY_MASK = 0x08FFFFFF
    }

    fun bind(
        context: Context,
        holder: RecipientViewHolder,
        recipient: Recipient,
        adapterCallback: RecipientAdapterCallback,
        isSelected: Boolean
    ) = with(binding) {
        fun setChecked(isChecked: Boolean = true) {
            if (isChecked) {
                val selectionColor = context.getColor(R.color.backgroundInfo)
                root.setBackgroundColor(selectionColor and SELECTION_TRANSPARENCY_MASK)
                avatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle)?.apply {
                    mutate().setTintList(ColorStateList.valueOf(selectionColor))
                })
                checkMarkImageView.setVisible()
                ColorUtils.colorIt(Color.WHITE, checkMarkImageView)
            } else {
                root.setBackgroundColor(Color.TRANSPARENT)
                checkMarkImageView.setGone()
            }
        }

        setChecked(false)

        // Clear checkbox listener so we don't trigger unwanted events as we recycle
        checkBox.setOnCheckedChangeListener(null)

        // Set recipient name
        title.text = Pronouns.span(recipient.name, recipient.pronouns)

        // Show user count if group, load avatars
        if (recipient.recipientType == Recipient.Type.Group) {
            checkBox.setVisible()
            if (isSelected) {
                setChecked(true)
            } else {
                ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
            }

            userCount.setVisible()
            userCount.text = context.resources.getQuantityString(R.plurals.people_count, recipient.userCount, recipient.userCount)
        } else if (recipient.recipientType == Recipient.Type.Metagroup) {
            checkBox.setGone()

            ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)

            userCount.setVisible()
            userCount.text = context.resources.getQuantityString(R.plurals.group_count, recipient.itemCount, recipient.itemCount)
        } else {
            checkBox.setGone()
            userCount.setGone()
            if (isSelected) {
                setChecked(true)
            } else {
                userCount.text = ""
                ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
            }
        }

        // Set checked if recipient is selected
        checkBox.isChecked = isSelected
        ViewStyler.themeCheckBox(context, checkBox, ThemePrefs.brandColor)
        title.contentDescription = if (isSelected) context.getString(R.string.selectedListItem, recipient.name) else recipient.name

        // Set whole item listener
        itemView.setOnClickListener { adapterCallback.onRowClicked(recipient, holder.adapterPosition, false) }

        // Set checkbox listener
        checkBox.setOnCheckedChangeListener { _, _ ->
            adapterCallback.onRowClicked(recipient, holder.adapterPosition, true)
        }
    }
}
