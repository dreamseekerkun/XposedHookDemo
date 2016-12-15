package com.xposedhookdemo.test;

import android.util.Log;

import java.io.File;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by likun on 16/12/8.
 */

public class HookTest implements IXposedHookLoadPackage {

    String[] macs = {"random 1", "random 2", "random 3", "random 4", "random 5", "random 6"};
    String[] imeis = {"35367871111", "35367872222", "35367873333", "35367874444", "35367875555", "35367876666"};

    private void hook_method(String className, ClassLoader classLoader, String methodName, Object... objects) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.e("XposedTest ", loadPackageParam.packageName);
        if(!"com.lbadvisor.userclear.demo.lbadvisor.debug".equals(loadPackageParam.packageName)
                ||!"com.xposedhookdemo.test".equals(loadPackageParam.packageName))
            return;
        XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", "iphone 7 Plus (5G)");
        XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", "IPhone");
        XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND", "apple");
        hook_method("android.telephony.TelephonyManager", loadPackageParam.classLoader, "getDeviceId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("XposedTest", "hook getDevicedId");
                Object obj = param.getResult();
                Log.d("XposedTest", "IMEI IS " + obj);
                param.setResult("IMEI is cracked:" + imeis[new Random().nextInt(imeis.length)]);
            }
        });
        hook_method("android.net.wifi.WifiInfo", loadPackageParam.classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("XposedTest", "hook getMacAddress");
                Object obj = param.getResult();
                Log.d("XposedTest", "MAC IS " + obj);
                param.setResult("Mac is cracked:" + macs[new Random().nextInt(macs.length)]);
            }
        });
        hook_method("android.os.SystemProperties", loadPackageParam.classLoader, "native_get",
                new Object[]{String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String key = (String) param.args[0];
                        Log.d("HookTest", "parm key " + key);
                        if ("ro.product.brand".equals(key)) {
                            param.setResult("Apple");
                        }
                        param.hasThrowable();
                    }
                }});

        //hook 判断是否开启代理模式
        hook_method("java.lang.System", loadPackageParam.classLoader, "getProperty", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("HookTest", "hook system ");
                String key = (String) param.args[0];
                if ("http.proxyHost".equals(key))
                    param.setResult(null);

                if ("http.proxyPort".equals(key))
                    param.setResult(null);
            }
        });

        //hook SSID
        hook_method("android.net.wifi.WifiInfo", loadPackageParam.classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("HookTest", "hook getSSID " + param.getResult());
                param.setResult("hookSSID");
            }
        });

        hook_method("android.content.pm.PackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("HookTest", "hook packages ");
                Log.d("hook install packages ", param.thisObject.toString());
                Log.d("hook install getresult ", param.getResult().toString());
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String name = (String) param.args[0];
                XposedBridge.log("Hooked getPackageInfo "+name);
//                if (name != null && "com.lbadvisor.userclear.demo.lbadvisor.debug".equals(name) ) {
//                    param.args[0] = "packages has been hookded"; // Set a fake package name
//                    XposedBridge.log("Found and hid package: " + name);
//                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                String name = (String) param.args[0];
//                XposedBridge.log("Hooked after getPackageInfo "+name);
            }
        });

        /**
         * hook构造函数，此处为hook文件打开操作，更改文件路径为/dev/hook
         */
        XposedBridge.hookAllConstructors(File.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("HookTest", "文件被 hook了 " + param.thisObject.toString());
                String str = param.thisObject.toString();
                if (str.contains("/proc/cpuinfo") | str.contains("/system/build.prop")) {
                    Log.d("HookTest", "contains " + str);
//                    Class clazz = XposedHelpers.findClass("java.io.File", loadPackageParam.classLoader);
//                    Field field = clazz.getDeclaredField("path");//hook path字段，通过反射更改其值
//                    field.setAccessible(true);
//                    field.set(param.thisObject, "/dev/hook");
                    XposedHelpers.setObjectField(param.thisObject, "path", "/dev/hook");
                }
                param.hasThrowable();
            }
        });
    }
}
