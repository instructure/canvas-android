package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CreateModuleWrapper
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.dataseeding.model.UpdateModule
import com.instructure.dataseeding.model.UpdateModuleWrapper
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

object ModulesApi {
    interface ModulesService {
        @POST("courses/{courseId}/modules")
        fun createModules(@Path("courseId") courseId: Long, @Body createModule: CreateModuleWrapper): Call<ModuleApiModel>

        @PUT("courses/{courseId}/modules/{id}")
        fun updateModule(@Path("courseId") courseId: Long, @Path("id") id: Long, @Body updateModule: UpdateModuleWrapper): Call<ModuleApiModel>
    }

    private fun modulesService(token: String): ModulesService
            = CanvasRestAdapter.retrofitWithToken(token).create(ModulesService::class.java)

    fun createModule(courseId: Long, teacherToken: String, unlockAt: String?): ModuleApiModel {
        val module = CreateModuleWrapper(Randomizer.createModule(unlockAt))
        return modulesService(teacherToken).createModules(courseId, module).execute().body()!!
    }

    // Canvas API does not support creating a published module.
    // All modules must be created and published in separate calls.
    fun updateModule(courseId: Long, id: Long, published: Boolean, teacherToken: String): ModuleApiModel {
        val update = UpdateModuleWrapper(UpdateModule(published))
        return modulesService(teacherToken).updateModule(courseId, id, update).execute().body()!!
    }
}
