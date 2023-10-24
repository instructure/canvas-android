// Copyright (C) 2019 - present Instructure, Inc.
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

// Sourced from https://github.com/flutter/flutter/blob/1ce4a4f36fc063ec78af56ea8df06e15e80c1dc8/dev/manual_tests/test/mock_image_http.dart

import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:transparent_image/transparent_image.dart';

import 'test_helpers/mock_helpers.dart';
import 'test_helpers/mock_helpers.mocks.dart';

void mockNetworkImageResponse() {
  final TestWidgetsFlutterBinding binding = TestWidgetsFlutterBinding.ensureInitialized();
  binding.runAsync(() async {
    HttpOverrides.global = _ImageHttpOverrides();
  });
  ;
}

class _ImageHttpOverrides extends HttpOverrides {
  @override
  HttpClient createHttpClient(SecurityContext? _) {
    final client = MockHttpClient();
    final request = MockHttpClientRequest();
    final response = MockHttpClientResponse();
    final headers = MockHttpHeaders();

    when(client.getUrl(any)).thenAnswer((_) => Future<HttpClientRequest>.value(request));
    when(request.headers).thenReturn(headers);
    when(request.close()).thenAnswer((_) => Future<HttpClientResponse>.value(response));
    when(response.contentLength).thenReturn(kTransparentImage.length);
    when(response.statusCode).thenReturn(HttpStatus.ok);
    when(response.compressionState).thenReturn(HttpClientResponseCompressionState.compressed);
    when(response.listen(any)).thenAnswer((Invocation invocation) {
      final void Function(List<int>) onData = invocation.positionalArguments[0];
      final void Function() onDone = invocation.namedArguments[#onDone];
      final void Function(Object, [StackTrace]) onError = invocation.namedArguments[#onError];
      final bool cancelOnError = invocation.namedArguments[#cancelOnError];

      return Stream<List<int>>.fromIterable(<List<int>>[kTransparentImage])
          .listen(onData, onDone: onDone, onError: onError, cancelOnError: cancelOnError);
    });
    return client;
  }
}
