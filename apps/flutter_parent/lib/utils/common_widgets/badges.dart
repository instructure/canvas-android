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
  final int count;
  final int maxCount;
  final ValueListenable countListenable;

  const WidgetBadge(this.icon, {Key key, this.count, this.maxCount = 99, this.countListenable})
      : assert(icon != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    return Stack(
      overflow: Overflow.visible,
      children: <Widget>[
        icon,
        _badge(),
      ],
    );
  }

  Widget _badge() {
    // If no badge count and no listenable are provided, we just want an indicator on the icon, not a badge with a count
    if (count == null && countListenable == null) {
      return PositionedDirectional(start: 8, top: 8, child: IndicatorBadge());
    }
    return PositionedDirectional(end: -10, child: NumberBadge(count: count, listenable: countListenable));
  }
}

/// A badge with a number in it. If the count is zero or less then no badge is shown. A listenable can be provided so
/// that the badge count will update automatically. If maxCount is provided (defaults to 99) then the count will be
/// capped at that number with a "+" appended.
class NumberBadge extends StatelessWidget {
  final int count;
  final int maxCount;
  final ValueListenable listenable;

  const NumberBadge({Key key, this.count, this.maxCount = 99, this.listenable}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (listenable == null) return _badge(context, count);
    return ValueListenableBuilder(
      valueListenable: listenable,
      builder: (context, count, _) => _badge(context, count),
    );
  }

  Widget _badge(BuildContext context, int count) {
    // If there's no count, then don't show anything
    if (count == null || count <= 0) return SizedBox();
    return Container(
      decoration: _badgeDecoration(context),
      child: Padding(
        padding: const EdgeInsets.all(6.0),
        child: Text(
          maxCount != null && count > maxCount ? L10n(context).badgeNumberPlus(maxCount) : '$count',
          style: TextStyle(
            fontSize: 12,
            color: Theme.of(context).accentIconTheme.color,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}

/// An empty colored circle, used as an indicator badge. Typically signals that an item is unread, which doesn't need a
/// count like the NumberBadge.
class IndicatorBadge extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 8,
      height: 8,
      decoration: _badgeDecoration(context),
    );
  }
}

Decoration _badgeDecoration(BuildContext context) => BoxDecoration(
      color: Theme.of(context).accentColor,
      shape: BoxShape.circle,
    );
