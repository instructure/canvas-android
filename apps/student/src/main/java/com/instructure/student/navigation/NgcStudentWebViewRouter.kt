/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.navigation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.interactions.FragmentInteractions
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.getResourceSelectorUrl

/**
 * [WebViewRouter] for the NGC experience. NGC runs in its own Activity (not inside the main
 * [com.instructure.student.activity.NavigationActivity], which is `singleTask`), so routing links
 * through [RouteMatcher] would reset that activity to a root route and surface its bottom navigation
 * bar. To match the Academic Assignment Details behavior (links open without the bottom bar), link
 * navigations open in the standalone [InternalWebViewActivity] and media opens in the media viewer.
 */
class NgcStudentWebViewRouter(private val activity: FragmentActivity) : WebViewRouter {

    // Never route into the bottom-nav shell; links are opened in the standalone web view instead.
    override fun canRouteInternally(url: String, routeIfPossible: Boolean): Boolean = false

    override fun routeInternally(url: String, extras: Bundle?) = openInWebView(url)

    override fun openMedia(url: String, mime: String, filename: String, canvasContext: CanvasContext?) {
        RouteMatcher.openMedia(activity, url)
    }

    override fun routeExternally(url: String) = openInWebView(url)

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) {
        RouteMatcher.route(activity, LtiLaunchFragment.makeSessionlessLtiUrlRoute(activity, canvasContext, url))
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) = openInWebView(url)

    // New Quizzes (and other assignment LTIs) need the assignment-LTI launch flow with the
    // pre-fetched tool; a plain sessionless launch of the tool URL fails. Hosted over the NGC
    // content (not routed to NavigationActivity) so there's no bottom bar and back returns to NGC.
    override fun launchAssignmentLtiTool(
        canvasContext: CanvasContext?,
        url: String,
        title: String?,
        ltiTool: LTITool?,
        openInternally: Boolean,
    ) {
        val route = LtiLaunchFragment.makeRoute(
            canvasContext = canvasContext ?: CanvasContext.emptyCourseContext(),
            url = url,
            title = title,
            sessionLessLaunch = false,
            assignmentLti = true,
            ltiTool = ltiTool,
            openInternally = openInternally,
        )
        LtiLaunchFragment.newInstance(route)?.let { showFragmentOverNgc(it) }
    }

    // Classic quiz: show the dedicated quiz screen over the NGC content (its own course-colored
    // toolbar, no bottom bar, back returns to NGC).
    override fun launchInternalQuiz(canvasContext: CanvasContext?, quiz: Quiz, url: String) {
        val route = BasicQuizViewFragment.makeRoute(canvasContext ?: CanvasContext.emptyCourseContext(), quiz, url)
        BasicQuizViewFragment.newInstance(route)?.let { showFragmentOverNgc(it) }
    }

    // Studio: reuse the academic flow (StudioWebViewFragment with the resource-selector URL and next
    // attempt), hosted over the NGC content so there's no bottom bar and back returns to NGC.
    override fun launchStudio(canvasContext: CanvasContext?, assignment: Assignment, studioLTITool: LTITool?) {
        val context = canvasContext ?: CanvasContext.emptyCourseContext()
        val nextAttempt = (assignment.submission?.attempt ?: 0) + 1
        val route = StudioWebViewFragment.makeRoute(
            context,
            studioLTITool?.getResourceSelectorUrl(context, assignment).orEmpty(),
            studioLTITool?.name.orEmpty(),
            true,
            assignment,
            nextAttempt.toLong(),
        )
        StudioWebViewFragment.newInstance(route)?.let { showFragmentOverNgc(it) }
    }

    // NGC runs its UI via Compose in its own Activity; hosting these fragments over its content
    // (instead of routing to NavigationActivity) keeps the bottom bar hidden and makes back/close
    // (popBackStack) return to the NGC screen.
    private fun showFragmentOverNgc(fragment: Fragment) {
        val fragmentManager = activity.supportFragmentManager
        fragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
        // NavigationActivity normally themes a fragment's own toolbar (title, back button, course
        // color) via FragmentInteractions.applyTheme(); replicate that here since we host it
        // ourselves. (LtiLaunchFragment themes its toolbar in onViewCreated, so it's a no-op there.)
        fragmentManager.executePendingTransactions()
        (fragment as? FragmentInteractions)?.applyTheme()

        // NGC's Compose navigation registers its back callback during setContent (after the
        // FragmentManager's), so it would otherwise win and pop the NGC screen behind the fragment.
        // Register a higher-priority callback tied to this fragment that lets the fragment handle
        // back first (e.g. WebView go-back) and otherwise pops the fragment back to NGC.
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val handledByFragment = (fragment as? NavigationCallbacks)?.onHandleBackPressed() == true
                if (!handledByFragment) fragmentManager.popBackStack()
            }
        }
        activity.onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, onBackPressedCallback)
    }

    private fun openInWebView(url: String) {
        activity.startActivity(InternalWebViewActivity.createIntent(activity, url, "", false))
    }
}
