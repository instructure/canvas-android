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
package com.instructure.teacher.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.percentlayout.widget.PercentLayoutHelper
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewTreeObserver
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.fragments.CourseBrowserEmptyFragment
import com.instructure.teacher.fragments.CourseBrowserFragment
import com.instructure.teacher.fragments.EmptyFragment
import com.instructure.interactions.Identity
import com.instructure.interactions.router.Route
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.router.RouteResolver
import kotlinx.android.synthetic.main.activity_master_detail.*
import retrofit2.Response

class MasterDetailActivity : BaseAppCompatActivity(), MasterDetailInteractions {

    private var mRoute: Route? = null

    private val collapseAnimation: ValueAnimator by lazy {
        ValueAnimator.ofFloat(collapsePercent).setDuration(500)
    }

    private val expandAnimation: ValueAnimator by lazy {
        ValueAnimator.ofFloat(expandPercent).setDuration(500)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_detail)

        mRoute = intent.extras!!.getParcelable<Route>(Route.ROUTE)

        if (mRoute == null) {
            finish()
        }

        if (savedInstanceState != null) {
            //Handles rotation and resizing of master/detail views
            val isMasterVisible = savedInstanceState.getBoolean(MASTER_VISIBLE, true)
            rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (!isMasterVisible) {
                        val info = (detail.layoutParams as PercentLayoutHelper.PercentLayoutParams).percentLayoutInfo
                        info.widthPercent = 1F
                        detail.requestLayout()
                    }
                    master.setVisible(isMasterVisible)
                }
            })
        } else {
            if (mRoute?.canvasContext != null && mRoute?.canvasContext is Course) {
                setupWithCanvasContext(mRoute?.canvasContext as Course)
            } else {
                val contextId = Route.extractCourseId(mRoute)
                if (contextId == 0L) {
                    //No CanvasContext, No URL
                    setupWithCanvasContext(null)
                } else {
                    CourseManager.getCourse(contextId, object : StatusCallback<Course>() {
                        override fun onResponse(response: Response<Course>, linkHeaders: LinkHeaders, type: ApiType) {
                            setupWithCanvasContext(response.body() as Course)
                        }
                    }, false)
                }
            }
        }

        if(mRoute?.canvasContext is Course) {
            val course = mRoute?.canvasContext as Course
            middleTopDivider.setBackgroundColor(course.color)
            fakeToolbarMaster.setBackgroundColor(course.color)
            fakeToolbarDetail.setBackgroundColor(course.color)
        } else {
            middleTopDivider.setBackgroundColor(ThemePrefs.primaryColor)
            fakeToolbarMaster.setBackgroundColor(ThemePrefs.primaryColor)
            fakeToolbarDetail.setBackgroundColor(ThemePrefs.primaryColor)
        }
    }

    private fun setupWithCanvasContext(course: Course?) {
        if(course != null) {
            //we have a route, get the fragment
            val masterFragment = RouteResolver.getMasterFragment(course, mRoute!!)
            val detailFragment = RouteResolver.getDetailFragment(course, mRoute!!)
            addMasterFragment(masterFragment)
            if (detailFragment != null) {
                //If we have a detail fragment add it, otherwise we need data from the master when it's finished loading
                addDetailFragment(detailFragment)
            } else {
                if (masterFragment is CourseBrowserFragment) {
                    addDetailFragment(CourseBrowserEmptyFragment.newInstance(course))
                } else {
                    addDetailFragment(EmptyFragment.newInstance(course, RouteMatcher.getClassDisplayName(this, mRoute!!.primaryClass)))
                }
            }
        } else {
            //we have a route, get the fragment
            val masterFragment = RouteResolver.getMasterFragment(course, mRoute!!)
            val detailFragment = RouteResolver.getDetailFragment(course, mRoute!!)
            addMasterFragment(masterFragment)
            if (detailFragment != null) {
                //If we have a detail fragment add it, otherwise we need data from the master when it's finished loading
                addDetailFragment(detailFragment)
            } else {
                addDetailFragment(EmptyFragment.newInstance(RouteMatcher.getClassDisplayName(this, mRoute!!.primaryClass)))
            }

            //TODO deal with situations where the CanvasContext is a User - not a Course
        }
    }

    private fun addMasterFragment(fragment: Fragment?) {
        if(fragment == null) throw IllegalStateException("MasterDetailActivity.class addMasterFragment was null")
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(master.id, fragment, fragment.javaClass.simpleName)
        ft.commit()
    }

    private fun addDetailFragment(fragment: Fragment?) {
        if(fragment == null) throw IllegalStateException("MasterDetailActivity.class addDetailFragment was null")

        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val currentFragment = fm.findFragmentById(detail.id)

        if(identityMatch(currentFragment, fragment)) return

        ft.replace(detail.id, fragment, fragment.javaClass.simpleName)
        if(currentFragment != null && !(currentFragment is EmptyFragment)) {
            //Add to back stack if not empty fragment and a fragment exists
            ft.addToBackStack(fragment.javaClass.simpleName)
        }
        ft.commit()
    }

    override fun addFragment(route: Route) {
        addDetailFragment(RouteResolver.getDetailFragment(route.canvasContext, route))
    }

    override fun popFragment(canvasContext: CanvasContext) {
        val fm = supportFragmentManager
        val currentFragment = fm.findFragmentById(detail.id)
        if(currentFragment != null) {
            val ft = fm.beginTransaction()
            ft.remove(currentFragment)
            ft.commit()
        }
        fm.popBackStack()
    }

    private fun identityMatch(fragment1: Fragment?, fragment2: Fragment?): Boolean{
        if(fragment2 is Identity && fragment1 is Identity) {
            if(fragment1.skipCheck || fragment2.skipCheck) return false

            if(fragment1.identity != null && fragment2.identity != null) {
                //Check if fragment identities are the same
                if(fragment1.identity == fragment2.identity) return true
            }
        }
        return false
    }

    //region Resize Helpers

    override fun toggleExpandCollapse() {
        toggleResize()
    }

    override val isMasterVisible: Boolean
        get() = master.visibility == View.VISIBLE

    private fun toggleResize() {
        //prevent animation from executing if already running
        if(expandAnimation.isRunning || collapseAnimation.isRunning) return

        if (master.visibility == View.VISIBLE) {
            expand()
        } else {
            collapse()
        }
    }

    private fun calculateAnimatedValue(initialValue: Float, targetValue: Float, animationFraction: Float): Float {
        return targetValue - (targetValue - initialValue) * (1.0F - animationFraction)
    }

    private fun expand() {
        detail.bringToFront()

        expandAnimation.addUpdateListener ({ animation ->
            val info = (detail.layoutParams as PercentLayoutHelper.PercentLayoutParams).percentLayoutInfo
            info.widthPercent = calculateAnimatedValue(info.widthPercent, expandPercent, animation.animatedFraction)
            detail.requestLayout()
        })
        expandAnimation.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                master.setGone()

            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })

        expandAnimation.start()
    }

    private fun collapse() {
        detail.bringToFront()

        collapseAnimation.setTarget(detail)
        collapseAnimation.addUpdateListener ({ animation ->
            val info = (detail.layoutParams as PercentLayoutHelper.PercentLayoutParams).percentLayoutInfo
            info.widthPercent = calculateAnimatedValue(info.widthPercent, collapsePercent, animation.animatedFraction)
            detail.requestLayout()
        })
        collapseAnimation.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                middleDividerWrapper.bringToFront()
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {
                master.setVisible()
            }
        })

        collapseAnimation.start()
    }

    //endregion

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(MASTER_VISIBLE, master.visibility == View.VISIBLE)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val MASTER_VISIBLE = "MASTER_VISIBLE"
        private val collapsePercent = 0.65F
        private val expandPercent = 1.0F

        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, MasterDetailActivity::class.java)
            intent.putExtra(Route.ROUTE, route as Parcelable)
            return intent
        }
    }
}
