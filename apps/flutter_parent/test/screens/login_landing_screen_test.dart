import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/accessibility_utils.dart';
import '../utils/test_app.dart';

void main() {
  setupLocator();

  testWidgetsWithAccessibilityChecks("Opens domain search screen", (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().findSchoolOrDistrict), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().findSchoolOrDistrict));
    await tester.pumpAndSettle();

    expect(find.byType(DomainSearchScreen), findsOneWidget);

    // TODO: Remove this back press once DomainSearchScreen is passing accessibility checks
    await tester.pageBack();
  });

  testWidgetsWithAccessibilityChecks("Displays Snicker Doodles drawer", (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    var size = tester.getSize(find.byType(LoginLandingScreen));
    await tester.flingFrom(Offset(size.width - 5, size.height / 2), Offset(-size.width / 2, 0), 1000);
    await tester.pumpAndSettle();

    expect(find.byType(Drawer), findsOneWidget);
  });
}
