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
package com.emeritus.student.fragment

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_BROWSER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import com.emeritus.student.adapter.CourseBrowserAdapter
import com.emeritus.student.events.CourseColorOverlayToggledEvent
import com.emeritus.student.router.RouteMatcher
import com.emeritus.student.util.Const
import com.emeritus.student.util.DisableableAppBarLayoutBehavior
import com.emeritus.student.util.StudentPrefs
import com.emeritus.student.util.TabHelper
import kotlinx.android.synthetic.main.fragment_course_browser.*
import kotlinx.android.synthetic.main.view_course_browser_header.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@ScreenView(SCREEN_VIEW_COURSE_BROWSER)
@PageView(url = "{canvasContext}")
class CourseBrowserFragment : Fragment(), FragmentInteractions, AppBarLayout.OnOffsetChangedListener  {

    private var apiCalls: Job? = null

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override val navigation: Navigation?
        get() = if (activity is Navigation) activity as Navigation else null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_course_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarLayout.addOnOffsetChangedListener(this)
        collapsingToolbarLayout.isTitleEnabled = false

        courseBrowserTitle.text = canvasContext.name

        (canvasContext as? Course)?.let {
            courseImage.setCourseImage(it, it.backgroundColor, !StudentPrefs.hideCourseColorOverlay)
            courseBrowserSubtitle.text = it.term?.name ?: ""
            courseBrowserHeader.setTitleAndSubtitle(it.name, it.term?.name ?: "")
        }

        (canvasContext as? Group)?.let {
            courseImage.setImageDrawable(ColorDrawable(it.backgroundColor))
        }

        collapsingToolbarLayout.setContentScrimColor(canvasContext.backgroundColor)

        // If course color overlay is disabled we show a static toolbar and hide the text overlay
        overlayToolbar.setupAsBackButton(this)
        noOverlayToolbar.setupAsBackButton(this)
        noOverlayToolbar.title = canvasContext.name
        (canvasContext as? Course)?.term?.name?.let { noOverlayToolbar.subtitle = it }
        noOverlayToolbar.setBackgroundColor(canvasContext.backgroundColor)
        updateToolbarVisibility()

        // Hide image placeholder if color overlay is disabled and there is no valid image
        val hasImage = (canvasContext as? Course)?.imageUrl?.isValid() == true
        val hideImagePlaceholder = StudentPrefs.hideCourseColorOverlay && !hasImage

        if (requireContext().a11yManager.isSwitchAccessEnabled || hideImagePlaceholder) {
            appBarLayout.setExpanded(false, false)
            appBarLayout.isActivated = false
            appBarLayout.isFocusable = false
            val layoutParams = (appBarLayout.layoutParams as? CoordinatorLayout.LayoutParams)
            val behavior = layoutParams?.behavior as? DisableableAppBarLayoutBehavior
            behavior?.isEnabled = false
        }

        swipeRefreshLayout.setOnRefreshListener { loadTabs(true) }

        loadTabs()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onColorOverlayToggled(event: CourseColorOverlayToggledEvent) {
        (canvasContext as? Course)?.let {
            courseImage.setCourseImage(it, it.backgroundColor, !StudentPrefs.hideCourseColorOverlay)
        }
        updateToolbarVisibility()
    }

    private fun updateToolbarVisibility() {
        val useOverlay = !StudentPrefs.hideCourseColorOverlay
        noOverlayToolbar.setVisible(!useOverlay)
        overlayToolbar.setVisible(useOverlay)
        courseHeader.setVisible(useOverlay)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Set course image again after orientation change to ensure correct scale/crop
        (canvasContext as? Course)?.let { courseImage.setCourseImage(it, it.backgroundColor, !StudentPrefs.hideCourseColorOverlay) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCalls?.cancel()
    }
    //endregion

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        ViewStyler.colorToolbarIconsAndText(requireActivity(), noOverlayToolbar, requireContext().getColor(R.color.white))
        ViewStyler.colorToolbarIconsAndText(requireActivity(), overlayToolbar, requireContext().getColor(R.color.white))
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.backgroundColor)
    }

    override fun getFragment(): Fragment? = this

    override fun title(): String = canvasContext.name ?: ""
    //endregion

    private fun loadTabs(isRefresh: Boolean = false) {
        apiCalls?.cancel()
        apiCalls = tryWeave {
            swipeRefreshLayout.isRefreshing = true

            // We don't want to list external tools that are hidden
            var homePageTitle: String? = null
            val isHomeAPage = if (canvasContext is Course) TabHelper.isHomeTabAPage(canvasContext as Course) else false // Courses are the only CanvasContext that have settable home pages

            if(isHomeAPage) {
                val homePage = awaitApi<Page> { PageManager.getFrontPage(canvasContext, isRefresh, it) }
                homePageTitle = homePage.title
            }

            val tabs = awaitApi<List<Tab>> { TabManager.getTabs(canvasContext, it, isRefresh) }.filter { !(it.isExternal && it.isHidden) }

            // Finds the home tab so we can reorder them if necessary
            val sortedTabs = tabs.toMutableList()
            sortedTabs.sortBy { if (TabHelper.isHomeTab(it)) -1 else 1 }

            courseBrowserRecyclerView.adapter = CourseBrowserAdapter(sortedTabs, canvasContext, homePageTitle) { tab ->
                if (isHomeAPage && TabHelper.isHomeTab(tab, canvasContext as Course)) {
                    // Load Pages List
                    if (tabs.any { it.tabId == Tab.PAGES_ID }) {
                        // Do not load the pages list if the tab is hidden or locked.
                        RouteMatcher.route(requireActivity(), TabHelper.getRouteByTabId(tab, canvasContext))
                    }

                    // If the home tab is a Page and we clicked it lets route directly there.
                    RouteMatcher.route(requireActivity(), PageDetailsFragment.makeRoute(canvasContext, Page.FRONT_PAGE_NAME).apply { ignoreDebounce = true })
                } else {
                    val route = TabHelper.getRouteByTabId(tab, canvasContext)?.apply { ignoreDebounce = true }
                    RouteMatcher.route(requireActivity(), route)
                }
            }

            swipeRefreshLayout.isRefreshing = false
        } catch {
            swipeRefreshLayout.isRefreshing = false

            if (it is StatusCallbackError && it.response?.code() == 401) {
                toast(R.string.unauthorized)
                activity?.onBackPressed()
            } else {
                toast(R.string.errorOccurred)
            }
        }
    }

    /**
     * Manages state of titles & subtitles when users scrolls
     */
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val percentage = Math.abs(verticalOffset).div(appBarLayout?.totalScrollRange?.toFloat() ?: 1F)

        if (percentage <= 0.3F) {
            val toolbarAnimation = if(courseBrowserHeader==null) null else ObjectAnimator.ofFloat(courseBrowserHeader, View.ALPHA, courseBrowserHeader.alpha, 0F)
            val titleAnimation = if(courseBrowserTitle==null) null else ObjectAnimator.ofFloat(courseBrowserTitle, View.ALPHA, courseBrowserTitle.alpha, 1F)
            val subtitleAnimation = if(courseBrowserSubtitle==null) null else ObjectAnimator.ofFloat(courseBrowserSubtitle, View.ALPHA, courseBrowserSubtitle.alpha, 0.8F)

            toolbarAnimation?.setAutoCancel(true)
            titleAnimation?.setAutoCancel(true)
            subtitleAnimation?.setAutoCancel(true)

            toolbarAnimation?.target = courseBrowserHeader
            titleAnimation?.target = courseBrowserTitle
            subtitleAnimation?.target = courseBrowserSubtitle

            toolbarAnimation?.duration = 200
            titleAnimation?.duration = 320
            subtitleAnimation?.duration = 320

            toolbarAnimation?.start()
            titleAnimation?.start()
            subtitleAnimation?.start()

        } else if (percentage > 0.7F) {
            val toolbarAnimation = if(courseBrowserHeader==null) null else ObjectAnimator.ofFloat(courseBrowserHeader, View.ALPHA, courseBrowserHeader.alpha, 1F)
            val titleAnimation = if(courseBrowserTitle==null) null else ObjectAnimator.ofFloat(courseBrowserTitle, View.ALPHA, courseBrowserTitle.alpha, 0F)
            val subtitleAnimation = if(courseBrowserSubtitle==null) null else ObjectAnimator.ofFloat(courseBrowserSubtitle, View.ALPHA, courseBrowserSubtitle.alpha, 0F)

            toolbarAnimation?.setAutoCancel(true)
            titleAnimation?.setAutoCancel(true)
            subtitleAnimation?.setAutoCancel(true)

            toolbarAnimation?.target = courseBrowserHeader
            titleAnimation?.target = courseBrowserTitle
            subtitleAnimation?.target = courseBrowserSubtitle

            toolbarAnimation?.duration = 200
            titleAnimation?.duration = 200
            subtitleAnimation?.duration = 200

            toolbarAnimation?.start()
            titleAnimation?.start()
            subtitleAnimation?.start()
        }
    }

    companion object {
        fun newInstance(route: Route) =
                if (validateRoute(route)) CourseBrowserFragment().apply {
                    arguments = route.canvasContext!!.makeBundle(route.arguments)
                } else null

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun makeRoute(canvasContext: CanvasContext?) = Route(CourseBrowserFragment::class.java, canvasContext)
    }
}
