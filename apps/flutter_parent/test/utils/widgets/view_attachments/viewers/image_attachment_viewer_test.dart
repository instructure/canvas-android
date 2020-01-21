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

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/image_attachment_viewer.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:photo_view/photo_view.dart';

import '../../../accessibility_utils.dart';
import '../../../network_image_response.dart';
import '../../../test_app.dart';

void main() {
  mockNetworkImageResponse();

  testWidgetsWithAccessibilityChecks('displays loading indicator', (tester) async {
    var attachment = Attachment((a) => a
      ..contentType = 'image/png'
      ..url = 'https://fake.url.com/fake-image.png');

    await tester.pumpWidget(TestApp(Material(child: ImageAttachmentViewer(attachment))));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays image', (tester) async {
    var attachment = Attachment((a) => a
      ..contentType = 'image/png'
      ..url = 'https://fake.url.com/fake-image.png');

    await tester.pumpWidget(TestApp(Material(child: ImageAttachmentViewer(attachment))));
    await tester.pump(Duration(milliseconds: 100));

    expect(find.byType(PhotoView), findsOneWidget);

    // PhotoView doesn't seem to resolve images in unit tests, so we'll check that the image provider is set up correctly
    PhotoView photoView = tester.widget(find.byType(PhotoView));
    expect((photoView.imageProvider as NetworkImage).url, attachment.url);
  });

  testWidgetsWithAccessibilityChecks('displays svg using SvgPicture', (tester) async {
    var attachment = Attachment((a) => a
      ..contentType = 'image/svg'
      ..url = 'https://fake.url.com/fake-image.svg');

    await tester.pumpWidget(TestApp(Material(child: ImageAttachmentViewer(attachment))));
    await tester.pump(Duration(milliseconds: 100));

    expect(find.byType(SvgPicture), findsOneWidget);
  });
}
