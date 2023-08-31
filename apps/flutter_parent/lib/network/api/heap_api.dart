// Copyright (C) 2023 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:convert';

import 'package:encrypt/encrypt.dart' as encrypt;
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/private_consts.dart';

class HeapApi {
  Future<bool> track(String event, {Map<String, dynamic> extras = const {}}) async {
    final heapDio = DioConfig.heap();
    final currentLogin = ApiPrefs.getCurrentLogin();
    if (currentLogin == null) return false;

    final userId = ApiPrefs.getCurrentLogin()?.user.id;
    if (userId == null) return false;

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

    var dio = heapDio.dio;
    final response = await dio.post('/track', data: data);
    return response.statusCode == 200;
  }
}