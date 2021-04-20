package com.instructure.teacher.view

import android.content.Context
import android.util.AttributeSet
import com.instructure.pandautils.views.EmptyView
import com.instructure.teacher.R

class EmptyInboxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : EmptyView(context, attrs) {

    override val viewId: Int
        get() = R.layout.empty_inbox_view

}
