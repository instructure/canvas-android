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

import 'package:flutter/material.dart';

const double tabletSmallestWidth = 600;
const double appBarHeightTablet = 64;
const double appBarHeightLandscape = 48;
const double appBarFontSizeLandscape = 14;

/// Wraps the provided [appBar] in a [PreferredSize] widget and modifies the height, font size, and padding to match
/// the style of the native Toolbar according to device orientation and screen size.
PreferredSizeWidget dynamicStyleAppBar({@required BuildContext context, @required AppBar appBar}) {
  var isTablet = MediaQuery.of(context).size.shortestSide >= tabletSmallestWidth;
  var isLandscape = MediaQuery.of(context).orientation == Orientation.landscape;

  // Return original Appbar for default configuration (i.e. non-tablet portrait)
  if (!isTablet && !isLandscape) return appBar;

  Color color = appBar.backgroundColor ?? Theme.of(context).appBarTheme?.color ?? Theme.of(context).primaryColor;
  TextTheme textTheme = appBar.textTheme;
  double appBarHeight;
  EdgeInsets padding;

  if (isTablet) {
    // Regardless of device orientation, tablets use a tall appbar and have additional padding
    appBarHeight = appBarHeightTablet;
    padding = EdgeInsets.symmetric(
      horizontal: appBarHeightTablet - kToolbarHeight,
      vertical: (appBarHeightTablet - kToolbarHeight) / 2,
    );
  } else {
    // Portrait mode uses a short appbar and small font size
    appBarHeight = appBarHeightLandscape;
    var theme = textTheme ?? AppBarTheme.of(context).textTheme ?? Theme.of(context).primaryTextTheme ?? TextTheme();
    var headline = (theme.headline6 ?? TextStyle()).copyWith(fontSize: appBarFontSizeLandscape);
    textTheme = theme.copyWith(headline6: headline);
    padding = EdgeInsets.zero;
  }

  // Clone the AppBar to apply the modified properties
  AppBar flatAppbar = AppBar(
    elevation: 0, // Remove elevation
    textTheme: textTheme, // Use modified text theme
    key: appBar.key,
    leading: appBar.leading,
    automaticallyImplyLeading: appBar.automaticallyImplyLeading,
    title: appBar.title,
    actions: appBar.actions,
    flexibleSpace: appBar.flexibleSpace,
    bottom: appBar.bottom,
    shape: appBar.shape,
    backgroundColor: appBar.backgroundColor,
    brightness: appBar.brightness,
    iconTheme: appBar.iconTheme,
    actionsIconTheme: appBar.actionsIconTheme,
    primary: appBar.primary,
    centerTitle: appBar.centerTitle,
    excludeHeaderSemantics: appBar.excludeHeaderSemantics,
    titleSpacing: appBar.titleSpacing,
    toolbarOpacity: appBar.toolbarOpacity,
    bottomOpacity: appBar.bottomOpacity,
  );

  return PreferredSize(
    preferredSize: Size.fromHeight(appBarHeight),
    child: Material(
      child: Container(
        color: color,
        height: appBarHeight,
        padding: padding,
        child: flatAppbar,
      ),
    ),
  );
}
