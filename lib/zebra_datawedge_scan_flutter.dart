
import 'dart:async';

import 'package:flutter/services.dart';

class ZebraDatawedgeScanFlutter {
  static const MethodChannel _channel = MethodChannel('zebra_datawedge_scan_flutter');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
