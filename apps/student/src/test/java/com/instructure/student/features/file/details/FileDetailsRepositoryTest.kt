package com.instructure.student.features.file.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.files.details.FileDetailsLocalDataSource
import com.instructure.student.features.files.details.FileDetailsNetworkDataSource
import com.instructure.student.features.files.details.FileDetailsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileDetailsRepositoryTest {

    private val fileDetailsLocalDataSource: FileDetailsLocalDataSource = mockk(relaxed = true)
    private val fileDetailsNetworkDataSource: FileDetailsNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val fileDetailsRepository = FileDetailsRepository(
        fileDetailsLocalDataSource,
        fileDetailsNetworkDataSource,
        networkStateProvider,
        featureFlagProvider
    )

    @Before
    fun setup() {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns true
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getFileFolderFromURL calls localDataSource when offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        fileDetailsRepository.getFileFolderFromURL("https://instructure.com", 1, false)

        coVerify { fileDetailsLocalDataSource.getFileFolderFromURL("https://instructure.com", 1, false) }
    }

    @Test
    fun `getFileFolderFromURL calls networkDataSource when online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true
        fileDetailsRepository.getFileFolderFromURL("https://instructure.com", 1, false)

        coVerify { fileDetailsNetworkDataSource.getFileFolderFromURL("https://instructure.com", 1, false) }
    }

    @Test
    fun `markAsRead calls networkDataSource when offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        fileDetailsRepository.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, false)

        coVerify { fileDetailsNetworkDataSource.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, false) }
    }

    @Test
    fun `markAsRead calls networkDataSource when online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true
        fileDetailsRepository.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, false)

        coVerify { fileDetailsNetworkDataSource.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, false) }
    }
}