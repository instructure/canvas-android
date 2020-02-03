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
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:webview_flutter/webview_flutter.dart';

class ConstrainedWebView extends StatefulWidget {
  final String content;
  final String emptyDescription;
  final double initialHeight;
  final double horizontalPadding;

  const ConstrainedWebView(
      {Key key, @required this.content, this.emptyDescription, this.initialHeight = 10, this.horizontalPadding = 0})
      : assert(initialHeight != null),
        assert(horizontalPadding != null),
        super(key: key);

  @override
  _ConstrainedWebViewState createState() => _ConstrainedWebViewState();
}

class _ConstrainedWebViewState extends State<ConstrainedWebView> {
  double _height;
  WebViewController _controller;

  @override
  void initState() {
    _height = widget.initialHeight;
    super.initState();
  }

  @override
  void dispose() {
    _controller = null;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final content = widget.content;

    if (content == null || content.isEmpty) {
      if (widget.emptyDescription == null || widget.emptyDescription.isEmpty) {
        return Container(); // No empty text, so just be empty
      } else {
        return Padding(
          padding: EdgeInsets.symmetric(horizontal: widget.horizontalPadding),
          child: Text(widget.emptyDescription, style: textTheme.body1),
        );
      }
    }

    return ConstrainedBox(
      constraints: BoxConstraints(maxHeight: _height),
      child: WebView(
        javascriptMode: JavascriptMode.unrestricted,
        onPageFinished: (url) async {
          if (!url.startsWith('data:text/html')) return; // An attempt to make links not resize the webview and crash
          final height =
              double.parse(await _controller?.evaluateJavascript('document.documentElement.scrollHeight;') ?? '0');

          setState(() => _height = height);
        },
        onWebViewCreated: (WebViewController webViewController) async {
          webViewController.loadHtml(content, horizontalPadding: widget.horizontalPadding);
          _controller = webViewController;
        },
      ),
    );
  }
}
