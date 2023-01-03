import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:encrypt/encrypt.dart' as encrypt;
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';
import 'package:flutter_parent/network/utils/private_consts.dart';

class HeapApi {
  Future<Response> track(String event, {Map<String, dynamic> extras = const {}}) {
    final heapDio = DioConfig.heap();
    final userId = ApiPrefs.getCurrentLogin().user.id;

    final encrypter = encrypt.Encrypter(encrypt.AES(encrypt.Key.fromUtf8(ENCRYPT_KEY)));
    final encryptedId = encrypter.encrypt(userId, iv: encrypt.IV.fromUtf8(ENCRYPT_IV)).base64;

    var data = {
      'app_id' : HEAP_PRODUCTION_ID,
      'identity' : encryptedId,
      'event' : event
    };

    if (extras.isNotEmpty) {
      data['properties'] = json.encode(extras);
    }

    return fetch(heapDio.dio.post('/track', data: data));
  }
}