/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

library serializers;

import 'package:built_value/serializer.dart';
import 'package:built_value/standard_json_plugin.dart';
import 'package:flutter_parent/models/school_domain.dart';

part 'serializers.g.dart';

/// If changes are made, run `flutter pub run build_runner build` from the project root. Alternatively, you can
/// have it watch for changes and automatically build if you run `flutter pub run build_runner watch`.
@SerializersFor([
  SchoolDomain,
])
final Serializers serializers = _$serializers;

Serializers jsonSerializers = (serializers.toBuilder()..addPlugin(StandardJsonPlugin())).build();

T deserialize<T>(dynamic value) => jsonSerializers.deserializeWith<T>(serializers.serializerForType(T), value);

List<T> deserializeList<T>(dynamic value) => List.from(value.map((value) => deserialize<T>(value)).toList());
