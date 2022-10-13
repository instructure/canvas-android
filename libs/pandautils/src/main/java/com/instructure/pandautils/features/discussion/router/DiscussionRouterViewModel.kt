package com.instructure.pandautils.features.discussion.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
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
    private val discussionRouter: DiscussionRouter,
    private val discussionManager: DiscussionManager
) : ViewModel() {

    fun route(
        canvasContext: CanvasContext,
        discussionTopicHeader: DiscussionTopicHeader?,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
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

            discussionRouter.routeToDiscussion(canvasContext, discussionRedesignEnabled, isAnnouncement, header)
        }
    }
}