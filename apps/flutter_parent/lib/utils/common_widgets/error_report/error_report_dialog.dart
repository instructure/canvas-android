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
import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../arrow_aware_focus_scope.dart';

class ErrorReportDialog extends StatefulWidget {
  static const Key subjectKey = Key('subject');
  static const Key descriptionKey = Key('description');
  static const Key emailKey = Key('email');

  final String title; // Used to specify different titles depending on how this dialog was shown
  final String? subject;
  final ErrorReportSeverity? severity;
  final FlutterErrorDetails? error;
  final bool includeEmail; // Used when shown during login, so that users can get responses from created service tickets
  final bool hideSeverityPicker;

  const ErrorReportDialog._internal(
    this.title,
    this.subject,
    this.severity,
    this.includeEmail,
    this.hideSeverityPicker,
    this.error, {
    super.key,
  });

  @override
  _ErrorReportDialogState createState() => _ErrorReportDialogState();

  static Future<void> asDialog(
      BuildContext context, {
      String? title,
      String? subject,
      ErrorReportSeverity? severity,
      bool includeEmail = false,
      bool hideSeverityPicker = false,
      FlutterErrorDetails? error}) {
    return showDialog(
      context: context,
      builder: (context) => ErrorReportDialog._internal(
        title ?? L10n(context).reportProblemTitle,
        subject,
        severity ?? ErrorReportSeverity.COMMENT,
        includeEmail,
        hideSeverityPicker,
        error,
      ),
    );
  }
}

class _ErrorReportDialogState extends State<ErrorReportDialog> {
  final _formKey = GlobalKey<FormState>();

  // Non state changing variables
  FocusScopeNode _focusScopeNode = FocusScopeNode();
  String? _subject;
  String? _email;
  String? _description;

  // State changing variables
  late ErrorReportSeverity? _selectedSeverity;
  late bool _autoValidate;

  @override
  void initState() {
    super.initState();

    _autoValidate = false;
    _subject = widget.subject;
    _selectedSeverity = widget.severity;
  }

  @override
  void dispose() {
    _focusScopeNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FullScreenScrollContainer(
      horizontalPadding: 0,
      children: <Widget>[
        AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
          title: Text(widget.title),
          actions: <Widget>[
            TextButton(
              child: Text(L10n(context).cancel.toUpperCase()),
              onPressed: () => Navigator.of(context).pop(),
            ),
            TextButton(
              child: Text(L10n(context).sendReport.toUpperCase()),
              onPressed: () async {
                if (_formKey.currentState?.validate() == true) {
                  await _submitReport();
                  Navigator.of(context).pop();
                } else {
                  // Start auto validating since they've tried to submit once
                  setState(() => _autoValidate = true);
                }
              },
            ),
          ],
          content: _content(context),
        ),
      ],
    );
  }

  Widget _content(BuildContext context) {
    final severityOptions = _getSeverityOptions();
    return SingleChildScrollView(
      child: Form(
        key: _formKey,
        autovalidateMode: _autoValidate ? AutovalidateMode.always : AutovalidateMode.disabled,
        child: ArrowAwareFocusScope(
          node: _focusScopeNode,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                key: ErrorReportDialog.subjectKey,
                initialValue: _subject,
                decoration: _decoration(L10n(context).reportProblemSubject),
                validator: (text) => text?.isEmpty == true ? L10n(context).reportProblemSubjectEmpty : null,
                onChanged: (text) => _subject = text,
                textInputAction: TextInputAction.next,
                onFieldSubmitted: (_) => _focusScopeNode.nextFocus(),
              ),
              if (widget.includeEmail) SizedBox(height: 16),
              if (widget.includeEmail)
                TextFormField(
                  key: ErrorReportDialog.emailKey,
                  decoration: _decoration(L10n(context).reportProblemEmail),
                  validator: (text) =>
                      (widget.includeEmail && text?.isEmpty == true) ? L10n(context).reportProblemEmailEmpty : null,
                  onChanged: (text) => _email = text,
                  textInputAction: TextInputAction.next,
                  onFieldSubmitted: (_) => _focusScopeNode.nextFocus(),
                ),
              SizedBox(height: 16),
              TextFormField(
                key: ErrorReportDialog.descriptionKey,
                minLines: 3,
                maxLines: null,
                decoration: _decoration(L10n(context).reportProblemDescription, alignLabelWithHint: true),
                validator: (text) => text?.isEmpty == true ? L10n(context).reportProblemDescriptionEmpty : null,
                onChanged: (text) => _description = text,
              ),
              SizedBox(height: 16),
              if (!widget.hideSeverityPicker) Text(L10n(context).reportProblemSeverity),
              if (!widget.hideSeverityPicker)
                Container(
                  color: ParentTheme.of(context)?.nearSurfaceColor,
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                  child: DropdownButton<_SeverityOption>(
                    itemHeight: null,
                    isExpanded: true,
                    underline: SizedBox(),
                    onChanged: (option) async {
                      setState(() => _selectedSeverity = option?.severity);
                      // Clear focus here, as it can go back to the text forms if they were previously selected
                      // NO!  This messes up dpad-nav
                      //_focusScopeNode.requestFocus(FocusNode());
                    },
                    value: severityOptions.firstWhere((option) => option.severity == _selectedSeverity),
                    items: severityOptions.map((option) {
                      return DropdownMenuItem<_SeverityOption>(value: option, child: Text(option.label));
                    }).toList(),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  _submitReport() async {
    final l10n = L10n(context);
    DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
    AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
    PackageInfo packageInfo = await PackageInfo.fromPlatform();

    // Add device and package info before the users description
    final comment = '' +
        '${l10n.device}: ${androidInfo.manufacturer} ${androidInfo.model}\n' +
        '${l10n.osVersion}: Android ${androidInfo.version.release}\n' +
        '${l10n.versionNumber}: ${packageInfo.appName} v${packageInfo.version} (${packageInfo.buildNumber})\n\n' +
        '-------------------------\n\n' +
        '$_description';

    // Send to the API; with stacktrace and device info
    await locator<ErrorReportInteractor>()
        .submitErrorReport(_subject, comment, _email, _selectedSeverity, widget.error?.stack?.toString());
  }

  InputDecoration _decoration(String label, {bool alignLabelWithHint = false}) => InputDecoration(
        labelText: label,
        alignLabelWithHint: alignLabelWithHint,
        fillColor: ParentTheme.of(context)?.nearSurfaceColor,
        filled: true,
      );

  List<_SeverityOption> _getSeverityOptions() {
    final l10n = L10n(context);
    return [
      _SeverityOption(ErrorReportSeverity.COMMENT, l10n.errorSeverityComment),
      _SeverityOption(ErrorReportSeverity.NOT_URGENT, l10n.errorSeverityNotUrgent),
      _SeverityOption(ErrorReportSeverity.WORKAROUND_POSSIBLE, l10n.errorSeverityWorkaroundPossible),
      _SeverityOption(ErrorReportSeverity.BLOCKING, l10n.errorSeverityBlocking),
      _SeverityOption(ErrorReportSeverity.CRITICAL, l10n.errorSeverityCritical),
    ];
  }
}

class _SeverityOption {
  final ErrorReportSeverity severity;
  final String label;

  _SeverityOption(this.severity, this.label);
}
