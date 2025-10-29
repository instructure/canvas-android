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
                val shouldShowDiscussionRedesign = discussionRouteHelper.shouldShowDiscussionRedesign()
                val header = discussionTopicHeader ?: discussionRouteHelper.getDiscussionHeader(
                    canvasContext,
                    discussionTopicHeaderId
                )

                if (header == null) {
                    // Unable to fetch discussion header (e.g., 404 for anonymous discussions)
                    if (shouldShowDiscussionRedesign) {
                        // Fallback to web view for redesigned discussions
                        routeToDiscussionWebView(canvasContext, discussionTopicHeaderId, isAnnouncement)
                    } else {
                        // Show error for non-redesigned discussions
                        _events.postValue(Event(DiscussionRouterAction.ShowToast(resources.getString(R.string.discussionErrorToast))))
                    }
                    return@launch
                }

                if (header.groupTopicChildren.isNotEmpty()) {
                    val discussionGroup = discussionRouteHelper.getDiscussionGroup(header)
                    discussionGroup?.let {
                        val groupDiscussionHeader = discussionRouteHelper.getDiscussionHeader(it.first, it.second)
                        if (groupDiscussionHeader != null) {
                            routeToDiscussionGroup(
                                it.first,
                                it.second,
                                groupDiscussionHeader,
                                shouldShowDiscussionRedesign
                            )
                        } else {
                            routeToDiscussion(canvasContext, header, shouldShowDiscussionRedesign, isAnnouncement)
                        }
                    } ?: routeToDiscussion(canvasContext, header, shouldShowDiscussionRedesign, isAnnouncement)
                } else {
                    routeToDiscussion(canvasContext, header, shouldShowDiscussionRedesign, isAnnouncement)
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
        shoudShowDiscussionRedesign: Boolean
    ) {
        _events.postValue(
            Event(
                DiscussionRouterAction.RouteToGroupDiscussion(
                    group,
                    discussionTopicHeaderId,
                    discussionTopicHeader,
                    shoudShowDiscussionRedesign
                )
            )
        )
    }

    private fun routeToDiscussion(
        canvasContext: CanvasContext,
        header: DiscussionTopicHeader,
        shouldShowDiscussionRedesign: Boolean,
        isAnnouncement: Boolean
    ) {
        _events.postValue(
            Event(
                DiscussionRouterAction.RouteToDiscussion(
                    canvasContext,
                    shouldShowDiscussionRedesign,
                    header,
                    isAnnouncement
                )
            )
        )
    }

    private fun routeToDiscussionWebView(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        _events.postValue(
            Event(
                DiscussionRouterAction.RouteToDiscussionWebView(
                    canvasContext,
                    discussionTopicHeaderId
                )
            )
        )
    }
}