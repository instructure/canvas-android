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
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

import 'conversation_list_interactor.dart';

class ConversationListScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return ConversationListState();
  }
}

class ConversationListState extends State<ConversationListScreen> {
  Future<List<Conversation>> _conversationsFuture;
  final GlobalKey<RefreshIndicatorState> _refreshIndicatorKey = new GlobalKey<RefreshIndicatorState>();

  ConversationListInteractor interactor = locator<ConversationListInteractor>();

  @override
  void initState() {
    super.initState();
    _conversationsFuture = interactor.getConversations();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(L10n(context).inbox),
          bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
        ),
        body: FutureBuilder(
          future: _conversationsFuture,
          builder: (BuildContext context, AsyncSnapshot<List<Conversation>> snapshot) {
            Widget body;
            if (snapshot.hasError) {
              body = _errorState(context);
            } else if (snapshot.hasData) {
              if (snapshot.data.isNotEmpty) {
                body = _successState(context, snapshot.data);
              } else {
                body = _emptyState(context);
              }
            } else {
              body = _loadingState(context);
            }
            return RefreshIndicator(
              key: _refreshIndicatorKey,
              child: body,
              onRefresh: () {
                setState(() {
                  _conversationsFuture = interactor.getConversations(forceRefresh: true);
                });
                return _conversationsFuture;
              },
            );
          },
        ),
        floatingActionButton: FloatingActionButton(
          tooltip: L10n(context).newMessageTitle,
          child: Padding(
            padding: const EdgeInsets.only(left: 4, top: 4),
            child: Icon(CanvasIconsSolid.comment),
          ),
          onPressed: () => _createMessage(context),
        ),
      ),
    );
  }

  Widget _errorState(BuildContext context) {
    return ErrorPandaWidget(L10n(context).errorLoadingMessages, () {
      _refreshIndicatorKey.currentState.show();
    });
  }

  Widget _loadingState(BuildContext context) => LoadingIndicator();

  Widget _successState(BuildContext context, List<Conversation> data) {
    return ListView.builder(
      padding: EdgeInsets.symmetric(vertical: 8),
      itemCount: data.length,
      itemBuilder: (context, index) {
        var item = data[index];
        final tile = ListTile(
          contentPadding: EdgeInsets.fromLTRB(8, 8, 16, 8),
          leading: _avatar(context, item),
          title: Row(
            textBaseline: TextBaseline.alphabetic,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.baseline,
            children: <Widget>[
              Flexible(
                child: Padding(
                  padding: const EdgeInsets.only(right: 8.0),
                  child: Text(
                    item.subject.isEmpty ? L10n(context).noSubject : item.subject,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(fontWeight: FontWeight.w500),
                  ),
                ),
              ),
              Text(
                _formatMessageDate(item.lastMessageAt ?? item.lastAuthoredMessageAt),
                style: Theme.of(context).textTheme.caption,
              ),
            ],
          ),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              SizedBox(height: 2),
              if (item.contextName?.isNotEmpty == true)
                Text(
                  item.contextName,
                  style: TextStyle(fontWeight: FontWeight.w500),
                ),
              SizedBox(height: 4),
              Text(
                item.lastMessage ?? item.lastAuthoredMessage,
                style: Theme.of(context).textTheme.body1,
                maxLines: 2,
              ),
            ],
          ),
          onTap: () async {
            var refresh = await locator<QuickNav>().push(
              context,
              ConversationDetailsScreen(
                conversationId: item.id,
                conversationSubject: item.subject,
                courseName: item.contextName,
              ),
            );
            if (refresh == true || item.isUnread()) _refreshIndicatorKey.currentState.show();
          },
        );
        return item.isUnread() ? WidgetBadge(tile) : tile;
      },
    );
  }

  Widget _emptyState(BuildContext context) {
    return EmptyPandaWidget(
      svgPath: 'assets/svg/panda-inbox-zero.svg',
      title: L10n(context).emptyInboxTitle,
      subtitle: L10n(context).emptyInboxSubtitle,
    );
  }

  String _formatMessageDate(DateTime date) {
    if (date == null) return '';
    date = date.toLocal();
    var format = DateFormat.MMM().add_d();
    var now = DateTime.now();
    if (date.year != now.year) {
      format = format.add_y();
    } else if (date.month == now.month && date.day == now.day) {
      format = DateFormat.jm();
    }
    return format.format(date);
  }

  Widget _avatar(BuildContext context, Conversation conversation) {
    Widget avatar;

    var users = conversation.participants?.toList() ?? [];
    users.retainWhere((user) => conversation.audience.contains(user.id));

    if (users.length == 2) {
      avatar = SizedBox(
        width: 40,
        height: 40,
        child: Stack(
          children: <Widget>[
            Avatar(users[0].avatarUrl, name: users[0].name, radius: 12),
            Positioned(
              bottom: 0,
              right: 0,
              child: CircleAvatar(
                radius: 14,
                backgroundColor: Theme.of(context).scaffoldBackgroundColor,
                child: Avatar(users[1].avatarUrl, name: users[1].name, radius: 12),
              ),
            )
          ],
        ),
      );
    } else if (users.length > 2) {
      avatar = Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: ParentTheme.of(context).nearSurfaceColor,
          shape: BoxShape.circle,
        ),
        child: Icon(CanvasIcons.group),
      );
    } else {
      avatar = Avatar(conversation.avatarUrl);
    }

    avatar = Container(
      width: 48,
      alignment: Alignment.topRight,
      child: avatar,
    );

    return avatar;
  }

  _createMessage(BuildContext context) {
    var coursesFuture = interactor.getCoursesForCompose();
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return FutureBuilder(
          future: coursesFuture,
          builder: (BuildContext context, AsyncSnapshot<List<Course>> snapshot) {
            if (snapshot.hasError) {
              return Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: <Widget>[
                    Icon(
                      CanvasIcons.warning,
                      color: ParentColors.failure,
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: Text(L10n(context).errorFetchingCourses),
                    ),
                  ],
                ),
              );
            } else if (snapshot.hasData) {
              return ListView(
                padding: EdgeInsets.symmetric(vertical: 8),
                shrinkWrap: true,
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    child: Text(
                      L10n(context).messageChooseCourse,
                      style: Theme.of(context).textTheme.caption,
                    ),
                  ),
                  ...snapshot.data.map((it) => ListTile(
                        title: Text(it.name),
                        onTap: () async {
                          Navigator.pop(context); // Dismisses the bottom sheet
                          var refresh = await locator<QuickNav>().push(context, CreateConversationScreen(it));
                          if (refresh == true) _refreshIndicatorKey.currentState.show();
                        },
                      )),
                ],
              );
            } else {
              return Container(
                height: 120,
                child: LoadingIndicator(),
              );
            }
          },
        );
      },
    );
  }
}
