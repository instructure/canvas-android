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
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';

/// A screen for viewing and configuring the app theme. This will not be a user-facing screen so
/// the Strings used here will not be translated.
class ThemeViewerScreen extends StatefulWidget {
  static final GlobalKey<ScaffoldState> scaffoldKey = GlobalKey<ScaffoldState>();
  static final Key studentColorKey = Key("student-color");

  @override
  _ThemeViewerScreenState createState() => _ThemeViewerScreenState();
}

class _ThemeViewerScreenState extends State<ThemeViewerScreen> {
  bool _allToggle = true;

  _toggleAll() {
    setState(() => _allToggle = !_allToggle);
  }

  Map<String, TextStyle> getStyles(TextTheme theme) => {
        'subtitle / caption': theme.subtitle,
        'overline / subhead': theme.overline,
        'body1 / body': theme.body1,
        'caption / subtitle': theme.caption,
        'subhead / title': theme.subhead,
        'headline / heading': theme.headline,
        'display1 / display': theme.display1,
        'button / -': theme.button,
        'body2 / -': theme.body2,
        'title / -': theme.title,
        'display2 / -': theme.display2,
        'display3 / -': theme.display3,
        'display4 / -': theme.display4,
      };

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        key: ThemeViewerScreen.scaffoldKey,
        drawer: Drawer(
          child: Container(
            color: Theme.of(context).scaffoldBackgroundColor,
            child: SafeArea(
              child: ListView(
                children: [
                  DrawerHeader(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Container(
                          key: ThemeViewerScreen.studentColorKey,
                          width: 48,
                          height: 48,
                          color: Theme.of(context).accentColor,
                        ),
                        Text("Theme configuration", style: Theme.of(context).textTheme.title),
                        Text("Play around with some values", style: Theme.of(context).textTheme.caption),
                      ],
                    ),
                  ),
                  ..._drawerContents(context),
                ],
              ),
            ),
          ),
        ),
        appBar: AppBar(
          title: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text("Theme Viewer"),
              Text("View all the things", style: Theme.of(context).primaryTextTheme.caption),
            ],
          ),
          actions: <Widget>[
            IconButton(icon: Icon(CanvasIcons.email), onPressed: () {}),
            IconButton(icon: Icon(CanvasIcons.search), onPressed: () {}),
          ],
          bottom: ParentTheme.of(context).appBarDivider(
            bottom: TabBar(
              indicatorColor: Theme.of(context).primaryIconTheme.color,
              tabs: [
                Tab(text: "Widgets"),
                Tab(text: "Text Styles"),
                Tab(text: 'Icons'),
              ],
            ),
          ),
        ),
        body: Container(
          child: TabBarView(
            children: [
              _content(context),
              _textStyles(context),
              _icons(context),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          child: Icon(CanvasIconsSolid.chat),
          onPressed: () {},
        ),
        bottomNavigationBar: Builder(
          builder: (context) {
            int selectedIdx = 1;
            return StatefulBuilder(
              builder: (context, setState) => Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Divider(height: 0.5, thickness: 0.5),
                  BottomNavigationBar(
                    backgroundColor: Theme.of(context).scaffoldBackgroundColor,
                    unselectedItemColor: Theme.of(context).textTheme.caption.color,
                    onTap: (value) => setState(() => selectedIdx = value), // new
                    currentIndex: selectedIdx, // new
                    items: [
                      new BottomNavigationBarItem(
                        icon: Icon(CanvasIcons.courses),
                        title: Text('Courses'),
                      ),
                      new BottomNavigationBarItem(
                        icon: Icon(CanvasIcons.calendar_month),
                        title: Text('Calendar'),
                      ),
                      new BottomNavigationBarItem(
                        icon: Icon(CanvasIcons.alerts),
                        title: Text('Alerts'),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  List<Widget> _drawerContents(BuildContext context) {
    return [
      Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: DropdownButton<int>(
          hint: Text("Tralala"),
          value: ParentTheme.of(context).studentIndex,
          onChanged: (index) => ParentTheme.of(context).studentIndex = index,
          isExpanded: true,
          items: StudentColorSet.all
              .asMap()
              .entries
              .map(
                (it) => DropdownMenuItem<int>(
                  value: it.key,
                  child: Row(
                    mainAxisSize: MainAxisSize.max,
                    children: <Widget>[
                      Container(
                        width: 12,
                        height: 12,
                        color: ParentTheme.of(context).getColorVariantForCurrentState(it.value),
                      ),
                      SizedBox(width: 8),
                      Flexible(child: Text("Student Color ${it.key + 1}")),
                    ],
                  ),
                ),
              )
              .toList(),
        ),
      ),
      SwitchListTile(
        title: Text("Dark Mode"),
        subtitle: Text("Subtitle"),
        value: ParentTheme.of(context).isDarkMode,
        onChanged: (_) => ParentTheme.of(context).toggleDarkMode(),
      ),
      SwitchListTile(
        title: Text("High Contrast Mode"),
        value: ParentTheme.of(context).isHC,
        onChanged: (_) => ParentTheme.of(context).toggleHC(),
      ),
    ];
  }

  Widget _content(BuildContext context) {
    var swatch = ParentColors.makeSwatch(ParentTheme.of(context).studentColor);
    return ListView(
      children: <Widget>[
        AppBar(
          textTheme: Theme.of(context).textTheme,
          iconTheme: Theme.of(context).iconTheme,
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
          title: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text("Inverse AppBar"),
              Text("Inbox, creating/editing, etc", style: Theme.of(context).textTheme.caption),
            ],
          ),
          actions: <Widget>[
            IconButton(icon: Icon(CanvasIcons.email), onPressed: () {}),
            IconButton(icon: Icon(CanvasIcons.search), onPressed: () {}),
          ],
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              for (var i = 0; i <= 900; i += 100)
                Material(
                  elevation: 1,
                  child: Container(
                    color: swatch[i == 0 ? 50 : i],
                    width: 24,
                    height: 24,
                    child: Center(
                      child: Text(
                        i == 0 ? "50" : i.toString(),
                        style: TextStyle(
                            fontSize: 8,
                            color: swatch[i == 0 ? 50 : i].computeLuminance() > 0.5 ? Colors.black : Colors.white),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
        Divider(),
        Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text("Essay: The Rocky Planet", style: Theme.of(context).textTheme.display1),
              ),
              Row(
                children: [
                  Text("100 pts", style: Theme.of(context).textTheme.caption),
                  Padding(
                    padding: const EdgeInsets.only(left: 12, right: 4),
                    child: Icon(Icons.check_circle, size: 20, color: ParentTheme.of(context).successColor),
                  ),
                  Text("Submitted",
                      style: Theme.of(context).textTheme.caption.apply(color: ParentTheme.of(context).successColor)),
                ],
              ),
            ],
          ),
        ),
        Divider(),
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: Text("Default Text Style"),
        ),
        Divider(),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text("Due", style: Theme.of(context).textTheme.overline),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Text("April 1 at 11:59pm", style: Theme.of(context).textTheme.subhead),
        ),
        Divider(),
        SwitchListTile(
          title: Text("SubHead"),
          subtitle: Text("Caption"),
          value: _allToggle,
          onChanged: (_) => _toggleAll(),
        ),
        SwitchListTile(
          title: Text("Switch (disabled)"),
          value: _allToggle,
          onChanged: null,
        ),
        CheckboxListTile(
          title: Text("Checkbox"),
          value: _allToggle,
          onChanged: (_) => _toggleAll(),
        ),
        CheckboxListTile(
          title: Text("Checkbox (disabled)"),
          value: _allToggle,
          onChanged: null,
        ),
        Divider(),
        Padding(
          padding: const EdgeInsets.only(left: 16, top: 16),
          child: Text("BIO 102", style: Theme.of(context).textTheme.overline),
        ),
        ListTile(
          title: Text("ListTile Title"),
          subtitle: Text("ListTile Subtitle"),
          leading: Icon(
            Icons.assignment,
            color: Theme.of(context).accentColor,
          ),
        ),
        Divider(),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            FlatButton(
              child: Text("Flat button"),
              textTheme: ButtonTextTheme.accent,
              onPressed: () {},
            ),
            RaisedButton(
              child: Text("Raised Button"),
              color: Theme.of(context).accentColor,
              colorBrightness: Brightness.dark,
              onPressed: () {},
            )
          ],
        ),
        Divider(),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            FlatButton(
              child: Text("Flat button (disabled)"),
              textTheme: ButtonTextTheme.accent,
              onPressed: null,
            ),
            RaisedButton(
              child: Text("Raised Button (disabled)"),
              color: Theme.of(context).accentColor,
              colorBrightness: Brightness.dark,
              onPressed: null,
            )
          ],
        ),
        Divider(),
      ],
    );
  }

  Widget _textStyles(BuildContext context) {
    var styles = getStyles(Theme.of(context).textTheme).entries.toList();
    return SingleChildScrollView(
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: DataTable(
          dataRowHeight: 64,
          columnSpacing: 24,
          horizontalMargin: 16,
          columns: [
            DataColumn(label: Text("Name / design name")),
            DataColumn(label: Text("Size")),
            DataColumn(label: Text("Weight")),
            DataColumn(label: Text("Color")),
            DataColumn(label: Text("Example")),
          ],
          rows: styles.map((entry) {
            var name = entry.key;
            var style = entry.value;
            return DataRow(cells: [
              DataCell(Text(name)),
              DataCell(Text(style.fontSize.toString())),
              DataCell(Text(style.fontWeight.toString().replaceFirst("FontWeight.w", ''))),
              DataCell(Row(
                children: <Widget>[
                  Container(
                    child: Container(
                      width: 20,
                      height: 20,
                      color: style.color,
                    ),
                    //color: bgColor,
                    padding: EdgeInsets.all(4),
                  ),
                  SizedBox(width: 6),
                  Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text("#" + style.color.value.toRadixString(16).substring(2).toUpperCase()),
                      Text((100 * style.color.opacity).toStringAsFixed(0) + "% opacity")
                    ],
                  )
                ],
              )),
              DataCell(
                Container(
                  child: Text("Sample", style: style),
                  padding: EdgeInsets.all(8),
                  //color: bgColor,
                ),
              ),
            ]);
          }).toList(),
        ),
      ),
    );
  }

  Widget _icons(BuildContext context) {
    return GridView.count(
        crossAxisCount: 3,
        children: List.generate(CanvasIcons.allIcons.length, (idx) {
          return Column(
            children: <Widget>[Icon(CanvasIcons.allIcons[idx][1]), Text(CanvasIcons.allIcons[idx][0])],
          );
        }));
  }
}
