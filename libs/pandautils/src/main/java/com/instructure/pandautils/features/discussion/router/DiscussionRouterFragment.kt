package com.instructure.pandautils.features.discussion.router

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.databinding.FragmentDiscussionRouterBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscussionRouterFragment : BaseCanvasFragment() {

    @Inject
    lateinit var discussionRouter: DiscussionRouter

    private val viewModel: DiscussionRouterViewModel by viewModels()

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopicHeader: DiscussionTopicHeader? by NullableParcelableArg(key = DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(
        default = 0L,
        key = DISCUSSION_TOPIC_HEADER_ID
    )
    private var isAnnouncement by BooleanArg(key = DISCUSSION_ANNOUNCEMENT, default = false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDiscussionRouterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.route(canvasContext, discussionTopicHeader, discussionTopicHeaderId, isAnnouncement)

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: DiscussionRouterAction) {
        when (action) {
            is DiscussionRouterAction.RouteToDiscussion -> {
                discussionRouter.routeToDiscussion(
                    action.canvasContext,
                    action.isRedesignEnabled,
                    action.discussionTopicHeader,
                    action.isAnnouncement
                )
            }
            is DiscussionRouterAction.RouteToGroupDiscussion -> {
                discussionRouter.routeToGroupDiscussion(action.group, action.id, action.header, action.isRedesignEnabled)
            }
            is DiscussionRouterAction.ShowToast -> {
                toast(action.toast, Toast.LENGTH_SHORT)
                Handler(Looper.getMainLooper()).post { requireActivity().onBackPressed() }
            }
        }
    }

    companion object {

        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TOPIC = "discussion_topic"
        const val DISCUSSION_ANNOUNCEMENT = "isAnnouncement"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader, isAnnouncement: Boolean = false): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                putBoolean(DISCUSSION_ANNOUNCEMENT, isAnnouncement)
            }

            return Route(null, DiscussionRouterFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeaderId: Long, isAnnouncement: Boolean = false): Route {
            val bundle = Bundle().apply {
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putBoolean(DISCUSSION_ANNOUNCEMENT, isAnnouncement)
            }

            return Route(null, DiscussionRouterFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(canvasContext: CanvasContext, route: Route) = if (validRoute(route)) {
            DiscussionRouterFragment().apply {
                arguments = canvasContext.makeBundle(route.arguments)

                // For routing
                if (route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
                    discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong() ?: 0L
            }
        } else null

        fun validRoute(route: Route) = route.arguments.containsKey(DISCUSSION_TOPIC_HEADER) ||
                route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID) ||
                route.paramsHash.containsKey(RouterParams.MESSAGE_ID)
    }
}