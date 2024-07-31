package com.instructure.pandautils.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.utils.isCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class InboxComposeViewModel @Inject constructor(
    private val inboxComposeRepository: InboxComposeRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(
        InboxComposeUiState()
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadContexts()
    }

    fun handleAction(action: InboxComposeActionHandler, activity: FragmentActivity) {
        when (action) {
            is InboxComposeActionHandler.CancelDismissDialog -> {
                _uiState.update { it.copy(
                    showConfirmationDialog = action.isShow
                ) }
            }
            is InboxComposeActionHandler.Close -> {
                activity.supportFragmentManager.popBackStack()
            }
            is InboxComposeActionHandler.OpenContextPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.ContextPicker) }
            }
            is InboxComposeActionHandler.RemoveRecipient -> {
                _uiState.update { it.copy(
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        selectedRecipients = it.recipientPickerUiState.selectedRecipients - action.recipient
                    )
                ) }
            }
            is InboxComposeActionHandler.OpenRecipientPicker -> {
                _uiState.update { it.copy(screenOption = InboxComposeScreenOptions.RecipientPicker) }
            }
            is InboxComposeActionHandler.BodyChanged -> {
                _uiState.update { it.copy(body = action.body) }
            }
            is InboxComposeActionHandler.SendClicked -> {
                createConversation(activity)
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
                _uiState.update { it.copy(
                    contextPickerUiState = it.contextPickerUiState.copy(selectedContext = action.context),
                    screenOption = InboxComposeScreenOptions.None
                ) }

                loadRecipients("", action.context)
            }
        }
    }

    fun handleAction(action: RecipientPickerActionHandler) {
        when (action) {
            is RecipientPickerActionHandler.DoneClicked -> {
                _uiState.update { uiState.value.copy(
                    screenOption = InboxComposeScreenOptions.None,
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        screenOption = RecipientPickerScreenOption.Roles,
                    )
                ) }

                handleAction(RecipientPickerActionHandler.SearchValueChanged(TextFieldValue("")))
            }
            is RecipientPickerActionHandler.RecipientBackClicked -> {
                _uiState.update { uiState.value.copy(
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        screenOption = RecipientPickerScreenOption.Roles,
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
                            recipientsToShow = it.recipientPickerUiState.recipientsByRole[action.role] ?: emptyList()
                        ),
                    )
                }
            }
            is RecipientPickerActionHandler.RecipientClicked -> {
                if (uiState.value.recipientPickerUiState.selectedRecipients.contains(action.recipient)) {
                    _uiState.update { it.copy(
                        recipientPickerUiState = it.recipientPickerUiState.copy(
                            selectedRecipients = it.recipientPickerUiState.selectedRecipients - action.recipient
                        )
                    ) }
                    _uiState.update { it.copy(
                        recipientPickerUiState = it.recipientPickerUiState.copy(
                            selectedRecipients = it.recipientPickerUiState.selectedRecipients - action.recipient
                        )
                    ) }
                } else {
                    _uiState.update { it.copy(
                        recipientPickerUiState = it.recipientPickerUiState.copy(
                            selectedRecipients = it.recipientPickerUiState.selectedRecipients + action.recipient
                        )
                    ) }
                }
            }
            is RecipientPickerActionHandler.SearchValueChanged -> {
                _uiState.update { it.copy(
                    recipientPickerUiState = it.recipientPickerUiState.copy(
                        searchValue = action.searchText
                    )
                ) }

                loadRecipients(action.searchText.text, uiState.value.contextPickerUiState.selectedContext ?: return)
            }
        }
    }

    private fun loadContexts(forceRefresh: Boolean = false) {
        _uiState.update { it.copy(
            contextPickerUiState = it.contextPickerUiState.copy(isLoading = true)
        ) }

        viewModelScope.launch {
            val courses = inboxComposeRepository.getCourses(forceRefresh)
            val groups = inboxComposeRepository.getGroups(forceRefresh)
            _uiState.update { it.copy(
                contextPickerUiState = it.contextPickerUiState.copy(
                    courses = courses,
                    groups = groups,
                    isLoading = false
                )
            ) }
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

            val recipientsToShow =
                if (uiState.value.recipientPickerUiState.searchValue.text.isEmpty() && uiState.value.recipientPickerUiState.selectedRole != null) {
                    roleRecipients[uiState.value.recipientPickerUiState.selectedRole] ?: emptyList()
                } else {
                    recipients
                }
            _uiState.update { it.copy(
                recipientPickerUiState = it.recipientPickerUiState.copy(
                    recipientsByRole = roleRecipients,
                    isLoading = false,
                    recipientsToShow = recipientsToShow
                )
            ) }
        }
    }

    private fun createConversation(activity: FragmentActivity) {
        uiState.value.contextPickerUiState.selectedContext?.let { context ->
            viewModelScope.launch {
                _uiState.update { uiState.value.copy(isSending = true) }

                inboxComposeRepository.createConversation(
                    recipients = uiState.value.recipientPickerUiState.selectedRecipients,
                    subject = uiState.value.subject.text,
                    message = uiState.value.body.text,
                    context = context,
                    attachments = emptyList(),
                    isIndividual = uiState.value.sendIndividual
                )

                handleAction(InboxComposeActionHandler.Close, activity)
            }
        }
    }
}