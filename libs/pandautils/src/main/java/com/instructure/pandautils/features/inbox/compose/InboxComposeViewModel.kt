package com.instructure.pandautils.features.inbox.compose

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
import kotlinx.coroutines.launch
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class InboxComposeViewModel @Inject constructor(
    private val inboxComposeRepository: InboxComposeRepository
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

    fun handleAction(action: InboxComposeActionHandler, activity: FragmentActivity?) {
        when (action) {
            is InboxComposeActionHandler.CancelClicked -> {
                activity?.supportFragmentManager?.popBackStack()
            }
            is InboxComposeActionHandler.OpenContextPicker -> {
                updateUiState(uiState.value.copy(screenOption = InboxComposeScreenOptions.ContextPicker))
            }
            is InboxComposeActionHandler.RemoveRecipient -> {
                updateUiState(uiState.value.copy(selectedRecipients = uiState.value.selectedRecipients - action.recipient))
                updateUiState(recipientPickerUiState.value.copy(selectedRecipients = recipientPickerUiState.value.selectedRecipients - action.recipient))
            }
            is InboxComposeActionHandler.OpenRecipientPicker -> {
                updateUiState(uiState.value.copy(screenOption = InboxComposeScreenOptions.RecipientPicker))
            }
            is InboxComposeActionHandler.BodyChanged -> {
                updateUiState(uiState.value.copy(body = action.body))
            }
            is InboxComposeActionHandler.SendClicked -> {
                createConversation {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
            is InboxComposeActionHandler.SubjectChanged -> {
                updateUiState(uiState.value.copy(subject = action.subject))
            }
            is InboxComposeActionHandler.SendIndividualChanged -> {
                updateUiState(uiState.value.copy(sendIndividual = action.sendIndividual))
            }
        }
    }

    fun handleAction(action: ContextPickerActionHandler) {
        when (action) {
            is ContextPickerActionHandler.DoneClicked -> {
                updateUiState(uiState.value.copy(screenOption = InboxComposeScreenOptions.None))
            }
            is ContextPickerActionHandler.RefreshCalled -> {
                loadContexts(forceRefresh = true)
            }
            is ContextPickerActionHandler.ContextClicked -> {
                updateUiState(
                    uiState.value.copy(selectedContext = action.context, screenOption = InboxComposeScreenOptions.None)
                )

                loadRecipients("", action.context)
            }
        }
    }

    fun handleAction(action: RecipientPickerActionHandler) {
        when (action) {
            is RecipientPickerActionHandler.DoneClicked -> {
                updateUiState(recipientPickerUiState.value.copy(screenOption = RecipientPickerScreenOption.Roles))
                updateUiState(uiState.value.copy(screenOption = InboxComposeScreenOptions.None))
            }
            is RecipientPickerActionHandler.RecipientBackClicked -> {
                updateUiState(recipientPickerUiState.value.copy(screenOption = RecipientPickerScreenOption.Roles))
            }
            is RecipientPickerActionHandler.RoleClicked -> {
                updateUiState(recipientPickerUiState.value.copy(
                    screenOption = RecipientPickerScreenOption.Recipients,
                    selectedRole = action.role
                ))
            }
            is RecipientPickerActionHandler.RecipientClicked -> {
                if (recipientPickerUiState.value.selectedRecipients.contains(action.recipient)) {
                    updateUiState(uiState.value.copy(selectedRecipients = uiState.value.selectedRecipients - action.recipient))
                    updateUiState(recipientPickerUiState.value.copy(selectedRecipients = recipientPickerUiState.value.selectedRecipients - action.recipient))
                } else {
                    updateUiState(uiState.value.copy(selectedRecipients = uiState.value.selectedRecipients + action.recipient))
                    updateUiState(recipientPickerUiState.value.copy(selectedRecipients = recipientPickerUiState.value.selectedRecipients + action.recipient))
                }
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

            updateUiState(
                recipientPickerUiState.value.copy(
                    recipientsByRole = roleRecipients,
                    isLoading = false
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