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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:webview_flutter/webview_flutter.dart';

class SimpleWebViewScreen extends StatefulWidget {
  final String _url;
  final String _title;
  final String? _infoText;

  SimpleWebViewScreen(this._url, this._title, {String? infoText}) : _infoText = infoText;

  @override
  State<StatefulWidget> createState() => _SimpleWebViewScreenState();
}

class _SimpleWebViewScreenState extends State<SimpleWebViewScreen> {
  WebViewController? _controller;

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => _handleBackPress(context),
      child: Scaffold(
        appBar: AppBar(
          elevation: 0,
          backgroundColor: Colors.transparent,
          iconTheme: Theme.of(context).iconTheme,
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
          title: Text(widget._title, style: Theme.of(context).textTheme.titleLarge),
        ),
        body: WebView(
          javascriptMode: JavascriptMode.unrestricted,
          userAgent: ApiPrefs.getUserAgent(),
          gestureRecognizers: Set()..add(Factory<WebViewGestureRecognizer>(() => WebViewGestureRecognizer())),
          navigationDelegate: _handleNavigation,
          onWebViewCreated: (controller) {
            _controller = controller;
            controller.loadUrl(widget._url);
          },
          onPageFinished: _handlePageLoaded,
        ),
      ),
    );
  }

  NavigationDecision _handleNavigation(NavigationRequest request) {
    if (!request.isForMainFrame || widget._url.startsWith(request.url)) return NavigationDecision.navigate;
    return NavigationDecision.prevent;
  }

  void _handlePageLoaded(String url) async {
    // If there's no info to show, just return
    if (widget._infoText == null || widget._infoText!.isEmpty) return;

    // Run javascript to show the info alert
    await _controller?.evaluateJavascript(_showAlertJavascript);
  }

  String get _showAlertJavascript => """
      const floatNode = `<div id="flash_message_holder_mobile" style="z-index: 10000; position: fixed; bottom: 0; left: 0; right: 0; margin: 16px; width: auto;">
        <div class="ic-flash-info" aria-hidden="true" style="width: unset; max-width: 475px">
          <div class="ic-flash__icon">
            <i class="icon-info"></i>
          </div>
          ${widget._infoText}
          <button type="button" class="Button Button--icon-action close_link">
            <i class="icon-x"></i>
          </button>
        </div>
      </div>`

      \$(floatNode)
        .appendTo(\$('body')[0])
        .on('click', 'button', event => {
          \$('#flash_message_holder_mobile').remove()
        })
    """;

  @override
  void dispose() {
    super.dispose();
    _controller = null;
  }

  Future<bool> _handleBackPress(BuildContext context) async {
    if (await _controller?.canGoBack() == true) {
      _controller?.goBack();
      return Future.value(false);
    } else {
      return Future.value(true);
    }
  }
}
