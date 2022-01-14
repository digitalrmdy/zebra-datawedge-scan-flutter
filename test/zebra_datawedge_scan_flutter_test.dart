import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zebra_datawedge_scan_flutter/zebra_datawedge_scan_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('zebra_datawedge_scan_flutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ZebraDatawedgeScanFlutter.platformVersion, '42');
  });
}
