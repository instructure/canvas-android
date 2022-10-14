package com.instructure.pandautils.features.discussion.router

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscussionRouterViewModel @Inject constructor(
    private val featuresManager: FeaturesManager,
    private val featureFlagProvider: FeatureFlagProvider,
    private val discussionManager: DiscussionManager,
    private val groupManager: GroupManager
) : ViewModel() {

    val events: LiveData<Event<DiscussionRouterAction>>
        get() = _events
    private val _events = MutableLiveData<Event<DiscussionRouterAction>>()

    fun route(
        canvasContext: CanvasContext,
        discussionTopicHeader: DiscussionTopicHeader?,
        discussionTopicHeaderId: Long
    ) {
        viewModelScope.launch {
            val discussionRedesignEnabled = if (canvasContext.isCourse) {
                val featureFlags =
                    featuresManager.getEnabledFeaturesForCourseAsync(canvasContext.id, true).await().dataOrNull
                featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
            } else if (canvasContext.isGroup) {
                val featureFlags =
                    featuresManager.getEnabledFeaturesForCourseAsync((canvasContext as Group).courseId, true)
                        .await().dataOrNull
                featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
            } else {
                false
            }

            val header: DiscussionTopicHeader = discussionTopicHeader
                ?: discussionManager.getDiscussionTopicHeaderAsync(canvasContext, discussionTopicHeaderId, false).await().dataOrThrow

            if (header.groupTopicChildren.isNotEmpty()) {
                val discussionGroup = getDiscussionGroup(header)
                discussionGroup?.let {
                    val groupDiscussionHeader = discussionManager.getDiscussionTopicHeaderAsync(it.first, it.second, false).await().dataOrThrow
                    routeToDiscussionGroup(it.first, it.second, groupDiscussionHeader, discussionRedesignEnabled)
                } ?: routeToDiscussion(canvasContext, header, discussionRedesignEnabled)
            } else {
                routeToDiscussion(canvasContext, header, discussionRedesignEnabled)
            }
        }
    }

    private fun routeToDiscussionGroup(group: Group, discussionTopicHeaderId: Long, discussionTopicHeader: DiscussionTopicHeader, isRedesignEnabled: Boolean) {
        _events.postValue(Event(DiscussionRouterAction.RouteToGroupDiscussion(group, discussionTopicHeaderId, discussionTopicHeader, isRedesignEnabled)))
    }

    private fun routeToDiscussion(canvasContext: CanvasContext, header: DiscussionTopicHeader, discussionRedesignEnabled: Boolean) {
        _events.postValue(Event(DiscussionRouterAction.RouteToDiscussion(canvasContext, discussionRedesignEnabled, header)))
    }

    private suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader): Pair<Group, Long>? {
        val groups = awaitApi<List<Group>> {
            groupManager.getAllGroups(it, false)
        }
        for (group in groups) {
            val groupsMap = discussionTopicHeader.groupTopicChildren.associateBy({it.groupId}, {it.id})
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