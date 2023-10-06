package com.instructure.student.features.modules.list.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderSubHeaderModuleBinding
import com.instructure.student.util.BinderUtils

class ModuleSubHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(moduleItem: ModuleItem, isFirstItem: Boolean, isLastItem: Boolean) = with(
        ViewholderSubHeaderModuleBinding.bind(itemView)
    ) {
        if (ModuleItem.Type.SubHeader.toString().equals(moduleItem.type, ignoreCase = true)) {
            subTitle.text = moduleItem.title
        }
        BinderUtils.updateShadows(isFirstItem, isLastItem, shadowTop, shadowBottom)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_sub_header_module
    }
}