import 'dart:async';

import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import '../widgets/user_header_widget.dart';
import '../widgets/user_message_widget.dart';

class UserPage extends StatefulWidget {
  String _userId;

  UserPage(Key key, this._userId) : super(key: key);

  @override
  _UserPageState createState() => new _UserPageState();
}

class _UserPageState extends State<UserPage> {
  String barcode;

  bool _loaded = false;
  FirebaseDatabase _database;

  @override
  void initState() {
    super.initState();
    _initDatabase();
  }

  Future<Null> _initDatabase() async {
    _database = FirebaseDatabase.instance;
    _database.setPersistenceEnabled(true);
    _database.setPersistenceCacheSizeBytes(10000000);

    _database
        .reference()
        .child("users")
        .child(widget._userId)
        .once()
        .then((DataSnapshot snapshot) {
      print('Connected to database and read user ${snapshot.value}');
    });
    _loaded = true;
    setState(() {
      _loaded;
    });
  }

  @override
  Widget build(BuildContext context) {
    return !_loaded
        ? new Center(
            child: new CircularProgressIndicator(),
          )
        : new SafeArea(
            top: false,
            bottom: false,
            child: new ListView(
              children: <Widget>[
                new UserHeaderWidget(
                  userRef: _database.reference().child("users").child(widget._userId),
                ),
                new UserMessageWidget(
                  title: 'Welcome Message',
                  msgRef: _database.reference().child("users").child(widget._userId).child('customAssignmentPhrase'),
                ),
                new UserMessageWidget(
                  title: 'Victory Message',
                  msgRef: _database.reference().child("users").child(widget._userId).child('customVictoryPhrase'),
                ),
              ],
            ),
          );
  }
}
