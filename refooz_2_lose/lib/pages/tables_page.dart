import 'dart:async';

import 'package:firebase_database/firebase_database.dart';
import 'package:firebase_database/ui/firebase_animated_list.dart';
import 'package:flutter/material.dart';
import 'package:refooz_2_lose/widgets/TableTile.dart';

class TablesPage extends StatefulWidget {
  final String userId;

  TablesPage(Key key, this.userId) : super(key: key);

  @override
  _TablesState createState() => new _TablesState();
}

class _TablesState extends State<TablesPage> {
  final FirebaseDatabase _database = FirebaseDatabase.instance;

  Future<Null> _requestTable(String tableId) async {
    _database.reference().child("incoming").child(tableId).update(
        {"tableRequestUserId": widget.userId, "tableRequestTime": DateTime.now().millisecondsSinceEpoch.toString()});
  }

  @override
  Widget build(BuildContext context) {
    return new SafeArea(
      top: false,
      bottom: false,
      child: new FirebaseAnimatedList(
        padding: EdgeInsets.all(12.0),
        query: _database.reference().child('tables'),
        defaultChild: new Center(
          child: new CircularProgressIndicator(),
        ),
        itemBuilder: (BuildContext context, DataSnapshot snapshot, Animation<double> animation, int index) {
          return new Padding(
            padding: new EdgeInsets.only(bottom: 8.0),
            child: new TableTile(
                snapshot: snapshot,
                onRequestTable: () {
                  _requestTable(snapshot.key);
                }),
          );
        },
      ),
    );
  }
}
