package com.xposedhookdemo.test;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检测native hook框架：Cydia Substrate、Xposed
 * Created by likun on 16/12/12.
 */

public class FindHook {

    private static boolean findHookAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applicationInfoList) {
            //full packagename : de.robv.android.xposed.installer
            if (applicationInfo.packageName.contains("xposed.installer")) {
                Log.d("FindHook", "has xposed ");
                return true;
            }
            if (applicationInfo.packageName.equals("com.saurik.substrate")) {
                return true;
            }
        }
        return false;
    }

    private static boolean findHookAppFile() {

        try {
            Set<String> libraries = new HashSet<String>();
            String mapsFilename = "/proc/" + android.os.Process.myPid() + "/maps";
            BufferedReader reader = new BufferedReader(new FileReader(mapsFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".so") || line.endsWith(".jar")) {
                    int n = line.lastIndexOf(" ");
                    libraries.add(line.substring(n + 1));
                }
            }

            reader.close();
            for (String library : libraries) {
                if (library.contains("com.saurik.substrate")) {
                    return true;
                }
                if (library.contains("XposedBridge.jar")) {
                    Log.d("findHookAppFile", " XposedBridge");
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean findHookAppDataDir(){

        String [] path = {"/data/data/de.robv.android.xposed.installer","/data/data/com.saurik.substrate"};
        for (String s : path) {
            File file = new File(s);
            if(file.exists()){
                Log.d("findHookAppDataDir", " findHookAppDataDir");
                return true;
            }
        }
        return false;
    }

    public static boolean isHook(Context context) {

        if (findHookAppName(context) || findHookAppFile()||findHookAppDataDir() ) {
            return true;
        }
        return false;
    }

}
