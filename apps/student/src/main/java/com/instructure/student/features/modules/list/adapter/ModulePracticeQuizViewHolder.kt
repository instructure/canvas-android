package com.instructure.student.features.modules.list.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderModuleBinding
import com.instructure.student.databinding.ViewholderModulePracticeQuizBinding

class ModulePracticeQuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        moduleObject: ModuleObject,
        moduleItem: ModuleItem,
        context: Context,
        adapterToFragmentCallback: ModuleAdapterToFragmentCallback?,
        courseColor: Int,
        b: Boolean,
        b1: Boolean,
        restrictQuantitativeData: Boolean?
    ) = with(ViewholderModulePracticeQuizBinding.bind(itemView)) {
        val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.ic_ai, courseColor)
        icon.setImageDrawable(drawable)
        root.setOnClickListener {
            adapterToFragmentCallback?.onRowClicked(moduleObject, moduleItem, adapterPosition, true)
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_module_practice_quiz
    }
}