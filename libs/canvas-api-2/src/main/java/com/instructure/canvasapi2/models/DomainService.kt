/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2.models

import com.instructure.canvasapi2.BuildConfig

enum class DomainService(
    val baseUrl: String,
    val workflows: List<String>,
) {
    REDWOOD(
        baseUrl = BuildConfig.REDWOOD_BASE_URL,
        workflows = listOf("redwood"),
    ),
    JOURNEY(
        baseUrl = BuildConfig.JOURNEY_BASE_URL,
        workflows = listOf("journey", "pine", "cedar"),
    )
}