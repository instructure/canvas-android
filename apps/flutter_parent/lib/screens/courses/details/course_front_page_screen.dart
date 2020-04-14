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
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/page.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseFrontPageScreen extends StatefulWidget {
  final String courseId;

  CourseFrontPageScreen({Key key, this.courseId})
      : assert(courseId != null),
        super(key: key);

  @override
  _CourseFrontPageScreenState createState() => _CourseFrontPageScreenState();
}

class _CourseFrontPageScreenState extends State<CourseFrontPageScreen> with AutomaticKeepAliveClientMixin {
  Future<Page> _pageFuture;

  @override
  bool get wantKeepAlive => true;

  Future<Page> _refreshPage() {
    setState(() {
      _pageFuture = _interactor.loadHomePage(widget.courseId, forceRefresh: true);
    });
    return _pageFuture?.catchError((_) {});
  }

  CourseDetailsInteractor get _interactor => locator<CourseDetailsInteractor>();

  @override
  void initState() {
    _pageFuture = _interactor.loadHomePage(widget.courseId);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context); // Required super call for AutomaticKeepAliveClientMixin
    CourseDetailsModel.selectedTab = 1;
    return RefreshIndicator(
      onRefresh: () {
        return _refreshPage();
      },
      child: FutureBuilder(
        future: _pageFuture,
        builder: (context, AsyncSnapshot<Page> snapshot) {
          if (snapshot.hasError) {
            return ErrorPandaWidget(L10n(context).unexpectedError, () => _refreshPage());
          } else if (!snapshot.hasData) {
            return LoadingIndicator();
          } else {
            return _CourseHomePage(snapshot.data);
          }
        },
      ),
    );
  }
}

class _CourseHomePage extends StatelessWidget {
  final Page homePage;

  const _CourseHomePage(this.homePage, {Key key})
      : assert(homePage != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      physics: AlwaysScrollableScrollPhysics(),
      child: Padding(
        padding: const EdgeInsets.only(top: 16.0),
        child: CanvasWebView(
          content: homePage.body,
          emptyDescription: homePage.lockExplanation ?? L10n(context).noPageFound,
          horizontalPadding: 16,
          fullScreen: false,
        ),
      ),
    );
  }
}
