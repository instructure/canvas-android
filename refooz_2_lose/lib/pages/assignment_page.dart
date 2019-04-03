import 'package:flutter/material.dart';
import 'dart:async';
import 'package:firebase_database/firebase_database.dart';
import 'package:refooz_2_lose/models/FoozTable.dart';
import 'package:refooz_2_lose/ui/ColorParser.dart';

class AssignmentPage extends StatefulWidget {
  final String _signInUri;
  final String _userId;

  AssignmentPage(Key key, this._signInUri, this._userId) : super(key: key);

  @override
  _AssignmentPageState createState() => new _AssignmentPageState();
}

class _AssignmentPageState extends State<AssignmentPage> {
  _AssignmentPageState();

  FoozTable _table;
  FirebaseDatabase _database;
  DatabaseReference _tablesRef;
  DatabaseReference _incomingRef;
  bool _loaded = false;
  bool _belongsToSideOne = true;

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
      assignTeam();
    });

    _tablesRef = _database.reference().child('tables');
    _incomingRef = _database.reference().child('incoming');
    _loaded = true;
    setState(() => _loaded);
  }

  @override
  Widget build(BuildContext context) {
    var tableColor = _table == null ? Colors.white : ColorParser.fromHex(_belongsToSideOne ? _table.sideOneColor : _table.sideTwoColor);
    var sideName = _table == null ? "" : (_belongsToSideOne ? _table.sideOneName : _table.sideTwoName);
    var tableName = _table == null ? "" : _table.name;

    _exitAfterDelay();
    return new Container(
        color: tableColor,
          child: new Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              new Container(
                  padding: new EdgeInsets.symmetric(vertical: 48.0),
                  child: new Icon(Icons.check_circle,
                      color: Colors.white, size: 80.0)),
              new Container(
                color: tableColor,
                padding: new EdgeInsets.symmetric(vertical: 48.0, horizontal: 30.0),
                child: new Column(
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    new Text(tableName, style: new TextStyle(color: Colors.white, fontSize: 16.0)),
                    new Container(
                      margin: new EdgeInsets.only(top: 20.0),
                      child: new Text("ASSIGNED TO",
                          style: new TextStyle(
                              color: Color(0x80FFFFFF),
                              fontWeight: FontWeight.bold,
                              fontSize: 13.0)),
                    ),
                    new Text(sideName, style: new TextStyle(color: Colors.white, fontSize: 30.0)),
                  ],
                ),
              )
            ],
        )
    );
  }

  void assignTeam() {
    var uri = Uri.parse(widget._signInUri);
    var segments = uri.pathSegments;

    var tableId = segments.length > 0 ? segments[0] : "0";
    var side = segments.length > 1 ? segments[1] : "0";

    print("URI: " + uri.toString());

    _tablesRef
        .child(tableId)
        .onValue
        .listen((Event event) => _onTableDataRetrieved(event, tableId, side));
  }

  void _onTableDataRetrieved(Event event, String tableId, String side) {
    FoozTable table = FoozTable.fromSnapshot(event.snapshot);
    _incomingRef
        .child(tableId)
        .child(side == "0" ? "sideOne" : "sideTwo")
        .set(widget._userId);
    setState(() {
      _belongsToSideOne = side == "0" ? true : false;
      _table = table;
    });
  }

  void _exitAfterDelay() {
    new Timer(Duration(milliseconds: 3000), () => Navigator.pop(context));
  }
}
