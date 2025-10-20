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

package com.instructure.student.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.CourseNicknameManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseNickname
import com.instructure.canvasapi2.models.DashboardPositions
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.ColorPickerDialog
import com.instructure.pandautils.dialogs.EditCourseNicknameDialog
import com.instructure.pandautils.features.dashboard.DashboardCourseItem
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationsFragment
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentFragment
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.OfflineSyncWorker
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.fadeAnimationWithAction
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.removeAllItemDecorations
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withRequireNetwork
import com.instructure.student.R
import com.instructure.student.adapter.DashboardRecyclerAdapter
import com.instructure.student.databinding.CourseGridRecyclerRefreshLayoutBinding
import com.instructure.student.databinding.FragmentCourseGridBinding
import com.instructure.student.decorations.VerticalGridSpacingDecoration
import com.instructure.student.events.CoreDataFinishedLoading
import com.instructure.student.events.CourseColorOverlayToggledEvent
import com.instructure.student.events.ShowGradesToggledEvent
import com.instructure.student.features.coursebrowser.CourseBrowserFragment
import com.instructure.student.features.dashboard.DashboardRepository
import com.instructure.student.holders.CourseViewHolder
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.StudentPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

private const val LIST_SPAN_COUNT = 1

@ScreenView(SCREEN_VIEW_DASHBOARD)
@PageView
@AndroidEntryPoint
class DashboardFragment : ParentFragment() {

    @Inject
    lateinit var repository: DashboardRepository

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var aggregateProgressObserver: AggregateProgressObserver

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var offlineAnalyticsManager: OfflineAnalyticsManager

    private val binding by viewBinding(FragmentCourseGridBinding::bind)
    private lateinit var recyclerBinding: CourseGridRecyclerRefreshLayoutBinding

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: DashboardRecyclerAdapter? = null

    private var courseColumns: Int = LIST_SPAN_COUNT
    private var groupColumns: Int = LIST_SPAN_COUNT

    private val runningWorkers = mutableSetOf<String>()

    private val somethingChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (recyclerAdapter != null && intent?.extras?.getBoolean(Const.COURSE_FAVORITES) == true) {
                recyclerBinding.swipeRefreshLayout.isRefreshing = true
                recyclerAdapter?.refresh()
            }
        }
    }

    override fun title(): String = if (isAdded) getString(R.string.dashboard) else ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_course_grid, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerBinding = CourseGridRecyclerRefreshLayoutBinding.bind(binding.root)

        applyTheme()

        binding.toolbar.applyTopSystemBarInsets()

        networkStateProvider.isOnlineLiveData.observe(this) { online ->
            recyclerAdapter?.refresh()
            if (online) recyclerBinding.swipeRefreshLayout.isRefreshing = true
        }

        lifecycleScope.launch {
            if (featureFlagProvider.offlineEnabled()) {
                subscribeToOfflineSyncUpdates()
            }
        }
    }

    private fun subscribeToOfflineSyncUpdates() {
        val workQuery = WorkQuery.Builder.fromTags(listOf(OfflineSyncWorker.PERIODIC_TAG, OfflineSyncWorker.ONE_TIME_TAG)).build()
        workManager.getWorkInfosLiveData(workQuery).observe(this) { workInfos ->
            workInfos.forEach { workInfo ->
                if (workInfo.state == State.RUNNING) {
                    runningWorkers.add(workInfo.id.toString())
                }
            }

            if (workInfos?.any { (it.state == State.SUCCEEDED || it.state == State.FAILED) && runningWorkers.contains(it.id.toString()) } == true) {
                recyclerAdapter?.silentRefresh()
                runningWorkers.clear()
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerAdapter = DashboardRecyclerAdapter(requireActivity(), object : CourseAdapterToFragmentCallback {

            override fun onRefreshFinished() {
                recyclerBinding.swipeRefreshLayout.isRefreshing = false
                recyclerBinding.notificationsFragment.setVisible()
            }

            override fun onSeeAllCourses() {
                RouteMatcher.route(requireActivity(), EditDashboardFragment.makeRoute())
            }

            override fun onGroupSelected(group: Group) {
                canvasContext = group
                RouteMatcher.route(requireActivity(), CourseBrowserFragment.makeRoute(group))
            }

            override fun onCourseSelected(course: Course) {
                lifecycleScope.launch {
                    if (!repository.isOnline()) { offlineAnalyticsManager.reportCourseOpenedInOfflineMode() }
                    canvasContext = course
                    RouteMatcher.route(requireActivity(), CourseBrowserFragment.makeRoute(course))
                }
            }

            @Suppress("EXPERIMENTAL_FEATURE_WARNING")
            override fun onEditCourseNickname(course: Course) {
                EditCourseNicknameDialog.getInstance(requireFragmentManager(), course) { s ->
                    tryWeave {
                        val response = awaitApi<CourseNickname> { CourseNicknameManager.setCourseNickname(course.id, s, it) }
                        if (response.nickname == null) {
                            course.name = response.name!!
                            course.originalName = null
                        } else {
                            course.name = response.nickname!!
                            course.originalName = response.name
                        }
                        recyclerAdapter?.notifyDataSetChanged()
                    } catch {
                        toast(R.string.courseNicknameError)
                    }
                }.show(requireFragmentManager(), EditCourseNicknameDialog::class.java.simpleName)
            }

            @Suppress("EXPERIMENTAL_FEATURE_WARNING")
            override fun onPickCourseColor(course: Course) {
                ColorPickerDialog.newInstance(requireFragmentManager(), course) { color ->
                    tryWeave {
                        awaitApi<CanvasColor> { UserManager.setColors(it, course.contextId, color) }
                        ColorKeeper.addToCache(course.contextId, color)
                        recyclerAdapter?.notifyDataSetChanged()
                    } catch {
                        toast(R.string.colorPickerError)
                    }
                }.show(requireFragmentManager(), ColorPickerDialog::class.java.simpleName)
            }

            override fun onManageOfflineContent(course: Course) {
                RouteMatcher.route(requireActivity(), OfflineContentFragment.makeRoute(course))
            }
        }, repository)

        configureRecyclerView()
        recyclerBinding.listView.isSelectionEnabled = false
        recyclerBinding.swipeRefreshLayout.applyBottomSystemBarInsets()
        initMenu()
    }

    override fun applyTheme() {
        with (binding) {
            toolbar.title = title()
            // Styling done in attachNavigationDrawer
            navigation?.attachNavigationDrawer(this@DashboardFragment, toolbar)

            recyclerAdapter?.notifyDataSetChanged()
        }
    }

    private fun initMenu() = with(binding) {
        toolbar.setMenu(R.menu.menu_dashboard) { item ->
            when (item.itemId) {
                R.id.menu_dashboard_cards -> changeDashboardLayout(item)
                R.id.menu_dashboard_offline -> activity?.withRequireNetwork {
                    RouteMatcher.route(requireActivity(), OfflineContentFragment.makeRoute())
                }
            }
        }

        val dashboardLayoutMenuItem = toolbar.menu.findItem(R.id.menu_dashboard_cards)
        val menuIconRes = if (StudentPrefs.listDashboard) R.drawable.ic_grid_dashboard else R.drawable.ic_list_dashboard
        dashboardLayoutMenuItem.setIcon(menuIconRes)

        val menuTitleRes = if (StudentPrefs.listDashboard) R.string.dashboardSwitchToGridView else R.string.dashboardSwitchToListView
        dashboardLayoutMenuItem.setTitle(menuTitleRes)

        lifecycleScope.launch {
            if (!featureFlagProvider.offlineEnabled()) {
                toolbar.menu.removeItem(R.id.menu_dashboard_offline)
                toolbar.menu.findItem(R.id.menu_dashboard_cards).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
    }

    private fun changeDashboardLayout(item: MenuItem) {
        if (StudentPrefs.listDashboard) {
            item.setIcon(R.drawable.ic_list_dashboard)
            item.setTitle(R.string.dashboardSwitchToListView)
            StudentPrefs.listDashboard = false
        } else {
            item.setIcon(R.drawable.ic_grid_dashboard)
            item.setTitle(R.string.dashboardSwitchToGridView)
            StudentPrefs.listDashboard = true
        }

        recyclerBinding.listView.fadeAnimationWithAction {
            configureGridSize()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureGridSize()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isTablet) {
                binding.emptyCoursesView.setGuidelines(.37f, .49f, .6f, .7f, .12f, .88f)
            } else {
                binding.emptyCoursesView.setGuidelines(.36f, .54f, .64f,.77f, .12f, .88f)

            }
        } else {
            if (isTablet) {
                // Change nothing, at least for now
            } else {
                binding.emptyCoursesView.setGuidelines(.27f, .52f, .58f,.73f, .15f, .85f)
            }
        }
    }

    private fun configureGridSize() {
        courseColumns =
            if (StudentPrefs.listDashboard) LIST_SPAN_COUNT else resources.getInteger(R.integer.course_card_columns)
        groupColumns =
            if (StudentPrefs.listDashboard) LIST_SPAN_COUNT else resources.getInteger(R.integer.group_card_columns)
        (recyclerBinding.listView.layoutManager as? GridLayoutManager)?.spanCount =
            courseColumns * groupColumns
        view?.post { recyclerAdapter?.notifyDataSetChanged() }
    }

    private fun configureRecyclerView() = with(binding) {
        // Set up GridLayoutManager
        courseColumns = if (StudentPrefs.listDashboard) LIST_SPAN_COUNT else resources.getInteger(R.integer.course_card_columns)
        groupColumns = if (StudentPrefs.listDashboard) LIST_SPAN_COUNT else resources.getInteger(R.integer.group_card_columns)
        val layoutManager = GridLayoutManager(
            context,
            courseColumns * groupColumns,
            RecyclerView.VERTICAL,
            false
        )
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = recyclerBinding.listView.adapter!!.getItemViewType(position)
                return when (DashboardRecyclerAdapter.ItemType.values()[viewType]) {
                    DashboardRecyclerAdapter.ItemType.COURSE -> groupColumns
                    DashboardRecyclerAdapter.ItemType.GROUP -> courseColumns
                    else -> courseColumns * groupColumns
                }
            }
        }

        // Add decoration
        recyclerBinding.listView.removeAllItemDecorations()
        recyclerBinding.listView.addItemDecoration(VerticalGridSpacingDecoration(requireContext(), layoutManager))
        recyclerBinding.listView.layoutManager = layoutManager
        recyclerBinding.listView.itemAnimator = DefaultItemAnimator()
        recyclerBinding.listView.adapter = recyclerAdapter
        recyclerBinding.listView.setEmptyView(emptyCoursesView)
        recyclerBinding.swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                recyclerBinding.swipeRefreshLayout.isRefreshing = false
            } else {
                recyclerAdapter?.refresh()
                recyclerBinding.notificationsFragment.setGone()
                (childFragmentManager.findFragmentByTag("notifications_fragment") as DashboardNotificationsFragment?)?.refresh()
            }
        }

        // Set up RecyclerView padding
        val padding = resources.getDimensionPixelSize(R.dimen.courseListPadding)
        recyclerBinding.listView.setPaddingRelative(padding, padding, padding, padding)
        recyclerBinding.listView.clipToPadding = false

        emptyCoursesView.onClickAddCourses {
            RouteMatcher.route(requireActivity(), EditDashboardFragment.makeRoute())
        }

        addItemTouchHelperForCardReorder()
    }

    fun cancelCardDrag() {
        recyclerBinding.listView.onTouchEvent(MotionEvent.obtain(0L, 0L, ACTION_CANCEL, 0f, 0f, 0))
    }

    private fun addItemTouchHelperForCardReorder() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            0
        ) {

            private var itemToMove: DashboardCourseItem? = null

            // We need to consider other items in the recyclerview when dealing with positions.
            // In the callbacks we get the adapter position, but we want to get the course items position so we use this.
            // If there is any other recyclerview item added above the courses this should be modified.
            private val POSITION_MODIFIER = 1

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder == null) return
                val fromPosition = viewHolder.bindingAdapterPosition
                val fromItem = recyclerAdapter?.getItem(DashboardRecyclerAdapter.ItemType.COURSE_HEADER, fromPosition - POSITION_MODIFIER) as? DashboardCourseItem

                itemToMove = fromItem
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                val itemsSize = recyclerAdapter?.getItems(DashboardRecyclerAdapter.ItemType.COURSE_HEADER)?.size ?: 0

                if (toPosition - POSITION_MODIFIER in 0..< itemsSize) {
                    recyclerAdapter?.notifyItemMoved(fromPosition, toPosition)
                }

                return true
            }

            override fun getDragDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder is CourseViewHolder && networkStateProvider.isOnline()) {
                    ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.DOWN or ItemTouchHelper.UP
                } else 0
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                val finishingPosition = viewHolder.bindingAdapterPosition

                if (finishingPosition == RecyclerView.NO_POSITION) {
                    itemToMove = null
                    firebaseCrashlytics.recordException(Throwable("Failed to reorder dashboard. finishingPosition == RecyclerView.NO_POSITION"))
                    toast(R.string.failedToUpdateDashboardOrder)
                    return
                }

                itemToMove?.let {
                    recyclerAdapter?.moveItems(DashboardRecyclerAdapter.ItemType.COURSE_HEADER, it, finishingPosition - 1)
                    recyclerAdapter?.notifyDataSetChanged()
                    itemToMove = null
                }

                val courseItems = recyclerAdapter?.getItems(DashboardRecyclerAdapter.ItemType.COURSE_HEADER)
                        ?.mapNotNull { it as? DashboardCourseItem } ?: emptyList()
                val positions = courseItems
                    .mapIndexed { index, course -> Pair(course.course.contextId, index) }
                    .toMap()

                val dashboardPositions = DashboardPositions(positions)
                lifecycleScope.launch {
                    val updateResult = repository.updateDashboardPositions(dashboardPositions)
                    if (updateResult.isFail) {
                        toast(R.string.failedToUpdateDashboardOrder)
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerBinding.listView)
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

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onShowGradesToggled(event: ShowGradesToggledEvent) {
        recyclerAdapter?.notifyDataSetChanged()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onColorOverlayToggled(event: CourseColorOverlayToggledEvent) {
        recyclerAdapter?.notifyDataSetChanged()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onCoreDataLoaded(event: CoreDataFinishedLoading) {
        applyTheme()
    }

    override fun onDestroy() {
        recyclerAdapter?.cancel()
        super.onDestroy()
    }

    companion object {
        fun newInstance(route: Route) =
                DashboardFragment().apply {
                    arguments = route.canvasContext?.makeBundle(route.arguments) ?: route.arguments
                }

        fun makeRoute(canvasContext: CanvasContext?) = Route(DashboardFragment::class.java, canvasContext)
    }
}
