/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase

/**
 * Represents the result of a use case operation.
 *
 * Can be in one of three states:
 * - Success: Operation completed successfully with data
 * - Error: Operation failed with an exception
 * - Loading: Operation is in progress
 *
 * Usage:
 * ```
 * when (result) {
 *     is UseCaseResult.Success -> {
 *         val data = result.data
 *         // Handle success
 *     }
 *     is UseCaseResult.Error -> {
 *         val exception = result.exception
 *         // Handle error
 *     }
 *     is UseCaseResult.Loading -> {
 *         // Handle loading state
 *     }
 * }
 * ```
 */
sealed class UseCaseResult<out T> {
    data class Success<T>(val data: T) : UseCaseResult<T>()
    data class Error(val exception: Throwable) : UseCaseResult<Nothing>()
    object Loading : UseCaseResult<Nothing>()
}