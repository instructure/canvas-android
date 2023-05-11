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
package com.emeritus.student.holders

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R
import kotlinx.android.synthetic.main.viewholder_header_people.view.*

class PeopleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var isExpanded = false

    fun <MODEL> bind(
        genericHeader: MODEL,
        headerText: String?,
        expanded: Boolean,
        viewHolderHeaderClicked: ViewHolderHeaderClicked<MODEL>
    ) = with(itemView) {
        title.text = headerText
        isExpanded = expanded
        expand_collapse.rotation = if (isExpanded) 180f else 0f
        divider.setVisible(!isExpanded)
        rootView.setOnClickListener { v ->
            viewHolderHeaderClicked.viewClicked(v, genericHeader)
            val animationType: Int
            if (isExpanded) {
                animationType = R.animator.rotation_from_neg90_to_0
            } else {
                animationType = R.animator.rotation_from_0_to_neg90
                divider.setInvisible()
            }
            isExpanded = !isExpanded

            val flipAnimator = AnimatorInflater.loadAnimator(v.context, animationType) as ObjectAnimator
            flipAnimator.target = expand_collapse
            flipAnimator.duration = 200
            flipAnimator.start()

            // Make the dividers visible/invisible after the animation
            flipAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    if (!isExpanded) divider.setVisible()
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_header_people
    }
}
