
import 'dart:async';

import 'package:flutter/services.dart';

class ZebraDatawedgeScanFlutter {
  static const MethodChannel _channel = MethodChannel('be.rmdy.zebra_datawedge_scan_flutter/operation');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool?> initScan() async {
    await _channel.invokeMethod('initScan');
    return true;
  }

  static Future<String?> doScan() async {
    final String? version = await _channel.invokeMethod('doScan');
    return version;
  }
}
