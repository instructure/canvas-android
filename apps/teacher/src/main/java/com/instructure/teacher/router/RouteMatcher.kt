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
import com.instructure.canvasapi2.utils.Logger
import com.instructure.interactions.BottomSheetInteractions
import com.instructure.interactions.InitActivityInteractions
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.BaseRouteMatcher
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.utils.*
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment
import com.instructure.teacher.R
import com.instructure.teacher.activities.*
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.fragments.*
import com.instructure.teacher.fragments.FileListFragment
import instructure.rceditor.RCEFragment

object RouteMatcher : BaseRouteMatcher() {

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? = null

    init {
        initRoutes()
        initClassMap()
    }

    private fun initRoutes() {
        routes.add(Route("/", CoursesFragment::class.java))

        routes.add(Route("/login.*", RouteContext.DO_NOT_ROUTE))//FIXME: we know about this

        routes.add(Route("/conversations", InboxFragment::class.java))
        routes.add(Route("/conversations/:conversation_id", MessageThreadFragment::class.java))

        routes.add(Route(courseOrGroup("/"), CoursesFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id"), CourseBrowserFragment::class.java))

        // We don't want to route to the syllabus, but this needs to be above the other assignments routing so it catches here first
        routes.add(Route(courseOrGroup("/:course_id/assignments/syllabus"), RouteContext.DO_NOT_ROUTE))

        routes.add(Route(courseOrGroup("/:course_id/assignments"), AssignmentListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/assignments/:assignment_id"), AssignmentListFragment::class.java, AssignmentDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/assignments/:assignment_id/submissions/:submission_id"), RouteContext.SPEED_GRADER))

        routes.add(Route(courseOrGroup("/:course_id/quizzes"), QuizListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/quizzes/:quiz_id"), QuizListFragment::class.java, QuizDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/discussion_topics"), DiscussionsListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/discussion_topics/:message_id"), DiscussionsListFragment::class.java, DiscussionsDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/files"), FileListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/files/:file_id/download"), RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:course_id/files/:file_id"), RouteContext.FILE))
        // Tries to get the folder or preview of file. File can be multiple folders deep (files/folder/folder1/folder2/folder3)...
        routes.add(Route(courseOrGroup("/:course_id/files/folder(\\/.*)*"), FileListFragment::class.java))
        // Same as above, but if they access nested user files instead of course files
        routes.add(Route("/files/folder(\\/.*)*", RouteContext.FILE))
        routes.add(Route("/files/:${RouterParams.FILE_ID}", RouteContext.FILE)) // Triggered by new RCE content file links

        routes.add(Route(courseOrGroup("/:course_id/files"), FileListFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/pages/"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/pages/:page_id/"), PageListFragment::class.java, PageDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/wiki/"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/wiki/:page_id/"), PageListFragment::class.java, PageDetailsFragment::class.java))

        routes.add(Route(courseOrGroup("/:course_id/announcements"), AnnouncementListFragment::class.java))
        routes.add(Route(courseOrGroup("/:course_id/announcements/:message_id"), DiscussionsDetailsFragment::class.java))

    }

    private fun initClassMap() {
        // Fullscreen Fragments
        fullscreenFragments.add(CoursesFragment::class.java)
        fullscreenFragments.add(AllCoursesFragment::class.java)
        fullscreenFragments.add(ProfileFragment::class.java)
        fullscreenFragments.add(ViewImageFragment::class.java)
        fullscreenFragments.add(FullscreenInternalWebViewFragment::class.java)
        fullscreenFragments.add(LTIWebViewFragment::class.java)
        fullscreenFragments.add(SpeedGraderQuizWebViewFragment::class.java)

        // Bottom Sheet Fragments
        bottomSheetFragments.add(EditAssignmentDetailsFragment::class.java)
        bottomSheetFragments.add(AssigneeListFragment::class.java)
        bottomSheetFragments.add(EditFavoritesFragment::class.java)
        bottomSheetFragments.add(CourseSettingsFragment::class.java)
        bottomSheetFragments.add(RCEFragment::class.java)
        bottomSheetFragments.add(EditQuizDetailsFragment::class.java)
        bottomSheetFragments.add(QuizPreviewWebviewFragment::class.java)
        bottomSheetFragments.add(AddMessageFragment::class.java)
        bottomSheetFragments.add(DiscussionsReplyFragment::class.java)
        bottomSheetFragments.add(DiscussionsUpdateFragment::class.java)
        bottomSheetFragments.add(ChooseRecipientsFragment::class.java)
        bottomSheetFragments.add(CreateDiscussionFragment::class.java)
        bottomSheetFragments.add(CreateOrEditAnnouncementFragment::class.java)
        bottomSheetFragments.add(AnnotationCommentListFragment::class.java)
        bottomSheetFragments.add(ProfileFragment::class.java)
        bottomSheetFragments.add(ProfileEditFragment::class.java)
        bottomSheetFragments.add(StudentContextFragment::class.java)
        bottomSheetFragments.add(AttendanceListFragment::class.java)
        bottomSheetFragments.add(EditFileFolderFragment::class.java)
        bottomSheetFragments.add(CreateOrEditPageDetailsFragment::class.java)
    }

    @JvmStatic
    private fun routeUrl(context: Context, url: String) {
        routeUrl(context, url, ApiPrefs.domain)
    }

    @JvmStatic
    fun routeUrl(context: Context, url: String, domain: String) {
        /* Possible activity types we can navigate to: Unknown Link, InitActivity, Master/Detail, Fullscreen, WebView, ViewMedia */

        // Find the best route
        // Pass that along to the activity
        // One or two classes? (F, or M/D)

        route(context, getInternalRoute(url, domain))
    }

    @JvmStatic
    fun route(context: Context, route: Route?) {
        if (route == null || route.routeContext === RouteContext.DO_NOT_ROUTE) {
            if (route?.uri != null) {
                //No route, no problem
                handleWebViewUrl(context, route.uri.toString())

            }
        } else if (route.routeContext == RouteContext.FILE || route.primaryClass?.isAssignableFrom(FileListFragment::class.java) == true && route.queryParamsHash.containsKey(RouterParams.PREVIEW)) {
            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                if (route.uri != null) {
                    openMedia(context as FragmentActivity, route.uri.toString())
                }
            } else {
                handleSpecificFile(
                        context as FragmentActivity,
                        (if (route.queryParamsHash.containsKey(RouterParams.PREVIEW)) route.queryParamsHash[RouterParams.PREVIEW] else route.paramsHash[RouterParams.FILE_ID]) ?: "",
                        route)
            }

        } else if (route.routeContext === RouteContext.MEDIA) {
            handleMediaRoute(context, route)
        } else if (route.routeContext === RouteContext.SPEED_GRADER) {
            handleSpeedGraderRoute(context, route)
        } else if (context.resources.getBoolean(R.bool.isDeviceTablet)) {
            handleTabletRoute(context, route)
        } else {
            handleFullscreenRoute(context, route)
        }
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    fun canRouteInternally(activity: Activity?, url: String?, domain: String, routeIfPossible: Boolean): Boolean {
        if (url.isNullOrBlank()) return false

        val canRoute = getInternalRoute(url!!, domain) != null

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
        if (context is MasterDetailInteractions) {
            Logger.i("RouteMatcher:handleDetailRoute() - MasterDetailInteractions")
            (context as MasterDetailInteractions).addFragment(route)
        } else if (context is InitActivityInteractions) {
            (context as InitActivityInteractions).addFragment(route)
        }
    }

    private fun handleFullscreenRoute(context: Context, route: Route) {
        Logger.i("RouteMatcher:handleFullscreenRoute()")
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
    @JvmStatic
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

    @JvmStatic
    fun getMasterFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        //TODO: INBOX
        return getFrag(route.primaryClass, canvasContext, route)
    }

    @JvmStatic
    fun getDetailFragment(canvasContext: CanvasContext?, route: Route): Fragment? {
        //TODO: INBOX
        return getFrag(route.secondaryClass, canvasContext, route)
    }

    @JvmStatic
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
            CourseBrowserEmptyFragment::class.java.isAssignableFrom(cls) -> fragment = CourseBrowserEmptyFragment.newInstance((canvasContext as Course?)!!)
            CoursesFragment::class.java.isAssignableFrom(cls) -> fragment = CoursesFragment.getInstance()
            AssignmentListFragment::class.java.isAssignableFrom(cls) -> fragment = AssignmentListFragment.getInstance(canvasContext!!, route.arguments)
            AssignmentDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getAssignmentDetailsFragment(canvasContext, route)
            DueDatesFragment::class.java.isAssignableFrom(cls) -> fragment = DueDatesFragment.getInstance((canvasContext as Course?)!!, route.arguments)
            AssignmentSubmissionListFragment::class.java.isAssignableFrom(cls) -> fragment = AssignmentSubmissionListFragment.newInstance((canvasContext as Course?)!!, route.arguments)
            PostPolicyFragment::class.java.isAssignableFrom(cls) -> fragment = PostPolicyFragment.newInstance(route.argsWithContext)
            EditAssignmentDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditAssignmentDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
            AssigneeListFragment::class.java.isAssignableFrom(cls) -> fragment = AssigneeListFragment.newInstance(route.arguments)
            EditFavoritesFragment::class.java.isAssignableFrom(cls) -> fragment = EditFavoritesFragment.newInstance(route.arguments)
            CourseSettingsFragment::class.java.isAssignableFrom(cls) -> fragment = CourseSettingsFragment.newInstance((canvasContext as Course?)!!)
            QuizListFragment::class.java.isAssignableFrom(cls) -> fragment = QuizListFragment.newInstance(canvasContext!!)
            QuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getQuizDetailsFragment(canvasContext, route)
            RCEFragment::class.java.isAssignableFrom(cls) -> fragment = RCEFragment.newInstance(route.arguments)
            EditQuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditQuizDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
            QuizPreviewWebviewFragment::class.java.isAssignableFrom(cls) -> fragment = QuizPreviewWebviewFragment.newInstance(route.arguments)
            EditQuizDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = EditQuizDetailsFragment.newInstance((canvasContext as Course?)!!, route.arguments)
            AnnouncementListFragment::class.java.isAssignableFrom(cls) -> fragment = AnnouncementListFragment.newInstance(canvasContext!!) // This needs to be above DiscussionsListFragment because it extends it
            DiscussionsListFragment::class.java.isAssignableFrom(cls) -> fragment = DiscussionsListFragment.newInstance(canvasContext!!)
            DiscussionsDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getDiscussionDetailsFragment(canvasContext, route)
            InboxFragment::class.java.isAssignableFrom(cls) -> fragment = InboxFragment()
            AddMessageFragment::class.java.isAssignableFrom(cls) -> fragment = AddMessageFragment.newInstance(route.arguments)
            MessageThreadFragment::class.java.isAssignableFrom(cls) -> fragment = getMessageThreadFragment(route)
            ViewPdfFragment::class.java.isAssignableFrom(cls) -> fragment = ViewPdfFragment.newInstance(route.arguments)
            ViewImageFragment::class.java.isAssignableFrom(cls) -> fragment = ViewImageFragment.newInstance(route.arguments)
            ViewMediaFragment::class.java.isAssignableFrom(cls) -> fragment = ViewMediaFragment.newInstance(route.arguments)
            ViewHtmlFragment::class.java.isAssignableFrom(cls) -> fragment = ViewHtmlFragment.newInstance(route.arguments)
            ViewUnsupportedFileFragment::class.java.isAssignableFrom(cls) -> fragment = ViewUnsupportedFileFragment.newInstance(route.arguments)
            cls.isAssignableFrom(DiscussionsReplyFragment::class.java) -> fragment = DiscussionsReplyFragment.newInstance(canvasContext!!, route.arguments)
            cls.isAssignableFrom(DiscussionsUpdateFragment::class.java) -> fragment = DiscussionsUpdateFragment.newInstance(canvasContext!!, route.arguments)
            ChooseRecipientsFragment::class.java.isAssignableFrom(cls) -> fragment = ChooseRecipientsFragment.newInstance(route.arguments)
            SpeedGraderQuizWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = SpeedGraderQuizWebViewFragment.newInstance(route.arguments)
            AnnotationCommentListFragment::class.java.isAssignableFrom(cls) -> fragment = AnnotationCommentListFragment.newInstance(route.arguments)
            CreateDiscussionFragment::class.java.isAssignableFrom(cls) -> fragment = CreateDiscussionFragment.newInstance(route.arguments)
            CreateOrEditAnnouncementFragment::class.java.isAssignableFrom(cls) -> fragment = CreateOrEditAnnouncementFragment.newInstance(route.arguments)
            SettingsFragment::class.java.isAssignableFrom(cls) -> fragment = SettingsFragment.newInstance(route.arguments)
            ProfileEditFragment::class.java.isAssignableFrom(cls) -> fragment = ProfileEditFragment.newInstance(route.arguments)
            LTIWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = LTIWebViewFragment.newInstance(route.arguments)
            PeopleListFragment::class.java.isAssignableFrom(cls) -> fragment = PeopleListFragment.newInstance(canvasContext!!)
            StudentContextFragment::class.java.isAssignableFrom(cls) -> fragment = StudentContextFragment.newInstance(route.arguments)
            AttendanceListFragment::class.java.isAssignableFrom(cls) -> fragment = AttendanceListFragment.newInstance(canvasContext!!, route.arguments)
            FileListFragment::class.java.isAssignableFrom(cls) -> fragment = FileListFragment.newInstance(canvasContext ?: route.canvasContext!!, route.arguments)
            PageListFragment::class.java.isAssignableFrom(cls) -> fragment = PageListFragment.newInstance(canvasContext!!)
            PageDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = getPageDetailsFragment(canvasContext, route)
            EditFileFolderFragment::class.java.isAssignableFrom(cls) -> fragment = EditFileFolderFragment.newInstance(route.arguments)
            CreateOrEditPageDetailsFragment::class.java.isAssignableFrom(cls) -> fragment = CreateOrEditPageDetailsFragment.newInstance(route.arguments)
            FullscreenInternalWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = FullscreenInternalWebViewFragment.newInstance(route.arguments)
            InternalWebViewFragment::class.java.isAssignableFrom(cls) -> fragment = InternalWebViewFragment.newInstance(route.arguments)
        } //NOTE: These should remain at or near the bottom to give fragments that extend InternalWebViewFragment the chance first

        return fragment as Type?
    }

    private fun getMessageThreadFragment(route: Route): Fragment? {
        return if (route.paramsHash.containsKey(Const.CONVERSATION_ID)) {
            val args = MessageThreadFragment.createBundle(route.paramsHash[Const.CONVERSATION_ID]?.toLong()
                    ?: 0L)
            MessageThreadFragment.newInstance(args)
        } else {
            MessageThreadFragment.newInstance(route.arguments)
        }
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

    private fun getDiscussionDetailsFragment(canvasContext: CanvasContext?, route: Route): DiscussionsDetailsFragment {
        return when {
            route.arguments.containsKey(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER) -> DiscussionsDetailsFragment.newInstance(canvasContext!!, route.arguments)
            route.arguments.containsKey(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER_ID) -> {
                val discussionTopicHeaderId = route.arguments.getLong(DiscussionsDetailsFragment.DISCUSSION_TOPIC_HEADER_ID)
                val args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId)
                DiscussionsDetailsFragment.newInstance(canvasContext!!, args)
            }
            else -> {
                // Parse the route to get the discussion id
                val discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong() ?: 0L
                val entryId = route.queryParamsHash[RouterParams.ENTRY_ID]?.toLong() ?: 0L
                val args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId, entryId)
                DiscussionsDetailsFragment.newInstance(canvasContext!!, args)
            }
        }
    }

    @JvmStatic
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

    private fun getLoaderCallbacks(activity: Activity): LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                    return OpenMediaAsyncTaskLoader(activity, args)
                }

                override fun onLoadFinished(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>, loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia) {
                    try {
                        if (loadedMedia.isError) {
                            if (loadedMedia.errorType == OpenMediaAsyncTaskLoader.ERROR_TYPE.NO_APPS) {
                                val args = ViewUnsupportedFileFragment.newInstance(loadedMedia.intent.data!!, (loader as OpenMediaAsyncTaskLoader).filename, loadedMedia.intent.type!!, null, R.drawable.vd_attachment).nonNullArgs
                                RouteMatcher.route(activity, Route(ViewUnsupportedFileFragment::class.java, null, args))
                            } else {
                                Toast.makeText(activity, activity.resources.getString(loadedMedia.errorMessage), Toast.LENGTH_LONG).show()
                            }
                        } else if (loadedMedia.isHtmlFile) {
                            val args = ViewHtmlFragment.makeDownloadBundle(loadedMedia.bundle.getString(Const.INTERNAL_URL)!!, loadedMedia.bundle.getString(Const.ACTION_BAR_TITLE)!!)
                            RouteMatcher.route(activity, Route(ViewHtmlFragment::class.java, null, args))
                        } else if (loadedMedia.intent != null) {
                            if (loadedMedia.intent.type!!.contains("pdf") && !loadedMedia.isUseOutsideApps) {
                                // Show pdf with PSPDFkit
                                val args = ViewPdfFragment.newInstance((loader as OpenMediaAsyncTaskLoader).url, 0).nonNullArgs
                                RouteMatcher.route(activity, Route(ViewPdfFragment::class.java, null, args))
                            } else if (loadedMedia.intent.type == "video/mp4") {
                                val bundle = BaseViewMediaActivity.makeBundle(loadedMedia.intent.data!!.toString(), null, "video/mp4", loadedMedia.intent.dataString, true)
                                RouteMatcher.route(activity, Route(bundle, RouteContext.MEDIA))
                            } else if (loadedMedia.intent.type!!.startsWith("image/")) {
                                val args = ViewImageFragment.newInstance(loadedMedia.intent.dataString!!, loadedMedia.intent.data!!, "image/*", true, 0).nonNullArgs
                                RouteMatcher.route(activity, Route(ViewImageFragment::class.java, null, args))
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
        return openMediaCallbacks
    }

    fun openMedia(activity: FragmentActivity?, url: String?) {
        if (activity != null) {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID)
        }
    }

    private fun openMedia(activity: FragmentActivity?, mime: String, url: String, filename: String, route: Route, fileId: String?) {
        if (activity == null) {
            return
        }

        // If we're trying to open an HTML file, don't download it. It could be referencing other files
        // through a relative URL which we won't be able to access. Instead, just showing the file in
        // a webview will load the file the user is trying to view and will resolve all relative paths
        if (filename.toLowerCase().endsWith(".htm") || filename.toLowerCase().endsWith(".html")) {
            RouteUtils.retrieveFileUrl(route, fileId) { fileUrl, context, needsAuth ->
                val bundle = InternalWebViewFragment.makeBundle(url = fileUrl, title = filename, shouldAuthenticate = needsAuth)
                RouteMatcher.route(activity, Route(FullscreenInternalWebViewFragment::class.java, context, bundle))
            }
        } else {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(mime, url, filename)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID)
        }
    }

    private fun handleSpecificFile(activity: FragmentActivity, fileID: String?, route: Route) {
        val fileFolderStatusCallback = object : StatusCallback<FileFolder>() {
            override fun onResponse(response: retrofit2.Response<FileFolder>, linkHeaders: com.instructure.canvasapi2.utils.LinkHeaders, type: ApiType) {
                super.onResponse(response, linkHeaders, type)
                val fileFolder = response.body()
                if (fileFolder!!.isLocked || fileFolder.isLockedForUser) {
                    Toast.makeText(activity, String.format(activity.getString(R.string.fileLocked), if (fileFolder.displayName == null) activity.getString(R.string.file) else fileFolder.displayName), Toast.LENGTH_LONG).show()
                } else {
                    openMedia(activity, fileFolder.contentType!!, fileFolder.url!!, fileFolder.displayName!!, route, fileID)
                }
            }
        }

        FileFolderManager.getFileFolderFromURL("files/$fileID", true, fileFolderStatusCallback)
    }
}
