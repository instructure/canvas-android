package com.instructure.pandautils.features.discussion.router

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscussionRouterViewModel @Inject constructor(
    private val discussionRouteHelper: DiscussionRouteHelper,
    private val resources: Resources,
) : ViewModel() {

    val events: LiveData<Event<DiscussionRouterAction>>
        get() = _events
    private val _events = MutableLiveData<Event<DiscussionRouterAction>>()

    fun route(
        canvasContext: CanvasContext,
        discussionTopicHeader: DiscussionTopicHeader?,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        viewModelScope.launch {
            try {
                val discussionRedesignEnabled = discussionRouteHelper.isDiscussionRedesignEnabled(canvasContext)
                val header = discussionTopicHeader ?: discussionRouteHelper.getDiscussionHeader(
                    canvasContext,
                    discussionTopicHeaderId
                )!!

                if (header.groupTopicChildren.isNotEmpty()) {
                    val discussionGroup = discussionRouteHelper.getDiscussionGroup(header)
                    discussionGroup?.let {
                        val groupDiscussionHeader = discussionRouteHelper.getDiscussionHeader(it.first, it.second)!!
                        routeToDiscussionGroup(
                            it.first,
                            it.second,
                            groupDiscussionHeader,
                            discussionRedesignEnabled
                        )
                    } ?: routeToDiscussion(canvasContext, header, discussionRedesignEnabled, isAnnouncement)
                } else {
                    routeToDiscussion(canvasContext, header, discussionRedesignEnabled, isAnnouncement)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _events.postValue(Event(DiscussionRouterAction.ShowToast(resources.getString(R.string.discussionErrorToast))))
            }
        }
    }

    private fun routeToDiscussionGroup(
        group: Group,
        discussionTopicHeaderId: Long,
        discussionTopicHeader: DiscussionTopicHeader,
        isRedesignEnabled: Boolean
    ) {
        _events.postValue(
            Event(
                DiscussionRouterAction.RouteToGroupDiscussion(
                    group,
                    discussionTopicHeaderId,
                    discussionTopicHeader,
                    isRedesignEnabled
                )
            )
        )
    }

    private fun routeToDiscussion(
        canvasContext: CanvasContext,
        header: DiscussionTopicHeader,
        discussionRedesignEnabled: Boolean,
        isAnnouncement: Boolean
    ) {
        _events.postValue(
            Event(
                DiscussionRouterAction.RouteToDiscussion(
                    canvasContext,
                    discussionRedesignEnabled,
                    header,
                    isAnnouncement
                )
            )
        )
    }
}