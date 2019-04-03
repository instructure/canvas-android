package com.instructure.refooz2lose

import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log

import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
        setupNfcChannel()
    }

    private fun setupNfcChannel() {
        val tableAssignmentUri = try {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val foosRecord = (rawMsgs[0] as NdefMessage).records[0]
            Log.i("Foos", "Read NFC Tag: ${foosRecord.toUri()}")
            foosRecord.toUri().toString()
        } catch (e: Throwable) {
            try {
                require(intent.data.scheme == "foos")
                Log.i("Foos", "Read URI: ${intent.data}")
                intent.data.toString()
            } catch (e: Throwable) {
                null
            }
        }

        MethodChannel(flutterView, NFC_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getTableAssignmentUri" -> result.success(tableAssignmentUri)
                else -> result.notImplemented()
            }
        }
    }

    companion object {
        const val NFC_CHANNEL = "com.instructure.refooz2lose/nfc"
    }

}
