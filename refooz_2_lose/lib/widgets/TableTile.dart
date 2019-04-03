import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:meta/meta.dart';
import 'package:refooz_2_lose/ui/FoozTheme.dart';
import 'package:refooz_2_lose/ui/ColorParser.dart';

class TableTile extends StatelessWidget {
  const TableTile({this.snapshot, Key key, @required this.onRequestTable}) : super(key: key);

  final DataSnapshot snapshot;

  final VoidCallback onRequestTable;

  @override
  Widget build(BuildContext context) {
    String currentGame = snapshot.value["currentGame"] ?? 'FREE';
    String currentBestOf = snapshot.value["currentBestOf"] ?? '';
    bool isFree = currentGame == 'FREE';
    String statusLabel;
    switch (currentGame) {
      case 'TABLE_KING':
        statusLabel = "King of the Table";
        break;
      case 'TEAM_TWISTER':
        statusLabel = "Team Twister";
        break;
      case 'BUSY':
        statusLabel = "Game in progress";
        break;
      case 'FREE':
      default:
        statusLabel = "FREE";
        break;
    }

    return new Card(
      child: new Padding(
        padding: new EdgeInsets.all(16.0),
        child: new Column(
          children: <Widget>[
            new Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                new Row(
                  children: <Widget>[
                    new Container(
                        decoration: new BoxDecoration(color: ColorParser.fromHex(snapshot.value["sideOneColor"])),
                        width: 6.5,
                        height: 16.0
                    ),
                    new Padding(
                      padding: EdgeInsets.only(left: 1.25, right: 1.25),
                      child: new Container(
                          decoration: new BoxDecoration(color: ColorParser.fromHex("888")),
                          width: 0.5,
                          height: 20.0
                      ),
                    ),
                    new Container(
                        decoration: new BoxDecoration(color: ColorParser.fromHex(snapshot.value["sideTwoColor"])),
                        width: 6.5,
                        height: 16.0
                    ),
                    new Padding(
                      padding: EdgeInsets.only(left: 8.0),
                      child: new Text(
                        "${snapshot.value["name"]}",
                        style: new TextStyle(
                          color: FoozThemeData.accentColor,
                          fontSize: 18.0,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
                new Row(crossAxisAlignment: CrossAxisAlignment.center, children: <Widget>[
                  new Padding(
                    padding: EdgeInsets.only(top: 0.7),
                    child: new Text(
                      "STATUS:",
                      style: new TextStyle(color: FoozThemeData.hintColor, fontSize: 12.0, fontWeight: FontWeight.w300),
                    ),
                  ),
                  new Padding(
                    padding: EdgeInsets.only(left: 2.0),
                    child: new Text(statusLabel,
                        style: new TextStyle(
                            color: isFree ? FoozColors.foozballBlueDefault : FoozThemeData.accentColor,
                            fontWeight: FontWeight.w700)),
                  )
                ]),
              ],
            ),
            currentBestOf.length == 0 ? new Container() : makeGameWidget(snapshot.value, context)
          ],
        ),
      ),
    );
  }

  Widget makeGameWidget(dynamic data, BuildContext context) {
    var teamOne = data["teamOne"]["users"];
    var teamTwo = data["teamTwo"]["users"];
    List<Widget> teamOneAvatars = [makeUserWidget(teamOne[0]), new Container(width: 4.0), makeUserWidget(teamOne[1])];
    List<Widget> teamTwoAvatars = [makeUserWidget(teamTwo[0]), new Container(width: 4.0), makeUserWidget(teamTwo[1])];

    return new Column(
      children: <Widget>[
        Divider(),
        new Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: <Widget>[
            new Text(data["teamOne"]["teamName"] ?? teamOne[0]["name"] + " & " + teamOne[1]["name"]),
            new Text("vs"),
            new Text(data["teamTwo"]["teamName"] ?? teamTwo[0]["name"] + " & " + teamTwo[1]["name"]),
          ],
        ),
        new Container(
          height: 8.0,
        ),
        new Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            new Row(children: teamOneAvatars),
            new Row(
              children: <Widget>[
                new Text("SCORE:"),
                new Padding(
                  padding: EdgeInsets.only(left: 8.0, right: 8.0),
                  child: new Text(
                    data["currentScoreTeamOne"],
                    style: new TextStyle(
                        fontSize: 24.0, fontWeight: FontWeight.w700, color: FoozColors.foozballRedDefault),
                  ),
                ),
                new Text("TO"),
                new Padding(
                  padding: EdgeInsets.only(left: 8.0, right: 8.0),
                  child: new Text(
                    data["currentScoreTeamTwo"],
                    style: new TextStyle(
                        fontSize: 24.0, fontWeight: FontWeight.w700, color: FoozColors.foozballRedDefault),
                  ),
                )
              ],
            ),
            new Row(children: teamTwoAvatars),
          ],
        ),
        new Divider(),
        new Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: <Widget>[
            new Row(
              children: <Widget>[
                new Text("ROUND"),
                new Padding(
                  padding: EdgeInsets.only(left: 8.0, right: 8.0),
                  child: new Text(
                    data["currentRound"],
                    style: new TextStyle(
                        fontSize: 24.0, fontWeight: FontWeight.w700, color: FoozColors.foozballRedDefault),
                  ),
                ),
                new Text("OF"),
                new Padding(
                  padding: EdgeInsets.only(left: 8.0, right: 8.0),
                  child: new Text(
                    data["currentBestOf"],
                    style: new TextStyle(
                        fontSize: 24.0, fontWeight: FontWeight.w700, color: FoozColors.foozballRedDefault),
                  ),
                ),
              ],
            ),
            new Row(
              children: <Widget>[
                new Padding(
                  padding: EdgeInsets.only(left: 8.0, right: 8.0),
                  child: new Text(
                    data["currentPointsToWin"],
                    style: new TextStyle(
                        fontSize: 24.0, fontWeight: FontWeight.w700, color: FoozColors.foozballRedDefault),
                  ),
                ),
                new Text("POINTS TO WIN"),
              ],
            ),
          ],
        ),
        new Divider(),
        new MaterialButton(
          child: new Text(
            "Request Table",
            style: new TextStyle(color: Colors.blue),
          ),
          onPressed: () {
            onRequestTable();
            Scaffold.of(context).showSnackBar(new SnackBar(content: new Text("Table Requested!")));
          },
        ),
      ],
    );
  }

  Widget makeUserWidget(dynamic data) {
    if (data == null) return new Container();
    String url = data["avatar"];
    String name = data["name"];
    String initials = name.split(" ").take(3).map((String s) => s[0]).join();
    if (url == null || url.isEmpty) {
      return new CircleAvatar(
        backgroundColor: FoozThemeData.primaryColor,
        child: new Text(
          initials,
          style: new TextStyle(color: Colors.white),
        ),
      );
    } else {
      return new CircleAvatar(backgroundImage: NetworkImage(url));
    }
  }
}
