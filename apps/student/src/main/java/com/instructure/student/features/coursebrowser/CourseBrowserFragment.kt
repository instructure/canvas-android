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
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
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
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.SearchBar
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.notebookPen
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesConfig
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.features.notes.notebook.NotebookTray
import com.instructure.pandautils.features.notes.notebook.NotebookTrayArgs
import com.instructure.pandautils.features.smartsearch.SmartSearchFragment
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.a11yManager
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isSwitchAccessEnabled
import com.instructure.pandautils.utils.launchCustomTab
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
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_COURSE_BROWSER)
@PageView(url = "{canvasContext}")
@AndroidEntryPoint
class CourseBrowserFragment : BaseCanvasFragment(), FragmentInteractions,
    AppBarLayout.OnOffsetChangedListener {

    @Inject
    lateinit var repository: CourseBrowserRepository

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var observeWidgetConfigUseCase: ObserveWidgetConfigUseCase

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    private val binding by viewBinding(FragmentCourseBrowserBinding::bind)

    private var apiCalls: Job? = null

    // Backed by plain mutableStateOf fields (rather than remember{} inside the Compose content below)
    // because notebookIconVisible is resolved asynchronously from a Fragment-scoped coroutine, outside
    // of composition - see onViewCreated. searchBar (see fragment_course_browser.xml) is not
    // ViewModel/UiState-driven, unlike the rest of this feature - it mounts a small local Compose tree
    // via setContent{} for just its own icon/search bar/tray, matching how the SearchBar itself already
    // works here.
    private var notebookIconVisible by mutableStateOf(false)
    private var notebookTrayVisible by mutableStateOf(false)
    private var searchBarExpanded by mutableStateOf(false)

    // Whether the Smart Search tab exists for this course - decoupled from notebookIconVisible (see
    // onViewCreated below) so the notebook entry point doesn't depend on Smart Search being enabled
    // for the account: the searchBar host View's own visibility no longer implies the tab exists.
    private var smartSearchTabAvailable by mutableStateOf(false)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private val isDashboardRedesignEnabled by lazy {
        RemoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN)
    }

    private var hideColorOverlayFromConfig: Boolean = true

    /**
     * Returns whether to hide the color overlay.
     * Uses widget config if dashboard redesign is enabled, otherwise uses StudentPrefs.
     */
    private val hideColorOverlay: Boolean
        get() = if (isDashboardRedesignEnabled) {
            hideColorOverlayFromConfig
        } else {
            StudentPrefs.hideCourseColorOverlay
        }

    override val navigation: Navigation?
        get() = if (activity is Navigation) activity as Navigation else null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_course_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        // Gated identically to the existing Page-level Notebook entry point (PageDetailsViewModel):
        // the notebook feature flag AND the user having a student enrollment in this course. Notebook
        // is Student-app only (StudyNote mutations/queries hard-require course.user_is_student? server
        // side), so this is never shown for a Group's CourseBrowserFragment.
        (canvasContext as? Course)?.let { course ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    notebookIconVisible = course.isStudent && featureFlagProvider.notebookEnabled()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }

        // Observe widget config for color overlay setting when dashboard redesign is enabled
        if (isDashboardRedesignEnabled) {
            viewLifecycleOwner.lifecycleScope.launch {
                observeWidgetConfigUseCase(WidgetMetadata.WIDGET_ID_COURSES).collect { settings ->
                    val showColorOverlay = settings.firstOrNull { it.key == CoursesConfig.KEY_SHOW_COLOR_OVERLAY }?.value as? Boolean ?: false
                    hideColorOverlayFromConfig = !showColorOverlay

                    // Update UI when setting changes
                    (canvasContext as? Course)?.let {
                        courseImage.setCourseImage(it, it.color, !hideColorOverlay)
                    }
                    updateToolbarVisibility()
                }
            }
        }

        networkStateProvider.isOnlineLiveData.observe(viewLifecycleOwner) { isOnline ->
            // Not gated on smartSearchTabAvailable - the notebook icon must stay visible (while online)
            // even for a course with no Smart Search tab. Going offline closes an open tray instead of
            // just hiding its host, so state doesn't silently linger behind a gone View.
            searchBar.setVisible(isOnline)
            if (!isOnline) notebookTrayVisible = false
        }

        searchBar.apply {
            setContent {
                CanvasTheme {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (notebookIconVisible && !searchBarExpanded) {
                            IconButton(
                                onClick = { notebookTrayVisible = true },
                                modifier = Modifier.testTag("courseBrowserNotebookButton")
                            ) {
                                Icon(
                                    imageVector = InstUIIcons.notebookPen,
                                    contentDescription = stringResource(id = R.string.a11y_notebookOpenCourseNotes),
                                    tint = colorResource(R.color.textLightest)
                                )
                            }
                        }
                        if (smartSearchTabAvailable) {
                            SearchBar(
                                icon = R.drawable.ic_smart_search,
                                tintColor = colorResource(R.color.textLightest),
                                placeholder = stringResource(R.string.smartSearchPlaceholder),
                                onExpand = { expanded ->
                                    searchBarExpanded = expanded
                                    overlayToolbar.background =
                                        if (expanded) ColorDrawable(canvasContext.color) else null
                                    overlayToolbar.navigationIcon =
                                        if (expanded) {
                                            null
                                        } else {
                                            ContextCompat.getDrawable(
                                                requireContext(),
                                                R.drawable.ic_back_arrow
                                            )
                                        }
                                    ViewStyler.colorToolbarIconsAndText(
                                        requireActivity(),
                                        overlayToolbar,
                                        requireContext().getColor(R.color.textLightest)
                                    )
                                },
                                onSearch = { query ->
                                    RouteMatcher.route(requireActivity(), SmartSearchFragment.makeRoute(canvasContext, query))
                                },
                                collapseOnSearch = true
                            )
                        }
                    }

                    if (notebookTrayVisible) {
                        NotebookTray(
                            args = NotebookTrayArgs(
                                courseId = canvasContext.id,
                                courseTitle = canvasContext.name.orEmpty()
                            ),
                            courseColor = Color(canvasContext.color),
                            onDismiss = { notebookTrayVisible = false }
                        )
                    }
                }
            }
        }

        appBarLayout.addOnOffsetChangedListener(this@CourseBrowserFragment)
        collapsingToolbarLayout.isTitleEnabled = false

        courseBrowserTitle.text = canvasContext.name

        (canvasContext as? Course)?.let {
            courseImage.setCourseImage(it, it.color, !hideColorOverlay)
            courseBrowserSubtitle.text = it.term?.name ?: ""
            binding.courseBrowserHeader.courseBrowserHeader.setTitleAndSubtitle(
                it.name,
                it.term?.name ?: ""
            )
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
        appBarLayout.setBackgroundColor(canvasContext.color)

        // Apply top padding to noOverlayToolbar (skip when masquerading - MasqueradeUI handles it)
        if (!ApiPrefs.isMasquerading) {
            noOverlayToolbar.applyTopSystemBarInsets()
        }

        // Handle insets for color overlay mode
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Skip top insets when masquerading - MasqueradeUI handles it
            val topInset = if (ApiPrefs.isMasquerading) 0 else systemBars.top

            if (overlayToolbar.isVisible) {
                // Color overlay enabled: apply padding to push content below status bar
                view.setPadding(view.paddingLeft, topInset, view.paddingRight, view.paddingBottom)
            } else {
                view.setPadding(view.paddingLeft, 0, view.paddingRight, view.paddingBottom)
            }

            // Reset any margins
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.topMargin = 0
            view.layoutParams = layoutParams

            insets
        }

        ViewCompat.requestApplyInsets(appBarLayout)

        // Add a status bar background view when color overlay is enabled
        setupStatusBarBackground()

        updateToolbarVisibility()

        // Hide image placeholder if color overlay is disabled and there is no valid image
        val hasImage = (canvasContext as? Course)?.imageUrl?.isValid() == true
        val hideImagePlaceholder = hideColorOverlay && !hasImage

        if (requireContext().a11yManager.isSwitchAccessEnabled || hideImagePlaceholder) {
            appBarLayout.setExpanded(false, false)
            appBarLayout.isActivated = false
            appBarLayout.isFocusable = false
            val layoutParams = (appBarLayout.layoutParams as? CoordinatorLayout.LayoutParams)
            val behavior = layoutParams?.behavior as? DisableableAppBarLayoutBehavior
            behavior?.isEnabled = false
        }

        swipeRefreshLayout.setOnRefreshListener { loadTabs(true) }

        swipeRefreshLayout.applyBottomSystemBarInsets()

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
            binding.courseImage.setCourseImage(it, it.color, !hideColorOverlay)
        }
        updateToolbarVisibility()
    }

    private fun updateToolbarVisibility() = with(binding) {
        val useOverlay = !hideColorOverlay
        if (!useOverlay) {
            if (searchBar.parent == overlayToolbar) {
                overlayToolbar.removeView(searchBar)
                noOverlayToolbar.addView(searchBar)
                // searchBar's default ViewCompositionStrategy disposes its composition on detach, so
                // reparenting would silently reset an open tray's NavHost/ViewModel - close it instead.
                notebookTrayVisible = false
            }
        } else {
            if (searchBar.parent == noOverlayToolbar) {
                noOverlayToolbar.removeView(searchBar)
                overlayToolbar.addView(searchBar)
                notebookTrayVisible = false
            }
        }
        noOverlayToolbar.setVisible(!useOverlay)
        overlayToolbar.setVisible(useOverlay)
        courseHeader.setVisible(useOverlay)
        ViewCompat.requestApplyInsets(appBarLayout)
    }

    private var statusBarBackgroundView: View? = null

    private fun setupStatusBarBackground() {
        if (hideColorOverlay) {
            // Remove status bar background if it exists
            statusBarBackgroundView?.let {
                (it.parent as? ViewGroup)?.removeView(it)
            }
            statusBarBackgroundView = null
            return
        }

        // Find the fullscreen container in the activity
        val fullscreenContainer = activity?.findViewById<ViewGroup>(R.id.fullscreen) ?: return

        // Create or update the status bar background view
        if (statusBarBackgroundView == null) {
            statusBarBackgroundView = View(requireContext()).apply {
                id = View.generateViewId()
            }
            fullscreenContainer.addView(statusBarBackgroundView, 0)
        }

        statusBarBackgroundView?.apply {
            setBackgroundColor(canvasContext.color)
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val params = view.layoutParams
                    ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, systemBars.top)
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = systemBars.top
                view.layoutParams = params
                insets
            }
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Set course image again after orientation change to ensure correct scale/crop
        (canvasContext as? Course)?.let {
            binding.courseImage.setCourseImage(
                it,
                it.color,
                !hideColorOverlay
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCalls?.cancel()
        // Clean up the status bar background view
        statusBarBackgroundView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }
        statusBarBackgroundView = null
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

    override fun getFragment(): Fragment = this

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

            binding.searchBar.setVisible(networkStateProvider.isOnline())
            smartSearchTabAvailable = tabs.find { it.tabId == Tab.SEARCH_ID } != null

            // Finds the home tab so we can reorder them if necessary
            val sortedTabs = tabs.filter { it.tabId != Tab.SEARCH_ID }.toMutableList()
            sortedTabs.sortBy { if (TabHelper.isHomeTab(it)) -1 else 1 }

            courseBrowserRecyclerView.adapter =
                CourseBrowserAdapter(sortedTabs, canvasContext, homePageTitle, networkStateProvider.isOnline()) { tab ->
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
                    } else if (tab.isExternalNavMenuLink) {
                        openCustomLink(tab)
                    } else {
                        val route = TabHelper.getRouteByTabId(tab, canvasContext)
                            ?.apply { ignoreDebounce = true }
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

        val percentage =
            Math.abs(verticalOffset).div(appBarLayout?.totalScrollRange?.toFloat() ?: 1F)

        if (percentage <= 0.3F) {
            val toolbarAnimation =
                ObjectAnimator.ofFloat(
                    binding.courseBrowserHeader.courseBrowserHeader,
                    View.ALPHA,
                    binding.courseBrowserHeader.courseBrowserHeader.alpha,
                    0F
                )
            val titleAnimation =
                ObjectAnimator.ofFloat(
                    binding.courseBrowserTitle,
                    View.ALPHA,
                    binding.courseBrowserTitle.alpha,
                    1F
                )
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
                ObjectAnimator.ofFloat(
                    binding.courseBrowserHeader.courseBrowserHeader,
                    View.ALPHA,
                    binding.courseBrowserHeader.courseBrowserHeader.alpha,
                    1F
                )
            val titleAnimation =
                ObjectAnimator.ofFloat(
                    binding.courseBrowserTitle,
                    View.ALPHA,
                    binding.courseBrowserTitle.alpha,
                    0F
                )
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

    private fun openCustomLink(tab: Tab) {
        val url = tab.externalUrl?.takeIf { it.isNotBlank() } ?: tab.htmlUrl?.takeIf { it.isNotBlank() } ?: return
        if (!RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, routeIfPossible = true)) {
            requireActivity().launchCustomTab(url, canvasContext.color)
        }
    }

    companion object {
        fun newInstance(route: Route) =
            if (validateRoute(route)) CourseBrowserFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
            } else null

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun makeRoute(canvasContext: CanvasContext?) =
            Route(CourseBrowserFragment::class.java, canvasContext)
    }
}
