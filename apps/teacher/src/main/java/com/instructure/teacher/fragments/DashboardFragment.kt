/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.MenuItem
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.pandautils.analytics.SCREEN_VIEW_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationsFragment
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.fadeAnimationWithAction
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.adapters.CoursesAdapter
import com.instructure.teacher.databinding.FragmentDashboardBinding
import com.instructure.teacher.decorations.VerticalGridSpacingDecoration
import com.instructure.teacher.events.CourseColorOverlayToggledEvent
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.factory.DashboardPresenterFactory
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.presenters.DashboardPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.CoursesView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

private const val LIST_SPAN_COUNT = 1

@PageView
@ScreenView(SCREEN_VIEW_DASHBOARD)
@AndroidEntryPoint
class DashboardFragment : BaseSyncFragment<Course, DashboardPresenter, CoursesView, CoursesViewHolder, CoursesAdapter>(), CoursesView {

    @Inject
    lateinit var userApi: UserAPI.UsersInterface

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    private val binding by viewBinding(FragmentDashboardBinding::bind)

    private lateinit var mGridLayoutManager: GridLayoutManager
    private lateinit var mDecorator: VerticalGridSpacingDecoration

    // Activity callbacks
    private var mCourseBrowserCallback: CourseBrowserCallback? = null
    private var mNeedToForceNetwork = false

    private val somethingChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.extras?.getBoolean(Const.COURSE_FAVORITES) == true) {
                binding.swipeRefreshLayout.isRefreshing = true
                presenter.refresh(true)
            }
        }
    }

    override fun layoutResId() = R.layout.fragment_dashboard
    override val recyclerView: RecyclerView get() = binding.courseRecyclerView
    override fun perPageCount() = ApiPrefs.perPageCount
    override fun withPagination() = false

    override fun getPresenterFactory() = DashboardPresenterFactory(userApi, networkStateProvider)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourseBrowserCallback) mCourseBrowserCallback = context
    }

    override fun onDetach() {
        super.onDetach()
        mCourseBrowserCallback = null
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(somethingChangedReceiver, IntentFilter(Const.COURSE_THING_CHANGED))
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(somethingChangedReceiver)
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(view: View) {
        val spanCount = if (TeacherPrefs.listDashboard) LIST_SPAN_COUNT else requireContext().resources.getInteger(R.integer.course_list_span_count)
        mGridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        mDecorator = VerticalGridSpacingDecoration(requireContext(), mGridLayoutManager)
    }

    override fun onPresenterPrepared(presenter: DashboardPresenter) {}

    override fun onReadySetGo(presenter: DashboardPresenter) = with(binding) {
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(requireContext())) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }

        courseRecyclerView.layoutManager = mGridLayoutManager
        courseRecyclerView.removeItemDecoration(mDecorator) // Remove existing decorator
        courseRecyclerView.addItemDecoration(mDecorator)
        addSwipeToRefresh(swipeRefreshLayout)

        // Set up RecyclerView padding
        val padding = resources.getDimensionPixelSize(R.dimen.course_list_padding)
        val paddingTop = resources.getDimensionPixelSize(R.dimen.course_list_top_padding)
        courseRecyclerView.setPaddingRelative(padding, paddingTop, padding, padding)
        courseRecyclerView.clipToPadding = false

        // Apply bottom insets to RecyclerView
        ViewCompat.setOnApplyWindowInsetsListener(courseRecyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = padding + systemBars.bottom)
            insets
        }
        if (courseRecyclerView.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(courseRecyclerView)
        }

        emptyCoursesView.onClickAddCourses { routeEditDashboard() }
        setupHeader()

        setupToolbar()
        if(courseRecyclerView.adapter == null) {
            courseRecyclerView.adapter = adapter
        }
        addItemTouchHelperForCardReorder()
        presenter.loadData(mNeedToForceNetwork)
        mNeedToForceNetwork = false
    }

    private fun setupHeader() {
        binding.editDashboardTextView.setTextColor(ThemePrefs.textButtonColor)
        binding.editDashboardTextView.setOnClickListener { routeEditDashboard() }
    }

    private fun routeEditDashboard() {
        RouteMatcher.route(requireActivity(), EditDashboardFragment.makeRoute())
    }

    private fun setupToolbar() = with(binding) {
        toolbar.applyTopSystemBarInsets()
        toolbar.setupMenu(R.menu.courses_fragment, menuItemCallback)

        val dashboardLayoutMenuItem = toolbar.menu.findItem(R.id.menu_dashboard_cards)
        val menuIconRes = if (TeacherPrefs.listDashboard) R.drawable.ic_grid_dashboard else R.drawable.ic_list_dashboard
        dashboardLayoutMenuItem.setIcon(menuIconRes)

        val menuTitleRes = if (TeacherPrefs.listDashboard) R.string.dashboardSwitchToGridView else R.string.dashboardSwitchToListView
        dashboardLayoutMenuItem.setTitle(menuTitleRes)

        val activity = requireActivity()
        if (activity is InitActivity) {
            activity.attachNavigationDrawer()
            activity.attachToolbar(toolbar)
        } else {
            toolbar.setupAsBackButton(this@DashboardFragment)
        }

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)

        toolbar.requestAccessibilityFocus()
    }

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_dashboard_cards -> changeDashboardLayout(item)
        }
    }

    private fun changeDashboardLayout(item: MenuItem) {
        if (TeacherPrefs.listDashboard) {
            item.setIcon(R.drawable.ic_list_dashboard)
            item.setTitle(R.string.dashboardSwitchToListView)
            TeacherPrefs.listDashboard = false
        } else {
            item.setIcon(R.drawable.ic_grid_dashboard)
            item.setTitle(R.string.dashboardSwitchToGridView)
            TeacherPrefs.listDashboard = true
        }

        recyclerView.fadeAnimationWithAction {
            if (TeacherPrefs.listDashboard) {
                mGridLayoutManager.spanCount = LIST_SPAN_COUNT
            } else {
                mGridLayoutManager.spanCount = requireContext().resources.getInteger(R.integer.course_list_span_count)
            }
            view?.post { adapter.notifyDataSetChanged() }
        }
    }

    override fun createAdapter(): CoursesAdapter {
        return CoursesAdapter(requireActivity(), presenter, mCourseBrowserCallback)
    }

    override fun onRefreshStarted(): Unit = with(binding) {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyCoursesView.visibility  = View.VISIBLE
        }
        emptyCoursesView.setLoading()
        coursesHeaderWrapper.setGone()
        notificationsFragment.setGone()
        (childFragmentManager.findFragmentByTag("notifications_fragment") as DashboardNotificationsFragment?)?.refresh()
    }

    override fun onRefreshFinished(): Unit = with(binding) {
        swipeRefreshLayout.isRefreshing = false
        if (presenter.isEmpty) {
            coursesHeaderWrapper.setGone()
        } else {
            coursesHeaderWrapper.setVisible()
        }
        notificationsFragment.setVisible()
    }

    override fun checkIfEmpty() = with(binding) {
        emptyCoursesView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_super))
        RecyclerViewUtils.checkIfEmpty(emptyCoursesView, courseRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    private fun addItemTouchHelperForCardReorder() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            0
        ) {
            private var draggedFromPosition: Int? = null

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    draggedFromPosition = viewHolder.bindingAdapterPosition
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                if (fromPosition in 0 until adapter.size() && toPosition in 0 until adapter.size()) {
                    adapter.notifyItemMoved(fromPosition, toPosition)
                }

                return true
            }

            override fun getDragDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder is CoursesViewHolder && presenter.isOnline()) {
                    ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.DOWN or ItemTouchHelper.UP
                } else 0
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                val finishingPosition = viewHolder.bindingAdapterPosition
                val startPosition = draggedFromPosition

                if (finishingPosition == RecyclerView.NO_POSITION || startPosition == null) {
                    draggedFromPosition = null
                    return
                }

                if (startPosition != finishingPosition) {
                    presenter.moveCourse(startPosition, finishingPosition)
                    adapter.notifyDataSetChanged()

                    lifecycleScope.launch {
                        val result = presenter.saveDashboardPositions()
                        if (result.isFail) {
                            toast(R.string.failedToUpdateDashboardOrder)
                        }
                    }
                }

                draggedFromPosition = null
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.courseRecyclerView)
    }

    fun cancelCardDrag() {
        val cancelEvent = MotionEvent.obtain(0L, 0L, ACTION_CANCEL, 0f, 0f, 0)
        binding.courseRecyclerView.onTouchEvent(cancelEvent)
        cancelEvent.recycle()
    }

    companion object {
        fun getInstance() = DashboardFragment()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onColorOverlayToggled(event: CourseColorOverlayToggledEvent) {
        adapter.notifyDataSetChanged()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCourseEdited(event: CourseUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            if (isResumed) {
                presenter.loadData(true)
            } else {
                mNeedToForceNetwork = true
            }
        }
    }

    interface CourseBrowserCallback {
        fun onShowCourseDetails(course: Course)
        fun onPickCourseColor(course: Course)
        fun onEditCourseNickname(course: Course)
    }
}
