package com.instructure.student.features.pages.list.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page

interface PageListDataSource {

    suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page>
}