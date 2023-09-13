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

/// _DialogRoute copied (with minor changes) from flutter/lib/src/widget/routes.dart
class _DialogRoute<T> extends PopupRoute<T> {
  _DialogRoute({
    required pageBuilder,
    required String barrierLabel,
    required RouteTransitionsBuilder transitionBuilder,
    bool barrierDismissible = true,
    Color barrierColor = const Color(0x80000000),
    Duration transitionDuration = const Duration(milliseconds: 200),
    super.settings,
  })  : _pageBuilder = pageBuilder,
        _barrierDismissible = barrierDismissible,
        _barrierLabel = barrierLabel,
        _barrierColor = barrierColor,
        _transitionDuration = transitionDuration,
        _transitionBuilder = transitionBuilder;

  final RoutePageBuilder _pageBuilder;

  @override
  bool get barrierDismissible => _barrierDismissible;
  final bool _barrierDismissible;

  @override
  String get barrierLabel => _barrierLabel;
  final String _barrierLabel;

  @override
  Color get barrierColor => _barrierColor;
  final Color _barrierColor;

  @override
  Duration get transitionDuration => _transitionDuration;
  final Duration _transitionDuration;

  final RouteTransitionsBuilder _transitionBuilder;

  @override
  Widget buildPage(BuildContext context, Animation<double> animation, Animation<double> secondaryAnimation) {
    return Semantics(
      child: _pageBuilder(context, animation, secondaryAnimation),
      scopesRoute: true,
      explicitChildNodes: true,
    );
  }

  @override
  Widget buildTransitions(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    Widget child,
  ) {
    return _transitionBuilder(context, animation, secondaryAnimation, child);
  }
}

/// Similar to [showDialog], but instead of taking a [BuildContext] this takes a [GlobalKey] of type [NavigatorState].
/// This is useful in situations where [showDialog] will not work because a [Navigator] is not accessible via the
/// available [BuildContext], such as the masquerading UI which is an ancestor of the [Navigator].
Future<T?> showDialogWithNavigatorKey<T>({
  required GlobalKey<NavigatorState> navKey,
  required WidgetBuilder builder,
  required BuildContext buildContext,
  bool barrierDismissible = true,
}) {
  BuildContext context = navKey.currentContext ?? buildContext;
  assert(debugCheckHasMaterialLocalizations(context));

  var route = _DialogRoute<T>(
    pageBuilder: (_, __, ___) => SafeArea(child: Builder(builder: builder)),
    barrierLabel: MaterialLocalizations.of(context).modalBarrierDismissLabel,
    transitionBuilder: (_, animation, __, child) {
      return FadeTransition(
        opacity: CurvedAnimation(parent: animation, curve: Curves.easeOut),
        child: child,
      );
    },
    barrierDismissible: barrierDismissible,
    barrierColor: Colors.black54,
    transitionDuration: const Duration(milliseconds: 150),
  );

  return (navKey.currentState)?.push<T>(route) ?? Future<T>.value(null);
}
