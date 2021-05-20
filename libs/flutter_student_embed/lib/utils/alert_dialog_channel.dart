import 'package:flutter/services.dart';

class AlertDialogChannel extends MethodChannel {
  AlertDialogChannel(String channelId) : super(channelId) {
    setMethodCallHandler((methodCall) async {
      // Set up call handling here
    });
  }

  void dispose() {
    setMethodCallHandler(null);
  }

  Future<bool> showDialog(String title, String message, String positiveButtonText, String negativeButtonText) {
    return invokeMethod('showDialog',
        {
          "title": title,
          "message": message,
          "positiveButtonText": positiveButtonText,
          "negativeButtonText": negativeButtonText
        });
  }
}