/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */
package com.instructure.pandautils.data.repository.user

import com.instructure.canvasapi2.apis.CourseNicknameAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.ColorChangeResponse
import com.instructure.canvasapi2.models.CourseNickname
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.ColorUtils.toApiHexString

class UserRepositoryImpl(
    private val userApi: UserAPI.UsersInterface,
    private val courseNicknameApi: CourseNicknameAPI.NicknameInterface
) : UserRepository {

    override suspend fun getAccount(forceRefresh: Boolean): DataResult<Account> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getAccount(params)
    }

    override suspend fun setCourseNickname(courseId: Long, nickname: String): DataResult<CourseNickname> {
        val params = RestParams(isForceReadFromNetwork = true)
        return courseNicknameApi.setNickname(courseId, nickname, params)
    }

    override suspend fun deleteCourseNickname(courseId: Long): DataResult<CourseNickname> {
        val params = RestParams(isForceReadFromNetwork = true)
        return courseNicknameApi.deleteNickname(courseId, params)
    }

    override suspend fun setCourseColor(contextId: String, color: Int): DataResult<ColorChangeResponse> {
        val params = RestParams()
        return userApi.setColor(contextId, color.toApiHexString(), params)
    }
}
