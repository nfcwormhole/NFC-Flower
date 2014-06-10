/** Copyright 2014 sam, xiao_nie@163.com  
 *  https://play.google.com/store/apps/details?id=com.flower.nfcaction
 *  More info : http://www.elecfreaks.com 
 */

package com.flower.nfcaction;

import android.app.Activity;
import android.content.Intent;
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

    private ViewPager navigationView;

    private View[] panels = new View[3];

    private String[] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        navigationView.setAdapter(new NavigationAdapter());
        titles = getResources().getStringArray(R.array.titles);
    }

    @Override
    public void startActivity(Intent intent) {
        Log.d("sam test", "startActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.startActivity(intent);
    }

    public void onResume() {
        super.onResume();

        Intent intent = null;
        intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage)rawMsgs[i];
                }
                // process user-defined task
                user_definded_task(msgs[0]);
            }
        }

    }

    private void user_definded_task(NdefMessage msgs) {
        NdefRecord[] records = msgs.getRecords();
        for (int i = 0; i < records.length; i++) {
            records[i] = (NdefRecord)records[i];
            Log.d("sam test", "records[" + i + "] =" + records[i].toString());
        }
        if (Arrays.equals(records[1].getType(), "flower:humidity".getBytes())) {
            Log.d("sam test", "equals flower:humidity");
            byte[] humidity = records[1].getPayload();
            for (byte b : humidity) {
                Log.d("sam test", "b=" + Integer.toHexString(b));
            }

            // display theme
            TextView hum = (TextView)panels[0].findViewById(R.id.humidity_textview_info);
            hum.setText(humidity[0] + "%");
            // hum.setText(Integer.toHexString(humidity[1])+"%");
        }
    }

    private void initView() {
        navigationView = (ViewPager)findViewById(R.id.navigations);

        getLayoutInflater();

        LayoutInflater lf = LayoutInflater.from(this);

        // info panel
        panels[0] = lf.inflate(R.layout.info, null);
        // button click
        Button watherBtn = (Button)panels[0].findViewById(R.id.wather_btn);
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
        final TextView humiditySetting = (TextView)panels[1]
                .findViewById(R.id.humidity_text_display);
        final SeekBar humiditySeekBar = (SeekBar)panels[1].findViewById(R.id.humidity_seekbar);
        humiditySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                humiditySetting.setText(progress + "%");
            }
        });

        // button click
        Button watherBtn1 = (Button)panels[1].findViewById(R.id.wather_btn);
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
            ((ViewPager)view).addView(panels[position]);
            return panels[position];
        }

        @Override
        public void destroyItem(View view, int arg1, Object obj) {
            ((ViewPager)view).removeView((View)obj);
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

}
