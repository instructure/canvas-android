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
            apolloClient.mutation(mutationCall).executeV3()
        }
    }
}