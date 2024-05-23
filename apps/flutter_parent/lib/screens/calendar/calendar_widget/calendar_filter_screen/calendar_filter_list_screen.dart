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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CalendarFilterListScreen extends StatefulWidget {
  final Set<String> _selectedCourses;

  CalendarFilterListScreen(this._selectedCourses);

  @override
  State<StatefulWidget> createState() => CalendarFilterListScreenState();
}

class CalendarFilterListScreenState extends State<CalendarFilterListScreen> {
  late Future<List<Course>?> _coursesFuture;
  Set<String> selectedContextIds = {}; // Public, to allow for testing
  final GlobalKey<RefreshIndicatorState> _refreshCoursesKey = new GlobalKey<RefreshIndicatorState>();
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  bool selectAllIfEmpty = true;
  int courseLength = -1;

  @override
  void initState() {
    _coursesFuture = locator.get<CalendarFilterListInteractor>().getCoursesForSelectedStudent(isRefresh: false);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        // An empty list is interpreted as all courses selected TODO - change back for planner api
        //Navigator.pop(context, courseLength == selectedContextIds.length ? <String>{} : selectedContextIds);
        Navigator.pop(context, selectedContextIds);
        return false;
      },
      child: DefaultParentTheme(
        builder: (context) => Scaffold(
          key: _scaffoldKey,
          appBar: AppBar(
            title: Text(L10n(context).calendars),
            bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
          ),
          body: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              SizedBox(height: 16.0),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Text(L10n(context).calendarTapToFavoriteDesc, style: Theme.of(context).textTheme.bodyMedium),
              ),
              SizedBox(height: 24.0),
              Expanded(child: _body())
            ],
          ),
        ),
      ),
    );
  }

  Widget _body() {
    return FutureBuilder(
        future: _coursesFuture,
        builder: (context, snapshot) {
          Widget _body;
          List<Course>?_courses;
          if (snapshot.hasError) {
            _body = ErrorPandaWidget(L10n(context).errorLoadingCourses, () => _refreshCoursesKey.currentState?.show());
          } else if (snapshot.hasData) {
            _courses = snapshot.data!;
            courseLength = _courses.length;
            if (selectedContextIds.isEmpty && selectAllIfEmpty) {
              // We only want to do this the first time we load, otherwise if the user ever deselects all the
              // contexts, then they will all automatically be put into the selected list
              // Note: As unlikely as it is, if the user deselects all contexts then all contexts will be returned in the calendar

              // List will be empty when all courses are selected (on first load)
              final tempList = _courses.map((c) => 'course_${c.id}').toList();
              selectedContextIds.addAll(tempList);
              selectAllIfEmpty = false;
            }
            _body = (_courses.isEmpty)
                ? EmptyPandaWidget(
                    svgPath: 'assets/svg/panda-book.svg',
                    title: L10n(context).noCoursesTitle,
                    subtitle: L10n(context).noCoursesMessage,
                  )
                : _courseList(_courses);
          } else {
            // This is a user with a filter from before the api migration, make sure we trim their list down.
            selectedContextIds.addAll(widget._selectedCourses);
            if (selectedContextIds.isNotEmpty) {
              // The list isn't empty so we don't want to continue checking if the list is empty above, and
              // select everything again (though if the user doesn't select anything and they go back, everything will be
              // selected).
              selectAllIfEmpty = false;
            }
            return LoadingIndicator();
          }

          return RefreshIndicator(
            key: _refreshCoursesKey,
            onRefresh: () {
              _coursesFuture =
                  locator.get<CalendarFilterListInteractor>().getCoursesForSelectedStudent(isRefresh: true);
              setState(() {});
              return _coursesFuture;
            },
            child: _body,
          );
        });
  }

  ListView _courseList(List<Course> courses) {
    List<Widget> _listItems = [
      _listHeader(L10n(context).coursesLabel),
      ...courses.map((c) => MergeSemantics(
            child: LabeledCheckbox(
                label: c.name,
                padding: const EdgeInsets.only(left: 2.0, right: 16.0),
                value: selectedContextIds.contains(c.contextFilterId()),
                onChanged: (bool newValue) {
                  setState(() {
                    if (newValue) {
                      selectedContextIds.add(c.contextFilterId());
                    } else {
                      if (selectedContextIds.length == 1) {
                        // The list cannot be empty, the calendar wouldn't do anything!
                        ScaffoldMessenger.of(context).removeCurrentSnackBar();
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(L10n(context).minimumCalendarsError)));
                      } else {
                        selectedContextIds.remove(c.contextFilterId());
                      }
                    }
                  });
                }),
          ))
    ];
    return ListView.builder(
        key: Key('calendar_filter_list_key'),
        physics: AlwaysScrollableScrollPhysics(),
        itemCount: _listItems.length, // Add one for the Courses header
        itemBuilder: (context, index) {
          return _listItems[index];
        });
  }

  Widget _listHeader(String title) => Padding(
        padding: const EdgeInsets.only(left: 16.0, right: 16.0),
        child: Text(
          title,
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      );
}

// Custom checkbox to better control the padding at the start
class LabeledCheckbox extends StatelessWidget {
  const LabeledCheckbox({
    required this.label,
    required this.padding,
    required this.value,
    required this.onChanged,
  });

  final String label;
  final EdgeInsets padding;
  final bool value;
  final Function onChanged;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        onChanged(!value);
      },
      child: Padding(
        padding: padding,
        child: Row(
          children: <Widget>[
            Checkbox(
              value: value,
              onChanged: (bool? newValue) {
                onChanged(newValue);
              },
            ),
            SizedBox(width: 21.0),
            Expanded(child: Text(label, style: Theme.of(context).textTheme.titleMedium)),
          ],
        ),
      ),
    );
  }
}
