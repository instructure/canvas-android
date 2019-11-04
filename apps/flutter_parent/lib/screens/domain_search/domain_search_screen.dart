/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:url_launcher/url_launcher.dart';

import 'domain_search_interactor.dart';

class DomainSearchScreen extends StatefulWidget {
  @override
  _DomainSearchScreenState createState() => _DomainSearchScreenState();
}

class _DomainSearchScreenState extends State<DomainSearchScreen> {
  var _interactor = locator<DomainSearchInteractor>();

  var _schoolDomains = List<SchoolDomain>();

  /// The minimum length of a trimmed query required to trigger a search
  static const int _MIN_SEARCH_LENGTH = 2;

  /// The trimmed user input, used when the user taps the 'Next' button
  String _query = "";

  /// The loading state
  bool _loading = false;

  /// Whether there was an error fetching the search results
  bool _error = false;

  /// The current query, tracked to help prevent race conditions when a previous search completes after a more recent search
  String currentQuery;

  final TextEditingController _inputController = TextEditingController();

  _searchDomains(String query) async {
    var thisQuery = query.trim();
    setState(() => _query = thisQuery);

    if (thisQuery.length < _MIN_SEARCH_LENGTH) thisQuery = "";

    if (thisQuery == currentQuery) return; // Do nothing if the search query has not effectively changed

    currentQuery = thisQuery;

    if (thisQuery.isEmpty) {
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
      await _interactor.performSearch(thisQuery).then((domains) {
        if (currentQuery != thisQuery) return;
        setState(() {
          _loading = false;
          _error = false;
          _schoolDomains = domains;
        });
      }).catchError((error) {
        if (currentQuery != thisQuery) return;
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
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        textTheme: Theme.of(context).primaryTextTheme.apply(bodyColor: Colors.black),
        iconTheme: Theme.of(context).primaryIconTheme.copyWith(color: Colors.black),
        title: Text(
          AppLocalizations.of(context).findSchoolOrDistrict,
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.w500),
        ),
        elevation: 0,
        backgroundColor: Colors.white,
        actions: <Widget>[
          FlatButton(
            child: Text(AppLocalizations.of(context).next.toUpperCase()),
            textColor: Colors.blue,
            onPressed: _query.isEmpty ? null : () => _next(context),
          ),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Divider(
            height: 0,
            color: Colors.grey,
          ),
          TextField(
            maxLines: 1,
            autofocus: true,
            controller: _inputController,
            style: TextStyle(fontSize: 18),
            keyboardType: TextInputType.url,
            textInputAction: TextInputAction.go,
            onSubmitted: (_) => _next(context),
            decoration: InputDecoration(
              contentPadding: EdgeInsets.all(16),
              border: InputBorder.none,
              hintText: AppLocalizations.of(context).domainSearchInputHint,
              suffixIcon: _query.isEmpty
                  ? null
                  : IconButton(
                      key: Key("clear-query"),
                      icon: Icon(Icons.clear),
                      onPressed: () {
                        // Need to perform this post-frame due to bug while widget testing
                        // See https://github.com/flutter/flutter/issues/17647
                        WidgetsBinding.instance.addPostFrameCallback((_) {
                          _inputController.text = "";
                          _searchDomains("");
                        });
                      },
                    ),
            ),
            onChanged: (query) => _searchDomains(query),
          ),
          SizedBox(
            height: 2,
            child: LinearProgressIndicator(
              value: _loading ? null : 0,
              backgroundColor: Colors.transparent,
            ),
          ),
          Divider(
            height: 0,
            color: Colors.grey,
          ),
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
                    child: Text(AppLocalizations.of(context).noDomainResults(_query)),
                  ));
                var item = _schoolDomains[index];
                return ListTile(
                  title: Text(item.name),
                  onTap: () {
                    // TODO: Wire up WebLoginPage once implemented
                    //QuickNav.push(context, WebLoginPage(item.domain));
                  },
                );
              },
            ),
          ),
          Divider(
            height: 0,
            color: Colors.grey,
          ),
          Center(
            child: FlatButton(
              child: Text(AppLocalizations.of(context).domainSearchHelpLabel),
              textTheme: ButtonTextTheme.accent,
              onPressed: () {
                _showHelpDialog(context);
              },
            ),
          ),
        ],
      ),
    );
  }

  _showHelpDialog(BuildContext context) {
    var canvasGuidesText = AppLocalizations.of(context).canvasGuides;
    var canvasSupportText = AppLocalizations.of(context).canvasSupport;
    var body = AppLocalizations.of(context).domainSearchHelpBody(canvasGuidesText, canvasSupportText);

    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(AppLocalizations.of(context).findSchoolOrDistrict),
            content: Text.rich(
              _helpBodySpan(
                text: body,
                inputSpans: [
                  TextSpan(
                    text: canvasGuidesText,
                    style: TextStyle(color: Theme.of(context).accentColor),
                    recognizer: TapGestureRecognizer()
                      ..onTap = () => launch(
                          "https://community.canvaslms.com/docs/DOC-9902-canvas-parent-android-guide-table-of-contents"),
                  ),
                  TextSpan(
                    text: canvasSupportText,
                    style: TextStyle(color: Theme.of(context).accentColor),
                    recognizer: TapGestureRecognizer()
                      ..onTap =
                          () => launch("https://community.canvaslms.com/docs/DOC-17624-how-to-contact-canvas-support"),
                  ),
                ],
              ),
            ),
            actions: <Widget>[
              FlatButton(
                child: Text(AppLocalizations.of(context).ok),
                onPressed: () => Navigator.of(context).pop(),
              ),
            ],
          );
        });
  }

  TextSpan _helpBodySpan({@required String text, @required List<TextSpan> inputSpans}) {
    var indexedSpans = inputSpans.map((it) => MapEntry(text.indexOf(it.text), it)).toList();
    indexedSpans.sort((a, b) => a.key.compareTo(b.key));

    int index = 0;
    List<TextSpan> spans = [];

    for (var indexedSpan in indexedSpans) {
      spans.add(TextSpan(text: text.substring(index, indexedSpan.key)));
      spans.add(indexedSpan.value);
      index = indexedSpan.key + indexedSpan.value.text.length;
    }
    spans.add(TextSpan(text: text.substring(index)));

    return TextSpan(children: spans);
  }

  void _next(BuildContext context) {
    var domain = _query;
    if (domain.indexOf('.') == -1) domain += ".instructure.com";
    // TODO: Wire up WebLoginPage once implemented
    //QuickNav.push(context, WebLoginPage(domain));
  }
}
