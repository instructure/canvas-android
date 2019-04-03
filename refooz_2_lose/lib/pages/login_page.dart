import 'package:google_sign_in/google_sign_in.dart';
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:refooz_2_lose/main.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:refooz_2_lose/ui/FoozTheme.dart' as Theme;
import 'package:firebase_database/firebase_database.dart';


final _googleSignIn = new GoogleSignIn();
final _auth = FirebaseAuth.instance;

class LoginPage extends StatefulWidget {

  LoginPage() : super();

  @override
  _LoginState createState() => new _LoginState();
}

class _LoginState extends State<LoginPage> {
  FirebaseDatabase _database;

  Future<Null> _initDatabase() async {
    _database = FirebaseDatabase.instance;
    _database.setPersistenceEnabled(true);
    _database.setPersistenceCacheSizeBytes(10000000);
  }

  Future<Null> _ensureLoggedIn() async {
    final GoogleSignInAccount googleUser = await _googleSignIn.signIn();
    final GoogleSignInAuthentication googleAuth =
    await googleUser.authentication;
    final FirebaseUser user = await _auth.signInWithGoogle(
      accessToken: googleAuth.accessToken,
      idToken: googleAuth.idToken,
    );
    assert(user.email != null);
    assert(user.displayName != null);
    assert(!user.isAnonymous);

    final FirebaseUser currentUser = await _auth.currentUser();
    assert(user.uid == currentUser.uid);

    await _initDatabase();
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
          _database.reference().child("users").child(googleUser.id).set(
              {"email": user.email,
                "name": user.displayName,
                "avatar": user.photoUrl,
                "id": googleUser.id,
                "wins": 0,
                "losses": 0}
          );
          Navigator.pushAndRemoveUntil(context, new MaterialPageRoute(
              builder: (_) => new RootPage("Foosball")), (Route<dynamic> route) => false,
          );

        } else {
          print(event.snapshot.value);
          var id = event.snapshot.value[googleUser.id]["id"];
          print(id);
          Map<dynamic, dynamic> values = event.snapshot.value;
          print(values.keys.first.toString());

          // User exists, take them to the app
          Navigator.pushAndRemoveUntil(context, new MaterialPageRoute(
              builder: (_) => new RootPage("Foosball")), (Route<dynamic> route) => false,
          );
        }
    });




  }
  
  

  @override
  Widget build(BuildContext context) {
    return new Material(
      color: Theme.FoozColors.primaryColor,
      child: new InkWell(
        onTap: _ensureLoggedIn,
        child: new Center(
            child: new Text("Tap to sign in", style: new TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 30.0))
        )
      ),
    );
  }
}
