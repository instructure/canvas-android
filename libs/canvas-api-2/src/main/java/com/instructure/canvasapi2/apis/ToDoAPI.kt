/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ToDo
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

object ToDoAPI {

    internal interface ToDosInterface {
        @GET("users/self/todo")
        fun getUserTodos(): Call<List<ToDo>>

        @GET("users/self/todo?include[]=ungraded_quizzes")
        fun getUserTodosWithUngradedQuizzes(): Call<List<ToDo>>

        @GET("{contextId}/todo")
        fun getCourseTodos(@Path("contextId") contextId: Long): Call<List<ToDo>>

        @DELETE
        fun dismissTodo(@Url url: String): Call<Void>
    }

    fun getUserTodos(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<ToDo>>) {
        callback.addCall(adapter.build(ToDosInterface::class.java, params).getUserTodos()).enqueue(callback)
    }

    fun getUserTodosWithUngradedQuizzes(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<ToDo>>) {
        callback.addCall(adapter.build(ToDosInterface::class.java, params).getUserTodosWithUngradedQuizzes()).enqueue(callback)
    }

    fun dismissTodo(toDo: ToDo, adapter: RestBuilder, params: RestParams, callback: StatusCallback<Void>) {
        callback.addCall(adapter.build(ToDosInterface::class.java, params).dismissTodo(toDo.ignore!!)).enqueue(callback)
    }

    fun getUserTodosSynchronous(adapter: RestBuilder, params: RestParams): List<ToDo>? {
        return try {
            adapter.build(ToDosInterface::class.java, params).getUserTodos().execute().body()
        } catch (E: Exception) {
            null
        }
    }

    fun getCourseTodosSynchronous(canvasContext: CanvasContext, adapter: RestBuilder, params: RestParams): List<ToDo>? {
        return try {
            adapter.build(ToDosInterface::class.java, params).getCourseTodos(canvasContext.id).execute().body()
        } catch (E: Exception) {
            null
        }
    }

    fun getCourseTodos(canvasContext: CanvasContext, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<ToDo>>) {
        callback.addCall(adapter.build(ToDosInterface::class.java, params).getCourseTodos(canvasContext.id)).enqueue(callback)
    }

}
