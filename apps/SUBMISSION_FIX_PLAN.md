# MBL-19546: Submission Flow Fix Implementation Plan

## Executive Summary

This document outlines the comprehensive plan to fix the critical submission flow bugs that cause files to be uploaded to Canvas but never actually submitted to assignments.

**Issue**: Files appear in student's Submissions folder but not in SpeedGrader, while the app shows "Submission Success" message.

**Root Causes**:
1. Race condition: UI closes before submission completes
2. False success indication when database is empty
3. No verification that final submission API call succeeded
4. Missing safeguards for process death scenarios

---

## Phase 1: Implement Submission State Machine (High Priority)

### Goal
Replace binary success/failure with granular state tracking throughout the submission lifecycle.

### Implementation

#### 1.1 Create Submission State Enum

**File**: `libs/pandautils/src/main/java/com/instructure/pandautils/room/studentdb/entities/CreateSubmissionEntity.kt`

```kotlin
enum class SubmissionState {
    QUEUED,              // Worker enqueued, not started
    UPLOADING_FILES,     // Files being uploaded to Canvas storage
    SUBMITTING,          // Making final submission API call
    VERIFYING,           // Verifying submission via API
    COMPLETED,           // Confirmed by Canvas API
    FAILED,              // Permanent failure
    RETRYING             // Temporary failure, will retry
}
```

**Add to CreateSubmissionEntity**:
```kotlin
@Entity(tableName = "createSubmissionEntity")
data class CreateSubmissionEntity(
    // ... existing fields ...

    @ColumnInfo(name = "submission_state")
    var submissionState: SubmissionState = SubmissionState.QUEUED,

    @ColumnInfo(name = "state_updated_at")
    var stateUpdatedAt: Date = Date(),

    @ColumnInfo(name = "retry_count")
    var retryCount: Int = 0,

    @ColumnInfo(name = "last_error_message")
    var lastErrorMessage: String? = null,

    @ColumnInfo(name = "canvas_submission_id")
    var canvasSubmissionId: Long? = null  // ID returned by Canvas API
)
```

#### 1.2 Add State Update Methods to DAO

**File**: `libs/pandautils/src/main/java/com/instructure/pandautils/room/studentdb/entities/daos/CreateSubmissionDao.kt`

```kotlin
@Query("UPDATE createSubmissionEntity SET submission_state = :state, state_updated_at = :timestamp WHERE id = :id")
suspend fun updateSubmissionState(id: Long, state: SubmissionState, timestamp: Date = Date())

@Query("UPDATE createSubmissionEntity SET retry_count = retry_count + 1, last_error_message = :error WHERE id = :id")
suspend fun incrementRetryCount(id: Long, error: String?)

@Query("UPDATE createSubmissionEntity SET canvas_submission_id = :submissionId WHERE id = :id")
suspend fun setCanvasSubmissionId(id: Long, submissionId: Long)

@Query("SELECT * FROM createSubmissionEntity WHERE submission_state IN (:states)")
suspend fun findSubmissionsByState(states: List<SubmissionState>): List<CreateSubmissionEntity>
```

#### 1.3 Database Migration

**File**: `libs/pandautils/src/main/java/com/instructure/pandautils/room/studentdb/StudentDb.kt`

```kotlin
val MIGRATION_XX_XX = object : Migration(XX, XX+1) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE createSubmissionEntity
            ADD COLUMN submission_state TEXT NOT NULL DEFAULT 'QUEUED'
        """)
        database.execSQL("""
            ALTER TABLE createSubmissionEntity
            ADD COLUMN state_updated_at INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE createSubmissionEntity
            ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0
        """)
        database.execSQL("""
            ALTER TABLE createSubmissionEntity
            ADD COLUMN last_error_message TEXT
        """)
        database.execSQL("""
            ALTER TABLE createSubmissionEntity
            ADD COLUMN canvas_submission_id INTEGER
        """)
    }
}
```

---

## Phase 2: Refactor SubmissionWorker with Proper Error Handling

### Goal
Implement proper Result.retry() vs Result.failure() distinction and state tracking.

### Implementation

#### 2.1 Update SubmissionWorker.kt

**File**: `student/src/main/java/com/instructure/student/mobius/common/ui/SubmissionWorker.kt`

```kotlin
override suspend fun doWork(): Result {
    return try {
        val action = inputData.getString(Const.ACTION) ?: return Result.failure()

        if (!inputData.hasKeyWithValueOfType<Long>(Const.SUBMISSION_ID)) {
            return Result.failure()
        }

        val dbSubmissionId = inputData.getLong(Const.SUBMISSION_ID, 0)
        val submission = createSubmissionDao.findSubmissionById(dbSubmissionId)
            ?: return Result.success() // Already deleted = already succeeded

        // Update state to indicate work started
        createSubmissionDao.updateSubmissionState(
            submission.id,
            SubmissionState.UPLOADING_FILES
        )

        when (SubmissionWorkerAction.valueOf(action)) {
            SubmissionWorkerAction.FILE_ENTRY -> uploadFileSubmissionWithRetry(submission)
            SubmissionWorkerAction.MEDIA_ENTRY -> uploadMediaWithRetry(submission)
            SubmissionWorkerAction.TEXT_ENTRY -> uploadTextWithRetry(submission)
            SubmissionWorkerAction.URL_ENTRY -> uploadUrlWithRetry(submission, false)
            SubmissionWorkerAction.STUDIO_ENTRY -> uploadUrlWithRetry(submission, true)
            SubmissionWorkerAction.STUDENT_ANNOTATION -> uploadStudentAnnotationWithRetry(submission)
            SubmissionWorkerAction.COMMENT_ENTRY -> uploadComment()
        }

    } catch (e: Exception) {
        handleWorkerException(e)
    }
}

private suspend fun uploadFileSubmissionWithRetry(submission: CreateSubmissionEntity): Result {
    return try {
        // Step 1: Upload files
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.UPLOADING_FILES)
        showProgressNotification(submission.assignmentName, submission.id)

        val (completed, pending) = createFileSubmissionDao
            .findFilesForSubmissionId(submission.id)
            .partition { it.attachmentId != null }

        val uploadedAttachmentIds = uploadFiles(submission, completed.size, pending)
            ?: return Result.retry() // Network error during upload

        // Step 2: Submit to assignment
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.SUBMITTING)
        showProgressNotification(submission.assignmentName, submission.id, alertOnlyOnce = true)

        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = apiPrefs.overrideDomains[submission.canvasContext.id],
            shouldLoginOnTokenError = false
        )

        val submissionResult = submissionApi.postSubmissionAttachments(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            attachmentIds,
            params
        )

        // Step 3: Verify submission was created
        return handleSubmissionResultWithVerification(submissionResult, submission)

    } catch (e: IOException) {
        // Network error - retry
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.RETRYING)
        createSubmissionDao.incrementRetryCount(submission.id, e.message)
        Result.retry()
    } catch (e: HttpException) {
        when (e.code()) {
            408, 429, 500, 502, 503, 504 -> {
                // Retryable HTTP errors
                createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.RETRYING)
                createSubmissionDao.incrementRetryCount(submission.id, "HTTP ${e.code()}: ${e.message()}")
                Result.retry()
            }
            else -> {
                // Permanent failure
                createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.FAILED)
                createSubmissionDao.setSubmissionError(true, submission.id)
                createSubmissionDao.incrementRetryCount(submission.id, "HTTP ${e.code()}: ${e.message()}")
                showErrorNotification(context, submission)
                Result.failure()
            }
        }
    }
}

private suspend fun handleSubmissionResultWithVerification(
    result: DataResult<Submission>,
    submission: CreateSubmissionEntity
): Result {
    return result.dataOrNull?.let { canvasSubmission ->
        // Update state to verifying
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.VERIFYING)

        // Store Canvas submission ID
        createSubmissionDao.setCanvasSubmissionId(submission.id, canvasSubmission.id)

        // Verify the submission actually exists on Canvas
        val verificationResult = verifySubmissionOnCanvas(
            submission.canvasContext.id,
            submission.assignmentId,
            canvasSubmission.id
        )

        if (verificationResult) {
            // Success! Mark as completed
            createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.COMPLETED)

            // Delete from database (only after verified)
            deleteSubmissionsForAssignment(submission.assignmentId)

            showCompleteNotification(context, submission, canvasSubmission.late)

            analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SUCCEEDED, Bundle().apply {
                putString(AnalyticsParamConstants.ATTEMPT, submission.attempt.toString())
            })

            Result.success()
        } else {
            // Verification failed - retry
            createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.RETRYING)
            createSubmissionDao.incrementRetryCount(submission.id, "Verification failed")
            Result.retry()
        }

    } ?: run {
        // API call failed
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.RETRYING)
        createSubmissionDao.incrementRetryCount(submission.id, "Submission API returned null")
        Result.retry()
    }
}

private suspend fun verifySubmissionOnCanvas(
    courseId: Long,
    assignmentId: Long,
    submissionId: Long
): Boolean {
    return try {
        val params = RestParams(shouldLoginOnTokenError = false)
        val submission = submissionApi.getSubmission(courseId, assignmentId, apiPrefs.user!!.id, params)

        // Verify the submission ID matches and is in a valid state
        submission.dataOrNull?.let {
            it.id == submissionId && it.workflowState != "unsubmitted"
        } ?: false

    } catch (e: Exception) {
        Log.e("SubmissionWorker", "Verification failed", e)
        false
    }
}

private fun handleWorkerException(e: Exception): Result {
    return when (e) {
        is IOException -> {
            Log.e("SubmissionWorker", "Network error", e)
            Result.retry()
        }
        is CancellationException -> {
            Log.w("SubmissionWorker", "Work cancelled", e)
            Result.failure()
        }
        else -> {
            Log.e("SubmissionWorker", "Unexpected error", e)
            Result.failure()
        }
    }
}
```

#### 2.2 Update SubmissionHelper with Backoff Policy

**File**: `student/src/main/java/com/instructure/student/mobius/common/ui/SubmissionHelper.kt`

```kotlin
override fun startSubmissionWorker(action: SubmissionWorkerAction, submissionId: Long?, commentId: Long?) {
    val data = Data.Builder()
    data.putString(Const.ACTION, action.name)

    submissionId?.let {
        data.putLong(Const.SUBMISSION_ID, it)
    }
    commentId?.let {
        data.putLong(Const.ID, it)
    }

    val submissionWork = OneTimeWorkRequest.Builder(SubmissionWorker::class.java)
        .setInputData(data.build())
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            30,  // 30 seconds initial delay
            TimeUnit.SECONDS
        )
        .addTag("SubmissionWorker")
        .addTag("submission_$submissionId")
        .build()

    // Use unique work to prevent duplicate submissions
    workManager.enqueueUniqueWork(
        "submission_${submissionId}_${action.name}",
        ExistingWorkPolicy.REPLACE,  // Cancel previous attempts
        submissionWork
    )
}
```

---

## Phase 3: Fix UI Flow and Add Real-Time Status

### Goal
Don't close submission UI until work is confirmed completed, show real-time progress.

### Implementation

#### 3.1 Update PickerSubmissionUploadEffectHandler

**File**: `student/src/main/java/com/instructure/student/mobius/assignmentDetails/submission/picker/PickerSubmissionUploadEffectHandler.kt`

```kotlin
private fun handleSubmit(model: PickerSubmissionUploadModel) {
    when (model.mode) {
        FileSubmission -> {
            val submissionId = submissionHelper.startFileSubmission(
                canvasContext = model.canvasContext,
                assignmentId = model.assignmentId,
                assignmentName = model.assignmentName,
                assignmentGroupCategoryId = model.assignmentGroupCategoryId,
                files = ArrayList(model.files),
                attempt = model.attemptId ?: 1L
            )

            // DON'T close view immediately!
            // Navigate to upload status screen instead
            consumer.accept(
                PickerSubmissionUploadEvent.NavigateToUploadStatus(submissionId)
            )
        }
        // ... other modes ...
    }
}
```

#### 3.2 Update BaseSubmissionHelper to Return Submission ID

**File**: `libs/pandautils/src/main/java/com/instructure/pandautils/features/submission/BaseSubmissionHelper.kt`

```kotlin
fun startFileSubmission(
    canvasContext: CanvasContext,
    assignmentId: Long,
    assignmentName: String?,
    assignmentGroupCategoryId: Long = 0,
    files: ArrayList<FileSubmitObject>,
    deleteBySubmissionTypeFilter: Assignment.SubmissionType? = null,
    attempt: Long = 1L
): Long {  // Return submission ID instead of Unit
    files.ifEmpty { return -1 }

    val dbSubmissionId = runBlocking {
        insertNewSubmission(assignmentId, files, deleteBySubmissionTypeFilter) {
            val entity = CreateSubmissionEntity(
                assignmentName = assignmentName,
                assignmentId = assignmentId,
                assignmentGroupCategoryId = assignmentGroupCategoryId,
                canvasContext = canvasContext,
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
                userId = getUserId(),
                lastActivityDate = Date(),
                fileCount = files.size,
                attempt = attempt,
                submissionState = SubmissionState.QUEUED  // Set initial state
            )
            it.submissionDao().insert(entity)
        }
    }

    startSubmissionWorker(SubmissionWorkerAction.FILE_ENTRY, submissionId = dbSubmissionId)

    return dbSubmissionId  // Return ID for UI tracking
}
```

#### 3.3 Add Navigation Event

**File**: `student/src/main/java/com/instructure/student/mobius/assignmentDetails/submission/picker/PickerSubmissionUploadModels.kt`

```kotlin
sealed class PickerSubmissionUploadEvent {
    // ... existing events ...
    data class NavigateToUploadStatus(val submissionId: Long) : PickerSubmissionUploadEvent()
}
```

#### 3.4 Update UploadStatusSubmissionPresenter Logic

**File**: `student/src/main/java/com/instructure/student/mobius/assignmentDetails/submission/file/UploadStatusSubmissionPresenter.kt`

```kotlin
override fun present(
    model: UploadStatusSubmissionModel,
    context: Context
): UploadStatusSubmissionViewState {
    return when {
        model.submissionState == SubmissionState.COMPLETED -> presentSuccess(context)
        model.submissionState == SubmissionState.FAILED -> presentFailed(model, context)
        model.isLoading -> UploadStatusSubmissionViewState.Loading
        model.submissionState in listOf(
            SubmissionState.QUEUED,
            SubmissionState.UPLOADING_FILES,
            SubmissionState.SUBMITTING,
            SubmissionState.VERIFYING,
            SubmissionState.RETRYING
        ) -> presentInProgress(model, context)
        else -> presentSuccess(context)  // Fallback for backward compatibility
    }
}

private fun presentInProgress(
    model: UploadStatusSubmissionModel,
    context: Context
): UploadStatusSubmissionViewState {
    val stateMessage = when (model.submissionState) {
        SubmissionState.QUEUED -> context.getString(R.string.submissionQueued)
        SubmissionState.UPLOADING_FILES -> context.getString(R.string.submissionUploadingFiles)
        SubmissionState.SUBMITTING -> context.getString(R.string.submissionSubmitting)
        SubmissionState.VERIFYING -> context.getString(R.string.submissionVerifying)
        SubmissionState.RETRYING -> context.getString(
            R.string.submissionRetrying,
            model.retryCount
        )
        else -> context.getString(R.string.submissionInProgress)
    }

    // ... existing progress calculation ...

    return UploadStatusSubmissionViewState.InProgress(
        title = context.getString(R.string.assignmentSubmissionUpload, model.assignmentName),
        subtitle = stateMessage,
        // ... existing fields ...
    )
}
```

---

## Phase 4: Add Submission Status Monitoring

### Goal
Allow users to check submission status from assignment details screen.

### Implementation

#### 4.1 Add Status Indicator to Assignment Details

**File**: `libs/pandautils/src/main/java/com/instructure/pandautils/features/assignments/details/AssignmentDetailsViewModel.kt`

```kotlin
private fun observeSubmissionStatus() {
    viewModelScope.launch {
        studentDb.submissionDao()
            .findSubmissionsByAssignmentIdLiveData(assignmentId, apiPrefs.user!!.id)
            .asFlow()
            .collect { submissions ->
                val activeSubmission = submissions.firstOrNull {
                    it.submissionState != SubmissionState.COMPLETED &&
                    it.submissionState != SubmissionState.FAILED
                }

                _activeSubmission.value = activeSubmission

                // Update grade cell to show status
                if (activeSubmission != null) {
                    updateGradeCellForActiveSubmission(activeSubmission)
                }
            }
    }
}

private fun updateGradeCellForActiveSubmission(submission: CreateSubmissionEntity) {
    val statusText = when (submission.submissionState) {
        SubmissionState.QUEUED -> context.getString(R.string.submissionQueued)
        SubmissionState.UPLOADING_FILES -> {
            val progress = calculateProgress(submission)
            context.getString(R.string.uploadingProgress, progress)
        }
        SubmissionState.SUBMITTING -> context.getString(R.string.submitting)
        SubmissionState.VERIFYING -> context.getString(R.string.verifying)
        SubmissionState.RETRYING -> context.getString(
            R.string.retryingAttempt,
            submission.retryCount
        )
        else -> ""
    }

    // Update data binding
    data.value?.gradeCellData?.submissionStatusText = statusText
    data.value?.notifyPropertyChanged(BR.gradeCellData)
}
```

#### 4.2 Update StudentAssignmentDetailsSubmissionHandler

**File**: `student/src/main/java/com/instructure/student/features/assignments/details/StudentAssignmentDetailsSubmissionHandler.kt`

```kotlin
private fun setupObserver(
    context: Context,
    resources: Resources,
    data: MutableLiveData<AssignmentDetailsViewData>,
    refreshAssignment: () -> Unit,
) {
    submissionObserver = Observer<List<CreateSubmissionEntity>> { submissions ->
        val submission = submissions.lastOrNull()
        lastSubmissionAssignmentId = submission?.assignmentId
        lastSubmissionSubmissionType = submission?.submissionType
        lastSubmissionIsDraft = submission?.isDraft ?: false
        lastSubmissionEntry = submission?.submissionEntry

        val attempts = data.value?.attempts
        submission?.let { dbSubmission ->
            val isDraft = dbSubmission.isDraft
            data.value?.hasDraft = isDraft
            data.value?.notifyPropertyChanged(BR.hasDraft)

            // Only show uploading state if actively in progress
            val isActivelyUploading = dbSubmission.submissionState in listOf(
                SubmissionState.QUEUED,
                SubmissionState.UPLOADING_FILES,
                SubmissionState.SUBMITTING,
                SubmissionState.VERIFYING,
                SubmissionState.RETRYING
            )

            val dateString = (dbSubmission.lastActivityDate?.toInstant()?.toEpochMilli()?.let { Date(it) } ?: Date()).toFormattedString()

            if (!isDraft && isActivelyUploading && !isUploading) {
                isUploading = true
                data.value?.attempts = attempts?.toMutableList()?.apply {
                    add(
                        0, AssignmentDetailsAttemptItemViewModel(
                            AssignmentDetailsAttemptViewData(
                                resources.getString(R.string.attempt, attempts.size + 1),
                                dateString,
                                isUploading = true
                            )
                        )
                    )
                }.orEmpty()
                data.value?.notifyPropertyChanged(BR.attempts)
            }

            // Handle errors
            if (isUploading && dbSubmission.submissionState == SubmissionState.FAILED) {
                data.value?.attempts = attempts?.toMutableList()?.apply {
                    if (isNotEmpty()) removeAt(0)
                    add(0, AssignmentDetailsAttemptItemViewModel(
                        AssignmentDetailsAttemptViewData(
                            resources.getString(R.string.attempt, attempts.size),
                            dateString,
                            isFailed = true
                        )
                    ))
                }.orEmpty()
                data.value?.notifyPropertyChanged(BR.attempts)
            }

            // Handle completion
            if (isUploading && dbSubmission.submissionState == SubmissionState.COMPLETED) {
                isUploading = false
                refreshAssignment()
                context.toast(R.string.submissionSuccessTitle)
            }

        } ?: run {
            // No submission in DB - only show success if was previously uploading
            if (isUploading) {
                isUploading = false
                refreshAssignment()
                context.toast(R.string.submissionSuccessTitle)
            }
        }
    }
}
```

---

## Phase 5: Add String Resources

### Implementation

**File**: `student/src/main/res/values/strings.xml`

```xml
<!-- Submission State Messages -->
<string name="submissionQueued">Queued for upload</string>
<string name="submissionUploadingFiles">Uploading files…</string>
<string name="submissionSubmitting">Submitting to assignment…</string>
<string name="submissionVerifying">Verifying submission…</string>
<string name="submissionRetrying">Retrying (attempt %1$d)…</string>
<string name="submissionInProgress">Submission in progress…</string>
<string name="uploadingProgress">Uploading: %1$d%%</string>
<string name="submitting">Submitting…</string>
<string name="verifying">Verifying…</string>
<string name="retryingAttempt">Retrying (attempt %1$d)</string>
```

---

## Phase 6: Testing Plan

### 6.1 Unit Tests

**File**: `student/src/test/java/com/instructure/student/test/SubmissionWorkerTest.kt`

```kotlin
@Test
fun `uploadFileSubmission handles network error with retry`() = runTest {
    // Arrange
    coEvery {
        fileUploadManager.uploadFile(any(), any())
    } throws IOException("Network error")

    // Act
    val result = worker.doWork()

    // Assert
    assertThat(result).isInstanceOf(Result.Retry::class.java)

    // Verify state updated
    val submission = createSubmissionDao.findSubmissionById(submissionId)
    assertThat(submission?.submissionState).isEqualTo(SubmissionState.RETRYING)
    assertThat(submission?.retryCount).isEqualTo(1)
}

@Test
fun `uploadFileSubmission verifies submission on Canvas`() = runTest {
    // Arrange
    val mockSubmission = Submission(id = 12345, workflowState = "submitted")
    coEvery {
        submissionApi.postSubmissionAttachments(any(), any(), any(), any(), any())
    } returns DataResult.Success(mockSubmission)

    coEvery {
        submissionApi.getSubmission(any(), any(), any(), any())
    } returns DataResult.Success(mockSubmission)

    // Act
    val result = worker.doWork()

    // Assert
    assertThat(result).isInstanceOf(Result.Success::class.java)

    // Verify verification was called
    coVerify { submissionApi.getSubmission(any(), any(), any(), any()) }

    // Verify state progression
    val states = stateHistory.map { it.submissionState }
    assertThat(states).containsExactly(
        SubmissionState.UPLOADING_FILES,
        SubmissionState.SUBMITTING,
        SubmissionState.VERIFYING,
        SubmissionState.COMPLETED
    )
}
```

### 6.2 Integration Tests

**File**: `student/src/androidTest/java/com/instructure/student/ui/SubmissionFlowTest.kt`

```kotlin
@Test
fun testSubmissionFlowWithProcessDeath() {
    // Navigate to assignment
    assignmentListPage.clickAssignment(assignment)

    // Start submission
    assignmentDetailsPage.clickSubmit()
    submissionPickerPage.selectFile(testFile)
    submissionPickerPage.clickSubmit()

    // Verify upload status screen shown
    uploadStatusPage.assertVisible()
    uploadStatusPage.assertStatus("Uploading files…")

    // Simulate process death
    device.killProcess()
    device.launchApp()

    // Navigate back to assignment
    dashboardPage.clickAssignments()
    assignmentListPage.clickAssignment(assignment)

    // Verify submission continues/completes
    assignmentDetailsPage.waitForSubmissionComplete(timeout = 60.seconds)
    assignmentDetailsPage.assertGradeShows("Submitted")
}

@Test
fun testSubmissionFlowWithNetworkInterruption() {
    // Start submission
    assignmentDetailsPage.clickSubmit()
    submissionPickerPage.selectFile(largeTestFile)
    submissionPickerPage.clickSubmit()

    // Wait for upload to start
    uploadStatusPage.waitForProgress(10)

    // Disable network
    device.disableNetwork()

    // Should show retrying state
    uploadStatusPage.assertStatus("Retrying")

    // Re-enable network
    device.enableNetwork()

    // Should complete
    uploadStatusPage.waitForSuccess(timeout = 120.seconds)
}
```

### 6.3 Manual Testing Checklist

- [ ] Submit file → Immediately background app → Verify submission completes
- [ ] Submit file → Kill app → Reopen → Verify submission completes
- [ ] Submit file → Airplane mode mid-upload → Turn on WiFi → Verify completion
- [ ] Submit file → Navigate to "Submission & Rubric" → Verify shows progress, not false success
- [ ] Submit file → Check SpeedGrader → Verify file appears
- [ ] Submit multiple files → Verify all files submitted
- [ ] Retry failed submission → Verify succeeds
- [ ] Check assignment list → Verify no "ghost" uploading states
- [ ] Submit while low on battery → Verify completes despite battery saver

---

## Phase 7: Rollout Strategy

### 7.1 Feature Flag

```kotlin
object FeatureFlags {
    const val IMPROVED_SUBMISSION_FLOW = "improved_submission_flow"
}

// In SubmissionHelper
fun startFileSubmission(...) {
    if (featureFlags.isEnabled(FeatureFlags.IMPROVED_SUBMISSION_FLOW)) {
        startFileSubmissionWithStateTracking(...)
    } else {
        startFileSubmissionLegacy(...)
    }
}
```

### 7.2 Gradual Rollout

1. **Week 1**: Internal testing with feature flag enabled
2. **Week 2**: Beta release to 5% of users
3. **Week 3**: Increase to 25% if no issues
4. **Week 4**: 50% rollout
5. **Week 5**: 100% rollout
6. **Week 6**: Remove feature flag and legacy code

### 7.3 Monitoring

Add analytics events:
- `submission_state_changed`: Track state transitions
- `submission_verification_failed`: Track verification failures
- `submission_retry_exceeded`: Track submissions that exceed max retries
- `submission_completed_after_retry`: Track successful recoveries

---

## Phase 8: Documentation Updates

### 8.1 Update CLAUDE.md

Document the new submission flow architecture for future AI assistance.

### 8.2 Update Developer Guide

Add section on submission lifecycle and debugging tips.

---

## Risk Mitigation

### High-Risk Areas

1. **Database migration**: Test on devices with existing submissions
2. **Backward compatibility**: Ensure old submissions still work
3. **Performance**: Monitor battery usage with foreground service
4. **Data usage**: Large file uploads on cellular

### Rollback Plan

1. Disable feature flag immediately
2. Keep legacy code for 2 releases
3. Monitor crash rates and ANRs
4. Have hotfix ready to revert database changes

---

## Timeline Estimate

- **Phase 1**: 3 days (State machine + DB migration)
- **Phase 2**: 5 days (Worker refactor + error handling)
- **Phase 3**: 3 days (UI flow updates)
- **Phase 4**: 2 days (Status monitoring)
- **Phase 5**: 1 day (String resources)
- **Phase 6**: 5 days (Testing)
- **Phase 7**: 2 weeks (Gradual rollout + monitoring)
- **Phase 8**: 1 day (Documentation)

**Total**: ~4-5 weeks for complete implementation and rollout

---

## Success Metrics

- **Zero reports** of files in Submissions folder but not in SpeedGrader
- **95% submission success rate** on first attempt
- **100% success rate** including retries
- **<1% submission failures** requiring manual intervention
- **<5 second** average time from submit click to upload status screen
- **User satisfaction** survey shows improvement in submission confidence

---

## Conclusion

This comprehensive plan addresses all three critical bugs:

1. ✅ **Race condition fixed**: UI shows progress, doesn't close early
2. ✅ **False success eliminated**: Only show success after VERIFIED state
3. ✅ **API verification added**: Confirm submission exists on Canvas

The implementation follows Android best practices, uses proper WorkManager patterns, and includes comprehensive testing and rollout strategy.
