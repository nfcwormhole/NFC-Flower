/**
 * Copyright 2014 sam, xiao_nie@163.com
 * https://play.google.com/store/apps/details?id=com.flower.nfcaction More info :
 * http://www.elecfreaks.com
 */

package com.flower.nfcaction;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private ViewPager navigationView;
    
    private View[] panels = new View[3];
    
    private String[] titles;
    TextView hum;
    TextView tem;
    TextView moi;
    
    SharedPreferences sp;
    BroadcastReceiver receiver;
    int newVerCode = 2;
    String newVerName = "";
    String resultStr = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate!");
        setContentView(R.layout.activity_main);
        
        initView();
        
        navigationView.setAdapter(new NavigationAdapter());
        titles = getResources().getStringArray(R.array.titles);
        
        hum = (TextView) panels[0].findViewById(R.id.humidity_textview_info);
        tem = (TextView) panels[0].findViewById(R.id.temperature_textview_info);
        moi = (TextView) panels[0].findViewById(R.id.moisture_textview_info);
        
        Context ctx = MainActivity.this;
        sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
        hum.setText(sp.getString("hum", "40%"));
        tem.setText(sp.getString("tem", "28°C"));
        moi.setText(sp.getString("moi", "80%"));
        
        ConnectivityManager connectMgr =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    void isupdate() {
        int vercode = getVerCode(this);
        if (newVerCode > vercode) {
            // doNewVersionUpdate();//发现新版本更新
            // update();
            doNewVersionUpdate();
            Toast.makeText(getApplicationContext(), "有新版本", Toast.LENGTH_LONG).show();// 没有新版本
        } else {
            Toast.makeText(getApplicationContext(), "目前是最新版本，感谢您的支持", Toast.LENGTH_LONG).show();// 没有新版本
        }
    }
    
    private void doNewVersionUpdate() {
        int verCode = getVerCode(this);
        String verName = getVerName(this);
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本:");
        sb.append(verName);
        sb.append(" Code:");
        sb.append(verCode);
        sb.append(", 发现新版本");
        sb.append(newVerName);
        sb.append(" Code:");
        sb.append(newVerCode);
        sb.append(",是否更新?");
        Dialog dialog = new AlertDialog.Builder(this).setTitle("软件更新").setMessage(sb.toString())
        // 设置内容
                .setPositiveButton("更新",// 设置确定按钮
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                update();
                            }
                            
                        }).setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 点击"取消"按钮之后退出程序
                        finish();
                    }
                }).create();// 创建
        // 显示对话框
        dialog.show();
    }
    
    private boolean getServerVerCode() throws JSONException {
        // try {
        // // 取得服务器地址和接口文件名
        // String verjson =
        // NetworkTool.getContent("http://mloader.sinaapp.com/NFC_Flower_version.html");
        // // Log.d(TAG, verjson);
        // JSONArray array = new JSONArray(verjson);
        // if (array.length() > 0) {
        // JSONObject obj = array.getJSONObject(0);
        // try {
        // newVerCode = Integer.parseInt(obj.getString("verCode"));
        // newVerName = obj.getString("verName");
        // } catch (Exception e) {
        // newVerCode = -1;
        // newVerName = "";
        // return false;
        // }
        // }
        // } catch (Exception e) {
        // // Log.e(TAG, e.getMessage());
        // Log.e(TAG, "get Server Error");
        // return false;
        // }
        
        // TODO Auto-generated method stub
        Thread visitBaiduThread = new Thread(new VisitWebRunnable());
        visitBaiduThread.start();
        // try {
        // visitBaiduThread.join();
        // if (!resultStr.equals("")) {
        // Log.d(TAG, "resultStr" + resultStr);
        // String verjson = resultStr;
        // JSONArray array = new JSONArray(verjson);
        // if (array.length() > 0) {
        // JSONObject obj = array.getJSONObject(0);
        // try {
        // newVerCode = Integer.parseInt(obj.getString("verCode"));
        // newVerName = obj.getString("verName");
        // Log.d(TAG, "newVerCode:" + newVerCode);
        // Log.d(TAG, "newVerName:" + newVerName);
        // Log.d(TAG, "appname:" + obj.getString("appname"));
        // Log.d(TAG, "apkname:" + obj.getString("apkname"));
        // } catch (Exception e) {
        // newVerCode = -1;
        // newVerName = "";
        // return false;
        // }
        // }
        // }
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return true;
    }
    
    class VisitWebRunnable implements Runnable {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            String data = getURLResponse("http://mloader.sinaapp.com/NFC_Flower_version.html");
            resultStr = data;
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    // Toast.makeText(getApplicationContext(), "拿到url 数据！",
                    // Toast.LENGTH_LONG).show();// 没有新版本
                    isupdate();
                }
            });
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        
        return resultData;
    }
    
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }
    
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verName;
    }
    
    private void update() {
        Uri uri = Uri.parse("http://mloader-mloader.stor.sinaapp.com/NFC_Flower/NFC_Flower.apk");
        DownloadManager.Request r = new DownloadManager.Request(uri);
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "NFC_Flower.apk");
        r.setDescription("NFC Flower");
        r.setTitle("NFC_Flower.apk");
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        
        // if apk exist, del it.
        File file =
                new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "NFC_Flower.apk");
        if (file.exists())
            file.delete();
        
        final DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        final long refernece = dm.enqueue(r);
        
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (refernece == myDwonloadID) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    Uri downloadFileUri = dm.getUriForDownloadedFile(refernece);
                    install.setDataAndType(downloadFileUri,
                            "application/vnd.android.package-archive");
                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(install);
                }
            }
        };
        registerReceiver(receiver, filter);
    }
    
    
    @Override
    public void startActivity(Intent intent) {
        Log.d("TAG", "startActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.startActivity(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume!");
        Intent intent = null;
        intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                // process user-defined task
                user_definded_task(msgs[0]);
            }
        }
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause!");
    }
    
    private void user_definded_task(NdefMessage msgs) {
        NdefRecord[] records = msgs.getRecords();
        for (int i = 0; i < records.length; i++) {
            records[i] = records[i];
            Log.d("sam test", "records[" + i + "] =" + records[i].toString());
        }
        if (Arrays.equals(records[1].getType(), "flower:humidity".getBytes())) {
            Log.d("sam test", "equals flower:humidity");
            byte[] humidity = records[1].getPayload();
            for (byte b : humidity) {
                Log.d("sam test", "b=" + Integer.toHexString(b));
            }
            
            // display theme
            hum.setText(humidity[0] + "%");
        }
        if (Arrays.equals(records[2].getType(), "flower:temperature".getBytes())) {
            Log.d("sam test", "equals flower:temperature");
            byte[] temperature = records[2].getPayload();
            for (byte b : temperature) {
                Log.d("sam test", "b=" + Integer.toHexString(b));
            }
            
            // display theme
            tem.setText(temperature[0] + "°C");
        }
        if (Arrays.equals(records[3].getType(), "flower:moisture".getBytes())) {
            Log.d("sam test", "equals flower:moisture");
            byte[] moisture = records[3].getPayload();
            for (byte b : moisture) {
                Log.d("sam test", "b=" + Integer.toHexString(b));
            }
            
            // display theme
            moi.setText(moisture[0] + "%");
        }
    }
    
    private void initView() {
        navigationView = (ViewPager) findViewById(R.id.navigations);
        
        getLayoutInflater();
        
        LayoutInflater lf = LayoutInflater.from(this);
        
        // info panel
        panels[0] = lf.inflate(R.layout.info, null);
        // button click
        Button watherBtn = (Button) panels[0].findViewById(R.id.wather_btn);
        watherBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // water flower
                String value = "water";
                Intent intent = new Intent(MainActivity.this, WriteTag.class);
                intent.putExtra("flower", value);
                intent.putExtra("water", 1);
                startActivity(intent);
                // Toast.makeText(getApplicationContext(), "water flower",
                // Toast.LENGTH_SHORT).show();
            }
        });
        
        // setting panel
        panels[1] = lf.inflate(R.layout.setttings, null);
        final TextView humiditySetting =
                (TextView) panels[1].findViewById(R.id.humidity_text_display);
        final SeekBar humiditySeekBar = (SeekBar) panels[1].findViewById(R.id.humidity_seekbar);
        humiditySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                
                humiditySetting.setText(progress + "%");
            }
        });
        
        // button click
        Button watherBtn1 = (Button) panels[1].findViewById(R.id.wather_btn);
        watherBtn1.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // water flower
                String type = "setting";
                Intent intent = new Intent(MainActivity.this, WriteTag.class);
                intent.putExtra("flower", type);
                intent.putExtra(type, humiditySeekBar.getProgress());
                startActivity(intent);
                // Toast.makeText(getApplicationContext(),
                // "Setting humidity warn!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // about panel
        panels[2] = lf.inflate(R.layout.about, null);
    }
    
    private class NavigationAdapter extends PagerAdapter {
        
        @Override
        public int getCount() {
            return panels.length;
        }
        
        @Override
        public Object instantiateItem(View view, int position) {
            ((ViewPager) view).addView(panels[position]);
            return panels[position];
        }
        
        @Override
        public void destroyItem(View view, int arg1, Object obj) {
            ((ViewPager) view).removeView((View) obj);
        }
        
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop!");
        Editor editor = sp.edit();
        editor.putString("hum", hum.getText().toString());
        editor.putString("tem", tem.getText().toString());
        editor.putString("moi", moi.getText().toString());
        editor.commit();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // unregisterReceiver(receiver);
    }
}
