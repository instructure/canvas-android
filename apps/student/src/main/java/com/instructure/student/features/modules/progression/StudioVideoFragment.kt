/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.features.modules.progression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.router.Route
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.color
import com.instructure.student.activity.VideoViewActivity
import com.instructure.student.fragment.ParentFragment
import dagger.hilt.android.AndroidEntryPoint

private const val COURSE_ID = "course_id"
private const val VIDEO_URI = "video_uri"
private const val VIDEO_TITLE = "video_title"
private const val POSTER_URI = "poster_uri"

@PageView
@AndroidEntryPoint
class StudioVideoFragment : ParentFragment() {

    private var courseId: Long by LongArg(key = COURSE_ID)
    private var videoUri: String by StringArg(key = VIDEO_URI)
    private var videoTitle: String by StringArg(key = VIDEO_TITLE)
    private var posterUri: String? by NullableStringArg(key = POSTER_URI)

    @PageViewUrl
    fun makePageViewUrl() = videoUri

    override fun title(): String = videoTitle

    override fun applyTheme() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    val courseColor = Color(CanvasContext.emptyCourseContext(courseId).color)
                    StudioVideoScreen(
                        title = videoTitle,
                        posterUri = posterUri,
                        courseColor = courseColor,
                        onOpenClick = {
                            startActivity(VideoViewActivity.createIntent(requireContext(), videoUri))
                        },
                        onBackClick = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun makeRoute(courseId: Long, videoUri: String, title: String, posterUri: String?): Route {
            val bundle = Bundle().apply {
                putLong(COURSE_ID, courseId)
                putString(VIDEO_URI, videoUri)
                putString(VIDEO_TITLE, title)
                putString(POSTER_URI, posterUri)
            }
            return Route(StudioVideoFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route) = StudioVideoFragment().apply {
            arguments = route.arguments
        }
    }
}
