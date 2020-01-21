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
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../accessibility_utils.dart';
import '../../test_app.dart';

void main() {
  TestApp _createTestApp(Future callback(BuildContext)) => TestApp(
        Builder(
            builder: (context) => RaisedButton(
                  child: Text('tap me', style: TextStyle(color: Colors.white)), // So it's 'accessible'
                  onPressed: () => callback(context),
                )),
        highContrast: true,
      );

  _showDialog(WidgetTester tester, Future callback(BuildContext)) async {
    await tester.pumpWidget(_createTestApp(callback));
    await tester.pumpAndSettle();

    // Tap the button to show the dialog
    await tester.tap(find.byType(RaisedButton));
    await tester.pumpAndSettle();
  }

  testWidgetsWithAccessibilityChecks('Shows a dialog', (tester) async {
    await _showDialog(tester, (context) => ErrorReportDialog.asDialog(context));

    expect(find.byType(ErrorReportDialog), findsOneWidget);
    expect(find.text(AppLocalizations().reportProblemTitle), findsOneWidget);
    expect(find.text(AppLocalizations().errorSeverityComment), findsOneWidget);

    expect(find.text(AppLocalizations().reportProblemEmail), findsNothing);
    expect(find.text(AppLocalizations().reportProblemEmailEmpty), findsNothing);
    expect(find.text(AppLocalizations().reportProblemSubjectEmpty), findsNothing);
    expect(find.text(AppLocalizations().reportProblemDescriptionEmpty), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog with customizations', (tester) async {
    final title = 'title';
    final subject = 'subject';
    final severity = ErrorReportSeverity.CRITICAL;

    await _showDialog(
      tester,
      (context) => ErrorReportDialog.asDialog(
        context,
        title: title,
        subject: subject,
        severity: severity,
        includeEmail: true,
      ),
    );

    expect(find.byType(ErrorReportDialog), findsOneWidget);
    expect(find.text(title), findsOneWidget);
    expect(find.text(subject), findsOneWidget);
    expect(find.text(AppLocalizations().errorSeverityCritical), findsOneWidget);
    expect(find.text(AppLocalizations().reportProblemEmail), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Selecting a severity updates dialog', (tester) async {
    await _showDialog(tester, (context) => ErrorReportDialog.asDialog(context));

    // Tap on dropdown to show list
    await tester.tap(find.text(AppLocalizations().errorSeverityComment));
    await tester.pumpAndSettle();

    // Tap new item in a list
    final x = find.text(AppLocalizations().errorSeverityBlocking);
    await tester.tap(x.last);
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().errorSeverityBlocking), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Cancel closes the dialog', (tester) async {
    await _showDialog(tester, (context) => ErrorReportDialog.asDialog(context));

    await tester.tap(find.text(AppLocalizations().cancel.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorReportDialog), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Submit validates the dialog and shows errors', (tester) async {
    await _showDialog(tester, (context) => ErrorReportDialog.asDialog(context, includeEmail: true));

    expect(find.text(AppLocalizations().reportProblemSubjectEmpty), findsNothing);
    expect(find.text(AppLocalizations().reportProblemDescriptionEmpty), findsNothing);
    expect(find.text(AppLocalizations().reportProblemEmailEmpty), findsNothing);

    // Try to send the report
    await tester.tap(find.text(AppLocalizations().sendReport.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().reportProblemSubjectEmpty), findsOneWidget);
    expect(find.text(AppLocalizations().reportProblemDescriptionEmpty), findsOneWidget);
    expect(find.text(AppLocalizations().reportProblemEmailEmpty), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Submit calls through to the interactor', (tester) async {
    final subject = 'Test subject';
    final description = 'Test description';
    final email = 'test@email.com';
    final error = FlutterErrorDetails(stack: StackTrace.fromString('fake stack'));

    final interactor = _MockErrorReportInteractor();
    setupTestLocator((locator) => locator.registerFactory<ErrorReportInteractor>(() => interactor));

    await _showDialog(tester, (context) => ErrorReportDialog.asDialog(context, includeEmail: true, error: error));

    // Enter in the details
    await tester.enterText(find.byKey(ErrorReportDialog.subjectKey), subject);
    await tester.testTextInput.receiveAction(TextInputAction.next);
    await tester.enterText(find.byKey(ErrorReportDialog.emailKey), email);
    await tester.testTextInput.receiveAction(TextInputAction.next);
    await tester.enterText(find.byKey(ErrorReportDialog.descriptionKey), description);

    // Try to send the report
    await tester.tap(find.text(AppLocalizations().sendReport.toUpperCase()));
    await tester.pumpAndSettle();

    final comment = '' +
        '${AppLocalizations().device}: Instructure Canvas Phone\n' +
        '${AppLocalizations().osVersion}: Android FakeOS 9000\n' +
        '${AppLocalizations().versionNumber}: Canvas v1.0.0 (3)\n\n' +
        '-------------------------\n\n' +
        '$description';

    verify(interactor.submitErrorReport(subject, comment, email, ErrorReportSeverity.COMMENT, error.stack.toString()))
        .called(1);
  });
}

class _MockErrorReportInteractor extends Mock implements ErrorReportInteractor {}
