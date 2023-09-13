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
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';

import 'attachment.dart';

part 'remote_file.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class RemoteFile implements Built<RemoteFile, RemoteFileBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<RemoteFile> get serializer => _$remoteFileSerializer;

  RemoteFile._();

  factory RemoteFile([void Function(RemoteFileBuilder) updates]) = _$RemoteFile;

  String get id;

  String get url;

  String? get filename;

  @BuiltValueField(wireName: 'preview_url')
  String? get previewUrl;

  @BuiltValueField(wireName: 'thumbnail_url')
  String? get thumbnailUrl;

  @BuiltValueField(wireName: 'content-type')
  String? get contentType;

  @BuiltValueField(wireName: 'display_name')
  String? get displayName;

  Attachment toAttachment() {
    return Attachment((a) => a
      ..jsonId = JsonObject('remote-file-$id')
      ..contentType = contentType
      ..filename = filename
      ..displayName = displayName
      ..previewUrl = previewUrl
      ..thumbnailUrl = thumbnailUrl
      ..url = url);
  }

  static void _initializeBuilder(RemoteFileBuilder b) => b
      ..id = ''
      ..url = ''
      ..filename = ''
      ..contentType = ''
      ..previewUrl = ''
      ..thumbnailUrl = ''
      ..displayName = '';

}