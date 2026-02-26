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
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.EmptyViewBinding
import com.instructure.pandautils.utils.isGone
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible

class EmptyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EmptyInterface {

    private val binding: EmptyViewBinding

    private var noConnectionText: String? = null
    private var titleText: String? = null
    private var messageText: String? = null
    private var isDisplayNoConnection = false

    init {
        binding = EmptyViewBinding.inflate(LayoutInflater.from(context), this, true)
        // Initialize with current orientation
        post {
            handleConfigChange(context.resources.getBoolean(R.bool.isDeviceTablet), context.resources.configuration.orientation)
        }
    }

    override fun setLoading() = with(binding) {
        title.setGone()
        message.setGone()
        image.setGone()
        loading.root.announceForAccessibility(context.getString(R.string.loading))
        loading.root.visibility = View.VISIBLE
    }

    override fun setLoadingWithAnimation(titleRes: Int, messageRes: Int, animationRes: Int): Unit = with(binding) {
        setTitleText(titleRes)
        setMessageText(messageRes)
        animationView.setAnimation(animationRes)
        title.setVisible()
        message.setVisible()
        animationView.setVisible()
    }

    override fun setDisplayNoConnection(isNoConnection: Boolean) {
        isDisplayNoConnection = isNoConnection
    }

    override fun setListEmpty() = with(binding) {
        if (isDisplayNoConnection) {
            loading.noConnection.text = noConnectionText
        } else {
            title.text = titleText
            message.text = messageText
        }
        title.setVisible()
        message.setVisible()
        loading.root.setGone()
        image.setVisible(image.drawable != null)
        // we don't have an image for the empty state we want the title to be centered instead.
        if (image.isGone) {
            centerTitle()
        } else {
            resetTitle()
        }
    }

    private fun resetTitle() {
        binding.emptyViewLayout.let {
            val constraintSet = ConstraintSet()
            constraintSet.clone(it)
            constraintSet.connect(R.id.textViews, ConstraintSet.TOP, R.id.titleTop, ConstraintSet.BOTTOM)
            constraintSet.clear(R.id.textViews, ConstraintSet.BOTTOM)
            constraintSet.applyTo(it)
        }
    }

    private fun centerTitle() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.emptyViewLayout)
        constraintSet.connect(R.id.textViews, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(R.id.textViews, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.applyTo(binding.emptyViewLayout)
    }

    fun getTitle(): TextView {
        return binding.title
    }

    override fun setTitleText(s: String) {
        titleText = s
        binding.title.text = titleText
    }

    override fun setTitleText(sResId: Int) {
        titleText = context.resources.getString(sResId)
        binding.title.text = titleText
    }

    fun getMessage(): TextView {
        return binding.message
    }

    override fun setMessageText(s: String) {
        messageText = s
        binding.message.text = messageText
    }

    override fun setMessageText(sResId: Int) {
        messageText = context.resources.getString(sResId)
        binding.message.text = messageText
    }

    override fun setNoConnectionText(s: String) {
        noConnectionText = s
        binding.loading.noConnection.text = noConnectionText
    }

    override fun getEmptyViewImage(): ImageView? = binding.image

    override fun setEmptyViewImage(drawable: Drawable) {
        binding.image.setImageDrawable(drawable)
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

    fun setImageVisible(visible: Boolean) = with(binding) {
        when (visible) {
            true -> image.setVisible()
            false -> image.setGone()
        }
    }

    fun setError(errorMessage: String, @DrawableRes errorImage: Int?) = with(binding) {
        title.setVisible()
        if (errorImage != null) {
            image.setImageResource(errorImage)
            image.setVisible()
        } else {
            centerTitle()
            image.setGone()
        }
        loading.root.setGone()
        titleText = errorMessage
        messageText = ""

        title.text = titleText
    }

    fun handleConfigChange(isTablet: Boolean, orientation: Int) {
        changeTextSize()
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isTablet) {
                setGuidelines(.24f, .53f, .62f, .12f, .88f)
            } else {
                setGuidelines(.28f, .6f, .73f, .12f, .88f)

            }
        } else {
            if (isTablet) {
                //change nothing, at least for now
            } else {
                setGuidelines(.25f, .7f, .74f, .15f, .85f)
            }
        }
    }

    fun changeTextSize(isCalendar: Boolean = false) = with(binding) {
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

    fun setGuidelines(imTop: Float, imBottom: Float, tiTop: Float, txLeft: Float, txRight: Float) = with(binding) {
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
