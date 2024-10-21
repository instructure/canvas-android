package com.instructure.student.features.file.details

import android.webkit.URLUtil
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.student.features.files.details.FileDetailsLocalDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class FileDetailsLocalDataSourceTest {

    private val fileFolderDao: FileFolderDao = mockk(relaxed = true)
    private val localFileDao: LocalFileDao = mockk(relaxed = true)

    private val fileDetailsLocalDataSource = FileDetailsLocalDataSource(
        fileFolderDao,
        localFileDao
    )

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
    fun `getFileFolderFromURL returns api model`() = runTest {
        val expected: List<FileFolder> = listOf(
            FileFolder(id = 1, name = "File 1", url = "localFile.path.1"),
            FileFolder(id = 2, name = "File 2", url = "localFile.path.2"),
            FileFolder(id = 3, name = "File 3", url = "localFile.path.3")
        )

        val localFiles: List<LocalFileEntity> = listOf(
            LocalFileEntity(id = 1, courseId = 1, createdDate = Date(), path = "localFile.path.1"),
            LocalFileEntity(id = 2, courseId = 1, createdDate = Date(), path = "localFile.path.2"),
            LocalFileEntity(id = 3, courseId = 1, createdDate = Date(), path = "localFile.path.3"),
        )

        coEvery { localFileDao.findById(2) } returns localFiles[1]
        coEvery { fileFolderDao.findById(2) } returns FileFolderEntity(expected[1])

        val fileFolder = fileDetailsLocalDataSource.getFileFolderFromURL("https://www.instructure.com", 2, false)

        assertEquals(expected[1], fileFolder)
    }

    @Test
    fun `getFileFolderFromURL returns null if not exists`() = runTest {
        coEvery { localFileDao.findById(5) } returns null

        val fileFolder = fileDetailsLocalDataSource.getFileFolderFromURL("https://www.instructure.com", 5, false)

        assertEquals(null, fileFolder)
    }
}