package com.instructure.teacher.view

import android.content.Context
import android.util.AttributeSet
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.empty_view.view.*
import kotlinx.android.synthetic.main.loading_lame.view.*

class EmptyInboxView@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : EmptyPandaView(context, attrs, defStyleAttr) {

    override val viewId: Int
        get() = R.layout.empty_inbox_view

}
