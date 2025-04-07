package com.instructure.dataseeding.api

import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseedingapi.CreateCommentMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CommentLibraryApi {

    fun createComment(courseId: Long, token: String, comment: String) {

        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = CreateCommentMutation(courseId.toString(), comment)

        CoroutineScope(Dispatchers.IO).launch {
            // Since we handle errors with exceptions, we keep the compat call of execute because the new doesn't throw exceptions
            apolloClient.mutation(mutationCall).executeV3()
        }
    }
}