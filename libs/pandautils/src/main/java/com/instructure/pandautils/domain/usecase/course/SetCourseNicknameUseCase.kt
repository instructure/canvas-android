/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.course

import com.instructure.canvasapi2.managers.CourseNicknameManager
import com.instructure.canvasapi2.models.CourseNickname
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class SetCourseNicknameParams(val courseId: Long, val nickname: String)

class SetCourseNicknameUseCase @Inject constructor() : BaseUseCase<SetCourseNicknameParams, CourseNickname>() {

    override suspend fun execute(params: SetCourseNicknameParams): CourseNickname {
        return awaitApi { CourseNicknameManager.setCourseNickname(params.courseId, params.nickname, it) }
    }
}
