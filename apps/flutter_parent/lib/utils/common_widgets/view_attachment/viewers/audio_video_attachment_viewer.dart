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

import 'package:chewie/chewie.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:video_player/video_player.dart';

import '../../empty_panda_widget.dart';
import 'audio_video_attachment_viewer_interactor.dart';

class AudioVideoAttachmentViewer extends StatefulWidget {
  final Attachment attachment;

  bool get isAudio => attachment.inferContentType()?.startsWith('audio') == true;

  const AudioVideoAttachmentViewer(this.attachment, {super.key});

  @override
  _AudioVideoAttachmentViewerState createState() => _AudioVideoAttachmentViewerState();
}

class _AudioVideoAttachmentViewerState extends State<AudioVideoAttachmentViewer> {
  static const defaultAspectRatio = 16 / 9;

  VideoPlayerController? _videoController;
  ChewieController? _chewieController;

  final _interactor = locator<AudioVideoAttachmentViewerInteractor>();

  late Future<ChewieController> controllerFuture;

  @override
  void initState() {
    controllerFuture = _initController();
    super.initState();
  }

  Future<ChewieController> _initController() async {
    _videoController = _interactor.makeController(widget.attachment.url!);

    try {
      // Initialized the video controller so we can get the aspect ratio
      await _videoController?.initialize();
    } catch (e) {
      // Intentionally left blank. Errors will be handled by ChewieController.errorBuilder.
    }

    // Get aspect ratio from controller, fall back to 16:9
    var aspectRatio = _videoController?.value.aspectRatio;
    if (aspectRatio == null || aspectRatio.isNaN || aspectRatio <= 0) aspectRatio = defaultAspectRatio;

    // Set up controller
    _chewieController = ChewieController(
      videoPlayerController: _videoController!,
      aspectRatio: aspectRatio,
      autoInitialize: true,
      autoPlay: true,
      allowedScreenSleep: false,
      overlay: widget.isAudio
          ? Container(
              color: Theme.of(context).scaffoldBackgroundColor,
              child: Center(
                child: Icon(CanvasIcons.audio),
              ),
            )
          : Container(),
      errorBuilder: (context, error) => _error(context),
    );
    return _chewieController!;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: controllerFuture,
      builder: (BuildContext context, AsyncSnapshot<ChewieController> snapshot) {
        if (snapshot.hasData) {
          return Center(
            child: Chewie(controller: snapshot.data!),
          );
        } else if (snapshot.hasError) {
          return _error(context);
        } else {
          return LoadingIndicator();
        }
      },
    );
  }

  Widget _error(BuildContext context) {
    return Container(
      color: Theme.of(context).scaffoldBackgroundColor,
      child: EmptyPandaWidget(
        svgPath: 'assets/svg/panda-not-supported.svg',
        title: L10n(context).errorPlayingMedia,
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    _videoController?.dispose();
    _chewieController?.dispose();
  }
}
