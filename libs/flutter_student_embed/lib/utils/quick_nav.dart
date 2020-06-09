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
import 'package:flutter/widgets.dart';

class QuickNav {
  Future<T> push<T extends Object>(BuildContext context, Widget widget) {
    return Navigator.of(context).push(QuickFadeRoute(widget));
  }
}

/// A [MaterialPageRoute] that matches the native fragment transition with a 200ms 'fade' animation
class QuickFadeRoute<T> extends MaterialPageRoute<T> {
  final Widget child;

  QuickFadeRoute(this.child) : super(builder: (_) => child);

  @override
  Duration get transitionDuration => Duration(milliseconds: 200);

  @override
  Widget buildTransitions(
      BuildContext context, Animation<double> animation, Animation<double> secondaryAnimation, Widget child) {
    return QuickFadePageTransition(routeAnimation: animation, child: child);
  }
}

class QuickFadePageTransition extends StatelessWidget {
  QuickFadePageTransition({
    Key key,
    @required Animation<double> routeAnimation,
    @required this.child,
  })  : _opacityAnimation = routeAnimation.drive(_easeInTween),
        super(key: key);

  static final Animatable<double> _easeInTween = CurveTween(curve: Curves.easeIn);

  final Animation<double> _opacityAnimation;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _opacityAnimation,
      child: child,
    );
  }
}
