// Copyright (C) 2020 - present Instructure, Inc.
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

import 'package:flutter_parent/network/utils/dio_config.dart';

class ErrorReportApi {
  // static const DEFAULT_DOMAIN = 'https://canvas.instructure.com';
  static const DEFAULT_DOMAIN = 'https://canvas-test.emeritus.org';

  Future<void> submitErrorReport({
    String? subject,
    String? description,
    String? email,
    String? severity,
    String? stacktrace,
    String? domain,
    String? name,
    String? becomeUser,
    String? userRoles,
  }) async {
    var config = domain == DEFAULT_DOMAIN ? DioConfig.core() : DioConfig.canvas();

    var dio = config.dio;

    await dio.post(
      '/error_reports.json',
      queryParameters: {
        'error[subject]': subject,
        'error[url]': domain,
        'error[email]': email,
        'error[comments]': description,
        'error[user_perceived_severity]': severity,
        'error[name]': name,
        'error[user_roles]': userRoles,
        'error[become_user]': becomeUser,
        if (stacktrace != null) 'error[backtrace]': stacktrace,
      },
    );
  }
}
