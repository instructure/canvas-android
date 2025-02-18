/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.displayText
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.utils.AttachmentCardItem
import com.instructure.pandautils.features.inbox.utils.AttachmentStatus
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.debounce
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.launchWithLoadingDelay
import com.instructure.pandautils.utils.orDefault
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
    savedStateHandle: SavedStateHandle,
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

    private val options = savedStateHandle.get<InboxComposeOptions>(InboxComposeOptions.COMPOSE_PARAMETERS)

    private val debouncedInnerSearch = debounce<String>(waitMs = 200, coroutineScope = viewModelScope) { searchQuery ->
        val contextId = uiState.value.selectContextUiState.selectedCanvasContext?.contextId ?: return@debounce
        val recipients = getRecipientList(
            searchQuery,
            contextId
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
        loadRecipients(
            searchQuery,
            uiState.value.selectContextUiState.selectedCanvasContext ?: return@debounce,
            uiState.value.recipientPickerUiState.selectedRole
        )
    }

    init {
        loadContexts()
        if (options != null) {
            initFromOptions(options)
        }
        loadSignature()
    }

    private fun initFromOptions(options: InboxComposeOptions?) {
        options?.let {
            val context = CanvasContext.fromContextCode(options.defaultValues.contextCode, options.defaultValues.contextName)
            context?.let { loadRecipients(
                "",
                it,
                uiState.value.recipientPickerUiState.selectedRole,
                false
            ) }
            _uiState.update {
                it.copy(
                    inboxComposeMode = options.mode,
                    previousMessages = options.previousMessages,
                    selectContextUiState = it.selectContextUiState.copy(
                        selectedCanvasContext = context
                    ),
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        selectedRecipients = options.defaultValues.recipients,
                    ),
                    inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                        selectedValues = options.defaultValues.recipients,
                        enabled = options.disabledFields.isRecipientsDisabled.not(),
                    ),
                    disabledFields = options.disabledFields,
                    hiddenFields = options.hiddenFields,
                    sendIndividual = options.defaultValues.sendIndividual,
                    subject = TextFieldValue(options.defaultValues.subject),
                    body = TextFieldValue(options.defaultValues.body),
                    attachments = options.defaultValues.attachments.map { attachment -> AttachmentCardItem(attachment, AttachmentStatus.UPLOADED, false) },
                    hiddenBodyMessage = options.hiddenBodyMessage,
                )
            }
            context?.let {
                viewModelScope.launch {
                    if (!options.autoSelectRecipientsFromRoles.isNullOrEmpty()) {
                        _uiState.update {
                            it.copy(
                                inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                                    isLoading = true
                                )
                            )
                        }

                        val recipients = getRecipientList("", context.contextId, false).dataOrNull.orEmpty()
                        val roleRecipients = groupRecipientList(context, recipients)
                        val selectedRecipients = mutableListOf<Recipient>()
                        options.autoSelectRecipientsFromRoles?.forEach { role ->
                            roleRecipients[role]?.let { selectedRecipients.addAll(it) }
                        }

                        _uiState.update {
                            it.copy(
                                recipientPickerUiState = it.recipientPickerUiState.copy(
                                    selectedRecipients = selectedRecipients.distinct(),
                                ),
                                inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                                    selectedValues = selectedRecipients.distinct(),
                                    isLoading = false
                                )
                            )
                        }
                    }
                }
            }

        }
    }

    fun updateAttachments(uuid: UUID?, workInfo: WorkInfo) {
        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
            viewModelScope.launch {
                uuid?.let { uuid ->
                    val attachmentEntities = attachmentDao.findByParentId(uuid.toString())
                    val status = workInfo.state.toAttachmentCardStatus()
                    attachmentEntities?.let { attachmentList ->
                        _uiState.update { it.copy(attachments = it.attachments + attachmentList.map { AttachmentCardItem(it.toApiModel(), status, false) }) }
                        attachmentDao.deleteAll(attachmentList)
                    } ?: sendScreenResult(context.getString(R.string.errorUploadingFile))
                } ?: sendScreenResult(context.getString(R.string.errorUploadingFile))

            }
        }
    }

    fun handleAction(action: InboxComposeActionHandler) {
        when (action) {
            is InboxComposeActionHandler.CancelDismissDialog -> {
                cancelDismissDialog(action.isShow)
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
                when(uiState.value.inboxComposeMode) {
                    InboxComposeOptionsMode.NEW_MESSAGE -> createConversation()
                    InboxComposeOptionsMode.REPLY -> createMessage()
                    InboxComposeOptionsMode.REPLY_ALL -> createMessage()
                    InboxComposeOptionsMode.FORWARD -> createMessage()
                }
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
            is InboxComposeActionHandler.HideSearchResults -> {
                _uiState.update { it.copy(
                    inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                        isShowResults = false,
                    )
                ) }
            }

            is InboxComposeActionHandler.UrlSelected -> {
                viewModelScope.launch {
                    _events.send(InboxComposeViewModelAction.UrlSelected(action.url))
                }
            }
        }
    }

    fun handleAction(action: ContextPickerActionHandler) {
        when (action) {
            is ContextPickerActionHandler.DoneClicked -> {
                closeContextPicker()
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
                    screenOption = InboxComposeScreenOptions.None,
                    inlineRecipientSelectorState = it.inlineRecipientSelectorState.copy(
                        selectedValues = emptyList(),
                    )
                ) }

                loadRecipients("", action.context, uiState.value.recipientPickerUiState.selectedRole)
            }
        }
    }

    fun handleAction(action: RecipientPickerActionHandler) {
        when (action) {
            is RecipientPickerActionHandler.RefreshCalled -> {
                loadRecipients(
                    uiState.value.recipientPickerUiState.searchValue.text,
                    uiState.value.selectContextUiState.selectedCanvasContext ?: return,
                    uiState.value.recipientPickerUiState.selectedRole,
                    forceRefresh = true
                )
            }
            is RecipientPickerActionHandler.DoneClicked -> {
                recipientPickerDone()
            }
            is RecipientPickerActionHandler.RecipientBackClicked -> {
                recipientPickerBackToRoles()
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

    fun cancelDismissDialog(isShow: Boolean) {
        _uiState.update { it.copy(
            enableCustomBackHandler = isShow.not(),
            showConfirmationDialog = isShow
        ) }
    }

    fun closeContextPicker() {
        _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.None) }
    }

    fun recipientPickerBackToRoles() {
        _uiState.update { uiState.value.copy(
            recipientPickerUiState = it.recipientPickerUiState.copy(
                selectedRole = null,
            )
        ) }
        _uiState.update { uiState.value.copy(
            recipientPickerUiState = it.recipientPickerUiState.copy(
                screenOption = RecipientPickerScreenOption.Roles,
                selectedRole = null,
                allRecipientsToShow = getAllRecipients()
            )
        ) }

        resetSearchFieldValue()
        resetSearchFieldResults()
    }

    fun recipientPickerDone() {
        _uiState.update { uiState.value.copy(
            screenOption = InboxComposeScreenOptions.None,
            recipientPickerUiState = it.recipientPickerUiState.copy(
                screenOption = RecipientPickerScreenOption.Roles,
                selectedRole = null,
                searchValue = TextFieldValue(""),
            ),
        ) }

        resetSearchFieldValue()
        resetSearchFieldResults()
    }

    private fun loadContexts(forceRefresh: Boolean = false) {

        viewModelScope.launch {
            val courses = inboxComposeRepository.getCourses(forceRefresh).dataOrNull.orEmpty()
            val groups = inboxComposeRepository.getGroups(forceRefresh).dataOrNull.orEmpty()

            _uiState.update { it.copy(
                selectContextUiState = it.selectContextUiState.copy(
                    canvasContexts = courses + groups
                )
            ) }
        }
    }

    private fun loadSignature() {
        viewModelScope.launchWithLoadingDelay(onLoadingStart = {
            _uiState.update { it.copy(signatureLoading = true) }
        }, onLoadingEnd = {
            _uiState.update { it.copy(signatureLoading = false) }
        }) {
            val signature = inboxComposeRepository.getInboxSignature()
            if (signature.isNotBlank()) {
                val signatureFooter = "\n\n---\n$signature"
                _uiState.update { it.copy(
                    body = TextFieldValue(it.body.text.plus(signatureFooter))
                ) }
            }
        }
    }

    private fun loadRecipients(searchQuery: String, context: CanvasContext, selectedRole: EnrollmentType?, forceRefresh: Boolean = false) {
        viewModelScope.launch {

            canSendToAll = inboxComposeRepository.canSendToAll(context).dataOrNull.orDefault()

            var recipients: List<Recipient> = emptyList()
            var newState: ScreenState = ScreenState.Empty
            try {
                val contextId = context.contextId + getEnrollmentTypeString(selectedRole)
                recipients = getRecipientList(searchQuery, contextId, forceRefresh).dataOrThrow
                if (recipients.isEmpty().not()) { newState = ScreenState.Data }
            } catch (e: Exception) {
                newState = ScreenState.Error
            }

            val roleRecipients = groupRecipientList(context, recipients)

            val recipientsToShow =
                if (searchQuery.isEmpty() && selectedRole != null) {
                    roleRecipients[selectedRole] ?: emptyList()
                } else {
                    recipients
                }
            val allRecipient = if (searchQuery.isEmpty()) getAllRecipients(roleRecipients = roleRecipients) else null
            _uiState.update { it.copy(
                recipientPickerUiState = it.recipientPickerUiState.copy(
                    recipientsByRole = roleRecipients,
                    screenState = newState,
                    recipientsToShow = recipientsToShow,
                    allRecipientsToShow = allRecipient
                )
            ) }
        }
    }

    private fun groupRecipientList(context: CanvasContext, recipients: List<Recipient>): EnumMap<EnrollmentType, List<Recipient>> {
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

        return roleRecipients
    }

    private suspend fun getRecipientList(searchQuery: String, contextId: String, forceRefresh: Boolean = false): DataResult<List<Recipient>> {
        return inboxComposeRepository.getRecipients(searchQuery, contextId, forceRefresh)
    }

    private fun createConversation() {
        uiState.value.selectContextUiState.selectedCanvasContext?.let { canvasContext ->
            viewModelScope.launch {
                _uiState.update { uiState.value.copy(screenState = ScreenState.Loading) }

                try {
                    inboxComposeRepository.createConversation(
                        recipients = uiState.value.recipientPickerUiState.selectedRecipients,
                        subject = uiState.value.subject.text,
                        message = getMessageBody(),
                        context = canvasContext,
                        attachments = uiState.value.attachments.map { it.attachment },
                        isIndividual = uiState.value.isSendIndividualEnabled
                    ).dataOrThrow

                    _uiState.update { it.copy(enableCustomBackHandler = false) }
                    _events.send(InboxComposeViewModelAction.UpdateParentFragment)

                    sendScreenResult(context.getString(R.string.messageSentSuccessfully))

                    closeFragment()

                } catch (e: IllegalStateException) {
                    sendScreenResult(context.getString(R.string.failed_to_send_message))
                } finally {
                    _uiState.update { uiState.value.copy(screenState = ScreenState.Data) }
                }
            }
        }
    }

    private fun createMessage() {
        uiState.value.selectContextUiState.selectedCanvasContext?.let { canvasContext ->
            viewModelScope.launch {
                _uiState.update { uiState.value.copy(screenState = ScreenState.Loading) }

                try {
                    inboxComposeRepository.addMessage(
                        conversationId = uiState.value.previousMessages?.conversation?.id ?: 0,
                        recipients = uiState.value.recipientPickerUiState.selectedRecipients,
                        message = getMessageBody(),
                        includedMessages = uiState.value.previousMessages?.previousMessages ?: emptyList(),
                        attachments = uiState.value.attachments.map { it.attachment },
                        context = canvasContext
                    ).dataOrThrow

                    _uiState.update { it.copy(enableCustomBackHandler = false) }
                    _events.send(InboxComposeViewModelAction.UpdateParentFragment)

                    sendScreenResult(context.getString(R.string.messageSentSuccessfully))

                    closeFragment()

                } catch (e: IllegalStateException) {
                    sendScreenResult(context.getString(R.string.failed_to_send_message))
                } finally {
                    _uiState.update { uiState.value.copy(screenState = ScreenState.Data) }
                }
            }
        }
    }

    private fun getMessageBody(): String {
        return if (uiState.value.hiddenBodyMessage.isNullOrBlank()) {
            uiState.value.body.text
        } else {
            "${uiState.value.body.text}\n\n${uiState.value.hiddenBodyMessage}"
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

    private fun closeFragment() {
        viewModelScope.launch {
            _events.send(InboxComposeViewModelAction.NavigateBack)
        }
    }

    private fun resetSearchFieldValue() {
        _uiState.update { it.copy(
            recipientPickerUiState = it.recipientPickerUiState.copy(
                searchValue = TextFieldValue("")
            )
        ) }
    }

    private fun resetSearchFieldResults() {
        loadRecipients("", uiState.value.selectContextUiState.selectedCanvasContext ?: return, null)
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