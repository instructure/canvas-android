/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.util

import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.*
import android.widget.CheckedTextView
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.student.R
import java.util.*

class UploadCheckboxManager(private val listener: OnOptionCheckedListener, private val selectionIndicator: View) {
    interface OnOptionCheckedListener {
        fun onUserFilesSelected()
        fun onAssignmentFilesSelected()
    }

    private var checkBoxes: MutableList<CheckedTextView> = ArrayList()

    var selectedCheckBox: CheckedTextView? = null
        private set

    private var isAnimating = false

    fun add(checkBox: CheckedTextView) {
        if (checkBoxes.size == 0) {
            selectedCheckBox = checkBox
            setInitialIndicatorHeight()
        }
        checkBoxes.add(checkBox)
        checkBox.setOnClickListener(destinationClickListener)
    }

    val selectedType: UploadFilesDialog.FileUploadType
        get() = when (selectedCheckBox?.id) {
            R.id.myFilesCheckBox -> UploadFilesDialog.FileUploadType.USER
            R.id.assignmentCheckBox -> UploadFilesDialog.FileUploadType.ASSIGNMENT
            else -> UploadFilesDialog.FileUploadType.USER
        }

    private fun setInitialIndicatorHeight() {
        selectionIndicator.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    selectionIndicator.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (selectedCheckBox != null) {
                        selectionIndicator.layoutParams.height = (selectedCheckBox!!.parent as View).height
                        selectionIndicator.layoutParams = selectionIndicator.layoutParams
                    }
                    listener.onUserFilesSelected()
                }
            }
        )
    }

    private fun moveIndicator(newCurrentCheckBox: CheckedTextView) {
        val moveAnimation: Animation = getAnimation(newCurrentCheckBox)
        selectionIndicator.startAnimation(moveAnimation)
        moveAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isAnimating = true
            }

            override fun onAnimationEnd(animation: Animation) {
                selectedCheckBox = newCurrentCheckBox
                isAnimating = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun getAnimation(toCheckBox: CheckedTextView): AnimationSet {
        val toView = toCheckBox.parent as View
        val fromView = selectedCheckBox!!.parent as View

        // get ratio between current height and new height
        val toRatio =
            toView.height.toFloat() / selectionIndicator.height.toFloat()
        val fromRatio =
            fromView.height.toFloat() / selectionIndicator.height.toFloat()
        val scaleAnimation = ScaleAnimation(
            1f,  // fromXType
            1f,  // toX
            fromRatio,  // fromY
            toRatio,  // toY
            .5f,  // pivotX
            0.0f
        ) // pivotY
        val translateAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,  // fromXType, fromXValue
            Animation.RELATIVE_TO_SELF, 0.0f,  // toXType, toXValue
            Animation.ABSOLUTE, fromView.top.toFloat(),  // fromYType, fromYValue
            Animation.ABSOLUTE, toView.top.toFloat()
        ) // toYTyp\e, toYValue
        translateAnimation.interpolator = AccelerateDecelerateInterpolator()
        translateAnimation.fillAfter = true
        val animSet = AnimationSet(true)
        animSet.addAnimation(scaleAnimation)
        animSet.addAnimation(translateAnimation)
        animSet.fillAfter = true
        animSet.duration = 200
        return animSet
    }

    private val destinationClickListener = View.OnClickListener { v: View ->
        if (isAnimating) return@OnClickListener
        val checkedTextView = v as CheckedTextView
        if (!checkedTextView.isChecked) {
            checkedTextView.isChecked = true
            notifyListener(checkedTextView)
            moveIndicator(checkedTextView)
            for (checkBox in checkBoxes) {
                if (checkBox.id != checkedTextView.id) {
                    checkBox.isChecked = false
                }
            }
        }
    }

    private fun notifyListener(checkedTextView: CheckedTextView) {
        when (checkedTextView.id) {
            R.id.myFilesCheckBox -> listener.onUserFilesSelected()
            R.id.assignmentCheckBox -> listener.onAssignmentFilesSelected()
        }
    }

}
