// Copyright (C) 2019 - present Instructure, Inc.
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
import 'package:mime/mime.dart';

part 'attachment.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class Attachment implements Built<Attachment, AttachmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Attachment> get serializer => _$attachmentSerializer;

  @BuiltValueField(serialize: false)
  String get id => jsonId.isString ? jsonId.asString : jsonId.asNum.toString();

  @BuiltValueField(wireName: 'id')
  JsonObject get jsonId;

  @BuiltValueField(wireName: 'content-type')
  String? get contentType;

  String? get filename;

  @BuiltValueField(wireName: 'display_name')
  String? get displayName;

  String? get url;

  @BuiltValueField(wireName: 'thumbnail_url')
  String? get thumbnailUrl;

  @BuiltValueField(wireName: 'preview_url')
  String? get previewUrl;

  @BuiltValueField(wireName: 'created_at')
  DateTime? get createdAt;

  int get size;

  String? inferContentType() {
    if (contentType != null && contentType?.isNotEmpty == true) return contentType!;

    // First, attempt to infer content type from file name
    String? type = lookupMimeType(filename ?? '');

    // Next, attempt to infer from url
    if (type == null) type = lookupMimeType(url ?? '');

    // Last, attempt to infer from display name
    if (type == null) type = lookupMimeType(displayName ?? '');

    return type;
  }

  Attachment._();
  factory Attachment([void Function(AttachmentBuilder) updates]) = _$Attachment;

  static void _initializeBuilder(AttachmentBuilder b) => b
    ..jsonId = JsonObject('')
    ..size = 0;
}
