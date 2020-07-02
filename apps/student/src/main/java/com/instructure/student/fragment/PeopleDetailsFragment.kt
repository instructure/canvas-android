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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.displayType
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_people_details.*
import java.util.ArrayList

@PageView(url = "{canvasContext}/users/{userId}")
class PeopleDetailsFragment : ParentFragment(), Bookmarkable {

    @Suppress("unused")
    @PageViewUrlParam(name = "userId")
    private fun getUserIdForPageView() = userId

    //This is necessary because the groups API doesn't currently support retrieving a single user
    //from a group, so we have to pass the user in as an argument.
    private var user by NullableParcelableArg<User>(key = Const.USER)

    private var userId by LongArg(key = Const.USER_ID)

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var userCall: WeaveJob? = null

    private var permissionCall: WeaveJob? = null

    override fun title(): String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.fragment_people_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val color = ColorKeeper.getOrGenerateColor(canvasContext)
        compose.colorNormal = color
        compose.colorPressed = color

        compose.setIconDrawable(ColorKeeper.getColoredDrawable(requireContext(), R.drawable.vd_send, Color.WHITE))
        compose.setOnClickListener {
            // Messaging other users is not available in Student view
            val route = if (ApiPrefs.isStudentView) NothingToSeeHereFragment.makeRoute() else {
                val participants = ArrayList<BasicUser>()
                participants.add(BasicUser.userToBasicUser(user!!))
                InboxComposeMessageFragment.makeRoute(canvasContext, participants)
            }

            RouteMatcher.route(requireContext(), route)
        }
        when {
            canvasContext.isCourse -> fetchUser()
            user == null -> {
                //They must have used a deep link, and there's no way to retrieve user data through a
                //deep link until the groups API gets updated. This redirects the user to the people list.
                val route = PeopleListFragment.makeRoute(canvasContext)
                RouteMatcher.route(requireContext(), route)
            }
            else -> setupUserViews()
        }
    }

    override fun onDestroy() {
        userCall?.cancel()
        permissionCall?.cancel()
        super.onDestroy()
    }

    private fun fetchUser() {
        userCall = tryWeave {
            user = awaitApi<User> { UserManager.getUserForContextId(canvasContext, userId, it, true) }
            setupUserViews()
        } catch {
            toast(R.string.errorOccurred)
            activity?.onBackPressed()
        }
    }

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ColorKeeper.getOrGenerateColor(canvasContext))
    }

    private fun setupUserViews() {
        user?.let { u ->
            ProfileUtils.loadAvatarForUser(avatar, u)
            userName.text = Pronouns.span(u.name, u.pronouns)
            userRole.text = u.enrollments.distinctBy { it.displayType }.joinToString { it.displayType }
            userBackground.setBackgroundColor(ColorKeeper.getOrGenerateColor(canvasContext))
            bioText.setVisible(u.bio.isValid() && u.bio != null).text = u.bio
            checkMessagePermission()
        }
    }

    private fun checkMessagePermission() {
        permissionCall = tryWeave {
            val id = canvasContext.id
            val canMessageUser = when {
                canvasContext.isGroup -> GroupManager.getPermissionsAsync(id).awaitOrThrow().send_messages
                canvasContext.isCourse -> {
                    val isTeacher = user?.enrollments?.any { it.courseId == id && (it.isTA || it.isTeacher) } ?: false
                    isTeacher || CourseManager.getPermissionsAsync(id).awaitOrThrow().send_messages
                }
                else -> false
            }
            compose.setVisible(canMessageUser)
        } catch {
            compose.setVisible(false)
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext is Course, canvasContext).withParam(RouterParams.USER_ID, userId.toString())

    companion object {

        @JvmStatic
        fun makeRoute(userId: Long, canvasContext: CanvasContext): Route {
            val bundle = Bundle().apply { putLong(Const.USER_ID, userId) }
            return Route(null, PeopleDetailsFragment::class.java, canvasContext, bundle)
        }

        @JvmStatic
        fun makeRoute(user: User, canvasContext: CanvasContext) : Route {
            val bundle = Bundle().apply { putParcelable(Const.USER, user) }
            return Route(null, PeopleDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            val hasUserId = route.paramsHash.containsKey(RouterParams.USER_ID)
                    || route.arguments.containsKey(Const.USER_ID) || route.arguments.containsKey(Const.USER)
            return route.canvasContext != null && hasUserId
        }

        @JvmStatic
        fun newInstance(route: Route): PeopleDetailsFragment? {
            if (!validateRoute(route)) return null
            route.paramsHash[RouterParams.USER_ID]?.let { route.arguments.putLong(Const.USER_ID, it.toLong()) }
            return PeopleDetailsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
