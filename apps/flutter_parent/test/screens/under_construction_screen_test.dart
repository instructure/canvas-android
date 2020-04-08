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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/accessibility_utils.dart';
import '../utils/test_app.dart';

void main() {
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('Shows under construction widget without app bar', (tester) async {
    await tester.pumpWidget(TestApp(UnderConstructionScreen()));
    await tester.pump(); // Wait for the widget to finish building

    expect(find.byType(UnderConstructionScreen), findsOneWidget);
    expect(find.byType(AppBar), findsNothing);

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.text(l10n.underConstruction), findsOneWidget);
    expect(find.text(l10n.currentlyBuildingThisFeature), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows under construction widget without app bar when no back stack exists',
      (tester) async {
    await tester.pumpWidget(TestApp(UnderConstructionScreen(showAppBar: true)));
    await tester.pump(); // Wait for the widget to finish building

    expect(find.byType(UnderConstructionScreen), findsOneWidget);
    expect(find.byType(AppBar), findsNothing);

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.text(l10n.underConstruction), findsOneWidget);
    expect(find.text(l10n.currentlyBuildingThisFeature), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows under construction widget with app bar', (tester) async {
    await TestApp.showWidgetFromTap(tester, (context) {
      return Navigator.of(context)
          .push(MaterialPageRoute(builder: (context) => UnderConstructionScreen(showAppBar: true)));
    });
    await tester.pump(); // Wait for the widget to finish building

    expect(find.byType(UnderConstructionScreen), findsOneWidget);

    expect(find.byType(AppBar), findsOneWidget);
    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.text(l10n.underConstruction), findsOneWidget);
    expect(find.text(l10n.currentlyBuildingThisFeature), findsOneWidget);
  });
}
