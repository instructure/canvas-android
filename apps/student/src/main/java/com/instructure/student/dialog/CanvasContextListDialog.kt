/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isNotDeleted
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.analytics.SCREEN_VIEW_CANVAS_CONTEXT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.adapter.CanvasContextDialogAdapter
import kotlinx.android.synthetic.main.dialog_canvas_context_list.*
import kotlinx.android.synthetic.main.dialog_canvas_context_list.view.*
import kotlinx.coroutines.Job
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_CANVAS_CONTEXT_LIST)
class CanvasContextListDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var selectedCallback: (canvasContext: CanvasContext) -> Unit by Delegates.notNull()
    private var apiCalls: Job? = null

    companion object {

        fun getInstance(
            manager: FragmentManager,
            callback: (canvasContext: CanvasContext) -> Unit
        ): CanvasContextListDialog {
            manager.dismissExisting<CanvasContextListDialog>()
            val dialog = CanvasContextListDialog()
            dialog.selectedCallback = callback
            return dialog
        }
    }

    private fun updateCanvasContexts(courses: List<Course>, groups: List<Group>) {
        dialog?.recyclerView?.adapter = CanvasContextDialogAdapter(getCanvasContextList(requireContext(), courses, groups)) {
            dismiss()
            selectedCallback(it)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.dialog_canvas_context_list, null)
        view.recyclerView.layoutManager = LinearLayoutManager(context)

        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle(requireContext().getString(R.string.selectCanvasContext))
            .setView(view)
            .create()

        dialog.setOnShowListener { loadData(false) }
        return dialog
    }

    private fun loadData(forceNetwork: Boolean) {
        apiCalls = tryWeave {
            dialog?.emptyView?.setLoading()
            val (courses, groups) = awaitApis<List<Course>, List<Group>>(
                { CourseManager.getCourses(forceNetwork, it) },
                { GroupManager.getFavoriteGroups(it, forceNetwork) }
            )
            val validCourses = courses.filter { it.isFavorite && it.isValidTerm() && it.hasActiveEnrollment() }
            val courseMap = validCourses.associateBy { it.id }
            val validGroups = groups.filter { it.courseId == 0L || courseMap[it.courseId] != null }
            updateCanvasContexts(validCourses, validGroups)
            dialog?.emptyView?.setGone()
        } catch {
            activity?.toast(R.string.errorOccurred)
            dismiss()
        }
    }

    private fun getCanvasContextList(
        context: Context,
        courses: List<Course>,
        groups: List<Group>
    ): List<CanvasContext> {
        val canvasContexts = mutableListOf<CanvasContext>()

        if (courses.isNotEmpty()) {
            val courseSeparator = Course(
                    name = context.getString(R.string.courses),
                    id = -1
            )

            canvasContexts.add(courseSeparator)
            canvasContexts.addAll(courses)
        }

        if (groups.isNotEmpty()) {
            val groupSeparator = Course(
                    name = context.getString(R.string.groups),
                    id = -1
            )

            canvasContexts.add(groupSeparator)
            canvasContexts.addAll(groups)
        }

        return canvasContexts
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        apiCalls?.cancel()
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
