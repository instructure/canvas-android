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
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

import 'alert_thresholds_extensions.dart';
import 'alert_thresholds_percentage_dialog.dart';

class AlertThresholdsScreen extends StatefulWidget {
  final User _student;

  AlertThresholdsScreen(this._student);

  @override
  State<StatefulWidget> createState() => AlertThresholdsState();
}

class AlertThresholdsState extends State<AlertThresholdsScreen> {
  late Future<List<AlertThreshold>?> _thresholdsFuture;
  late Future<bool> _canDeleteStudentFuture;

  Future<List<AlertThreshold>?> _loadThresholds() =>
      locator<AlertThresholdsInteractor>().getAlertThresholdsForStudent(widget._student.id);
  List<AlertThreshold?>? _thresholds = [];

  @override
  void initState() {
    _thresholdsFuture = _loadThresholds();
    _canDeleteStudentFuture = locator<AlertThresholdsInteractor>().canDeleteStudent(widget._student.id);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      useNonPrimaryAppBar: false,
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(L10n(context).alertSettings),
          bottom: ParentTheme.of(context)?.appBarDivider(),
          actions: <Widget>[_deleteOption()],
        ),
        body: FutureBuilder(
          future: _thresholdsFuture,
          builder: (context, AsyncSnapshot<List<AlertThreshold>?> snapshot) {
            Widget view;
            if (snapshot.hasError) {
              view = _error(context);
            } else if (snapshot.connectionState == ConnectionState.waiting) {
              view = LoadingIndicator();
            } else {
              if (snapshot.hasData) {
                _thresholds = snapshot.data!;
              }
              view = _body();
            }
            return view;
          },
        ),
      ),
    );
  }

  Widget _deleteOption() {
    return FutureBuilder<bool>(
      future: _canDeleteStudentFuture,
      builder: (context, snapshot) {
        if (snapshot.data != true) return Container();
        return PopupMenuButton<int>(
          key: Key('overflow-menu'),
          onSelected: (_) {
            showDeleteDialog(context);
          },
          itemBuilder: (_) => [PopupMenuItem(value: 0, child: Text(L10n(context).delete))],
        );
      },
    );
  }

  void showDeleteDialog(BuildContext context) {
    bool busy = false;
    bool error = false;
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) {
        return StatefulBuilder(
          builder: (BuildContext context, void Function(void Function()) setState) {
            return AlertDialog(
              title: Text(L10n(context).delete),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Text(L10n(context).confirmDeleteStudentMessage),
                  if (error)
                    Padding(
                      padding: const EdgeInsets.only(top: 16),
                      child: Text(
                        L10n(context).deleteStudentFailure,
                        style: Theme.of(context).textTheme.bodyLarge?.copyWith(color: ParentColors.failure),
                      ),
                    )
                ],
              ),
              actions: <Widget>[
                if (!busy)
                  TextButton(
                    child: Text(L10n(context).cancel.toUpperCase()),
                    onPressed: () => Navigator.of(context).pop()
                  ),
                if (!busy)
                  TextButton(
                    child: Text(L10n(context).delete.toUpperCase()),
                    onPressed: () async {
                      setState(() {
                        busy = true;
                        error = false;
                      });
                      var success = await locator<AlertThresholdsInteractor>().deleteStudent(widget._student.id);
                      if (success) {
                        // Pop dialog
                        Navigator.of(context).pop();
                        // Pop screen with 'true' so ManageStudentScreen knows to refresh itself
                        Navigator.of(context).pop(true);
                      } else {
                        setState(() {
                          busy = false;
                          error = true;
                        });
                      }
                    },
                  ),
                if (busy)
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Container(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
                  ),
              ],
            );
          },
        );
      },
    );
  }

  Widget _body() {
    return Column(
      children: <Widget>[
        SizedBox(
          height: 16,
        ),
        ListTile(
          leading: Avatar.fromUser(
            widget._student,
            radius: 26,
          ),
          title: UserName.fromUser(
            widget._student,
            style: Theme.of(context).textTheme.titleMedium,
          ),
        ),
        SizedBox(
          height: 12,
        ),
        Container(
            alignment: Alignment.centerLeft,
            child: Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 6,
              ),
              child: Text(
                L10n(context).alertMeWhen,
                 style: Theme.of(context).textTheme.bodyMedium,
              ),
            )),
        Expanded(
          child: SingleChildScrollView(
            child: _thresholdWidgetList(),
          ),
        )
      ],
    );
  }

  Widget _thresholdWidgetList() {
    return Column(
      children: <Widget>[
        _generateAlertThresholdTile(AlertType.courseGradeLow),
        _generateAlertThresholdTile(AlertType.courseGradeHigh),
        _generateAlertThresholdTile(AlertType.assignmentMissing),
        _generateAlertThresholdTile(AlertType.assignmentGradeLow),
        _generateAlertThresholdTile(AlertType.assignmentGradeHigh),
        _generateAlertThresholdTile(AlertType.courseAnnouncement),
        _generateAlertThresholdTile(AlertType.institutionAnnouncement),
      ],
    );
  }

  Widget _error(BuildContext context) {
    return ErrorPandaWidget(L10n(context).alertThresholdsLoadingError, () {
      setState(() {
        _thresholdsFuture = _loadThresholds();
      });
    });
  }

  Widget _generateAlertThresholdTile(AlertType type) => type.isPercentage() ? _percentageTile(type) : _switchTile(type);

  Widget _percentageTile(AlertType type) {
    int? value = int.tryParse(_thresholds?.getThreshold(type)?.threshold ?? '');
    return ListTile(
      title: Text(
        type.getTitle(context),
        style: Theme.of(context).textTheme.titleMedium,
      ),
      trailing: Text(
        value != null ? NumberFormat.percentPattern().format(value / 100) : L10n(context).never,
        style: Theme.of(context).textTheme.titleMedium?.copyWith(color: StudentColorSet.electric.light),
      ),
      onTap: () async {
        AlertThreshold? update = await showDialog(
            context: context, builder: (context) => AlertThresholdsPercentageDialog(_thresholds, type, widget._student.id));

        if (update == null) {
          // User hit cancel - do nothing
          return;
        }

        // Grab the index of the threshold, if it exists
        var idx = _thresholds?.indexWhere((threshold) => threshold?.alertType == type);

        // Update the UI
        setState(() {
          if (update.threshold != '-1') {
            // Threshold was created or updated
            if (idx == null || idx == -1) {
              // Threshold got created
              _thresholds?.add(update);
            } else {
              // Existing threshold was updated
              _thresholds?[idx] = update;
            }
          } else {
            // Threshold was either deleted or left at 'never'
            if (idx != null && idx != -1) {
              // Threshold exists but was deleted
              _thresholds?.removeAt(idx);
            }
          }
        });
      },
    );
  }

  Widget _switchTile(AlertType type) {
    AlertThreshold? threshold = _thresholds?.getThreshold(type);
    bool value = threshold != null;
    return _TalkbackSwitchTile(
      title: type.getTitle(context),
      initValue: value,
      onChange: (changed) {
        _updateThreshold(type, threshold);
      },
    );
  }

  Future<void> _updateThreshold(AlertType type, AlertThreshold? threshold) async {
    var update = await locator<AlertThresholdsInteractor>().updateAlertThreshold(type, widget._student.id, threshold);

    // Grab the index of the threshold, if it exists
    var idx = _thresholds?.indexWhere((t) => t?.alertType == type);
    setState(() {
      if (idx == null || idx == -1) {
        // Threshold got created
        _thresholds?.add(update);
      } else {
        // Existing threshold was deleted
        _thresholds?[idx] = null;
      }
    });
  }
}

/// Create a switch tile that can set it's state to have talkback read correctly. Having onChanged be a future seems to
/// update the value too late for talkback, so it reads the previous value.
class _TalkbackSwitchTile extends StatefulWidget {
  final String title;
  final bool? initValue;
  final ValueChanged<bool> onChange;

  const _TalkbackSwitchTile({required this.title, this.initValue, required this.onChange, super.key});

  @override
  _TalkbackSwitchTileState createState() => _TalkbackSwitchTileState();
}

class _TalkbackSwitchTileState extends State<_TalkbackSwitchTile> {
  late bool _value;

  @override
  void initState() {
    _value = widget.initValue ?? false;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return SwitchListTile(
      title: Text(widget.title, style: Theme.of(context).textTheme.titleMedium),
      value: _value,
      contentPadding: const EdgeInsets.fromLTRB(16, 0, 7, 0),
      onChanged: (bool state) {
        setState(() {
          _value = state;
        });
        widget.onChange(state);
      },
    );
  }
}
