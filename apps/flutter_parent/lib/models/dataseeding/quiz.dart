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

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'quiz.g.dart';

abstract class Quiz implements Built<Quiz, QuizBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Quiz> get serializer => _$quizSerializer;

  Quiz._();
  factory Quiz([void Function(QuizBuilder) updates]) = _$Quiz;

  String get id;

  String get title;

  String get description;

  @BuiltValueField(wireName: 'due_at')
  DateTime get dueAt;

  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  static void _initializeBuilder(QuizBuilder b) => b;
}
