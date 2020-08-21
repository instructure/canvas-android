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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.SectionAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object SectionManager {

    fun getAllSectionsForCourse(courseId: Long, callback: StatusCallback<List<Section>>, forceNetwork: Boolean) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Section>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Section>>, nextUrl: String, isCached: Boolean) {
                SectionAPI.getNextPageSections(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        SectionAPI.getFirstSectionsForCourse(courseId, adapter, depaginatedCallback, params)
    }

    fun getAllSectionsForCourseAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<List<Section>> {
        getAllSectionsForCourse(courseId, it, forceNetwork)
    }

    fun getSection(courseId: Long, sectionId: Long, callback: StatusCallback<Section>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        SectionAPI.getSection(courseId, sectionId, adapter, callback, params)
    }

}
