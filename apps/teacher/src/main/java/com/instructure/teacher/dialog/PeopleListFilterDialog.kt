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

package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.R
import com.instructure.teacher.adapters.PeopleFilterAdapter
import kotlinx.coroutines.Job

class PeopleListFilterDialog : BaseCanvasAppCompatDialogFragment() {
    private var recyclerView: RecyclerView? = null
    private var canvasContext: CanvasContext? = null
    private var canvasContextMap: HashMap<CanvasContext, Boolean> = HashMap()
    private var canvasContextIdList: ArrayList<Long> = ArrayList()
    private var shouldIncludeGroups = true
    private var mApiCalls: Job? = null

    private fun updateCanvasContexts(sections: ArrayList<Section>, groups: ArrayList<Group>) {
        // The selected contexts and the previously selected contexts need to be preserved on rotation
        val combinedContextList: ArrayList<Long> = canvasContextIdList
        combinedContextList.addAll(canvasContextMap.filter { it.value }.keys.map { it.id })
        recyclerView?.adapter = PeopleFilterAdapter(getCanvasContextList(sections, groups), combinedContextList) { canvasContext: CanvasContext, isChecked: Boolean ->
            canvasContextMap[canvasContext] = isChecked
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(ContextThemeWrapper(requireActivity(), 0), R.layout.dialog_canvas_context_list, null)

        canvasContext = nonNullArgs.getParcelable(Const.CANVAS_CONTEXT)
        shouldIncludeGroups = nonNullArgs.getBoolean(Const.GROUPS)

        // Restore selected contexts from saved state
        savedInstanceState?.let {
            val selectedContexts = it.getParcelableArrayList<CanvasContext>(SAVED_SELECTED_CONTEXTS_KEY)
            selectedContexts?.forEach { context ->
                canvasContextMap[context] = true
            }
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val dialog = AlertDialog.Builder(requireActivity())
                .setCancelable(true)
                .setTitle(getString(R.string.filterBy))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    val selectedContexts = canvasContextMap.filter { it.value }.keys.toMutableList() as ArrayList<CanvasContext>
                    val result = Bundle().apply {
                        putParcelableArrayList(RESULT_SELECTED_CONTEXTS, selectedContexts)
                    }
                    parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
                }
            .setNegativeButton(getString(R.string.cancel), null)
                .create()

        dialog.setOnShowListener {
            loadData(false)
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        return dialog
    }

    fun loadData(forceNetwork: Boolean) {
        mApiCalls = weave {

            try {
                var groups: ArrayList<Group> = ArrayList()
                var sections: ArrayList<Section> = ArrayList()
                inParallel {

                    // Get Sections
                    await<List<Section>>({
                        SectionManager.getAllSectionsForCourse(canvasContext?.id
                                ?: 0, it, forceNetwork)
                    }) {
                        sections = it as ArrayList<Section>
                    }

                    if (shouldIncludeGroups) {
                        // Get groups
                        await<List<Group>>({
                            GroupManager.getAllGroupsForCourse(canvasContext?.id
                                    ?: 0, it, forceNetwork)
                        }) {
                            groups = it as ArrayList<Group>
                        }
                    }
                }

                updateCanvasContexts(sections, groups)
            } catch (ignore: Throwable) {
                if (activity != null) {
                    Toast.makeText(requireActivity(), R.string.error_occurred, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCanvasContextList(sections: List<Section>, groups: List<Group>): ArrayList<CanvasContext> {
        val canvasContexts = ArrayList<CanvasContext>()

        if (sections.isNotEmpty()) {
            val sectionSeparator = Course(
                    name = getString(R.string.sections),
                    id = -1
            )
            canvasContexts.add(sectionSeparator)
            canvasContexts.addAll(sections)
        }

        if (groups.isNotEmpty()) {
            val groupSeparator = Course(
                    name = getString(R.string.assignee_type_groups),
                    id = -1
            )

            canvasContexts.add(groupSeparator)
            canvasContexts.addAll(groups)
        }

        return canvasContexts
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedContexts = canvasContextMap.filter { it.value }.keys.toCollection(ArrayList())
        outState.putParcelableArrayList(SAVED_SELECTED_CONTEXTS_KEY, selectedContexts)
    }

    override fun onDestroyView() {
        mApiCalls?.cancel()
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "PeopleListFilterDialog"
        const val RESULT_SELECTED_CONTEXTS = "selected_contexts"
        private const val SAVED_SELECTED_CONTEXTS_KEY = "saved_selected_contexts"

        fun getInstance(manager: FragmentManager, canvasContextIdList: ArrayList<Long>, canvasContext: CanvasContext, shouldIncludeGroups: Boolean) : PeopleListFilterDialog {
            manager.dismissExisting<PeopleListFilterDialog>()
            val args = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putBoolean(Const.GROUPS, shouldIncludeGroups)
            }
            val dialog = PeopleListFilterDialog()
            dialog.canvasContextIdList = canvasContextIdList
            dialog.arguments = args
            return dialog
        }
    }

}
