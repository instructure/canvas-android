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

import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class ErrorReportInteractor {
  Future<void> submitErrorReport(
      String? subject, String description, String? email, ErrorReportSeverity? severity, String? stacktrace) async {
    final user = ApiPrefs.getUser();
    final domain = (ApiPrefs.getDomain()?.isNotEmpty == true) ? ApiPrefs.getDomain() : ErrorReportApi.DEFAULT_DOMAIN;
    final becomeUser = (user?.id.isNotEmpty == true) ? '$domain?become_user_id=${user?.id}' : '';
    final userEmail = (email?.isNotEmpty == true) ? email : user?.primaryEmail ?? '';

    final enrollments = user == null ? [] : await locator<EnrollmentsApi>().getSelfEnrollments(forceRefresh: true);
    final userRoles = enrollments == null ? '' : Set.from(enrollments.map((enrollment) => enrollment.type)).toList().join(',');

    return locator<ErrorReportApi>().submitErrorReport(
      subject: subject,
      description: description,
      email: userEmail,
      severity: _errorReportSeverityTag(severity),
      stacktrace: stacktrace,
      domain: domain,
      name: user?.name ?? '',
      becomeUser: becomeUser,
      userRoles: userRoles,
    );
  }

  String _errorReportSeverityTag(ErrorReportSeverity? severity) {
    switch (severity) {
      case ErrorReportSeverity.COMMENT:
        return 'just_a_comment';
      case ErrorReportSeverity.NOT_URGENT:
        return 'not_urgent';
      case ErrorReportSeverity.WORKAROUND_POSSIBLE:
        return 'workaround_possible';
      case ErrorReportSeverity.BLOCKING:
        return 'blocks_what_i_need_to_do';
      case ErrorReportSeverity.CRITICAL:
        return 'extreme_critical_emergency';
      default:
        throw ArgumentError('The provided severity is not supported: ${severity.toString()} not in ${ErrorReportSeverity.values.toString()}');
    }
  }
}

enum ErrorReportSeverity {
  COMMENT,
  NOT_URGENT,
  WORKAROUND_POSSIBLE,
  BLOCKING,
  CRITICAL,
}
