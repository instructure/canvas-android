/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.QuizContextDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.QuizContextEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity

class QuizFacade(
    private val quizDao: QuizDao,
    private val quizContextDao: QuizContextDao
) {
    suspend fun insertQuiz(contextType: String, contextId: Long, quiz: Quiz) {
        quizDao.insert(QuizEntity(quiz))
        quizContextDao.insert(QuizContextEntity(contextType, contextId, quiz))
    }

    suspend fun getQuizzesByContext(contextType: String, contextId: Long): List<Quiz> {
        val quizContextEntities = quizContextDao.findByContext(contextType, contextId)
        return quizContextEntities.mapNotNull { quizDao.findById(it.quizId)?.toApiModel() }
    }
}