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

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_BROWSER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Const.CANVAS_STUDENT_ID
import com.instructure.pandautils.utils.Const.MARKET_URI_PREFIX
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.a11yManager
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isSwitchAccessEnabled
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.pandautils.utils.setCourseImage
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.adapters.CourseBrowserAdapter
import com.instructure.teacher.databinding.FragmentCourseBrowserBinding
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.factory.CourseBrowserPresenterFactory
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.syllabus.ui.SyllabusFragment
import com.instructure.teacher.holders.CourseBrowserViewHolder
import com.instructure.teacher.presenters.CourseBrowserPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.DisableableAppBarLayoutBehavior
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.view.CourseBrowserHeaderView
import com.instructure.teacher.viewinterface.CourseBrowserView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView(url = "{canvasContext}")
@ScreenView(SCREEN_VIEW_COURSE_BROWSER)
class CourseBrowserFragment : BaseSyncFragment<
        Tab,
        CourseBrowserPresenter,
        CourseBrowserView,
        CourseBrowserViewHolder,
        CourseBrowserAdapter>(),
        CourseBrowserView, AppBarLayout.OnOffsetChangedListener {

    private val binding by viewBinding(FragmentCourseBrowserBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(Course())

    private val courseBrowserHeader by lazy { rootView.findViewById<CourseBrowserHeaderView>(R.id.courseBrowserHeader) }

    companion object {
        fun newInstance(context: CanvasContext) = CourseBrowserFragment().apply {
            canvasContext = context
        }

        fun makeRoute(canvasContext: CanvasContext?) = Route(CourseBrowserFragment::class.java, canvasContext)
    }

    private lateinit var mRecyclerView: RecyclerView

    override fun layoutResId(): Int = R.layout.fragment_course_browser

    override val recyclerView: RecyclerView get() = binding.courseBrowserRecyclerView
    override fun withPagination() = false
    override fun getPresenterFactory() = CourseBrowserPresenterFactory(canvasContext) { tab, attendanceId ->
        //Filter for white-list supported features
        //TODO: support other things like it.isHidden
        when(tab.tabId) {
            Tab.ASSIGNMENTS_ID,
            Tab.QUIZZES_ID,
            Tab.DISCUSSIONS_ID,
            Tab.ANNOUNCEMENTS_ID,
            Tab.PEOPLE_ID,
            Tab.FILES_ID,
            Tab.PAGES_ID,
            Tab.MODULES_ID,
            Tab.STUDENT_VIEW,
            Tab.SYLLABUS_ID -> true
            else -> {
                if (attendanceId != 0L && tab.tabId.endsWith("_$attendanceId")) {
                    TeacherPrefs.attendanceExternalToolId = tab.tabId
                }
                tab.type == Tab.TYPE_EXTERNAL
            }
        }
    }

    override fun onCreateView(view: View) = Unit

    override fun onPresenterPrepared(presenter: CourseBrowserPresenter) = with(binding) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.courseBrowserRecyclerView,
            emptyViewResId = R.id.emptyView,
            emptyViewText = getString(R.string.no_items_to_display_short)
        )
        appBarLayout.addOnOffsetChangedListener(this@CourseBrowserFragment)
        collapsingToolbarLayout.isTitleEnabled = false
    }

    override fun onReadySetGo(presenter: CourseBrowserPresenter) {
        if (recyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }
        presenter.loadData(false)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this@CourseBrowserFragment)
        (presenter.canvasContext as? Course)?.let {
            binding.courseImage.setCourseImage(it, it.color, !TeacherPrefs.hideCourseColorOverlay)
        }
        binding.courseBrowserTitle.text = presenter.canvasContext.name
        binding.courseBrowserSubtitle.text = (presenter.canvasContext as? Course)?.term?.name.orEmpty()
        courseBrowserHeader.setTitleAndSubtitle(presenter.canvasContext.name.orEmpty(), (presenter.canvasContext as? Course)?.term?.name.orEmpty())
        setupToolbar()
        if (!presenter.isEmpty) {
            checkIfEmpty()
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCourseUpdated(event: CourseUpdatedEvent) {
        event.once(javaClass.simpleName) { course ->
            if (course.id == presenter.canvasContext.id) (presenter.canvasContext as? Course)?.apply {
                name = course.name
                originalName = course.originalName
                homePage = course.homePage
            }
        }
    }

    private fun setupToolbar() = with(binding) {
        // If course color overlay is disabled we show a static toolbar and hide the text overlay
        val toolbar = if (TeacherPrefs.hideCourseColorOverlay) {
            overlayToolbar.setGone()
            courseHeader.setGone()
            noOverlayToolbar.title = presenter.canvasContext.name
            (presenter.canvasContext as? Course)?.term?.name?.let { noOverlayToolbar.subtitle = it }
            noOverlayToolbar.setBackgroundColor(presenter.canvasContext.color)
            noOverlayToolbar
        } else {
            noOverlayToolbar.setGone()
            overlayToolbar
        }

        toolbar.setupBackButton(this@CourseBrowserFragment)
        toolbar.setupMenu(R.menu.menu_course_browser, menuItemCallback)
        ViewStyler.colorToolbarIconsAndText(requireActivity(), toolbar, requireContext().getColor(R.color.textLightest))
        ViewStyler.setStatusBarDark(requireActivity(), presenter.canvasContext.color)

        collapsingToolbarLayout.setContentScrimColor(presenter.canvasContext.color)

        // Hide image placeholder if color overlay is disabled and there is no valid image
        val hasImage = (presenter.canvasContext as? Course)?.imageUrl?.isValid() == true
        val hideImagePlaceholder = TeacherPrefs.hideCourseColorOverlay && !hasImage

        if (isTablet || requireContext().a11yManager.isSwitchAccessEnabled || hideImagePlaceholder) {
            appBarLayout.setExpanded(false, false)
            appBarLayout.isActivated = false
            appBarLayout.isFocusable = false
            val layoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as DisableableAppBarLayoutBehavior).isEnabled = false
        }

        courseBrowserTitle.requestAccessibilityFocus(600)
    }

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_course_browser_settings -> {
                RouteMatcher.route(requireActivity(), Route(CourseSettingsFragment::class.java, presenter.canvasContext))
            }
        }
    }

    override fun createAdapter(): CourseBrowserAdapter {
        return CourseBrowserAdapter(requireActivity(), presenter, presenter.canvasContext.color) { tab ->
            when (tab.tabId) {
                Tab.ASSIGNMENTS_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(AssignmentListFragment::class.java, presenter.canvasContext)
                )
                Tab.QUIZZES_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(QuizListFragment::class.java, presenter.canvasContext)
                )
                Tab.DISCUSSIONS_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(DiscussionsListFragment::class.java, presenter.canvasContext)
                )
                Tab.ANNOUNCEMENTS_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(AnnouncementListFragment::class.java, presenter.canvasContext)
                )
                Tab.PEOPLE_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(PeopleListFragment::class.java, presenter.canvasContext)
                )
                Tab.FILES_ID -> {
                    val args = FileListFragment.makeBundle(presenter.canvasContext)
                    RouteMatcher.route(
                        requireActivity(),
                        Route(FileListFragment::class.java, presenter.canvasContext, args)
                    )
                }
                Tab.PAGES_ID -> RouteMatcher.route(
                    requireActivity(),
                    Route(PageListFragment::class.java, presenter.canvasContext)
                )
                Tab.MODULES_ID -> {
                    val bundle = ModuleListFragment.makeBundle(presenter.canvasContext)
                    RouteMatcher.route(requireActivity(), Route(ModuleListFragment::class.java, presenter.canvasContext, bundle))
                }
                Tab.STUDENT_VIEW -> {
                    Analytics.logEvent(AnalyticsEventConstants.STUDENT_VIEW_TAPPED)
                    presenter.handleStudentViewClick()
                }
                Tab.SYLLABUS_ID -> {
                    RouteMatcher.route(requireActivity(), Route(SyllabusFragment::class.java, presenter.canvasContext, presenter.canvasContext.makeBundle()))
                }
                else -> {
                    if (tab.type == Tab.TYPE_EXTERNAL) {
                        val attendanceExternalToolId = TeacherPrefs.attendanceExternalToolId
                        if (attendanceExternalToolId.isNotBlank() && attendanceExternalToolId == tab.tabId) {
                            val args = AttendanceListFragment.makeBundle(tab)
                            RouteMatcher.route(
                                requireActivity(),
                                Route(AttendanceListFragment::class.java, presenter.canvasContext, args)
                            )
                        } else {
                            val route = LtiLaunchFragment.makeRoute(presenter.canvasContext, tab)
                            RouteMatcher.route(requireActivity(), route)
                        }
                    }
                }
            }
        }
    }

    override fun onRefreshFinished() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() = binding.emptyView.setLoading()
    override fun checkIfEmpty() = RecyclerViewUtils.checkIfEmpty(
        binding.emptyView,
        binding.courseBrowserRecyclerView,
        binding.swipeRefreshLayout,
        adapter,
        presenter.isEmpty
    )

    /**
     * Manages state of titles & subtitles when users scrolls
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val percentage = Math.abs(verticalOffset).div(appBarLayout?.totalScrollRange?.toFloat() ?: 1F)

        if(percentage <= 0.3F) {
            val toolbarAnimation = ObjectAnimator.ofFloat(courseBrowserHeader, View.ALPHA, courseBrowserHeader.alpha, 0F)
            val titleAnimation = ObjectAnimator.ofFloat(binding.courseBrowserTitle, View.ALPHA, binding.courseBrowserTitle.alpha, 1F)
            val subtitleAnimation = ObjectAnimator.ofFloat(binding.courseBrowserSubtitle, View.ALPHA, binding.courseBrowserSubtitle.alpha, 0.8F)

            toolbarAnimation.setAutoCancel(true)
            titleAnimation.setAutoCancel(true)
            subtitleAnimation.setAutoCancel(true)

            toolbarAnimation.target = courseBrowserHeader
            titleAnimation.target = binding.courseBrowserTitle
            subtitleAnimation.target = binding.courseBrowserSubtitle

            toolbarAnimation.duration = 200
            titleAnimation.duration = 320
            subtitleAnimation.duration = 320

            toolbarAnimation.start()
            titleAnimation.start()
            subtitleAnimation.start()

        } else if(percentage > 0.7F) {
            val toolbarAnimation = ObjectAnimator.ofFloat(courseBrowserHeader, View.ALPHA, courseBrowserHeader.alpha, 1F)
            val titleAnimation = ObjectAnimator.ofFloat(binding.courseBrowserTitle, View.ALPHA, binding.courseBrowserTitle.alpha, 0F)
            val subtitleAnimation = ObjectAnimator.ofFloat(binding.courseBrowserSubtitle, View.ALPHA, binding.courseBrowserSubtitle.alpha, 0F)

            toolbarAnimation.setAutoCancel(true)
            titleAnimation.setAutoCancel(true)
            subtitleAnimation.setAutoCancel(true)

            toolbarAnimation.target = courseBrowserHeader
            titleAnimation.target = binding.courseBrowserTitle
            subtitleAnimation.target = binding.courseBrowserSubtitle

            toolbarAnimation.duration = 200
            titleAnimation.duration = 200
            subtitleAnimation.duration = 200

            toolbarAnimation.start()
            titleAnimation.start()
            subtitleAnimation.start()
        }
    }


    override
    fun isStudentInstalled(): Boolean {
        var studentInstalled = false
        context?.packageManager?.getInstalledPackages(0)?.let { packages ->
            studentInstalled = packages.any {
                it.packageName == CANVAS_STUDENT_ID
            }
        }

        return studentInstalled
    }

    override
    fun gotoStudentPlayStoreListing() {
        // Send the user to the Play Store
        val playStoreIntent: Intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(MARKET_URI_PREFIX + CANVAS_STUDENT_ID)
        }

        startActivity(playStoreIntent)
    }

    override
    fun showStudentView() {
        // Student app is installed - take the user to the Student app
        val token = ApiPrefs.getValidToken() // Need the user token so they can masquerade in the Student app

        // Create bundle with test student user id
        val studentViewIntent: Intent = Intent().apply {
            `package` = CANVAS_STUDENT_ID
            action = Const.INTENT_ACTION_STUDENT_VIEW
            putExtra(Const.TOKEN, token)
            putExtra(Const.COURSE_ID, canvasContext.id) // Required to create/get test user
            putExtra(Const.DOMAIN, ApiPrefs.domain)
            putExtra(Const.CLIENT_ID, ApiPrefs.clientId)
            putExtra(Const.CLIENT_SECRET, ApiPrefs.clientSecret)
            putExtra(Const.IS_ELEMENTARY, ApiPrefs.user?.k5User ?: false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pm = requireActivity().packageManager
        val canHandleIntent = pm.queryIntentActivities(studentViewIntent, 0).any()
        if (canHandleIntent)
            // Send bundle to the student app
            startActivity(studentViewIntent)
        // If there is no activity that can handle the intent, then the Student app is not up to date - take them
        // to the student app listing for them to update it
        else gotoStudentPlayStoreListing()
    }
}

