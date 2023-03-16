/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.interactions.router

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.APIHelper
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

@Parcelize
data class Route(
        /* A CanvasContext representing a Course, Group, or User*/
        var canvasContext: CanvasContext? = null,
        /* Any params needed for the fragment */
        var arguments: @WriteWith<BundleParceler> Bundle = Bundle(),
        /* The pattern of the URL we want to match against */
        var routePattern: Pattern? = null,
        /* The original URL as a Uri */
        var uri: Uri? = null,
        /* Primary Java Class Name ex: AssignmentsList.class */
        var primaryClass: Class<out Fragment>? = null,
        /* Secondary Java Class Name ex: AssignmentsList.class */
        var secondaryClass: Class<out Fragment>? = null,
        /* The hash of params we care about. ex: assignments/12345 */
        var paramsHash: HashMap<String, String> = HashMap(),
        /* The hash of query params we care about. ex: ?modules/12345 */
        var queryParamsHash: HashMap<String, String> = HashMap(),
        /* A temporary store of param names that get used when we obtain a URL */
        val paramNames: ArrayList<String> = ArrayList(),
        /* A temporary store of query param names that get used when we obtain a URL */
        val queryParamNames: ArrayList<String> = ArrayList(),
        /* The Course ID, no course id if not relevant. ex: Inbox vs AssignmentsList */
        val courseId: Long? = null,
        /* Information to understand what the route should do */
        var routeContext: RouteContext = RouteContext.UNKNOWN,
        /* Where the placement of the fragment belongs. Typically applied from the RouteMatcher. */
        var routeType: RouteType = RouteType.FULLSCREEN,
        /* A request to ignore any debouncing at the FragmentTransaction stage */
        var ignoreDebounce: Boolean = false,
        /* The URL of the path before the params have been replaced. */
        var routePath: String? = null,
        /* A tab id see the Tabs api for a definition. Not required for most routes. */
        var tabId: String? = null,
        /* If true removes the previous screen from the backstack when routing to this route. */
        var removePreviousScreen: Boolean = false
) : Parcelable {

    //All constructors should eventually set the [Route.routePath]
    constructor(routePath: String?) : this() {
        if (routePath == null) return
        this.routePath = routePath

        /* match anything but a slash after a colon and create a group for the name of the param */
        val matcher = Pattern.compile("/:([^/]*)").matcher(routePath)

        // Get the names of the params
        while (matcher.find()) {
            paramNames.add(matcher.group(1))
        }

        /* match a slash, colon and then anything but a slash. Matched value is replaced so the param value can be parsed */
        val paramValueMatcher = Pattern.compile("/:[^/]*").matcher(routePath)

        if (paramValueMatcher.find()) {
            /* Create a group where the param was, so the value can be located */
            var paramValueRegex = paramValueMatcher.replaceAll("/([^/]*)")
            paramValueRegex = addLineMatchingAndOptionalEndSlash(paramValueRegex)
            routePattern = Pattern.compile(paramValueRegex)
        } else { // does not contain params, just look for exact match
            routePattern = Pattern.compile(addLineMatchingAndOptionalEndSlash(routePath))
        }
    }

    constructor(routePath: String?, routeContext: RouteContext) : this(routePath) {
        this.routeContext = routeContext
    }

    constructor(routePath: String?, routeContext: RouteContext, secondaryClass: Class<out Fragment>?) : this(routePath) {
        this.routeContext = routeContext
        this.secondaryClass = secondaryClass
    }

    constructor(routePath: String?, primaryClass: Class<out Fragment>?) : this(routePath) {
        this.primaryClass = primaryClass
    }

    constructor(routePath: String?, primaryClass: Class<out Fragment>?, tabId: String) : this(routePath) {
        this.primaryClass = primaryClass
        this.tabId = tabId
    }

    constructor(routePath: String?, primaryClass: Class<out Fragment>?, secondaryClass: Class<out Fragment>?) : this(routePath, primaryClass) {
        this.secondaryClass = secondaryClass
    }

    constructor(routePath: String?, primaryClass: Class<out Fragment>?, secondaryClass: Class<out Fragment>?, queryParamNames: List<String> = ArrayList()) : this(routePath, primaryClass) {
        this.secondaryClass = secondaryClass
        this.queryParamNames.addAll(queryParamNames)
    }

    constructor(primaryClass: Class<out Fragment>?, canvasContext: CanvasContext?) : this() {
        this.primaryClass = primaryClass
        this.secondaryClass = null
        this.canvasContext = canvasContext
    }

    constructor(primaryClass: Class<out Fragment>?, canvasContext: CanvasContext?, arguments: Bundle) : this(primaryClass, canvasContext) {
        this.arguments = arguments
    }

    constructor(primaryClass: Class<out Fragment>?, canvasContext: CanvasContext?, arguments: Bundle, tabId: String) : this(primaryClass, canvasContext) {
        this.arguments = arguments
        this.tabId = tabId
    }

    constructor(primaryClass: Class<out Fragment>?, canvasContext: CanvasContext?, arguments: Bundle, ignoreDebounce: Boolean) : this(primaryClass, canvasContext) {
        this.arguments = arguments
        this.ignoreDebounce = ignoreDebounce
    }

    constructor(primaryClass: Class<out Fragment>?, secondaryClass: Class<out Fragment>?, canvasContext: CanvasContext?, arguments: Bundle) : this(null, primaryClass, secondaryClass) {
        this.canvasContext = canvasContext
        this.arguments = arguments
    }

    constructor(bundle: Bundle, routeContext: RouteContext) : this() {
        this.arguments = bundle
        this.routeContext = routeContext
    }

    fun getQueryString(): String = uri?.query ?: ""

    fun getFragmentIdentifier(): String = uri?.fragment ?: ""


    /**
     * Adds '^' and '$' to regex for line matching
     * Also makes ending slash and api/v1 optional
     *
     * @param regex value to regex against
     * @return a value :)
     */
    private fun addLineMatchingAndOptionalEndSlash(regex: String): String {
        var reg = regex
        reg = if (reg.endsWith("/")) {
            String.format("^(?:/api/v1)?%s?$", reg)
        } else {
            String.format("^(?:/api/v1)?%s/?$", reg)
        }
        return reg
    }

    /**
     * When a route is a match, the paramsHash, queryParamsHash, and Uri are set.
     *
     * @param url A Url string to be checked against routes
     * @return true is route is a match, false otherwise
     */
    fun apply(url: String?): Boolean {
        if (url == null) return false

        val parsedUri = Uri.parse(url)
        val path = parsedUri.path.orEmpty()
        val isMatch = routePattern?.matcher(path)?.find() ?: false
        if (isMatch) {
            if (RouteContext.EXTERNAL == routeContext) return true // recognized as a match so the unsupported fragment doesn't match it, then getInternalRoute will handle it

            uri = parsedUri
            paramsHash = createParamsHash(path)
            queryParamsHash = createQueryParamsHash(parsedUri)

            if (!queryParamNames.isEmpty()) return queryParamsHash.keys.any {
                key -> queryParamNames.any { param -> param.contains(key) || key.contains(param) }
            }

            this.uri = Uri.parse(url)
        }
        return isMatch
    }

    fun apply(primaryClass: Class<out Fragment>?, secondaryClass: Class<out Fragment>?): Boolean {
        return RouteContext.EXTERNAL != routeContext && this.primaryClass == primaryClass && this.secondaryClass == secondaryClass
    }

    /**
     * A param hash contains the key and values for the route
     * Example: If the route is /courses/:course_id and the url /courses/1234 is applied. The paramsHash will contain "course_id" -> "1234"
     *
     * @param url A Url String used to create a hash of url params
     * @return HashMap of url params
     */
    private fun createParamsHash(url: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        if (routePattern == null) return params
        val matcher = routePattern!!.matcher(url)
        val paramValues = ArrayList<String>()
        if (matcher.find()) {
            for (i in 0 until matcher.groupCount()) {
                try {
                    // index 0 is the original string that was matched. Just get the group values
                    paramValues.add(matcher.group(i + 1))
                } catch (e: Exception) {
                    //do nothing
                }

            }
        }
        paramNames.indices
                .filter { it < paramValues.size }
                .forEach {
                    params[paramNames[it]] = if (paramNames[it] in IDS_TO_EXPAND_TILDE) {
                         APIHelper.expandTildeId(paramValues[it])
                    } else {
                        paramValues[it]
                    }
                }

        return params
    }

    /**
     * Query params for the url.
     * Example: The url /courses/1234?hello=world would have a Query params hash containing "hello" -> "world"
     *
     * @return HashMap of query params
     */
    private fun createQueryParamsHash(uri: Uri?): HashMap<String, String> {
        val queryParams = HashMap<String, String>()
        if (uri != null) {
            for (param in uri.queryParameterNames) {
                uri.getQueryParameter(param)?.let {
                    queryParams[param] = it
                }
            }
        }
        return queryParams
    }

    private fun checkQueryParamNamesExist(expectedQueryParams: List<String>, actualQueryParams: Set<String>): Boolean {
        return actualQueryParams.any { expectedQueryParams.contains(it) }
    }

    fun getContextType(): CanvasContext.Type {
        if (uri == null && canvasContext == null) return CanvasContext.Type.UNKNOWN
        if (canvasContext != null) return canvasContext!!.type

        val coursesMatcher = Pattern.compile("/courses/?").matcher(uri?.toString() ?: "")
        if (coursesMatcher.find()) return CanvasContext.Type.COURSE

        val groupsMatcher = Pattern.compile("/groups/?").matcher(uri?.toString() ?: "")
        if (groupsMatcher.find()) return CanvasContext.Type.GROUP

        val usersMatcher = Pattern.compile("/users/?").matcher(uri?.toString() ?: "")
        return if (usersMatcher.find()) CanvasContext.Type.USER else CanvasContext.Type.UNKNOWN
    }

    fun createUrl(replacementParams: HashMap<String, String>?): String {
        if (replacementParams == null) return ""

        var path = routePath ?: ""

        for (key in replacementParams.keys) {
            var keyParamIndicator = key
            if (!keyParamIndicator.startsWith(":")) {
                keyParamIndicator = ":$key"
            }
            path = path.replace(keyParamIndicator.toRegex(), replacementParams[key] ?: "")
        }

        return path
    }

    companion object {
        const val ROUTE = "route2.0"
        val IDS_TO_EXPAND_TILDE = listOf(
            RouterParams.ASSIGNMENT_ID,
            RouterParams.COURSE_ID,
            RouterParams.CONVERSATION_ID,
            RouterParams.MODULE_ITEM_ID,
            RouterParams.MODULE_ID,
            RouterParams.QUIZ_ID,
            RouterParams.MESSAGE_ID,
            RouterParams.FILE_ID,
            RouterParams.USER_ID,
            RouterParams.EVENT_ID,
            RouterParams.SUBMISSION_ID
        )

        fun extractCourseId(route: Route?): Long {
            return if (route != null && route.paramsHash.containsKey(RouterParams.COURSE_ID)) {
                route.paramsHash[RouterParams.COURSE_ID]?.toLong() ?: 0L
            } else 0L
        }

        /* [@Parcelize] uses the paremeterless [Parcel.readBundle] for Bundles which apparently doesn't work for our
           custom classes if passed across a remote procedure call (e.g. in an intent passed to [android.content.Context.startActivity]). */
        private object BundleParceler : Parceler<Bundle> {
            override fun create(parcel: Parcel): Bundle {
                return parcel.readBundle(Route::class.java.classLoader) ?: throw IllegalStateException("Bundled route does not exist")
            }

            override fun Bundle.write(parcel: Parcel, flags: Int) {
                parcel.writeBundle(this)
            }
        }
    }
}
