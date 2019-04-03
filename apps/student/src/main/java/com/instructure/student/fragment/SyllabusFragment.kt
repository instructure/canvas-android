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

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.android.synthetic.main.fragment_syllabus.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView(url = "{canvasContext}/assignments/syllabus")
class SyllabusFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var apiJob: WeaveJob? = null

    // model variables
    private var syllabus by NullableParcelableArg<ScheduleItem>()

    override fun title(): String {
        return if (syllabus != null && syllabus!!.title!!.isNotBlank()) syllabus!!.title!!
        else getString(R.string.syllabus)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_syllabus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        canvasWebView.addVideoClient(requireActivity())
        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                openMedia(mime, url, filename, canvasContext)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
            }
        }

        canvasWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                return true
            }

            override fun launchInternalWebViewFragment(url: String) {
                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(canvasContext, url, false))
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (syllabus == null || syllabus!!.description == null) getCourseSyllabus() else populateViews()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (syllabus?.description == null) {
            emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    emptyView.setGuidelines(.28f, .47f, .62f, .12f, .88f)
                } else {
                    emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    private fun getCourseSyllabus() {
        apiJob = tryWeave {
            val course = awaitApi<Course> { CourseManager.getCourseWithSyllabus(canvasContext.id, it, true) }
            if (!course.syllabusBody.isNullOrBlank()) {
                emptyView.visibility = View.GONE
                syllabus = ScheduleItem(
                        itemType = ScheduleItem.Type.TYPE_SYLLABUS,
                        title = course.name,
                        description = course.syllabusBody
                )
                populateViews()
            } else {
                //No syllabus
                setEmptyView(emptyView, R.drawable.vd_panda_nosyllabus, R.string.noSyllabus, R.string.noSyllabusSubtext)
            }
        } catch {
            //No syllabus
            emptyView.emptyViewText(R.string.syllabusMissing)
            emptyView.setListEmpty()
        }
    }

    override fun applyTheme() {
        toolbar.title = title()
        setupToolbarMenu(toolbar)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override fun onPause() {
        super.onPause()
        canvasWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        canvasWebView.onResume()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(SyllabusFragment::class.java) == true) {
                canvasWebView.onResume()
            } else {
                canvasWebView.onPause()
            }
        }
    }

    override fun handleBackPressed(): Boolean {
        return canvasWebView.handleGoBack()
    }

    private fun populateViews() {
        if (activity == null || syllabus?.itemType != ScheduleItem.Type.TYPE_SYLLABUS) {
            return
        }

        toolbar.title = title()
        canvasWebView.loadHtml(syllabus!!.description, syllabus!!.title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiJob?.cancel()
    }

    companion object {

        private const val SYLLABUS = "syllabus"

        @JvmStatic
        fun newInstance(route: Route) : SyllabusFragment? {
            return if(validRoute(route)) SyllabusFragment().apply {
                arguments = route.arguments

                with(nonNullArgs) {
                    if (containsKey(SYLLABUS)) syllabus = getParcelable(SYLLABUS)
                }

                this.canvasContext = route.canvasContext!!
            } else null
        }

        @JvmStatic
        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.containsKey(SYLLABUS)
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, syllabus: ScheduleItem): Route {
            return Route(null, SyllabusFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(SYLLABUS, syllabus) }))
        }
    }
}
