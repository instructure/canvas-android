package com.instructure.student.features.modules.list.adapter

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderHeaderModuleBinding
import com.instructure.student.features.modules.util.ModuleUtility

class ModuleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var isExpanded: Boolean = false

    fun bind(
        moduleObject: ModuleObject,
        context: Context,
        viewHolderHeaderClicked: ViewHolderHeaderClicked<ModuleObject>,
        expanded: Boolean
    ) = with(ViewholderHeaderModuleBinding.bind(itemView)) {
        val isLocked = ModuleUtility.isGroupLocked(moduleObject)
        isExpanded = expanded
        expandCollapse.rotation = if (isExpanded) 180f else 0f
        divider.setVisible(!isExpanded)
        val color = ContextCompat.getColor(context, R.color.textDark)
        root.setOnClickListener { v ->
            viewHolderHeaderClicked.viewClicked(v, moduleObject)
            val animationType: Int
            if (isExpanded) {
                animationType = R.animator.rotation_from_neg90_to_0
            } else {
                animationType = R.animator.rotation_from_0_to_neg90
                divider.setGone()
            }
            isExpanded = !isExpanded
            val flipAnimator = AnimatorInflater.loadAnimator(
                v.context,
                animationType
            ) as ObjectAnimator
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
                moduleObject.state.equals(
                    ModuleObject.State.Locked.apiString,
                    ignoreCase = true
                ) -> R.drawable.ic_lock
                moduleObject.state.equals(
                    ModuleObject.State.Completed.apiString,
                    ignoreCase = true
                ) -> R.drawable.ic_check_white_24dp
                else -> R.drawable.ic_module_circle
            }
        } else {
            if (isLocked) R.drawable.ic_lock else R.drawable.ic_module_circle
        }
        moduleStatus.setImageDrawable(
            ColorUtils.colorIt(
                color,
                ContextCompat.getDrawable(context, drawable)!!
            )
        )
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_header_module
    }
}