package com.instructure.student.features.pages.list.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.utils.isGroup

class PageListLocalDataSource(private val pageDao: PageDao) : PageListDataSource {

    override suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        if (canvasContext.isGroup) return emptyList()

        return pageDao.findByCourseId(canvasContext.id).map { it.toApiModel() }
    }
}