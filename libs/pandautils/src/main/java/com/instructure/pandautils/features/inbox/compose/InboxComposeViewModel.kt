package com.instructure.pandautils.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.displayText
import com.instructure.pandautils.R
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.debounce
import com.instructure.pandautils.utils.isCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.EnumMap
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class InboxComposeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloader: FileDownloader,
    private val inboxComposeRepository: InboxComposeRepository,
    private val attachmentDao: AttachmentDao
): ViewModel() {
    private var canSendToAll = false

    private val _uiState = MutableStateFlow(InboxComposeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<InboxComposeViewModelAction>()
    val events = _events.receiveAsFlow()

    private val debouncedInnerSearch = debounce<String>(waitMs = 200, coroutineScope = viewModelScope) { searchQuery ->
        val recipients = getRecipientList(
            searchQuery,
            uiState.value.selectContextUiState.selectedCanvasContext
                ?: return@debounce
        ).dataOrNull.orEmpty().filterNot { uiState.value.recipientPickerUiState.selectedRecipients.contains(it) }

        _uiState.update {
            it.copy(
                inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                    searchResults = recipients,
                    isShowResults = recipients.isNotEmpty(),
                )
            )
        }
    }

    private val debouncedRecipientScreenSearch = debounce<String>(waitMs = 200, coroutineScope = viewModelScope) { searchQuery ->
        loadRecipients(searchQuery, uiState.value.selectContextUiState.selectedCanvasContext ?: return@debounce)
    }

    init {
        loadContexts()
    }

    fun updateAttachments(uuid: UUID?, workInfo: WorkInfo) {
        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
            viewModelScope.launch {
                uuid?.let { uuid ->
                    val attachmentEntities = attachmentDao.findByParentId(uuid.toString())
                    val status = workInfo.state.toAttachmentCardStatus()
                    attachmentEntities?.let { attachmentList ->
                        _uiState.update { it.copy(attachments = it.attachments + attachmentList.map { AttachmentCardItem(it.toApiModel(), status) }) }
                        attachmentDao.deleteAll(attachmentList)
                    } ?: sendScreenResult(context.getString(R.string.errorUploadingFile))
                } ?: sendScreenResult(context.getString(R.string.errorUploadingFile))

            }
        }
    }

    fun handleAction(action: InboxComposeActionHandler) {
        when (action) {
            is InboxComposeActionHandler.CancelDismissDialog -> {
                _uiState.update { it.copy(
                    showConfirmationDialog = action.isShow
                ) }
            }
            is InboxComposeActionHandler.Close -> {
                viewModelScope.launch {
                    _events.send(InboxComposeViewModelAction.NavigateBack)
                }
            }
            is InboxComposeActionHandler.OpenContextPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.ContextPicker) }
            }
            is InboxComposeActionHandler.RemoveRecipient -> {
                val newRecipients = uiState.value.recipientPickerUiState.selectedRecipients - action.recipient
                updateSelectedRecipients(newRecipients)
            }
            is InboxComposeActionHandler.OpenRecipientPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.RecipientPicker) }
            }
            is InboxComposeActionHandler.BodyChanged -> {
                _uiState.update { it.copy(body = action.body) }
            }
            is InboxComposeActionHandler.SendClicked -> {
                createConversation()
            }
            is InboxComposeActionHandler.SubjectChanged -> {
                _uiState.update { it.copy(subject = action.subject) }
            }
            is InboxComposeActionHandler.SendIndividualChanged -> {
                _uiState.update { it.copy(sendIndividual = action.sendIndividual) }
            }
            is InboxComposeActionHandler.AddAttachmentSelected -> {
                viewModelScope.launch {
                    _events.send(InboxComposeViewModelAction.OpenAttachmentPicker)
                }
            }
            is InboxComposeActionHandler.RemoveAttachment -> {
                _uiState.update { it.copy(attachments = it.attachments - action.attachment) }
            }
            is InboxComposeActionHandler.OpenAttachment -> {
                viewModelScope.launch {
                    fileDownloader.downloadFileToDevice(action.attachment.attachment)
                }
            }
            is InboxComposeActionHandler.AddRecipient -> {
                val newRecipients = uiState.value.recipientPickerUiState.selectedRecipients + action.recipient
                updateSelectedRecipients(newRecipients)
            }
            is InboxComposeActionHandler.SearchRecipientQueryChanged -> {
                _uiState.update { it.copy(
                    inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                        searchQuery = action.searchValue
                    )
                ) }

                if (action.searchValue.text.length > 1) {
                    debouncedInnerSearch(action.searchValue.text)
                } else {
                    _uiState.update {
                        it.copy(
                            inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                                isShowResults = false,
                            )
                        )
                    }
                }
            }

            InboxComposeActionHandler.HideSearchResults -> {
                _uiState.update { it.copy(
                    inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                        isShowResults = false,
                    )
                ) }
            }
        }
    }

    fun handleAction(action: ContextPickerActionHandler) {
        when (action) {
            is ContextPickerActionHandler.DoneClicked -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.None) }
            }
            is ContextPickerActionHandler.RefreshCalled -> {
                loadContexts(forceRefresh = true)
            }
            is ContextPickerActionHandler.ContextClicked -> {
                _uiState.update { it.copy(
                    selectContextUiState = it.selectContextUiState.copy(selectedCanvasContext = action.context),
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        screenOption = RecipientPickerScreenOption.Roles,
                        selectedRole = null,
                        selectedRecipients = emptyList()
                    ),
                    screenOption = InboxComposeScreenOptions.None
                ) }

                loadRecipients("", action.context)
            }
        }
    }

    fun handleAction(action: RecipientPickerActionHandler) {
        when (action) {
            is RecipientPickerActionHandler.RefreshCalled -> {
                loadRecipients(uiState.value.recipientPickerUiState.searchValue.text, uiState.value.selectContextUiState.selectedCanvasContext ?: return, forceRefresh = true)
            }
            is RecipientPickerActionHandler.DoneClicked -> {
                _uiState.update { uiState.value.copy(
                    screenOption = InboxComposeScreenOptions.None,
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        screenOption = RecipientPickerScreenOption.Roles,
                        selectedRole = null
                    )
                ) }

                handleAction(RecipientPickerActionHandler.SearchValueChanged(TextFieldValue("")))
            }
            is RecipientPickerActionHandler.RecipientBackClicked -> {
                _uiState.update { uiState.value.copy(
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        screenOption = RecipientPickerScreenOption.Roles,
                        selectedRole = null
                    )
                ) }

                handleAction(RecipientPickerActionHandler.SearchValueChanged(TextFieldValue("")))
            }
            is RecipientPickerActionHandler.RoleClicked -> {
                _uiState.update {
                    it.copy(
                        recipientPickerUiState = it.recipientPickerUiState.copy(
                            screenOption = RecipientPickerScreenOption.Recipients,
                            selectedRole = action.role,
                            recipientsToShow = it.recipientPickerUiState.recipientsByRole[action.role] ?: emptyList(),
                            allRecipientsToShow = getAllRecipients(action.role)
                        ),
                    )
                }
            }
            is RecipientPickerActionHandler.RecipientClicked -> {
                val newRecipients = if (uiState.value.recipientPickerUiState.selectedRecipients.contains(action.recipient)) {
                    uiState.value.recipientPickerUiState.selectedRecipients - action.recipient
                } else {
                    uiState.value.recipientPickerUiState.selectedRecipients + action.recipient
                }

                updateSelectedRecipients(newRecipients)
            }
            is RecipientPickerActionHandler.SearchValueChanged -> {
                _uiState.update { it.copy(
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        searchValue = action.searchText
                    )
                ) }

                debouncedRecipientScreenSearch(action.searchText.text)
            }
        }
    }

    private fun loadContexts(forceRefresh: Boolean = false) {

        viewModelScope.launch {
            var courses: List<CanvasContext> = emptyList()
            var groups: List<CanvasContext> = emptyList()
            try {
                courses = inboxComposeRepository.getCourses(forceRefresh).dataOrThrow
                groups = inboxComposeRepository.getGroups(forceRefresh).dataOrThrow
            } catch (_: Exception) { }
            _uiState.update { it.copy(
                selectContextUiState = it.selectContextUiState.copy(
                    canvasContexts = courses + groups
                )
            ) }
        }
    }

    private fun loadRecipients(searchQuery: String, context: CanvasContext, forceRefresh: Boolean = false) {
        viewModelScope.launch {

            canSendToAll = inboxComposeRepository.canSendToAll(context).dataOrThrow

            var recipients: List<Recipient> = emptyList()
            var newState: ScreenState = ScreenState.Empty
            try {
                recipients = getRecipientList(searchQuery, context, forceRefresh).dataOrThrow
                if (recipients.isEmpty().not()) { newState = ScreenState.Data }
            } catch (e: Exception) {
                newState = ScreenState.Error
            }
            val roleRecipients: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java)

            recipients.forEach { recipient ->
                if (context.isCourse) {
                    recipient.commonCourses?.let { commonCourse ->
                        commonCourse[context.id.toString()]?.forEach { role ->
                            val enrollmentType = EnrollmentType.safeValueOf(role)
                            if (roleRecipients[enrollmentType] == null || roleRecipients[enrollmentType]?.contains(recipient) == false) {
                                roleRecipients[enrollmentType] = roleRecipients[enrollmentType]?.plus(recipient) ?: listOf(recipient)
                            }
                        }
                    }
                } else {
                    recipient.commonGroups?.let { commonGroup ->
                        commonGroup[context.id.toString()]?.forEach { role ->
                            val enrollmentType = EnrollmentType.safeValueOf(role)
                            if (roleRecipients[enrollmentType] == null || roleRecipients[enrollmentType]?.contains(recipient) == false) {
                                roleRecipients[enrollmentType] = roleRecipients[enrollmentType]?.plus(recipient) ?: listOf(recipient)
                            }
                        }
                    }
                }
            }

            val recipientsToShow =
                if (uiState.value.recipientPickerUiState.searchValue.text.isEmpty() && uiState.value.recipientPickerUiState.selectedRole != null) {
                    roleRecipients[uiState.value.recipientPickerUiState.selectedRole] ?: emptyList()
                } else {
                    recipients
                }
            _uiState.update { it.copy(
                recipientPickerUiState = it.recipientPickerUiState.copy(
                    recipientsByRole = roleRecipients,
                    screenState = newState,
                    recipientsToShow = recipientsToShow,
                    allRecipientsToShow = getAllRecipients(roleRecipients = roleRecipients)
                )
            ) }
        }
    }

    private suspend fun getRecipientList(searchQuery: String, context: CanvasContext, forceRefresh: Boolean = false): DataResult<List<Recipient>> {
        return inboxComposeRepository.getRecipients(searchQuery, context, forceRefresh)
    }

    private fun createConversation() {
        uiState.value.selectContextUiState.selectedCanvasContext?.let { canvasContext ->
            viewModelScope.launch {
                _uiState.update { uiState.value.copy(screenState = ScreenState.Loading) }

                try {
                    inboxComposeRepository.createConversation(
                        recipients = uiState.value.recipientPickerUiState.selectedRecipients,
                        subject = uiState.value.subject.text,
                        message = uiState.value.body.text,
                        context = canvasContext,
                        attachments = uiState.value.attachments.map { it.attachment },
                        isIndividual = uiState.value.sendIndividual
                    ).dataOrThrow

                    _events.send(InboxComposeViewModelAction.UpdateParentFragment)

                    sendScreenResult(context.getString(R.string.messageSentSuccessfully))

                    handleAction(InboxComposeActionHandler.Close)

                } catch (e: IllegalStateException) {
                    sendScreenResult(context.getString(R.string.failed_to_send_message))
                } finally {
                    _uiState.update { uiState.value.copy(screenState = ScreenState.Data) }
                }
            }
        }
    }

    private fun getAllRecipients(selected: EnrollmentType? = null, roleRecipients: EnumMap<EnrollmentType, List<Recipient>>? = null): Recipient? {
        if (!canSendToAll) return null

        val recipientState = uiState.value.recipientPickerUiState
        val selectedContext = uiState.value.selectContextUiState.selectedCanvasContext
        val selectedRole = selected ?: recipientState.selectedRole
        val contextString = selectedContext?.contextId ?: ""
        val recipientsString = getEnrollmentTypeString(selectedRole)
        val allRecipientId = contextString + recipientsString
        val allRecipientName = context.getString(
            R.string.all_recipients_in_selected_context,
            selectedRole?.displayText ?: selectedContext?.name ?: ""
        )
        val allUserCount =
            if (selectedRole == null)
                (roleRecipients ?: recipientState.recipientsByRole).values.flatten().distinct().size
            else
                recipientState.recipientsByRole[selectedRole]?.size ?: 0

        return Recipient(
            stringId = allRecipientId,
            name = allRecipientName,
            userCount = allUserCount,
        )
    }

    private fun getEnrollmentTypeString(enrollmentType: EnrollmentType?): String {
        return when (enrollmentType) {
            EnrollmentType.STUDENTENROLLMENT -> "_students"
            EnrollmentType.TEACHERENROLLMENT -> "_teachers"
            EnrollmentType.TAENROLLMENT -> "_tas"
            EnrollmentType.OBSERVERENROLLMENT -> "_observers"
            else -> ""
        }
    }

    private fun WorkInfo.State.toAttachmentCardStatus(): AttachmentStatus {
        return when (this) {
            WorkInfo.State.SUCCEEDED -> AttachmentStatus.UPLOADED
            WorkInfo.State.FAILED -> AttachmentStatus.FAILED
            WorkInfo.State.ENQUEUED -> AttachmentStatus.UPLOADING
            WorkInfo.State.RUNNING -> AttachmentStatus.UPLOADING
            WorkInfo.State.BLOCKED -> AttachmentStatus.FAILED
            WorkInfo.State.CANCELLED -> AttachmentStatus.FAILED
        }
    }
    
    private fun sendScreenResult(message: String) {
        viewModelScope.launch {
            _events.send(InboxComposeViewModelAction.ShowScreenResult(message))
        }
    }

    private fun updateSelectedRecipients(newRecipientList: List<Recipient>) {
        _uiState.update { it.copy(
            recipientPickerUiState = it.recipientPickerUiState.copy(
                selectedRecipients = newRecipientList
            ),
            inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                selectedValues = newRecipientList,
            )
        ) }
    }
}