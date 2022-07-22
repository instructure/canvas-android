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
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentShareExtensionTargetBinding
import com.instructure.pandautils.features.shareextension.ShareExtensionViewModel
import com.instructure.pandautils.utils.AnimationHelpers
import com.instructure.pandautils.utils.ThemePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_share_extension_target.*

@AndroidEntryPoint
class ShareExtensionTargetFragment : DialogFragment() {

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

        viewModel.events.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        setRevealContentsListener()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentShareExtensionTargetBinding.inflate(layoutInflater, null, false)

        val alertDialog = AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .setPositiveButton(R.string.next, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create()

        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.setOnShowListener {
            val positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(ThemePrefs.buttonColor)
            positive.setOnClickListener { validateAndShowNext() }
            val negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.setTextColor(ThemePrefs.buttonColor)
            negative.setOnClickListener {
                dismissAllowingStateLoss()
                shareExtensionViewModel.finish()
            }
        }

        return alertDialog
    }

    private fun moveSelection(@IdRes viewId: Int) {
        TransitionManager.beginDelayedTransition(selectionWrapper)

        val set = ConstraintSet()
        set.clone(selectionWrapper)

        set.connect(R.id.selectionIndicator, ConstraintSet.TOP, viewId, ConstraintSet.TOP)
        set.connect(R.id.selectionIndicator, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM)

        set.applyTo(selectionWrapper)
    }

    private fun toggleSpinners(visible: Boolean) {
        TransitionManager.beginDelayedTransition(assignmentContainer)

        val set = ConstraintSet()
        set.clone(assignmentContainer)

        set.setVisibility(R.id.studentCourseSpinner, if (visible) View.VISIBLE else View.GONE)
        set.setVisibility(R.id.assignmentSpinner, if (visible) View.VISIBLE else View.GONE)

        set.applyTo(assignmentContainer)
    }

    private fun validateAndShowNext() {
        val data = viewModel.getValidatedData()
        if (data != null) {
            shareExtensionViewModel.showUploadDialog(data.course, data.assignment, data.fileUploadType)
            dismiss()
        }
    }

    private fun setRevealContentsListener() {
        val avatarAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_shrink)
        val titleAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_bottom)
        avatar.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(avatar, this)
                        avatar.startAnimation(avatarAnimation)
                        userName.startAnimation(titleAnimation)
                        dialogTitle.startAnimation(titleAnimation)
                    }
                }
        )
        selectionWrapper.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(selectionWrapper, this)
                        val revealAnimator = AnimationHelpers.createRevealAnimator(selectionWrapper)
                        Handler().postDelayed({
                            if (!isAdded) return@postDelayed
                            selectionWrapper.visibility = View.VISIBLE
                            revealAnimator.start()
                        }, 600)
                    }
                }
        )
    }

    private fun handleAction(action: ShareExtensionTargetAction) {
        when (action) {
            is ShareExtensionTargetAction.FilesTargetSelected -> {
                assignmentCheckBox.isChecked = false
                filesCheckBox.isChecked = true
                toggleSpinners(false)
                moveSelection(R.id.filesCheckBox)
            }
            is ShareExtensionTargetAction.AssignmentTargetSelected -> {
                assignmentCheckBox.isChecked = true
                filesCheckBox.isChecked = false
                toggleSpinners(true)
                moveSelection(R.id.assignmentContainer)
            }
            is ShareExtensionTargetAction.ShowToast -> {
                Toast.makeText(requireContext(), action.toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

}