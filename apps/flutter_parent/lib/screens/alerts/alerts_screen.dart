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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

class AlertsScreen extends StatefulWidget {
  final _interactor = locator<AlertsInteractor>();
  final User _student;

  AlertsScreen(this._student, {Key key}) : super(key: key);

  @override
  _AlertsScreenState createState() => _AlertsScreenState();
}

class _AlertsScreenState extends State<AlertsScreen> {
  Future<List<Alert>> _alertsFuture;

  Future<List<Alert>> _loadAlerts() => widget._interactor.getAlertsForStudent(widget._student.id);

  @override
  void initState() {
    super.initState();
    _alertsFuture = _loadAlerts();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _alertsFuture,
      builder: (context, AsyncSnapshot<List<Alert>> snapshot) {
        // Show loading if we're waiting for data, not inside the refresh indicator as it's unnecessary
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(child: CircularProgressIndicator());
        }

        // Get the child widget to show in the refresh indicator
        Widget child;
        if (snapshot.hasError) {
          child = _error(context);
        } else if (snapshot.data == null || snapshot.data.isEmpty) {
          child = _empty(context);
        } else {
          child = _AlertsList(snapshot.data);
        }

        return RefreshIndicator(
          onRefresh: () {
            _alertsFuture = _loadAlerts();
            setState(() {});
            return _alertsFuture;
          },
          child: child,
        );
      },
    );
  }

  Widget _error(BuildContext context) {
    return FullScreenScrollContainer(children: [Text(AppLocalizations.of(context).unexpectedError)]);
  }

  Widget _empty(BuildContext context) {
    return FullScreenScrollContainer(children: [Text(AppLocalizations.of(context).noAlertsMessage)]);
  }
}

/// A helper widget to handle updating read status of alerts, and displaying as a list
class _AlertsList extends StatefulWidget {
  final _interactor = locator<AlertsInteractor>();
  final List<Alert> _alerts;

  _AlertsList(this._alerts, {Key key}) : super(key: key);

  @override
  __AlertsListState createState() => __AlertsListState();
}

class __AlertsListState extends State<_AlertsList> {
  List<Alert> _alerts;

  @override
  void initState() {
    super.initState();
    _alerts = widget._alerts;
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: _alerts.length,
      itemBuilder: (context, index) => _alertTile(context, _alerts[index], index),
    );
  }

  Widget _alertTile(BuildContext context, Alert alert, int index) {
    return ListTile(
      leading: Icon(_alertIcon(alert), color: _alertColor(context, alert), size: 20),
      title: Text(alert.title),
      subtitle: Text(_formatDate(context, alert.actionDate)),
      onTap: () => _routeAlert(alert, index),
    );
  }

  /// Utilities

  IconData _alertIcon(Alert alert) {
    if (alert.isAlertInfo() || alert.isAlertPositive()) return CanvasIcons.info;
    if (alert.isAlertNegative()) return CanvasIcons.warning;

    return CanvasIcons.warning;
  }

  Color _alertColor(BuildContext context, Alert alert) {
    if (alert.isAlertInfo()) return ParentTheme.ash;
    if (alert.isAlertPositive()) return ParentTheme.of(context).defaultTheme.accentColor;
    if (alert.isAlertNegative()) return ParentTheme.failure;

    return ParentTheme.failure;
  }

  String _formatDate(BuildContext context, DateTime date) {
    return DateFormat(AppLocalizations.of(context).dateTimeFormat).format(date.toLocal());
  }

  void _routeAlert(Alert alert, int index) async {
    final readAlert = await widget._interactor.markAlertRead(alert.id);
    setState(() => _alerts.setRange(index, index + 1, [readAlert]));

    // TODO: Show detail page for alert
    // TODO: Update alerts badge count, when we implement that feature
  }
}
