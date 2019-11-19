import 'package:flutter_parent/api/auth_api.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';

class WebLoginInteractor {
  Future<MobileVerifyResult> mobileVerify(String domain) {
    return AuthApi.mobileVerify(domain);
  }

  Future performLogin(MobileVerifyResult result, String oAuthRequest) async {
    final tokens = await AuthApi.getTokens(result, oAuthRequest);

    ApiPrefs.updateLoginInfo(tokens, result);
  }
}
