/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class FileFolderDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var fileFolderDao: FileFolderDao
    private lateinit var localFileDao: LocalFileDao
    private lateinit var fileSyncSettingsDao: FileSyncSettingsDao
    private lateinit var courseDao: CourseDao
    private lateinit var courseSyncSettingsDao: CourseSyncSettingsDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        fileFolderDao = db.fileFolderDao()
        localFileDao = db.localFileDao()
        fileSyncSettingsDao = db.fileSyncSettingsDao()
        courseDao = db.courseDao()
        courseSyncSettingsDao = db.courseSyncSettingsDao()
    }

    @Test
    fun testInsertReplace() = runTest {
        val fileFolder = FileFolderEntity(FileFolder(id = 1L, name = "original"))
        val updated = FileFolderEntity(FileFolder(id = 1L, name = "updated"))

        fileFolderDao.insert(fileFolder)
        fileFolderDao.insert(updated)

        val result = fileFolderDao.findById(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testDeleteAll() = runTest {
        val files = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "original1")),
            FileFolderEntity(FileFolder(id = 2L, name = "original2"))
        )

        fileFolderDao.insertAll(files)
        fileFolderDao.deleteAllByCourseId(1L)

        val result = fileFolderDao.findAllFilesByCourseId(1L)

        assertEquals(emptyList<FileFolderEntity>(), result)
    }

    @Test
    fun testFindAllByCourseId() = runTest {
        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    contextId = 1L,
                    contextType = "Course",
                    name = "folder",
                    parentFolderId = 0
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    contextId = 1L,
                    contextType = "Course",
                    name = "folder2",
                    parentFolderId = 0
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 3L,
                    contextId = 2L,
                    contextType = "Course",
                    name = "folder2",
                    parentFolderId = 0
                )
            )
        )
        val files = listOf(
            FileFolderEntity(FileFolder(id = 4L, name = "file1", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 5L, name = "file2", folderId = 2L)),
            FileFolderEntity(FileFolder(id = 6L, name = "file3", folderId = 3L))
        )

        fileFolderDao.insertAll(folders)
        fileFolderDao.insertAll(files)

        val result = fileFolderDao.findAllFilesByCourseId(1L)

        assertEquals(files.subList(0, 2), result)
    }

    @Test
    fun testFindById() = runTest {
        val files = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "file1")),
            FileFolderEntity(FileFolder(id = 2L, name = "file2"))
        )

        fileFolderDao.insertAll(files)

        val result = fileFolderDao.findById(1L)

        assertEquals(files[0], result)
    }

    @Test
    fun testVisibleFindFoldersByParentId() = runTest {
        val folders = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "folder1", parentFolderId = 0)),
            FileFolderEntity(FileFolder(id = 2L, name = "folder2", parentFolderId = 0)),
            FileFolderEntity(FileFolder(id = 3L, name = "folder3", parentFolderId = 1L)),
            FileFolderEntity(FileFolder(id = 4L, name = "folder4", parentFolderId = 1L, isHidden = true)),
            FileFolderEntity(FileFolder(id = 7L, name = "folder7", parentFolderId = 1L)),
            FileFolderEntity(FileFolder(id = 8L, name = "folder8", parentFolderId = 1L, isHiddenForUser = true)),
            FileFolderEntity(FileFolder(id = 5L, name = "folder5", parentFolderId = 2L)),
            FileFolderEntity(FileFolder(id = 6L, name = "folder6", parentFolderId = 2L))
        )

        fileFolderDao.insertAll(folders)

        val result = fileFolderDao.findVisibleFoldersByParentId(1L)

        assertEquals(2, result.size)
        assertEquals("folder3", result.first().name)
        assertEquals("folder7", result.last().name)
    }

    @Test
    fun testFindVisibleFilesByFolderId() = runTest {
        val folders = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "folder1", parentFolderId = 0)),
            FileFolderEntity(FileFolder(id = 2L, name = "folder2", parentFolderId = 0))
        )

        val files = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file1", folderId = 1L, isHidden = true)),
            FileFolderEntity(FileFolder(id = 7L, name = "file5", folderId = 1L, isHiddenForUser = true)),
            FileFolderEntity(FileFolder(id = 8L, name = "file6", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 4L, name = "file2", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 5L, name = "file3", folderId = 2L)),
            FileFolderEntity(FileFolder(id = 6L, name = "file4", folderId = 2L))
        )

        fileFolderDao.insertAll(folders)
        fileFolderDao.insertAll(files)

        val result = fileFolderDao.findVisibleFilesByFolderId(1L)

        assertEquals(2, result.size)
        assertEquals("file2", result.first().name)
        assertEquals("file6", result.last().name)
    }

    @Test
    fun testFindRootFolderForContext() = runTest {
        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val result = fileFolderDao.findRootFolderForContext(1L)

        assertEquals(folders[0], result)
    }

    @Test
    fun testReplaceAll() = runTest {
        val files = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "file1", folderId = 1L, contextId = 1L)),
            FileFolderEntity(FileFolder(id = 2L, name = "file2", folderId = 1L, contextId = 1L))
        )

        val newFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file3", folderId = 1L, contextId = 1L)),
            FileFolderEntity(FileFolder(id = 4L, name = "file4", folderId = 1L, contextId = 1L))
        )

        fileFolderDao.insertAll(files)

        fileFolderDao.replaceAll(newFiles, 1L)

        val result = fileFolderDao.findVisibleFilesByFolderId(1L)

        assertEquals(newFiles, result)
    }

    @Test
    fun testFindFilesToSyncCreatedDate() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))
        courseDao.insert(CourseEntity(Course(id = 2L, name = "course2")))

        val localFiles = listOf(
            LocalFileEntity(id = 3L, path = "file1", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 4L, path = "file2", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 5L, path = "file3", courseId = 2L, createdDate = Date(1000)),
            LocalFileEntity(id = 6L, path = "file4", courseId = 2L, createdDate = Date(1000))
        )

        localFiles.forEach { localFileDao.insert(it) }

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file1", folderId = 1L, createdDate = Date(2))),
            FileFolderEntity(FileFolder(id = 4L, name = "file2", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 5L, name = "file3", folderId = 2L, createdDate = Date(1000))),
            FileFolderEntity(FileFolder(id = 6L, name = "file4", folderId = 2L, createdDate = Date(1000)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(1L, true)

        assertEquals(listOf(remoteFiles[0]), result)
    }

    @Test
    fun testFindFilesToSyncUpdatedDate() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))
        courseDao.insert(CourseEntity(Course(id = 2L, name = "course2")))

        val localFiles = listOf(
            LocalFileEntity(id = 3L, path = "file1", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 4L, path = "file2", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 5L, path = "file3", courseId = 2L, createdDate = Date(1000)),
            LocalFileEntity(id = 6L, path = "file4", courseId = 2L, createdDate = Date(1000))
        )

        localFiles.forEach { localFileDao.insert(it) }

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file1", folderId = 1L, updatedDate = Date(2))),
            FileFolderEntity(FileFolder(id = 4L, name = "file2", folderId = 1L, updatedDate = Date(1))),
            FileFolderEntity(FileFolder(id = 5L, name = "file3", folderId = 2L, updatedDate = Date(1000))),
            FileFolderEntity(FileFolder(id = 6L, name = "file4", folderId = 2L, updatedDate = Date(1000)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(1L, true)

        assertEquals(listOf(remoteFiles[0]), result)
    }

    @Test
    fun testFindFilesToSyncNoLocalFiles() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))
        courseDao.insert(CourseEntity(Course(id = 2L, name = "course2")))

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file1", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 4L, name = "file2", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 5L, name = "file3", folderId = 2L, createdDate = Date(1000))),
            FileFolderEntity(FileFolder(id = 6L, name = "file4", folderId = 2L, createdDate = Date(1000)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(2L, true)

        assertEquals(remoteFiles.subList(2, 4), result)
    }

    @Test
    fun testFindFilesToSyncSyncSettingsNoLocalFiles() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))

        val courseSyncSettings = CourseSyncSettingsEntity(courseId = 1L, courseName = "Course 1", fullContentSync = true)
        courseSyncSettingsDao.insert(courseSyncSettings)

        val fileSyncSettings = FileSyncSettingsEntity(id = 3L, courseId = 1L, fileName = "file1", url = "url1")

        fileSyncSettingsDao.insert(fileSyncSettings)

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            )
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "file1", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 4L, name = "file2", folderId = 1L, createdDate = Date(1)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(1L, false)

        assertEquals(listOf(remoteFiles[0]), result)
    }

    @Test
    fun testFindFileToSyncFull() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))
        courseDao.insert(CourseEntity(Course(id = 2L, name = "course2")))

        val localFiles = listOf(
            LocalFileEntity(id = 3L, path = "createAt", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 4L, path = "no update", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 5L, path = "updatedAt", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 7L, path = "file4", courseId = 2L, createdDate = Date(1000)),
            LocalFileEntity(id = 8L, path = "file5", courseId = 2L, createdDate = Date(1000))
        )

        localFiles.forEach { localFileDao.insert(it) }

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "createdAt", folderId = 1L, createdDate = Date(2))),
            FileFolderEntity(FileFolder(id = 4L, name = "no update", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 5L, name = "updatedAt", folderId = 1L, updatedDate = Date(2))),
            FileFolderEntity(FileFolder(id = 6L, name = "not synced", folderId = 1L, updatedDate = Date(1))),
            FileFolderEntity(FileFolder(id = 7L, name = "file4", folderId = 2L, createdDate = Date(1000))),
            FileFolderEntity(FileFolder(id = 8L, name = "file5", folderId = 2L, createdDate = Date(1000)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(1L, true)

        assertEquals(listOf(remoteFiles[0], remoteFiles[2], remoteFiles[3]), result)
    }

    @Test
    fun testFindFileToSyncSelected() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1L, name = "course1")))
        courseDao.insert(CourseEntity(Course(id = 2L, name = "course2")))

        val courseSyncSettings = CourseSyncSettingsEntity(courseId = 1L, courseName = "Course 1", fullContentSync = false)
        courseSyncSettingsDao.insert(courseSyncSettings)

        val fileSyncSettings = listOf(
            FileSyncSettingsEntity(id = 3L, courseId = 1L, fileName = "file1", url = "url1"),
            FileSyncSettingsEntity(id = 4L, courseId = 1L, fileName = "file3", url = "url3"),
            FileSyncSettingsEntity(id = 6L, courseId = 1L, fileName = "file4", url = "url4"),
        )

        fileSyncSettingsDao.insertAll(fileSyncSettings)

        val localFiles = listOf(
            LocalFileEntity(id = 3L, path = "createAt", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 4L, path = "no update", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 5L, path = "updatedAt", courseId = 1L, createdDate = Date(1)),
            LocalFileEntity(id = 7L, path = "file4", courseId = 2L, createdDate = Date(1000)),
            LocalFileEntity(id = 8L, path = "file5", courseId = 2L, createdDate = Date(1000))
        )

        localFiles.forEach { localFileDao.insert(it) }

        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    name = "folder1",
                    parentFolderId = 0,
                    contextId = 1L,
                    contextType = "Course"
                )
            ),
            FileFolderEntity(
                FileFolder(
                    id = 2L,
                    name = "folder2",
                    parentFolderId = 0,
                    contextId = 2L,
                    contextType = "Course"
                )
            ),
        )

        fileFolderDao.insertAll(folders)

        val remoteFiles = listOf(
            FileFolderEntity(FileFolder(id = 3L, name = "createdAt", folderId = 1L, createdDate = Date(2))),
            FileFolderEntity(FileFolder(id = 4L, name = "no update", folderId = 1L, createdDate = Date(1))),
            FileFolderEntity(FileFolder(id = 5L, name = "updatedAt", folderId = 1L, updatedDate = Date(2))),
            FileFolderEntity(FileFolder(id = 6L, name = "not synced", folderId = 1L, updatedDate = Date(1))),
            FileFolderEntity(FileFolder(id = 7L, name = "file4", folderId = 2L, createdDate = Date(1000))),
            FileFolderEntity(FileFolder(id = 8L, name = "file5", folderId = 2L, createdDate = Date(1000)))
        )

        fileFolderDao.insertAll(remoteFiles)

        val result = fileFolderDao.findFilesToSync(1L, false)

        assertEquals(listOf(remoteFiles[0], remoteFiles[3]), result)
    }

    @Test
    fun testFindByIds() = runTest {
        val files = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "file1")),
            FileFolderEntity(FileFolder(id = 2L, name = "file2")),
            FileFolderEntity(FileFolder(id = 3L, name = "file3"))
        )

        fileFolderDao.insertAll(files)

        val result = fileFolderDao.findByIds(setOf(1, 2))

        assertEquals(listOf(files[0], files[1]), result)
    }

    @Test
    fun testDeleteAllByCourseIdDeleteFilesWhereParentFolderHasCourseId() = runTest {
        val files = listOf(
            FileFolderEntity(FileFolder(id = 1L, name = "file1", folderId = 1L, contextId = 1L)),
            FileFolderEntity(FileFolder(id = 2L, name = "file2", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 3L, name = "file2", folderId = 2L)),
        )

        fileFolderDao.insertAll(files)

        fileFolderDao.deleteAllByCourseId(1L)

        val result = fileFolderDao.findByIds(setOf(1, 2, 3))

        assertEquals(listOf(files[2]), result)
    }

    @Test
    fun testSearchFiles() = runTest {
        val folders = listOf(
            FileFolderEntity(
                FileFolder(
                    id = 1L,
                    contextId = 1L,
                    contextType = "Course",
                    name = "folder",
                    parentFolderId = 0
                )
            )
        )
        val files = listOf(
            FileFolderEntity(FileFolder(id = 2L, displayName = "file1", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 3L, displayName = "file2", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 4L, displayName = "different name", folderId = 1L)),
            FileFolderEntity(FileFolder(id = 5L, displayName = "file hidden", folderId = 1L, isHidden = true)),
            FileFolderEntity(FileFolder(id = 6L, displayName = "file hidden for user", folderId = 1L, isHiddenForUser = true)),
        )

        fileFolderDao.insertAll(folders)
        fileFolderDao.insertAll(files)

        val result = fileFolderDao.searchCourseFiles(1L, "fil")

        assertEquals(files.subList(0, 2), result)
    }
}