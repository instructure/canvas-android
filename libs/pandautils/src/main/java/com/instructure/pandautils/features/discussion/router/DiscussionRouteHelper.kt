package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.orDefault

class DiscussionRouteHelper(
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository,
) {

    suspend fun isDiscussionRedesignEnabled(canvasContext: CanvasContext): Boolean {
        return discussionRouteHelperRepository.getEnabledFeaturesForCourse(canvasContext, false)
        return if (canvasContext.isCourse) {
            val featureFlags =
                featuresManager.getEnabledFeaturesForCourseAsync(canvasContext.id, false).await().dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else if (canvasContext.isGroup) {
            val group = canvasContext as Group
            val discussionFeatureFlagEnabled = if (group.courseId == 0L) {
                val featureFlags = featuresManager.getEnvironmentFeatureFlagsAsync(false).await().dataOrNull
                featureFlags?.get("react_discussions_post") == true
            } else {
                featuresManager.getEnabledFeaturesForCourseAsync(group.courseId, false).await().dataOrNull?.contains("react_discussions_post") == true
            }
            discussionFeatureFlagEnabled && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else {
            false
        }
    }

    suspend fun getDiscussionHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long
    ): DiscussionTopicHeader? {
       return discussionRouteHelperRepository.getDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, false)
       }

    suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader, user: User? = null): Pair<Group, Long>? {
        val userId = user?.id ?: ApiPrefs.user?.id.orDefault()
        val groups = discussionRouteHelperRepository.getAllGroups(discussionTopicHeader, userId, false)
        for (group in groups) {
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