package com.instructure.pandautils.features.shareextension.progress

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.toJson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class ShareExtensionProgressViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val workManager: WorkManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var mockLiveData: MutableLiveData<WorkInfo>
    private lateinit var viewModel: ShareExtensionProgressDialogViewModel
    private lateinit var uuid: UUID

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        setupStrings()

        uuid = UUID.randomUUID()

        mockLiveData = MutableLiveData<WorkInfo>()
        every { workManager.getWorkInfoByIdLiveData(uuid) } returns mockLiveData

        viewModel = createViewModel()
    }

    @Test
    fun `Show success dialog after uploading`() {
        viewModel.setUUID(uuid)
        mockLiveData.postValue(WorkInfo(uuid, WorkInfo.State.SUCCEEDED, Data.EMPTY, emptyList(), Data.EMPTY, 1))

        viewModel.events.observe(lifecycleOwner) {}
        assertEquals(ShareExtensionProgressAction.ShowSuccessDialog, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Show error dialog when upload failed`() {
        viewModel.setUUID(uuid)
        mockLiveData.postValue(WorkInfo(uuid, WorkInfo.State.FAILED, Data.EMPTY, emptyList(), Data.EMPTY, 1))

        viewModel.events.observe(lifecycleOwner) {}
        assertEquals(ShareExtensionProgressAction.ShowErrorDialog, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Cancel clicked`() {
        viewModel.events.observe(lifecycleOwner) {}
        viewModel.setUUID(uuid)
        viewModel.cancelClicked()

        assertEquals(ShareExtensionProgressAction.CancelUpload, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Close clicked`() {
        viewModel.events.observe(lifecycleOwner) {}
        viewModel.setUUID(uuid)
        viewModel.onCloseClicked()

        assertEquals(ShareExtensionProgressAction.Close, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Loading until liveData is available`() {
        viewModel.setUUID(uuid)
        viewModel.state.observe(lifecycleOwner) {}

        assertEquals(ViewState.Loading, viewModel.state.value)

        val progressData = Data.Builder()
            .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
            .putStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD, emptyArray())
            .build()
        mockLiveData.postValue(
            WorkInfo(
                uuid,
                WorkInfo.State.RUNNING,
                Data.EMPTY,
                emptyList(),
                progressData,
                1
            )
        )

        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Live Data maps correctly`() {

        every { resources.getString(R.string.submissionProgressSubtitle, "Assignment") } returns "Uploading submission to \"Assignment\""

        val filesToUpload = listOf(
            FileSubmitObject(name = "Test 1", size = 1L, contentType = "text/file", fullPath = ""),
            FileSubmitObject(name = "Test 2", size = 1L, contentType = "text/file", fullPath = "")
        ).map { it.toJson() }.toTypedArray()

        val progressData = Data.Builder()
            .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 2L)
            .putStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD, filesToUpload)
            .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
            .putString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME, "Assignment")
            .build()

        mockLiveData.postValue(
            WorkInfo(
                uuid,
                WorkInfo.State.RUNNING,
                Data.EMPTY,
                emptyList(),
                progressData,
                1
            )
        )

        val expectedItemData = listOf(
            FileProgressViewData(
                "Test 1",
                "1 B",
                R.drawable.ic_attachment,
                false
            ),
            FileProgressViewData(
                "Test 2",
                "1 B",
                R.drawable.ic_attachment,
                false
            )
        )

        viewModel.setUUID(uuid)
        viewModel.data.observe(lifecycleOwner) {}

        val viewData = viewModel.data.value

        viewData?.items?.forEachIndexed { index, fileProgressItemViewModel ->
            assertEquals(expectedItemData[index], fileProgressItemViewModel.data)
        }
        assertEquals("Submission", viewData?.dialogTitle)
        assertEquals("Uploading submission to \"Assignment\"", viewData?.subtitle)
        assertEquals("0 B", viewData?.currentSize)
        assertEquals("2 B", viewData?.maxSize)
        assertEquals("0.0%", viewData?.percentage)
    }

    @Test
    fun `Update view data when live data changes`() {
        every { resources.getString(R.string.submissionProgressSubtitle, "Assignment") } returns "Uploading submission to \"Assignment\""

        val filesToUpload = listOf(
            FileSubmitObject(name = "Test 1", size = 1L, contentType = "text/file", fullPath = ""),
            FileSubmitObject(name = "Test 2", size = 1L, contentType = "text/file", fullPath = "")
        ).map { it.toJson() }.toTypedArray()

        val progressData = Data.Builder()
            .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 2L)
            .putStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD, filesToUpload)
            .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
            .putString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME, "Assignment")

        mockLiveData.postValue(
            WorkInfo(
                uuid,
                WorkInfo.State.RUNNING,
                Data.EMPTY,
                emptyList(),
                progressData.build(),
                1
            )
        )

        val expectedItemData = listOf(
            FileProgressViewData(
                "Test 1",
                "1 B",
                R.drawable.ic_attachment,
                true
            ),
            FileProgressViewData(
                "Test 2",
                "1 B",
                R.drawable.ic_attachment,
                false
            )
        )

        viewModel.setUUID(uuid)
        viewModel.data.observe(lifecycleOwner) {}

        progressData
            .putStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD, filesToUpload)
            .putStringArray(FileUploadWorker.PROGRESS_DATA_UPLOADED_FILES, arrayOf(filesToUpload[0]))
            .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 1L)

        mockLiveData.postValue(
            WorkInfo(
                uuid,
                WorkInfo.State.RUNNING,
                Data.EMPTY,
                emptyList(),
                progressData.build(),
                1
            )
        )

        val viewData = viewModel.data.value

        viewData?.items?.forEachIndexed { index, fileProgressItemViewModel ->
            assertEquals(expectedItemData[index], fileProgressItemViewModel.data)
        }
        assertEquals("Submission", viewData?.dialogTitle)
        assertEquals("Uploading submission to \"Assignment\"", viewData?.subtitle)
        assertEquals("1 B", viewData?.currentSize)
        assertEquals("2 B", viewData?.maxSize)
        assertEquals("50.0%", viewData?.percentage)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.fileUpload) } returns "File Upload"
        every { resources.getString(R.string.submission) } returns "Submission"
        every { resources.getString(R.string.fileUploadProgressSubtitle) } returns "Uploading to Files"
    }

    private fun createViewModel(): ShareExtensionProgressDialogViewModel {
        return ShareExtensionProgressDialogViewModel(workManager, resources)
    }
}