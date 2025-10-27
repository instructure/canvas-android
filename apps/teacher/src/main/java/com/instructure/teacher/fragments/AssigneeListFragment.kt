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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNEE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.pandautils.views.EmptyView
import com.instructure.teacher.R
import com.instructure.teacher.adapters.AssigneeListAdapter
import com.instructure.teacher.databinding.FragmentAssigneeListBinding
import com.instructure.teacher.factory.AssigneeListPresenterFactory
import com.instructure.teacher.holders.AssigneeViewHolder
import com.instructure.teacher.models.AssigneeCategory
import com.instructure.teacher.presenters.AssigneeListPresenter
import com.instructure.teacher.utils.*
import com.instructure.teacher.viewinterface.AssigneeListView

@ScreenView(SCREEN_VIEW_ASSIGNEE_LIST)
class AssigneeListFragment : BaseExpandableSyncFragment<
        AssigneeCategory,
        CanvasComparable<*>,
        AssigneeListView,
        AssigneeListPresenter,
        AssigneeViewHolder,
        AssigneeListAdapter>(), AssigneeListView {

    private val binding by viewBinding(FragmentAssigneeListBinding::bind)

    private var mDateGroups: EditDateGroups by ParcelableArrayListArg(arrayListOf())
    private var mTargetIdx: Int by IntArg()
    private var sections by ParcelableArrayListArg<Section>(arrayListOf())
    private var groups by ParcelableArrayListArg<Group>(arrayListOf())
    private var students by ParcelableArrayListArg<User>(arrayListOf())

    private val assigneeRecyclerView by bind<RecyclerView>(R.id.recyclerView)
    private val swipeRefreshLayout by bind<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
    private val emptyPandaView by bind<EmptyView>(R.id.emptyPandaView)
    private val saveButton: TextView? get() = view?.findViewById(R.id.menuSave)

    override fun layoutResId() = R.layout.fragment_assignee_list
    override val recyclerView get() = assigneeRecyclerView
    override fun withPagination() = false
    override fun perPageCount() = ApiPrefs.perPageCount
    override fun getPresenterFactory() = AssigneeListPresenterFactory(mDateGroups, mTargetIdx, sections, groups, students)
    override fun onCreateView(view: View) {
        binding.toolbar.applyTopSystemBarInsets()

        ViewCompat.setOnApplyWindowInsetsListener(assigneeRecyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun performSave() {
        presenter.save()
        requireActivity().onBackPressed()
    }

    private fun <T : SpannableStringBuilder> T.appendColored(text: CharSequence, color: Int): T = apply {
        val start = length
        append(text)
        setSpan(ForegroundColorSpan(color), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun updateSelectedAssignees(assigneeNames: ArrayList<CharSequence>, displayEveryone: Boolean, displayAsEveryoneElse: Boolean) {
        if (displayEveryone) assigneeNames.add(0, getString(if (displayAsEveryoneElse) R.string.everyone_else else R.string.everyone))
        val span = SpannableStringBuilder()
        val nameColor = requireContext().getColorCompat(R.color.textInfo)
        val separatorColor = requireContext().getColorCompat(R.color.textDark)
        for (name in assigneeNames) {
            span.appendColored(name, nameColor)
            if (name !== assigneeNames.last()) span.appendColored(", ", separatorColor)
        }
        binding.selectedAssigneesTextView.setVisible(assigneeNames.isNotEmpty()).text = span
    }

    override fun onPresenterPrepared(presenter: AssigneeListPresenter) {
        RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.recyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = getString(R.string.no_items_to_display_short)
        )
        addSwipeToRefresh(swipeRefreshLayout)
    }

    override fun onReadySetGo(presenter: AssigneeListPresenter) {
        binding.root.applyHorizontalSystemBarInsets()
        assigneeRecyclerView.adapter = adapter
        presenter.loadData(false)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupCloseButton(this@AssigneeListFragment)
        toolbar.title = getString(R.string.page_title_add_assignees)
        toolbar.setupMenu(R.menu.menu_save_generic) { performSave() }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        saveButton?.setTextColor(ThemePrefs.textButtonColor)
    }

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, assigneeRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun createAdapter() = AssigneeListAdapter(requireContext(), presenter)

    override fun notifyItemChanged(position: Int) {
        adapter.notifyItemChanged(position)
    }

    companion object {
        @JvmStatic val DATE_GROUPS = "dateGroups"
        @JvmStatic val TARGET_INDEX = "targetIndex"
        @JvmStatic val SECTIONS = "sections"
        @JvmStatic val GROUPS = "groups"
        @JvmStatic val STUDENTS = "students"

        @Suppress("UNCHECKED_CAST")
        fun newInstance(args: Bundle) = AssigneeListFragment().apply {
            this.mDateGroups = args.getParcelableArrayList(DATE_GROUPS)!!
            this.mTargetIdx = args.getInt(TARGET_INDEX)
            this.sections = args.getParcelableArrayList(SECTIONS)!!
            this.groups = args.getParcelableArrayList(GROUPS)!!
            this.students = args.getParcelableArrayList(STUDENTS)!!
        }

        fun makeBundle(dateGroups: EditDateGroups, targetIdx: Int, sections: List<Section>, groups: List<Group>, students: List<User>): Bundle {
            val args = Bundle()
            args.putSerializable(AssigneeListFragment.DATE_GROUPS, ArrayList(dateGroups))
            args.putInt(AssigneeListFragment.TARGET_INDEX, targetIdx)
            args.putParcelableArrayList(AssigneeListFragment.SECTIONS, ArrayList(sections.toList()))
            args.putParcelableArrayList(AssigneeListFragment.GROUPS, ArrayList(groups.toList()))
            args.putParcelableArrayList(AssigneeListFragment.STUDENTS, ArrayList(students.toList()))
            return args
        }
    }
}
