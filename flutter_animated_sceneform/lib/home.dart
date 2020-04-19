import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Home extends StatelessWidget {

  static const platform = const MethodChannel("test_activity");

  _getNewActivity() async {
    try {
      await platform.invokeMethod('startNewActivity');
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Flutter AR Animated SceneForm"),
      ),
      body: Center(),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _getNewActivity();
        },
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}