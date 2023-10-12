package com.instructure.student.features.discussion.routing.datasource

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

class DiscussionRouteHelperStudentNetworkDataSource(
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val featureFlagProvider: FeatureFlagProvider,
): DiscussionRouteHelperStudentDataSource {

    suspend fun getEnabledFeaturesForCourse(canvasContext: CanvasContext, forceNetwork: Boolean): Boolean {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return if (canvasContext.isCourse) {
            val featureFlags = featuresApi.getEnabledFeaturesForCourse(canvasContext.id, params).dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else if (canvasContext.isGroup) {
            val featureFlags = featuresApi.getEnabledFeaturesForCourse((canvasContext as Group).courseId, params).dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else {
            false
        }
    }

    override suspend fun getDiscussionTopicHeader(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return discussionApi.getDiscussionTopicHeader(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, params).dataOrNull
    }

    override suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, forceNetwork: Boolean): Pair<Group, Long>? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        val groups = groupApi.getFirstPageGroups(params).depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }.dataOrNull

        for (group in groups ?: emptyList()) {
            val groupsMap = discussionTopicHeader.groupTopicChildren.associateBy({ it.groupId }, { it.id })
            if (groupsMap.contains(group.id) && groupsMap[group.id] != null) {
                groupsMap[group.id]?.let { topicHeaderId ->
                    return Pair(group, topicHeaderId)
                }

                return null // There is a group, but not a matching topic header id
            }
        }
        // If we made it to here, there are no groups that match this
        return null
    }
}