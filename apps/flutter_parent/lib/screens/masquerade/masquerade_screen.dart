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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/flutter_svg.dart';

import 'masquerade_screen_interactor.dart';

class MasqueradeScreen extends StatefulWidget {
  @override
  MasqueradeScreenState createState() => MasqueradeScreenState();
}

class MasqueradeScreenState extends State<MasqueradeScreen> {
  static Offset pandaMaskOffset = const Offset(150, 40);

  late TextEditingController _domainController;
  late TextEditingController _userIdController;
  MasqueradeScreenInteractor _interactor = locator<MasqueradeScreenInteractor>();
  late bool _enableDomainInput;

  String? _domainErrorText;
  String? _userIdErrorText;

  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  final Duration _pandaAnimDuration = const Duration(milliseconds: 1500);
  final Curve _pandaAnimCurve = Curves.ease;
  final double _pandaMaskRotation = 0.8;
  bool _maskIllustration = false;
  bool _flipAnimSide = false;

  bool _startingMasquerade = false;

  late Timer timer;

  @override
  void initState() {
    _enableDomainInput = _interactor.getDomain()?.contains(MasqueradeScreenInteractor.siteAdminDomain) ?? false;

    // Set up Domain input controller
    _domainController = TextEditingController(text: _enableDomainInput ? null : _interactor.getDomain());
    _domainController.addListener(() {
      if (_domainErrorText != null) setState(() => _domainErrorText = null);
    });

    // Set up User ID input controller
    _userIdController = TextEditingController();
    _userIdController.addListener(() {
      if (_userIdErrorText != null) setState(() => _userIdErrorText = null);
    });

    // Set up timer for animation
    timer = Timer.periodic(Duration(seconds: 2), (timer) {
      setState(() {
        _maskIllustration = !_maskIllustration;
        if (!_maskIllustration) _flipAnimSide = !_flipAnimSide;
      });
    });

    // Initial animation
    WidgetsBinding.instance.addPostFrameCallback((_) {
      setState(() {
        _maskIllustration = !_maskIllustration;
      });
    });
    super.initState();
  }

  @override
  void dispose() {
    timer.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        key: _scaffoldKey,
        appBar: AppBar(
          title: Text(L10n(context).actAsUser),
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
        ),
        body: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: <Widget>[
                SizedBox(height: 16),
                _pandaIllustration(),
                Text(L10n(context).actAsDescription),
                SizedBox(height: 16),
                TextField(
                  key: Key('domain-input'),
                  enabled: _enableDomainInput && !_startingMasquerade,
                  controller: _domainController,
                  keyboardType: TextInputType.url,
                  style: _enableDomainInput ? null : TextStyle(color: ParentColors.ash),
                  decoration: InputDecoration(
                    errorText: _domainErrorText,
                    hintText: L10n(context).domainInputHint,
                    labelText: L10n(context).domainInputHint,
                    alignLabelWithHint: true,
                    border: _enableDomainInput ? OutlineInputBorder() : InputBorder.none,
                  ),
                ),
                SizedBox(height: 12),
                TextField(
                  key: Key('user-id-input'),
                  enabled: !_startingMasquerade,
                  controller: _userIdController,
                  decoration: InputDecoration(
                    errorText: _userIdErrorText,
                    hintText: L10n(context).userIdInputHint,
                    labelText: L10n(context).userIdInputHint,
                    border: OutlineInputBorder(),
                  ),
                ),
                SizedBox(height: 12),
                if (_startingMasquerade)
                  Container(
                    height: 64,
                    child: Center(
                      child: Container(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(strokeWidth: 3),
                      ),
                    ),
                  ),
                if (!_startingMasquerade)
                  Container(
                    width: double.maxFinite,
                    height: 64,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Theme.of(context).primaryColor,
                        textStyle: TextStyle(color: Colors.white),
                      ),
                      child: Text(L10n(context).actAsUser, style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.white),),
                      onPressed: () => _startMasquerading(),
                    ),
                  )
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _pandaIllustration() {
    return ExcludeSemantics(
      child: Container(
        width: double.maxFinite,
        height: 146,
        child: Stack(
          clipBehavior: Clip.none,
          children: <Widget>[
            Positioned(child: SvgPicture.asset('assets/svg/masquerade-white-panda.svg'), left: 0, right: 0),
            AnimatedPositioned(
              duration: _pandaAnimDuration,
              curve: _pandaAnimCurve,
              child: TweenAnimationBuilder(
                tween: Tween<double>(
                  begin: -_pandaMaskRotation,
                  end: _maskIllustration ? 0 : _flipAnimSide ? _pandaMaskRotation : -_pandaMaskRotation,
                ),
                duration: _pandaAnimDuration,
                curve: _pandaAnimCurve,
                builder: (context, value, _) {
                  return Transform.rotate(
                    angle: value,
                    child: SvgPicture.asset('assets/svg/masquerade-red-panda.svg', key: Key('red-panda-mask')),
                  );
                },
              ),
              right: 0,
              left: _maskIllustration ? 0 : _flipAnimSide ? pandaMaskOffset.dx : -pandaMaskOffset.dx,
              top: _maskIllustration ? 0 : pandaMaskOffset.dy,
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _startMasquerading() async {
    String domain = _interactor.sanitizeDomain(_domainController.text);
    String userId = _userIdController.text.trim();

    if (domain.isEmpty || userId.isEmpty) {
      setState(() {
        if (domain.isEmpty) _domainErrorText = L10n(context).domainInputError;
        if (userId.isEmpty) _userIdErrorText = L10n(context).userIdInputError;
      });
      return;
    }

    setState(() => _startingMasquerade = true);
    bool success = await _interactor.startMasquerading(userId, domain);
    if (success) {
      Respawn.of(context)?.restart();
    } else {
      setState(() => _startingMasquerade = false);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(L10n(context).actAsUserError)));
    }
  }
}
