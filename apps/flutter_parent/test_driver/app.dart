import 'dart:convert';

import 'package:flutter_driver/driver_extension.dart';
import 'package:flutter_parent/main.dart' as app;
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/api/dataseeding/user_seed_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

void main() {
  // This line enables the extension.
  enableFlutterDriverExtension(handler: (String message)  async{
    if(message == "SeedUser") {
      var user = await UserSeedApi.createUser();
      ApiPrefs.switchLogins(user.toLogin()); // temporary
      return json.encode(serialize(user));
    }
  });

  // Call the `main()` function of the app, or call `runApp` with
  // any widget you are interested in testing.
  app.main();
}
