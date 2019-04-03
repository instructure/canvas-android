//
// Copyright (C) 2018-present Instructure, Inc.
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



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test

class UsersTest {
    @Test
    fun createCanvasUser() {
        val user = UserApi.createCanvasUser()
        assertThat(user, instanceOf(CanvasUserApiModel::class.java))
        assertTrue(user.id >= 1)
        assertTrue(user.loginId.isNotEmpty())
        assertTrue(user.password.isNotEmpty())
        assertTrue(user.domain.isNotEmpty())
        assertTrue(user.token.isNotEmpty())
        assertTrue(user.name.isNotEmpty())
        assertTrue(user.shortName.isNotEmpty())
        assertTrue(user.avatarUrl?.isEmpty() ?: true) // null avatarUrl is OK as well
    }
}
