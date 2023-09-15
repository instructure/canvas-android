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
import 'package:video_player_platform_interface/video_player_platform_interface.dart';

import '../../../accessibility_utils.dart';
import '../../../test_app.dart';
import '../../../test_helpers/mock_helpers.mocks.dart';

void main() {
  testWidgetsWithAccessibilityChecks('displays loading indicator', (tester) async {
    var interactor = MockAudioVideoAttachmentViewerInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => interactor);
    });

    var controller = MockVideoPlayerController();
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
    var interactor = MockAudioVideoAttachmentViewerInteractor();
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
    var interactor = MockAudioVideoAttachmentViewerInteractor();
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
  }, skip: true);

  // Testing w/o a11y checks due to minor issues in Chewie that we can't control
  testWidgets('displays video player', (tester) async {
    var interactor = MockAudioVideoAttachmentViewerInteractor();
    VideoPlayerPlatform.instance = _FakeVideoPlayerPlatform();
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
    var interactor = MockAudioVideoAttachmentViewerInteractor();
    VideoPlayerPlatform.instance = _FakeVideoPlayerPlatform();
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

class _FakeVideoController extends Fake implements VideoPlayerController {
  final bool hasError;

  _FakeVideoController({this.hasError = false});



  @override
  VideoPlayerValue get value => VideoPlayerValue(
        duration: Duration(seconds: 3),
        errorDescription: hasError ? 'Error' : null,
        isInitialized: true,
      );

  @override
  Future<void> setLooping(bool looping) async => null;

  @override
  Future<void> initialize() async => null;

  @override
  Future<void> dispose() async => null;

  @override
  Future<void> play() async => null;

  @override
  int get textureId => 0;

  @override
  void addListener(listener) {}

  @override
  void removeListener(listener) {}
}

class _FakeVideoPlayerPlatform extends VideoPlayerPlatform {
  final Completer<bool> initialized = Completer<bool>();
  final List<String> calls = <String>[];
  final List<DataSource> dataSources = <DataSource>[];
  final Map<int, StreamController<VideoEvent>> streams =
  <int, StreamController<VideoEvent>>{};
  final bool forceInitError;
  int nextTextureId = 0;
  final Map<int, Duration> _positions = <int, Duration>{};

  _FakeVideoPlayerPlatform({
    this.forceInitError = false,
  });

  @override
  Future<int?> create(DataSource dataSource) async {
    calls.add('create');
    final StreamController<VideoEvent> stream = StreamController<VideoEvent>();
    streams[nextTextureId] = stream;
    stream.add(
        VideoEvent(
          eventType: VideoEventType.initialized,
          size: const Size(100, 100),
          duration: const Duration(seconds: 1),
        ),
      );
    dataSources.add(dataSource);
    return nextTextureId++;
  }

  @override
  Future<void> dispose(int textureId) async {
    calls.add('dispose');
  }

  @override
  Future<void> init() async {
    calls.add('init');
    initialized.complete(true);
  }

  @override
  Stream<VideoEvent> videoEventsFor(int textureId) {
    return streams[textureId]!.stream;
  }

  @override
  Future<void> pause(int textureId) async {
    calls.add('pause');
  }

  @override
  Future<void> play(int textureId) async {
    calls.add('play');
  }

  @override
  Future<Duration> getPosition(int textureId) async {
    calls.add('position');
    return _positions[textureId] ?? Duration.zero;
  }

  @override
  Future<void> seekTo(int textureId, Duration position) async {
    calls.add('seekTo');
    _positions[textureId] = position;
  }

  @override
  Future<void> setLooping(int textureId, bool looping) async {
    calls.add('setLooping');
  }

  @override
  Future<void> setVolume(int textureId, double volume) async {
    calls.add('setVolume');
  }

  @override
  Future<void> setPlaybackSpeed(int textureId, double speed) async {
    calls.add('setPlaybackSpeed');
  }

  @override
  Future<void> setMixWithOthers(bool mixWithOthers) async {
    calls.add('setMixWithOthers');
  }

  @override
  Widget buildView(int textureId) {
    return Texture(textureId: textureId);
  }
}

