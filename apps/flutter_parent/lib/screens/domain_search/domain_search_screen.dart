// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/debouncer.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'domain_search_interactor.dart';

class DomainSearchScreen extends StatefulWidget {
  @visibleForTesting
  static final GlobalKey helpDialogBodyKey = GlobalKey();

  const DomainSearchScreen({this.loginFlow, super.key});

  final LoginFlow? loginFlow;

  @override
  _DomainSearchScreenState createState() => _DomainSearchScreenState();
}

class _DomainSearchScreenState extends State<DomainSearchScreen> {
  var _interactor = locator<DomainSearchInteractor>();

  var _schoolDomains = [];

  /// The minimum length of a trimmed query required to trigger a search
  static const int _MIN_SEARCH_LENGTH = 2;

  /// The trimmed user input, used when the user taps the 'Next' button
  String _query = '';

  /// The loading state
  bool _loading = false;

  /// Whether there was an error fetching the search results
  bool _error = false;

  /// The current query, tracked to help prevent race conditions when a previous search completes after a more recent search
  String? _currentQuery;

  Debouncer _debouncer = Debouncer(Duration(milliseconds: 500));

  final TextEditingController _inputController = TextEditingController();

  _searchDomains(String query) async {
    if (query.length < _MIN_SEARCH_LENGTH) query = '';

    if (query == _currentQuery) return; // Do nothing if the search query has not effectively changed

    _currentQuery = query;

    if (query.isEmpty) {
      setState(() {
        _loading = false;
        _error = false;
        _schoolDomains = [];
      });
    } else {
      setState(() {
        _loading = true;
        _error = false;
      });
      await _interactor.performSearch(query).then((domains) {
        if (_currentQuery != query) return;
        setState(() {
          _loading = false;
          _error = false;
          if (domains != null) _schoolDomains = domains;
        });
      }).catchError((error) {
        if (_currentQuery != query) return;
        setState(() {
          _loading = false;
          _error = true;
          _schoolDomains = [];
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(
            L10n(context).findSchool,
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w500),
          ),
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
          actions: <Widget>[
            MaterialButton(
              minWidth: 20,
              highlightColor: Colors.transparent,
              splashColor: Theme.of(context).colorScheme.secondary.withAlpha(100),
              textColor: Theme.of(context).colorScheme.secondary,
              shape: CircleBorder(side: BorderSide(color: Colors.transparent)),
              onPressed: _query.isEmpty ? null : () => _next(context),
              child: Text(
                L10n(context).next,
                textAlign: TextAlign.end,
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            TextField(
              maxLines: 1,
              autofocus: true,
              key: Key("FindSchoolTextField"),
              controller: _inputController,
              style: TextStyle(fontSize: 18),
              keyboardType: TextInputType.url,
              textInputAction: TextInputAction.go,
              onSubmitted: (_) => _query.isNotEmpty ? _next(context) : null,
              decoration: InputDecoration(
                contentPadding: EdgeInsets.all(16),
                border: InputBorder.none,
                hintText: L10n(context).domainSearchInputHint,
                suffixIcon: _query.isEmpty
                    ? null
                    : IconButton(
                        key: Key('clear-query'),
                        icon: Icon(
                          Icons.clear,
                          color: ParentColors.ash,
                        ),
                        onPressed: () {
                          // Need to perform this post-frame due to bug while widget testing
                          // See https://github.com/flutter/flutter/issues/17647
                          WidgetsBinding.instance.addPostFrameCallback((_) {
                            _inputController.text = '';
                            _searchDomains('');
                          });
                        },
                      ),
              ),
              onChanged: (query) {
                // Update the state for the new query, then debounce the search request
                var thisQuery = query.trim();
                setState(() => _query = thisQuery);
                _debouncer.debounce(() => _searchDomains(query));
              },
            ),
            SizedBox(
              height: 2,
              child: LinearProgressIndicator(
                value: _loading ? null : 0,
                backgroundColor: Colors.transparent,
              ),
            ),
            Divider(height: 0),
            Flexible(
              flex: 10000,
              child: ListView.separated(
                shrinkWrap: true,
                separatorBuilder: (context, index) => Divider(
                  height: 0,
                ),
                itemCount: _schoolDomains.length + (_error ? 1 : 0),
                itemBuilder: (context, index) {
                  if (_error)
                    return Center(
                        child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(L10n(context).noDomainResults(_query)),
                    ));
                  var item = _schoolDomains[index];
                  return ListTile(
                    title: Text(item.name),
                    onTap: () async {
                      final accountName = (item.name == null || item.name.isEmpty) ? item.domain : item.name;
                      return locator<QuickNav>().pushRoute(context,
                        PandaRouter.loginWeb(item.domain, accountName: accountName, authenticationProvider: item.authenticationProvider));
                    },
                  );
                },
              ),
            ),
            if (_schoolDomains.isNotEmpty) Divider(height: 0),
            Center(
              child: TextButton(
                key: Key('help-button'),
                child: Text(L10n(context).domainSearchHelpLabel),
                style: TextButton.styleFrom(
                  textStyle: TextStyle(color: Theme.of(context).colorScheme.secondary),
                ),
                onPressed: () {
                  _showHelpDialog(context);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  _showHelpDialog(BuildContext context) {
    var canvasGuidesText = L10n(context).canvasGuides;
    var canvasSupportText = L10n(context).canvasSupport;
    var body = L10n(context).domainSearchHelpBody(canvasGuidesText, canvasSupportText);

    locator<Analytics>().logEvent(AnalyticsEventConstants.HELP_DOMAIN_SEARCH);
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(L10n(context).findSchool),
            content: Text.rich(
              _helpBodySpan(
                text: body,
                inputSpans: [
                  TextSpan(
                    text: canvasGuidesText,
                    style: TextStyle(color: Theme.of(context).colorScheme.secondary),
                    recognizer: TapGestureRecognizer()..onTap = _interactor.openCanvasGuides,
                  ),
                  TextSpan(
                    text: canvasSupportText,
                    style: TextStyle(color: Theme.of(context).colorScheme.secondary),
                    recognizer: TapGestureRecognizer()..onTap = _interactor.openCanvasSupport,
                  ),
                ],
              ),
              key: DomainSearchScreen.helpDialogBodyKey,
            ),
            actions: <Widget>[
              TextButton(
                child: Text(L10n(context).ok),
                onPressed: () => Navigator.of(context).pop(),
              ),
            ],
          );
        });
  }

  TextSpan _helpBodySpan({required String text, required List<TextSpan> inputSpans}) {
    var indexedSpans = inputSpans.map((it) => MapEntry(text.indexOf(it.text!), it)).toList();
    indexedSpans.sort((a, b) => a.key.compareTo(b.key));

    int index = 0;
    List<TextSpan> spans = [];

    for (var indexedSpan in indexedSpans) {
      spans.add(TextSpan(text: text.substring(index, indexedSpan.key)));
      spans.add(indexedSpan.value);
      index = indexedSpan.key + indexedSpan.value.text!.length;
    }
    spans.add(TextSpan(text: text.substring(index)));

    return TextSpan(children: spans);
  }

  void _next(BuildContext context) {
    String domain = _query;
    RegExp regExp = new RegExp(r'(.*)([a-zA-Z0-9]){1}');
    if (regExp.hasMatch(domain)) domain = regExp.stringMatch(domain)!;
    if (domain.startsWith('www.')) domain = domain.substring(4); // Strip off www. if they typed it
    if (!domain.contains('.') || domain.endsWith('.beta')) domain += '.instructure.com';
    if (widget.loginFlow != null)
      locator<QuickNav>().pushRoute(context, PandaRouter.loginWeb(domain, accountName: domain, loginFlow: widget.loginFlow!));
    else
      locator<QuickNav>().pushRoute(context, PandaRouter.loginWeb(domain, accountName: domain));
  }
}
