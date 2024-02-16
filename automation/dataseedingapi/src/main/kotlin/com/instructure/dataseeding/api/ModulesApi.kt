package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CreateModuleItem
import com.instructure.dataseeding.model.CreateModuleItemWrapper
import com.instructure.dataseeding.model.CreateModuleWrapper
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.dataseeding.model.ModuleItemApiModel
import com.instructure.dataseeding.model.UpdateModule
import com.instructure.dataseeding.model.UpdateModuleWrapper
import com.instructure.dataseeding.util.CanvasNetworkAdapter
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

        @POST("courses/{courseId}/modules/{moduleId}/items")
        fun createModuleItem(
                @Path("courseId") courseId: Long,
                @Path("moduleId") moduleId: Long,
                @Body newItem: CreateModuleItemWrapper
        ) : Call<ModuleItemApiModel>
    }

    private fun modulesService(token: String): ModulesService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(ModulesService::class.java)

    fun createModule(courseId: Long, teacherToken: String, unlockAt: String? = null): ModuleApiModel {
        val module = CreateModuleWrapper(Randomizer.createModule(unlockAt))
        return modulesService(teacherToken).createModules(courseId, module).execute().body()!!
    }

    // Canvas API does not support creating a published module.
    // All modules must be created and published in separate calls.
    fun updateModule(courseId: Long, teacherToken: String, moduleId: Long, published: Boolean = true): ModuleApiModel {
        val update = UpdateModuleWrapper(UpdateModule(published))
        return modulesService(teacherToken).updateModule(courseId, moduleId, update).execute().body()!!
    }

    fun createModuleItem(
        courseId: Long,
        teacherToken: String,
        moduleId: Long,
        moduleItemTitle: String,
        moduleItemType: String,
        contentId: String? = null,
        pageUrl: String? = null
    ): ModuleItemApiModel {
        val newItem = CreateModuleItem(moduleItemTitle, moduleItemType, contentId, pageUrl)
        val newItemWrapper = CreateModuleItemWrapper(moduleItem = newItem)
        return modulesService(teacherToken).createModuleItem(
                courseId = courseId,
                moduleId = moduleId,
                newItem = newItemWrapper
        ).execute().body()!!
    }
}
