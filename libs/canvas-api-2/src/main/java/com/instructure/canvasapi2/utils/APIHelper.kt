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
 *
 */

package com.instructure.canvasapi2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.text.TextUtils
import com.instructure.canvasapi2.builders.RestParams
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

object APIHelper {

    // Spelled as it should - misspelled
    val referrer: Map<String, String>
        get() {
            val extraHeaders = HashMap<String, String>()
            extraHeaders["Referer"] = ApiPrefs.domain
            return extraHeaders
        }

    // Lazy init so we don't break unit tests
    private val networkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    private fun getConnectivityManager(): ConnectivityManager {
        return ContextKeeper.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @SuppressLint("MissingPermission")
    fun hasNetworkConnection(): Boolean {
        val netInfo = getConnectivityManager().activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    @SuppressLint("MissingPermission")
    fun registerNetworkCallback(callback: ConnectivityManager.NetworkCallback) {
        getConnectivityManager().registerNetworkCallback(networkRequest, callback)
    }

    @SuppressLint("MissingPermission")
    fun unregisterNetworkCallback(callback: ConnectivityManager.NetworkCallback) {
        getConnectivityManager().unregisterNetworkCallback(callback)
    }

    /**
     * parseLinkHeaderResponse parses HTTP headers to return the first, next, prev, and last urls. Used for pagination.
     *
     * @param headers List of headers
     * @return A LinkHeaders object
     */
    fun parseLinkHeaderResponse(headers: Headers): LinkHeaders {
        val map = headers.toMultimap()

        var nextUrl: String? = null
        var prevUrl: String? = null
        var firstUrl: String? = null
        var lastUrl: String? = null
        for (name in map.keys) {
            if ("link".equals(name, ignoreCase = true)) {
                for (value in map[name]!!) {
                    val split = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (segment in split) {
                        val index = segment.indexOf(">")
                        var url: String? = segment.substring(0, index)
                        url = url!!.substring(1)

                        //Remove the domain, keep the encoding (for time zones MBL-11262)
                        url = removeDomainFromUrl(url)

                        when {
                            segment.contains("rel=\"next\"") -> nextUrl = url
                            segment.contains("rel=\"prev\"") -> prevUrl = url
                            segment.contains("rel=\"first\"") -> firstUrl = url
                            segment.contains("rel=\"last\"") -> lastUrl = url
                        }
                    }
                }
                break
            }
        }

        return LinkHeaders(prevUrl, nextUrl, lastUrl, firstUrl)
    }

    internal fun parseLinkHeaderResponse(linkField: String?): LinkHeaders {
        if (linkField.isNullOrEmpty()) {
            return LinkHeaders()
        }

        val split = linkField.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        var nextUrl: String? = null
        var prevUrl: String? = null
        var firstUrl: String? = null
        var lastUrl: String? = null
        for (segment in split) {
            val index = segment.indexOf(">")
            var url: String? = segment.substring(0, index)
            url = url!!.substring(1)

            // Remove the domain.
            url = removeDomainFromUrl(url)

            when {
                segment.contains("rel=\"next\"") -> nextUrl = url
                segment.contains("rel=\"prev\"") -> prevUrl = url
                segment.contains("rel=\"first\"") -> firstUrl = url
                segment.contains("rel=\"last\"") -> lastUrl = url
            }
        }
        return LinkHeaders(prevUrl, nextUrl, lastUrl, firstUrl)
    }

    /**
     * removeDomainFromUrl is a helper function for removing the domain from a url. Used for pagination/routing
     *
     * @param url A url
     * @return a String without a domain
     */
    fun removeDomainFromUrl(url: String?): String? = url?.substringAfter("/api/v1/")

    fun isCachedResponse(response: okhttp3.Response): Boolean = response.cacheResponse != null
    fun isCachedResponse(response: Response<*>): Boolean = isCachedResponse(response.raw())

    fun paramIsNull(vararg args: Any?): Boolean {
        if (args == null) return true
        for (arg in args) {
            if (arg == null) {
                return true
            }
        }
        return false
    }


    fun dateToString(date: GregorianCalendar?): String? {
        if (date == null) {
            return null
        }

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        format.timeZone = date.timeZone
        val formatted = format.format(Date(date.timeInMillis))
        return formatted.substring(0, 22) + ":" + formatted.substring(22)
    }

    /**
     * booleanToInt is a Helper function for Converting boolean to URL booleans (ints)
     */
    fun booleanToInt(bool: Boolean): Int {
        return if (bool) {
            1
        } else 0
    }

    /*
     * The fromHTML method can cause a character that looks like [obj]
     * to show up. This is undesired behavior most of the time.
     *
     * Replace the [obj] with an empty space
     * [obj] is char 65532 and an empty space is char 32
     * @param sequence The fromHTML typically
     * @return The modified charSequence
     */
    fun simplifyHTML(sequence: CharSequence?): String {
        if (sequence != null) {
            var toReplace: CharSequence = sequence
            toReplace = toReplace.toString().replace(65532.toChar(), 32.toChar()).trim { it <= ' ' }
            return toReplace.toString()
        }
        return ""
    }

    fun paramsWithDomain(domain: String, params: RestParams): RestParams = params.copy(domain = domain)

    fun makeRequestBody(part: String?): RequestBody = if (part == null) {
        ByteArray(0).toRequestBody("multipart/form-data".toMediaTypeOrNull(), 0, 0)
    } else {
        part.toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    fun getQuizURL(courseid: Long, quizId: Long): String {
        // https://mobiledev.instructure.com/api/v1/courses/24219/quizzes/1129998/
        return ApiPrefs.protocol + "://" + ApiPrefs.domain + "/courses/" + courseid + "/quizzes/" + quizId
    }

    /**
     * Parse an ID that references a shard, replaces "~" with the appropriate 0 padding
     * i.e., converts a sharded ID '12345~4321' to 123450000000004321
     *
     * @param id the ID to convert into a long
     */
    fun expandTildeId(id: String): String {

        return if (id.contains("~")) {
            val parts = id.split("~".toRegex())
            ((parts[0].toLong() * 10_000_000_000_000L) + parts[1].toLong()).toString()
        } else {
            id
        }
    }

    /**
     * Extract shard ID from Canvas access token if it contains one
     * Canvas tokens can be in the format: shardId~token
     *
     * @param token the access token to parse
     * @return the shard ID if present, null otherwise
     */
    fun getShardIdFromToken(token: String): String? {
        return if (token.contains("~")) {
            token.substringBefore("~")
        } else {
            null
        }
    }

    /**
     * Create a global user ID from a shard ID and user ID, then expand it
     * i.e., converts shardId "7053" and userId 2848 to 70530000000002848
     *
     * @param shardId the shard ID
     * @param userId the user ID
     * @return the expanded global user ID as a Long
     */
    fun createGlobalUserId(shardId: String, userId: Long): Long {
        val tildeId = "$shardId~$userId"
        return expandTildeId(tildeId).toLongOrNull()
            ?: throw IllegalArgumentException("Invalid tilde ID: $tildeId")
    }

    /**
     * Get the appropriate user ID for a course, converting to global user ID if the course is on a different shard
     *
     * @param courseId the course ID
     * @param userId the user ID
     * @param shardIds map of course IDs to shard IDs
     * @param accessToken the access token to extract the user's shard ID from
     * @return the user ID to use (either original or global)
     */
    fun getUserIdForCourse(
        courseId: Long,
        userId: Long,
        shardIds: Map<Long, String?>,
        accessToken: String
    ): Long {
        val courseShardId = shardIds[courseId]
        val tokenShardId = getShardIdFromToken(accessToken)

        return if (courseShardId != null && tokenShardId != null && courseShardId != tokenShardId) {
            createGlobalUserId(tokenShardId, userId)
        } else {
            userId
        }
    }
}
