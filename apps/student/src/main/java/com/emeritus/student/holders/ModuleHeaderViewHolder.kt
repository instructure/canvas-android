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
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import com.emeritus.student.util.ModuleUtility
import kotlinx.android.synthetic.main.viewholder_header_module.view.*

class ModuleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var isExpanded: Boolean = false

    fun bind(
        moduleObject: ModuleObject,
        context: Context,
        viewHolderHeaderClicked: ViewHolderHeaderClicked<ModuleObject>,
        expanded: Boolean
    ) = with(itemView){
        val isLocked = ModuleUtility.isGroupLocked(moduleObject)
        isExpanded = expanded
        expandCollapse.rotation = if (isExpanded) 180f else 0f
        divider.setVisible(!isExpanded)
        val color = ContextCompat.getColor(context, R.color.textDark)
        itemView.setOnClickListener { v ->
            viewHolderHeaderClicked.viewClicked(v, moduleObject)
            val animationType: Int
            if (isExpanded) {
                animationType = R.animator.rotation_from_neg90_to_0
            } else {
                animationType = R.animator.rotation_from_0_to_neg90
                divider.setGone()
            }
            isExpanded = !isExpanded
            val flipAnimator = AnimatorInflater.loadAnimator(v.context, animationType) as ObjectAnimator
            flipAnimator.target = expandCollapse
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
        title.text = moduleObject.name

        // Reset the status text and drawable to default state
        val drawable: Int = if (moduleObject.state != null) {
            when {
                moduleObject.state.equals(ModuleObject.State.Locked.apiString, ignoreCase = true) -> R.drawable.ic_lock
                moduleObject.state.equals(ModuleObject.State.Completed.apiString, ignoreCase = true) -> R.drawable.ic_check_white_24dp
                else -> R.drawable.ic_module_circle
            }
        } else {
            if (isLocked) R.drawable.ic_lock else R.drawable.ic_module_circle
        }
        moduleStatus.setImageDrawable(ColorUtils.colorIt(color, ContextCompat.getDrawable(context, drawable)!!))
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_header_module
    }
}
