/**
 * Copyright 2014 sam, xiao_nie@163.com wangtengoo7@gmail.com
 * https://play.google.com/store/apps/details?id=com.flower.nfcaction More info :
 * http://www.elecfreaks.com
 */

package com.flower.nfcaction;

import com.flower.nfcaction.AppOnlineUpdate.Listener;

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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
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
    AppOnlineUpdate apkUp;
    
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
        
        appUpdate();
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
        unregisterReceiver(receiver);
    }
    
    private void appUpdate() {
        apkUp = new AppOnlineUpdate(this);
        apkUp.compareVersion(new Listener() {
            
            @Override
            public void onNewVersion(final String currentVersion, final String newVersion) {
                Log.d(TAG, "currentVersion" + currentVersion);
                Log.d(TAG, "newVersion" + newVersion);
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        doNewVersionUpdate(currentVersion, newVersion);
                    }
                });
            }
            
            @Override
            public void onVersion(String currentVersion) {
                Log.d(TAG, "Current version is lastest:" + currentVersion);
            }
        });
        
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                apkUp.installApk(intent);
            }
        };
        registerReceiver(receiver, filter);
    }
    
    private void doNewVersionUpdate(String currentVersion, String newVersion) {
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本:");
        sb.append(currentVersion);
        sb.append(", 发现新版本");
        sb.append(newVersion);
        sb.append(",是否更新?");
        Dialog dialog =
                new AlertDialog.Builder(this).setTitle("软件更新").setMessage(sb.toString())
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                apkUp.downloadApk();
                            }
                            
                        }).setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                            }
                        }).create();
        dialog.show();
    }
}
