/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
 */
package com.instructure.pandautils.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.isGone
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import kotlinx.android.synthetic.main.empty_view.view.*
import kotlinx.android.synthetic.main.loading_lame.view.*

open class EmptyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EmptyInterface {

    open val viewId: Int = R.layout.empty_view

    private var noConnectionText: String? = null
    private var titleText: String? = null
    private var messageText: String? = null
    private var isDisplayNoConnection = false

    init {
        View.inflate(context, viewId, this)
    }

    override fun setLoading() {
        title.setGone()
        message.setGone()
        image.setGone()
        loading.announceForAccessibility(context.getString(R.string.loading))
        loading.setVisible()
    }

    override fun setDisplayNoConnection(isNoConnection: Boolean) {
        isDisplayNoConnection = isNoConnection
    }

    override fun setListEmpty() {
        if (isDisplayNoConnection) {
            noConnection.text = noConnectionText
        } else {
            title.text = titleText
            message.text = messageText
        }
        title.setVisible()
        message.setVisible()
        loading.setGone()
        image.setVisible(image.drawable != null)
        // we don't have an image for the empty state we want the title to be centered instead.
        if (image.isGone) {
            centerTitle()
        } else {
            resetTitle()
        }
    }

    private fun resetTitle() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(emptyViewLayout)
        constraintSet.connect(R.id.textViews, ConstraintSet.TOP, R.id.titleTop, ConstraintSet.BOTTOM)
        constraintSet.clear(R.id.textViews, ConstraintSet.BOTTOM)
        constraintSet.applyTo(emptyViewLayout)
    }

    private fun centerTitle() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(emptyViewLayout)
        constraintSet.connect(R.id.textViews, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(R.id.textViews, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.applyTo(emptyViewLayout)
    }

    fun getTitle(): TextView {
        return title
    }

    override fun setTitleText(s: String) {
        titleText = s
        title.text = titleText
    }

    override fun setTitleText(sResId: Int) {
        titleText = context.resources.getString(sResId)
        title.text = titleText
    }

    fun getMessage(): TextView {
        return message
    }

    override fun setMessageText(s: String) {
        messageText = s
        message.text = messageText
    }

    override fun setMessageText(sResId: Int) {
        messageText = context.resources.getString(sResId)
        message.text = messageText
    }

    override fun setNoConnectionText(s: String) {
        noConnectionText = s
        noConnection.text = noConnectionText
    }

    override fun getEmptyViewImage(): ImageView? = image

    override fun setEmptyViewImage(drawable: Drawable) {
        image.setImageDrawable(drawable)
    }

    override fun emptyViewText(s: String) {
        setTitleText(s)
    }

    override fun emptyViewText(sResId: Int) {
        setTitleText(sResId)
    }

    override fun emptyViewImage(drawable: Drawable) {
        setEmptyViewImage(drawable)
    }

    fun setEmptyViewImage(dResId: Int) {
        ContextCompat.getDrawable(context, dResId)?.let { setEmptyViewImage(it) }
    }

    fun setImageVisible(visible: Boolean) {
        when (visible) {
            true -> image.setVisible()
            false -> image.setGone()
        }
    }

    fun setError(errorMessage: String) {
        title.setVisible()
        image.setGone()
        loading.setGone()
        centerTitle()
        titleText = errorMessage
        messageText = ""

        title.text = titleText
    }

    fun changeTextSize(isCalendar: Boolean = false) {
        if (isCalendar) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            } else {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
            }
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
    }

    fun setGuidelines(imTop: Float, imBottom: Float, tiTop: Float, txLeft: Float, txRight: Float) {
        val iTop = imageTop.layoutParams as ConstraintLayout.LayoutParams
        iTop.guidePercent = imTop
        imageTop.layoutParams = iTop
        val iBottom = imageBottom.layoutParams as ConstraintLayout.LayoutParams
        iBottom.guidePercent = imBottom
        imageBottom.layoutParams = iBottom
        val tTop = titleTop.layoutParams as ConstraintLayout.LayoutParams
        tTop.guidePercent = tiTop
        titleTop.layoutParams = tTop
        val tLeft = textLeft.layoutParams as ConstraintLayout.LayoutParams
        tLeft.guidePercent = txLeft
        textLeft.layoutParams = tLeft
        val tRight = textRight.layoutParams as ConstraintLayout.LayoutParams
        tRight.guidePercent = txRight
        textRight.layoutParams = tRight
    }
}
