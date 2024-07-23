package com.instructure.pandautils.features.inbox.recipientpicker

import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class RecipientPickerRepositoryImpl @Inject constructor(
    private val recipientAPI: RecipientAPI.RecipientInterface,
): RecipientPickerRepository {
    override suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient> {
        val params = RestParams(usePerPageQueryParam = true)
        return recipientAPI.getFirstPageRecipientList(
            searchQuery = searchQuery,
            context = context.apiContext(),
            restParams = params,
        ).depaginate {
            recipientAPI.getNextPageRecipientList(it, params)
        }.dataOrNull ?: emptyList()
    }
}