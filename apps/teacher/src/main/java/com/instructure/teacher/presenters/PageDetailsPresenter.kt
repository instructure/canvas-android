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

import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.R
import com.instructure.teacher.viewinterface.PageDetailsView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job

class PageDetailsPresenter(val mCanvasContext: CanvasContext, var mPage: Page) : FragmentPresenter<PageDetailsView>() {

    var mPageCall: Job? = null

    override fun loadData(forceNetwork: Boolean) {

    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getPage(pageId: String, canvasContext: CanvasContext, forceNetwork: Boolean) {
        mPageCall = tryWeave {
            viewCallback?.onRefreshStarted()
            mPage = awaitApi { PageManager.getPageDetails(canvasContext, pageId, forceNetwork, it) }

            viewCallback?.let {
                it.onRefreshFinished()
                it.populatePageDetails(mPage)
            }
        } catch {
            // Show error
            viewCallback?.onError(R.string.errorOccurred)
            it.printStackTrace()
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean) {
        mPageCall = tryWeave {
            viewCallback?.onRefreshStarted()
            mPage = awaitApi { PageManager.getFrontPage(canvasContext, forceNetwork, it) }

            viewCallback?.let {
                it.onRefreshFinished()
                it.populatePageDetails(mPage)
            }

        } catch {
            // Show error
            viewCallback?.onError(R.string.errorOccurred)
            it.printStackTrace()
        }
    }

    override fun refresh(forceNetwork: Boolean) {}

    override fun onDestroyed() {
        super.onDestroyed()
        mPageCall?.cancel()
    }
}
