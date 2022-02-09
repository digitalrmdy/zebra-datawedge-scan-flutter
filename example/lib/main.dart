import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:zebra_datawedge_scan_flutter/zebra_datawedge_scan_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _scanResult = ' - ';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    //only makes sense on a Zebra device
    //await ZebraDatawedgeScanFlutter.initScan();
  }



  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Zebra scan example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ElevatedButton(onPressed: () {
                doScan();
              }, child: const Text("Scan")),
              const SizedBox(height: 16,),
              Text('Scan result : $_scanResult'),
            ],
          ),
        ),
      ),
    );
  }

  void doScan() async {
    setState(() {
      _scanResult = ' - ';
    });
    String scanResult;
    try {
      scanResult =
          await ZebraDatawedgeScanFlutter.doScan() ?? ' - ';
    } on PlatformException {
      scanResult = 'Failed to get scan';
    }

    if (!mounted) return;

    setState(() {
      _scanResult = scanResult;
    });
  }
}
