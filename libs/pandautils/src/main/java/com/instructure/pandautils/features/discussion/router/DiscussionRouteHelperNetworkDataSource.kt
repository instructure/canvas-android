/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup

class DiscussionRouteHelperNetworkDataSource(
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val featureFlagProvider: FeatureFlagProvider
) : DiscussionRouteHelperDataSource {
    suspend fun getEnabledFeaturesForCourse(canvasContext: CanvasContext, forceNetwork: Boolean): Boolean {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return if (canvasContext.isCourse) {
            val featureFlags = featuresApi.getEnabledFeaturesForCourse(canvasContext.id, params).dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false
        } else if (canvasContext.isGroup) {
            val group = canvasContext as Group
            if (group.courseId == 0L) {
                featureFlagProvider.getDiscussionRedesignFeatureFlag()
            } else {
                val featureFlags = featuresApi.getEnabledFeaturesForCourse(group.courseId, params).dataOrNull
                featureFlags?.contains("react_discussions_post") ?: false
            }
        } else {
            false
        }
    }

    override suspend fun getDiscussionTopicHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return discussionApi.getDiscussionTopicHeader(
            canvasContext.apiContext(),
            canvasContext.id,
            discussionTopicHeaderId,
            params
        ).dataOrNull
    }

    override suspend fun getAllGroups(
        discussionTopicHeader: DiscussionTopicHeader,
        userId: Long,
        forceNetwork: Boolean
    ): List<Group> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return groupApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }.dataOrNull.orEmpty()
    }
}