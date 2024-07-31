package com.instructure.pandautils.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.isCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class InboxComposeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inboxComposeRepository: InboxComposeRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(
        InboxComposeUiState()
    )
    val uiState = _uiState.asStateFlow()

    private val _contextPickerUiState = MutableStateFlow(
        ContextPickerUiState(
            courses = emptyList(),
            groups = emptyList(),
            selectedContext = null,
            isLoading = true
        )
    )
    val contextPickerUiState = _contextPickerUiState.asStateFlow()

    private val _recipientPickerUiState = MutableStateFlow(
        RecipientPickerUiState(
            recipientsByRole = EnumMap(EnrollmentType::class.java),
            selectedRecipients = emptyList(),
            isLoading = true
        )
    )
    val recipientPickerUiState = _recipientPickerUiState.asStateFlow()

    init {
        loadContexts()
    }

    fun handleAction(action: InboxComposeActionHandler) {
        when (action) {
            is InboxComposeActionHandler.CancelClicked -> {
                context.getFragmentActivity().supportFragmentManager.popBackStack()
            }
            is InboxComposeActionHandler.OpenContextPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.ContextPicker) }
            }
            is InboxComposeActionHandler.RemoveRecipient -> {
                _uiState.update { it.copy(selectedRecipients = it.selectedRecipients - action.recipient) }
                _recipientPickerUiState.update { it.copy(selectedRecipients = it.selectedRecipients - action.recipient) }
            }
            is InboxComposeActionHandler.OpenRecipientPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.RecipientPicker) }
            }
            is InboxComposeActionHandler.BodyChanged -> {
                _uiState.update { it.copy(body = action.body) }
            }
            is InboxComposeActionHandler.SendClicked -> {
                createConversation {
                    context.getFragmentActivity().supportFragmentManager.popBackStack()
                }
            }
            is InboxComposeActionHandler.SubjectChanged -> {
                _uiState.update { it.copy(subject = action.subject) }
            }
            is InboxComposeActionHandler.SendIndividualChanged -> {
                _uiState.update { it.copy(sendIndividual = action.sendIndividual) }
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
                _uiState.update { it.copy(selectedContext = action.context, screenOption = InboxComposeScreenOptions.None) }
                _contextPickerUiState.update { it.copy(selectedContext = action.context) }

                loadRecipients("", action.context)
            }
        }
    }

    fun handleAction(action: RecipientPickerActionHandler) {
        when (action) {
            is RecipientPickerActionHandler.DoneClicked -> {
                _recipientPickerUiState.update { it.copy(screenOption = RecipientPickerScreenOption.Roles) }
                _uiState.update { uiState.value.copy(screenOption = InboxComposeScreenOptions.None) }

                handleAction(RecipientPickerActionHandler.SearchValueChanged(TextFieldValue("")))
            }
            is RecipientPickerActionHandler.RecipientBackClicked -> {
                _recipientPickerUiState.update { it.copy(screenOption = RecipientPickerScreenOption.Roles) }

                handleAction(RecipientPickerActionHandler.SearchValueChanged(TextFieldValue("")))
            }
            is RecipientPickerActionHandler.RoleClicked -> {
                _recipientPickerUiState.update {
                    it.copy(
                        screenOption = RecipientPickerScreenOption.Recipients,
                        selectedRole = action.role,
                        recipientsToShow = recipientPickerUiState.value.recipientsByRole[action.role] ?: emptyList()
                    )
                }
            }
            is RecipientPickerActionHandler.RecipientClicked -> {
                if (recipientPickerUiState.value.selectedRecipients.contains(action.recipient)) {
                    _uiState.update { it.copy(selectedRecipients = it.selectedRecipients - action.recipient) }
                    _recipientPickerUiState.update { it.copy(selectedRecipients = it.selectedRecipients - action.recipient) }
                } else {
                    _uiState.update { uiState.value.copy(selectedRecipients = it.selectedRecipients + action.recipient) }
                    _recipientPickerUiState.update { it.copy(selectedRecipients = it.selectedRecipients + action.recipient) }
                }
            }
            is RecipientPickerActionHandler.SearchValueChanged -> {
                _recipientPickerUiState.update { it.copy(searchValue = action.searchText) }

                loadRecipients(action.searchText.text, contextPickerUiState.value.selectedContext ?: return)
            }
        }
    }

    private fun updateUiState(uiState: InboxComposeUiState) {
        viewModelScope.launch {
            _uiState.emit(uiState)
        }
    }

    private fun updateUiState(uiState: ContextPickerUiState) {
        viewModelScope.launch {
            _contextPickerUiState.emit(uiState)
        }
    }

    private fun updateUiState(uiState: RecipientPickerUiState) {
        viewModelScope.launch {
            _recipientPickerUiState.emit(uiState)
        }
    }

    private fun loadContexts(forceRefresh: Boolean = false) {
        updateUiState(
            contextPickerUiState.value.copy(
                isLoading = true
            )
        )

        viewModelScope.launch {
            val courses = inboxComposeRepository.getCourses(forceRefresh)
            val groups = inboxComposeRepository.getGroups(forceRefresh)
            updateUiState(
                contextPickerUiState.value.copy(
                    courses = courses,
                    groups = groups,
                    isLoading = false
                )
            )
        }
    }

    private fun loadRecipients(searchQuery: String, context: CanvasContext, forceRefresh: Boolean = false) {
        viewModelScope.launch {

            val recipients = inboxComposeRepository.getRecipients(searchQuery, context, forceRefresh)
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

            val recipientsToShow = if (recipientPickerUiState.value.searchValue.text.isEmpty() && recipientPickerUiState.value.selectedRole != null) {
                roleRecipients[recipientPickerUiState.value.selectedRole] ?: emptyList()
            } else {
                recipients
            }
            updateUiState(
                recipientPickerUiState.value.copy(
                    recipientsByRole = roleRecipients,
                    isLoading = false,
                    recipientsToShow = recipientsToShow
                )
            )
        }
    }

    private fun createConversation(
        onFinished: () -> Unit = {}
    ) {
        uiState.value.selectedContext?.let { context ->
            viewModelScope.launch {
                updateUiState(uiState.value.copy(isSending = true))

                inboxComposeRepository.createConversation(
                    recipients = uiState.value.selectedRecipients,
                    subject = uiState.value.subject.text,
                    message = uiState.value.body.text,
                    context = context,
                    attachments = emptyList(),
                    isIndividual = uiState.value.sendIndividual
                )

                onFinished()
            }
        }
    }
}