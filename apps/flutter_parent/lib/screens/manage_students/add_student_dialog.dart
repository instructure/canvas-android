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
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'manage_students_interactor.dart';

class AddStudentDialog extends StatefulWidget {
  final _interactor = locator<ManageStudentsInteractor>();

  final String _pairingCode;

  AddStudentDialog(this._pairingCode, {Key key});

  @override
  State<StatefulWidget> createState() => AddStudentDialogState(_pairingCode);
}

class AddStudentDialogState extends State<AddStudentDialog> {
  var _pairingCodeError = false;
  var _makingApiCall = false;
  var _initialPairingCode = '';

  final GlobalKey<FormFieldState> _formKey = GlobalKey<FormFieldState>();

  AddStudentDialogState(this._initialPairingCode);

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
      title: Text(L10n(context).addStudent),
      content: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.only(bottom: 20.0),
            child: Text(
              L10n(context).pairingCodeEntryExplanation,
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 12.0),
            ),
          ),
          TextFormField(
            key: _formKey,
            autofocus: true,
            autocorrect: false,
            autovalidate: false,
            initialValue: _initialPairingCode,
            onChanged: (value) {
              _showParingCodeError(false);
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

              _showParingCodeError(false);

              var successful = await widget._interactor.pairWithStudent(code);
              if (successful) {
                // Close dialog - return 'true' to represent that a student was paired
                Navigator.of(context).pop(true);
              } else {
                _showParingCodeError(true);
              }

              // Enable OK and Cancel buttons
              setState(() {
                _makingApiCall = false;
              });
            },
            onFieldSubmitted: (code) async {
              _formKey.currentState.save();
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
      actions: <Widget>[
        FlatButton(
          disabledTextColor: ParentColors.parentApp.withAlpha(100),
          child: Text(L10n(context).cancel.toUpperCase()),
          onPressed: _makingApiCall
              ? null
              : () {
                  // Pop dialog - false indicates no student was paired
                  Navigator.of(context).pop(false);
                },
        ),
        FlatButton(
          disabledTextColor: ParentColors.parentApp.withAlpha(100),
          child: Text(L10n(context).ok),
          onPressed: _makingApiCall
              ? null
              : () async {
                  _showParingCodeError(false);
                  _formKey.currentState.save();
                },
        ),
      ],
    );
  }

  void _showParingCodeError(bool show) {
    _formKey.currentState.validate();
    // Update the UI with the error state
    setState(() {
      _pairingCodeError = show;
    });
  }
}
