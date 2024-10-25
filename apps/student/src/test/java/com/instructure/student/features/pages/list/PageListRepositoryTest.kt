package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.pages.list.datasource.PageListLocalDataSource
import com.instructure.student.features.pages.list.datasource.PageListNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PageListRepositoryTest {

    private val networkDataSource: PageListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PageListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val pageListRepository = PageListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get tabs from network if online`() = runTest {
        val offlineExpected = listOf(Page(id = 1L, title = "Offline"))
        val onlineExpected = listOf(Page(id = 1L, title = "Online"))
        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.loadPages(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadPages(any(), any()) } returns onlineExpected

        val pages = pageListRepository.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(onlineExpected, pages)
    }

    @Test
    fun `Get tabs from db if offline`() = runTest {
        val offlineExpected = listOf(Page(id = 1L, title = "Offline"))
        val onlineExpected = listOf(Page(id = 1L, title = "Online"))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadPages(any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadPages(any(), any()) } returns onlineExpected

        val pages = pageListRepository.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(offlineExpected, pages)
    }
}