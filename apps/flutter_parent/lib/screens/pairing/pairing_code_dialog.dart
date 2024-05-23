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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class PairingCodeDialog extends StatefulWidget {
  final _interactor = locator<PairingInteractor>();

  final String? _pairingCode;

  PairingCodeDialog(this._pairingCode, {super.key});

  @override
  State<StatefulWidget> createState() => PairingCodeDialogState();
}

class PairingCodeDialogState extends State<PairingCodeDialog> {
  var _pairingCodeError = false;
  var _makingApiCall = false;
  FocusScopeNode _focusScopeNode = FocusScopeNode();

  final GlobalKey<FormFieldState> _formKey = GlobalKey<FormFieldState>();

  @override
  void dispose() {
    _focusScopeNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ArrowAwareFocusScope(
      node: _focusScopeNode,
      child: AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
        title: Text(L10n(context).addStudent),
        content: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(bottom: 20.0),
                child: Text(
                  L10n(context).pairingCodeEntryExplanation,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(fontSize: 12.0),
                ),
              ),
              TextFormField(
                key: _formKey,
                autofocus: true,
                autocorrect: false,
                autovalidateMode: AutovalidateMode.disabled,
                initialValue: widget._pairingCode,
                onChanged: (value) {
                  _showPairingCodeError(false);
                },
                validator: (text) {
                  if (_pairingCodeError) {
                    return L10n(context).errorPairingFailed;
                  } else
                    return null;
                },
                onSaved: (code) async {
                  // Disable OK and Cancel buttons
                  setState(() {
                    _makingApiCall = true;
                  });

                  _showPairingCodeError(false);

                  var successful = await widget._interactor.pairWithStudent(code);
                  if (successful == true) {
                    // Close dialog - return 'true' to represent that a student was paired
                    locator<Analytics>().logEvent(AnalyticsEventConstants.ADD_STUDENT_SUCCESS);
                    Navigator.of(context).pop(true);
                  } else {
                    locator<Analytics>().logEvent(AnalyticsEventConstants.ADD_STUDENT_FAILURE);
                    _showPairingCodeError(true);
                  }

                  // Enable OK and Cancel buttons
                  setState(() {
                    _makingApiCall = false;
                  });
                },
                onFieldSubmitted: (code) async {
                  _formKey.currentState?.save();
                },
                decoration: InputDecoration(
                  hintText: L10n(context).pairingCode,
                  hintStyle: TextStyle(color: ParentColors.ash),
                  contentPadding: EdgeInsets.only(bottom: 2),
                  errorText: _pairingCodeError ? L10n(context).errorPairingFailed : null,
                ),
              ),
            ],
          ),
        ),
        actions: <Widget>[
          TextButton(
            child: Text(L10n(context).cancel.toUpperCase()),
            style: TextButton.styleFrom(disabledForegroundColor: Theme.of(context).primaryColor.withAlpha(100)),
            onPressed: _makingApiCall
                ? null
                : () {
                    // Pop dialog - false indicates no student was paired
                    Navigator.of(context).pop(false);
                  },
          ),
          TextButton(
            style: TextButton.styleFrom(disabledForegroundColor: Theme.of(context).primaryColor.withAlpha(100)),
            child: Text(L10n(context).ok),
            onPressed: _makingApiCall
                ? null
                : () async {
                    _showPairingCodeError(false);
                    _formKey.currentState?.save();
                  },
          ),
        ],
      ),
    );
  }

  void _showPairingCodeError(bool show) {
    _formKey.currentState?.validate();
    // Update the UI with the error state
    setState(() {
      _pairingCodeError = show;
    });
  }
}
