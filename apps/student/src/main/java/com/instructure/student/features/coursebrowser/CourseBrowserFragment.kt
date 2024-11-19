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
package com.instructure.student.features.coursebrowser

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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_BROWSER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.a11yManager
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isSwitchAccessEnabled
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setCourseImage
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.adapter.CourseBrowserAdapter
import com.instructure.student.databinding.FragmentCourseBrowserBinding
import com.instructure.student.events.CourseColorOverlayToggledEvent
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Const
import com.instructure.student.util.DisableableAppBarLayoutBehavior
import com.instructure.student.util.StudentPrefs
import com.instructure.student.util.TabHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_COURSE_BROWSER)
@PageView(url = "{canvasContext}")
@AndroidEntryPoint
class CourseBrowserFragment : BaseCanvasFragment(), FragmentInteractions, AppBarLayout.OnOffsetChangedListener {

    @Inject
    lateinit var repository: CourseBrowserRepository

    private val binding by viewBinding(FragmentCourseBrowserBinding::bind)

    private var apiCalls: Job? = null

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override val navigation: Navigation?
        get() = if (activity is Navigation) activity as Navigation else null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_course_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        appBarLayout.addOnOffsetChangedListener(this@CourseBrowserFragment)
        collapsingToolbarLayout.isTitleEnabled = false

        courseBrowserTitle.text = canvasContext.name

        (canvasContext as? Course)?.let {
            courseImage.setCourseImage(it, it.color, !StudentPrefs.hideCourseColorOverlay)
            courseBrowserSubtitle.text = it.term?.name ?: ""
            binding.courseBrowserHeader.courseBrowserHeader.setTitleAndSubtitle(it.name, it.term?.name ?: "")
        }

        (canvasContext as? Group)?.let {
            courseImage.setImageDrawable(ColorDrawable(it.color))
        }

        collapsingToolbarLayout.setContentScrimColor(canvasContext.color)

        // If course color overlay is disabled we show a static toolbar and hide the text overlay
        overlayToolbar.setupAsBackButton(this@CourseBrowserFragment)
        noOverlayToolbar.setupAsBackButton(this@CourseBrowserFragment)
        noOverlayToolbar.title = canvasContext.name
        (canvasContext as? Course)?.term?.name?.let { noOverlayToolbar.subtitle = it }
        noOverlayToolbar.setBackgroundColor(canvasContext.color)
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
            binding.courseImage.setCourseImage(it, it.color, !StudentPrefs.hideCourseColorOverlay)
        }
        updateToolbarVisibility()
    }

    private fun updateToolbarVisibility() = with(binding) {
        val useOverlay = !StudentPrefs.hideCourseColorOverlay
        noOverlayToolbar.setVisible(!useOverlay)
        overlayToolbar.setVisible(useOverlay)
        courseHeader.setVisible(useOverlay)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Set course image again after orientation change to ensure correct scale/crop
        (canvasContext as? Course)?.let {
            binding.courseImage.setCourseImage(
                it,
                it.color,
                !StudentPrefs.hideCourseColorOverlay
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCalls?.cancel()
    }
    //endregion

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        ViewStyler.colorToolbarIconsAndText(
            requireActivity(),
            binding.noOverlayToolbar,
            requireContext().getColor(R.color.textLightest)
        )
        ViewStyler.colorToolbarIconsAndText(
            requireActivity(),
            binding.overlayToolbar,
            requireContext().getColor(R.color.textLightest)
        )
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.color)
    }

    override fun getFragment(): Fragment? = this

    override fun title(): String = canvasContext.name ?: ""
    //endregion

    private fun loadTabs(isRefresh: Boolean = false) = with(binding) {
        apiCalls?.cancel()
        apiCalls = tryWeave {
            swipeRefreshLayout.isRefreshing = true

            // We don't want to list external tools that are hidden
            var homePageTitle: String? = null
            val isHomeAPage =
                if (canvasContext is Course) TabHelper.isHomeTabAPage(canvasContext as Course) else false // Courses are the only CanvasContext that have settable home pages

            if (isHomeAPage) {
                val homePage = repository.getFrontPage(canvasContext, isRefresh)
                homePageTitle = homePage?.title
            }

            val tabs = repository.getTabs(canvasContext, isRefresh)

            // Finds the home tab so we can reorder them if necessary
            val sortedTabs = tabs.toMutableList()
            sortedTabs.sortBy { if (TabHelper.isHomeTab(it)) -1 else 1 }

            courseBrowserRecyclerView.adapter = CourseBrowserAdapter(sortedTabs, canvasContext, homePageTitle) { tab ->
                if (isHomeAPage && TabHelper.isHomeTab(tab, canvasContext as Course)) {
                    // Load Pages List
                    if (tabs.any { it.tabId == Tab.PAGES_ID }) {
                        // Do not load the pages list if the tab is hidden or locked.
                        val route = TabHelper.getRouteByTabId(tab, canvasContext)
                        route?.arguments = route?.arguments?.apply {
                            putString(PageDetailsFragment.PAGE_NAME, homePageTitle)
                        } ?: Bundle()
                        RouteMatcher.route(requireActivity(), route)
                    }

                    // If the home tab is a Page and we clicked it lets route directly there.
                    val route = PageDetailsFragment.makeFrontPageRoute(canvasContext)
                        .apply { ignoreDebounce = true }
                    route.arguments = route.arguments.apply {
                        putString(PageDetailsFragment.PAGE_NAME, homePageTitle)
                    }
                    RouteMatcher.route(requireActivity(), route)
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
        if (view == null) return

        val percentage = Math.abs(verticalOffset).div(appBarLayout?.totalScrollRange?.toFloat() ?: 1F)

        if (percentage <= 0.3F) {
            val toolbarAnimation =
                if (binding.courseBrowserHeader.courseBrowserHeader == null) null else ObjectAnimator.ofFloat(
                    binding.courseBrowserHeader.courseBrowserHeader,
                    View.ALPHA,
                    binding.courseBrowserHeader.courseBrowserHeader.alpha,
                    0F
                )
            val titleAnimation =
                ObjectAnimator.ofFloat(binding.courseBrowserTitle, View.ALPHA, binding.courseBrowserTitle.alpha, 1F)
            val subtitleAnimation = ObjectAnimator.ofFloat(
                binding.courseBrowserSubtitle,
                View.ALPHA,
                binding.courseBrowserSubtitle.alpha,
                0.8F
            )

            toolbarAnimation?.setAutoCancel(true)
            titleAnimation?.setAutoCancel(true)
            subtitleAnimation?.setAutoCancel(true)

            toolbarAnimation?.target = binding.courseBrowserHeader.courseBrowserHeader
            titleAnimation?.target = binding.courseBrowserTitle
            subtitleAnimation?.target = binding.courseBrowserSubtitle

            toolbarAnimation?.duration = 200
            titleAnimation?.duration = 320
            subtitleAnimation?.duration = 320

            toolbarAnimation?.start()
            titleAnimation?.start()
            subtitleAnimation?.start()

        } else if (percentage > 0.7F) {
            val toolbarAnimation =
                if (binding.courseBrowserHeader.courseBrowserHeader == null) null else ObjectAnimator.ofFloat(
                    binding.courseBrowserHeader.courseBrowserHeader,
                    View.ALPHA,
                    binding.courseBrowserHeader.courseBrowserHeader.alpha,
                    1F
                )
            val titleAnimation =
                ObjectAnimator.ofFloat(binding.courseBrowserTitle, View.ALPHA, binding.courseBrowserTitle.alpha, 0F)
            val subtitleAnimation = ObjectAnimator.ofFloat(
                binding.courseBrowserSubtitle,
                View.ALPHA,
                binding.courseBrowserSubtitle.alpha,
                0F
            )

            toolbarAnimation?.setAutoCancel(true)
            titleAnimation?.setAutoCancel(true)
            subtitleAnimation?.setAutoCancel(true)

            toolbarAnimation?.target = binding.courseBrowserHeader.courseBrowserHeader
            titleAnimation?.target = binding.courseBrowserTitle
            subtitleAnimation?.target = binding.courseBrowserSubtitle

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
