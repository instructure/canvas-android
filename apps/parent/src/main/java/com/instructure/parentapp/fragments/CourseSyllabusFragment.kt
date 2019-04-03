/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.canvasapi2.RequestInterceptor
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.android.synthetic.main.fragment_syllabus.*
import retrofit2.Response

class CourseSyllabusFragment : ParentFragment() {

    // model variables
    private var syllabus: ScheduleItem? = null

    // callbacks
    private lateinit var syllabusCallback: StatusCallback<Course>

    private var course by ParcelableArg<Course>(key = Const.COURSE)
    private var student by ParcelableArg<User>(key = Const.USER)

    override val rootLayout: Int
        get() = R.layout.fragment_syllabus

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogToolbar(view)
        description.addVideoClient(activity)
        description.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                return true
            }

            override fun launchInternalWebViewFragment(url: String) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                val internalWebviewFragment = InternalWebViewFragment()
                internalWebviewFragment.arguments = InternalWebViewFragment.createBundle(url + RequestInterceptor.sessionLocaleString, "", null, student)

                val ft = requireFragmentManager().beginTransaction()
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.javaClass.name)
                ft.addToBackStack(internalWebviewFragment.javaClass.name)
                ft.commitAllowingStateLoss()
            }
        }

        description.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {

            }

            override fun onPageStartedCallback(webView: WebView, url: String) {

            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {

            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                val uri = Uri.parse(url)
                return RouteMatcher.canRouteInternally(null, url, student, uri.host, false)
            }

            override fun routeInternallyCallback(url: String) {
                val uri = Uri.parse(url)
                RouteMatcher.canRouteInternally(activity, url, student, uri.host, true)
            }
        }
    }

    override fun setupDialogToolbar(rootView: View) {
        super.setupDialogToolbar(rootView)

        toolbarTitle.setText(R.string.syllabus)
    }

    internal fun populateViews() {
        if (activity == null) return

        if (syllabus == null || syllabus?.itemType != ScheduleItem.Type.TYPE_SYLLABUS) {
            //course has no syllabus
            emptyTextView.visibility = View.VISIBLE
            return
        }

        description.formatHTML(syllabus?.description, syllabus?.title)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupCallbacks()

        if (syllabus == null || syllabus!!.description == null) {
            CourseManager.getCourseWithSyllabus(
                    course.id,
                    syllabusCallback,
                    true
            )
        } else {
            populateViews()
        }
    }

    override fun onPause() {
        super.onPause()
        description.onPause()
    }

    override fun onResume() {
        super.onResume()
        description.onResume()
    }


    private fun setupCallbacks() {
        syllabusCallback = object : StatusCallback<Course>() {
            override fun onResponse(response: Response<Course>, linkHeaders: LinkHeaders, type: ApiType) {
                val course = response.body()
                if (course?.syllabusBody != null) {
                    syllabus = ScheduleItem(
                            itemType = ScheduleItem.Type.TYPE_SYLLABUS,
                            title = course.name,
                            description = course.syllabusBody
                    )
                    populateViews()
                } else {
                    // Course has no syllabus?
                    populateViews()
                }
            }
        }
    }

    companion object {

        fun newInstance(course: Course, student: User): CourseSyllabusFragment {
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            args.putParcelable(Const.USER, student)
            val fragment = CourseSyllabusFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
