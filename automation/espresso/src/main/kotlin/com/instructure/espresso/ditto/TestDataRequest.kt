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
import java.net.URLEncoder

/**
 * Represents an internal request for arbitrary String data. This allows for mocking of non-network data that may be
 * unique to a particular test recording (e.g. seeded data).
 *
 * @param requestName A short description of the request, e.g. "random course name". This should be distinct from the
 * request names of all other [TestDataRequest]s in the same test, otherwise it may be matched with an incorrect response.
 */
class TestDataRequest(requestName: String) : Request by RecordedRequest.Builder()
    .method("GET", null)
    .url("https://com.instructure.testing/${URLEncoder.encode(requestName, "UTF-8")}")
    .build()

/**
 * Represents an internal response for arbitrary String data. This allows for mocking of non-network data that may be
 * unique to a particular test recording (e.g. seeded data).
 *
 * @param body The String data to be recorded.
 */
class TestDataResponse(body: String) : Response by RecordedResponse.Builder()
    .code(201)
    .body(ResponseBody.create(MediaType.parse("text/plain"), body))
    .build()
