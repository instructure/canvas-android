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
package com.instructure.teacher.router

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.interactions.BottomSheetInteractions
import com.instructure.interactions.InitActivityInteractions
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.BaseRouteMatcher
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoFragment
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.discussion.create.CreateDiscussionWebViewFragment
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.features.notification.preferences.EmailNotificationPreferencesFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.features.settings.SettingsFragment
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureFragment
import com.instructure.pandautils.fragments.HtmlContentFragment
import com.instructure.pandautils.fragments.RemoteConfigParamsFragment
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LoaderUtils
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.R
import com.instructure.teacher.activities.BottomSheetActivity
import com.instructure.teacher.activities.FullscreenActivity
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.activities.MasterDetailActivity
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.activities.ViewMediaActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionListFragment
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.modules.progression.ModuleProgressionFragment
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.features.syllabus.edit.EditSyllabusFragment
import com.instructure.teacher.features.syllabus.ui.SyllabusFragment
import com.instructure.teacher.fragments.AnnouncementListFragment
import com.instructure.teacher.fragments.AssigneeListFragment
import com.instructure.teacher.fragments.AttendanceListFragment
import com.instructure.teacher.fragments.ChooseRecipientsFragment
import com.instructure.teacher.fragments.CourseBrowserEmptyFragment
import com.instructure.teacher.fragments.CourseBrowserFragment
import com.instructure.teacher.fragments.CourseSettingsFragment
import com.instructure.teacher.fragments.CreateOrEditPageDetailsFragment
import com.instructure.teacher.fragments.DashboardFragment
import com.instructure.teacher.fragments.DiscussionsListFragment
import com.instructure.teacher.fragments.DueDatesFragment
import com.instructure.teacher.fragments.EditAssignmentDetailsFragment
import com.instructure.teacher.fragments.EditFileFolderFragment
import com.instructure.teacher.fragments.EditQuizDetailsFragment
import com.instructure.teacher.fragments.FeatureFlagsFragment
import com.instructure.teacher.fragments.FileListFragment
import com.instructure.teacher.fragments.FullscreenInternalWebViewFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.PageListFragment
import com.instructure.teacher.fragments.PeopleListFragment
import com.instructure.teacher.fragments.ProfileEditFragment
import com.instructure.teacher.fragments.ProfileFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import com.instructure.teacher.fragments.QuizListFragment
import com.instructure.teacher.fragments.QuizPreviewWebviewFragment
import com.instructure.teacher.fragments.SpeedGraderQuizWebViewFragment
import com.instructure.teacher.fragments.ViewHtmlFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewMediaFragment
import com.instructure.teacher.fragments.ViewPdfFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment
import java.util.Locale

object RouteMatcher : BaseRouteMatcher() {

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? = null

    init {
        initRoutes()
        initClassMap()
    }

    private fun initRoutes() {
        routes.add(Route("/", DashboardFragment::class.java))

        routes.add(Route("/login.*", RouteContext.DO_NOT_ROUTE))//FIXME: we know about this

        routes.add(Route("/conversations", InboxFragment::class.java))
        routes.add(Route("/conversations/:conversation_id", InboxFragment::class.java, InboxDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/"), DashboardFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id"), CourseBrowserFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/assignments/syllabus"), SyllabusFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/modules/:module_id"), ModuleListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/modules/items/:${RouterParams.MODULE_ITEM_ID}"),
                ModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"),
                ModuleProgressionFragment::class.java,
                null,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"),
                ModuleProgressionFragment::class.java,
                null,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"),
                ModuleProgressionFragment::class.java,
                null,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"),
                ModuleProgressionFragment::class.java,
                null,
                listOf(":${RouterParams.MODULE_ITEM_ID}")
            )
        )

        routes.add(Route(courseOrGroup("/:course_id/assignments"), AssignmentListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:course_id/assignments/:assignment_id"),
                AssignmentListFragment::class.java,
                ModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:course_id/assignments/:assignment_id"),
                AssignmentListFragment::class.java,
                AssignmentDetailsFragment::class.java
            )
        )
        routes.add(Route(courseOrGroup("/:course_id/assignments/:assignment_id/submissions/:submission_id"), RouteContext.SPEED_GRADER))

        routes.add(Route(courseOrGroup("/:course_id/quizzes"), QuizListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/quizzes/:quiz_id"), QuizListFragment::class.java, ModuleProgressionFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/quizzes/:quiz_id"), QuizListFragment::class.java, QuizDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/discussion_topics"), DiscussionsListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:course_id/discussion_topics/:message_id"),
                DiscussionsListFragment::class.java,
                ModuleProgressionFragment::class.java
            )
        )
        routes.add(
            Route(
                courseOrGroup("/:course_id/discussion_topics/:message_id"),
                DiscussionsListFragment::class.java,
                DiscussionRouterFragment::class.java
            )
        )

        routes.add(Route(courseOrGroup("/:course_id/files"), FileListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/files/:file_id/download"), RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:course_id/files/:file_id/preview"), RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:course_id/files/:file_id"), RouteContext.FILE))
        // Tries to get the folder or preview of file. File can be multiple folders deep (files/folder/folder1/folder2/folder3)...
        routes.add(Route(courseOrGroup("/:course_id/files/folder(\\/.*)*"), FileListFragment::class.java))
        // Same as above, but if they access nested user files instead of course files
        routes.add(Route("/files/folder(\\/.*)*", RouteContext.FILE))
        routes.add(Route("/files/:${RouterParams.FILE_ID}", RouteContext.FILE)) // Triggered by new RCE content file links
        routes.add(Route("/files/:${RouterParams.FILE_ID}/download", RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:course_id/files"), FileListFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/pages/"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/pages/:page_id/"), PageListFragment::class.java, ModuleProgressionFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/pages/:page_id/"), PageListFragment::class.java, PageDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/wiki/"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/wiki/:page_id/"), PageListFragment::class.java, PageDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/announcements"), AnnouncementListFragment::class.java))
        routes.add(
            Route(
                courseOrGroup("/:course_id/announcements/:message_id"),
                AnnouncementListFragment::class.java,
                DiscussionRouterFragment::class.java
            )
        )
        routes.add(Route(courseOrGroup("/:course_id/users"), PeopleListFragment::class.java))

        // Calendar
        routes.add(Route("/:${EventFragment.CONTEXT_TYPE}/:${EventFragment.CONTEXT_ID}/calendar_events/:${EventFragment.SCHEDULE_ITEM_ID}", EventFragment::class.java))

        // To Do
        routes.add(Route("/todos/:${ToDoFragment.PLANNABLE_ID}", ToDoFragment::class.java))
    }

    private fun initClassMap() {
        // Fullscreen Fragments
        fullscreenFragments.add(DashboardFragment::class.java)
        fullscreenFragments.add(ProfileFragment::class.java)
        fullscreenFragments.add(ViewImageFragment::class.java)
        fullscreenFragments.add(FullscreenInternalWebViewFragment::class.java)
        fullscreenFragments.add(LtiLaunchFragment::class.java)
        fullscreenFragments.add(SpeedGraderQuizWebViewFragment::class.java)
        fullscreenFragments.add(HtmlContentFragment::class.java)
        fullscreenFragments.add(ViewPdfFragment::class.java)
        fullscreenFragments.add(ViewHtmlFragment::class.java)
        fullscreenFragments.add(EditDashboardFragment::class.java)
        fullscreenFragments.add(CourseBrowserFragment::class.java)
        fullscreenFragments.add(ModuleProgressionFragment::class.java)
        fullscreenFragments.add(ToDoFragment::class.java)
        fullscreenFragments.add(EventFragment::class.java)
        fullscreenFragments.add(SettingsFragment::class.java)
        fullscreenFragments.add(EmailNotificationPreferencesFragment::class.java)
        fullscreenFragments.add(PushNotificationPreferencesFragment::class.java)
        fullscreenFragments.add(FeatureFlagsFragment::class.java)
        fullscreenFragments.add(RemoteConfigParamsFragment::class.java)
        fullscreenFragments.add(InboxSignatureFragment::class.java)

        // Bottom Sheet Fragments
        bottomSheetFragments.add(EditAssignmentDetailsFragment::class.java)
        bottomSheetFragments.add(AssigneeListFragment::class.java)
        bottomSheetFragments.add(CourseSettingsFragment::class.java)
        bottomSheetFragments.add(EditQuizDetailsFragment::class.java)
        bottomSheetFragments.add(QuizPreviewWebviewFragment::class.java)
        bottomSheetFragments.add(InboxComposeFragment::class.java)
        bottomSheetFragments.add(ChooseRecipientsFragment::class.java)
        bottomSheetFragments.add(CreateDiscussionWebViewFragment::class.java)
        bottomSheetFragments.add(AnnotationCommentListFragment::class.java)
        bottomSheetFragments.add(ProfileFragment::class.java)
        bottomSheetFragments.add(ProfileEditFragment::class.java)
        bottomSheetFragments.add(StudentContextFragment::class.java)
        bottomSheetFragments.add(AttendanceListFragment::class.java)
        bottomSheetFragments.add(EditFileFolderFragment::class.java)
        bottomSheetFragments.add(CreateOrEditPageDetailsFragment::class.java)
        bottomSheetFragments.add(EditSyllabusFragment::class.java)
        bottomSheetFragments.add(PostPolicyFragment::class.java)
        bottomSheetFragments.add(CreateUpdateToDoFragment::class.java)
        bottomSheetFragments.add(CreateUpdateEventFragment::class.java)
    }

    private fun routeUrl(activity: FragmentActivity, url: String) {
        routeUrl(activity, url, ApiPrefs.domain)
    }

    fun routeUrl(activity: FragmentActivity, url: String, domain: String) {
        /* Possible activity types we can navigate to: Unknown Link, InitActivity, Master/Detail, Fullscreen, WebView, ViewMedia */

        // Find the best route
        // Pass that along to the activity
        // One or two classes? (F, or M/D)

        route(activity, getInternalRoute(url, domain))
    }

    fun route(activity: FragmentActivity, route: Route?) {
        if (route == null || route.routeContext === RouteContext.DO_NOT_ROUTE) {
            if (route?.uri != null) {
                //No route, no problem
                handleWebViewUrl(activity, route.uri.toString())

            }
        } else if (route.routeContext == RouteContext.FILE
            || route.primaryClass?.isAssignableFrom(FileListFragment::class.java) == true
            && route.queryParamsHash.containsKey(RouterParams.PREVIEW)
        ) {
            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                if (route.uri != null) {
                    openMedia(activity, route.uri.toString())
                }
            } else {
                handleSpecificFile(
                    activity,
                    (if (route.queryParamsHash.containsKey(RouterParams.PREVIEW)) route.queryParamsHash[RouterParams.PREVIEW] else route.paramsHash[RouterParams.FILE_ID]).orEmpty(),
                    route
                )
            }

        } else if (route.routeContext === RouteContext.MEDIA) {
            handleMediaRoute(activity, route)
        } else if (route.routeContext === RouteContext.SPEED_GRADER) {
            handleSpeedGraderRoute(activity, route)
        } else if (activity.resources.getBoolean(R.bool.isDeviceTablet)) {
            handleTabletRoute(activity, route)
        } else {
            handleFullscreenRoute(activity, route)
        }
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    fun canRouteInternally(activity: FragmentActivity?, url: String?, domain: String, routeIfPossible: Boolean): Boolean {
        if (url.isNullOrBlank()) return false

        val canRoute = getInternalRoute(url, domain) != null

        if (canRoute && activity != null && routeIfPossible) {
            routeUrl(activity, url)
        }
        return canRoute
    }

    private fun handleTabletRoute(context: Context, route: Route) {
        val primaryClass = route.primaryClass
        val secondaryClass = route.secondaryClass

        if (primaryClass != null && secondaryClass != null) {
            handleMasterDetailRoute(context, route)
        } else {
            if (primaryClass == null && secondaryClass == null) {
                handleWebViewRoute(context, route)
            } else if (primaryClass == null) {
                handleDetailRoute(context, route)
            } else {
                when {
                    isFullScreenClass(primaryClass) -> handleFullscreenRoute(context, route)
                    isBottomSheetClass(primaryClass) -> handleBottomSheetRoute(context, route)
                    else -> handleMasterDetailRoute(context, route) // Master only, no Detail exists yet
                }
            }
        }
    }

    private fun handleMasterDetailRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleMasterDetailRoute()")
        context.startActivity(MasterDetailActivity.createIntent(context, route))
    }

    private fun handleDetailRoute(context: Context, route: Route) {
        if (route.removePreviousScreen) (context as? FragmentActivity)?.supportFragmentManager?.popBackStackImmediate()
        if (context is MasterDetailInteractions) {
            Logger.i("RouteMatcher:handleDetailRoute() - MasterDetailInteractions")
            (context as MasterDetailInteractions).addFragment(route)
        } else if (context is InitActivityInteractions) {
            (context as InitActivityInteractions).addFragment(route)
        }
    }

    private fun handleFullscreenRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleFullscreenRoute()")
        if (route.removePreviousScreen) (context as? Activity)?.finish()
        context.startActivity(FullscreenActivity.createIntent(context, route))
    }

    private fun handleMediaRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleMediaRoute()")
        context.startActivity(ViewMediaActivity.createIntent(context, route))
    }

    private fun handleSpeedGraderRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleSpeedGraderRoute()")
        context.startActivity(SpeedGraderActivity.createIntent(context, route))
    }

    private fun handleWebViewRoute(context: Context, route: Route) {
        context.startActivity(InternalWebViewActivity.createIntent(context, route, "", false))
    }

    private fun handleWebViewUrl(context: Context, url: String?) {
        Logger.i("RouteMatcher:handleWebViewRoute()")
        context.startActivity(InternalWebViewActivity.createIntent(context, url!!, "", false))
    }

    private fun handleBottomSheetRoute(context: Context, route: Route) {
        if (context is BottomSheetInteractions) {
            Logger.i("RouteMatcher:handleBottomSheetRoute() - BottomSheetInteractions")
            (context as BottomSheetInteractions).addFragment(route)
        } else {
            Logger.i("RouteMatcher:handleBottomSheetRoute()")
            context.startActivity(BottomSheetActivity.createIntent(context, route))
        }
    }

    /**
     * Pass in a route and a course, get a fragment back!
     */
    fun getFullscreenFragment(canvasContext: CanvasContext?, route: Route): Fragment? {

        return if (canvasContext == null) {
            //TODO: INBOX, PROFILE, or CourseList
            if (route.primaryClass != null) {
                getFrag(route.primaryClass, null, route)
            } else {
                getFrag(route.secondaryClass, null, route)
            }
        } else {
            //we have a route, load up the secondary class if there is one, otherwise primary
            if (route.secondaryClass != null) {
                //load it up
                getFrag(route.secondaryClass, canvasContext, route)
            } else {
                //load up the primary class
                getFrag(route.primaryClass, canvasContext, route)
            }
        }
    }

    fun getMasterFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        //TODO: INBOX
        return getFrag(route.primaryClass, canvasContext, route)
    }

    fun getDetailFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        //TODO: INBOX
        return getFrag(route.secondaryClass, canvasContext, route)
    }

    fun getBottomSheetFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        return getFrag(route.primaryClass, canvasContext, route)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Type : Fragment> getFrag(cls: Class<Type>?, canvasContext: CanvasContext?, route: Route): Type? {
        if (cls == null) return null

        var fragment: Fragment? = null

        when {
            ProfileFragment::class.java.isAssignableFrom(cls) -> fragment = ProfileFragment()
            CourseBrowserFragment::class.java.isAssignableFrom(cls) -> fragment = CourseBrowserFragment.newInstance((canvasContext as Course?)!!)
            CourseBrowserEmptyFragment::class.java.isAssignableFrom(cls) -> fragment = CourseBrowserEmptyFragment
                .newInstance((canvasContext as Course?)!!)
            DashboardFragment::class.java.isAssignableFrom(cls) -> fragment = DashboardFragment.getInstance()
            AssignmentListFragment::class.java.isAssignableFrom(cls) -> fragment = AssignmentListFragment
                .newInstance(canvasContext!!, route)
            AssignmentDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getAssignmentDetailsFragment(canvasContext, route)
            DueDatesFragment::class.java.isAssignableFrom(cls) -> fragment = DueDatesFragment
                .getInstance((canvasContext as Course?)!!, route.arguments)
            AssignmentSubmissionListFragment::class.java.isAssignableFrom(cls) -> fragment = AssignmentSubmissionListFragment
                .newInstance((canvasContext as Course?)!!, route.arguments)
            PostPolicyFragment::class.java.isAssignableFrom(cls) -> fragment = PostPolicyFragment.newInstance(route.argsWithContext)
            EditAssignmentDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditAssignmentDetailsFragment
                .newInstance((canvasContext as Course?)!!, route.arguments)
            AssigneeListFragment::class.java.isAssignableFrom(cls) -> fragment = AssigneeListFragment.newInstance(route.arguments)
            CourseSettingsFragment::class.java.isAssignableFrom(cls) -> fragment = CourseSettingsFragment.newInstance((canvasContext as Course?)!!)
            QuizListFragment::class.java.isAssignableFrom(cls) -> fragment = QuizListFragment.newInstance(canvasContext!!)
            QuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getQuizDetailsFragment(canvasContext, route)
            EditQuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditQuizDetailsFragment
                .newInstance((canvasContext as Course?)!!, route.arguments)
            QuizPreviewWebviewFragment::class.java.isAssignableFrom(cls) -> fragment = QuizPreviewWebviewFragment.newInstance(route.arguments)
            EditQuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditQuizDetailsFragment
                .newInstance((canvasContext as Course?)!!, route.arguments)
            AnnouncementListFragment::class.java.isAssignableFrom(cls) -> fragment = AnnouncementListFragment
                .newInstance(canvasContext!!) // This needs to be above DiscussionsListFragment because it extends it
            DiscussionsListFragment::class.java.isAssignableFrom(cls) -> fragment = DiscussionsListFragment.newInstance(canvasContext!!)
            DiscussionDetailsWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = DiscussionDetailsWebViewFragment.newInstance(route)
            DiscussionRouterFragment::class.java.isAssignableFrom(cls) -> fragment = DiscussionRouterFragment.newInstance(canvasContext!!, route)
            InboxFragment::class.java.isAssignableFrom(cls) -> fragment = InboxFragment.newInstance(route)
            InboxComposeFragment::class.java.isAssignableFrom(cls) -> fragment = InboxComposeFragment.newInstance(route)
            InboxDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = InboxDetailsFragment.newInstance(route)
            ViewPdfFragment::class.java.isAssignableFrom(cls) -> fragment = ViewPdfFragment.newInstance(route.arguments)
            ViewImageFragment::class.java.isAssignableFrom(cls) -> fragment = ViewImageFragment.newInstance(route.arguments)
            ViewMediaFragment::class.java.isAssignableFrom(cls) -> fragment = ViewMediaFragment.newInstance(route.arguments)
            ViewHtmlFragment::class.java.isAssignableFrom(cls) -> fragment = ViewHtmlFragment.newInstance(route.arguments)
            ViewUnsupportedFileFragment::class.java.isAssignableFrom(cls) -> fragment = ViewUnsupportedFileFragment.newInstance(route.arguments)
            ChooseRecipientsFragment::class.java.isAssignableFrom(cls) -> fragment = ChooseRecipientsFragment.newInstance(route.arguments)
            SpeedGraderQuizWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = SpeedGraderQuizWebViewFragment.newInstance(route.arguments)
            AnnotationCommentListFragment::class.java.isAssignableFrom(cls) -> fragment = AnnotationCommentListFragment.newInstance(route.arguments)
            CreateDiscussionWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = CreateDiscussionWebViewFragment.newInstance(route)
            SettingsFragment::class.java.isAssignableFrom(cls) -> fragment = SettingsFragment.newInstance(route)
            ProfileEditFragment::class.java.isAssignableFrom(cls) -> fragment = ProfileEditFragment.newInstance(route.arguments)
            LtiLaunchFragment::class.java.isAssignableFrom(cls) -> fragment = LtiLaunchFragment.newInstance(route)
            PeopleListFragment::class.java.isAssignableFrom(cls) -> fragment = PeopleListFragment.newInstance(canvasContext!!)
            StudentContextFragment::class.java.isAssignableFrom(cls) -> fragment = StudentContextFragment.newInstance(route.arguments)
            AttendanceListFragment::class.java.isAssignableFrom(cls) -> fragment = AttendanceListFragment
                .newInstance(canvasContext!!, route.arguments)
            FileListFragment::class.java.isAssignableFrom(cls) -> fragment = FileListFragment
                .newInstance(canvasContext ?: route.canvasContext!!, route.arguments)
            PageListFragment::class.java.isAssignableFrom(cls) -> fragment = PageListFragment.newInstance(canvasContext!!)
            PageDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getPageDetailsFragment(canvasContext, route)
            EditFileFolderFragment::class.java.isAssignableFrom(cls) -> fragment = EditFileFolderFragment.newInstance(route.arguments)
            CreateOrEditPageDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = CreateOrEditPageDetailsFragment
                .newInstance(route.arguments)
            FullscreenInternalWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = FullscreenInternalWebViewFragment
                .newInstance(route.arguments)
            InternalWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = InternalWebViewFragment.newInstance(route.arguments)
            HtmlContentFragment::class.java.isAssignableFrom(cls) -> fragment = HtmlContentFragment.newInstance(route.arguments)
        } //NOTE: These should remain at or near the bottom to give fragments that extend InternalWebViewFragment the chance first

        return fragment as Type?
    }

    private fun getAssignmentDetailsFragment(canvasContext: CanvasContext?, route: Route): AssignmentDetailsFragment {
        return if (route.arguments.containsKey(AssignmentDetailsFragment.ASSIGNMENT)) {
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
        } else {
            //parse the route to get the assignment id
            val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLongOrNull() ?: 0L
            val args = AssignmentDetailsFragment.makeBundle(assignmentId)
            AssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, args)
        }
    }

    private fun getQuizDetailsFragment(canvasContext: CanvasContext?, route: Route): Fragment {
        return if (route.arguments.containsKey(QuizDetailsFragment.QUIZ)) {
            QuizDetailsFragment.newInstance(canvasContext as Course, route.arguments)
        } else {
            // Parse the route to get the quiz id
            val quizId = route.paramsHash[RouterParams.QUIZ_ID]?.toLongOrNull() ?: route.arguments.get(QuizDetailsFragment.QUIZ_ID) as Long?
            if (quizId != null) {
                val args = QuizDetailsFragment.makeBundle(quizId)
                QuizDetailsFragment.newInstance(canvasContext as Course, args)
            } else {
                // Could not get quiz ID; route to quiz list instead
                QuizListFragment.newInstance(canvasContext!!)
            }
        }
    }

    private fun getPageDetailsFragment(canvasContext: CanvasContext?, route: Route): PageDetailsFragment {
        return if (route.arguments.containsKey(PageDetailsFragment.PAGE)) {
            PageDetailsFragment.newInstance(canvasContext!!, route.arguments)
        } else {
            //parse the route to get the page id
            val pageId = route.paramsHash[RouterParams.PAGE_ID]
            val args = PageDetailsFragment.makeBundle(pageId ?: "")
            PageDetailsFragment.newInstance(canvasContext!!, args)
        }
    }

    fun <Type : Fragment> getClassDisplayName(context: Context, cls: Class<Type>?): String {
        return when {
            cls == null -> return ""
            cls.isAssignableFrom(AssignmentListFragment::class.java) -> context.getString(R.string.tab_assignments)
            cls.isAssignableFrom(QuizListFragment::class.java) -> context.getString(R.string.tab_quizzes)
            cls.isAssignableFrom(DiscussionsListFragment::class.java) -> context.getString(R.string.tab_discussions)
            cls.isAssignableFrom(InboxFragment::class.java) -> context.getString(R.string.tab_inbox)
            else -> ""
        }
    }

    private fun getLoaderCallbacks(activity: FragmentActivity): LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                    return OpenMediaAsyncTaskLoader(activity, args)
                }

                override fun onLoadFinished(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>, loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia) {
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
                            val args = ViewHtmlFragment.makeDownloadBundle(
                                loadedMedia.bundle!!.getString(Const.INTERNAL_URL)!!,
                                loadedMedia.bundle!!.getString(Const.ACTION_BAR_TITLE)!!
                            )
                            route(activity, Route(ViewHtmlFragment::class.java, null, args))
                        } else if (loadedMedia.intent != null) {
                            if (loadedMedia.intent?.type?.contains("pdf") == true && !loadedMedia.isUseOutsideApps) {
                                // Show pdf with PSPDFkit
                                val args = ViewPdfFragment.newInstance((loader as OpenMediaAsyncTaskLoader).url, 0).nonNullArgs
                                route(activity, Route(ViewPdfFragment::class.java, null, args))
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

                override fun onLoaderReset(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>) {}
            }
        }
        return openMediaCallbacks!!
    }

    fun openMedia(activity: FragmentActivity?, url: String?, fileName: String? = null, fileId: String? = null) {
        if (activity != null) {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url, fileName, fileId)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID
            )
        }
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
                val bundle = InternalWebViewFragment.makeBundle(url = fileUrl, title = filename, shouldAuthenticate = needsAuth)
                route(activity, Route(FullscreenInternalWebViewFragment::class.java, context, bundle))
            }
        } else {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(mime, url, filename, fileId)
            LoaderUtils.restartLoaderWithBundle(
                LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID
            )
        }
    }

    private fun handleSpecificFile(activity: FragmentActivity, fileID: String?, route: Route) {
        val fileFolderStatusCallback = object : StatusCallback<FileFolder>() {
            override fun onResponse(
                response: retrofit2.Response<FileFolder>,
                linkHeaders: LinkHeaders,
                type: ApiType
            ) {
                super.onResponse(response, linkHeaders, type)
                val fileFolder = response.body()
                if (fileFolder!!.isLocked || fileFolder.isLockedForUser) {
                    Toast.makeText(
                        activity,
                        String.format(
                            activity.getString(R.string.fileLocked),
                            if (fileFolder.displayName == null) activity.getString(R.string.file) else fileFolder.displayName
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    openMedia(activity, fileFolder.contentType!!, fileFolder.url!!, fileFolder.displayName!!, route, fileID)
                }
            }
        }

        FileFolderManager.getFileFolderFromURL("files/$fileID", true, fileFolderStatusCallback)
    }
}
