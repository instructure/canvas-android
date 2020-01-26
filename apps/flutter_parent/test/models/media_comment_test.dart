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

import 'package:flutter_parent/models/media_comment.dart';
import 'package:test/test.dart';

void main() {
  test('toAttachment returns Attachment with correct properties', () {
    final mediaComment = MediaComment((m) => m
      ..mediaId = 'mediaId'
      ..displayName = 'Display Name'
      ..url = 'fake url'
      ..mediaType = MediaType.video
      ..contentType = 'video/mp4');
    final attachment = mediaComment.toAttachment();

    expect(attachment.id, 'media-comment-${mediaComment.mediaId}');
    expect(attachment.contentType, mediaComment.contentType);
    expect(attachment.filename, mediaComment.mediaId);
    expect(attachment.displayName, mediaComment.displayName);
    expect(attachment.url, mediaComment.url);
  });
}
