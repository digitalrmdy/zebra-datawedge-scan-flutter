
import 'dart:async';

import 'package:flutter/services.dart';

class ZebraDatawedgeScanFlutter {
  static const MethodChannel _channel = MethodChannel('be.rmdy.zebra_datawedge_scan_flutter/operation');

  static Future<bool?> initScan() async {
    await _channel.invokeMethod('initScan');
    return true;
  }

  static Future<String?> doScan() async {
    return await _channel.invokeMethod('doScan');
  }

  static Future cancelScan() async {
    _channel.invokeMethod('cancelScan');
    return;
  }
}
