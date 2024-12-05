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
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.postmodels.DiscussionEntryPostBody
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.teacher.interfaces.RceMediaUploadPresenter
import com.instructure.teacher.viewinterface.DiscussionsUpdateView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job

class DiscussionsUpdatePresenter(
        val canvasContext: CanvasContext,
        val discussionTopicHeaderId: Long,
        val discussionEntry: DiscussionEntry,
        val discussionTopic: DiscussionTopic) : FragmentPresenter<DiscussionsUpdateView>(), RceMediaUploadPresenter {

    override var rceImageUploadJob: WeaveJob? = null
    private var updateDiscussionJob: Job? = null

    var attachmentRemoved = false

    override fun loadData(forceNetwork: Boolean) {}

    override fun refresh(forceNetwork: Boolean) {}

    fun editMessage(message: String?) {
        if (updateDiscussionJob?.isActive == true) {
            viewCallback?.messageFailure(REASON_MESSAGE_IN_PROGRESS)
            return
        }

        updateDiscussionJob = tryWeave {
            if (attachmentRemoved) discussionEntry.attachments = null

            if (message == null) {
                viewCallback?.messageFailure(REASON_MESSAGE_EMPTY)
            } else {
                val response = awaitApiResponse<DiscussionEntry> {
                    DiscussionManager.updateDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntry.id,
                            DiscussionEntryPostBody(message, discussionEntry.attachments), it)
                }

                if (response.code() in 200..299) {
                    response.body()?.let { entry ->
                        // Server doesn't send back attachments in the response for some reason
                        entry.attachments = discussionEntry.attachments
                        DiscussionCaching(discussionTopicHeaderId).saveEntry(entry)
                        viewCallback?.messageSuccess(entry)
                    }
                } else {
                    viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
                }
            }
        } catch {
            //Message update failure
            viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
        }
    }

    override fun uploadRceImage(imageUri: Uri, activity: Activity) {
        MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, activity) { imageUrl -> viewCallback?.insertImageIntoRCE(imageUrl) }
    }


    companion object {
        const val REASON_MESSAGE_IN_PROGRESS = 1
        const val REASON_MESSAGE_EMPTY = 2
        const val REASON_MESSAGE_FAILED_TO_SEND = 3
        var attachment: FileSubmitObject? = null
    }

    override fun onDestroyed() {
        super.onDestroyed()
        updateDiscussionJob?.cancel()
        rceImageUploadJob?.cancel()
    }
}
