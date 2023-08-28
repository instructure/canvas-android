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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';

/// Typedef for the function callback to get the semantics string.
/// e.x., semantics: (context, count) => L10n(context).unreadCount(count)
///
/// See Also for more details:
/// * [NumberBadge]
/// * [IndicatorBadge]
typedef String GetSemantics(BuildContext context, int? count);

/// A simple class to wrap options for [NumberBadge] and [WidgetBadge]
class BadgeOptions {
  /// The initial count to show for the badge
  final int? count;

  /// The max count a badge can show, for counts greater than this the string shown will be '$maxCount+'
  final int? maxCount;

  /// True if the badge should include a border
  final bool includeBorder;

  /// True if the badge colors should be changed to look better against the primary color background
  final bool onPrimarySurface;

  const BadgeOptions({this.count, this.maxCount = 99, this.includeBorder = false, this.onPrimarySurface = false});
}

/// Adds a badge to a widget. If a count or a listenable is provided, a circle with the count is used as the badge.
/// Otherwise an simple circle will be added as an indicator.
///
/// Positioning of Indicator or Number badge is different, with indicators being in the top left, and numbers being on
/// the right side. If more custom cases are needed, this may need to be modified.
///
/// See Also:
///
/// * [NumberBadge] for when a count or listenable is provided
/// * [IndicatorBadge] for when a simple circle is all that is needed
class WidgetBadge extends StatelessWidget {
  final Widget icon;
  final BadgeOptions? options;
  final GetSemantics? semantics;
  final ValueListenable? countListenable;

  const WidgetBadge(this.icon, {this.options = const BadgeOptions(), this.semantics, this.countListenable, super.key});

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      children: <Widget>[
        icon,
        _badge(),
      ],
    );
  }

  Widget _badge() {
    // If no badge count and no listenable are provided, we just want an indicator on the icon, not a badge with a count
    if (options?.count == null && countListenable == null) {
      return PositionedDirectional(start: 8, top: 8, child: IndicatorBadge(semantics: semantics));
    }
    return PositionedDirectional(
      end: -10,
      top: -10,
      child: NumberBadge(options: options!, semantics: semantics, listenable: countListenable),
    );
  }
}

/// A badge with a number in it. If the count is zero or less then no badge is shown. A listenable can be provided so
/// that the badge count will update automatically. If maxCount is provided (defaults to 99) then the count will be
/// capped at that number with a "+" appended.
/// Defaults semantics to [AppLocalizations.unreadCount] if not provided, can be overridden to return null so no
/// semantics label is added (which then just reads the count provided)
class NumberBadge extends StatelessWidget {
  static final backgroundKey = const ValueKey('backgroundKey');
  static final borderKey = const ValueKey('borderKey');

  final BadgeOptions options;
  final GetSemantics? semantics;
  final ValueListenable? listenable;

  const NumberBadge({this.options = const BadgeOptions(), this.semantics, this.listenable, super.key});

  @override
  Widget build(BuildContext context) {
    if (listenable == null) return _badge(context, options.count);
    return ValueListenableBuilder(
      valueListenable: listenable!,
      builder: (context, count, _) => _badge(context, count as int?),
    );
  }

  Widget _badge(BuildContext context, int? count) {
    // If there's no count, then don't show anything
    if (count == null || count <= 0) return SizedBox();

    final maxCount = options.maxCount;
    final accentColor = (ParentTheme.of(context)?.isDarkMode == true ? Colors.black : Theme.of(context).colorScheme.secondary);

    // Wrap in another container to get the border around the badge, since using border for circles in a box decoration
    // has antialiasing issues.
    return Container(
      key: borderKey,
      padding: EdgeInsets.all(options.includeBorder ? 2.0 : 0.0),
      decoration: BoxDecoration(
        color: options.onPrimarySurface ? accentColor : Theme.of(context).scaffoldBackgroundColor,
        shape: BoxShape.circle,
      ),
      child: Container(
        key: backgroundKey,
        decoration: _badgeDecoration(context, options),
        child: Padding(
          padding: const EdgeInsets.all(6.0),
          child: Text(
            maxCount != null && count > maxCount ? L10n(context).badgeNumberPlus(maxCount) : '$count',
            semanticsLabel: semantics != null ? semantics!(context, count) : L10n(context).unreadCount(count),
            style: TextStyle(
              fontSize: 10,
              color: options.onPrimarySurface ? accentColor : Theme.of(context).scaffoldBackgroundColor,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}

/// An empty colored circle, used as an indicator badge. Typically signals that an item is unread, which doesn't need a
/// count like the NumberBadge.
/// Defaults semantics to [AppLocalizations.unread] if not provided, can be overridden to return null so no semantics
/// label is added. Never provides a value for 'count' in the semantics function.
class IndicatorBadge extends StatelessWidget {
  final GetSemantics? semantics;

  const IndicatorBadge({this.semantics, super.key});

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: semantics != null ? semantics!(context, null) : L10n(context).unread,
      child: Container(
        key: Key('unread-indicator'),
        width: 8,
        height: 8,
        decoration: _badgeDecoration(context, BadgeOptions(includeBorder: false)),
      ),
    );
  }
}

/// A simple circle decoration to use for the badge background. Keys off of [BadgeOptions.onPrimarySurface] to
/// determine what color to make the background.
Decoration _badgeDecoration(BuildContext context, BadgeOptions options) => BoxDecoration(
      shape: BoxShape.circle,
      color: options.onPrimarySurface ? Theme.of(context).primaryIconTheme.color : Theme.of(context).colorScheme.secondary,
      // Can't use border here as there is an antialiasing issue: https://github.com/flutter/flutter/issues/13675
//      border: !options.includeBorder ? null : Border.all(color: Theme.of(context).scaffoldBackgroundColor, width: 2),
    );
