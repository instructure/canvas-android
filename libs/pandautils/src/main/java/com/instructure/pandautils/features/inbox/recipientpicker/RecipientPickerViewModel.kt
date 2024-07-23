package com.instructure.pandautils.features.inbox.recipientpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipientPickerViewModel @Inject constructor(
    private val repository: RecipientPickerRepository
): ViewModel() {
    private val searchText = MutableStateFlow("")
    private val selectedContext: CanvasContext? = null

    val selectedRecipients = MutableStateFlow<List<Recipient>>(emptyList())
    var listener: RecipientPickerFragment.RecipientPickerListener? = null

    val allRecipient = MutableStateFlow<List<Recipient>>(emptyList())

    fun loadRecipients() {
        viewModelScope.launch {
            if (selectedContext != null) {
                allRecipient.emit(repository.getRecipients(searchText.value, selectedContext))
            }
        }
    }
}