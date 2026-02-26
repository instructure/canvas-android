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

package com.instructure.student.features.people.details

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.displayType
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_PEOPLE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.applyBottomSystemBarMargin
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.databinding.FragmentPeopleDetailsBinding
import com.instructure.student.features.people.list.PeopleListFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_PEOPLE_DETAILS)
@PageView(url = "{canvasContext}/users/{userId}")
class PeopleDetailsFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(FragmentPeopleDetailsBinding::bind)

    @Suppress("unused")
    @PageViewUrlParam(name = "userId")
    fun getUserIdForPageView() = userId

    @Inject
    lateinit var repository: PeopleDetailsRepository

    //This is necessary because the groups API doesn't currently support retrieving a single user
    //from a group, so we have to pass the user in as an argument.
    private var user by NullableParcelableArg<User>(key = Const.USER)

    private var userId by LongArg(key = Const.USER_ID)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    override fun title(): String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.fragment_people_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userBackground.applyTopSystemBarInsets()
        binding.avatar.applyTopSystemBarInsets()
        binding.compose.backgroundTintList = ColorStateList.valueOf(ThemePrefs.buttonColor)
        binding.compose.setImageDrawable(ColorKeeper.getColoredDrawable(requireContext(), R.drawable.ic_send, ThemePrefs.buttonTextColor))
        binding.compose.applyBottomSystemBarMargin()
        binding.compose.setOnClickListener {
            // Messaging other users is not available in Student view
            val route = if (ApiPrefs.isStudentView) NothingToSeeHereFragment.makeRoute() else {
                val options = InboxComposeOptions.buildNewMessage().copy(
                    defaultValues = InboxComposeOptionsDefaultValues(
                        contextName = canvasContext.name,
                        contextCode = canvasContext.contextId,
                        recipients = listOf(Recipient.from(user!!))
                    ),
                    disabledFields = InboxComposeOptionsDisabledFields(
                        isContextDisabled = true,
                    )
                )
                InboxComposeFragment.makeRoute(options)
            }

            RouteMatcher.route(requireActivity(), route)
        }
        when {
            canvasContext.isCourse && user == null -> fetchUser()
            user == null -> {
                //They must have used a deep link, and there's no way to retrieve user data through a
                //deep link until the groups API gets updated. This redirects the user to the people list.
                val route = PeopleListFragment.makeRoute(canvasContext)
                RouteMatcher.route(requireActivity(), route)
            }
            else -> setupUserViews()
        }
    }

    private fun fetchUser() {
        lifecycleScope.tryLaunch {
            user = repository.loadUser(canvasContext, userId, true)
            setupUserViews()
        } catch {
            toast(R.string.errorOccurred)
            activity?.onBackPressed()
        }
    }

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.color)
    }

    private fun setupUserViews() = with(binding) {
        user?.let { u ->
            ProfileUtils.loadAvatarForUser(avatar, u.name, u.avatarUrl)
            userName.text = Pronouns.span(u.name, u.pronouns)
            userRole.text = u.enrollments.distinctBy { it.displayType }.joinToString { it.displayType }
            userBackground.setBackgroundColor(canvasContext.color)
            bioText.setVisible(u.bio.isValid() && u.bio != null).text = u.bio
            checkMessagePermission()
        }
    }

    private fun checkMessagePermission() {
        lifecycleScope.tryLaunch {
            val canMessageUser = repository.loadMessagePermission(canvasContext, user, true)
            binding.compose.setVisible(canMessageUser)
        } catch {
            binding.compose.setVisible(false)
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext is Course, canvasContext).withParam(RouterParams.USER_ID, userId.toString())

    companion object {

        fun makeRoute(userId: Long, canvasContext: CanvasContext): Route {
            val bundle = Bundle().apply { putLong(Const.USER_ID, userId) }
            return Route(null, PeopleDetailsFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(user: User, canvasContext: CanvasContext) : Route {
            val bundle = Bundle().apply { putParcelable(Const.USER, user) }
            return Route(null, PeopleDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            val hasUserId = route.paramsHash.containsKey(RouterParams.USER_ID)
                    || route.arguments.containsKey(Const.USER_ID) || route.arguments.containsKey(Const.USER)
            return route.canvasContext != null && hasUserId
        }

        fun newInstance(route: Route): PeopleDetailsFragment? {
            if (!validateRoute(route)) return null
            route.paramsHash[RouterParams.USER_ID]?.let { route.arguments.putLong(Const.USER_ID, it.toLong()) }
            return PeopleDetailsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
