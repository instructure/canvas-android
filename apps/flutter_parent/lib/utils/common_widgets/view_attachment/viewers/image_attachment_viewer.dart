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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:photo_view/photo_view.dart';

class ImageAttachmentViewer extends StatelessWidget {
  final Attachment attachment;

  const ImageAttachmentViewer(this.attachment, {super.key});

  @override
  Widget build(BuildContext context) {
    var minScale = PhotoViewComputedScale.contained;
    var backgroundDecoration = BoxDecoration(color: Theme.of(context).scaffoldBackgroundColor);

    if (attachment.inferContentType()?.contains('svg') == true) {
      return PhotoView.customChild(
        backgroundDecoration: backgroundDecoration,
        child: SvgPicture.network(
          attachment.url ?? '',
          placeholderBuilder: (context) => LoadingIndicator(),
        ),
        minScale: minScale,
      );
    }

    return PhotoView(
      backgroundDecoration: backgroundDecoration,
      imageProvider: NetworkImage(attachment.url ?? ''),
      minScale: minScale,
      loadingBuilder: (context, imageChunkEvent) => LoadingIndicator(),
      errorBuilder: (context, error, stackTrace) => EmptyPandaWidget(
        svgPath: 'assets/svg/panda-not-supported.svg',
        title: L10n(context).errorLoadingImage,
      ),
    );
  }
}
