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

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/router/parent_router.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:provider/provider.dart';

class CourseSummaryScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<CourseDetailsModel>(
      builder: (context, model, _) => _CourseSummary(model),
    );
  }
}

class _CourseSummary extends StatefulWidget {
  final CourseDetailsModel model;

  const _CourseSummary(this.model, {Key key}) : super(key: key);

  @override
  __CourseSummaryState createState() => __CourseSummaryState();
}

class __CourseSummaryState extends State<_CourseSummary> with AutomaticKeepAliveClientMixin {
  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey();
  Future<List<ScheduleItem>> _future;

  @override
  bool get wantKeepAlive => true; // Retain this screen's state when switching tabs

  @override
  void initState() {
    _future = widget.model.loadSummary(refresh: false);
    super.initState();
  }

  Future<void> _refresh() async {
    setState(() {
      _future = widget.model.loadSummary(refresh: true);
    });
    return _future.catchError((_) {});
  }

  @override
  Widget build(BuildContext context) {
    super.build(context); // Required super call for AutomaticKeepAliveClientMixin
    return RefreshIndicator(
      key: _refreshKey,
      onRefresh: _refresh,
      child: FutureBuilder(
        future: _future,
        builder: (BuildContext context, AsyncSnapshot<List<ScheduleItem>> snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting && !snapshot.hasData) {
            return LoadingIndicator();
          } else if (snapshot.hasError) {
            return ErrorPandaWidget(L10n(context).errorLoadingCourseSummary, _refresh);
          } else {
            return _body(snapshot.data);
          }
        },
      ),
    );
  }

  Widget _body(List<ScheduleItem> items) {
    if (items.isEmpty) {
      return EmptyPandaWidget(
        svgPath: 'assets/svg/panda-space-no-assignments.svg',
        title: L10n(context).noCourseSummaryTitle,
        subtitle: L10n(context).noCourseSummaryMessage,
      );
    }
    return ListView.builder(
      itemCount: items.length,
      itemBuilder: (context, index) => _buildListItem(items[index]),
    );
  }

  Widget _buildListItem(ScheduleItem item) {
    String dateText;
    var date = item.startAt ?? item.allDayDate;
    if (date == null) {
      dateText = L10n(context).noDueDate;
    } else {
      dateText = date.l10nFormat(L10n(context).dateAtTime);
    }

    return ListTile(
      title: Text(item.title),
      subtitle: Text(dateText),
      leading: Icon(_getIcon(item), color: Theme.of(context).accentColor),
      onTap: () {
        if (item.type == ScheduleItem.typeCalendar) {
          ParentRouter.router.navigateTo(context, ParentRouter.eventDetails(widget.model.courseId, item.id),
              transition: TransitionType.material);
        } else {
          ParentRouter.router.navigateTo(
              context, ParentRouter.assignmentDetails(widget.model.courseId, item.assignment.id),
              transition: TransitionType.material);
        }
      },
    );
  }

  IconData _getIcon(ScheduleItem item) {
    if (item.type == ScheduleItem.typeCalendar) return CanvasIcons.calendar_month;
    if (item.assignment?.lockedForUser == true) return CanvasIcons.lock;
    if (item.assignment?.isQuiz == true) return CanvasIcons.quiz;
    if (item.assignment?.isDiscussion == true) return CanvasIcons.discussion;
    return CanvasIcons.assignment;
  }
}
