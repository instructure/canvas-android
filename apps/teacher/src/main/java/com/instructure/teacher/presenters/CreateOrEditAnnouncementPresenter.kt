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
import com.instructure.canvasapi2.apis.SectionAPI
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.teacher.events.DiscussionCreatedEvent
import com.instructure.teacher.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.interfaces.RceMediaUploadPresenter
import com.instructure.teacher.viewinterface.CreateOrEditAnnouncementView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreateOrEditAnnouncementPresenter(
        private val canvasContext: CanvasContext,
        editAnnouncement: DiscussionTopicHeader? = null
) : FragmentPresenter<CreateOrEditAnnouncementView>(), RceMediaUploadPresenter {
    override var rceImageUploadJob: WeaveJob? = null

    private var apiJob: Job? = null
    private var sectionsJob: Job? = null

    /** *True* for editing mode, *false* for creation mode */
    var isEditing = editAnnouncement != null

    var courseSections: List<Section> = listOf()

    /**
     * The announcement that is being edited/created. Changes should be applied directly to this
     * object. For editing mode this object should be passed to the constructor as a deep copy of
     * the original so that canceled changes are not erroneously propagated back to other pages. In
     * creation mode this object will be generated with the values necessary to distinguish it as
     * an announcement instead of a normal discussion topic header.
     *
     * We get a list of 'Section' objects, but send a list of Section ids as a comma separated list of Strings (when updating or creating).
     * We use this comma separated list as our local copy to edit and then ultimately send up to the server
     */
    val announcement: DiscussionTopicHeader = editAnnouncement.apply { this?.specificSections = this?.sections?.map { it.id }?.joinToString(",") ?: "all" } // Set initial sections for this announcement
            ?: DiscussionTopicHeader().apply {
                announcement = true
                published = true
                locked = true
                type = DiscussionTopicHeader.DiscussionType.SIDE_COMMENT
                specificSections = sections?. map { it.id }?.joinToString(",") ?: SectionAPI.ALL_SECTIONS // Set initial sections for this announcement
            }

    /**
     * (Creation mode only) An attachment to be uploaded alongside the announcement. Note that this
     * can only be used when creating new Announcements. Setting/changing attachments on existing
     * announcements (editing mode) is currently unsupported.
     */
    var attachment: FileSubmitObject? = null

    /** (Editing mode only) Set to *true* if the existing Announcement's attachment should be removed */
    var attachmentRemoved = false

    override fun loadData(forceNetwork: Boolean) {
        sectionsJob = tryWeave{
            // Get course sections
            courseSections = awaitApi { SectionManager.getAllSectionsForCourse(canvasContext.id, it, forceNetwork) }
            viewCallback?.onSectionsLoaded()
        } catch {
            // Error grabbing sections
        }
    }
    override fun refresh(forceNetwork: Boolean) {}

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun saveAnnouncement() {
        viewCallback?.onSaveStarted()
        apiJob = tryWeave {
            if (isEditing) {
                val postBody = DiscussionTopicPostBody.fromAnnouncement(announcement, attachmentRemoved)
                val updatedAnnouncement = awaitApi<DiscussionTopicHeader> { callback ->
                    DiscussionManager.editDiscussionTopic(canvasContext, announcement.id, postBody, callback)
                }

                DiscussionUpdatedEvent(updatedAnnouncement).post()
            } else {
                var filePart: MultipartBody.Part? = null
                attachment?.let {
                    val file = File(it.fullPath)
                    val requestBody = file.asRequestBody(it.contentType.toMediaTypeOrNull())
                    filePart = MultipartBody.Part.createFormData("attachment", file.name, requestBody)
                }

                awaitApi<DiscussionTopicHeader> {
                    DiscussionManager.createDiscussion(canvasContext, announcement, filePart, it)
                }
                DiscussionCreatedEvent(true).post()
            }
            viewCallback?.onSaveSuccess()
        } catch {
            viewCallback?.onSaveError()
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun deleteAnnouncement() {
        viewCallback?.onSaveStarted()
        apiJob = tryWeave {
            awaitApi { DiscussionManager.deleteDiscussionTopicHeader(canvasContext, announcement.id, it) }
            DiscussionTopicHeaderDeletedEvent(announcement.id, (DiscussionDetailsWebViewFragment::class.java.toString() + ".onResume()")).post()
            viewCallback?.onDeleteSuccess()
        } catch {
            viewCallback?.onDeleteError()
        }
    }

    /**
     * We receive a 'section' field with a list of 'Section' objects in it from the server when retrieving an announcement.
     * We send a list of strings with 'Section' ids when updating an announcement.
     *
     * Here, we're converting from the list of string ids to a list of Sections. We do this in order to keep the original list ('sections') and have
     * an editable/edited list ('specific_sections') to work with
     */
    fun getSelectedSections(): List<Section> {
        val ids = announcement.specificSections?.split(",") ?: emptyList()
        return courseSections.filter {
            ids.contains(it.id.toString())
        }
    }

    override fun uploadRceImage(imageUri: Uri, activity: Activity) {
        rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, activity) { imageUrl -> viewCallback?.insertImageIntoRCE(imageUrl) }
    }


    override fun onDestroyed() {
        sectionsJob?.cancel()
        rceImageUploadJob?.cancel()
    }
}
