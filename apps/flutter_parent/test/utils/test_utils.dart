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

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

enum ScreenVerticalLocation { TOP, MID_TOP, CENTER, MID_BOTTOM, BOTTOM }

double _getScreenHeightOffset(WidgetTester tester, ScreenVerticalLocation location) {
  final screenHeight = tester.binding.window.physicalSize.height / tester.binding.window.devicePixelRatio;

  double result = 0.0;
  switch (location) {
    case ScreenVerticalLocation.TOP:
      result = 0.0;
      break;
    case ScreenVerticalLocation.MID_TOP:
      result = screenHeight / 4;
      break;
    case ScreenVerticalLocation.CENTER:
      result = screenHeight / 2;
      break;
    case ScreenVerticalLocation.MID_BOTTOM:
      result = screenHeight * 3 / 4;
      break;
    case ScreenVerticalLocation.BOTTOM:
      result = screenHeight - 1;
  }

  return result;
}

/// Adapted from https://github.com/flutter/flutter/issues/17668
///
/// Scrolls until [finder] finds a single [Widget].
///
/// This helper is only required because [WidgetTester.ensureVisible] does not yet work for items that are scrolled
/// out of view in a [ListView]. See https://github.com/flutter/flutter/issues/17668. Once that issue is resolved,
/// we should be able to remove this altogether.
///
/// On top of that, this would ideally be an extension method against [WidgetTester], but at the time of writing,
/// extension methods are not yet available in the stable channel.
Future<void> ensureVisibleByScrolling(
  Finder finder,
  WidgetTester widgetTester, {
  required ScreenVerticalLocation scrollFrom,
  Offset scrollBy = const Offset(0, -50),
  int maxScrolls = 100,
}) async {

  final scrollFromY = _getScreenHeightOffset(widgetTester, scrollFrom);
  final gesture = await widgetTester.startGesture(Offset(0, scrollFromY));

  Widget? foundWidget;

  for (var i = 0; i < maxScrolls; ++i) {
    await gesture.moveBy(scrollBy);
    await widgetTester.pump();
    final widgets = widgetTester.widgetList(finder);

    if (widgets.length == 1) {
      foundWidget = widgets.first;
      break;
    }
  }

  await gesture.cancel();

  expect(foundWidget, isNotNull);

  // Just because we found the widget, doesn't mean it's visible. It could be off-stage. But now we can at least use the standard
  // ensureVisible method to bring it on-screen.
  await widgetTester.ensureVisible(finder);
  // Attempting to bring the widget on-screen may result in it being scrolled too far up in a list, in which case it will bounce back
  // once the gesture above completes. We need to pump for long enough for the bounce-back animation to complete.
  await widgetTester.pump(const Duration(seconds: 1));
}
