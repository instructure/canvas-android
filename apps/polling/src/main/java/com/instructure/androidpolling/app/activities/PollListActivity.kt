/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.androidpolling.app.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.asynctasks.PollsLogoutTask
import com.instructure.androidpolling.app.fragments.ClosedPollListFragment
import com.instructure.androidpolling.app.fragments.OpenPollExpandableListFragment
import com.instructure.androidpolling.app.fragments.ParentFragment
import com.instructure.androidpolling.app.util.ApplicationManager
import com.instructure.androidpolling.app.util.Constants
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Poll
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.tasks.LogoutTask
import com.jfeinstein.jazzyviewpager.JazzyViewPager
import kotlinx.android.synthetic.main.activity_poll_list.*
import java.util.*

open class PollListActivity : BaseActivity(), ParentFragment.OnUpdatePollListener {

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private var mPagerAdapter: PagerAdapter? = null

    private var courseList: ArrayList<Course>? = null
    private var user: User? = null
    private val hasTeacherEnrollment: Boolean = false
    private var fragments: ArrayList<Fragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poll_list)

        // Remember that the first view should be the student view
        ApplicationManager.setFirstView(this, false)

        user = ApiPrefs.user

        checkEnrollments(user)
        courseList = ApplicationManager.getCourseList(this)

        val pollsExist = checkPollsExist()

        if (!pollsExist) {
            displayEmptyState()
        }

        fragments = ArrayList()
        val bundle = Bundle()
        bundle.putSerializable(Constants.COURSES_LIST, courseList)
        val openPollExpandableListFragment = OpenPollExpandableListFragment()
        openPollExpandableListFragment.arguments = bundle

        fragments!!.add(openPollExpandableListFragment)

        // Now add the closed list
        val closedPollListFragment = ClosedPollListFragment()
        val closedBundle = Bundle()
        closedBundle.putSerializable(Constants.COURSES_LIST, courseList)
        closedPollListFragment.arguments = closedBundle

        fragments!!.add(closedPollListFragment)

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet)

        pager.adapter = mPagerAdapter
        // Bind the tabs to the ViewPager
        tabs.setViewPager(pager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (ApplicationManager.hasTeacherEnrollment(this)) {
            menuInflater.inflate(R.menu.switch_to_teacher_view, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                PollsLogoutTask(LogoutTask.Type.LOGOUT).execute()
                return true
            }
            R.id.action_switch_to_teacher -> {
                startActivity(FragmentManagerActivity.createIntent(this))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun reloadData(view: View) {
        (fragments!![pager!!.currentItem] as ParentFragment).reloadData()
    }

    private fun displayEmptyState() {
        // We don't want to show the tabs
        pager.visibility = View.GONE
        tabs.visibility = View.GONE
        noPollText.visibility = View.VISIBLE
    }

    //TODO: API call to check for polls
    private fun checkPollsExist(): Boolean = true

    /**
     * A simple pager adapter that represents 2 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        private val TITLES = arrayOf(getString(R.string.current_polls), getString(R.string.closed_polls))

        override fun getPageTitle(position: Int): CharSequence = TITLES[position]

        override fun getItem(position: Int): Fragment = fragments!![position]

        override fun getCount(): Int = if (fragments == null) 0 else fragments!!.size


        /**
         * Due to the limitations of the ViewPager class (which JazzyViewPager is built upon) in order to get the animations
         * to work correctly for more than 3 Views, you'll have to add the following to the instantiateItem method of your PagerAdapter.
         */
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val obj = super.instantiateItem(container, position)
            pager!!.setObjectForPosition(obj, position)
            return obj
        }
    }

    override fun onUpdatePoll(poll: Poll, fragmentTag: String) = Unit

    companion object {

        // The number of pages to show.
        private val NUM_PAGES = 2

        fun createIntent(context: Context): Intent = Intent(context, PollListActivity::class.java)

        fun createIntent(context: Context, passedURI: Uri): Intent = Intent(context, PollListActivity::class.java).apply {
            putExtra(Constants.PASSED_URI, passedURI)
        }
    }
}
