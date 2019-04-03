import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';

import '../ui/FoozTheme.dart' as Theme;
import 'package:flutter/widgets.dart';

class UserHeaderWidget extends StatefulWidget {
  final DatabaseReference userRef;

  const UserHeaderWidget({Key key, this.userRef}) : super(key: key);

  _UserHeaderWidgetState createState() => _UserHeaderWidgetState();
}

class _UserHeaderWidgetState extends State<UserHeaderWidget> {
  @override
  Widget build(BuildContext context) {
    return new Container(
        padding: new EdgeInsets.all(30.0),
        child: new Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            new Flexible(
                flex: 1,
                child: new Padding(
                    padding: EdgeInsets.only(top: 30.0),
                    child: new Column(children: [
                      new Text(
                        "Wins",
                        style: new TextStyle(
                          fontSize: 25.0,
                          fontWeight: FontWeight.bold,
                          color: Theme.FoozColors.primaryColor[900],
                        ),
                      ),
                      new SizedBox(
                        height: 10.0,
                        child: new Center(
                          child: new Container(
                            height: 0.0,
                            decoration: new BoxDecoration(
                              border: new Border(
                                bottom: new BorderSide(
                                  color: Theme.FoozColors.accentColor[900],
                                  width: 3.0,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                      new FutureBuilder<DataSnapshot>(
                          future: widget.userRef.child("wins").once(),
                          builder: ((BuildContext context, AsyncSnapshot<DataSnapshot> snapshot) {
                            if (!snapshot.hasData) return new Center(child: new Text('Loading...'));
                            return new Text(snapshot.data.value != null ? snapshot.data.value.toString() : "0", style: new TextStyle(fontSize: 18.0, color: Theme.FoozColors.primaryColor[900]));
                          }))
                    ]))),
            new Flexible(
              flex: 1,
              child: new Column(children: [
                new FutureBuilder<DataSnapshot>(
                    future: widget.userRef.child("avatar").once(),
                    builder: ((BuildContext context, AsyncSnapshot<DataSnapshot> snapshot) {
                      if (!snapshot.hasData) return new Center(child: new Text('Loading...'));
                      return new Container(
                          child: new CircleAvatar(
                            backgroundColor: Theme.FoozColors.primaryColor,
                            backgroundImage: new NetworkImage(snapshot.data.value.toString())
                          ),
                          width: 90.0,
                          height: 90.0,
                          padding: const EdgeInsets.all(3.0),
                          decoration: new BoxDecoration(
                            color: Theme.FoozColors.accentColor[900], // border color
                            shape: BoxShape.circle,
                          )
                      );
                    }))
              ]),
            ),
            new Flexible(
                flex: 1,
                child: new Padding(
                  padding: EdgeInsets.only(top: 30.0),
                  child: new Column(children: [
                    new Text(
                      "Losses",
                      style: new TextStyle(
                        fontSize: 25.0,
                        fontWeight: FontWeight.bold,
                        color: Theme.FoozColors.primaryColor[900],
                      ),
                    ),
                    new SizedBox(
                      height: 10.0,
                      child: new Center(
                        child: new Container(
                          height: 0.0,
                          decoration: new BoxDecoration(
                            border: new Border(
                              bottom: new BorderSide(
                                color: Theme.FoozColors.accentColor[900],
                                width: 3.0,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                    new FutureBuilder<DataSnapshot>(
                        future: widget.userRef.child("losses").once(),
                        builder: ((BuildContext context, AsyncSnapshot<DataSnapshot> snapshot) {
                          if (!snapshot.hasData) return new Center(child: new Text('Loading...'));
                          return new Text(
                            snapshot.data.value != null ? snapshot.data.value.toString() : "0",
                            style: new TextStyle(
                              fontSize: 18.0,
                              color: Theme.FoozColors.primaryColor[900],
                            ),
                          );
                        }))
                  ]),
                ))
          ],
        ));
  }
}
