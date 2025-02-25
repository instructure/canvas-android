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
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.postmodels.PagePostBody
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.teacher.events.PageCreatedEvent
import com.instructure.teacher.events.PageDeletedEvent
import com.instructure.teacher.events.PageUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.interfaces.RceMediaUploadPresenter
import com.instructure.teacher.viewinterface.CreateOrEditPageView
import com.instructure.pandautils.blueprint.FragmentPresenter
import retrofit2.Response

class CreateOrEditPagePresenter(private val canvasContext: CanvasContext, mPage: Page? = null)
    : FragmentPresenter<CreateOrEditPageView>(), RceMediaUploadPresenter {

    override var rceImageUploadJob: WeaveJob? = null
    private var apiJob: WeaveJob? = null

    /** *True* for editing mode, *false* for creation mode */
    var isEditing = mPage != null

    /**
     * The page that is being edited/created. Changes should be applied directly to this
     * object. For editing mode this object should be passed to the constructor as a deep copy of
     * the original so that canceled changes are not erroneously propagated back to other pages.
     */
    val page: Page = mPage ?: Page()

    override fun loadData(forceNetwork: Boolean) { }

    override fun refresh(forceNetwork: Boolean) { }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun savePage() {
        viewCallback?.onSaveStarted()
        apiJob = tryWeave {
            if (isEditing) {
                val postBody = PagePostBody(page.body, page.title, page.frontPage, page.editingRoles, page.published)

                val updatedPage = awaitApi<Page> { PageManager.editPage(canvasContext, page.url ?: "", postBody, it) }
                PageUpdatedEvent(updatedPage).post()

            } else {
                awaitApi<Page> { PageManager.createPage(canvasContext, page, it) }
                PageCreatedEvent(true).post()
            }
            viewCallback?.onSaveSuccess()
        } catch {
            viewCallback?.onSaveError()

        }
    }

    fun deletePage(pageUrl: String) {
        PageManager.deletePage(canvasContext, pageUrl, object: StatusCallback<Page>(){
            override fun onResponse(response: Response<Page>, linkHeaders: LinkHeaders, type: ApiType) {
                if(response.code() in 200..299) {
                    PageDeletedEvent(page, (PageDetailsFragment::class.java.toString() + ".onResume()")).post()
                    viewCallback?.pageDeletedSuccessfully()
                }
            }
        })
    }

    override fun uploadRceImage(imageUri: Uri, activity: Activity) {
        rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext , activity) { imageUrl -> viewCallback?.insertImageIntoRCE(imageUrl) }
    }

    override fun onDestroyed() {
        rceImageUploadJob?.cancel()
    }
}