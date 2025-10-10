package com.instructure.pandautils.features.shareextension.progress

import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.hasKeyWithValueOfType
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.humanReadableByteCount
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShareExtensionProgressDialogViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val resources: Resources,
    private val fileUploadInputDao: FileUploadInputDao,
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val fileUploadUtilsHelper: FileUploadUtilsHelper
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<ShareExtensionProgressViewData>
        get() = _data
    private val _data = MutableLiveData<ShareExtensionProgressViewData>()

    val events: LiveData<Event<ShareExtensionProgressAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionProgressAction>>()

    private var viewData: ShareExtensionProgressViewData? = null

    private var itemViewData: List<FileProgressViewData> = emptyList()

    private var workerId: UUID? = null
    private var fileUploadType = FileUploadType.USER

    private val observer = Observer<WorkInfo> {
        when (it.state) {
            WorkInfo.State.SUCCEEDED -> {
                _events.postValue(Event((ShareExtensionProgressAction.ShowSuccessDialog(fileUploadType))))
            }
            WorkInfo.State.RUNNING -> {
                viewModelScope.launch { updateViewData(it.progress, false) }
            }
            WorkInfo.State.FAILED -> {
                viewModelScope.launch { updateViewData(it.outputData, true) }
            }
            else -> {}
        }
    }

    fun setUUID(uuid: UUID) {
        this.workerId = uuid
        _state.postValue(ViewState.Loading)
        workManager.getWorkInfoByIdLiveData(uuid).observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        workerId?.let { workManager.getWorkInfoByIdLiveData(it).removeObserver(observer) }
    }

    private suspend fun updateViewData(progress: Data, failed: Boolean) {
        if (allDataPresent(progress)) {
            val inputData = fileUploadInputDao.findByWorkerId(workerId.toString())
            _state.postValue(ViewState.Success)

            val maxSize = progress.getLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
            val currentSize = progress.getLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
            val assignmentName = progress.getString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME)
            fileUploadType = if (assignmentName.isNullOrEmpty()) FileUploadType.USER else FileUploadType.ASSIGNMENT
            val subtitle = if (failed) {
                resources.getString(R.string.fileUploadFailedSubtitle)
            } else if (assignmentName.isNullOrEmpty()) {
                resources.getString(R.string.fileUploadProgressSubtitle)
            } else {
                resources.getString(R.string.submissionProgressSubtitle, assignmentName)
            }

            val filesToUpload = progress.getStringArray(FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD)
                .orEmpty().map { it.fromJson<FileSubmitObject>() }
                .filter { fso -> inputData?.filePaths?.any { it.contains(Uri.fromFile(File(fso.fullPath)).toString()) }.orDefault() }

            val uploadedFilesMap = progress.getStringArray(FileUploadWorker.PROGRESS_DATA_UPLOADED_FILES)
                .orEmpty().map { it.fromJson<FileSubmitObject>() }.associateBy { it.name }

            if (viewData == null) {
                itemViewData = filesToUpload.map {
                    FileProgressViewData(
                        it.name,
                        it.size.humanReadableByteCount(),
                        getIconDrawableRes(it.contentType, (failed && !uploadedFilesMap.containsKey(it.name))),
                        getFileUploadStatus(uploadedFilesMap, it, failed)
                    ) { fileUploadProgressViewData -> removeUploadItem(fileUploadProgressViewData, filesToUpload) }
                }

                viewData = ShareExtensionProgressViewData(
                    items = itemViewData.map { FileProgressItemViewModel(it) },
                    dialogTitle = resources.getString(if (assignmentName.isNullOrEmpty()) R.string.fileUpload else R.string.submission),
                    subtitle = subtitle,
                    maxSize = maxSize.humanReadableByteCount(),
                    currentSize = currentSize.humanReadableByteCount(),
                    progressInt = ((currentSize.toDouble() / maxSize.toDouble()) * 100.0).toInt(),
                    percentage = "${String.format("%.1f", currentSize.toDouble() / maxSize.toDouble() * 100.0)}%",
                    failed = failed
                ).also {
                    _data.postValue(it)
                }
            } else {
                viewData?.apply {
                    this.subtitle = subtitle
                    this.maxSize = maxSize.humanReadableByteCount()
                    this.currentSize = currentSize.humanReadableByteCount()
                    this.progressInt = ((currentSize.toDouble() / maxSize.toDouble()) * 100).toInt()
                    this.percentage = "${String.format("%.1f", currentSize.toDouble() / maxSize.toDouble() * 100.0)}%"
                    this.failed = failed
                    filesToUpload.forEach { fileToUpload ->
                        val status = getFileUploadStatus(uploadedFilesMap, fileToUpload, failed)
                        this.items.find { it.data.name == fileToUpload.name }.apply {
                            if (this?.data?.status != status) {
                                this?.data?.status = status
                                this?.data?.icon = getIconDrawableRes(
                                    fileToUpload.contentType,
                                    (failed && !uploadedFilesMap.containsKey(this?.data?.name))
                                )
                                this?.data?.notifyPropertyChanged(BR.status)
                                this?.data?.notifyPropertyChanged(BR.icon)
                                this?.data?.notifyPropertyChanged(BR.iconTint)
                            }
                        }
                    }

                    notifyPropertyChanged(BR.subtitle)
                    notifyPropertyChanged(BR.maxSize)
                    notifyPropertyChanged(BR.currentSize)
                    notifyPropertyChanged(BR.progressInt)
                    notifyPropertyChanged(BR.percentage)
                    notifyPropertyChanged(BR.failed)
                }
            }
        }
    }

    private fun getFileUploadStatus(
        uploadedFilesMap: Map<String, FileSubmitObject>,
        fileToUpload: FileSubmitObject,
        failed: Boolean
    ): FileProgressStatus {
        return if (uploadedFilesMap.containsKey(fileToUpload.name)) {
            FileProgressStatus.UPLOADED
        } else if (failed) {
            FileProgressStatus.FAILED
        } else {
            FileProgressStatus.IN_PROGRESS
        }
    }

    private fun allDataPresent(progress: Data): Boolean {
        return progress.hasKeyWithValueOfType<Long>(
            FileUploadWorker.PROGRESS_DATA_FULL_SIZE
        ) && progress.hasKeyWithValueOfType<Array<String>>(
            FileUploadWorker.PROGRESS_DATA_FILES_TO_UPLOAD
        )
    }

    @DrawableRes
    private fun getIconDrawableRes(contentType: String, failed: Boolean): Int {
        return when {
            failed -> R.drawable.ic_warning
            contentType.contains("image") -> R.drawable.ic_image
            contentType.contains("video") -> R.drawable.ic_media
            contentType.contains("pdf") -> R.drawable.ic_pdf
            else -> R.drawable.ic_attachment
        }
    }

    private fun removeUploadItem(fileUploadProgressViewData: FileProgressViewData, filesToUpload: List<FileSubmitObject>) {
        if (fileUploadProgressViewData.status != FileProgressStatus.FAILED) return
        viewModelScope.launch {
            val uploadItems = data.value?.items?.toMutableList()
            uploadItems?.removeIf { it.data.name == fileUploadProgressViewData.name }
            data.value?.items = uploadItems.orEmpty()
            data.value?.notifyPropertyChanged(BR.items)
            val pathToRemove = filesToUpload.find { it.name == fileUploadProgressViewData.name }?.fullPath.orEmpty()
            fileUploadInputDao.findByWorkerId(workerId.toString())?.let { fileUploadEntity ->
                if (fileUploadEntity.filePaths.size > 1) {
                    val filePaths = fileUploadEntity.filePaths.filter { !it.contains(Uri.fromFile(File(pathToRemove)).toString()) }
                    fileUploadInputDao.update(fileUploadEntity.copy(filePaths = filePaths))
                    fileUploadUtilsHelper.deleteCachedFiles(listOf(pathToRemove))
                } else {
                    cancelUpload()
                }
            }
        }
    }

    fun onRetryClick() {
        viewModelScope.launch {
            fileUploadInputDao.findByWorkerId(workerId.toString())?.let {
                val worker = OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .addTag(FileUploadWorker.WORKER_TAG)
                    .build()
                fileUploadInputDao.insert(it.copy(workerId = worker.id.toString()))
                fileUploadInputDao.delete(it)
                dashboardFileUploadDao.deleteByWorkerId(it.workerId)
                workManager.enqueue(worker)
                setUUID(worker.id)
            }
        }
    }

    fun onCloseClicked() {
        _events.postValue(Event(ShareExtensionProgressAction.Close))
    }

    fun cancelUpload() {
        viewModelScope.launch {
            workerId?.let { workerId ->
                workManager.cancelWorkById(workerId)
                dashboardFileUploadDao.deleteByWorkerId(workerId.toString())
                fileUploadInputDao.findByWorkerId(workerId.toString())?.let {
                    fileUploadUtilsHelper.deleteCachedFiles(it.filePaths)
                    fileUploadInputDao.delete(it)
                }
            }
            onCloseClicked()
        }
    }

    fun cancelClicked() {
        _events.postValue(Event(ShareExtensionProgressAction.CancelUpload(
            title = if (fileUploadType == FileUploadType.ASSIGNMENT) resources.getString(R.string.cancelSubmissionDialogTitle) else resources.getString(R.string.cancelFileUploadTitle),
            message = if (fileUploadType == FileUploadType.ASSIGNMENT) resources.getString(R.string.cancelSubmissionDialogMessage) else resources.getString(R.string.cancelFileUploadMessage)
        )))
    }
}