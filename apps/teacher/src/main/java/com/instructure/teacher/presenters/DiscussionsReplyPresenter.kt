/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.presenters

import android.app.Activity
import android.net.Uri
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.teacher.interfaces.RceMediaUploadPresenter
import com.instructure.teacher.viewinterface.DiscussionsReplyView
import instructure.androidblueprint.FragmentPresenter
import kotlinx.coroutines.Job
import retrofit2.Response
import java.io.File

class DiscussionsReplyPresenter(
        val canvasContext: CanvasContext,
        val discussionTopicHeaderId: Long,
        private val discussionEntryId: Long) : FragmentPresenter<DiscussionsReplyView>(), RceMediaUploadPresenter {
    override var rceImageUploadJob: WeaveJob? = null

    private var postDiscussionJob: Job? = null

    private var attachment: FileSubmitObject? = null

    override fun loadData(forceNetwork: Boolean) {}
    override fun refresh(forceNetwork: Boolean) {}

    fun sendMessage(message: String?) {
        if(postDiscussionJob?.isActive == true) {
            viewCallback?.messageFailure(REASON_MESSAGE_IN_PROGRESS)
            return
        }

        if(message == null) {
            viewCallback?.messageFailure(REASON_MESSAGE_EMPTY)
        } else {
            postDiscussionJob = tryWeave {
                if (attachment == null) {
                    if (discussionEntryId == discussionTopicHeaderId) {
                        messageSentResponse(awaitApiResponse { DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message, it) })
                    } else {
                        messageSentResponse(awaitApiResponse { DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message, it) })
                    }
                } else {
                    if (discussionEntryId == discussionTopicHeaderId) {
                        messageSentResponse(awaitApiResponse { DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message, File(attachment!!.fullPath), attachment?.contentType ?: "multipart/form-data", it) })
                    } else {
                        messageSentResponse(awaitApiResponse { DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message, File(attachment!!.fullPath), attachment?.contentType  ?: "multipart/form-data", it) })
                    }
                }
            } catch { }
        }
    }

    private fun messageSentResponse(response: Response<DiscussionEntry>) {
        if (response.code() in 200..299) {
            response.body()?.let { entry ->
                DiscussionCaching(discussionTopicHeaderId).saveEntry(entry)
                viewCallback?.messageSuccess(entry)
            }
        } else {
            viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
        }
    }

    fun setAttachment(fileSubmitObject: FileSubmitObject?) {
        attachment = fileSubmitObject
    }

    fun getAttachment(): FileSubmitObject? = attachment

    override fun uploadRceImage(imageUri: Uri, activity: Activity) {
        rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, activity) { imageUrl -> viewCallback?.insertImageIntoRCE(imageUrl) }
    }

    companion object {
        const val REASON_MESSAGE_IN_PROGRESS = 1
        const val REASON_MESSAGE_EMPTY = 2
        const val REASON_MESSAGE_FAILED_TO_SEND = 3
    }

    override fun onDestroyed() {
        super.onDestroyed()
        postDiscussionJob?.cancel()
        rceImageUploadJob?.cancel()
    }
}
