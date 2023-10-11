package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.orDefault

class DiscussionRouteHelper(
    private val featuresManager: FeaturesManager,
    private val featureFlagProvider: FeatureFlagProvider,
    private val discussionManager: DiscussionManager,
    private val groupManager: GroupManager,
    private val groupFacade: GroupFacade,
    private val networkStateProvider: NetworkStateProvider,
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
) {

    suspend fun isDiscussionRedesignEnabled(canvasContext: CanvasContext): Boolean {
        return if (canvasContext.isCourse) {
            val featureFlags =
                featuresManager.getEnabledFeaturesForCourseAsync(canvasContext.id, false).await().dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else if (canvasContext.isGroup) {
            val featureFlags =
                featuresManager.getEnabledFeaturesForCourseAsync((canvasContext as Group).courseId, false)
                    .await().dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else {
            false
        }
    }

    suspend fun getDiscussionHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long
    ): DiscussionTopicHeader? {
       return if (networkStateProvider.isOnline()) {
           discussionManager.getDiscussionTopicHeaderAsync(canvasContext, discussionTopicHeaderId, false)
               .await().dataOrNull
        }
        else {
              discussionTopicHeaderFacade.getDiscussionTopicHeaderById(discussionTopicHeaderId)
          }
       }

    suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader): Pair<Group, Long>? {
        val groups = if (networkStateProvider.isOnline()) {
            groupManager.getAllGroupsAsync(false).await().dataOrNull
        }
        else {
            groupFacade.getGroupsByUserId(ApiPrefs.user?.id.orDefault())
        }
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