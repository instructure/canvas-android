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
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/simple_web_view_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_view_interactor.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// A general purpose web view to display html content to users.
class CanvasWebView extends StatefulWidget {
  /// The html content to load into the webview
  final String content;

  /// The empty description to show when the provided content is blank
  final String emptyDescription;

  /// The title string passed into [SimpleWebViewScreen] when navigating on a link click. If not provided, links will
  /// load in this webview
  final String navigationTitle;

  /// The initial height to set the webview to, not used if [fullScreen] is true
  final double initialHeight;

  /// The horizontal padding to add to the content being loaded
  final double horizontalPadding;

  /// Flag to authenticate links in the provided html content
  final bool authContentIfNecessary;

  /// Flag to set the webview as fullscreen, otherwise it resizes to fit it's content size (used for embedding html content on a screen with other widgets)
  ///
  /// Note: When set to true, overscroll gestures tend to get swallowed and disables the use of a [RefreshIndicator] PTR
  final bool fullScreen;

  const CanvasWebView({
    Key key,
    this.content,
    this.emptyDescription,
    this.navigationTitle,
    this.initialHeight = 1,
    this.horizontalPadding = 0,
    this.authContentIfNecessary = true,
    this.fullScreen = true,
  })  : assert(initialHeight != null),
        assert(horizontalPadding != null),
        assert(authContentIfNecessary != null),
        assert(fullScreen != null),
        super(key: key);

  @override
  _CanvasWebViewState createState() => _CanvasWebViewState();
}

class _CanvasWebViewState extends State<CanvasWebView> {
  String _content;
  Future<String> _contentFuture;

  WebViewInteractor get _interactor => locator<WebViewInteractor>();

  @override
  void initState() {
    _content = widget.content;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    if (!widget.authContentIfNecessary) {
      _contentFuture = Future.value(widget.content);
    } else if (_contentFuture == null || _content != widget.content) {
      // If we don't have a future or if the content has changed (i.e., PTR changed the data), update the content future
      _content = widget.content;
      _contentFuture = _interactor.authContent(_content, L10n(context).launchExternalTool);
    }

    return FutureBuilder(
      future: _contentFuture,
      builder: (context, AsyncSnapshot<String> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting && !snapshot.hasData) return LoadingIndicator();

        return _ResizingWebView(
          content: snapshot.data,
          emptyDescription: widget.emptyDescription,
          navigationTitle: widget.navigationTitle,
          initialHeight: widget.initialHeight,
          horizontalPadding: widget.horizontalPadding,
          fullScreen: widget.fullScreen,
        );
      },
    );
  }
}

class _ResizingWebView extends StatefulWidget {
  final String content;
  final String emptyDescription;
  final String navigationTitle;

  final double initialHeight;
  final double horizontalPadding;

  final bool fullScreen;

  const _ResizingWebView({
    Key key,
    this.content,
    this.emptyDescription,
    this.navigationTitle,
    this.initialHeight,
    this.horizontalPadding,
    this.fullScreen,
  }) : super(key: key);

  @override
  _ResizingWebViewState createState() => _ResizingWebViewState();
}

class _ResizingWebViewState extends State<_ResizingWebView> {
  double _height;
  String _content;
  WebViewController _controller;

  WebViewInteractor get _interactor => locator<WebViewInteractor>();

  @override
  void initState() {
    _height = widget.initialHeight;
    _content = widget.content;

    super.initState();
  }

  @override
  void dispose() {
    _controller = null;

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Check for empty content
    if (widget.content == null || widget.content.isEmpty) {
      if (widget.emptyDescription == null || widget.emptyDescription.isEmpty) {
        return Container(); // No empty text, so just be empty
      } else {
        return Padding(
          padding: EdgeInsets.symmetric(horizontal: widget.horizontalPadding),
          child: Text(widget.emptyDescription, style: Theme.of(context).textTheme.body1),
        );
      }
    }

    // Handle being rebuilt by parent widgets
    if (_content != widget.content) {
      _height = widget.initialHeight;
      _content = widget.content;
      _controller?.loadHtml(_content, horizontalPadding: widget.horizontalPadding);
    }

    // Unfortunately, there is an issue where the web view will load a black screen first before it's initialized. This
    // is a known problem and can be followed here: https://github.com/flutter/flutter/issues/27198
    final webView = WebView(
      javascriptMode: JavascriptMode.unrestricted,
      onPageFinished: _handlePageLoaded,
      onWebViewCreated: _handleWebViewCreated,
      navigationDelegate: _handleNavigation,
      gestureRecognizers: _webViewGestures(),
      javascriptChannels: _webViewChannels(),
    );

    if (widget.fullScreen) {
      return webView;
    } else {
      return ConstrainedBox(
        constraints: BoxConstraints(maxHeight: _height),
        child: webView,
      );
    }
  }

  Future<NavigationDecision> _handleNavigation(NavigationRequest request) async {
    // TODO: Wire in routing here when it's ready
    if (widget.navigationTitle == null) return NavigationDecision.navigate;

    String url = await _interactor.getAuthUrl(request.url);

    locator.get<QuickNav>().push(context, SimpleWebViewScreen(url, widget.navigationTitle));
    return NavigationDecision.prevent;
  }

  void _handleWebViewCreated(WebViewController webViewController) async {
    webViewController.loadHtml(_content, baseUrl: ApiPrefs.getDomain(), horizontalPadding: widget.horizontalPadding);
    _controller = webViewController;
  }

  void _handlePageLoaded(String url) async {
    // Don't resize if fullscreen, or if a link was clicked
    // TODO: May not need initialHeight check for links with new navigation request handling
    if (widget.fullScreen || _height != widget.initialHeight) return;
    final height = double.parse(await _controller?.evaluateJavascript('document.documentElement.scrollHeight;') ??
        (widget.initialHeight + 1).toString());

    // TODO: May need to revisit this in the future, on an emulator I was able to get an out of resource exception for too big of a canvas
    // here for creating too large of a webview. Doesn't seem to happen on a real device, will have to monitor crashes.
    // One possible idea is to set to let the max height of the content be the height of the available space (wrapping
    // the ConstrainedBox in a LayoutBuilder to size appropriately). Though this would require adding the
    // WebViewGestureRecognizer, which doesn't support PTR, and would need some extra flag besides just fullScreen.
    setState(() => _height = height);
  }

  Set<Factory<OneSequenceGestureRecognizer>> _webViewGestures() {
    final gestures = Set<Factory<OneSequenceGestureRecognizer>>();

    // Only add the gesture recognizer if we're a full screen webview, otherwise PTR detection gets swallowed
    if (widget.fullScreen) {
      gestures.add(Factory<WebViewGestureRecognizer>(() => WebViewGestureRecognizer()));
    }

    return gestures;
  }

  Set<JavascriptChannel> _webViewChannels() {
    return Set()
      ..add(_interactor.ltiToolPressedChannel((JavascriptMessage message) {
        // TODO: Create an LTI webview that handles this better, so we don't have to just launch them out to a browser
        launch(message.message);
      }));
  }
}
