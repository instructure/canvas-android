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

import 'dart:async';

import 'package:chewie/chewie.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer_interactor.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:video_player/video_player.dart';

import '../../../accessibility_utils.dart';
import '../../../test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('displays loading indicator', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    var controller = _MockVideoController();
    when(interactor.makeController(any)).thenReturn(controller);

    Completer<void> initCompleter = Completer();
    when(controller.initialize()).thenAnswer((_) => initCompleter.future);

    var attachment = Attachment((a) => a
      ..contentType = 'video/mp4'
      ..url = 'https://fake.url.com/fake-video.mp4');

    await tester.pumpWidget(TestApp(Material(child: AudioVideoAttachmentViewer(attachment))));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('displays error widget', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    var controller = _FakeVideoController(hasError: true);
    when(interactor.makeController(any)).thenReturn(controller);

    var attachment = Attachment((a) => a
      ..contentType = 'video/mp4'
      ..url = '');

    await tester.pumpWidget(TestApp(Material(child: AudioVideoAttachmentViewer(attachment))));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('displays error widget when controller is null', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    when(interactor.makeController(any)).thenReturn(null);

    var attachment = Attachment((a) => a
      ..contentType = 'video/mp4'
      ..url = '');

    await tester.pumpWidget(TestApp(Material(child: AudioVideoAttachmentViewer(attachment))));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
  });

  // Testing w/o a11y checks due to minor issues in Chewie that we can't control
  testWidgets('displays video player', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    var controller = _FakeVideoController();
    when(interactor.makeController(any)).thenReturn(controller);

    var attachment = Attachment((a) => a
      ..contentType = 'video/mp4'
      ..url = '');

    await tester.pumpWidget(TestApp(Material(child: AudioVideoAttachmentViewer(attachment))));
    await tester.pumpAndSettle();

    expect(find.byType(Chewie), findsOneWidget);
  });

  // Testing w/o a11y checks due to minor issues in Chewie that we can't control
  testWidgets('displays audio icon for audio attachment', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    var controller = _FakeVideoController();
    when(interactor.makeController(any)).thenReturn(controller);

    var attachment = Attachment((a) => a
      ..contentType = 'audio/mp3'
      ..url = '');

    await tester.pumpWidget(TestApp(Material(child: AudioVideoAttachmentViewer(attachment))));
    await tester.pumpAndSettle();

    expect(find.byIcon(CanvasIcons.audio), findsOneWidget);
  });
}

class _MockInteractor extends Mock implements AudioVideoAttachmentViewerInteractor {}

class _MockVideoController extends Mock implements VideoPlayerController {}

class _FakeVideoController extends Fake implements VideoPlayerController {
  final bool hasError;

  _FakeVideoController({this.hasError = false});

  @override
  VideoPlayerValue get value => VideoPlayerValue(
        duration: hasError ? null : Duration(seconds: 3),
        errorDescription: hasError ? 'Error' : null,
      );

  @override
  Future<void> setLooping(bool looping) async => null;

  @override
  Future<void> initialize() async => null;

  @override
  Future<void> dispose() async => null;

  @override
  Future<void> play() => null;

  @override
  int get textureId => 0;

  @override
  void addListener(listener) {}

  @override
  void removeListener(listener) {}
}
