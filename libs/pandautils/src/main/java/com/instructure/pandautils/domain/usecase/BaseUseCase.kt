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
 * Base class for use cases that execute suspending operations and return a single result.
 *
 * @param Params The type of parameters this use case accepts
 * @param Result The type of result this use case returns
 *
 * Usage:
 * ```
 * class GetUserUseCase @Inject constructor(
 *     private val userRepository: UserRepository
 * ) : BaseUseCase<String, User>() {
 *     override suspend fun execute(params: String): User {
 *         return userRepository.getUser(params)
 *     }
 * }
 *
 * // Call with invoke operator
 * val user = getUserUseCase("user123")
 * ```
 */
abstract class BaseUseCase<in Params, out Result> {
    abstract suspend fun execute(params: Params): Result

    suspend operator fun invoke(params: Params): Result = execute(params)
}