import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:refooz_2_lose/pages/tables_page.dart';
import 'package:refooz_2_lose/pages/user_page.dart';
import 'package:refooz_2_lose/pages/login_page.dart';
import 'package:refooz_2_lose/ui/FoozTheme.dart' as Theme;
import 'package:google_sign_in/google_sign_in.dart';
import 'dart:async';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:refooz_2_lose/pages/assignment_page.dart';
import 'package:firebase_database/firebase_database.dart';


final _googleSignIn = new GoogleSignIn();
final _auth = FirebaseAuth.instance;
FirebaseDatabase _database;

void main() async {
  await _ensureLoggedIn();
  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      title: 'Mobile Foosball',
      color: Theme.FoozColors.primaryColor[500],
      theme: Theme.FoozThemeData,
      home: _checkLogin() ? new RootPage("Foosball") : new LoginPage(),
    );
  }
}

class RootPage extends StatefulWidget {
  final String title;

  RootPage(this.title) : super();

  @override
  _RootState createState() => new _RootState();
}

class _RootState extends State<RootPage> {
  final _scaffoldKey = new GlobalKey<ScaffoldState>();

  static const nfcChannel =
      const MethodChannel('com.instructure.refooz2lose/nfc');

  bool cameraState = false;
  String userName = "temp";
  GoogleSignInAccount _currentUser;
  String _userId;
  String _userName;
  MenuItem _menuItemSelected = menuItems[0]; // The app's "state".

  void _menuItemSelect(MenuItem item) async {
    if (item.id == MenuItem.LOGOUT) {
      _logout(context);
    }
    setState(() => _menuItemSelected = item);
  }

  @override
  void initState() {
    super.initState();
    _initDatabase();
    _googleSignIn.onCurrentUserChanged.listen((GoogleSignInAccount account) {

      setState(() {
        _currentUser = account;
        _userId = _currentUser.id;
        _checkUser(account);
      });
    });
    _googleSignIn.signInSilently();

    if(_googleSignIn.currentUser != null) {
      _currentUser = _googleSignIn.currentUser;
      _userId = _currentUser.id;
      _checkUser(_currentUser);
    }
    getNfcInfo();
  }

  void _checkUser(GoogleSignInAccount user) {
    // Check the database for the user and add them if they're not in it
    _database.reference().
    child("users").
    orderByChild("email").
    equalTo(user.email).
    limitToFirst(1).
    onValue.
    listen((Event event) {
      if(event.snapshot.value == null) {
        //no user matches this. create one.
        print("No user");
      } else {
        Map<dynamic, dynamic> values = event.snapshot.value;
        setState(() {
          // Use the values from the database, not the google info
          _userId = values.keys.first.toString();
          _userName = event.snapshot.value[_userId]["name"];
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return new DefaultTabController(
      length: 2,
      child: new Scaffold(
          key: _scaffoldKey,
          appBar: new AppBar(
            title: new Text(widget.title),
            actions: <Widget>[
              new PopupMenuButton<MenuItem>(
                // overflow menu
                onSelected: _menuItemSelect,
                itemBuilder: (BuildContext context) {
                  return menuItems.map((MenuItem choice) {
                    return new PopupMenuItem<MenuItem>(value: choice, child: new Text(choice.title));
                  }).toList();
                },
              ),
            ],
            bottom: new TabBar(
              tabs: [
                new Tab(text: (_userName != null) ? _userName : "Profile"),
                new Tab(text: "Tables")
              ],
            ),
          ),
        body: new TabBarView(
          children: [
            new UserPage(widget.key, (_userId != null) ? _userId : ""),
            new TablesPage(widget.key, (_userId != null) ? _userId : "")
          ],
        ),
          floatingActionButton: new FloatingActionButton(
              onPressed: () => getQRCodeInfo()
          , child: new Icon(Icons.flip))
      ),
    );
  }

  void assignTeamFlow(String uri) {
    var dialog = new AlertDialog(
      content: new AssignmentPage(widget.key, uri, _currentUser.id),
      contentPadding: new EdgeInsets.all(0.0),
    );

    showDialog(context: this.context, builder: (_) => dialog);
  }

  getNfcInfo() async {
    var uri = await nfcChannel.invokeMethod("getTableAssignmentUri");
    if (uri != null) assignTeamFlow(uri);
  }

  getQRCodeInfo() async {
    try {
      String uri = (await BarcodeScanner.scan()).toString();
      if(uri != null) new Timer(Duration(milliseconds: 300), () => assignTeamFlow(uri));
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.cameraAccessDenied) {
        print('The user did not grant the camera permission!');
      } else {
        print('Unknown error: $e');
      }
    } on FormatException {
      print('null (User returned using the "back"-button before scanning anything. Result)');
    } catch (e) {
      print('Unknown error: $e');
    }
  }
}

Future<Null> _logout(BuildContext context) async {
  // Log out and go back to the login page
  await _googleSignIn.signOut();
  await _auth.signOut();

  Navigator.pushAndRemoveUntil(context, new MaterialPageRoute(builder: (_) => new LoginPage()), (Route<dynamic> route) => false);
}

bool _checkLogin() {
  GoogleSignInAccount user = _googleSignIn.currentUser;
  if(user != null) { print(user.displayName + " " + user.photoUrl); }

  return !(user == null);
}


Future<Null> _initDatabase() async {
  _database = FirebaseDatabase.instance;
  _database.setPersistenceEnabled(true);
  _database.setPersistenceCacheSizeBytes(10000000);
}

// Try to log the user in right away
Future<Null> _ensureLoggedIn() async {
  final GoogleSignInAccount googleUser = await _googleSignIn.signInSilently();
  if (googleUser == null) return;
  print(googleUser.displayName);
  // otherwise, log them the rest of the way in
  final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
  final FirebaseUser user = await _auth.signInWithGoogle(
    accessToken: googleAuth.accessToken,
    idToken: googleAuth.idToken,
  );
  assert(user.email != null);
  assert(user.displayName != null);
  assert(!user.isAnonymous);

  final FirebaseUser currentUser = await _auth.currentUser();
  assert(user.uid == currentUser.uid);

  //init the database here, we'll check it later
  await _initDatabase();

}

class MenuItem {
  const MenuItem({this.id, this.title, this.icon});

  static const int LOGOUT = 0;

  final int id;
  final String title;
  final IconData icon;
}

const List<MenuItem> menuItems = const <MenuItem>[
  const MenuItem(id: MenuItem.LOGOUT, title: 'Logout', icon: Icons.exit_to_app)
];
