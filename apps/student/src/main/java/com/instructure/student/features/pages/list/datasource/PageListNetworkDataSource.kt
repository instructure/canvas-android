package com.instructure.student.features.pages.list.datasource

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.depaginate

class PageListNetworkDataSource(
    private val api: PageAPI.PagesInterface
) : PageListDataSource {

    override suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return api.getFirstPagePages(canvasContext.id, canvasContext.apiContext(), restParams)
            .depaginate { api.getNextPagePagesList(it, restParams) }.dataOrThrow
    }
}