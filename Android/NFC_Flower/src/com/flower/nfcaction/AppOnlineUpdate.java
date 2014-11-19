package com.flower.nfcaction;

import org.json.JSONException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AppOnlineUpdate {
    String TAG = "AppOnlineUpdate";
    Context mContext;
    String resultStr = "";
    
    public AppOnlineUpdate(Context mContext) {
        this.mContext = mContext;
    }
    
    void version() {
        ConnectivityManager connectMgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "WIFI");
                // 比较服务器版本//在 onCreate函数中调用
                try {
                    if (getServerVerCode()) {
                        ;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean getServerVerCode() throws JSONException {
        Thread visitBaiduThread = new Thread(new VisitWebRunnable());
        visitBaiduThread.start();
        return true;
    }
    
    class VisitWebRunnable implements Runnable {
        
        @Override
        public void run() {
            String data = getURLResponse("http://mloader.sinaapp.com/NFC_Flower_version.html");
            resultStr = data;
//            runOnUiThread(new Runnable() {
//                
//                @Override
//                public void run() {
//                    // Toast.makeText(getApplicationContext(), "拿到url 数据！",
//                    // Toast.LENGTH_LONG).show();// 没有新版本
//                    isupdate();
//                }
//            });
        }
        
    }
        
    /**
     * 获取指定URL的响应字符串
     * 
     * @param urlString
     * @return
     */
    private String getURLResponse(String urlString) {
        HttpURLConnection conn = null; // 连接对象
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(urlString); // URL对象
            conn = (HttpURLConnection) url.openConnection(); // 使用URL打开一个链接
            conn.setDoInput(true); // 允许输入流，即允许下载
            conn.setDoOutput(true); // 允许输出流，即允许上传
            conn.setUseCaches(false); // 不使用缓冲
            conn.setRequestMethod("GET"); // 使用get请求
            is = conn.getInputStream(); // 获取输入流，此时才真正建立链接
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(isr);
            String inputLine = "";
            while ((inputLine = bufferReader.readLine()) != null) {
                resultData += inputLine + "\n";
            }
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return resultData;
    }
}
