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
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_BROWSER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Const.CANVAS_STUDENT_ID
import com.instructure.pandautils.utils.Const.MARKET_URI_PREFIX
import com.instructure.teacher.R
import com.instructure.teacher.adapters.CourseBrowserAdapter
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.factory.CourseBrowserPresenterFactory
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.syllabus.ui.SyllabusFragment
import com.instructure.teacher.holders.CourseBrowserViewHolder
import com.instructure.teacher.presenters.CourseBrowserPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.CourseBrowserHeaderView
import com.instructure.teacher.viewinterface.CourseBrowserView
import kotlinx.android.synthetic.main.fragment_course_browser.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_COURSE_BROWSER)
class CourseBrowserFragment : BaseSyncFragment<
        Tab,
        CourseBrowserPresenter,
        CourseBrowserView,
        CourseBrowserViewHolder,
        CourseBrowserAdapter>(),
        CourseBrowserView, AppBarLayout.OnOffsetChangedListener {

    private var mCanvasContext: CanvasContext by ParcelableArg(Course())

    private val mCourseBrowserHeader by lazy { rootView.findViewById<CourseBrowserHeaderView>(R.id.courseBrowserHeader) }

    companion object {
        fun newInstance(context: CanvasContext) = CourseBrowserFragment().apply {
            mCanvasContext = context
        }

        fun makeRoute(canvasContext: CanvasContext?) = Route(CourseBrowserFragment::class.java, canvasContext)
    }

    private lateinit var mRecyclerView: RecyclerView

    override fun layoutResId(): Int = R.layout.fragment_course_browser

    override val recyclerView: RecyclerView get() = courseBrowserRecyclerView
    override fun withPagination() = false
    override fun getPresenterFactory() = CourseBrowserPresenterFactory(mCanvasContext) { tab, attendanceId ->
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

    override fun onPresenterPrepared(presenter: CourseBrowserPresenter) {
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
        appBarLayout.addOnOffsetChangedListener(this)
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
        EventBus.getDefault().register(this)
        (presenter.canvasContext as? Course)?.let {
            courseImage.setCourseImage(it, it.backgroundColor, !TeacherPrefs.hideCourseColorOverlay)
        }
        courseBrowserTitle.text = presenter.canvasContext.name
        courseBrowserSubtitle.text = (presenter.canvasContext as? Course)?.term?.name ?: ""
        mCourseBrowserHeader.setTitleAndSubtitle(presenter.canvasContext.name ?: "", (presenter.canvasContext as? Course)?.term?.name ?: "")
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

    private fun setupToolbar() {
        // If course color overlay is disabled we show a static toolbar and hide the text overlay
        val toolbar = if (TeacherPrefs.hideCourseColorOverlay) {
            overlayToolbar.setGone()
            courseHeader.setGone()
            noOverlayToolbar.title = presenter.canvasContext.name
            (presenter.canvasContext as? Course)?.term?.name?.let { noOverlayToolbar.subtitle = it }
            noOverlayToolbar.setBackgroundColor(presenter.canvasContext.backgroundColor)
            noOverlayToolbar
        } else {
            noOverlayToolbar.setGone()
            overlayToolbar
        }

        toolbar.setupBackButton(this)
        toolbar.setupMenu(R.menu.menu_course_browser, menuItemCallback)
        ViewStyler.colorToolbarIconsAndText(requireActivity(), toolbar, requireContext().getColor(R.color.white))
        ViewStyler.setStatusBarDark(requireActivity(), presenter.canvasContext.backgroundColor)

        collapsingToolbarLayout.setContentScrimColor(presenter.canvasContext.backgroundColor)

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

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_course_browser_settings -> {
                RouteMatcher.route(requireContext(), Route(CourseSettingsFragment::class.java, presenter.canvasContext))
            }
        }
    }

    override fun createAdapter(): CourseBrowserAdapter {
        return CourseBrowserAdapter(requireActivity(), presenter, presenter.canvasContext.textAndIconColor) { tab ->
            when (tab.tabId) {
                Tab.ASSIGNMENTS_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(AssignmentListFragment::class.java, presenter.canvasContext)
                )
                Tab.QUIZZES_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(QuizListFragment::class.java, presenter.canvasContext)
                )
                Tab.DISCUSSIONS_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(DiscussionsListFragment::class.java, presenter.canvasContext)
                )
                Tab.ANNOUNCEMENTS_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(AnnouncementListFragment::class.java, presenter.canvasContext)
                )
                Tab.PEOPLE_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(PeopleListFragment::class.java, presenter.canvasContext)
                )
                Tab.FILES_ID -> {
                    val args = FileListFragment.makeBundle(presenter.canvasContext)
                    RouteMatcher.route(
                        requireContext(),
                        Route(FileListFragment::class.java, presenter.canvasContext, args)
                    )
                }
                Tab.PAGES_ID -> RouteMatcher.route(
                    requireContext(),
                    Route(PageListFragment::class.java, presenter.canvasContext)
                )
                Tab.MODULES_ID -> {
                    val bundle = ModuleListFragment.makeBundle(presenter.canvasContext)
                    RouteMatcher.route(requireContext(), Route(ModuleListFragment::class.java, null, bundle))
                }
                Tab.STUDENT_VIEW -> {
                    Analytics.logEvent(AnalyticsEventConstants.STUDENT_VIEW_TAPPED)
                    presenter.handleStudentViewClick()
                }
                Tab.SYLLABUS_ID -> {
                    RouteMatcher.route(requireContext(), Route(null, SyllabusFragment::class.java, presenter.canvasContext, presenter.canvasContext.makeBundle()))
                }
                else -> {
                    if (tab.type == Tab.TYPE_EXTERNAL) {
                        // if the user is a designer we don't want to let them look at LTI tools (like attendance)
                        if ((presenter.canvasContext as? Course)?.isDesigner == true) {
                            toast(R.string.errorIsDesigner)
                            return@CourseBrowserAdapter
                        }
                        val attendanceExternalToolId = TeacherPrefs.attendanceExternalToolId
                        if (attendanceExternalToolId.isNotBlank() && attendanceExternalToolId == tab.tabId) {
                            val args = AttendanceListFragment.makeBundle(tab)
                            RouteMatcher.route(
                                requireContext(),
                                Route(AttendanceListFragment::class.java, presenter.canvasContext, args)
                            )
                        } else {
                            val args = LtiLaunchFragment.makeTabBundle(presenter.canvasContext, tab)
                            RouteMatcher.route(
                                requireContext(),
                                Route(LtiLaunchFragment::class.java, presenter.canvasContext, args)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() = emptyView.setLoading()
    override fun checkIfEmpty() = RecyclerViewUtils.checkIfEmpty(emptyView, courseBrowserRecyclerView,
            swipeRefreshLayout, adapter, presenter.isEmpty)

    /**
     * Manages state of titles & subtitles when users scrolls
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val percentage = Math.abs(verticalOffset).div(appBarLayout?.totalScrollRange?.toFloat() ?: 1F)

        if(percentage <= 0.3F) {
            val toolbarAnimation = ObjectAnimator.ofFloat(mCourseBrowserHeader, View.ALPHA, mCourseBrowserHeader.alpha, 0F)
            val titleAnimation = ObjectAnimator.ofFloat(courseBrowserTitle, View.ALPHA, courseBrowserTitle.alpha, 1F)
            val subtitleAnimation = ObjectAnimator.ofFloat(courseBrowserSubtitle, View.ALPHA, courseBrowserSubtitle.alpha, 0.8F)

            toolbarAnimation.setAutoCancel(true)
            titleAnimation.setAutoCancel(true)
            subtitleAnimation.setAutoCancel(true)

            toolbarAnimation.target = mCourseBrowserHeader
            titleAnimation.target = courseBrowserTitle
            subtitleAnimation.target = courseBrowserSubtitle

            toolbarAnimation.duration = 200
            titleAnimation.duration = 320
            subtitleAnimation.duration = 320

            toolbarAnimation.start()
            titleAnimation.start()
            subtitleAnimation.start()

        } else if(percentage > 0.7F) {
            val toolbarAnimation = ObjectAnimator.ofFloat(mCourseBrowserHeader, View.ALPHA, mCourseBrowserHeader.alpha, 1F)
            val titleAnimation = ObjectAnimator.ofFloat(courseBrowserTitle, View.ALPHA, courseBrowserTitle.alpha, 0F)
            val subtitleAnimation = ObjectAnimator.ofFloat(courseBrowserSubtitle, View.ALPHA, courseBrowserSubtitle.alpha, 0F)

            toolbarAnimation.setAutoCancel(true)
            titleAnimation.setAutoCancel(true)
            subtitleAnimation.setAutoCancel(true)

            toolbarAnimation.target = mCourseBrowserHeader
            titleAnimation.target = courseBrowserTitle
            subtitleAnimation.target = courseBrowserSubtitle

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
            putExtra(Const.COURSE_ID, mCanvasContext.id) // Required to create/get test user
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

