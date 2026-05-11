/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.models.User
import com.instructure.horizon.database.dao.HorizonUserDao
import com.instructure.horizon.database.entity.HorizonUserEntity
import javax.inject.Inject

class AccountLocalDataSource @Inject constructor(
    private val userDao: HorizonUserDao,
) {
    suspend fun saveUser(user: User) {
        userDao.upsert(
            HorizonUserEntity(
                id = user.id,
                name = user.name,
                shortName = user.shortName,
            )
        )
    }

    suspend fun getUser(): User? {
        val entity = userDao.getUser() ?: return null
        return User(
            id = entity.id,
            name = entity.name,
            shortName = entity.shortName,
        )
    }
}