/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("PackageDirectoryMismatch")

package okreplay

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*

/**
 * A class for performing modifications to the response of a request whose URL match the provided [matchRegex].
 *
 * @param matchRegex A [Regex] for matching request URLs
 */
abstract class DittoResponseMod(private val matchRegex: Regex) {

    fun matches(request: Request) = matchRegex.matches(request.url().toString())

    abstract fun modifyResponse(response: Response): Response

    internal fun Response.withBody(bodyString: String) = withBody(bodyString.toByteArray())

    internal fun Response.withBody(bodyBytes: ByteArray): Response {
        val newBody = ResponseBody.create(MediaType.parse(contentType), bodyBytes)
        return newBuilder().body(newBody).build()
    }

}

/**
 * Specifies a new String value to replace the existing value for a specific json key.
 *
 * @param keyPath The path to the json key. Nested elements are separated by a colon ":", and array elements are
 * specified inside square brackets "[]".
 *
 * For example, "name" is the path to the _name_ element in the following JSON:
 * ```
 * {
 *   "name": "John Wick"
 *   "email": "jwick@hotmail.com"
 * }
 * ```
 *
 * If nested, the path to _name_ becomes "user:name":
 *
 * ```
 * {
 *   "user": {
 *      "name": "John Wick"
 *      "email": "jwick@hotmail.com"
 *   }
 * }
 * ```
 *
 * With arrays, the path to _name_ in this case is "users[0]:name":
 * ```
 * {
 *   "users": [
 *      {
 *        "name": "John Wick"
 *        "email": "jwick@hotmail.com"
 *      }
 *    ]
 * }
 * ```
 *
 */
data class JsonObjectValueMod(val keyPath: String, val newValue: String)

/**
 * A concrete implementation of [DittoResponseMod] that works with JSON object responses.
 *
 * @param matchRegex A [Regex] for matching request URLs
 * @param mods One or more [JsonObjectValueMod]s for performing the actual modifications
 */
class JsonObjectResponseMod(
    matchRegex: Regex,
    private vararg val mods: JsonObjectValueMod
) : DittoResponseMod(matchRegex) {

    override fun modifyResponse(response: Response): Response {
        val text = response.bodyAsText()
        val jsonRoot = JSONTokener(text).nextValue()
        val json = jsonRoot as? JSONObject
                ?: throw InputMismatchException("Expected an object for json root but got ${jsonRoot?.javaClass?.name}")
        for (mod in mods) {
            var node = json
            val pathSegments = mod.keyPath.split(":")
            pathSegments.forEachIndexed { idx, segment ->
                if (idx == pathSegments.lastIndex) {
                    node.put(segment, mod.newValue)
                } else {
                    node = if (arrayRegex.matches(segment)) {
                        val (key, index) = arrayRegex.find(segment)!!.destructured
                        node.getJSONArray(key).getJSONObject(index.toInt())
                    } else {
                        node.getJSONObject(segment)
                    }
                }
            }
        }
        return response.withBody(json.toString())
    }

    companion object {
        private val arrayRegex = Regex("""(.*)\[(\d+)\]""")
    }

}

