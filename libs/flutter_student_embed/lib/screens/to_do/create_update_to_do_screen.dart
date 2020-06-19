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
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen_interactor.dart';
import 'package:flutter_student_embed/utils/common_widgets/appbar_dynamic_style.dart';
import 'package:flutter_student_embed/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_student_embed/utils/common_widgets/colored_status_bar.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/core_extensions/string_extensions.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

class CreateUpdateToDoScreen extends StatefulWidget with ColoredStatusBar {
  final DateTime initialDate;
  final PlannerItem editToDo;

  const CreateUpdateToDoScreen({Key key, this.editToDo, this.initialDate}) : super(key: key);

  @override
  _CreateUpdateToDoScreenState createState() => _CreateUpdateToDoScreenState();
}

class _CreateUpdateToDoScreenState extends State<CreateUpdateToDoScreen> {
  Course _selectedCourse;
  Future<List<Course>> _coursesFuture;
  DateTime _date;
  TextEditingController _titleController;
  TextEditingController _descriptionController;
  bool _saving = false;
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  FocusScopeNode _focusScopeNode = FocusScopeNode();

  @override
  void initState() {
    _coursesFuture = locator<CreateUpdateToDoScreenInteractor>().getCoursesForUser();
    _date = widget.editToDo?.plannable?.toDoDate?.toLocal() ?? widget.initialDate ?? DateTime.now();
    _titleController = TextEditingController(text: widget.editToDo?.plannable?.title);
    _descriptionController = TextEditingController(text: widget.editToDo?.plannable?.details);
    if (widget.editToDo != null) {
      _coursesFuture.then((courses) {
        var course = courses.firstWhere((it) => it.id == widget.editToDo.plannable.courseId, orElse: () => null);
        setState(() => _selectedCourse = course);
      });
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: _onWillPop,
      child: WhiteAppBarTheme(
        builder: (context) => ArrowAwareFocusScope(
          node: _focusScopeNode,
          child: Scaffold(
            key: _scaffoldKey,
            appBar: dynamicStyleAppBar(
              context: context,
              appBar: AppBar(
                title: Text(widget.editToDo == null ? L10n(context).newToDo : L10n(context).editToDo),
                actions: <Widget>[
                  if (_saving)
                    Container(
                      alignment: Alignment.center,
                      padding: EdgeInsets.only(right: 16),
                      child: SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            valueColor: AlwaysStoppedAnimation<Color>(Theme.of(context).buttonColor),
                          )),
                    ),
                  if (!_saving)
                    InkWell(
                      onTap: _save,
                      child: Container(
                        height: 48,
                        padding: EdgeInsets.symmetric(horizontal: 16),
                        alignment: Alignment.center,
                        child: Text(
                          L10n(context).save.toUpperCase(),
                          style: Theme.of(context).textTheme.subhead.copyWith(color: Theme.of(context).buttonColor),
                        ),
                      ),
                    )
                ],
              ),
            ),
            body: SingleChildScrollView(
              child: Column(
                children: <Widget>[
                  TextField(
                    key: Key('title-input'),
                    controller: _titleController,
                    textCapitalization: TextCapitalization.sentences,
                    decoration: InputDecoration(
                      contentPadding: EdgeInsets.all(16),
                      border: InputBorder.none,
                      hintText: L10n(context).toDoTitleHint,
                    ),
                  ),
                  Divider(height: 0),
                  FutureBuilder<List<Course>>(
                    future: _coursesFuture,
                    builder: (context, snapshot) {
                      List<DropdownMenuItem<Course>> items = [];
                      items.add(
                        DropdownMenuItem(
                          value: null,
                          child: Text(
                            L10n(context).toDoCourseNone,
                            style: TextStyle(color: StudentColors.ash),
                          ),
                        ),
                      );
                      if (snapshot.hasData) {
                        items += snapshot.data
                            .map((it) => DropdownMenuItem(
                                value: it,
                                child: Row(
                                  children: <Widget>[
                                    Container(
                                      width: 16,
                                      height: 16,
                                      decoration: BoxDecoration(
                                        shape: BoxShape.circle,
                                        color: StudentTheme.of(context).getCanvasContextColor('course_${it.id}'),
                                      ),
                                    ),
                                    SizedBox(width: 12),
                                    Text(it.name),
                                  ],
                                )))
                            .toList();
                      }
                      return Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        child: DropdownButton<Course>(
                          isExpanded: true,
                          value: _selectedCourse,
                          underline: Container(),
                          selectedItemBuilder: (context) {
                            return items.map((it) {
                              if (it.value == null) {
                                return Container(
                                  height: 48,
                                  alignment: Alignment.centerLeft,
                                  child: Text(
                                    L10n(context).toDoCourseLabel,
                                    style: it.value == null ? TextStyle(color: StudentColors.ash) : null,
                                  ),
                                );
                              }
                              Course course = it.value;
                              return Container(
                                height: 48,
                                alignment: Alignment.centerLeft,
                                child: Row(
                                  children: <Widget>[
                                    Container(
                                      width: 16,
                                      height: 16,
                                      decoration: BoxDecoration(
                                        shape: BoxShape.circle,
                                        color: StudentTheme.of(context).getCanvasContextColor('course_${course.id}'),
                                      ),
                                    ),
                                    SizedBox(width: 12),
                                    Text(course.name,
                                        style: it.value == null ? TextStyle(color: StudentColors.ash) : null),
                                  ],
                                ),
                              );
                            }).toList();
                          },
                          items: items,
                          onChanged: (course) => setState(() => _selectedCourse = course),
                        ),
                      );
                    },
                  ),
                  Divider(height: 0),
                  InkWell(
                    onTap: _setDate,
                    child: Container(
                      padding: EdgeInsets.symmetric(horizontal: 16),
                      height: 48,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: <Widget>[
                          Text(
                            L10n(context).date,
                            style: Theme.of(context).textTheme.subhead,
                          ),
                          Text(
                            _date.l10nFormat(L10n(context).dateAtTime),
                            style: Theme.of(context).textTheme.subhead,
                          ),
                        ],
                      ),
                    ),
                  ),
                  Divider(height: 0),
                  TextField(
                    key: Key('description-input'),
                    controller: _descriptionController,
                    textCapitalization: TextCapitalization.sentences,
                    style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
                    minLines: 8,
                    maxLines: 100,
                    decoration: InputDecoration(
                        contentPadding: EdgeInsets.all(16),
                        border: InputBorder.none,
                        hintText: L10n(context).toDoDescriptionHint,
                        hintStyle: TextStyle(color: StudentColors.ash)),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _setDate() async {
    DateTime date;
    TimeOfDay time;

    date = await showDatePicker(
      context: context,
      initialDate: _date,
      firstDate: DateTime(2000, 1, 1),
      lastDate: _date.add(Duration(days: 365)),
    );

    if (date != null) {
      time = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(_date));
    }

    if (date != null && time != null) {
      DateTime newDate = DateTime(date.year, date.month, date.day, time.hour, time.minute);
      setState(() {
        _date = newDate;
      });
    }
  }

  bool _hasUnsavedChanges() {
    if (widget.editToDo != null) {
      return _titleController.text != (widget.editToDo.plannable.title ?? '') ||
          _descriptionController.text != (widget.editToDo.plannable.details ?? '') ||
          _selectedCourse?.id != widget.editToDo.plannable.courseId ||
          _date != widget.editToDo.plannable.toDoDate?.toLocal();
    } else {
      return _titleController.text.isNotEmpty ||
          _descriptionController.text.isNotEmpty ||
          _selectedCourse != null ||
          _date != widget.initialDate;
    }
  }

  Future<bool> _onWillPop() async {
    if (_saving) return false;
    if (!_hasUnsavedChanges()) return true;
    return await showDialog(
          context: context,
          builder: (context) => new AlertDialog(
            title: new Text(L10n(context).unsavedChangesDialogTitle),
            content: new Text(L10n(context).unsavedChangesDialogBody),
            actions: <Widget>[
              new FlatButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: new Text(L10n(context).no.toUpperCase()),
              ),
              new FlatButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: new Text(L10n(context).yes.toUpperCase()),
              ),
            ],
          ),
        ) ??
        false;
  }

  Future<void> _save() async {
    String title = _titleController.text;
    String description = _descriptionController.text;
    String courseId = _selectedCourse?.id;
    DateTime date = _date;

    if (title.isNullOrBlank()) {
      _scaffoldKey.currentState.showSnackBar(SnackBar(content: Text(L10n(context).titleEmptyErrorMessage)));
      return;
    }

    try {
      setState(() => _saving = true);
      var interactor = locator<CreateUpdateToDoScreenInteractor>();
      if (widget.editToDo != null) {
        await interactor.updateToDo(widget.editToDo.plannable.id, title, description, date, courseId);
        Navigator.pop(context, [_date, widget.editToDo.plannable.toDoDate]);
      } else {
        await interactor.createToDo(title, description, date, courseId);
        Navigator.pop(context, [_date]);
      }
    } catch (e, s) {
      setState(() => _saving = false);
      _scaffoldKey.currentState.showSnackBar(SnackBar(content: Text(L10n(context).errorSavingToDo)));
    }
  }
}
