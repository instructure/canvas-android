package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.list.datasource.PageListDataSource
import com.instructure.student.features.pages.list.datasource.PageListLocalDataSource
import com.instructure.student.features.pages.list.datasource.PageListNetworkDataSource

class PageListRepository(
    pageListLocalDataSource: PageListLocalDataSource,
    pageListNetworkDataSource: PageListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<PageListDataSource>(pageListLocalDataSource, pageListNetworkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        return dataSource().loadPages(canvasContext, forceNetwork)
    }
}