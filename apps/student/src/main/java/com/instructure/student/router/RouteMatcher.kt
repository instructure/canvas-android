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
package com.instructure.student.router

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater.from
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.router.BaseRouteMatcher
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouteType
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressFragment
import com.instructure.pandautils.features.shareextension.ShareFileSubmissionTarget
import com.instructure.pandautils.features.todolist.ToDoListFragment
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LoaderUtils
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.CanvasLoadingView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.activity.NothingToSeeHereFragment
import com.instructure.student.activity.ViewMediaActivity
import com.instructure.student.features.coursebrowser.CourseBrowserFragment
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment
import com.instructure.student.features.discussion.list.DiscussionListFragment
import com.instructure.student.features.elementary.course.ElementaryCourseFragment
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.features.grades.GradesListFragment
import com.instructure.student.features.modules.list.ModuleListFragment
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.features.pages.list.PageListFragment
import com.instructure.student.features.people.details.PeopleDetailsFragment
import com.instructure.student.features.people.list.PeopleListFragment
import com.instructure.student.features.quiz.list.QuizListFragment
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.CourseSettingsFragment
import com.instructure.student.fragment.OldDashboardFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.OldToDoListFragment
import com.instructure.student.fragment.ProfileSettingsFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.fragment.UnsupportedFeatureFragment
import com.instructure.student.fragment.UnsupportedTabFragment
import com.instructure.student.fragment.ViewHtmlFragment
import com.instructure.student.fragment.ViewImageFragment
import com.instructure.student.fragment.ViewUnsupportedFileFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListRepositoryFragment
import com.instructure.student.mobius.syllabus.ui.SyllabusRepositoryFragment
import com.instructure.student.util.FileUtils
import com.instructure.student.util.onMainThread
import java.util.Locale
import java.util.regex.Pattern

object RouteMatcher : BaseRouteMatcher() {

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? = null // I'll bet this causes a memory leak

    var offlineDb: OfflineDatabase? = null
    var networkStateProvider: NetworkStateProvider? = null
    var enabledTabs: EnabledTabs? = null

    init {
        initRoutes()
        initClassMap()
    }

    // Be sensitive to the order of items. It really, really matters.
    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    private fun initRoutes() {
        routes.add(Route("/", OldDashboardFragment::class.java))
        // region Conversations
        routes.add(Route("/conversations", InboxFragment::class.java))
        routes.add(Route("/conversations/:${InboxDetailsFragment.CONVERSATION_ID}", InboxDetailsFragment::class.java))
        routes.add(Route("/login.*", RouteContext.DO_NOT_ROUTE))//FIXME: we know about this
        // endregion

        //////////////////////////
        // Courses
        //////////////////////////
        routes.add(Route(courseOrGroup("/"), OldDashboardFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}"),
                CourseBrowserFragment::class.java,
                NotificationListFragment::class.java,
                listOf(":${RouterParams.RECENT_ACTIVITY}")
            )
        ) // Recent Activity
        if (ApiPrefs.showElementaryView) {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}"), ElementaryCourseFragment::class.java))
        } else {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}"), CourseBrowserFragment::class.java))
        }

        // region Modules

        /*
        Modules with query params
        !!!!!!!!!!!!
        !  CAUTION: Order matters, these are purposely placed above the pages, quizzes, disscussions, assignments, and files so they are matched if query params exist and routed to Modules
        !!!!!!!!!!!!
        */
        if (ApiPrefs.showElementaryView) {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules"), ElementaryCourseFragment::class.java, tabId = Tab.MODULES_ID))
        } else {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules"), ModuleListFragment::class.java))
        }
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/modules/items/:${RouterParams.MODULE_ITEM_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules/:${RouterParams.MODULE_ID}"), ModuleListFragment::class.java))

        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}"),
                ModuleListFragment::class.java,
                CourseModuleProgressionFragment::class.java,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        ) // TODO TEST
        // endregion

        // Notifications
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/notifications"), NotificationListFragment::class.java))

        // Grades
        if (ApiPrefs.showElementaryView) {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/grades"), ElementaryCourseFragment::class.java, tabId = Tab.GRADES_ID))
        } else {
            routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/grades"), GradesListFragment::class.java))
        }
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/grades/:${RouterParams.ASSIGNMENT_ID}"),
                GradesListFragment::class.java,
                AssignmentDetailsFragment::class.java
            )
        )

        // People
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/users"), PeopleListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/users/:${RouterParams.USER_ID}"),
                PeopleListFragment::class.java,
                PeopleDetailsFragment::class.java
            )
        )

        // Files
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files"), FileListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/folder/:${RouterParams.FOLDER_NAME}"), RouteContext.FILE))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}/download"),
                RouteContext.FILE
            )
        ) // trigger webview's download listener
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}/preview"), RouteContext.FILE))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}"),
                RouteContext.FILE,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/folder(\\/.*)*/:${RouterParams.FILE_ID}"), RouteContext.FILE))
        routes.add(Route("/files/folder(\\/.*)*/:${RouterParams.FILE_ID}", RouteContext.FILE))

        // Discussions
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics"), DiscussionListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"),
                DiscussionListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"),
                DiscussionListFragment::class.java,
                DiscussionRouterFragment::class.java
            )
        ) // Route for bookmarking

        // Pages
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/pages"), PageListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"),
                PageListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"),
                PageListFragment::class.java,
                PageDetailsFragment::class.java
            )
        )
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/wiki"), PageListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/wiki/:${RouterParams.PAGE_ID}"),
                PageListFragment::class.java,
                PageDetailsFragment::class.java
            )
        )

        // Announcements
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/announcements"), AnnouncementListFragment::class.java))
        // :message_id because it shares with discussions
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/announcements/:${RouterParams.MESSAGE_ID}"),
                AnnouncementListFragment::class.java,
                DiscussionRouterFragment::class.java
            )
        )
        // Announcements from the notifications tab
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/announcements/:${RouterParams.MESSAGE_ID}"),
                NotificationListFragment::class.java,
                DiscussionRouterFragment::class.java
            )
        )

        // Quiz
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes"), QuizListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"),
                QuizListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"),
                QuizListFragment::class.java,
                BasicQuizViewFragment::class.java
            )
        )

        // Calendar
        routes.add(Route("/calendar/:${CalendarFragment.SELECTED_DAY}", CalendarFragment::class.java))
        routes.add(Route("/calendar", CalendarFragment::class.java))
        routes.add(Route("/:${EventFragment.CONTEXT_TYPE}/:${EventFragment.CONTEXT_ID}/calendar_events/:${EventFragment.SCHEDULE_ITEM_ID}", EventFragment::class.java))

        // To Do
        routes.add(Route("/todos/new", CreateUpdateToDoFragment::class.java))
        routes.add(Route("/todos/:${ToDoFragment.PLANNABLE_ID}", ToDoFragment::class.java))

        // To Do List
        val todoListFragmentClass = if (RemoteConfigUtils.getBoolean(RemoteConfigParam.TODO_REDESIGN)) {
            ToDoListFragment::class.java
        } else {
            OldToDoListFragment::class.java
        }
        routes.add(Route("/todolist", todoListFragmentClass).copy(canvasContext = ApiPrefs.user))

        // Syllabus
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/syllabus"), SyllabusRepositoryFragment::class.java))

        // Assignments
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments"), AssignmentListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                AssignmentListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                NotificationListFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                CalendarFragment::class.java,
                CourseModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                AssignmentListFragment::class.java,
                AssignmentDetailsFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                NotificationListFragment::class.java,
                AssignmentDetailsFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                CalendarFragment::class.java,
                AssignmentDetailsFragment::class.java
            )
        )

        // Studio
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/external_tools/:${RouterParams.EXTERNAL_ID}/resource_selection"),
                StudioWebViewFragment::class.java
            )
        )

        // Studio Media Immersive View
        routes.add(
            Route(
                "/media_attachments/:${RouterParams.ATTACHMENT_ID}/immersive_view",
                InternalWebviewFragment::class.java
            )
        )

        // Submissions
        // :sliding_tab_type can be /rubric or /submissions (used to navigate to the nested fragment)
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}/:${RouterParams.SLIDING_TAB_TYPE}"),
                AssignmentDetailsFragment::class.java,
                SubmissionDetailsFragment::class.java
            )
        )
        // Route to Assignment Details first - no submission/on paper assignments won't have grades on the Submission Details page, but we also need to account for routing to submission comments (Assignment Details will check for that)
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}/:${RouterParams.SLIDING_TAB_TYPE}/:${RouterParams.SUBMISSION_ID}"),
                AssignmentDetailsFragment::class.java
            )
        )

        // Settings
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/settings"), CourseSettingsFragment::class.java))

        // Profile
        routes.add(Route("/profile/settings", ProfileSettingsFragment::class.java))

        // Unsupported
        // NOTE: An Exception to how the router usually works (Not recommended for urls that are meant to be internally routed)
        //  The .* will catch anything and route to UnsupportedFragment. If the users decides to press "open in browser" from the UnsupportedFragment, then InternalWebviewFragment is setup to handle the unsupportedFeature
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/lti_collaborations.*"),
                UnsupportedTabFragment::class.java,
                Tab.COLLABORATIONS_ID
            )
        )
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/collaborations.*"), UnsupportedTabFragment::class.java, Tab.COLLABORATIONS_ID))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/outcomes.*"), UnsupportedTabFragment::class.java, Tab.OUTCOMES_ID))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/conferences.*"),
                ConferenceListRepositoryFragment::class.java,
                Tab.CONFERENCES_ID
            )
        )

        routes.add(Route("/files", FileListFragment::class.java).apply {
            canvasContext = ApiPrefs.user
        }) // validRoute for FileListFragment checks for a canvasContext, which is null on deep links
        routes.add(Route("/files/folder/:${RouterParams.FOLDER_NAME}", RouteContext.FILE))
        routes.add(Route("/files/:${RouterParams.FILE_ID}/download", RouteContext.FILE)) // trigger webview's download listener
        routes.add(Route("/files/:${RouterParams.FILE_ID}", RouteContext.FILE)) // Triggered by new RCE content file links

        //Notification Preferences
        routes.add(Route("/profile/communication", RouteContext.NOTIFICATION_PREFERENCES))

        //LTI
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/external_tools/:${RouterParams.EXTERNAL_ID}"), RouteContext.LTI))

        //Single Detail Pages (Typically routing from To-dos (may not be handling every use case)
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                AssignmentDetailsFragment::class.java,
                null
            )
        )

        routes.add(Route("/enroll/.*", RouteContext.DO_NOT_ROUTE))

        routes.add(Route("/syncProgress", SyncProgressFragment::class.java))

        // Catch all (when nothing has matched, these take over)
        // Note: Catch all only happens with supported domains such as instructure.com
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/.*"), UnsupportedFeatureFragment::class.java))
        // course_id fetches the course context
        routes.add(Route(".*", UnsupportedFeatureFragment::class.java))
    }

    private fun initClassMap() {
        /* Fullscreen Fragments */
//        fullscreenFragments.add(DashboardFragment::class.java)

        /* Bottom Sheet Fragments */
//        bottomSheetFragments.add(EditFavoritesFragment::class.java)
    }

    fun routeUrl(activity: FragmentActivity, url: String, extras: Bundle? = null) {
        routeUrl(activity, url, ApiPrefs.domain, extras)
    }

    fun routeUrl(activity: FragmentActivity, url: String, domain: String, extras: Bundle? = null, secondaryClass: Class<out Fragment>? = null) {
        /* Possible activity types we can navigate too: Unknown Link, InitActivity, Master/Detail, Fullscreen, WebView, ViewMedia */

        //Find the best route
        //Pass that along to the activity
        //One or two classes? (F, or M/D)

        val route = getInternalRoute(url, domain)

        // Prevent routing to unsupported features while in student view
        if (ApiPrefs.isStudentView
            && (route?.primaryClass == InboxFragment::class.java
                    || route?.tabId == Tab.CONFERENCES_ID
                    || route?.tabId == Tab.COLLABORATIONS_ID)
        ) {
            route.primaryClass = NothingToSeeHereFragment::class.java
        }

        extras?.let { route?.arguments?.putAll(it) }

        // The Group API will not load an individual user's details, so we route to the List fragment by default
        // FIXME: Remove if the group context works with grabbing a user
        if (route?.getContextType() == CanvasContext.Type.GROUP && route.primaryClass == PeopleListFragment::class.java && route.secondaryClass == PeopleDetailsFragment::class.java) {
            route.primaryClass = null
            route.secondaryClass = PeopleListFragment::class.java
        }

        if (secondaryClass != null) {
            route?.secondaryClass = secondaryClass
        }

        route(activity, route)
    }

    fun route(activity: FragmentActivity, route: Route?) {
        if (enabledTabs?.isPathTabNotEnabled(route).orDefault()) {
            if (activity is InterwebsToApplication) {
                val intent = Intent(activity, NavigationActivity.startActivityClass)
                activity.startActivity(intent)
            }
            activity.toast(R.string.route_not_available)
            return
        }

        // Check for Studio embed immersive view BEFORE other routing logic
        // This prevents it from being caught by the LTI route matcher
        Logger.e("RouteMatcher - Checking route: ${route?.uri?.toString()}")
        if (route?.uri?.toString()?.contains("external_tools/retrieve") == true &&
            route.uri?.toString()?.contains("custom_arc_launch_type") == true &&
            route.uri?.toString()?.contains("immersive_view") == true) {
            Logger.e("RouteMatcher - Detected Studio embed immersive view URL in route()")
            // Handle Studio embed immersive view - pass the full URL and title to InternalWebviewFragment
            val uri = route.uri!!
            val urlString = uri.toString()

            route.primaryClass = InternalWebviewFragment::class.java
            route.routeContext = RouteContext.INTERNAL
            route.arguments.putString(Const.INTERNAL_URL, urlString)

            // Extract title from URL query parameter if present, otherwise use fallback
            val title = uri.getQueryParameter("title") ?: activity.getString(R.string.immersiveView)
            route.arguments.putString(Const.ACTION_BAR_TITLE, title)

            Logger.e("RouteMatcher - Routing to InternalWebviewFragment with URL: $urlString")

            if (activity.resources.getBoolean(R.bool.isDeviceTablet)) {
                handleTabletRoute(activity, route)
            } else {
                handleFullscreenRoute(activity, route)
            }
            return
        }

        if (route == null || route.routeContext == RouteContext.DO_NOT_ROUTE) {
            if (route?.uri != null) {
                // No route, no problem
                handleWebViewUrl(activity, route.uri.toString())
            }
        } else if (route.primaryClass == InternalWebviewFragment::class.java && route.uri?.toString()?.contains("media_attachments") == true) {
            // Handle studio media immersive view - pass the full URL and title to InternalWebviewFragment
            val uri = route.uri!!
            var urlString = uri.toString()

            // Convert media_attachments_iframe to media_attachments (for iframe button)
            urlString = urlString.replace("media_attachments_iframe", "media_attachments")

            // Ensure embedded=true parameter is always present
            if (!urlString.contains("embedded=true")) {
                val separator = if (urlString.contains("?")) "&" else "?"
                urlString = "$urlString${separator}embedded=true"
            }

            route.arguments.putString(Const.INTERNAL_URL, urlString)

            // Extract title from URL query parameter if present, otherwise use fallback
            val title = uri.getQueryParameter("title") ?: activity.getString(R.string.immersiveView)
            route.arguments.putString(Const.ACTION_BAR_TITLE, title)

            if (activity.resources.getBoolean(R.bool.isDeviceTablet)) {
                handleTabletRoute(activity, route)
            } else {
                handleFullscreenRoute(activity, route)
            }
        } else if (route.routeContext == RouteContext.FILE
            || route.primaryClass?.isAssignableFrom(FileListFragment::class.java) == true
            && route.queryParamsHash.containsKey(RouterParams.PREVIEW)
        ) {
            when {
                route.secondaryClass == CourseModuleProgressionFragment::class.java -> handleFullscreenRoute(activity, route)
                route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD) -> {
                    if (route.removePreviousScreen) {
                        val fragmentManager = activity.supportFragmentManager
                        fragmentManager.popBackStackImmediate()
                    }
                    if (route.uri != null) {
                        openMedia(activity, route.uri.toString())
                    }
                }
                route.paramsHash.containsKey(RouterParams.FOLDER_NAME) && !route.queryParamsHash.containsKey(RouterParams.PREVIEW) -> {
                    // Preview query params are caught under the same route matcher with the :folder_name param, make sure we're not catching preview urls here as well
                    // Route to the FileListFragment but to the folder - To route we need to modify the route a bit.
                    if (!route.paramsHash.containsKey(RouterParams.COURSE_ID)) {
                        route.canvasContext = ApiPrefs.user
                    }
                    route.routeContext = RouteContext.UNKNOWN
                    route.primaryClass = FileListFragment::class.java
                    handleFullscreenRoute(activity, route)
                }
                else -> {
                    if (route.removePreviousScreen) {
                        val fragmentManager = activity.supportFragmentManager
                        fragmentManager.popBackStackImmediate()
                    }
                    val isGroupRoute = "groups" == route.uri?.pathSegments?.get(0)
                    handleSpecificFile(
                        activity,
                        (if (route.queryParamsHash.containsKey(RouterParams.PREVIEW)) route.queryParamsHash[RouterParams.PREVIEW] else route.paramsHash[RouterParams.FILE_ID])
                            ?: "",
                        route,
                        isGroupRoute
                    )
                }
            }
        } else if (route.routeContext == RouteContext.MEDIA) {
            handleMediaRoute(activity, route)
        } else if (route.routeContext == RouteContext.SPEED_GRADER) {
            //handleSpeedGraderRoute(context, route) //Annotations for student maybe?
        } else if (activity.resources.getBoolean(R.bool.isDeviceTablet)) {
            handleTabletRoute(activity, route)
        } else {
            handleFullscreenRoute(activity, route)
            (activity as? InterwebsToApplication)?.finish()
        }
    }

    private fun handleTabletRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleTabletRoute()")
        context.startActivity(NavigationActivity.createIntent(context, route))
    }

    private fun handleFullscreenRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleFullscreenRoute()")
        route.routeType = RouteType.FULLSCREEN

        val intent = NavigationActivity.createIntent(context, route)
        context.startActivity(intent)
    }

    private fun handleMediaRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleMediaRoute()")
        context.startActivity(ViewMediaActivity.createIntent(context, route))
    }

    private fun handleWebViewUrl(context: Context, url: String?) { //TODO: probably want to handle this in a fragment instead?
        context.startActivity(InternalWebViewActivity.createIntent(context, url!!, "", false))
        Logger.i("RouteMatcher:handleWebViewRoute()")
    }

    private fun getLoaderCallbacks(activity: FragmentActivity): LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
        if (openMediaCallbacks == null) {

            openMediaCallbacks = object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                var dialog: AlertDialog? = null

                override fun onCreateLoader(id: Int, args: Bundle?): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                    if (!activity.isFinishing) {
                        val view = from(activity).inflate(R.layout.dialog_loading_view, null)
                        val loadingView = view.findViewById<CanvasLoadingView>(R.id.canvasLoadingView)
                        val studentColor = getColor(activity, R.color.login_studentAppTheme)
                        loadingView?.setOverrideColor(studentColor)

                        dialog = AlertDialog.Builder(activity, R.style.CustomViewAlertDialog)
                            .setView(view)
                            .create()
                        dialog?.show()
                    }
                    return OpenMediaAsyncTaskLoader(activity, args)
                }

                override fun onLoadFinished(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>, loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia) {
                    if (dialog == null || dialog?.isShowing == false) {
                        return // The user doesn't actually want to load the thing
                    }
                    dialog?.dismiss()
                    try {
                        if (loadedMedia.isError) {
                            if (loadedMedia.errorType == OpenMediaAsyncTaskLoader.ErrorType.NO_APPS) {
                                val args = ViewUnsupportedFileFragment.newInstance(
                                    loadedMedia.intent!!.data!!,
                                    (loader as OpenMediaAsyncTaskLoader).filename!!,
                                    loadedMedia.intent!!.type!!,
                                    null,
                                    R.drawable.ic_attachment
                                ).nonNullArgs
                                route(activity, Route(ViewUnsupportedFileFragment::class.java, null, args))
                            } else {
                                Toast.makeText(activity, activity.resources.getString(loadedMedia.errorMessage), Toast.LENGTH_LONG).show()
                            }
                        } else if (loadedMedia.isHtmlFile) {
                            val args = ViewHtmlFragment.newInstance(
                                loadedMedia.bundle!!.getString(Const.INTERNAL_URL)!!,
                                loadedMedia.bundle!!.getString(Const.ACTION_BAR_TITLE)!!
                            ).nonNullArgs
                            route(activity, Route(ViewHtmlFragment::class.java, null, args))
                        } else if (loadedMedia.intent != null) {
                            if (loadedMedia.intent!!.type!!.contains("pdf") && !loadedMedia.isUseOutsideApps) {
                                // Show pdf with PSPDFkit
                                val uri = loadedMedia.intent!!.data
                                val submissionTarget = loadedMedia.bundle?.getParcelable<ShareFileSubmissionTarget>(Const.SUBMISSION_TARGET)
                                FileUtils.showPdfDocument(uri!!, loadedMedia, activity, submissionTarget)
                            } else if (loadedMedia.intent?.type == "video/mp4") {
                                val bundle = BaseViewMediaActivity.makeBundle(
                                    loadedMedia.intent!!.data!!.toString(),
                                    null,
                                    "video/mp4",
                                    loadedMedia.intent!!.dataString,
                                    true
                                )
                                route(activity, Route(bundle, RouteContext.MEDIA))

                            } else if (loadedMedia.intent?.type?.startsWith("image/") == true) {
                                val args = ViewImageFragment.newInstance(
                                    loadedMedia.intent!!.dataString!!,
                                    loadedMedia.intent!!.data!!,
                                    "image/*",
                                    true,
                                    0
                                ).nonNullArgs
                                route(activity, Route(ViewImageFragment::class.java, null, args))
                            } else {
                                activity.startActivity(loadedMedia.intent)
                            }
                        }
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(activity, R.string.noApps, Toast.LENGTH_LONG).show()
                    }

                    openMediaBundle = null
                }

                override fun onLoaderReset(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>) {
                    dialog?.dismiss()
                }
            }
        }
        return openMediaCallbacks!!
    }

    fun openMedia(activity: FragmentActivity?, url: String?) {
        if (activity != null) {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url)
            LoaderUtils.restartLoaderWithBundle(
                LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID
            )
        }
    }

    private fun handleSpecificFile(activity: FragmentActivity, fileID: String?, route: Route, isGroupFile: Boolean) {
        if (fileID == null || offlineDb == null) {
            Toast.makeText(activity, R.string.fileNotFound, Toast.LENGTH_SHORT).show()
            return
        }

        activity.lifecycleScope.tryLaunch {
            val fileFolder = if (networkStateProvider?.isOnline() == true) {
                FileFolderManager.getFileFolderFromUrlAsync("files/$fileID", true).await().dataOrNull
            } else {
                getFileFolderFromURL(fileID.toLong(), offlineDb!!)
            }

            if (fileFolder == null) {
                Toast.makeText(activity, R.string.fileNotFound, Toast.LENGTH_SHORT).show()
                return@tryLaunch
            }

            if (!isGroupFile && (fileFolder.isLocked || fileFolder.isLockedForUser)) {
                val fileName = if (fileFolder.displayName == null) activity.getString(R.string.file) else fileFolder.displayName
                Toast.makeText(activity, String.format(activity.getString(R.string.fileLocked), fileName), Toast.LENGTH_LONG).show()
            } else {
                // This is either a group file (which have no permissions), or a file that is accessible by the user
                if (networkStateProvider?.isOnline() == true) {
                    openMedia(activity, fileFolder.contentType!!, fileFolder.url!!, fileFolder.displayName!!, route, fileID)
                } else {
                    openLocalMedia(activity, fileFolder.contentType, fileFolder.url, fileFolder.displayName, fileID, route.canvasContext!!)
                }
            }
        } catch {
            Toast.makeText(activity, R.string.fileNotFound, Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getFileFolderFromURL(fileId: Long, offlineDatabase: OfflineDatabase): FileFolder? {
        val fileFolderDao = offlineDatabase.fileFolderDao()
        val localFileFolderDao = offlineDatabase.localFileDao()

        val file = fileFolderDao.findById(fileId) ?: return null
        val localFile = localFileFolderDao.findById(fileId) ?: return null
        return file.copy(url = localFile.path).toApiModel()
    }

    private fun openMedia(activity: FragmentActivity?, mime: String, url: String, filename: String, route: Route, fileId: String?) {
        if (activity == null) {
            return
        }

        // If we're trying to open an HTML file, don't download it. It could be referencing other files
        // through a relative URL which we won't be able to access. Instead, just showing the file in
        // a webview will load the file the user is trying to view and will resolve all relative paths
        if (filename.lowercase(Locale.getDefault()).endsWith(".htm") || filename.lowercase(Locale.getDefault()).endsWith(".html")) {
            RouteUtils.retrieveFileUrl(route, fileId) { fileUrl, context, needsAuth ->
                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(context, fileUrl, needsAuth, true))
            }
        } else {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(mime, url, filename, fileId, route.arguments)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                LoaderManager.getInstance(
                    activity
                ), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID
            )
        }
    }

    private fun openLocalMedia(activity: FragmentActivity?, mime: String?, path: String?, filename: String?, fileId: String?, canvasContext: CanvasContext) {
        val owner = activity ?: return
        onMainThread {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createLocalBundle(canvasContext, mime, path, filename, fileId, false)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                LoaderManager.getInstance(owner),
                openMediaBundle,
                getLoaderCallbacks(owner),
                R.id.openMediaLoaderID
            )
        }
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param url
     * @param routeIfPossible
     * @param allowUnsupported If true (default), this function will return true for unsupported urls - i.e. urls that
     *                         match the provided domain but do not match any existing routes. If [routeIfPossible] is
     *                         also true, the user will be routed to UnsupportedFeatureFragment for such urls.
     * @return
     */

    @JvmOverloads
    fun canRouteInternally(
        activity: FragmentActivity?,
        url: String,
        domain: String,
        routeIfPossible: Boolean,
        allowUnsupported: Boolean = true
    ): Boolean {
        val route = getInternalRoute(url, domain)
        val canRoute = route != null && (allowUnsupported || route.primaryClass != UnsupportedFeatureFragment::class.java)
        if (canRoute && routeIfPossible) activity?.let { routeUrl(activity, url) }
        return canRoute
    }

    fun generateUrl(url: String?, queryParams: HashMap<String, String>): String? {
        if (url == null) return null
        return createQueryParamString(url, queryParams)
    }

    fun generateUrl(type: CanvasContext.Type, masterCls: Class<out Fragment>?, replacementParams: HashMap<String, String>): String? {
        return generateUrl(type, masterCls, null, replacementParams, null)
    }

    fun generateUrl(
        type: CanvasContext.Type,
        masterCls: Class<out Fragment>?,
        detailCls: Class<out Fragment>?,
        replacementParams: HashMap<String, String>?,
        queryParams: HashMap<String, String>?
    ): String? {
        val domain = ApiPrefs.fullDomain

        // Workaround for the discussion details because we bookmark a different class that we use for routing
        val detailsClass = if (detailCls == DiscussionDetailsFragment::class.java) DiscussionRouterFragment::class.java else detailCls

        val urlRoute = getInternalRoute(masterCls, detailsClass)
        if (urlRoute != null) {
            var path = urlRoute.createUrl(replacementParams)
            if (path.contains(COURSE_OR_GROUP_REGEX)) {
                val pattern = Pattern.compile(COURSE_OR_GROUP_REGEX, Pattern.LITERAL)
                val matcher = pattern.matcher(path)
                when (type) {
                    CanvasContext.Type.COURSE -> path = matcher.replaceAll("/courses")
                    CanvasContext.Type.GROUP -> path = matcher.replaceAll("/groups")
                    else -> {}
                }
            }
            return createQueryParamString(domain + path, queryParams)
        }
        return null
    }

    fun getContextFromUrl(url: String?): CanvasContext? {
        return getContextFromURL(url, routes)
    }

    fun resetRoutes() {
        routes.clear()
        initRoutes()
    }
}
