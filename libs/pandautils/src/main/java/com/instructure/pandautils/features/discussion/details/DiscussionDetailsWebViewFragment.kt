/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.discussion.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.databinding.FragmentDiscussionDetailsWebViewBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_discussion_details_web_view.*

@AndroidEntryPoint
class DiscussionDetailsWebViewFragment : Fragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopic: DiscussionTopic? by NullableParcelableArg(key = DISCUSSION_TOPIC)
    private var discussionTopicHeader: DiscussionTopicHeader by ParcelableArg(default = DiscussionTopicHeader(), key = DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID)
    private var discussionTitle: String? by NullableStringArg(key = DISCUSSION_TITLE)
    private var discussionEntryId: Long by LongArg(default = 0L, key = DISCUSSION_ENTRY_ID)
    private var isNestedDetail: Boolean by BooleanArg(default = false, key = IS_NESTED_DETAIL)
    private val groupDiscussion: Boolean by BooleanArg(default = false, key = GROUP_DISCUSSION)

    private val viewModel: DiscussionDetailsWebViewViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentDiscussionDetailsWebViewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadData(canvasContext, discussionTopicHeader)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
    }

    private fun applyTheme() {
        toolbar.title = discussionTopicHeader.title
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    companion object {

        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TITLE = "discussion_title"
        const val DISCUSSION_TOPIC = "discussion_topic"
        const val DISCUSSION_ENTRY_ID = "discussion_entry_id"
        const val IS_NESTED_DETAIL = "is_nested_detail"
        const val GROUP_DISCUSSION = "group_discussion"

        private const val JS_CONST_SET_LIKED = "setLiked"
        private const val JS_CONST_SET_UNLIKED = "setUnliked"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            }

            return Route(null, DiscussionDetailsWebViewFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeaderId: Long, title: String? = null, groupDiscussion: Boolean = false): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putString(DISCUSSION_TITLE, title)
                putBoolean(GROUP_DISCUSSION, groupDiscussion)
            }
            return Route(null, DiscussionDetailsWebViewFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(
                canvasContext: CanvasContext,
                discussionTopicHeader: DiscussionTopicHeader,
                discussionTopic: DiscussionTopic,
                discussionEntryId: Long): Route {
            val bundle = Bundle().apply {
                // Used for viewing more entries, beyond the default nesting
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                putParcelable(DISCUSSION_TOPIC, discussionTopic)
                putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
                putBoolean(IS_NESTED_DETAIL, true)
            }

            return Route(null, DiscussionDetailsWebViewFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            DiscussionDetailsWebViewFragment().apply {
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