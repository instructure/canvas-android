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
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_extensions.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertThresholdsPercentageDialog extends StatefulWidget {
  final AlertType _alertType;
  final List<AlertThreshold?>? thresholds;
  final String _studentId;

  AlertThresholdsPercentageDialog(this.thresholds, this._alertType, this._studentId);

  @override
  State<StatefulWidget> createState() => AlertThresholdsPercentageDialogState();
}

class AlertThresholdsPercentageDialogState extends State<AlertThresholdsPercentageDialog> {
  bool _disableButtons = false;
  AlertThreshold? _threshold;
  bool _networkError = false;
  bool _neverClicked = false;

  String? maxValue;
  String? minValue;

  final int _disabledAlpha = 90;

  static final UniqueKey okButtonKey = UniqueKey(); // For testing

  String? errorMsg;

  final GlobalKey<FormFieldState> _formKey = GlobalKey<FormFieldState>();

  FocusScopeNode _focusScopeNode = FocusScopeNode();

  @override
  void initState() {
    _threshold = widget.thresholds.getThreshold(widget._alertType);

    var values = widget._alertType.getMinMax(widget.thresholds);
    minValue = values[0];
    maxValue = values[1];

    super.initState();
  }

  @override
  void dispose() {
    _focusScopeNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => ArrowAwareFocusScope(
        node: _focusScopeNode,
        child: AlertDialog(
          scrollable: true,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
          title: Text(widget._alertType.getTitle(context)),
          content: TextFormField(
            key: _formKey,
            autofocus: true,
            autovalidateMode: AutovalidateMode.always,
            keyboardType: TextInputType.number,
            initialValue: _threshold?.threshold,
            maxLength: 3,
            buildCounter: (_, {required currentLength, maxLength, required isFocused}) => null, // Don't show the counter
            inputFormatters: [
              // Only accept numbers, no other characters (including '-')
              FilteringTextInputFormatter.deny(RegExp('[^0-9]')),
            ],
            onChanged: (input) {
              errorMsg = null;
              // Check if we had a network error
              if (input.isEmpty) {
                // Don't validate when there's no input (no error)
                errorMsg = null;
              } else {
                var inputParsed = int.tryParse(input);
                var maxParsed = maxValue != null ? int.tryParse(maxValue!) ?? 100 : 100;
                var minParsed = minValue != null ? int.tryParse(minValue!) ?? 0 : 0;

                if (inputParsed != null) {
                  if (maxParsed == 100 && inputParsed > 100) {
                    errorMsg = L10n(context).mustBeBelow100;
                  } else if (maxParsed != 100 && inputParsed >= maxParsed) {
                    errorMsg = L10n(context).mustBeBelowN(maxParsed);
                  } else if (inputParsed <= minParsed) {
                    errorMsg = L10n(context).mustBeAboveN(minParsed);
                  }
                }
              }

              setState(() {
                if (errorMsg != null) {
                  // We had an error, disable the buttons
                  _disableButtons = true;
                } else {
                  _disableButtons = false;
                }
              });
            },
            validator: (input) {
              if (_networkError) {
                errorMsg = L10n(context).genericNetworkError;
              }
              return errorMsg;
            },
            onSaved: (input) async {
              // Don't do anything if there are existing validation errors and the user didn't click 'Never'
              if (_formKey.currentState?.validate() == false && !_neverClicked) return;

              if (_threshold == null && (input == null || input.isEmpty)) {
                // Threshold is already disabled
                Navigator.of(context).pop(null);
              }

              _showNetworkError(false);

              var result = await locator<AlertThresholdsInteractor>()
                  .updateAlertThreshold(widget._alertType, widget._studentId, _threshold,
                      value: ((input == null || input.isNotEmpty) && !_neverClicked) ? input : '-1')
                  .catchError((_) => null);

              if (result != null) {
                // Threshold was updated/deleted successfully
                if (input == null || input.isEmpty || _neverClicked) {
                  // Deleted a threshold
                  Navigator.of(context).pop(_threshold?.rebuild((b) => b.threshold = '-1'));
                } else {
                  // Updated a threshold
                  Navigator.of(context).pop(result);
                }
              } else {
                // There was a network error
                _showNetworkError(true);
              }

              _neverClicked = false;
            },
            onFieldSubmitted: (input) async {
              _formKey.currentState?.save();
            },
            decoration: InputDecoration(
              hintText: L10n(context).gradePercentage,
              hintStyle: TextStyle(color: ParentColors.ash),
              contentPadding: EdgeInsets.only(bottom: 2),
            ),
          ),
          actions: <Widget>[
            TextButton(
                child: Text(L10n(context).cancel.toUpperCase()),
                style: TextButton.styleFrom(disabledForegroundColor: ParentColors.parentApp.withAlpha(_disabledAlpha)),
                onPressed: () {
                  Navigator.of(context).pop(null);
                }),
            TextButton(
                child: Text(L10n(context).never.toUpperCase()),
                style: TextButton.styleFrom(disabledForegroundColor: ParentColors.parentApp.withAlpha(_disabledAlpha)),
                onPressed: () async {
                  if (_threshold == null) {
                    // Threshold is already disabled
                    Navigator.of(context).pop(null);
                    return;
                  }
                  _threshold = _threshold?.rebuild((b) => b.threshold = '-1');
                  _neverClicked = true;
                  _showNetworkError(false);
                  _formKey.currentState?.save();
                }),
            TextButton(
              key: okButtonKey,
              child: Text(L10n(context).ok),
              style: TextButton.styleFrom(disabledForegroundColor: ParentColors.parentApp.withAlpha(_disabledAlpha)),
              onPressed: _disableButtons
                  ? null
                  : () async {
                      _showNetworkError(false);
                      _formKey.currentState?.save();
                    },
            ),
          ],
        ),
      ),
    );
  }

  void _showNetworkError(bool show) {
    _formKey.currentState?.validate();
    setState(() {
      _networkError = show;
    });
  }
}
