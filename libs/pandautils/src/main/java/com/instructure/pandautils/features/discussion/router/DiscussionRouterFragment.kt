package com.instructure.pandautils.features.discussion.router

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.databinding.FragmentDiscussionRouterBinding
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.makeBundle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscussionRouterFragment : Fragment() {

    private val viewModel: DiscussionRouterViewModel by viewModels()

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopicHeader: DiscussionTopicHeader by ParcelableArg(default = DiscussionTopicHeader(), key = DiscussionDetailsWebViewFragment.DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DiscussionDetailsWebViewFragment.DISCUSSION_TOPIC_HEADER_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDiscussionRouterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    companion object {

        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TOPIC = "discussion_topic"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            }

            return Route(null, DiscussionRouterFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            DiscussionRouterFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)

                // For routing
                if (route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
                    discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong() ?: 0L
            }
        } else null

        fun validRoute(route: Route) = route.canvasContext != null &&
                (route.arguments.containsKey(DISCUSSION_TOPIC_HEADER) ||
                        route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID) ||
                        route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
    }
}