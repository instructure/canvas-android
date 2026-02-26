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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.isCourseOrGroup
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ActivityFullscreenBinding
import com.instructure.teacher.router.RouteResolver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job

@AndroidEntryPoint
open class FullscreenActivity : BaseAppCompatActivity(), FullScreenInteractions {

    private val binding by viewBinding(ActivityFullscreenBinding::inflate)

    private var mRoute: Route? = null
    private var groupApiCall: Job? = null
    private var courseApiCall: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupWindowInsets()

        if (savedInstanceState == null) {
            mRoute = intent.extras!!.getParcelable(Route.ROUTE)
            mRoute?.let { handleRoute(it) }

            if (mRoute == null) {
                finish()
            }
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                0
            )
            // Don't consume the insets - let them propagate to child fragments
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupApiCall?.cancel()
        courseApiCall?.cancel()
    }

    override fun handleRoute(route: Route) {
        mRoute = route

        if (mRoute?.canvasContext != null && mRoute?.canvasContext?.isCourseOrGroup == true) {
            setupWithCanvasContext(mRoute?.canvasContext)
        } else {
            val contextId = Route.extractCourseId(mRoute)
            if (contextId == 0L) {
                // No CanvasContext, No URL
                setupWithCanvasContext(null)
            } else {
                when (route.getContextType()) {
                    CanvasContext.Type.COURSE -> {
                        courseApiCall = tryWeave {
                            val course = awaitApi<Course> { CourseManager.getCourse(contextId, it, false) }
                            setupWithCanvasContext(course)
                        } catch {}
                    }
                    CanvasContext.Type.GROUP -> {
                        groupApiCall = tryWeave {
                            val group = awaitApi<Group> { GroupManager.getDetailedGroup(contextId, it, false) }
                            setupWithCanvasContext(group)
                        } catch {}
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupWithCanvasContext(context: CanvasContext?) {
        addFragment(RouteResolver.getFullscreenFragment(context, mRoute!!))
    }

    private fun addFragment(fragment: Fragment?) {
        if(fragment == null) throw IllegalStateException("FullscreenActivity.class addFragment was null")
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val currentFragment = fm.findFragmentById(binding.container.id)
        if(currentFragment != null) {
            // Add to back stack if a fragment exists
            ft.addToBackStack(fragment.javaClass.simpleName)
        }
        ft.replace(binding.container.id, fragment, fragment.javaClass.simpleName)
        ft.commit() // This may need to become a commitAllowingStateLoss(), as its the result of an async call
    }

    /* Used for empty screens-- this sets a placeholder for the bottom bar as invisible. That way, the empty
    screen images will be the correct size, and the bottom bar will be easier to implement later*/

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.container) is NavigationCallbacks) {
            if ((supportFragmentManager.findFragmentById(R.id.container) as NavigationCallbacks).onHandleBackPressed()) return
        }
        super.onBackPressed()
    }

    companion object {
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, FullscreenActivity::class.java)
            intent.putExtra(Route.ROUTE, route as Parcelable)
            return intent
        }
    }
}
