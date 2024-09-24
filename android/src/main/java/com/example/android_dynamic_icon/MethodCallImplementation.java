package com.example.android_dynamic_icon;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AndDynamicIconPlugin */
public class MethodCallImplementation implements MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;
  private Activity activity;
  private static final String TAG = AndroidDynamicIconPlugin.getTAG();

  private static List<String> classNames = null;
  private static boolean iconChanged = false;
  private static List<String> args =  new ArrayList<>();

  MethodCallImplementation(Context context, Activity activity) {
    this.context = context;
    this.activity = activity;
  }

  void setActivity(Activity activity) {
      this.activity = activity;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
      switch (call.method) {
        case "initialize":
          {
              classNames = call.arguments();
              break;
          }
        case "changeIcon":
          {
              changeIcon(call, result);
              break;
          }
        case "getCurrentIcon":
          {
              getCurrentIcon(result);
              break;
          }
        default:
            result.notImplemented();
            break;
      }
  }

  private void changeIcon(MethodCall call, Result result) {
        if(classNames == null || classNames.isEmpty()) {
        Log.e(TAG,"Initialization Failed!");
        Log.i(TAG,"List all the activity-alias class names in initialize()");
        return;
      }

      args = call.arguments();
      iconChanged = true;
      result.success(true);
  }

  void updateIcon() {
    if (iconChanged){
        String className = args.get(0);
        PackageManager pm = activity.getPackageManager();
        String packageName = activity.getPackageName();
        int componentState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        int i=0;

        for (;i<classNames.size();i++) {
            ComponentName cn = new ComponentName(packageName, packageName+"."+classNames.get(i));
            if(className.equals(classNames.get(i))) {
                componentState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            }
            else{
                componentState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }
            pm.setComponentEnabledSetting(cn, componentState, PackageManager.DONT_KILL_APP);
        }

        if (i>classNames.size()) {
            Log.e(TAG,"class name "+className+" did not match in the initialized list.");
            return;
        }

        iconChanged = false;
        Log.d(TAG,"Icon switched to "+className);
      }
  }

  private void getCurrentIcon(Result result) {
    try {
        PackageManager pm = activity.getPackageManager();
        String packageName = activity.getPackageName();

        for (String className : classNames) {
            ComponentName cn = new ComponentName(packageName, packageName + "." + className);
            int state = pm.getComponentEnabledSetting(cn);

            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                result.success(className);
                return;
            }
        }

        // If no component is enabled, return a default value
        result.success(null);
    } catch (Exception e) {
        result.error("ERROR", "Failed to get the current icon", e.getMessage());
    }
  }
}