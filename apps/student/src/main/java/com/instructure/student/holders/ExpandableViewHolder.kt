/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.holders

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandautils.utils.hasSpokenFeedback
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderHeaderExpandableBinding

class ExpandableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var isExpanded = false

    fun <MODEL> bind(
        context: Context,
        genericHeader: MODEL,
        headerText: String?,
        expanded: Boolean,
        viewHolderHeaderClicked: ViewHolderHeaderClicked<MODEL>
    ) = with(ViewholderHeaderExpandableBinding.bind(itemView)) {
        title.text = headerText
        isExpanded = expanded
        expandCollapse.rotation = if (expanded) 180f else 0f
        val a11yManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        // Expand/collapse is disabled when TalkBack is enabled, so we prevent TalkBack announcing that functionality by not adding a click listener
        if (!a11yManager.hasSpokenFeedback) {
            root.setOnClickListener { v ->
                viewHolderHeaderClicked.viewClicked(v, genericHeader)
                val animationType: Int = if (isExpanded) {
                    R.animator.rotation_from_neg90_to_0
                } else {
                    R.animator.rotation_from_0_to_neg90
                }
                isExpanded = !isExpanded
                val flipAnimator = AnimatorInflater.loadAnimator(v.context, animationType) as ObjectAnimator
                flipAnimator.target = expandCollapse
                flipAnimator.duration = 200
                flipAnimator.start()
            }

            root.accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.Button"
                }
            }
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_header_expandable
    }
}
