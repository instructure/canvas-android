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
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
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
                .setPositiveButton(R.string.next) { _, _ -> validateAndShowNext() }
                .setNegativeButton(R.string.cancel) { _, _ -> dismissAllowingStateLoss() }
                .setCancelable(true)
                .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return alertDialog
    }

    private fun moveSelection(@IdRes viewId: Int) {
        TransitionManager.beginDelayedTransition(constraintSelectionWrapper)

        val set = ConstraintSet()
        set.clone(constraintSelectionWrapper)

        set.connect(R.id.selectionIndicator, ConstraintSet.TOP, viewId, ConstraintSet.TOP)
        set.connect(R.id.selectionIndicator, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM)

        set.applyTo(constraintSelectionWrapper)
    }

    private fun toggleSpinners(visible: Boolean) {
        TransitionManager.beginDelayedTransition(newAssignmentContainer)

        val set = ConstraintSet()
        set.clone(newAssignmentContainer)

        set.setVisibility(R.id.newStudentCourseSpinner, if (visible) View.VISIBLE else View.GONE)
        set.setVisibility(R.id.newAssignmentSpinner, if (visible) View.VISIBLE else View.GONE)

        set.applyTo(newAssignmentContainer)
    }

    private fun validateAndShowNext() {

    }

    private fun setRevealContentsListener() {
        val avatarAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_shrink)
        val titleAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_bottom)
        newAvatar.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(newAvatar, this)
                        newAvatar.startAnimation(avatarAnimation)
                        newUserName.startAnimation(titleAnimation)
                        newDialogTitle.startAnimation(titleAnimation)
                    }
                }
        )
        constraintSelectionWrapper.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        AnimationHelpers.removeGlobalLayoutListeners(constraintSelectionWrapper, this)
                        val revealAnimator = AnimationHelpers.createRevealAnimator(constraintSelectionWrapper)
                        Handler().postDelayed({
                            if (!isAdded) return@postDelayed
                            constraintSelectionWrapper.visibility = View.VISIBLE
                            revealAnimator.start()
                        }, 600)
                    }
                }
        )
    }

    private fun handleAction(action: ShareExtensionTargetAction) {
        when (action) {
            is ShareExtensionTargetAction.FilesTargetSelected -> {
                newAssignmentCheckBox.isChecked = false
                filesCheckBox.isChecked = true
                toggleSpinners(false)
                moveSelection(R.id.filesCheckBox)
            }
            is ShareExtensionTargetAction.AssignmentTargetSelected -> {
                newAssignmentCheckBox.isChecked = true
                filesCheckBox.isChecked = false
                toggleSpinners(true)
                moveSelection(R.id.newAssignmentContainer)
            }
        }
    }

}