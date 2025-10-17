/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.DifferentiationTagsQuery
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager

class FakeDifferentiationTagsManager(
    private val tags: List<DifferentiationTagsQuery.Group> = emptyList(),
    private val groupSetName: String? = null
) : DifferentiationTagsManager {
    override suspend fun getDifferentiationTags(
        courseId: Long,
        forceNetwork: Boolean
    ): DifferentiationTagsQuery.Data? {
        if (tags.isEmpty()) {
            return null
        }

        // Use the first tag's name as the group set name if not specified
        // This ensures no subtitle appears when group set name == group name
        val setName = groupSetName ?: tags.firstOrNull()?.name ?: "Test Group Set"

        return DifferentiationTagsQuery.Data(
            course = DifferentiationTagsQuery.Course(
                groupSets = listOf(
                    DifferentiationTagsQuery.GroupSet(
                        _id = "1",
                        name = setName,
                        nonCollaborative = true,
                        groups = tags
                    )
                )
            )
        )
    }
}