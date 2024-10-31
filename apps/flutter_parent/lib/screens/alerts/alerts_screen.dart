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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_extensions.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:provider/provider.dart';

class AlertsScreen extends StatefulWidget {
  final _interactor = locator<AlertsInteractor>();

  @override
  _AlertsScreenState createState() => _AlertsScreenState();
}

class _AlertsScreenState extends State<AlertsScreen> {
  Future<AlertsList?>? _alertsFuture;
  late User _student;

  Future<AlertsList?> _loadAlerts({bool forceRefresh = false}) =>
      widget._interactor.getAlertsForStudent(_student.id, forceRefresh);

  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    var _selectedStudent = Provider.of<SelectedStudentNotifier>(context, listen: true).value!;
    if (_alertsFuture == null) {
      // First time
      _student = _selectedStudent;
      _alertsFuture = _loadAlerts();
    }

    if (_student != _selectedStudent) {
      // The student was changed by the user, get the new alerts
      _student = _selectedStudent;
      _alertsFuture = _loadAlerts(forceRefresh: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      key: _refreshKey,
      future: _alertsFuture,
      builder: (context, AsyncSnapshot<AlertsList?> snapshot) {
        // Show loading if we're waiting for data, not inside the refresh indicator as it's unnecessary
        if (snapshot.connectionState == ConnectionState.waiting) {
          return LoadingIndicator();
        }

        // Get the child widget to show in the refresh indicator
        Widget child;
        if (snapshot.hasError) {
          child = _error(context);
        } else {
          child = _AlertsList(_student, snapshot.data);
        }

        return RefreshIndicator(
          onRefresh: () async {
            _alertsFuture = _loadAlerts(forceRefresh: true);
            setState(() {});
            await _alertsFuture;
          },
          child: child,
        );
      },
    );
  }

  Widget _error(BuildContext context) {
    return FullScreenScrollContainer(children: [Text(L10n(context).unexpectedError)]);
  }
}

/// A helper widget to handle updating read status of alerts, and displaying as a list
class _AlertsList extends StatefulWidget {
  final _interactor = locator<AlertsInteractor>();
  final AlertsList? _data;
  final User _student;

  _AlertsList(this._student, this._data, {super.key});

  @override
  __AlertsListState createState() => __AlertsListState();
}

class __AlertsListState extends State<_AlertsList> {
  GlobalKey<AnimatedListState> _listKey = GlobalKey();
  AlertsList? _data;

  @override
  void initState() {
    super.initState();
    _data = widget._data;
  }

  @override
  Widget build(BuildContext context) {
    if (_data == null || _data?.alerts == null || _data?.alerts?.isEmpty == true) {
      return _empty(context);
    } else {
      return AnimatedList(
        key: _listKey,
        initialItemCount: _data!.alerts!.length,
        itemBuilder: (context, index, animation) => _alertTile(context, _data!.alerts![index], index),
      );
    }
  }

  Widget _alertTile(BuildContext context, Alert alert, int index, {Animation? animation = null}) {
    final textTheme = Theme.of(context).textTheme;
    final alertColor = _alertColor(context, alert);
    Widget tile = InkWell(
      onTap: () => _routeAlert(alert, index),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(width: 18),
          Padding(
            padding: const EdgeInsets.only(top: 12),
            child: Icon(_alertIcon(alert), color: alertColor, size: 20),
          ),
          SizedBox(width: 34),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                SizedBox(height: 16),
                Text(_alertTitle(context, alert), style: textTheme.titleSmall?.copyWith(color: alertColor)),
                SizedBox(height: 4),
                Text(alert.title, style: textTheme.titleMedium),
                SizedBox(height: 4),
                Text(_formatDate(context, alert.actionDate!) ?? '', style: textTheme.titleSmall),
                SizedBox(height: 12),
              ],
            ),
          ),
          SizedBox(width: 16),
          IconButton(
            tooltip: L10n(context).dismissAlertLabel(alert.title),
            color: ParentColors.ash,
            padding: EdgeInsets.all(0), // No extra padding, we're already padded enough with min touch size
            icon: Icon(Icons.clear, size: 20),
            onPressed: () => _dismissAlert(alert),
          ),
        ],
      ),
    );

    if (alert.workflowState == AlertWorkflowState.unread) {
      tile = WidgetBadge(tile);
    }

    if (animation != null) {
      tile = SizeTransition(
        sizeFactor: animation as Animation<double>,
        axis: Axis.vertical,
        child: tile,
      );
    }

    return tile;
  }

  /// Utilities

  Widget _empty(BuildContext context) {
    return EmptyPandaWidget(
      svgPath: 'assets/svg/panda-no-alerts.svg',
      title: L10n(context).noAlertsTitle,
      subtitle: L10n(context).noAlertsMessage,
    );
  }

  IconData _alertIcon(Alert alert) {
    if (alert.lockedForUser) return CanvasIcons.lock;
    if (alert.isAlertInfo() || alert.isAlertPositive()) return CanvasIcons.info;
    if (alert.isAlertNegative()) return CanvasIcons.warning;

    return CanvasIcons.warning;
  }

  Color _alertColor(BuildContext context, Alert alert) {
    if (alert.isAlertInfo()) return ParentColors.ash;
    if (alert.isAlertPositive()) return ParentTheme.of(context)!.defaultTheme.colorScheme.secondary;
    if (alert.isAlertNegative()) return ParentColors.failure;

    return ParentColors.failure;
  }

  String _alertTitle(BuildContext context, Alert alert) {
    final l10n = L10n(context);
    final threshold = _data?.thresholds?.getThreshold(alert.alertType)?.threshold ?? '';
    String title = '';
    switch (alert.alertType) {
      case AlertType.institutionAnnouncement:
        title = l10n.globalAnnouncement;
        break;
      case AlertType.courseAnnouncement:
        title = l10n.courseAnnouncement;
        break;
      case AlertType.assignmentMissing:
        title = l10n.assignmentMissing;
        break;
      case AlertType.courseGradeHigh:
        title = l10n.courseGradeAboveThreshold(threshold);
        break;
      case AlertType.courseGradeLow:
        title = l10n.courseGradeBelowThreshold(threshold);
        break;
      case AlertType.assignmentGradeHigh:
        title = l10n.assignmentGradeAboveThreshold(threshold);
        break;
      case AlertType.assignmentGradeLow:
        title = l10n.assignmentGradeBelowThreshold(threshold);
        break;
    }

    if (alert.lockedForUser) {
      title = '$title • ${L10n(context).lockedForUserTitle}';
    }
    return title;
  }

  String? _formatDate(BuildContext context, DateTime date) {
    return date.l10nFormat(L10n(context).dateAtTime);
  }

  void _routeAlert(Alert alert, int index) async {
    if (alert.lockedForUser) {
      final snackBar = SnackBar(content: Text(L10n(context).lockedForUserError));
      ScaffoldMessenger.of(context).showSnackBar(snackBar);
    } else {
      if (alert.alertType == AlertType.institutionAnnouncement) {
        locator<QuickNav>().pushRoute(context,
            PandaRouter.institutionAnnouncementDetails(alert.contextId));
      } else {
        locator<QuickNav>().routeInternally(context, alert.htmlUrl);
      }

      // We're done if the alert was already read, otherwise mark it read
      if (alert.workflowState == AlertWorkflowState.read) return;

      final readAlert = await widget._interactor.markAlertRead(
          widget._student.id, alert.id);
      setState(() => _data!.alerts!.setRange(index, index + 1, [readAlert!]));
      locator<AlertCountNotifier>().update(widget._student.id);
    }
  }

  void _dismissAlert(Alert alert) async {
    _markAlertDismissed(alert);

    int itemIndex = _data!.alerts!.indexOf(alert);

    _listKey.currentState?.removeItem(
        itemIndex, (context, animation) => _alertTile(context, alert, itemIndex, animation: animation),
        duration: const Duration(milliseconds: 200));

    setState(() => _data!.alerts!.remove(alert));
  }

  void _markAlertDismissed(Alert alert) async {
    await widget._interactor.markAlertDismissed(widget._student.id, alert.id);

    // Update the unread count if the alert was unread
    if (alert.workflowState == AlertWorkflowState.unread) {
      locator<AlertCountNotifier>().update(widget._student.id);
    }
  }
}
