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

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AttachmentFetcher extends StatefulWidget {
  final Attachment attachment;
  final Widget Function(BuildContext context, File file) builder;

  const AttachmentFetcher({required this.attachment, required this.builder, super.key});

  @override
  _AttachmentFetcherState createState() => _AttachmentFetcherState();
}

class _AttachmentFetcherState extends State<AttachmentFetcher> {
  final _interactor = locator<AttachmentFetcherInteractor>();
  late CancelToken _cancelToken;
  late Future<File> _fileFuture;

  @override
  void initState() {
    _cancelToken = _interactor.generateCancelToken();
    _fileFuture = _interactor.fetchAttachmentFile(widget.attachment, _cancelToken);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _fileFuture,
      builder: (BuildContext context, AsyncSnapshot<File> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return LoadingIndicator();
        } else if (snapshot.hasData) {
          return widget.builder(context, snapshot.data!);
        } else {
          return ErrorPandaWidget(L10n(context).errorLoadingFile, () {
            setState(() {
              _fileFuture = _interactor.fetchAttachmentFile(widget.attachment, _cancelToken);
            });
          });
        }
      },
    );
  }

  @override
  void dispose() {
    _cancelToken.cancel();
    super.dispose();
  }
}
