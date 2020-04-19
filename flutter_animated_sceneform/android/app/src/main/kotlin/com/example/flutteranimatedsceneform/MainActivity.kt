package com.example.flutteranimatedsceneform

import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    private val channel = "test_activity"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler {
            _, _ -> startNewActivity()
        }
    }

    private fun startNewActivity() {
        val intent = Intent(this, ArAnimatedActivity::class.java)
        startActivity(intent)
    }
}