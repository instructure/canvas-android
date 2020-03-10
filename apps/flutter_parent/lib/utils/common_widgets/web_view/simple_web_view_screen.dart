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

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:webview_flutter/webview_flutter.dart';

class SimpleWebViewScreen extends StatefulWidget {
  final String _url;
  final String _title;

  SimpleWebViewScreen(this._url, this._title);

  @override
  State<StatefulWidget> createState() => _SimpleWebViewScreenState();
}

class _SimpleWebViewScreenState extends State<SimpleWebViewScreen> {
  WebViewController _controller;

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => _handleBackPress(context),
      child: Scaffold(
        appBar: AppBar(
          elevation: 0,
          backgroundColor: Colors.transparent,
          iconTheme: Theme.of(context).iconTheme,
          bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
          title: Text(widget._title, style: Theme.of(context).textTheme.title),
        ),
        body: WebView(
            javascriptMode: JavascriptMode.unrestricted,
            gestureRecognizers: Set()..add(Factory<WebViewGestureRecognizer>(() => WebViewGestureRecognizer())),
            onWebViewCreated: (controller) async {
              _controller = controller;
              controller.loadUrl(widget._url);
            }),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    _controller = null;
  }

  Future<bool> _handleBackPress(BuildContext context) async {
    if (await _controller?.canGoBack()) {
      _controller?.goBack();
      return Future.value(false);
    } else {
      return Future.value(true);
    }
  }
}
