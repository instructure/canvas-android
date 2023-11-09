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
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// A general purpose web view to display html content to users.
class CanvasWebView extends StatefulWidget {
  /// Flag to authenticate links in the provided html content
  final bool authContentIfNecessary;

  /// The html content to load into the webview
  final String? content;

  /// The empty description to show when the provided content is blank
  final String? emptyDescription;

  /// Flag to set the webview as fullscreen, otherwise it resizes to fit it's content size (used for embedding html content on a screen with other widgets)
  ///
  /// Note: When set to true, overscroll gestures tend to get swallowed and disables the use of a [RefreshIndicator] PTR
  final bool fullScreen;

  /// If set, delays loading the page once the webview is created
  final Future? futureDelay;

  /// The horizontal padding to add to the content being loaded
  final double horizontalPadding;

  /// The initial height to set the webview to, not used if [fullScreen] is true
  final double initialHeight;

  const CanvasWebView({
    this.authContentIfNecessary = true,
    this.content,
    this.emptyDescription,
    this.fullScreen = true,
    this.futureDelay = null,
    this.horizontalPadding = 0,
    this.initialHeight = 1,
    super.key,
  });

  @override
  _CanvasWebViewState createState() => _CanvasWebViewState();
}

class _CanvasWebViewState extends State<CanvasWebView> {
  String? _content;
  Future<String?>? _contentFuture;

  WebContentInteractor get _interactor => locator<WebContentInteractor>();

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

    return _ResizingWebView(
      contentFuture: _contentFuture,
      emptyDescription: widget.emptyDescription,
      initialHeight: widget.initialHeight,
      horizontalPadding: widget.horizontalPadding,
      fullScreen: widget.fullScreen,
      futureDelay: widget.futureDelay,
    );
  }
}

class _ResizingWebView extends StatefulWidget {
  final Future<String?>? contentFuture;
  final String? emptyDescription;

  final double initialHeight;
  final double horizontalPadding;

  final bool fullScreen;
  final Future? futureDelay;

  const _ResizingWebView({
    this.contentFuture,
    required this.emptyDescription,
    required this.initialHeight,
    required this.horizontalPadding,
    required this.fullScreen,
    required this.futureDelay,
    super.key
  });

  @override
  _ResizingWebViewState createState() => _ResizingWebViewState();
}

class _ResizingWebViewState extends State<_ResizingWebView> with WidgetsBindingObserver {
  String? _content;
  WebViewController? _controller;
  late double _height;
  late bool _loading;
  late bool _inactive;

  WebContentInteractor get _interactor => locator<WebContentInteractor>();

  @override
  void initState() {
    _height = widget.initialHeight;

    // We avoid using _loading in tests due to the complexity of mocking its dependencies.
    // We determine if we're in a test by checking the runtime type of WidgetsBinding. In prod it's an instance of
    // WidgetsFlutterBinding and in tests it's an instance of AutomatedTestWidgetsFlutterBinding.
    var isTest = WidgetsBinding.instance.runtimeType != WidgetsFlutterBinding;

    _loading = true && !isTest;
    _inactive = false;

    WidgetsBinding.instance.addObserver(this);
    super.initState();
  }

  @override
  void dispose() {
    _controller = null;
    WidgetsBinding.instance.removeObserver(this);

    super.dispose();
  }

  /// Adding this observer method to watch for inactive states, which can break the webview. More on this discussion
  /// can be found here: https://github.com/flutter/flutter/issues/28651
  ///
  /// There were fix attempts by the flutter team to prevent the JNI link error, but we can still reproduce by doing
  /// the following steps:
  /// Kill the app -> Click a link that goes to a screen with a webview (assignment details) -> Open in parent app ->
  /// Press back -> Click a link -> Open in parent app -> App crashes
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    print(state);
    switch (state) {
      case AppLifecycleState.inactive:
        setState(() => _inactive = true);
        break;
      case AppLifecycleState.resumed:
        setState(() => _inactive = false);
        break;
      default:
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: widget.contentFuture,
      builder: (context, AsyncSnapshot<String?> snapshot) => FutureBuilder(
        future: widget.futureDelay,
        builder: (context, delay) {
          // We're delaying if we're still waiting for data (besides a pull to refresh, show the previous while waiting for new data)
          final delaying = (snapshot.connectionState == ConnectionState.waiting && !snapshot.hasData) ||
              delay.connectionState == ConnectionState.waiting;

          // If there is no content, and we're not delaying, then we can stop showing loading since pageFinished will never get called
          final emptyContent = (snapshot.data == null || snapshot.data!.isEmpty);
          if (!delaying && emptyContent) _loading = false;

          return Stack(
            children: <Widget>[
              if (!delaying) _contentBody(snapshot.data, emptyContent),
              if (_loading || delaying)
                // Wrap in a container that's the scaffold color so we don't get the gross black bar while webview is initializing
                Container(
                  color: Theme.of(context).scaffoldBackgroundColor,
                  child: LoadingIndicator(),
                ),
            ],
          );
        },
      ),
    );
  }

  Widget _contentBody(String? widgetContent, bool emptyContent) {
    // Check for empty content
    if (emptyContent) {
      if (widget.emptyDescription?.isEmpty ?? true) {
        return Container(); // No empty text, so just be empty
      } else {
        return Padding(
          padding: EdgeInsets.symmetric(horizontal: widget.horizontalPadding),
          child: Text(widget.emptyDescription ?? '', style: Theme.of(context).textTheme.bodyMedium),
        );
      }
    }

    // If we are not active, don't display a webview
    if (_inactive) {
      return Container();
    }

    // Handle being rebuilt by parent widgets (refresh)
    if (_content != widgetContent) {
      _height = widget.initialHeight;
      _content = widgetContent!;
      _controller?.loadHtml(_content,
          horizontalPadding: widget.horizontalPadding,
          darkMode: ParentTheme.of(context)?.isWebViewDarkMode ?? false);
    }

    Widget child = WebView(
      javascriptMode: JavascriptMode.unrestricted,
      onPageFinished: _handlePageLoaded,
      onWebViewCreated: _handleWebViewCreated,
      navigationDelegate: _handleNavigation,
      gestureRecognizers: _webViewGestures(),
      javascriptChannels: _webViewChannels(),
    );

    if (!widget.fullScreen) {
      child = ConstrainedBox(
        constraints: BoxConstraints(maxHeight: _height),
        child: child,
      );
    }

    return child;
  }

  Future<NavigationDecision> _handleNavigation(NavigationRequest request) async {
    // Otherwise, we'll let the router handle it
    locator<QuickNav>().routeInternally(context, request.url);
    return NavigationDecision.prevent;
  }

  void _handleWebViewCreated(WebViewController webViewController) async {
    webViewController.loadHtml(_content,
        baseUrl: ApiPrefs.getDomain(),
        horizontalPadding: widget.horizontalPadding,
        darkMode: ParentTheme.of(context)?.isWebViewDarkMode ?? false);
    _controller = webViewController;
  }

  void _handlePageLoaded(String url) async {
    if (!mounted) return;

    setState(() => _loading = false);

    // Don't resize if fullscreen, or if a link was clicked
    // TODO: May not need initialHeight check for links with new navigation request handling
    if (widget.fullScreen || _height != widget.initialHeight) return;
    final height = double.tryParse((await _controller?.evaluateJavascript('document.documentElement.scrollHeight;')) ??
            (widget.initialHeight + 1).toString()) ??
        widget.initialHeight;

    // TODO: May need to revisit this in the future, on an emulator I was able to get an out of resource exception for too big of a canvas
    // here for creating too large of a webview. Doesn't seem to happen on a real device, will have to monitor crashes.
    // One possible idea is to set to let the max height of the content be the height of the available space (wrapping
    // the ConstrainedBox in a LayoutBuilder to size appropriately). Though this would require adding the
    // WebViewGestureRecognizer, which doesn't support PTR, and would need some extra flag besides just fullScreen.
    if (!mounted) return; // Possible race condition here
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
        PandaRouter.routeInternally(context, message.message);
      }));
  }
}
