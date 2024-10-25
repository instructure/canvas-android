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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*


class APIHelperTest {
    @Test
    @Throws(Exception::class)
    fun parseLinkHeaderResponse() {
        val headers = Headers.Builder().add("link: <https://mobiledev.instructure.com/api/v1/courses/123456/discussion_topics.json?page2>; rel=\"next\"").build()

        val linkHeaders = APIHelper.parseLinkHeaderResponse(headers)

        assertEquals(linkHeaders.nextUrl, "courses/123456/discussion_topics.json?page2")
    }

    @Test
    @Throws(Exception::class)
    fun removeDomainFromUrl() {
        val url = "https://mobiledev.instructure.com/api/v1/courses/833052/external_tools?include_parents=true"
        val urlNoDomain = "courses/833052/external_tools?include_parents=true"
        assertEquals(urlNoDomain, APIHelper.removeDomainFromUrl(url))
    }

    @Test
    @Throws(Exception::class)
    fun isCachedResponse() {
        val cacheResponse = okhttp3.Response.Builder() //
                .code(200)
                .message("")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build()

        val response = okhttp3.Response.Builder() //
                .code(200)
                .message("")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .cacheResponse(cacheResponse)
                .build()

        assertEquals(true, APIHelper.isCachedResponse(response))

    }

    @Test
    @Throws(Exception::class)
    fun paramIsNull_multiple() {
        assertEquals(true, APIHelper.paramIsNull("", null))
    }

    @Test
    @Throws(Exception::class)
    fun paramIsNull_noNulls() {
        assertEquals(false, APIHelper.paramIsNull("", 17, ArrayList<Any>()))
    }

    @Test
    @Throws(Exception::class)
    fun stringToDate() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        //clear out milliseconds because we're not displaying that in the simple date format
        calendar.set(Calendar.MILLISECOND, 0)

        val date = calendar.time
        val nowAsString = df.format(date)

        //add a 'Z' at the end. Dates have a Z at the end of the string ("2037-07-28T19:38:31Z")
        //so we parse that out in the function
        assertEquals(date, nowAsString.toDate())
    }

    @Test
    @Throws(Exception::class)
    fun dateToString_date() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-6"))

        calendar.set(Calendar.YEAR, 2037)
        calendar.set(Calendar.MONTH, 6)
        calendar.set(Calendar.DAY_OF_MONTH, 28)
        calendar.set(Calendar.HOUR_OF_DAY, 19)
        calendar.set(Calendar.MINUTE, 38)
        calendar.set(Calendar.SECOND, 31)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateString = "2037-07-28T19:38:31-06:00"
        val date = calendar.time
        assertEquals(dateString, date.toApiString(calendar.timeZone))
    }

    @Test
    @Throws(Exception::class)
    fun dateToString_dateNull() {
        assertEquals(null, APIHelper.dateToString(null))
    }

    @Test
    @Throws(Exception::class)
    fun dateToString_gregorianCalendar() {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("GMT-6"))
        calendar.set(Calendar.YEAR, 2027)
        calendar.set(Calendar.MONTH, 8)
        calendar.set(Calendar.DAY_OF_MONTH, 28)
        calendar.set(Calendar.HOUR_OF_DAY, 19)
        calendar.set(Calendar.MINUTE, 33)
        calendar.set(Calendar.SECOND, 31)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateString = "2027-09-28T19:33:31-06:00"

        assertEquals(dateString, APIHelper.dateToString(calendar))
    }

    @Test
    @Throws(Exception::class)
    fun dateToString_gregorianCalendarNull() {
        assertEquals(null, APIHelper.dateToString(null as GregorianCalendar?))
    }

    @Test
    @Throws(Exception::class)
    fun booleanToInt_true() {
        assertEquals(1, APIHelper.booleanToInt(true))
    }

    @Test
    @Throws(Exception::class)
    fun booleanToInt_false() {
        assertEquals(0, APIHelper.booleanToInt(false))
    }

    @Test
    @Throws(Exception::class)
    fun simplifyHTML() {
        val builder = StringBuilder()
        val sampleText = "Here is some sample text"
        builder.append(sampleText)
        builder.append(65532.toChar())
        builder.append(32.toChar())

        assertEquals(sampleText, APIHelper.simplifyHTML(builder.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun paramsWithDomain() {
        val domain = "www.domain.com"
        val params = RestParams(apiVersion = "")

        assertNotNull(APIHelper.paramsWithDomain(domain, params))
    }

    @Test
    fun expandTildeId() {
        val expected = "123450000000001234"
        val actual = "12345~1234"

        assertEquals(expected, APIHelper.expandTildeId(actual))
    }

    @Test
    fun expandTildeIdLongerId() {
        val expected = "107920000001139989"
        val actual = "10792~1139989"

        assertEquals(expected, APIHelper.expandTildeId(actual))
    }

    @Test
    fun expandTildeIdShortShard() {
        val expected = "30000012771174"
        val actual = "3~12771174"

        assertEquals(expected, APIHelper.expandTildeId(actual))
    }
}
