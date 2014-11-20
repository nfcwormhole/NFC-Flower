package com.flower.nfcaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AppOnlineUpdate {
    private final static String TAG = "AppOnlineUpdate";
    
    // version json url
    private final static String VERURL = "http://mloader.sinaapp.com/NFC_Flower_version.html";
    // apk download url
    
    private final static String APKURL =
            "http://mloader-mloader.stor.sinaapp.com/NFC_Flower/NFC_Flower.apk";
    
    // apk download directory
    private final static String DOWNLOADDIR = Environment.DIRECTORY_DOWNLOADS;
    
    // apk name
    private final static String APKNAME = "NFC_Flower.apk";
    
    
    private Context mContext;
    private String curVerName = "";
    private String newVerName = "";
    private int curVerCode;
    private int newVerCode;
    private long dmID;
    private DownloadManager dm;
    
    public AppOnlineUpdate(Context mContext) {
        this.mContext = mContext;
        curVerName = getVerName(mContext);
        curVerCode = getVerCode(mContext);
        dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    
    public void CompareVersion(Listener listener) {
        new Thread(new MyRunnable(listener)).start();
    }
    
    public interface Listener {
        /**
         * Called when have new version
         */
        public void onNewVersion(String currentVersion, String newVersion);
        
        /**
         * Called when have not new version
         */
        public void onVersion(String currentVersion);
    }
    
    private class MyRunnable implements Runnable {
        Listener listener;
        
        public MyRunnable(Listener listener) {
            this.listener = listener;
        }
        
        @Override
        public void run() {
            String data = getURLResponse(VERURL);
            try {
                parseJson(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            if (newVerCode > curVerCode) {
                listener.onNewVersion(curVerName, newVerName);
            } else {
                listener.onVersion(curVerName);
            }
        }
    }
    
    public void Download() {
        deleteOldFile();
        
        Uri uri = Uri.parse(APKURL);
        DownloadManager.Request r = new DownloadManager.Request(uri);
        r.setDestinationInExternalPublicDir(DOWNLOADDIR, APKNAME);
        r.setDescription("NFC Flower");
        r.setTitle(APKNAME);
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        dmID = dm.enqueue(r);
    }
    
    public boolean Install(Intent intent) {
        long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (dmID == myDwonloadID) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            Uri downloadFileUri = dm.getUriForDownloadedFile(dmID);
            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(install);
        }
        return true;
    }
    
    private static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }
    
    private static String getVerName(Context context) {
        String verName = "";
        try {
            verName =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verName;
    }
    
    private String getURLResponse(String urlString) {
        HttpURLConnection conn = null;
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            is = conn.getInputStream();
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
    
    private boolean parseJson(String data) throws JSONException {
        if (!data.equals("")) {
            // Log.d(TAG, "data" + data);
            String verjson = data;
            JSONArray array = new JSONArray(verjson);
            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                try {
                    newVerCode = Integer.parseInt(obj.getString("verCode"));
                    newVerName = obj.getString("verName");
                    // Log.d(TAG, "newVerCode:" + newVerCode);
                    // Log.d(TAG, "newVerName:" + newVerName);
                    // Log.d(TAG, "appname:" + obj.getString("appname"));
                    // Log.d(TAG, "apkname:" + obj.getString("apkname"));
                } catch (Exception e) {
                    newVerCode = -1;
                    newVerName = "";
                    return false;
                }
            }
        }
        return true;
    }
    
    private void deleteOldFile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(DOWNLOADDIR), APKNAME);
        if (file.exists())
            file.delete();
    }
}
