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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.globalName
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.pandautils.analytics.SCREEN_VIEW_COURSE_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setCourseImage
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentCourseSettingsBinding
import com.instructure.teacher.dialog.EditCourseNameDialog
import com.instructure.teacher.dialog.RadioButtonDialog
import com.instructure.teacher.factory.CourseSettingsFragmentPresenterFactory
import com.instructure.teacher.presenters.CourseSettingsFragmentPresenter
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.CourseSettingsFragmentView

@PageView
@ScreenView(SCREEN_VIEW_COURSE_SETTINGS)
class CourseSettingsFragment : BasePresenterFragment<
        CourseSettingsFragmentPresenter,
        CourseSettingsFragmentView,
        FragmentCourseSettingsBinding>(),
    CourseSettingsFragmentView {

    private var course: Course by ParcelableArg(default = Course())

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "courses/${course.id}/settings"

    private val mHomePages: Map<String, String> by lazy {
        // Use LinkedHashMap map to keep order consistent between API levels
        linkedMapOf(
                Pair("feed", getString(R.string.course_activity_stream)),
                Pair("wiki", getString(R.string.pages_front_page)),
                Pair("modules", getString(R.string.course_modules)),
                Pair("assignments", getString(R.string.assignments_list)),
                Pair("syllabus", getString(R.string.syllabus))
        )
    }

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentCourseSettingsBinding = FragmentCourseSettingsBinding::inflate

    override fun getPresenterFactory() = CourseSettingsFragmentPresenterFactory()

    override fun onReadySetGo(presenter: CourseSettingsFragmentPresenter) {
        setupToolbar()
        binding.courseImage.setCourseImage(course, course.color, !TeacherPrefs.hideCourseColorOverlay)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            updateCourseName(course)
            updateCourseHomePage(course.homePage)
        } else {
            updateCourseName(course)
        }
    }

    override fun onPresenterPrepared(presenter: CourseSettingsFragmentPresenter) = with(binding) {
        renameCourse.root.onClickWithRequireNetwork {
            presenter.editCourseNameClicked()
        }

        editCourseHomepage.root.onClickWithRequireNetwork {
            presenter.editCourseHomePageClicked()
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupBackButton(this@CourseSettingsFragment)
        toolbar.title = getString(R.string.course_settings)
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        toolbar.setSubtitleTextColor(course.color)
    }

    override fun showEditCourseNameDialog() {
        val dialog: EditCourseNameDialog = EditCourseNameDialog.getInstance(requireActivity().supportFragmentManager, course) { newName ->
            presenter.editCourseName(newName, course)
        }

        dialog.show(requireActivity().supportFragmentManager, EditCourseNameDialog::class.java.simpleName)
    }

    override fun showEditCourseHomePageDialog() {
        val (keys, values) = mHomePages.toList().unzip()
        val selectedIdx = keys.indexOf(course.homePage?.apiString)
        val dialog = RadioButtonDialog.getInstance(requireActivity().supportFragmentManager, getString(R.string.set_home_to), values as ArrayList<String>, selectedIdx) { idx ->
            presenter.editCourseHomePage(keys[idx], course)
        }

       dialog.show(requireActivity().supportFragmentManager, RadioButtonDialog::class.java.simpleName)
    }

    override fun updateCourseName(course: Course) = with(binding) {
        renameCourse.courseName.text = course.globalName
        toolbar.subtitle = course.globalName
        this@CourseSettingsFragment.course.globalName = course.globalName
        setResult()
    }

    override fun updateCourseHomePage(newHomePage: Course.HomePage?) {
        binding.editCourseHomepage.courseHomePage.text = mHomePages[newHomePage?.apiString]
        course.homePage = newHomePage
        setResult()
    }

    private fun setResult() {
        requireActivity().setResult(Activity.RESULT_OK, Intent().apply { putExtra(Const.COURSE, course as Parcelable) })
    }

    override fun onRefreshFinished() {}

    override fun onRefreshStarted() {}

    companion object {
        fun newInstance(course: Course) = CourseSettingsFragment().apply {
            this.course = course
        }
    }
}
