/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_BASIC
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.OnBackStackChangedEvent
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.databinding.FragmentAssignmentBasicBinding
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_ASSIGNMENT_BASIC)
class AssignmentBasicFragment : ParentFragment() {

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    private val binding by viewBinding(FragmentAssignmentBasicBinding::bind)

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var assignment: Assignment by ParcelableArg()
    private var loadHtmlJob: Job? = null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_assignment_basic, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        binding.assignmentWebViewWrapper.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.assignmentWebViewWrapper.webView.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    //endregion

    //region Setup
    private fun setupViews() = with(binding) {
        if (assignment.dueAt != null) {
            dueDate.text = getString(R.string.dueAtTime, DateHelper.getDateTimeString(activity, assignment.dueDate))
        } else {
            dueDateWrapper.setGone()
        }

        assignmentWebViewWrapper.webView.addVideoClient(requireActivity())
        assignmentWebViewWrapper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                // Create and add the InternalWebviewFragment to deal with the link they clicked
                val route = InternalWebviewFragment.makeRoute(url, "", false, "")
                val fragment = InternalWebviewFragment.newInstance(route)
                val ft = requireActivity().supportFragmentManager.beginTransaction()
                ft.setCustomAnimations(R.anim.slide_in_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_out_to_bottom)
                ft.add(R.id.fullscreen, fragment, fragment.javaClass.name)
                ft.addToBackStack(fragment.javaClass.name)
                ft.commitAllowingStateLoss()

            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        assignmentWebViewWrapper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(requireActivity(), url)
            }
            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
            }
        }

        // Assignment description can be null
        var description = when {
            assignment.isLocked -> getLockedInfoHTML(assignment.lockInfo!!, R.string.lockedAssignmentDesc)
            assignment.lockDate?.before(Calendar.getInstance(Locale.getDefault()).time) == true ->
                // If an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
                // but no lock info because it isn't Locked as part of a module
                assignment.lockExplanation
            else -> assignment.description
        }

        if (description.isNullOrBlank() || description == "null") {
            description = "<p>" + getString(R.string.noDescription) + "</p>"
        }

        loadHtmlJob = assignmentWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), featureFlagProvider, description, {
            assignmentWebViewWrapper.loadHtml(it, assignment.name)
        }, {
            RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), canvasContext, it))
        }, courseId = canvasContext.id)
    }

    //endregion

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(AssignmentBasicFragment::class.java) == true) {
                binding.assignmentWebViewWrapper.webView.onResume()
            } else {
                binding.assignmentWebViewWrapper.webView.onPause()
            }
        }
    }
    //endregion

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        binding.toolbar.let {
            it.title = assignment.name ?: ""
            it.setupAsBackButton(this)
            ViewStyler.themeToolbarColored(requireActivity(), it, canvasContext)
        }
    }

    override fun title(): String = assignment.name ?: ""
    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed(): Boolean = binding.assignmentWebViewWrapper.webView.handleGoBack() ?: super.handleBackPressed()
    //endregion

    private fun getLockedInfoHTML(lockInfo: LockInfo, explanationFirstLine: Int): String {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        // Get the Locked message and make the module name bold
        var lockedMessage = ""

        if (lockInfo.lockedModuleName != null) {
            lockedMessage = "<p>" + String.format(requireContext().getString(explanationFirstLine), "<b>" + lockInfo.lockedModuleName + "</b>") + "</p>"
        }

        if (lockInfo.modulePrerequisiteNames!!.size > 0) {
            // We only want to add this text if there are module completion requirements
            lockedMessage += getString(R.string.mustComplete) + "<ul>"
            for (i in 0 until lockInfo.modulePrerequisiteNames!!.size) {
                lockedMessage += "<li>" + lockInfo.modulePrerequisiteNames!![i] + "</li>"  // "&#8226; "
            }
            lockedMessage += "</ul>"
        }

        // Check to see if there is an unlocked date
        if (lockInfo.unlockDate?.after(Date()) == true) {
            val unlocked = DateHelper.getDateTimeString(requireContext(), lockInfo.unlockDate)
            // If there is an unlock date but no module then the assignment is Locked
            if (lockInfo.contextModule == null) {
                lockedMessage = "<p>" + requireContext().getString(R.string.lockedAssignmentNotModule) + "</p>"
            }
            lockedMessage += requireContext().getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>"
        }

        return lockedMessage
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, assignment: Assignment): Route {
            val bundle = Bundle().apply { putParcelable(Const.ASSIGNMENT, assignment) }
            return Route(AssignmentBasicFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): AssignmentBasicFragment? {
            if (!validRoute(route)) return null
            return AssignmentBasicFragment().withArgs(route.argsWithContext)
        }

        private fun validRoute(route: Route) = route.canvasContext != null && route.arguments.containsKey(Const.ASSIGNMENT)
    }
}
