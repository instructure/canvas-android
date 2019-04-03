import 'package:flutter/material.dart';

class ColorParser {

  static Color fromHex(String hex) {
    if (hex == null) return Color.fromARGB(255, 0, 0, 0);
    String code = hex.replaceFirst("#", "").replaceFirst("0x", "").replaceFirst("0X", "");
    int a = 255;
    int r, g, b = 0;

    switch(code.length) {
      case 3:
        r = int.parse(code[0] + code[0], radix: 16);
        g = int.parse(code[1] + code[1], radix: 16);
        b = int.parse(code[2] + code[2], radix: 16);
        break;
      case 4:
        a = int.parse(code[0] + code[0], radix: 16);
        r = int.parse(code[1] + code[1], radix: 16);
        g = int.parse(code[2] + code[2], radix: 16);
        b = int.parse(code[3] + code[3], radix: 16);
        break;
      case 6:
        r = int.parse(code.substring(0, 2), radix: 16);
        g = int.parse(code.substring(2, 4), radix: 16);
        b = int.parse(code.substring(4), radix: 16);
        break;
      case 8:
        a = int.parse(code.substring(0, 2), radix: 16);
        r = int.parse(code.substring(2, 4), radix: 16);
        g = int.parse(code.substring(4, 6), radix: 16);
        b = int.parse(code.substring(6), radix: 16);
        break;
      default:
        break;
    }

    return Color.fromARGB(a, r, g, b);
  }

}
