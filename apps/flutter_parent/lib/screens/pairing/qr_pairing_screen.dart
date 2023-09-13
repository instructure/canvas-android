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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:tuple/tuple.dart';

class QRPairingScreen extends StatefulWidget {
  final QRPairingInfo? pairingInfo;
  final bool isCreatingAccount;

  const QRPairingScreen({this.pairingInfo, this.isCreatingAccount = false, super.key});

  @override
  _QRPairingScreenState createState() => _QRPairingScreenState();
}

class _QRPairingScreenState extends State<QRPairingScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  bool _isPairing = false;
  Tuple2<String, String>? _errorInfo;

  PairingInteractor _interactor = locator<PairingInteractor>();

  @override
  void initState() {
    if (widget.pairingInfo != null) {
      _isPairing = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _handleScanResult(widget.pairingInfo!);
      });
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    bool showTutorial = !_isPairing && _errorInfo == null;
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        key: _scaffoldKey,
        appBar: AppBar(
          title: Text(showTutorial ? L10n(context).qrPairingTutorialTitle : L10n(context).qrPairingTitle),
          automaticallyImplyLeading: true,
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
          actions: <Widget>[
            if (showTutorial) _nextButton(context),
          ],
        ),
        body: _body(context),
      ),
    );
  }

  Widget _body(BuildContext context) {
    if (_isPairing) {
      return LoadingIndicator();
    } else if (_errorInfo != null) {
      return _errorMessage(context);
    } else {
      return _tutorial(context);
    }
  }

  Widget _tutorial(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: <Widget>[
          Text(L10n(context).qrPairingTutorialMessage, style: Theme.of(context).textTheme.titleMedium),
          Expanded(
            child: FractionallySizedBox(
              alignment: Alignment.center,
              heightFactor: 0.75,
              child: Image.asset(
                'assets/png/locate-pairing-qr-tutorial.png',
                semanticLabel: L10n(context).qrPairingScreenshotContentDescription,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _nextButton(BuildContext context) {
    return ButtonTheme(
      textTheme: Theme.of(context).buttonTheme.textTheme,
      minWidth: 48,
      child: TextButton(
        style: TextButton.styleFrom(
          visualDensity: VisualDensity.compact,
          shape: CircleBorder(side: BorderSide(color: Colors.transparent)),
        ),
        child: Text(L10n(context).next.toUpperCase()),
        onPressed: () async {
          QRPairingScanResult result = await _interactor.scanQRCode();
          _handleScanResult(result);
        },
      ),
    );
  }

  Widget _errorMessage(BuildContext context) {
    return EmptyPandaWidget(
      svgPath: 'assets/svg/panda-no-pairing-code.svg',
      title: _errorInfo!.item1,
      subtitle: _errorInfo!.item2,
      buttonText: L10n(context).retry,
      onButtonTap: () async {
        QRPairingScanResult result = await _interactor.scanQRCode();
        _handleScanResult(result);
      },
    );
  }

  void _handleScanResult(QRPairingScanResult result) async {
    var l10n = L10n(context);
    if (result is QRPairingInfo) {
      setState(() => _isPairing = true);

      if (widget.isCreatingAccount) {
        setState(() {
          _isPairing = false;
        });

        // Don't pair, just route the info to account creation
        locator<QuickNav>()
            .pushRoute(context, PandaRouter.accountCreation(result.code, result.domain, result.accountId));
      } else {
        _handleScanResultForPairing(result);
      }
    } else if (result is QRPairingScanError) {
      locator<Analytics>().logMessage(result.type.toString());
      Tuple2<String, String>? errorInfo;
      switch (result.type) {
        case QRPairingScanErrorType.invalidCode:
          errorInfo = Tuple2(l10n.qrPairingInvalidCodeTitle, l10n.invalidQRCodeError);
          break;
        case QRPairingScanErrorType.cameraError:
          errorInfo = Tuple2(l10n.qrPairingCameraPermissionTitle, l10n.qrCodeNoCameraError);
          break;
        case QRPairingScanErrorType.unknown:
          errorInfo = Tuple2(l10n.qrPairingFailedTitle, l10n.qrPairingFailedSubtitle);
          break;
        case QRPairingScanErrorType.canceled:
          // Don't set or change the error if scan was canceled
          break;
      }
      setState(() {
        _isPairing = false;
        _errorInfo = errorInfo;
      });
    }
  }

  void _handleScanResultForPairing(QRPairingInfo result) async {
    var l10n = L10n(context);
    // 'success' is true if pairing worked, false for API/pairing error, null for network error
    bool? success = await _interactor.pairWithStudent(result.code);

    if (success == true) {
      // If opened from a deep link in a cold state, this will be the top route and we'll want to go to the splash instead of popping
      if (ModalRoute.of(context)?.isFirst == true) {
        locator<QuickNav>().replaceRoute(context, PandaRouter.rootSplash());
      } else {
        locator<StudentAddedNotifier>().notify();
        Navigator.of(context).pop(true);
      }
    } else {
      Tuple2<String, String> errorInfo;
      if (success == false) {
        if (ApiPrefs.isLoggedIn() && ApiPrefs.getDomain()?.endsWith(result.domain) == false) {
          errorInfo = Tuple2(l10n.qrPairingWrongDomainTitle, l10n.qrPairingWrongDomainSubtitle);
        } else {
          errorInfo = Tuple2(l10n.qrPairingFailedTitle, l10n.qrPairingFailedSubtitle);
        }
      } else {
        errorInfo = Tuple2(l10n.genericNetworkError, l10n.qrPairingNetworkError);
      }
      setState(() {
        _isPairing = false;
        _errorInfo = errorInfo;
      });
    }
  }
}
