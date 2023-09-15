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

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter/material.dart' as Material show showDialog;
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class QuickNav {
  @Deprecated('Deprecated in favor of using PushRoute etc, end goal is for all routes to go through PandaRouter')
  Future<dynamic> push<T extends Object>(BuildContext context, Widget widget) {
    _logShow(widget);
    return Navigator.of(context).push<T>(MaterialPageRoute(builder: (context) => widget));
  }

  /// Default method for pushing screens, uses material transition
  Future<dynamic> pushRoute<T extends Object>(BuildContext context, String route,
      {TransitionType transitionType = TransitionType.material}) {
    return PandaRouter.router.navigateTo(context, route, transition: transitionType);
  }

  Future<dynamic> replaceRoute<T extends Object>(BuildContext context, String route,
      {TransitionType transitionType = TransitionType.material}) {
    return PandaRouter.router.navigateTo(context, route, transition: transitionType, replace: true);
  }

  Future<dynamic> pushRouteAndClearStack<T extends Object>(BuildContext context, String route,
      {TransitionType transitionType = TransitionType.material}) {
    return PandaRouter.router.navigateTo(context, route, transition: transitionType, clearStack: true);
  }

  Future<dynamic> pushRouteWithCustomTransition<T extends Object>(BuildContext context, String route, bool clearStack,
      Duration transitionDuration, RouteTransitionsBuilder transitionsBuilder,
      {TransitionType transitionType = TransitionType.custom}) {
    return PandaRouter.router.navigateTo(context, route,
        clearStack: clearStack,
        transitionDuration: transitionDuration,
        transition: transitionType,
        transitionBuilder: transitionsBuilder);
  }

  Future<void> routeInternally(BuildContext context, String url) {
    return PandaRouter.routeInternally(context, url);
  }

  void _logShow(Widget widget) {
    final widgetName = widget.runtimeType.toString();
    final message = 'Pushing widget: $widgetName';
    locator<Analytics>().logMessage(message);
    locator<Analytics>().setCurrentScreen(widgetName);
  }

  Future<T?> showDialog<T>({
    required BuildContext context,
    bool barrierDismissible = true,
    required WidgetBuilder builder,
    bool useRootNavigator = true,
    RouteSettings? routeSettings,
  }) =>
      Material.showDialog(
        context: context,
        barrierDismissible: barrierDismissible,
        builder: builder,
        useRootNavigator: useRootNavigator,
        routeSettings: routeSettings,
      );
}
