import 'package:permission_handler/permission_handler.dart';

class PermissionHandler {
  Future<PermissionStatus> checkPermissionStatus(Permission permission) async {
    return permission.status;
  }

  Future<PermissionStatus> requestPermission(Permission permission) async {
    return permission.request();
  }
}