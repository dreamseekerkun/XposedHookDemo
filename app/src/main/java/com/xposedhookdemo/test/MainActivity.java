package com.xposedhookdemo.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    TextView tvIMEI;
    TextView tvMac;
    TextView tvModel;
    TextView tvBrand;
    TextView tvManufacuter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvIMEI = (TextView) findViewById(R.id.tv_imei);
        tvMac = (TextView) findViewById(R.id.tv_mac);
        tvModel = (TextView) findViewById(R.id.tv_model);
        tvBrand = (TextView) findViewById(R.id.tv_brand);
        tvManufacuter = (TextView) findViewById(R.id.tv_manufacuter);
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String IMEI = telephonyManager.getDeviceId();
        tvIMEI.setText("IMEI IS "+IMEI);
        tvMac.setText("MAC IS "+getMac());
        tvModel.setText("Model is "+ Build.MODEL);
        tvBrand.setText("Brand is "+get(this,"ro.product.brand","")+" Build brand is "+Build.BRAND+" read build.prop is "+getSystemBuildInfo("ro.product.brand"));
        tvManufacuter.setText("has xposed :"+FindHook.isHook(this));
    }

    @SuppressWarnings("MissingPermission")
    private String getMac() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {//防止获取SSID为0X或者<unknown ssid>
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return "null";
            }
            return wifiManager.getConnectionInfo().getMacAddress();
        }else{
            return "no wifi";
        }
    }

    public static String get(Context context, String key, String def) {
        String ret = def;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;
            Method get = SystemProperties.getMethod("get", paramTypes);
            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new String(def);
            ret = (String) get.invoke(SystemProperties, params);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
        }
        return ret;
    }
    public static String getSystemBuildInfo(String str) {
        BufferedReader br = null;
        String systemStr = "";
        try {
            br = new BufferedReader(new FileReader("/system/build.prop"));
            String text = br.readLine();
            while (text != null) {
                text = br.readLine();
                if (!"".equals(str) && text.contains(str)) {
                    systemStr = text.substring(text.indexOf('=') + 1);
                    return systemStr;
                } else {
                    systemStr += text;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
         if(br!=null){
             try {
                 br.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
        }
        return systemStr;
    }

}
