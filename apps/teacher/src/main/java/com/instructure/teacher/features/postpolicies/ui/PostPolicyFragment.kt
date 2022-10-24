/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.postpolicies.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import kotlinx.android.synthetic.main.fragment_post_policy_settings.*
import kotlinx.android.synthetic.main.fragment_post_policy_settings.view.*

class PostPolicyFragment : Fragment() {

    private var assignment: Assignment by ParcelableArg(Assignment(), Const.ASSIGNMENT)
    private var course: Course by ParcelableArg(Course(), Const.CANVAS_CONTEXT)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_post_policy_settings, container, false)

        val titles = listOf(getString(R.string.postGradesTab), getString(R.string.hideGradesTab))
        view.postPolicyPager.adapter = PostPolicyPagerAdapter(course, assignment, childFragmentManager, titles)
        view.postPolicyTabLayout.setupWithViewPager(view.postPolicyPager, true)

        return view
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun applyTheme() {
        postPolicyToolbar.subtitle = assignment.name
        postPolicyToolbar.setupBackButtonAsBackPressedOnly(this)

        ViewStyler.themeToolbarLight(requireActivity(), postPolicyToolbar)

        val courseColor = course.textAndIconColor
        postPolicyTabLayout.setSelectedTabIndicatorColor(courseColor)
        postPolicyTabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.textDark), courseColor)
    }

    companion object {
        fun makeRoute(course: Course, assignment: Assignment) =
            Route(PostPolicyFragment::class.java, course, Bundle().apply { putParcelable(Const.ASSIGNMENT, assignment) })

        fun newInstance(args: Bundle) = PostPolicyFragment().withArgs(args)
    }
}

private class PostPolicyPagerAdapter(val course: Course, val assignment: Assignment, fragmentManager: FragmentManager, val titles: List<String>) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount() = 2
    override fun getPageTitle(position: Int) = titles[position]

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PostGradeFragment.newInstance(assignment, false)
            1 -> PostGradeFragment.newInstance(assignment, true)
            else -> throw IndexOutOfBoundsException("No post policy adapter item at position $position")
        }
    }
}