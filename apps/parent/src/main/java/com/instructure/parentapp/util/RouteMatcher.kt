/*
 * Copyright (C) 2019 - present  Instructure, Inc.
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
package com.instructure.parentapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.BaseRouteMatcher
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.DetailViewActivity
import com.instructure.parentapp.activity.SplashActivity
import com.instructure.parentapp.fragments.*

object RouteMatcher : BaseRouteMatcher() {

    private const val COURSE_REGEX = "/(?:courses|groups)"

    private fun getCourseFromRoute(route: String) = COURSE_REGEX + route

    init {
        initRoutes()
    }

    private fun initRoutes() {
        // Course
        routes.add(Route(getCourseFromRoute("/:course_id"), CourseWeekFragment::class.java))

        // Announcements
        routes.add(Route(getCourseFromRoute("/:course_id/announcements/:announcement_id"), AnnouncementFragment::class.java))
        routes.add(Route(getCourseFromRoute("/:course_id/discussion_topics/:announcement_id"), AnnouncementFragment::class.java))

        // Institution Announcements
        routes.add(Route("/accounts/self/users/:student_id/account_notifications/:notification_id", RouteContext.ACCOUNT_NOTIFICATIONS))

        // Calendar
        routes.add(Route(getCourseFromRoute("/:course_id/calendar_events/:event_id"), EventFragment::class.java))

        // Syllabus
        routes.add(Route(getCourseFromRoute("/:course_id/assignments/syllabus"), CourseSyllabusFragment::class.java))

        // Assignments
        routes.add(Route(getCourseFromRoute("/:course_id/assignments/:assignment_id"), AssignmentFragment::class.java))

        // Files
        routes.add(Route(getCourseFromRoute("/:course_id/files/:file_id/download"), RouteContext.FILE)) // trigger webview's download listener
        routes.add(Route("/files/:file_id/download", RouteContext.FILE))
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    fun canRouteInternally(activity: Activity?, url: String?, user: User?, domain: String, routeIfPossible: Boolean): Boolean {
        if (url.isNullOrBlank()) return false

        val canRoute = getInternalRoute(url, domain) != null

        if (canRoute && activity != null && routeIfPossible) {
            routeUrl(activity, url, user, domain)
        }

        return canRoute
    }

    @JvmStatic
    fun routeUrl(context: Context, url: String, user: User?, domain: String) {
        val urlValidity = UrlValidity(url, domain)

        if (!urlValidity.isValid) {
            routeToMainPage(context, false)
        }

        val isHostForLoggedInUser = urlValidity.isHostForLoggedInUser

        val host = urlValidity.uri?.host
        if (isHostForLoggedInUser || host == null) {
            val route = getInternalRoute(url, urlValidity.uri?.host ?: "")

            if (route != null) {
                handleRoute(context, route, user)
            }
        } else {
            openInInternalWebViewFragment(context, url, true)
        }
    }

    private fun openInInternalWebViewFragment(context: Context, url: String, isReceivedFromOutsideOfApp: Boolean) {
        Logger.d("couldNotParseUrl()")
        val bundle = InternalWebViewFragment.createBundle(url, "", null, null)

        val intent = Intent(context, DetailViewActivity::class.java)
        if (isReceivedFromOutsideOfApp) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    private fun routeToMainPage(context: Context, isReceivedFromOutsideOfApp: Boolean, msgResId: Int = -1) {
        val intent = if (msgResId != -1) SplashActivity.createIntent(context, true, msgResId)
        else SplashActivity.createIntent(context, false, msgResId)

        if (isReceivedFromOutsideOfApp) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
    }

    private fun handleRoute(context: Context, route: Route, student: User?) {
        try {
            // Currently all of our routes contain the course
            if (route.paramsHash.containsKey(Param.COURSE_ID)) {
                val courseId = route.paramsHash[Param.COURSE_ID]?.toLong() ?: -1

                if (route.getContextType() == CanvasContext.Type.COURSE) {
                    getCourseForRouting(context, courseId, route, student)
                }
                return  // Do not remove return
            } else {
                if (route.routeContext == RouteContext.FILE) {
                    // Currently not supported
                    routeToMainPage(context, false, R.string.filesNotSupported)
                    return
                } else if (route.routeContext == RouteContext.ACCOUNT_NOTIFICATIONS) {
                    if (student != null) {
                        routeToAccountNotification(context, route, student)
                    } else {
                        // We don't have the student information, take user back to main page
                        routeToMainPage(context, false)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("Could not parse and route url in RouteValidatorActivity")
        }
    }

    private fun routeToFragment(context: Context, canvasContext: CanvasContext?, student: User?, route: Route) {
        if (student == null) {
            // Error here, don't route
            return
        }

        if (canvasContext == null) {
            // Error - no canvas context
            return
        }

        if (route.routeContext == RouteContext.FILE) {
            // Not currently supported - take user to main page
            routeToMainPage(context, true, R.string.filesNotSupported)
            return
        }

        // Check if it's the assignment route
        when {
            route.primaryClass?.equals(AssignmentFragment::class.java) == true -> {
                // The params has should have the assignment id, and we have the course id at this point
                val assignmentId = route.paramsHash["assignment_id"]?.toLong() ?: -1

                tryWeave {
                    val assignment = awaitApi<Assignment> { AssignmentManager.getAssignment(assignmentId, canvasContext.id, true, it) }
                    routeToMainPage(context, true)
                    context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.ASSIGNMENT, assignment, (canvasContext as Course), student))
                } catch {
                    it.printStackTrace()
                    routeToMainPage(context, true, R.string.could_not_route_assignment)
                }
            }

            route.primaryClass?.equals(AnnouncementFragment::class.java) == true -> {
                val announcementId = route.paramsHash["announcement_id"]?.toLong() ?: -1
                tryWeave {
                    val discussionTopicHeader = awaitApi<DiscussionTopicHeader> { DiscussionManager.getDetailedDiscussion(canvasContext, announcementId, it, false) }
                    routeToMainPage(context, false)
                    context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.ANNOUNCEMENT, discussionTopicHeader, canvasContext.name ?: "", student))
                } catch {
                    it.printStackTrace()
                    routeToMainPage(context, true, R.string.could_not_route_announcement)
                }
            }

            route.primaryClass?.equals(EventFragment::class.java) == true -> {
                val eventId = route.paramsHash["event_id"]?.toLong() ?: -1
                tryWeave {
                    val scheduleItem = awaitApi<ScheduleItem> { CalendarEventManager.getCalendarEvent(eventId, it, true) }
                    routeToMainPage(context, false)
                    context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.EVENT, scheduleItem, student))
                } catch {
                    it.printStackTrace()
                    routeToMainPage(context, true, R.string.could_not_route_event)
                }
            }

            route.primaryClass?.equals(CourseWeekFragment::class.java) == true -> {
                routeToMainPage(context, false)
                context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.WEEK, student, (canvasContext as Course?)!!))
            }
            route.primaryClass?.equals(CourseSyllabusFragment::class.java) == true -> { // We have the course and the student, just go to the syllabus fragment
                routeToMainPage(context, false)
                context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.SYLLABUS, student, (canvasContext as Course?)!!))
            }

            else -> {
                routeToMainPage(context, false, R.string.could_not_route_unknown)
            }
        }
    }

    private fun routeToAccountNotification(context: Context, route: Route, student: User) {
        val notificationId = route.paramsHash["notification_id"]?.toLong() ?: -1
        tryWeave {
            val accountNotification = awaitApi<AccountNotification> { AccountNotificationManager.getAccountNotification(notificationId, it, true) }
            routeToMainPage(context, false)
            context.startActivity(DetailViewActivity.createIntent(context, DetailViewActivity.DETAIL_FRAGMENT.ACCOUNT_NOTIFICATION, accountNotification, student))
        } catch {
            it.printStackTrace()
            routeToMainPage(context, true, R.string.could_not_route_account_notifications)
        }
    }

    private fun getCourseForRouting(context: Context, id: Long, route: Route, student: User?) {
        tryWeave {
            val courseContext = awaitApi<Course> { CourseManager.getCourseWithGrade(id, it, true) }
            routeToFragment(context, courseContext, student, route)
        } catch {
            it.printStackTrace()
            // Either we failed to get the course info, or the user doesn't have access to it (status code 401)
            // just route them back to the courses view
            routeToMainPage(context, false, R.string.could_not_find_course)
        }
    }

    private class UrlValidity(url: String, userDomain: String) {
        var isHostForLoggedInUser = false
        var isValid = false
            private set
        val uri: Uri? = Uri.parse(url)

        init {
            if (uri != null) {
                isValid = true
            }

            val host = uri?.host
            isHostForLoggedInUser = isLoggedInUserHost(host, userDomain)
        }

        private fun isLoggedInUserHost(host: String?, userDomain: String?): Boolean {
            // Assumes user is already signed in (InterwebsToApplication in Student does a sign in check)
            return userDomain != null && userDomain == host
        }
    }
}
