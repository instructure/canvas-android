/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.teacher.features.speedgrader

import android.content.res.Resources
import android.net.Uri
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.features.speedgrader.content.AnonymousSubmissionContent
import com.instructure.pandautils.features.speedgrader.content.DiscussionContent
import com.instructure.pandautils.features.speedgrader.content.ExternalToolContent
import com.instructure.pandautils.features.speedgrader.content.GradeableContent
import com.instructure.pandautils.features.speedgrader.content.ImageContent
import com.instructure.pandautils.features.speedgrader.content.MediaContent
import com.instructure.pandautils.features.speedgrader.content.NoSubmissionContent
import com.instructure.pandautils.features.speedgrader.content.NoneContent
import com.instructure.pandautils.features.speedgrader.content.OnPaperContent
import com.instructure.pandautils.features.speedgrader.content.OtherAttachmentContent
import com.instructure.pandautils.features.speedgrader.content.PdfContent
import com.instructure.pandautils.features.speedgrader.content.QuizContent
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentRoute
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentRouter
import com.instructure.pandautils.features.speedgrader.content.TextContent
import com.instructure.pandautils.features.speedgrader.content.UnsupportedContent
import com.instructure.pandautils.features.speedgrader.content.UrlContent
import com.instructure.pandautils.utils.iconRes
import com.instructure.teacher.R
import com.instructure.teacher.fragments.PdfSubmissionFragment
import com.instructure.teacher.fragments.SimpleWebViewFragment
import com.instructure.teacher.fragments.SpeedGraderEmptyFragment
import com.instructure.teacher.fragments.SpeedGraderLtiSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderQuizSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderTextSubmissionFragment
import com.instructure.teacher.fragments.SpeedGraderUrlSubmissionFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewMediaFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment

class TeacherSpeedGraderContentRouter(private val resources: Resources) : SpeedGraderContentRouter {

    override fun navigateToContent(content: GradeableContent): SpeedGraderContentRoute {
        return when (content) {
            is TextContent -> SpeedGraderContentRoute(
                SpeedGraderTextSubmissionFragment::class.java,
                SpeedGraderTextSubmissionFragment.createBundle(content.text)
            )

            is NoSubmissionContent -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java, SpeedGraderEmptyFragment.createBundle(
                    title = resources.getString(
                        R.string.noSubmission
                    ), message = resources.getString(R.string.noSubmissionTeacher)
                )
            )

            is UnsupportedContent -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java,
                SpeedGraderEmptyFragment.createBundle(
                    message = resources.getString(R.string.speedgrader_unsupported_type)
                )
            )

            is UrlContent -> SpeedGraderContentRoute(
                SpeedGraderUrlSubmissionFragment::class.java,
                SpeedGraderUrlSubmissionFragment.createBundle(content.url, content.previewUrl)
            )

            is QuizContent -> SpeedGraderContentRoute(
                SpeedGraderQuizSubmissionFragment::class.java,
                SpeedGraderQuizSubmissionFragment.createBundle(content)
            )

            is OtherAttachmentContent -> SpeedGraderContentRoute(
                ViewUnsupportedFileFragment::class.java,
                ViewUnsupportedFileFragment.createBundle(
                    Uri.parse(content.attachment.url),
                    content.attachment.displayName.orEmpty(),
                    content.attachment.contentType.orEmpty(),
                    content.attachment.thumbnailUrl?.let { Uri.parse(it) },
                    Attachment(
                        id = content.attachment.id,
                        contentType = content.attachment.contentType,
                        createdAt = content.attachment.createdAt,
                        displayName = content.attachment.displayName,
                        thumbnailUrl = content.attachment.thumbnailUrl,
                        url = content.attachment.url
                    ).iconRes
                )
            )

            is MediaContent -> SpeedGraderContentRoute(
                ViewMediaFragment::class.java,
                ViewMediaFragment.createBundle(content)
            )

            is ImageContent -> SpeedGraderContentRoute(
                ViewImageFragment::class.java,
                ViewImageFragment.createBundle(
                    title = content.url,
                    url = content.url,
                    contentType = content.contentType,
                    showToolbar = false,
                )
            )

            is NoneContent -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java,
                SpeedGraderEmptyFragment.createBundle(
                    message = resources.getString(R.string.speedGraderNoneMessage)
                )
            )

            is ExternalToolContent -> SpeedGraderContentRoute(
                SpeedGraderLtiSubmissionFragment::class.java,
                SpeedGraderLtiSubmissionFragment.createBundle(content)
            )

            is OnPaperContent -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java,
                SpeedGraderEmptyFragment.createBundle(
                    message = resources.getString(R.string.speedGraderOnPaperMessage)
                )
            )

            is DiscussionContent -> SpeedGraderContentRoute(
                SimpleWebViewFragment::class.java,
                SimpleWebViewFragment.createBundle(content.previewUrl.orEmpty())
            )

            is AnonymousSubmissionContent -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java,
                SpeedGraderEmptyFragment.createBundle(
                    message = resources.getString(R.string.speedGraderAnonymousSubmissionMessage)
                )
            )

            is PdfContent -> SpeedGraderContentRoute(
                PdfSubmissionFragment::class.java,
                PdfSubmissionFragment.createBundle(
                    url = content.url,
                    courseId = content.courseId ?: 0L,
                    assigneeId = content.assigneeId ?: 0L
                )
            )

            else -> SpeedGraderContentRoute(
                SpeedGraderEmptyFragment::class.java,
                SpeedGraderEmptyFragment.createBundle(
                    message = resources.getString(R.string.speedgrader_unsupported_type)
                )
            )
        }
    }
}

