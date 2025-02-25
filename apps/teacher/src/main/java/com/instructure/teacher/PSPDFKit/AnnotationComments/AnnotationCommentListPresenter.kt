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
package com.instructure.teacher.PSPDFKit.AnnotationComments

import com.instructure.annotations.*
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.view.*
import com.instructure.pandautils.blueprint.ListPresenter
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus

class AnnotationCommentListPresenter(val annotations: ArrayList<CanvaDocAnnotation>,
                                     val docSession: DocSession,
                                     val apiValues: ApiValues,
                                     val assigneeId: Long,
                                     val headAnnotationId: String) : ListPresenter<CanvaDocAnnotation, AnnotationCommentListView>(CanvaDocAnnotation::class.java) {

    private var mSendCommentJob: Job? = null
    private var mEditCommentJob: Job? = null
    private var mDeleteCommentJob: Job? = null

    override fun getItemId(item: CanvaDocAnnotation): Long {
        return item.annotationId.hashCode().toLong()
    }

    override fun loadData(forceNetwork: Boolean) {
        //this prevents deleted items from reloading on rotation
        if(data.size() == 0) {
            data.addAll(annotations)
        }
    }

    override fun refresh(forceNetwork: Boolean) {}

    override fun onDestroyed() {
        mSendCommentJob?.cancel()
        mEditCommentJob?.cancel()
        mDeleteCommentJob?.cancel()
        EventBus.getDefault().post(
                AnnotationCommentDeleteAcknowledged(
                        annotations.filter { it.deleted && it.deleteAcknowledged.isNullOrEmpty() },
                        assigneeId))
        super.onDestroyed()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun sendComment(comment: String) {
        mSendCommentJob = weave {
            try {
                viewCallback?.showSendingStatus()
                //first we need to find the root comment
                val rootComment = data[0]
                if (rootComment != null) {
                    val newCommentReply = awaitApi<CanvaDocAnnotation> { CanvaDocsManager.putAnnotation(apiValues.sessionId, generateAnnotationId(), createCommentReplyAnnotation(comment, headAnnotationId, apiValues.documentId, ApiPrefs.user?.id.toString(), rootComment.page), apiValues.canvaDocsDomain, it) }
                    EventBus.getDefault().post(AnnotationCommentAdded(newCommentReply, assigneeId))

                    // The put request doesn't return this property, so we need to set it to true
                    newCommentReply.isEditable = true
                    data.add(newCommentReply) //ALSO, add it to the UI
                    viewCallback?.hideSendingStatus(true)
                } else {
                    viewCallback?.hideSendingStatus(false)
                }
            } catch (e: Throwable) {
                viewCallback?.hideSendingStatus(false)
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun editComment(annotation: CanvaDocAnnotation, position: Int) {
        mEditCommentJob = tryWeave {
            awaitApi<CanvaDocAnnotation> { CanvaDocsManager.putAnnotation(apiValues.sessionId, annotation.annotationId, annotation, apiValues.canvaDocsDomain, it) }
            EventBus.getDefault().post(AnnotationCommentEdited(annotation, assigneeId))

            //ALSO, add it to the UI
            data.addOrUpdate(annotation)
            viewCallback?.notifyItemChanged(position)
        } catch {
            viewCallback?.hideSendingStatus(false)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun deleteComment(annotation: CanvaDocAnnotation, position: Int) {
        mDeleteCommentJob = tryWeave {
            awaitApi<ResponseBody> { CanvaDocsManager.deleteAnnotation(apiValues.sessionId, annotation.annotationId, apiValues.canvaDocsDomain, it) }
            if(annotation.annotationId == data[0]?.annotationId) {
                //this is the root comment, deleting this deletes the entire thread
                EventBus.getDefault().post(AnnotationCommentDeleted(annotation, true, assigneeId))
                viewCallback?.headAnnotationDeleted()
            } else {
                EventBus.getDefault().post(AnnotationCommentDeleted(annotation, false, assigneeId))
                data.remove(annotation)
                viewCallback?.notifyItemChanged(position)
            }
        } catch {
            viewCallback?.hideSendingStatus(false)
        }
    }

    override fun compare(item1: CanvaDocAnnotation, item2: CanvaDocAnnotation): Int {
        val createdAt1 = item1.createdAt
        val createdAt2 = item2.createdAt

        return if(createdAt1 != null && createdAt2 != null) {
            createdAt1.compareTo(createdAt2)
        } else if(createdAt1 != null && createdAt2 == null) {
            1
        } else {
            -1
        }
    }
}
