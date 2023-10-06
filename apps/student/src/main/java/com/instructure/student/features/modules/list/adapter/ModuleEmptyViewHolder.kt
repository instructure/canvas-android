package com.instructure.student.features.modules.list.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderModuleEmptyBinding

class ModuleEmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(text: String?) = with(ViewholderModuleEmptyBinding.bind(itemView)){
        titleText.text = text
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_module_empty
    }
}