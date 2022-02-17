package be.rmdy.zebra_datawedge_scan_flutter

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject
import java.util.*

/** ZebraDatawedgeScanFlutterPlugin */
class ZebraDatawedgeScanFlutterPlugin: FlutterPlugin, MethodCallHandler {
  private val OPERATION_CHANNEL = "be.rmdy.zebra_datawedge_scan_flutter/operation"
  //private val RESULT_CHANNEL = "be.rmdy.zebra_datawedge_scan_flutter/result"
  private val PROFILE_INTENT_ACTION = "be.rmdy.zebradatawedgescan.SCAN"

  private lateinit var operationChannel : MethodChannel
  private lateinit var resultChannel : EventChannel
  private lateinit var context: Context
  private val dwInterface = DataWedgeInterface()

  private var dataWedgeBroadcastReceiver: BroadcastReceiver? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    operationChannel = MethodChannel(flutterPluginBinding.binaryMessenger, OPERATION_CHANNEL)
    operationChannel.setMethodCallHandler(this)

   /* resultChannel = EventChannel(flutterPluginBinding.binaryMessenger, RESULT_CHANNEL)
    resultChannel.setStreamHandler(
      object : StreamHandler {
        private var dataWedgeBroadcastReceiver: BroadcastReceiver? = null
        override fun onListen(arguments: Any?, events: EventSink?) {
          dataWedgeBroadcastReceiver = createDataWedgeBroadcastReceiver(events)
          val intentFilter = IntentFilter()
          intentFilter.addAction(PROFILE_INTENT_ACTION)
          intentFilter.addAction(DataWedgeInterface.DATAWEDGE_RETURN_ACTION)
          intentFilter.addCategory(DataWedgeInterface.DATAWEDGE_RETURN_CATEGORY)
          context.registerReceiver(
            dataWedgeBroadcastReceiver, intentFilter)
        }

        override fun onCancel(arguments: Any?) {
          context.unregisterReceiver(dataWedgeBroadcastReceiver)
          dataWedgeBroadcastReceiver = null
        }
      }
    )*/

    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
        "initScan" -> {
          createDataWedgeProfile("oh-green")
          result.success(true)
        }
        "doScan" -> {
          startIntentListener(result, context)
          //createMockResult(result)
        }
        "cancelScan" -> {
          stopIntentListener(context)
        }
        else -> {
          result.notImplemented()
        }
    }
  }

  private fun createMockResult(result: Result) {
    Handler().postDelayed({
      val scanData = "72"
      val symbology = "barcode"
      val currentScan = Scan(scanData ?: "", symbology ?: "");
      //todo, see if we need anything else than the value
      result.success("72")
    }, 3000)
  }

  private fun startIntentListener(result: Result, context: Context) {
    dataWedgeBroadcastReceiver = createDataWedgeBroadcastReceiver(result, context)
    val intentFilter = IntentFilter()
    intentFilter.addAction(PROFILE_INTENT_ACTION)
    intentFilter.addAction(DataWedgeInterface.DATAWEDGE_RETURN_ACTION)
    intentFilter.addCategory(DataWedgeInterface.DATAWEDGE_RETURN_CATEGORY)
    context.registerReceiver(
      dataWedgeBroadcastReceiver, intentFilter)
  }

  private fun stopIntentListener(context: Context) {
    if (dataWedgeBroadcastReceiver != null) {
      context.unregisterReceiver(dataWedgeBroadcastReceiver)
      dataWedgeBroadcastReceiver = null
    }
  }

  /* private fun createDataWedgeBroadcastReceiver(events: EventSink?): BroadcastReceiver? {
     return object : BroadcastReceiver() {
       override fun onReceive(context: Context, intent: Intent) {
         if (intent.action.equals(PROFILE_INTENT_ACTION))
         {
           //  A barcode has been scanned
           var scanData = intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
           var symbology = intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
           var currentScan = Scan(scanData ?: "", symbology ?: "");
           //todo, see if we need anything else than the value
           //events?.success(currentScan.toJson())
           events?.success(scanData)
         }
       }
     }
   }*/

  private fun createDataWedgeBroadcastReceiver(result: Result, context: Context): BroadcastReceiver {
    return object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(PROFILE_INTENT_ACTION))
        {
          val scanData = intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
          val symbology = intent.getStringExtra(DataWedgeInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
          val currentScan = Scan(scanData ?: "", symbology ?: "");
          result.success(currentScan.toJson())
          context.unregisterReceiver(dataWedgeBroadcastReceiver)
          dataWedgeBroadcastReceiver = null
        }
      }
    }
  }

  private fun createDataWedgeProfile(profileName: String) {
    //  Create and configure the DataWedge profile associated with this application
    //  For readability's sake, I have not defined each of the keys in the DWInterface file
    dwInterface.sendCommandString(context, DataWedgeInterface.DATAWEDGE_SEND_CREATE_PROFILE, profileName)
    val profileConfig = Bundle()
    profileConfig.putString("PROFILE_NAME", profileName)
    profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
    profileConfig.putString("CONFIG_MODE", "UPDATE")
    val barcodeConfig = Bundle()
    barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
    barcodeConfig.putString("RESET_CONFIG", "true") //  This is the default but never hurts to specify
    val barcodeProps = Bundle()
    barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
    profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
    val appConfig = Bundle()
    appConfig.putString("PACKAGE_NAME", context.packageName)      //  Associate the profile with this app
    appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
    profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
    dwInterface.sendCommandBundle(context, DataWedgeInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
    //  You can only configure one plugin at a time in some versions of DW, now do the intent output
    profileConfig.remove("PLUGIN_CONFIG")
    val intentConfig = Bundle()
    intentConfig.putString("PLUGIN_NAME", "INTENT")
    intentConfig.putString("RESET_CONFIG", "true")
    val intentProps = Bundle()
    intentProps.putString("intent_output_enabled", "true")
    intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
    intentProps.putString("intent_delivery", "2")
    intentConfig.putBundle("PARAM_LIST", intentProps)
    profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
    dwInterface.sendCommandBundle(context, DataWedgeInterface.DATAWEDGE_SEND_SET_CONFIG, profileConfig)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    operationChannel.setMethodCallHandler(null)
  }
}
