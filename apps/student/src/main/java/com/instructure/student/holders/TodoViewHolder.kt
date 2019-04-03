package com.instructure.student.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.instructure.student.R

import androidx.recyclerview.widget.RecyclerView

class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var title: TextView = itemView.findViewById(R.id.title)
    var description: TextView = itemView.findViewById(R.id.description)
    var course: TextView = itemView.findViewById(R.id.course)
    var icon: ImageView = itemView.findViewById(R.id.icon)

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_todo
    }
}
