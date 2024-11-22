/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view.edit_rubric

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.firstAncestorOrNull
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.topOffsetIn
import com.instructure.teacher.databinding.ViewRubricCriterionItemBinding
import com.instructure.teacher.dialog.CriterionLongDescriptionDialog
import com.instructure.teacher.dialog.EditRubricCommentDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RubricCriterionItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRubricCriterionItemBinding

    /** Criterion ID*/
    private var mCriterionId = ""
    private var mStudentId = -1L
    private var mAssigneeName: String = ""
    private var mAssigneePronouns: String? = null
    private var mIsFreeForm = false

    var gradeAnonymously = false

    init {
        // Inflate
        binding = ViewRubricCriterionItemBinding.inflate(LayoutInflater.from(context), this, true)

        if (!isInEditMode) {
            // Set 'long description' button color to theme button color
            binding.viewLongDescriptionButton.setTextColor(ThemePrefs.textButtonColor)

            // Set 'add comment' button color to theme button color
            binding.addCommentButton.setTextColor(ThemePrefs.textButtonColor)
        }
    }

    /** Populates the view with data from the given [RubricCriterion] */
    fun setCriterion(
        criterion: RubricCriterion,
        studentId: Long,
        assigneeName: String,
        assigneePronouns: String?,
        criterionIdx: Int,
        isFreeForm: Boolean
    ) = with(binding) {
        mCriterionId = criterion.id ?: ""
        mStudentId = studentId
        mAssigneeName = assigneeName
        mAssigneePronouns = assigneePronouns
        mIsFreeForm = isFreeForm
        criterionDescriptionTextView.text = criterion.description

        addCommentButton.setVisible(mIsFreeForm)
        criterionActionSeparator.setVisible(mIsFreeForm)

        if (criterion.longDescription.isNullOrBlank()) {
            criterionActionSeparator.setGone()
            viewLongDescriptionButton.setGone()
        } else {
            viewLongDescriptionButton.onClick {
                (context as? AppCompatActivity)?.supportFragmentManager?.let {
                    CriterionLongDescriptionDialog.show(it, criterion.description ?: "", criterion.longDescription ?: "")
                }
            }
        }
        ratingLayout.setCriterion(criterion, mStudentId, criterionIdx, mIsFreeForm)
    }

    /** Sets the current/working criterion assessment */
    fun setAssessment(assessment: RubricCriterionAssessment) = with(binding) {
        updateComment(assessment.comments)
        addCommentButton.onClick { editComment() }
        editCommentButton.onClick { editComment(commentTextView.text.toString()) }
        ratingLayout.selectValue(assessment.ratingId, assessment.points)
    }

    private fun editComment(default: String = "") {
        (context as? AppCompatActivity)?.supportFragmentManager?.let {
            // Show dialog
            EditRubricCommentDialog.show(
                it,
                mCriterionId,
                mStudentId,
                mAssigneeName,
                mAssigneePronouns,
                gradeAnonymously,
                default
            )

            // Attempt to scroll to the criterion description
            firstAncestorOrNull<ScrollView>()?.let {
                it.smoothScrollTo(0, binding.criterionDescriptionTextView.topOffsetIn(it))
            }
        }
    }

    fun updateComment(comment: String?) = with(binding) {
        if (comment.isNullOrBlank()) {
            criterionCommentContainer.setGone()
            addCommentButton.setVisible(mIsFreeForm)
            criterionActionSeparator.setVisible(mIsFreeForm && viewLongDescriptionButton.visibility == View.VISIBLE)
        } else {
            criterionCommentContainer.setVisible()
            commentTextView.text = comment
            criterionActionSeparator.setGone()
            addCommentButton.setGone()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateComment(event: RubricCommentEditedEvent) {
        if (event.criterionId == mCriterionId && event.studentId == mStudentId) {
            updateComment(event.text)
        }
    }

}
