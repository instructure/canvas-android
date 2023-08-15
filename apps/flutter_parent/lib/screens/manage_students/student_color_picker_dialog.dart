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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_interactor.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class StudentColorPickerDialog extends StatefulWidget {
  final String studentId;
  final Color initialColor;

  const StudentColorPickerDialog({required this.initialColor, required this.studentId, super.key});
  @override
  _StudentColorPickerDialogState createState() => _StudentColorPickerDialogState();
}

class _StudentColorPickerDialogState extends State<StudentColorPickerDialog> {
  late Color _selectedColor;
  bool _saving = false;
  bool _error = false;

  @override
  void initState() {
    _selectedColor = widget.initialColor;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      contentPadding: EdgeInsets.zero,
      title: Text(L10n(context).selectStudentColor),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          SingleChildScrollView(child: _colorOptions(), scrollDirection: Axis.horizontal),
          if (_error)
            Padding(
              padding: const EdgeInsetsDirectional.only(start: 24, top: 8, end: 24),
              child: Text(
                L10n(context).errorSavingColor,
                style: TextStyle(color: ParentColors.failure),
              ),
            ),
        ],
      ),
      actions: <Widget>[
        if (_saving)
          TextButton(
            child: Container(
              width: 18,
              height: 18,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
            onPressed: null,
          ),
        if (!_saving) TextButton(child: Text(L10n(context).cancel), onPressed: () => Navigator.of(context).pop(false)),
        if (!_saving) TextButton(child: Text(L10n(context).ok), onPressed: _save),
      ],
    );
  }

  Widget _colorOptions() {
    return Padding(
      padding: EdgeInsetsDirectional.only(start: 16, top: 12, end: 16),
      child: Row(
        key: Key('color-options'),
        children: <Widget>[...StudentColorSet.all.map((colorSet) => _colorOption(colorSet))],
      ),
    );
  }

  Widget _colorOption(StudentColorSet colorSet) {
    var selected = _selectedColor == colorSet.light;
    var displayColor = ParentTheme.of(context)?.getColorVariantForCurrentState(colorSet) ?? Colors.transparent;
    return Semantics(
      selected: selected,
      label: StudentColorSet.getA11yName(colorSet, context),
      child: InkResponse(
        onTap: () {
          setState(() {
            _selectedColor = colorSet.light;
          });
        },
        child: Container(
          width: 48,
          height: 48,
          decoration: ShapeDecoration(
            shape: CircleBorder(
              side: BorderSide(color: selected ? displayColor : Colors.transparent, width: 3),
            ),
          ),
          padding: EdgeInsets.all(4),
          child: Container(
            decoration: BoxDecoration(
              color: displayColor,
              shape: BoxShape.circle,
            ),
          ),
        ),
      ),
    );
  }

  void _save() async {
    if (_selectedColor == widget.initialColor) {
      // Selection has not changed, pop without saving
      Navigator.of(context).pop(false);
      return;
    }
    setState(() => _saving = true);

    try {
      await locator<StudentColorPickerInteractor>().save(widget.studentId, _selectedColor);
      ParentTheme.of(context)?.refreshStudentColor();
      Navigator.of(context).pop(true);
    } catch (e, s) {
      setState(() {
        _saving = false;
        _error = true;
      });
    }
  }
}
