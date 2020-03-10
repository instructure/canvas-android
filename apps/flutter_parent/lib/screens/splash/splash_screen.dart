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

import 'dart:math';
import 'dart:ui';

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/parent_router.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/canvas_loading_indicator.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> with SingleTickerProviderStateMixin {
  Future<List<User>> _dataFuture;

  // Controller and animation used on the loading indicator for the 'zoom out' effect immediately before routing
  AnimationController _controller;
  Animation<double> _animation;
  String _route;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(milliseconds: 300));
    _dataFuture = locator<DashboardInteractor>().getStudents(forceRefresh: true);
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInBack);
    _animation.addListener(_animationListener);
  }

  @override
  Widget build(BuildContext context) {
    if (!ApiPrefs.isLoggedIn()) {
      // route to login screen
      _navigate(PandaRouter.login());
      return _defaultBody(context);
    } else {
      return Scaffold(
        backgroundColor: Theme.of(context).primaryColor,
        body: FutureBuilder(
          future: _dataFuture,
          builder: (BuildContext context, AsyncSnapshot<List<User>> snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data.isEmpty) {
                // User is not observing any students. Show the not-a-parent screen.
                _navigate(PandaRouter.notParent());
              } else {
                // Proceed with pre-fetched student list
                // TODO - revert to include pre-fetch later on
                _navigate(PandaRouter.dashboard());
              }
            } else if (snapshot.hasError) {
              // On error, proceed without pre-fetched student list
              _navigate(PandaRouter.dashboard());
            }
            return Container(
              child: Center(
                child: ScaleTransition(
                    scale: Tween<double>(begin: 1.0, end: 0.0).animate(_animation),
                    child: const CanvasLoadingIndicator()),
              ),
            );
          },
        ),
      );
    }
  }

  Widget _defaultBody(BuildContext cont) {
    return Scaffold(
        backgroundColor: Theme.of(context).primaryColor,
        body: Container(
          child: Center(
            child: ScaleTransition(
                scale: Tween<double>(begin: 1.0, end: 0.0).animate(_animation), child: const CanvasLoadingIndicator()),
          ),
        ));
  }

  _navigate(String route) {
    _route = route;
    _controller.forward(); // Start the animation, we'll navigate when it finishes
  }

  _animationListener() {
    if (_animation.status == AnimationStatus.completed) {
      // Use a custom page route for the circle reveal animation
      PandaRouter.router.navigateTo(
        context,
        _route,
        replace: true,
        transitionDuration: const Duration(milliseconds: 500),
        transition: TransitionType.custom,
        transitionBuilder: (
          context,
          animation,
          secondaryAnimation,
          child,
        ) {
          return ScaleTransition(
            scale: Tween<double>(
              begin: 2.0,
              end: 1.0,
            ).animate(CurvedAnimation(
              parent: animation,
              curve: Curves.easeOutQuad,
            )),
            child: _CircleClipTransition(
              child: child,
              scale: Tween<double>(
                begin: 0.0,
                end: 1.0,
              ).animate(CurvedAnimation(
                parent: animation,
                curve: Curves.easeInExpo,
              )),
            ),
          );
        },
      );
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}

class _CircleClipTransition extends AnimatedWidget {
  const _CircleClipTransition({
    Key key,
    @required Animation<double> scale,
    this.child,
  })  : assert(scale != null),
        super(key: key, listenable: scale);

  Animation<double> get animation => listenable;

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: animation,
      builder: (context, _) => ClipPath(
        clipper: _CircleClipper(animation.value),
        child: child,
      ),
    );
  }
}

class _CircleClipper extends CustomClipper<Path> {
  final double animationValue;

  _CircleClipper(this.animationValue);

  @override
  Path getClip(Size size) {
    return Path()
      ..addOval(
        Rect.fromCircle(
          center: size.center(Offset.zero),
          radius: animationValue * sqrt(pow(size.width, 2) + pow(size.height, 2)) / 2,
        ),
      );
  }

  @override
  bool shouldReclip(CustomClipper<Path> oldClipper) => true;
}
