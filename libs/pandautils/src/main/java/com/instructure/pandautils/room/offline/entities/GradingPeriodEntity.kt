/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.GradingPeriod

@Entity
data class GradingPeriodEntity(
    @PrimaryKey
    val id: Long,
    val title: String?,
    val startDate: String?,
    val endDate: String?,
    val weight: Double,
) {
    constructor(gradingPeriod: GradingPeriod) : this(
        gradingPeriod.id,
        gradingPeriod.title,
        gradingPeriod.startDate,
        gradingPeriod.endDate,
        gradingPeriod.weight
    )

    fun toApiModel() = GradingPeriod(
        id = id,
        title = title,
        startDate = startDate,
        endDate = endDate,
        weight = weight
    )
}