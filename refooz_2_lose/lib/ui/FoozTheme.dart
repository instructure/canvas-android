import 'package:flutter/material.dart';

final ThemeData FoozThemeData = new ThemeData(
    brightness: Brightness.light,
    primarySwatch: FoozColors.primaryColor,
    primaryColor: FoozColors.primaryColor[500],
    primaryColorBrightness: Brightness.dark,
    accentColor: FoozColors.accentColor[500],
    accentColorBrightness: Brightness.dark
);

class FoozColors {
  FoozColors._();

  static const _bluePrimaryValue = 0xFF133b73;
  static const _redPrimaryValue = 0xFFbb2944;

  static const foozballBlueDefault = Color(0xFF133b73);
  static const foozballRedDefault = const Color(0xFFbb2944);


  static const MaterialColor primaryColor = const MaterialColor(_bluePrimaryValue, const <int, Color>{
    50: const Color(0xFFe3e7ee),
    100: const Color(0xFFb8c4d5),
    200: const Color(0xFF899db9),
    300: const Color(0xFF5a769d),
    400: const Color(0xFF365888),
    500: const Color(0xFF133b73),
    600: const Color(0xFF11356b),
    700: const Color(0xFF0e2d60),
    800: const Color(0xFF0b2656),
    900: const Color(0xFF061943),
  });

  static const MaterialColor accentColor = const MaterialColor(_redPrimaryValue, const <int, Color>{
    50: const Color(0xFFf7e5e9),
    100: const Color(0xFFebbfc7),
    200: const Color(0xFFdd94a2),
    300: const Color(0xFFcf697c),
    400: const Color(0xFFc54960),
    500: const Color(0xFFbb2944),
    600: const Color(0xFFb5243e),
    700: const Color(0xFFac1f35),
    800: const Color(0xFFa4192d),
    900: const Color(0xFF960f1f),
  });
}
