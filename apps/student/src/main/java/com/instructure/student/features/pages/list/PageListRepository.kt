package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.NetworkStateProvider

class PageListRepository(
    pageListLocalDataSource: PageListLocalDataSource,
    pageListNetworkDataSource: PageListNetworkDataSource,
    networkStateProvider: NetworkStateProvider
) : Repository<PageListDataSource>(pageListLocalDataSource, pageListNetworkDataSource, networkStateProvider) {

    suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        return dataSource.loadPages(canvasContext, forceNetwork)
    }
}