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
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.R
import com.instructure.parentapp.fragments.*

class DetailViewActivity : BaseParentActivity() {

    private var addFragmentEnabled = true

    enum class DETAIL_FRAGMENT {
        WEEK, ASSIGNMENT, EVENT, ANNOUNCEMENT, SYLLABUS, ACCOUNT_NOTIFICATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_activity_layout)
        if (savedInstanceState == null) {
            routeFromIntent(intent)
        }
    }

    private fun routeFromIntent(intent: Intent) {
        val fragmentType = intent.extras?.getSerializable(Const.FRAGMENT_TYPE) as DETAIL_FRAGMENT?
        if (fragmentType != null) {
            val student = intent.extras?.getParcelable<User>(Const.USER)

            when (fragmentType) {
                DetailViewActivity.DETAIL_FRAGMENT.WEEK -> {
                    val user = intent.extras?.getParcelable<User>(Const.USER)
                    val course = intent.extras?.getParcelable(Const.COURSE) as Course?
                    if (user != null && course != null) {
                        addFragment(CourseWeekFragment.newInstance(user, course))
                    }
                }
                DetailViewActivity.DETAIL_FRAGMENT.ASSIGNMENT -> {
                    val assignment = intent.extras?.getParcelable<Assignment>(Const.ASSIGNMENT)
                    val course = intent.extras?.getParcelable(Const.COURSE) as Course?
                    addFragment(AssignmentFragment.newInstance(assignment!!, course!!, student!!), false)
                }
                DetailViewActivity.DETAIL_FRAGMENT.ANNOUNCEMENT -> {
                    val announcement = intent.extras?.getParcelable<DiscussionTopicHeader>(Const.ANNOUNCEMENT)
                    val announcementCourseName = intent.extras?.getString(Const.NAME)
                    addFragment(AnnouncementFragment.newInstance(announcement!!, announcementCourseName!!, student!!), false)
                }
                DetailViewActivity.DETAIL_FRAGMENT.EVENT -> {
                    val item = intent.extras?.getParcelable<ScheduleItem>(Const.SCHEDULE_ITEM)
                    addFragment(EventFragment.newInstance(item!!, student!!), false)
                }
                DetailViewActivity.DETAIL_FRAGMENT.SYLLABUS -> {
                    val syllabusCourse = intent.extras?.getParcelable(Const.COURSE) as Course
                    addFragment(CourseSyllabusFragment.newInstance(syllabusCourse, student!!), false)
                }
                DetailViewActivity.DETAIL_FRAGMENT.ACCOUNT_NOTIFICATION -> {
                    val accountNotification = intent.extras?.getParcelable<AccountNotification>(Const.ACCOUNT_NOTIFICATION)
                    addFragment(AccountNotificationFragment.newInstance(accountNotification!!, student!!))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        //A new assignment came in, set everything up again
        routeFromIntent(intent)
    }

    override fun getTopFragment(): Fragment? {
        if (supportFragmentManager.backStackEntryCount > 0) {
            val fragments = supportFragmentManager.fragments
            if (!fragments.isEmpty()) {
                return fragments[supportFragmentManager.backStackEntryCount - 1]
            }
        }
        return null
    }

    override fun onBackPressed() {
        val entryCount = supportFragmentManager.backStackEntryCount

        //If there is more than one fragment on the stack, we want to simply pop that fragment off
        if (entryCount > 1) {
            super.onBackPressed()
        } else if (entryCount == 1) {
            finish()
            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom)
        }//If there is only one fragment on the stack, we want to close the activity containing it
    }

    private fun addFragment(fragment: Fragment?) {
        if (fragment == null) {
            Logger.e("FAILED TO addFragmentToSomething with null fragment...")
            return
        } else if (!addFragmentEnabled) {
            Logger.e("FAILED TO addFragmentToSomething. Too many fragment transactions...")
            return
        }

        setTransactionDelay()

        try {
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
            ft.add(R.id.fullscreen, fragment, fragment.javaClass.name)
            ft.addToBackStack(fragment.javaClass.name)
            ft.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            Logger.e("Could not commit fragment transaction: $e")
        }

    }

    fun addFragment(fragment: ParentFragment?, ignoreDebounce: Boolean) {
        if (fragment == null) {
            Logger.e("FAILED TO addFragmentToSomething with null fragment...")
            return
        } else if (!ignoreDebounce && !addFragmentEnabled) {
            Logger.e("FAILED TO addFragmentToSomething. Too many fragment transactions...")
            return
        }

        setTransactionDelay()

        try {
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fullscreen)
            if(currentFragment is Fragment) ft.hide(currentFragment)
            ft.add(R.id.fullscreen, fragment, fragment.javaClass.name)
            ft.addToBackStack(fragment.javaClass.name)
            ft.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            Logger.e("Could not commit fragment transaction: $e")
        }

    }


    private fun setTransactionDelay() {
        addFragmentEnabled = false
        Handler().postDelayed({ addFragmentEnabled = true }, FRAGMENT_TRANSACTION_DELAY_TIME.toLong())
    }

    companion object {
        private const val FRAGMENT_TRANSACTION_DELAY_TIME = 1000

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT): Intent {
            val intent = Intent(context, DetailViewActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Const.FRAGMENT_TYPE, fragment)

            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, scheduleItem: ScheduleItem, student: User): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.SCHEDULE_ITEM, scheduleItem as Parcelable)
            intent.putExtra(Const.USER, student as Parcelable)
            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, user: User, course: Course): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.USER, user as Parcelable)
            intent.putExtra(Const.COURSE, course as Parcelable)
            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, assignment: Assignment, courseName: String, student: User): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.USER, student as Parcelable)
            intent.putExtra(Const.ASSIGNMENT, assignment as Parcelable)
            intent.putExtra(Const.NAME, courseName)
            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, assignment: Assignment, course: Course, student: User): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.USER, student as Parcelable)
            intent.putExtra(Const.ASSIGNMENT, assignment as Parcelable)
            intent.putExtra(Const.COURSE, course as Parcelable)
            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, announcement: DiscussionTopicHeader, courseName: String, student: User): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.ANNOUNCEMENT, announcement as Parcelable)
            intent.putExtra(Const.NAME, courseName)
            intent.putExtra(Const.USER, student as Parcelable)
            return intent
        }

        fun createIntent(context: Context, fragment: DETAIL_FRAGMENT, accountNotification: AccountNotification, student: User): Intent {
            val intent = createIntent(context, fragment)
            intent.putExtra(Const.ACCOUNT_NOTIFICATION, accountNotification as Parcelable)
            intent.putExtra(Const.USER, student as Parcelable)
            return intent
        }
    }
}
