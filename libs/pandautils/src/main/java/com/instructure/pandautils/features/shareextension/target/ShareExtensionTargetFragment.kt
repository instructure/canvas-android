/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.shareextension.target

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AnimationUtils
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentShareExtensionTargetBinding
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.ShareExtensionViewModel
import com.instructure.pandautils.utils.AnimationHelpers
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareExtensionTargetFragment : BaseCanvasDialogFragment() {

    companion object {
        const val TAG = "ShareExtensionTargetFragment"

        fun newInstance() = ShareExtensionTargetFragment()
    }

    private val viewModel: ShareExtensionTargetViewModel by viewModels()

    private val shareExtensionViewModel: ShareExtensionViewModel by activityViewModels()

    private lateinit var binding: FragmentShareExtensionTargetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isAccessibilityEnabled = isAccessibilityEnabled(requireContext())

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        setRevealContentsListener()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentShareExtensionTargetBinding.inflate(layoutInflater, null, false)

        binding.studentCourseSpinner.isEnabled = !isAccessibilityEnabled(requireContext())
        binding.assignmentSpinner.isEnabled = !isAccessibilityEnabled(requireContext())

        val alertDialog = AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .setPositiveButton(R.string.next, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create()

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setOnShowListener {
            val positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(ThemePrefs.textButtonColor)
            positive.setOnClickListener { validateAndShowNext() }
            val negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.setTextColor(ThemePrefs.textButtonColor)
            negative.setOnClickListener {
                dismissAllowingStateLoss()
                shareExtensionViewModel.finish()
            }
        }

        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return alertDialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (viewModel.data.value?.uploadType) {
            FileUploadType.ASSIGNMENT -> assignmentTargetSelected()
            FileUploadType.USER -> filesTargetSelected()
            else -> filesTargetSelected()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        TransitionManager.endTransitions(binding.assignmentContainer)
        TransitionManager.endTransitions(binding.selectionWrapper)
    }

    private fun moveSelection(@IdRes viewId: Int) {
        TransitionManager.beginDelayedTransition(binding.selectionWrapper)

        val set = ConstraintSet()
        set.clone(binding.selectionWrapper)

        set.connect(R.id.selectionIndicator, ConstraintSet.TOP, viewId, ConstraintSet.TOP)
        set.connect(R.id.selectionIndicator, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM)

        set.applyTo(binding.selectionWrapper)
    }

    private fun toggleSpinners(visible: Boolean) {
        if (viewModel.isAccessibilityEnabled) {
            binding.studentCourseSpinner.isEnabled = visible
            binding.assignmentSpinner.isEnabled = visible
            return
        }

        TransitionManager.beginDelayedTransition(binding.assignmentContainer)

        val set = ConstraintSet()
        set.clone(binding.assignmentContainer)

        set.setVisibility(R.id.studentCourseSpinner, if (visible) View.VISIBLE else View.GONE)
        set.setVisibility(R.id.assignmentSpinner, if (visible) View.VISIBLE else View.GONE)

        set.applyTo(binding.assignmentContainer)
    }

    private fun validateAndShowNext() {
        viewModel.validateDataAndMoveToFileUpload()
    }

    private fun setRevealContentsListener() {
        val avatarAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_shrink)
        val titleAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_bottom)
        binding.avatar.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(binding.avatar, this)
                        binding.avatar.startAnimation(avatarAnimation)
                        binding.userName.startAnimation(titleAnimation)
                        binding.dialogTitle.startAnimation(titleAnimation)
                    }
                }
        )
        binding.selectionWrapper.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(binding.selectionWrapper, this)
                        val revealAnimator = AnimationHelpers.createRevealAnimator(binding.selectionWrapper)
                        Handler().postDelayed({
                            if (!isAdded) return@postDelayed
                            binding.selectionWrapper.visibility = View.VISIBLE
                            revealAnimator.start()
                        }, 600)
                    }
                }
        )
    }

    private fun handleAction(action: ShareExtensionTargetAction) {
        when (action) {
            is ShareExtensionTargetAction.FilesTargetSelected -> {
                filesTargetSelected()
            }
            is ShareExtensionTargetAction.AssignmentTargetSelected -> {
                assignmentTargetSelected()
            }
            is ShareExtensionTargetAction.ShowToast -> {
                toast(action.toast)
            }
            is ShareExtensionTargetAction.ShowFileUpload -> {
                shareExtensionViewModel.showUploadDialog(action.data.course, action.data.assignment, action.data.fileUploadType)
                dismiss()
            }
            is ShareExtensionTargetAction.UpdateSpinnerContentDescriptions -> {
                binding.studentCourseSpinner.contentDescription = action.courseContentDescription
                binding.assignmentSpinner.contentDescription = action.assignmentContentDescription
            }
        }
    }

    private fun filesTargetSelected() {
        binding.assignmentCheckBox.isChecked = false
        binding.filesCheckBox.isChecked = true
        toggleSpinners(false)
        moveSelection(R.id.filesCheckBox)
    }

    private fun assignmentTargetSelected() {
        binding.assignmentCheckBox.isChecked = true
        binding.filesCheckBox.isChecked = false
        toggleSpinners(true)
        moveSelection(R.id.assignmentContainer)
    }

}