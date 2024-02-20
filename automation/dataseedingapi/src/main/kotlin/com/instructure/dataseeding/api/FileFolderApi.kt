//
// Copyright (C) 2024-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CourseFolderUploadApiModel
import com.instructure.dataseeding.model.CourseFolderUploadApiRequestModel
import com.instructure.dataseeding.model.CourseRootFolderApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.*

object FileFolderApi {
    interface FileFolderService {

        @GET("courses/{courseId}/folders/root")
        fun getCourseRootFolder(
            @Path("courseId") courseId: Long): Call<CourseRootFolderApiModel>

        @POST("folders/{folderId}/folders")
        fun createCourseFolder(
                @Path("folderId") folderId: Long,
                @Body courseFolderUploadApiRequestModel: CourseFolderUploadApiRequestModel): Call<CourseFolderUploadApiModel>

    }

    private fun fileFolderService(token: String): FileFolderService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(FileFolderService::class.java)

    fun getCourseRootFolder(courseId: Long, token: String): CourseRootFolderApiModel {
        return fileFolderService(token)
            .getCourseRootFolder(courseId = courseId)
            .execute()
            .body()!!
    }

    fun createCourseFolder(
        folderId: Long,
        token: String,
        name: String,
        locked: Boolean = false
     ): CourseFolderUploadApiModel {
        val courseFolderUploadRequestModel = CourseFolderUploadApiRequestModel(
            name = name,
            locked = locked
        )
        return fileFolderService(token)
            .createCourseFolder(folderId, courseFolderUploadRequestModel)
            .execute()
            .body()!!
    }

}
