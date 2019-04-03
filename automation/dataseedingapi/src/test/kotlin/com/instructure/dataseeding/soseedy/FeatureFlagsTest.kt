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

import com.instructure.dataseeding.api.FeatureFlagsApi
import com.instructure.dataseeding.model.ContextTypes.ACCOUNT
import com.instructure.dataseeding.model.FeatureFlagApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class FeatureFlagsTest {
    @Test
    fun setAccountFeatureFlag() {
        val feature = "canvas_parent"
        val state = "on"
        val featureFlag = FeatureFlagsApi.setAccountFeatureFlag(
                feature = feature,
                state = state
        )
        assertThat(featureFlag, instanceOf(FeatureFlagApiModel::class.java))
        assertEquals(feature, featureFlag.feature)
        assertEquals(state, featureFlag.state)
        assertEquals(ACCOUNT, featureFlag.contextType)
    }
}
