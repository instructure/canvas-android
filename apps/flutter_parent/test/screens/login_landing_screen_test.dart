import 'package:flutter/material.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/test_app.dart';

void main() {
  setupLocator();

  testWidgets("Opens domain search screen", (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    expect(find.text("Find School or District"), findsOneWidget);
    await tester.tap(find.text("Find School or District"));
    await tester.pumpAndSettle();

    expect(find.byType(DomainSearchScreen), findsOneWidget);
  });

  testWidgets("Displays Snicker Doodles drawer", (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    var size = tester.getSize(find.byType(LoginLandingScreen));
    await tester.flingFrom(Offset(size.width - 5, size.height / 2), Offset(-size.width / 2, 0), 1000);
    await tester.pumpAndSettle();

    expect(find.byType(Drawer), findsOneWidget);
  });
}
