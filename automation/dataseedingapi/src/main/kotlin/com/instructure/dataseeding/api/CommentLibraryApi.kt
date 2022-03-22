package com.instructure.dataseeding.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.instructure.dataseeding.CreateCommentMutation
import com.instructure.dataseeding.util.CanvasNetworkAdapter

object CommentLibraryApi {

    fun createComment(courseId: Long, token: String, comment: String) {

        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = CreateCommentMutation(courseId.toString(), comment)

        apolloClient.mutate(mutationCall).enqueue(object : ApolloCall.Callback<CreateCommentMutation.Data>() {
            override fun onResponse(response: Response<CreateCommentMutation.Data>) = Unit
            override fun onFailure(e: ApolloException) = Unit
        })
    }
}