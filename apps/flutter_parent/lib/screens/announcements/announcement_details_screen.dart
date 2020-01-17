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
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:tuple/tuple.dart';
import 'package:webview_flutter/webview_flutter.dart';

import 'announcement_details_interactor.dart';

class AnnouncementDetailScreen extends StatefulWidget {
  final _interactor = locator<AnnouncementDetailsInteractor>();
  final String _announcementId;
  final String _courseId;

  AnnouncementDetailScreen(this._announcementId, this._courseId, {Key key}) : super(key: key);

  @override
  State createState() => _AnnouncementDetailScreenState();
}

class _AnnouncementDetailScreenState extends State<AnnouncementDetailScreen> {
  Future<Tuple2<Announcement, Course>> _announcementFuture;

  Future<Tuple2<Announcement, Course>> _loadAnnouncement() =>
      widget._interactor.getCourseAnnouncement(
          widget._courseId, widget._announcementId);


  @override
  void initState() {
    super.initState();
    _announcementFuture = _loadAnnouncement();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _announcementFuture,
      builder: (context, AsyncSnapshot<Tuple2<Announcement, Course>> snapshot) {
        if(snapshot.connectionState == ConnectionState.waiting) {
          return Container(child: LoadingIndicator());
        }

        if(snapshot.hasError || snapshot.data == null) {
          return _error();
        } else {
          return _courseAnnouncementWidget(snapshot.data.item1, snapshot.data.item2);
        }
      },
    );
  }

  Widget _courseAnnouncementWidget(Announcement announcement, Course course) {
    return Scaffold(
      appBar: AppBar(
        title: Text(course.name),
      ),
      body: _announcementBody(announcement),
    );
  }

  Widget _announcementBody(Announcement announcement) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.fromLTRB(0, 0, 0, 4),
            child: Text("This is a course announcement title"),
          ),
          Text("Jun 19 at 10:02pm"),
          Divider(),
          Container(
            height: 400,
            child: WebView(
              initialUrl: '',

            ),
          ),
        ],
      ),
    );
  }

  Widget _error() {
    // TODO ERROR STATE
    return Container(color: Colors.red);
  }
}