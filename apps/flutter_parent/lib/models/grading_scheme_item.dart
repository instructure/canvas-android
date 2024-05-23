/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

import 'package:built_value/built_value.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';

part 'grading_scheme_item.g.dart';

abstract class GradingSchemeItem implements Built<GradingSchemeItem, GradingSchemeItemBuilder> {
  static Serializer<GradingSchemeItem> get serializer => _$gradingSchemeItemSerializer;

  GradingSchemeItem._();

  factory GradingSchemeItem([void Function(GradingSchemeItemBuilder) updates]) = _$GradingSchemeItem;

  factory GradingSchemeItem.fromJson(JsonObject json) {
    var emptyGradingSchemeItem = GradingSchemeItem((b) => b
      ..grade = null
      ..value = null);
    if (!json.isList) return emptyGradingSchemeItem;
    List<dynamic> items = json.asList;
    if (!(items[0] is String) || !(items[1] is num)) return emptyGradingSchemeItem;
    String grade = items[0] as String;
    double value = (items[1] as num).toDouble();
    return GradingSchemeItem((b) => b
      ..grade = grade
      ..value = value);
  }

  String? get grade;

  double? get value;
}
