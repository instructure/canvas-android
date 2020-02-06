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

part 'course_tab.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class CourseTab implements Built<CourseTab, CourseTabBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CourseTab> get serializer => _$courseTabSerializer;

  CourseTab._();

  factory CourseTab([void Function(CourseTabBuilder) updates]) = _$CourseTab;

  String get id;

  // There are more fields, but we don't need any others for parent _yet_
}
