/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.student.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.instructure.pandarecycler.interfaces.EmptyInterface
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.EmptyCoursesViewBinding

class EmptyCoursesView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), EmptyInterface {

    private val binding: EmptyCoursesViewBinding

    private var noConnectionText: String? = null
    private var isDisplayNoConnection = false

    init {
        binding = EmptyCoursesViewBinding.inflate(LayoutInflater.from(getContext()), this, true)
    }

    override fun setLoading() = with(binding) {
        image.setGone()
        textViews.setGone()
        addCoursesButton.setGone()
        loading.setVisible()
        loading.announceForAccessibility(context.getString(R.string.loading))
    }

    override fun setDisplayNoConnection(isNoConnection: Boolean) {
        isDisplayNoConnection = isNoConnection
    }

    override fun setListEmpty() {
        if (isDisplayNoConnection) {
            binding.noConnectionView.text = noConnectionText
        }

        binding.loading.setGone()
        binding.image.setVisible()
        binding.textViews.setVisible()
        binding.addCoursesButton.setVisible()
    }

    override fun setTitleText(s: String) {}

    override fun setTitleText(sResId: Int) {}

    override fun setMessageText(s: String) {}

    override fun setMessageText(sResId: Int) {}

    override fun setNoConnectionText(s: String) {
        noConnectionText = s
        binding.noConnectionView.text = s
    }

    override fun getEmptyViewImage(): ImageView? {
        return null
    }

    override fun setEmptyViewImage(drawable: Drawable) {}

    override fun emptyViewText(s: String) {
        setTitleText(s)
    }

    override fun emptyViewText(sResId: Int) {
        setTitleText(sResId)
    }

    override fun emptyViewImage(drawable: Drawable) {
        setEmptyViewImage(drawable)
    }

    fun onClickAddCourses(onClick: () -> Unit) {
        binding.addCoursesButton.apply {
            ViewStyler.themeButton(this)
            setOnClickListener { onClick() }
        }
    }

    fun setGuidelines(imTop: Float, imBottom: Float, tiTop: Float, buTop: Float, txLeft: Float, txRight: Float) = with(binding) {
        val iTop = imageTop.layoutParams as ConstraintLayout.LayoutParams
        iTop.guidePercent = imTop
        imageTop.layoutParams = iTop
        val iBottom = imageBottom.layoutParams as ConstraintLayout.LayoutParams
        iBottom.guidePercent = imBottom
        imageBottom.layoutParams = iBottom
        val tTop = titleTop.layoutParams as ConstraintLayout.LayoutParams
        tTop.guidePercent = tiTop
        titleTop.layoutParams = tTop
        val bTop = buttonTop.layoutParams as ConstraintLayout.LayoutParams
        bTop.guidePercent = buTop
        buttonTop.layoutParams = bTop
        val tLeft = textLeft.layoutParams as ConstraintLayout.LayoutParams
        tLeft.guidePercent = txLeft
        textLeft.layoutParams = tLeft
        val tRight = textRight.layoutParams as ConstraintLayout.LayoutParams
        tRight.guidePercent = txRight
        textRight.layoutParams = tRight
    }
}
