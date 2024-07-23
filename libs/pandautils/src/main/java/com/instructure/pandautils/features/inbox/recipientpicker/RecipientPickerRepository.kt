package com.instructure.pandautils.features.inbox.recipientpicker

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient

interface RecipientPickerRepository {
    suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient>
}