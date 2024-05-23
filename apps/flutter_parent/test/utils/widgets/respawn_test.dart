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
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('Resets state', (tester) async {
    await tester.pumpWidget(
      TestApp(
        Respawn(child: _RespawnTestWidget()),
      ),
    );
    await tester.pump();

    // Counter should read 'Count: 0'
    expect(find.text('Count: 0'), findsOneWidget);

    // Tap 'increment' button twice
    await tester.tap(find.byKey(_RespawnTestWidget.incrementKey));
    await tester.pump();
    await tester.tap(find.byKey(_RespawnTestWidget.incrementKey));
    await tester.pump();

    // Counter should now read 'Count: 2'
    expect(find.text('Count: 2'), findsOneWidget);

    // Tap respawn button
    await tester.tap(find.byKey(_RespawnTestWidget.respawnKey));
    await tester.pumpAndSettle();

    // Counter should have reset
    expect(find.text('Count: 0'), findsOneWidget);
  });
}

class _RespawnTestWidget extends StatefulWidget {
  static Key respawnKey = Key('respawn-button');
  static Key incrementKey = Key('increment-button');
  static Key counterKey = Key('counter');

  @override
  _RespawnTestWidgetState createState() => _RespawnTestWidgetState();
}

class _RespawnTestWidgetState extends State<_RespawnTestWidget> {
  int _counter = 0;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        children: <Widget>[
          Text(
            'Count: $_counter',
            key: _RespawnTestWidget.counterKey,
          ),
          TextButton(
            key: _RespawnTestWidget.incrementKey,
            child: Text('Tap to increment'),
            onPressed: () => setState(() => _counter++),
          ),
          TextButton(
            key: _RespawnTestWidget.respawnKey,
            child: Text('Tap to Respawn'),
            onPressed: () {
              Respawn.of(context)?.restart();
            },
          ),
        ],
      ),
    );
  }
}
