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

import android.app.Activity
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.features.postpolicies.PostGradeEvent
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.adapter_post_policy_section.view.*
import kotlinx.android.synthetic.main.fragment_post_grade.*
import com.instructure.pandautils.utils.applyTheme
import android.view.*
import androidx.appcompat.app.AlertDialog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.post
import kotlinx.android.synthetic.main.dialog_post_graded_everyone.*

class PostGradeView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<PostGradeViewState, PostGradeEvent>(R.layout.fragment_post_grade, inflater, parent) {

    private var adapter: PostGradeSectionRecyclerAdapter? = null

    init {
        postPolicyRecycler.layoutManager = LinearLayoutManager(context)
    }

    override fun onConnect(output: Consumer<PostGradeEvent>) {

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
        builder.setView(R.layout.dialog_post_graded_everyone)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.postDialogEveryone.setOnClickListener {
                output.accept(PostGradeEvent.GradedOnlySelected(false))
                dialog.cancel()
            }
            dialog.postDialogGraded.setOnClickListener {
                output.accept(PostGradeEvent.GradedOnlySelected(true))
                dialog.cancel()
            }
        }
        val wmlp = dialog.window?.attributes

        wmlp?.gravity = Gravity.TOP or Gravity.END
        wmlp?.x = (view.x).toInt()
        wmlp?.y = (view.y + view.height * 2).toInt()

        dialog.show()
    }

    override fun onDispose() {
        adapter = null
        postPolicyRecycler.adapter = null
    }

    override fun render(state: PostGradeViewState) {
        renderVisibilities(state.visibilities)

        when (state) {
            is PostGradeViewState.Loading -> renderLoading(state)
            is PostGradeViewState.EmptyViewState -> renderEmpty(state)
            is PostGradeViewState.LoadedViewState -> renderData(state)
        }
    }

    private fun renderVisibilities(visibilities: PostGradeVisibilities) {
        postGradeLoading.setVisible(visibilities.loading)
        postEmptyLayout.setVisible(visibilities.emptyView)
        postPolicyLayout.setVisible(visibilities.policyView)
        postPolicyOnlyGradedRow.setVisible(visibilities.gradedOnlySelector)
        postPolicyRecycler.setVisible(visibilities.sectionRecycler)
        postGradeButtonProcessing.setVisible(visibilities.postProcessing)
    }

    private fun renderLoading(state: PostGradeViewState.Loading) {
        ViewStyler.themeProgressBar(postGradeLoading, state.courseColor)
    }

    private fun renderEmpty(state: PostGradeViewState.EmptyViewState) {
        postEmptyPanda.setImageResource(state.imageResId)
        postEmptyTitle.text = state.emptyTitle
        postEmptyMessage.text = state.emptyMessage
    }

    private fun renderData(state: PostGradeViewState.LoadedViewState) {
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
        AssignmentGradedEvent(assignmentId).post() //post bus event
        (context as Activity).onBackPressed()
    }

    fun showPostFailed(isHidingGrades: Boolean) {
        Toast.makeText(context, if (isHidingGrades) R.string.postPolicyHideFailedToast else R.string.postPolicyPostFailedToast, Toast.LENGTH_LONG).show()
    }
}