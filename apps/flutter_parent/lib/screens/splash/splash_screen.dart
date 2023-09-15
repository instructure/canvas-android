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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/canvas_loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class SplashScreen extends StatefulWidget {
  final String? qrLoginUrl;

  SplashScreen({this.qrLoginUrl, super.key});

  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> with SingleTickerProviderStateMixin {
  Future<SplashScreenData?>? _dataFuture;
  Future<int>? _cameraFuture;

  // Controller and animation used on the loading indicator for the 'zoom out' effect immediately before routing
  late AnimationController _controller;
  late Animation<double> _animation;
  late String _route;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(milliseconds: 300));
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInBack);
    _animation.addListener(_animationListener);
  }

  @override
  Widget build(BuildContext context) {
    if (!ApiPrefs.isLoggedIn() && widget.qrLoginUrl == null) {
      // If they aren't logged in or logging in with QR, route to login screen
      if (_cameraFuture == null) {
        _cameraFuture = locator<SplashScreenInteractor>().getCameraCount();
      }

      return FutureBuilder(
          future: _cameraFuture,
          builder: (BuildContext context, AsyncSnapshot<int> snapshot) {
            if (snapshot.hasData || snapshot.hasError) {
              // Even if the camera count fails, we don't want to trap the user on splash
              _navigate(PandaRouter.login());
            }

            return _defaultBody(context);
          });
    } else {
      if (_dataFuture == null) {
        _dataFuture = locator<SplashScreenInteractor>().getData(qrLoginUrl: widget.qrLoginUrl);
      }

      return Scaffold(
        backgroundColor: Theme.of(context).primaryColor,
        body: FutureBuilder(
          future: _dataFuture,
          builder: (BuildContext context, AsyncSnapshot<SplashScreenData?> snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data!.isObserver || snapshot.data!.canMasquerade) {
                _navigateToDashboardOrAup();
              } else {
                // User is not an observer and cannot masquerade. Show the not-a-parent screen.
                _navigate(PandaRouter.notParent());
              }
            } else if (snapshot.hasError) {
              if (snapshot.error is QRLoginError) {
                WidgetsBinding.instance
                    .addPostFrameCallback((_) => Navigator.pop(context, L10n(context).loginWithQRCodeError));
              } else {
                // On error, proceed without pre-fetched student list
                _navigateToDashboardOrAup();
              }
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
    MasqueradeUI.of(context)?.refresh();
    _route = route;
    _controller.forward(); // Start the animation, we'll navigate when it finishes
  }

  _navigateToDashboardOrAup() {
    locator<SplashScreenInteractor>()
        .isTermsAcceptanceRequired()
        .then((aupRequired) => {
      if (aupRequired == true) {
        _navigate(PandaRouter.aup())
      }
      else {
        _navigate(PandaRouter.dashboard())
      }
    });
  }

  _animationListener() {
    if (_animation.status == AnimationStatus.completed) {
      // Use a custom page route for the circle reveal animation
      locator<QuickNav>().pushRouteWithCustomTransition(
        context,
        _route,
        true,
        Duration(milliseconds: 500),
        (
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
    required Animation<double> scale,
    this.child,
    super.key
  })  : super(listenable: scale);

  Animation<double> get animation => listenable as Animation<double>;

  final Widget? child;

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
