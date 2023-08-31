// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter_parent/models/dataseeding/quiz.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class QuizSeedApi {
  static Future<Quiz?> createQuiz(String courseId, String title, DateTime dueAt, {String description = ""}) async {
    var queryParams = {
      'quiz[title]': title,
      'quiz[description]': description,
      'quiz[published]': true,
      'quiz[due_at]': dueAt.toIso8601String(),
    };

    var dio = seedingDio();
    return fetch(dio.post('courses/$courseId/quizzes', queryParameters: queryParams));
  }
}
