package com.instructure.student.features.file.details

import android.webkit.URLUtil
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.files.details.FileDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileDetailsNetworkDataSourceTest {

    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val fileListNetworkDataSource = FileDetailsNetworkDataSource(moduleApi, fileFolderApi)

    @Before
    fun setup() {
        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getFileFolderFromURL returns api model`() = runTest{
        val expected: List<FileFolder> = listOf(
            FileFolder(id = 1, name = "File 1", url = "localFile.path.1"),
            FileFolder(id = 2, name = "File 2", url = "localFile.path.2"),
            FileFolder(id = 3, name = "File 3", url = "localFile.path.3")
        )

        coEvery { fileFolderApi.getFileFolderFromURL(any(), any()) } returns DataResult.Success(expected[0])

        val result = fileListNetworkDataSource.getFileFolderFromURL("url", 1, true)

        assertEquals(expected[0], result)
    }

    @Test
    fun `getFileFolderFromURL returns null on error`() = runTest {
        coEvery { fileFolderApi.getFileFolderFromURL(any(), any()) } returns DataResult.Fail()

        val result = fileListNetworkDataSource.getFileFolderFromURL("url", 1, true)

        assertEquals(null, result)
    }

    @Test
    fun `markAsRead returns if successful`() = runTest {
        val expected = "".toResponseBody(null)

        coEvery { moduleApi.markModuleItemRead(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = fileListNetworkDataSource.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, true)

        assertEquals(expected, result)
    }

    @Test
    fun `markAsRead returns null on error`() = runTest {
        coEvery { moduleApi.markModuleItemRead(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = fileListNetworkDataSource.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1, true)

        assertEquals(null, result)
    }
}