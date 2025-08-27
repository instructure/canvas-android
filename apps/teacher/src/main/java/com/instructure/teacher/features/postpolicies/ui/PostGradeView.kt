/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.postpolicies.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.AssignmentGradedEvent
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.positionOnScreen
import com.instructure.pandautils.utils.post
import com.instructure.pandautils.utils.postSticky
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.DialogPostGradedEveryoneBinding
import com.instructure.teacher.databinding.FragmentPostGradeBinding
import com.instructure.teacher.features.postpolicies.PostGradeEvent
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class PostGradeView(
    inflater: LayoutInflater, parent: ViewGroup
) : MobiusView<PostGradeViewState, PostGradeEvent, FragmentPostGradeBinding>(inflater, FragmentPostGradeBinding::inflate, parent) {

    private var adapter: PostGradeSectionRecyclerAdapter? = null

    init {
        binding.postPolicyRecycler.layoutManager = LinearLayoutManager(context)
    }

    override fun onConnect(output: Consumer<PostGradeEvent>) = with(binding) {

        postGradeButton.setOnClickListener {
            output.accept(PostGradeEvent.PostGradesClicked)
        }

        postPolicyOnlyGradedRow.setOnClickListener {
            showOnlyGradedDialog(it, output)
        }
        postPolicySectionToggleHolder.postPolicySectionToggle.setOnCheckedChangeListener { _, isChecked ->
            output.accept(PostGradeEvent.SpecificSectionsToggled)
        }
        adapter = PostGradeSectionRecyclerAdapter(object : PostGradeSectionCallback {
            override fun sectionToggled(sectionId: Long) {
                output.accept(PostGradeEvent.SectionToggled(sectionId))
            }
        })
        postPolicyRecycler.adapter = adapter
    }

    private fun showOnlyGradedDialog(view: View, output: Consumer<PostGradeEvent>) {
        val builder = AlertDialog.Builder(context, R.style.RoundedContextDialog)
        val dialogBinding = DialogPostGradedEveryoneBinding.inflate(LayoutInflater.from(context))
        builder.setView(dialogBinding.root)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialogBinding.postDialogEveryone.setOnClickListener {
                output.accept(PostGradeEvent.GradedOnlySelected(false))
                dialog.cancel()
            }
            dialogBinding.postDialogGraded.setOnClickListener {
                output.accept(PostGradeEvent.GradedOnlySelected(true))
                dialog.cancel()
            }
        }
        val wmlp = dialog.window?.attributes

        val (offsetX, offsetY) = binding.postPolicyOnlyGradedSelection.positionOnScreen

        wmlp?.gravity = Gravity.TOP or Gravity.START
        wmlp?.x = offsetX
        wmlp?.y = offsetY

        dialog.show()
    }

    override fun onDispose() {
        adapter = null
        binding.postPolicyRecycler.adapter = null
    }

    override fun render(state: PostGradeViewState) {
        renderVisibilities(state.visibilities)

        when (state) {
            is PostGradeViewState.Loading -> renderLoading(state)
            is PostGradeViewState.EmptyViewState -> renderEmpty(state)
            is PostGradeViewState.LoadedViewState -> renderData(state)
        }
    }

    private fun renderVisibilities(visibilities: PostGradeVisibilities) = with(binding) {
        postGradeLoading.setVisible(visibilities.loading)
        postEmptyLayout.setVisible(visibilities.emptyView)
        postPolicyLayout.setVisible(visibilities.policyView)
        postPolicyOnlyGradedRow.setVisible(visibilities.gradedOnlySelector)
        postPolicyRecycler.setVisible(visibilities.sectionRecycler)
        postGradeButtonProcessing.setVisible(visibilities.postProcessing)
    }

    private fun renderLoading(state: PostGradeViewState.Loading) {
        ViewStyler.themeProgressBar(binding.postGradeLoading, state.courseColor)
    }

    private fun renderEmpty(state: PostGradeViewState.EmptyViewState) = with(binding) {
        postEmptyPanda.setImageResource(state.imageResId)
        postEmptyTitle.text = state.emptyTitle
        postEmptyMessage.text = state.emptyMessage
    }

    private fun renderData(state: PostGradeViewState.LoadedViewState) = with(binding) {
        postPolicyStatusCount.text = state.statusText
        postPolicyOnlyGradedSelection.text = state.gradedOnlyText

        postGradeButton.text = state.postText
        postGradeButton.setTextColor(ThemePrefs.buttonTextColor)
        postGradeButton.isEnabled = !state.postProcessing
        postGradeButtonLayout.setBackgroundColor(ThemePrefs.buttonColor)

        ViewStyler.themeProgressBar(postGradeButtonProcessing, ThemePrefs.buttonTextColor)

        postPolicySectionToggleHolder.postPolicySectionToggle.applyTheme(state.courseColor)

        adapter?.data = state.sections
    }

    fun showGradesPosted(isHidingGrades: Boolean, assignmentId: Long) {
        Toast.makeText(context, if (isHidingGrades) R.string.postPolicyHiddenToast else R.string.postPolicyPostedToast, Toast.LENGTH_SHORT).show()
        AssignmentGradedEvent(assignmentId).postSticky() //post bus event
        activity.onBackPressed()
    }

    fun showPostFailed(isHidingGrades: Boolean) {
        Toast.makeText(context, if (isHidingGrades) R.string.postPolicyHideFailedToast else R.string.postPolicyPostFailedToast, Toast.LENGTH_LONG).show()
    }
}