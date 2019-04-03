import 'package:flutter/material.dart';
import 'package:refooz_2_lose/ui/FoozTheme.dart' as Theme;

import 'package:firebase_database/firebase_database.dart';

class UserMessageWidget extends StatefulWidget {
  final String title;
  final DatabaseReference msgRef;

  const UserMessageWidget({this.title: '', this.msgRef});

  _UserMessageState createState() => new _UserMessageState();
}

class _UserMessageState extends State<UserMessageWidget> {
  String msg = '';

  @override
  Widget build(BuildContext context) {
    return new Container(
      padding:
          const EdgeInsets.only(left: 10.0, right: 10.0, bottom: 5.0, top: 5.0),
      child: new Card(
        child: new InkWell(
          onTap: () => _showDialog(),
          child: new Container(
            padding: const EdgeInsets.only(
                left: 14.0, right: 10.0, top: 10.0, bottom: 10.0),
            child: new Column(
              children: <Widget>[
                new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: <Widget>[
                      new Text(widget.title,
                          style: const TextStyle(
                            color: Theme.FoozColors.foozballRedDefault,
                            fontSize: 14.0,
                            fontWeight: FontWeight.w800,
                          )),
                      const Icon(
                        Icons.edit,
                        size: 20.0,
                        color: Color(0x44000000),
                      ),
                    ]),
                new Container(
                  padding: const EdgeInsets.only(top: 5.0),
                  child: new Align(
                      alignment: Alignment.topLeft,
                      child: new FutureBuilder<DataSnapshot>(
                          future: widget.msgRef.once(),
                          builder: (BuildContext context,
                              AsyncSnapshot<DataSnapshot> snapshot) {
                            switch (snapshot.connectionState) {
                              case ConnectionState.waiting:
                                msg = 'Loading...';
                                break;
                              default:
                                if (snapshot.hasError)
                                  msg = 'Error: ${snapshot.error}';
                                else if (snapshot.data.value != null) {
                                  msg = snapshot.data.value;
                                } else
                                  msg = '';
                            }
                            return new Text(
                              msg,
                              maxLines: 3,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 16.0),
                            );
                          })),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  _showDialog() async {
    TextEditingController controller = new TextEditingController(text: msg);
    await showDialog<String>(
      context: context,
      builder:  (builderContext) => new AlertDialog(
        contentPadding: const EdgeInsets.all(16.0),
        content: new Row(
          children: <Widget>[
            new Expanded(
              child: new TextField(
                controller: controller,
                maxLines: 3,
                autofocus: true,
                decoration: null,
              ),
            ),
          ],
        ),
        actions: <Widget>[
          new FlatButton(
              child: const Text('CANCEL'),
              onPressed: () {
                Navigator.pop(context);
              }),
          new FlatButton(
              child: const Text('SAVE'),
              onPressed: () {
                Navigator.pop(context);
                saveMessage(controller.text);
              })
        ],
      ),
    );
  }

  void saveMessage(String msg) {
    widget.msgRef
        .set(msg)
        .then((value) => this.setState(() => this.msg = msg))
        .catchError((error) => Scaffold.of(context).showSnackBar(new SnackBar(
              content: new Text('Couldn\'t update: $error'),
            )
    ));
  }
}
