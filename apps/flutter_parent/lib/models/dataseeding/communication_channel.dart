
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

part 'communication_channel.g.dart';

abstract class CommunicationChannel implements Built<CommunicationChannel, CommunicationChannelBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CommunicationChannel> get serializer => _$communicationChannelSerializer;

  CommunicationChannel._();
  factory CommunicationChannel([void Function(CommunicationChannelBuilder) updates]) = _$CommunicationChannel;

  @BuiltValueField(wireName: "skip_confirmation")
  bool get skipConfirmation;

  static void _initializeBuilder(CommunicationChannelBuilder b) => b
      ..skipConfirmation = false;

}