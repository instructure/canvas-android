package com.instructure.student.features.pages.list.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.utils.isGroup

class PageListLocalDataSource(
    private val pageFacade: PageFacade
) : PageListDataSource {

    override suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        if (canvasContext.isGroup) return emptyList()

        return pageFacade.findByCourseId(canvasContext.id)
    }
}