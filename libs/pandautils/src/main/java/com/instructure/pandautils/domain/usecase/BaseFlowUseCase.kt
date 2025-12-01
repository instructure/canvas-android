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

import kotlinx.coroutines.flow.Flow

/**
 * Base class for use cases that return a Flow of results.
 *
 * Use this for use cases that emit multiple values over time or need reactive updates.
 *
 * @param Params The type of parameters this use case accepts
 * @param Result The type of result this use case emits
 *
 * Usage:
 * ```
 * class ObserveUserUpdatesUseCase @Inject constructor(
 *     private val userRepository: UserRepository
 * ) : BaseFlowUseCase<String, UseCaseResult<User>>() {
 *     override fun execute(params: String): Flow<UseCaseResult<User>> {
 *         return userRepository.observeUser(params)
 *             .map { UseCaseResult.Success(it) }
 *             .catch { emit(UseCaseResult.Error(it)) }
 *     }
 * }
 *
 * // Collect results
 * observeUserUpdatesUseCase("user123").collect { result ->
 *     when (result) {
 *         is UseCaseResult.Success -> // Handle success
 *         is UseCaseResult.Error -> // Handle error
 *         is UseCaseResult.Loading -> // Handle loading
 *     }
 * }
 * ```
 */
abstract class BaseFlowUseCase<in Params, out Result> {
    abstract fun execute(params: Params): Flow<Result>

    operator fun invoke(params: Params): Flow<Result> = execute(params)
}