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
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_screen.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

/// A simple class to handle wrapping embedded html content. Displays a description title with a student colored
/// button to view the html in a full screen web view. This is done to avoid creating embedded web views that are
/// too large and crash devices. By using a full screen web view [HtmlDescriptionScreen] we can let the webview handle
/// its content and scrolling.
///
/// Handles showing an empty state if there is no html content (null or empty), provided that emptyDescription is also
/// not null or empty.
class HtmlDescriptionTile extends StatelessWidget {
  /// Html passed to a full screen web view
  final String? html;

  /// Only used if an emptyDescription is not null or not empty.
  /// Defaults to AppLocalizations.descriptionTitle
  final String? descriptionTitle;

  /// Only used if html is not null and not empty.
  /// Defaults to AppLocalizations.viewDescription
  final String? buttonLabel;

  /// If null or empty, this will render an empty container and nothing else.
  final String? emptyDescription;

  const HtmlDescriptionTile({
    required this.html,
    this.descriptionTitle,
    this.buttonLabel,
    this.emptyDescription,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    if (html == null || html?.isEmpty == true) {
      return _buildEmptyState(context);
    }

    return InkWell(
      onTap: () => _launchSimpleWebView(context),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 16),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                _title(context),
                Text(
                  buttonLabel ?? L10n(context).viewDescription,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(color: ParentTheme.of(context)?.studentColor),
                ),
              ],
            ),
            Icon(Icons.arrow_forward, color: ParentColors.tiara),
          ],
        ),
      ),
    );
  }

  Widget _title(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(descriptionTitle ?? L10n(context).descriptionTitle, style: Theme.of(context).textTheme.labelSmall),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    // No empty text, so just be empty
    if (emptyDescription == null || emptyDescription!.isEmpty) return Container();

    final parentTheme = ParentTheme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _title(context),
          Container(
            width: double.infinity,
            height: 72,
            padding: EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: parentTheme?.nearSurfaceColor,
              borderRadius: BorderRadius.circular(4),
            ),
            child: Center(
              child: Text(
                emptyDescription!,
                style: Theme.of(context).textTheme.titleSmall?.copyWith(color: parentTheme?.onSurfaceColor),
              ),
            ),
          ),
        ],
      ),
    );
  }

  _launchSimpleWebView(BuildContext context) {
    // We don't want/need a route for this screen, as passing around large amounts of html may break Flutter routing or
    // may have personally identifiable information (and shouldn't be logged by the screen parameter analytics).
    // We'll at least get a screen view for that screen name, and a log entry, so we should still be able to track
    // any issues that come from navigating to this screen.
    locator<QuickNav>().push(context, HtmlDescriptionScreen(html!, L10n(context).descriptionTitle));
  }
}
