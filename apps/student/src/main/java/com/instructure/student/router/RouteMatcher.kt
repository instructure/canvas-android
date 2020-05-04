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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.interactions.router.*
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LoaderUtils
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.student.R
import com.instructure.student.activity.*
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListFragment
import com.instructure.student.mobius.syllabus.ui.SyllabusFragment
import com.instructure.student.util.FileUtils
import retrofit2.Call
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern

object RouteMatcher : BaseRouteMatcher() {

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? = null // I'll bet this causes a memory leak

    init {
        initRoutes()
        initClassMap()
    }

    // Be sensitive to the order of items. It really, really matters.
    private fun initRoutes() {
        routes.add(Route("/", DashboardFragment::class.java))
        // region Conversations
        routes.add(Route("/conversations", InboxFragment::class.java))
        routes.add(Route("/conversations/:${RouterParams.CONVERSATION_ID}", InboxConversationFragment::class.java))
        routes.add(Route("/login.*", RouteContext.DO_NOT_ROUTE))//FIXME: we know about this
        // endregion

        //////////////////////////
        // Courses
        //////////////////////////
        routes.add(Route(courseOrGroup("/"), DashboardFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}"), CourseBrowserFragment::class.java, NotificationListFragment::class.java, Arrays.asList(":${RouterParams.RECENT_ACTIVITY}"))) // Recent Activity
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}"), CourseBrowserFragment::class.java))

        // region Modules

        /*
        Modules with query params
        !!!!!!!!!!!!
        !  CAUTION: Order matters, these are purposely placed above the pages, quizzes, disscussions, assignments, and files so they are matched if query params exist and routed to Modules
        !!!!!!!!!!!!
        */
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules"), ModuleListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules/items/:${RouterParams.MODULE_ITEM_ID}"), ModuleListFragment::class.java)) // Just route to modules list. API does not have a way to fetch a module item without knowing the module id (even though web canvas can do it)
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/modules/:${RouterParams.MODULE_ID}"), ModuleListFragment::class.java))

        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"), ModuleListFragment::class.java, CourseModuleProgressionFragment::class.java, Arrays.asList(":${RouterParams.MODULE_ITEM_ID}")))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"), ModuleListFragment::class.java, CourseModuleProgressionFragment::class.java, Arrays.asList(":${RouterParams.MODULE_ITEM_ID}")))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"), ModuleListFragment::class.java, CourseModuleProgressionFragment::class.java, Arrays.asList(":${RouterParams.MODULE_ITEM_ID}")))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"), ModuleListFragment::class.java, CourseModuleProgressionFragment::class.java, Arrays.asList(":${RouterParams.MODULE_ITEM_ID}")))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}"), ModuleListFragment::class.java, CourseModuleProgressionFragment::class.java, Arrays.asList(":${RouterParams.MODULE_ITEM_ID}"))) // TODO TEST
        // endregion

        // Notifications
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/notifications"), NotificationListFragment::class.java))

        // Grades
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/grades"), GradesListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/grades/:${RouterParams.ASSIGNMENT_ID}"), GradesListFragment::class.java, AssignmentDetailsFragment::class.java))

        // People
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/users"), PeopleListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/users/:${RouterParams.USER_ID}"), PeopleListFragment::class.java, PeopleDetailsFragment::class.java))

        // Files
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files"), FileListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/folder/:${RouterParams.FOLDER_NAME}"), RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}/download"), RouteContext.FILE)) // trigger webview's download listener
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/:${RouterParams.FILE_ID}"), RouteContext.FILE))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/files/folder(\\/.*)*/:${RouterParams.FILE_ID}"), RouteContext.FILE))
        routes.add(Route("/files/folder(\\/.*)*/:${RouterParams.FILE_ID}", RouteContext.FILE))

        // Discussions
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics"), DiscussionListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/discussion_topics/:${RouterParams.MESSAGE_ID}"), DiscussionListFragment::class.java, DiscussionDetailsFragment::class.java))

        // Pages
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/pages"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/pages/:${RouterParams.PAGE_ID}"), PageListFragment::class.java, PageDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/wiki"), PageListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/wiki/:${RouterParams.PAGE_ID}"), PageListFragment::class.java, PageDetailsFragment::class.java))

        // Announcements
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/announcements"), AnnouncementListFragment::class.java))
        // :message_id because it shares with discussions
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/announcements/:${RouterParams.MESSAGE_ID}"), AnnouncementListFragment::class.java, DiscussionDetailsFragment::class.java))
        // Announcements from the notifications tab
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/announcements/:${RouterParams.MESSAGE_ID}"), NotificationListFragment::class.java, DiscussionDetailsFragment::class.java))

        // Quiz
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes"), QuizListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/quizzes/:${RouterParams.QUIZ_ID}"), QuizListFragment::class.java, BasicQuizViewFragment::class.java))


        // Calendar
        routes.add(Route("/calendar", CalendarFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/calendar_events/:${RouterParams.EVENT_ID}"), CalendarFragment::class.java))

        // Syllabus
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/syllabus"), SyllabusFragment::class.java))

        // Assignments
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments"), AssignmentListFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"), AssignmentListFragment::class.java, AssignmentDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"), NotificationListFragment::class.java, AssignmentDetailsFragment::class.java))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"), CalendarFragment::class.java, AssignmentDetailsFragment::class.java))

        // Studio
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/external_tools/:${RouterParams.EXTERNAL_ID}/resource_selection"), StudioWebViewFragment::class.java))


        // Submissions
        // :sliding_tab_type can be /rubric or /submissions (used to navigate to the nested fragment)
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}/:${RouterParams.SLIDING_TAB_TYPE}"), AssignmentDetailsFragment::class.java, SubmissionDetailsFragment::class.java))
        // Route to Assignment Details first - no submission/on paper assignments won't have grades on the Submission Details page, but we also need to account for routing to submission comments (Assignment Details will check for that)
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}/:${RouterParams.SLIDING_TAB_TYPE}/:${RouterParams.SUBMISSION_ID}"), AssignmentDetailsFragment::class.java))

        // Settings
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/settings"), CourseSettingsFragment::class.java))

        // Profile
        routes.add(Route("/profile/settings", ProfileSettingsFragment::class.java))

        // Unsupported
        // NOTE: An Exception to how the router usually works (Not recommended for urls that are meant to be internally routed)
        //  The .* will catch anything and route to UnsupportedFragment. If the users decides to press "open in browser" from the UnsupportedFragment, then InternalWebviewFragment is setup to handle the unsupportedFeature
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/lti_collaborations.*"), UnsupportedTabFragment::class.java, Tab.COLLABORATIONS_ID))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/collaborations.*"), UnsupportedTabFragment::class.java, Tab.COLLABORATIONS_ID))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/outcomes.*"), UnsupportedTabFragment::class.java, Tab.OUTCOMES_ID))
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/conferences.*"), ConferenceListFragment::class.java, Tab.CONFERENCES_ID))

        routes.add(Route("/files", FileListFragment::class.java).apply{ canvasContext = ApiPrefs.user }) // validRoute for FileListFragment checks for a canvasContext, which is null on deep links
        routes.add(Route("/files/folder/:${RouterParams.FOLDER_NAME}", RouteContext.FILE))
        routes.add(Route("/files/:${RouterParams.FILE_ID}/download", RouteContext.FILE)) // trigger webview's download listener
        routes.add(Route("/files/:${RouterParams.FILE_ID}", RouteContext.FILE)) // Triggered by new RCE content file links

        //Notification Preferences
        routes.add(Route("/profile/communication", RouteContext.NOTIFICATION_PREFERENCES))

        //LTI
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/external_tools/:${RouterParams.EXTERNAL_ID}"), RouteContext.LTI))

        //Single Detail Pages (Typically routing from To-dos (may not be handling every use case)
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/assignments/:${RouterParams.ASSIGNMENT_ID}"), AssignmentDetailsFragment::class.java, null))

        // Catch all (when nothing has matched, these take over)
        // Note: Catch all only happens with supported domains such as instructure.com
        routes.add(Route(courseOrGroup("/:${RouterParams.COURSE_ID}/.*"), UnsupportedFeatureFragment::class.java)) // course_id fetches the course context
        routes.add(Route(".*", UnsupportedFeatureFragment::class.java))
    }

    private fun initClassMap() {
        /* Fullscreen Fragments */
//        fullscreenFragments.add(DashboardFragment::class.java)

        /* Bottom Sheet Fragments */
//        bottomSheetFragments.add(EditFavoritesFragment::class.java)
    }

    fun routeUrl(context: Context, url: String, extras: Bundle? = null) {
        routeUrl(context, url, ApiPrefs.domain, extras)
    }

    fun routeUrl(context: Context, url: String, domain: String, extras: Bundle? = null) {
        /* Possible activity types we can navigate too: Unknown Link, InitActivity, Master/Detail, Fullscreen, WebView, ViewMedia */

        //Find the best route
        //Pass that along to the activity
        //One or two classes? (F, or M/D)

        val route = getInternalRoute(url, domain)
        extras?.let { route?.arguments?.putAll(it) }

        // The Group API will not load an individual user's details, so we route to the List fragment by default
        // FIXME: Remove if the group context works with grabbing a user
        if (route?.getContextType() == CanvasContext.Type.GROUP && route.primaryClass == PeopleListFragment::class.java && route.secondaryClass == PeopleDetailsFragment::class.java ) {
            route.primaryClass = null
            route.secondaryClass = PeopleListFragment::class.java
        }

        route(context, route)
    }

    @JvmStatic
    fun route(context: Context, route: Route?) {

        if (route == null || route.routeContext == RouteContext.DO_NOT_ROUTE) {
            if (route?.uri != null) {
                //No route, no problem
                handleWebViewUrl(context, route.uri.toString())
            }
        } else if (route.routeContext == RouteContext.FILE || route.primaryClass?.isAssignableFrom(FileListFragment::class.java) == true && route.queryParamsHash.containsKey(RouterParams.PREVIEW)) {
            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                if (route.uri != null) {
                    openMedia(context as FragmentActivity, route.uri.toString())
                } else if (route.uri != null) {
                    openMedia(context as FragmentActivity, route.uri!!.toString())
                }
            }  else if (route.paramsHash.containsKey(RouterParams.FOLDER_NAME) && !route.queryParamsHash.containsKey(RouterParams.PREVIEW)) {
                // Preview query params are caught under the same route matcher with the :folder_name param, make sure we're not catching preview urls here as well
                // Route to the FileListFragment but to the folder - To route we need to modify the route a bit.
                if (!route.paramsHash.containsKey(RouterParams.COURSE_ID)) {
                    route.canvasContext = ApiPrefs.user
                }
                route.routeContext = RouteContext.UNKNOWN
                route.primaryClass = FileListFragment::class.java
                handleFullscreenRoute(context, route)
            } else {
                val isGroupRoute = "groups" == route.uri?.pathSegments?.get(0)
                handleSpecificFile(
                        context as FragmentActivity,
                        (if (route.queryParamsHash.containsKey(RouterParams.PREVIEW)) route.queryParamsHash[RouterParams.PREVIEW] else route.paramsHash[RouterParams.FILE_ID]) ?: "",
                        route,
                        isGroupRoute)
            }
        } else if (route.routeContext == RouteContext.MEDIA) {
            handleMediaRoute(context, route)
        } else if (route.routeContext == RouteContext.SPEED_GRADER) {
            //handleSpeedGraderRoute(context, route) //Annotations for student maybe?
        } else if (context.resources.getBoolean(R.bool.is_device_tablet)) {
            handleTabletRoute(context, route)
        } else {
            handleFullscreenRoute(context, route)
            (context as? InterwebsToApplication)?.finish()
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

    private fun getLoaderCallbacks(activity: Activity): LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? {
        if (openMediaCallbacks == null) {

            openMediaCallbacks = object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                var dialog: AlertDialog? = null

                override fun onCreateLoader(id: Int, args: Bundle?): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                    if(!activity.isFinishing) {
                        dialog = AlertDialog.Builder(activity, com.instructure.pandautils.R.style.CustomViewAlertDialog)
                                .setView(com.instructure.pandautils.R.layout.dialog_loading_view)
                                .create()
                        dialog!!.show()
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
                            if (loadedMedia.errorType == OpenMediaAsyncTaskLoader.ERROR_TYPE.NO_APPS) {
                                val args = ViewUnsupportedFileFragment.newInstance(loadedMedia.intent.data!!, (loader as OpenMediaAsyncTaskLoader).filename, loadedMedia.intent.type!!, null, R.drawable.vd_attachment).nonNullArgs
                                RouteMatcher.route(activity, Route(ViewUnsupportedFileFragment::class.java, null, args))
                            } else {
                                Toast.makeText(activity, activity.resources.getString(loadedMedia.errorMessage), Toast.LENGTH_LONG).show()
                            }
                        } else if (loadedMedia.isHtmlFile) {
                            val args = ViewHtmlFragment.newInstance(loadedMedia.bundle.getString(Const.INTERNAL_URL)!!, loadedMedia.bundle.getString(Const.ACTION_BAR_TITLE)!!).nonNullArgs
                            RouteMatcher.route(activity, Route(ViewHtmlFragment::class.java, null, args))
                        } else if (loadedMedia.intent != null) {
                            if (loadedMedia.intent.type!!.contains("pdf") && !loadedMedia.isUseOutsideApps) {
                                // Show pdf with PSPDFkit
                                val uri = loadedMedia.intent.data
                                val submissionTarget = loadedMedia.bundle?.getParcelable<ShareFileSubmissionTarget>(Const.SUBMISSION_TARGET)
                                FileUtils.showPdfDocument(uri, loadedMedia, activity, submissionTarget)
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

                override fun onLoaderReset(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>) {
                    dialog?.dismiss()
                }
            }
        }
        return openMediaCallbacks
    }

    fun openMedia(activity: FragmentActivity?, url: String?) {
        if (activity != null) {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID)
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
                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(context, fileUrl, needsAuth, true))
            }
        } else {
            openMediaCallbacks = null
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(mime, url, filename, route.arguments)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(LoaderManager.getInstance(activity), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID)
        }
    }

    private fun handleSpecificFile(activity: FragmentActivity, fileID: String?, route: Route, isGroupFile: Boolean) {

        val fileFolderStatusCallback = object : StatusCallback<FileFolder>() {
            override fun onResponse(response: Response<FileFolder>, linkHeaders: LinkHeaders, type: ApiType) {
                super.onResponse(response, linkHeaders, type)
                response.body()?.let { fileFolder ->
                    if (!isGroupFile && (fileFolder.isLocked || fileFolder.isLockedForUser)) {
                        Toast.makeText(activity, String.format(activity.getString(R.string.fileLocked), if (fileFolder.displayName == null) activity.getString(R.string.file) else fileFolder.displayName), Toast.LENGTH_LONG).show()
                    } else {
                        // This is either a group file (which have no permissions), or a file that is accessible by the user
                        openMedia(activity, fileFolder.contentType!!, fileFolder.url!!, fileFolder.displayName!!, route, fileID)
                    }
                } ?: Toast.makeText(activity, activity.getString(R.string.errorOccurred), Toast.LENGTH_LONG).show()
            }

            override fun onFail(call: Call<FileFolder>?, error: Throwable, response: Response<*>?) {
                super.onFail(call, error, response)
                Toast.makeText(activity, R.string.fileNotFound, Toast.LENGTH_SHORT).show()
            }
        }

        FileFolderManager.getFileFolderFromURL("files/$fileID", true, fileFolderStatusCallback)
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
    @JvmStatic
    @JvmOverloads
    fun canRouteInternally(
        context: Context,
        url: String,
        domain: String,
        routeIfPossible: Boolean,
        allowUnsupported: Boolean = true
    ): Boolean {
        val route = getInternalRoute(url, domain)
        val canRoute = route != null && (allowUnsupported || route.primaryClass != UnsupportedFeatureFragment::class.java)
        if (canRoute && routeIfPossible) routeUrl(context, url)
        return canRoute
    }

    @JvmStatic
    fun generateUrl(url: String?, queryParams: HashMap<String, String>): String? {
        if(url == null) return null
        return createQueryParamString(url, queryParams)
    }

    @JvmStatic
    fun generateUrl(type: CanvasContext.Type, masterCls: Class<out Fragment>?, replacementParams: HashMap<String, String>): String? {
        return generateUrl(type, masterCls, null, replacementParams, null)
    }

    @JvmStatic
    fun generateUrl(type: CanvasContext.Type, masterCls: Class<out Fragment>?, detailCls: Class<out Fragment>?, replacementParams: HashMap<String, String>?, queryParams: HashMap<String, String>?): String? {
        val domain = ApiPrefs.fullDomain
        val urlRoute = getInternalRoute(masterCls, detailCls)
        if(urlRoute != null) {
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

    @JvmStatic
    fun getContextIdFromURL(url: String?): String? {
        return getContextIdFromURL(url, routes)
    }
}

