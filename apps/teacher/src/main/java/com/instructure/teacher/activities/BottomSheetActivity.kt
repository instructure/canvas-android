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
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.percentlayout.widget.PercentRelativeLayout
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.interactions.BottomSheetInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.events.AssignmentDescriptionEvent
import com.instructure.teacher.fragments.AddMessageFragment
import com.instructure.teacher.router.RouteResolver
import com.instructure.teacher.utils.getColorCompat
import kotlinx.android.synthetic.main.activity_bottom_sheet.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import org.greenrobot.eventbus.EventBus
import retrofit2.Response

class BottomSheetActivity : BaseAppCompatActivity(), BottomSheetInteractions {

    private var mRoute: Route? = null
    private var mWindowHeight = 0
    private var mKeyboardEventListener: Unregistrar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_sheet)


        if(savedInstanceState != null) {
            bottomSheetRootView.post {
                bottom.visibility = View.VISIBLE
                bottomSheetRootView.setBackgroundColor(getColorCompat(R.color.semiTransparentDark))
            }
        } else {

            mRoute = intent.extras!!.getParcelable(Route.ROUTE)

            if (mRoute == null) {
                finish()
                return
            }

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
            animateBottomSheetIntoView()
        }

        bottomSheetRootView.setOnClickListener {
            animateBottomSheetGoneAndFinish()
        }

        bottomSheetRootView.viewTreeObserver.addOnGlobalLayoutListener {
            bottomSheetRootView.viewTreeObserver.removeOnGlobalLayoutListener { }
            mWindowHeight = bottomSheetRootView.height
        }

        mKeyboardEventListener = KeyboardVisibilityEvent.registerEventListener(this, { isOpen ->
            if (isOpen) { keyboardVisible() } else keyboardHidden()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mKeyboardEventListener?.unregister()
    }

    private fun setupWithCanvasContext(course: Course?) {
        addFragment(RouteResolver.getBottomSheetFragment(course, mRoute!!))
    }

    override fun addFragment(route: Route) {
        addFragment(RouteResolver.getBottomSheetFragment(route.canvasContext, route))
    }

    private fun addFragment(fragment: Fragment?) {
        if(fragment == null) throw IllegalStateException("BottomSheetActivity.class addFragment was null")
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val currentFragment = fm.findFragmentById(bottom.id)
        if(currentFragment != null) {
            //Add to back stack if a fragment exists
            ft.addToBackStack(fragment.javaClass.simpleName)
        }
        ft.replace(R.id.bottom, fragment, fragment.javaClass.simpleName)
        ft.commit()
    }

    private fun animateBottomSheetIntoView() {
        bottom.post {
            fadeInBackground()
            animateBottomIn()
        }
    }

    private fun animateBottomSheetGoneAndFinish() {
        //don't want to finish if there are unsaved changes
        if (supportFragmentManager.findFragmentById(R.id.bottom) is AddMessageFragment) {
            if(!(supportFragmentManager.findFragmentById(R.id.bottom) as AddMessageFragment).shouldAllowExit()) {
                (supportFragmentManager.findFragmentById(R.id.bottom) as AddMessageFragment).handleExit()
                return
            }
        }
        bottom.post {
            animateBottomOut()
            fadeOutBackground()
        }
    }

    private fun fadeOutBackground() {
        ValueAnimator.ofObject(ArgbEvaluator(), getColorCompat(R.color.semiTransparentDark), Color.TRANSPARENT).apply {
            duration = 380
            addUpdateListener { animator -> bottomSheetRootView.setBackgroundColor(animator.animatedValue as Int) }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    finish()
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }.start()
    }

    private fun animateBottomOut() {
        ObjectAnimator.ofFloat<View>(bottom, View.TRANSLATION_Y, 0F, bottom.height.toFloat()).apply {
            duration = 280
            interpolator = AccelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    bottom.setGone()
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }.start()
    }

    private fun fadeInBackground() {
        ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, getColorCompat(R.color.semiTransparentDark)).apply {
            duration = 280
            addUpdateListener { animator -> bottomSheetRootView.setBackgroundColor(animator.animatedValue as Int) }
        }.start()
    }

    private fun animateBottomIn() {
        ObjectAnimator.ofFloat<View>(bottom, View.TRANSLATION_Y, bottom.height.toFloat(), 0F).apply {
            duration = 380
            interpolator = DecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    bottom.setVisible()
                }
                override fun onAnimationEnd(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ANIMATION_COMPLETE, bottom.visibility == View.VISIBLE)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val ANIMATION_COMPLETE = "ANIMATION_COMPLETE"
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, BottomSheetActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra(Route.ROUTE, route as Parcelable)
            return intent
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        if(supportFragmentManager.findFragmentById(R.id.bottom) == null ||
                supportFragmentManager.backStackEntryCount == 0) {
            animateBottomSheetGoneAndFinish()
        } else if(supportFragmentManager.findFragmentById(R.id.bottom) is NavigationCallbacks) {
             if((supportFragmentManager.findFragmentById(R.id.bottom) as NavigationCallbacks).onHandleBackPressed())
                 return
             else super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    private fun keyboardHidden() {
        val params = bottom.layoutParams as PercentRelativeLayout.LayoutParams
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
        val heightPercent = resources.getFraction(R.dimen.tabletHeightPercent, 1, 1)
        val widthPercent = resources.getFraction(R.dimen.tabletWidthPercent, 1, 1)
        params.percentLayoutInfo.heightPercent = heightPercent
        params.percentLayoutInfo.widthPercent = widthPercent
        params.setMargins(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.bottomSheetCardBottomPadding))
        bottom.layoutParams = params
    }

    private fun keyboardVisible() {
        val params = bottom.layoutParams as PercentRelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.setMargins(0, 0, 0, 0)
        bottom.layoutParams = params
    }
}
