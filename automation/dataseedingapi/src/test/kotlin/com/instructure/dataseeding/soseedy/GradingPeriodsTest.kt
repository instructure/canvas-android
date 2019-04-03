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

import com.instructure.dataseeding.api.EnrollmentTermsApi
import com.instructure.dataseeding.api.GradingPeriodsApi
import com.instructure.dataseeding.model.GradingPeriodApiModel
import com.instructure.dataseeding.model.GradingPeriodSetApiModel
import com.instructure.dataseeding.model.GradingPeriodSetApiModelWrapper
import com.instructure.dataseeding.model.GradingPeriods
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Test

class GradingPeriodsTest {
    private val enrollmentTerm = EnrollmentTermsApi.createEnrollmentTerm()

    @Test
    fun createGradingPeriodSet() {
        val setWrapper = GradingPeriodsApi.createGradingPeriodSet(enrollmentTermId = enrollmentTerm.id)
        assertThat(setWrapper, instanceOf(GradingPeriodSetApiModelWrapper::class.java))
        assertThat(setWrapper.gradingPeriodSet, instanceOf(GradingPeriodSetApiModel::class.java))
        assertTrue(setWrapper.gradingPeriodSet.id >= 1)
    }

    @Test
    fun createGradingPeriod() {
        val gradingPeriodSet = GradingPeriodsApi.createGradingPeriodSet(enrollmentTermId = enrollmentTerm.id)
        val period = GradingPeriodsApi.createGradingPeriod(gradingPeriodSetId = gradingPeriodSet.gradingPeriodSet.id)
        assertThat(period, instanceOf(GradingPeriods::class.java))
        assertNotNull(period.gradingPeriods)
        assertTrue(period.gradingPeriods.size > 0)
        assertThat(period.gradingPeriods[0], instanceOf(GradingPeriodApiModel::class.java))
        assertTrue(period.gradingPeriods[0].id >= 1)
    }
}
