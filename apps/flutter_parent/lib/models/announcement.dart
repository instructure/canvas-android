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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'remote_file.dart';

part 'announcement.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class Announcement implements Built<Announcement, AnnouncementBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Announcement> get serializer => _$announcementSerializer;

  Announcement._();

  factory Announcement([void Function(AnnouncementBuilder) updates]) = _$Announcement;

  String get id;

  String get title;

  String get message;

  @BuiltValueField(wireName: 'posted_at')
  DateTime get postedAt;

  @BuiltValueField(wireName: 'html_url')
  String get htmlUrl;

  BuiltList<RemoteFile> get attachments;

  static void _initializeBuilder(AnnouncementBuilder b) => b
    ..id = ''
    ..title = ''
    ..message = ''
    ..postedAt = DateTime.now()
    ..htmlUrl = '';
}
