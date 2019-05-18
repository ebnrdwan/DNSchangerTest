package com.frostnerd.dnschangerlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.frostnerd.dnschangerlight.API.API;
import com.frostnerd.dnschangerlight.API.MaterialEditText;
import com.frostnerd.dnschangerlight.API.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * Terms on usage of my code can be found here: https://git.frostnerd.com/PublicAndroidApps/DnsChanger/blob/master/README.md
 *
 * <p>
 * development@frostnerd.com
 */
public class MainActivity extends AppCompatActivity {
    private Button startStopButton;
    private boolean vpnRunning;
    private MaterialEditText met_dns1, met_dns2;
    private EditText dns1, dns2;
    private static final HashMap<String, List<String>> defaultDNS = new HashMap<>();
    private static final HashMap<String, List<String>> defaultDNS_V6 = new HashMap<>();
    private static final List<String> defaultDNSKeys, DefaultDNSKeys_V6;
    private boolean doStopVPN = true;

    private TextView connectionText;
    private ImageView connectionImage;
    private LinearLayout defaultDNSView;
    private Button rate, info;
    private ImageButton importButton;
    private View running_indicator;

    private AlertDialog defaultDnsDialog;
    private LinearLayout wrapper;
    private boolean settingV6 = false;

    @Override
    protected void onDestroy() {
        if(dialog1 != null)dialog1.cancel();
        if(dialog2 != null)dialog2.cancel();
        if(defaultDnsDialog != null)defaultDnsDialog.cancel();
        super.onDestroy();
    }

    private AlertDialog dialog1, dialog2;

    private BroadcastReceiver serviceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            vpnRunning = intent.getBooleanExtra("vpn_running",false);
            setIndicatorState(intent.getBooleanExtra("vpn_running",false));
        }
    };

    static {
        defaultDNS.put("Google DNS", Arrays.asList("185.228.168.168", "185.228.169.168", "2001:4860:4860::8888", "2001:4860:4860::8844"));
        defaultDNS.put("OpenDNS", Arrays.asList("208.67.222.222", "208.67.220.220", "2620:0:ccc::2", "2620:0:ccd::2"));
        defaultDNS.put("Level3", Arrays.asList("209.244.0.3", "209.244.0.4"));
        defaultDNS.put("FreeDNS", Arrays.asList("37.235.1.174", "37.235.1.177"));
        defaultDNS.put("Yandex DNS", Arrays.asList("77.88.8.8", "77.88.8.1", "2a02:6b8::feed:0ff", "2a02:6b8:0:1::feed:0ff"));
        defaultDNS.put("Verisign", Arrays.asList("64.6.64.6", "64.6.65.6", "2620:74:1b::1:1", "2620:74:1c::2:2"));
        defaultDNS.put("Alternate DNS", Arrays.asList("198.101.242.72", "23.253.163.53"));

        defaultDNS_V6.put("Google DNS", Arrays.asList("2001:4860:4860::8888", "2001:4860:4860::8844"));
        defaultDNS_V6.put("OpenDNS", Arrays.asList("2620:0:ccc::2", "2620:0:ccd::2"));
        defaultDNS_V6.put("Yandex DNS", Arrays.asList("2a02:6b8::feed:0ff", "2a02:6b8:0:1::feed:0ff"));
        defaultDNS_V6.put("Verisign", Arrays.asList("2620:74:1b::1:1", "2620:74:1c::2:2"));
        defaultDNSKeys = new ArrayList<>(defaultDNS.keySet());
        DefaultDNSKeys_V6 = new ArrayList<>(defaultDNS_V6.keySet());
    }

    private void setIndicatorState(boolean vpnRunning) {
//        ObjectAnimator colorFade = ObjectAnimator.ofObject(wrapper, "backgroundColor", new ArgbEvaluator(),
//                vpnRunning ? Color.parseColor("#2196F3") : Color.parseColor("#4CAF50"),
//                vpnRunning ? Color.parseColor("#4CAF50") : Color.parseColor("#2196F3"));
//        colorFade.setDuration(400);
//        colorFade.start();
        if (vpnRunning) {
            int color = Color.parseColor("#42A5F5");
            connectionText.setText(R.string.running);
            connectionImage.setImageResource(R.drawable.ic_thumb_up);
            startStopButton.setBackgroundColor(color);
            met_dns1.setCardColor(color);
            met_dns1.setCardStrokeColor(color);
            met_dns2.setCardColor(color);
            met_dns2.setCardStrokeColor(color);
            defaultDNSView.setBackgroundColor(color);
            rate.setBackgroundColor(color);
            info.setBackgroundColor(color);
            importButton.setBackgroundColor(color);
            startStopButton.setText(R.string.stop);
            running_indicator.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            int color = Color.parseColor("#42A5F5");
            connectionText.setText(R.string.not_running);
            connectionImage.setImageResource(R.drawable.ic_thumb_down);
            startStopButton.setBackgroundColor(color);
            met_dns1.setCardColor(color);
            met_dns1.setCardStrokeColor(color);
            met_dns2.setCardColor(color);
            met_dns2.setCardStrokeColor(color);
            defaultDNSView.setBackgroundColor(color);
            rate.setBackgroundColor(color);
            info.setBackgroundColor(color);
            importButton.setBackgroundColor(color);
            startStopButton.setText(R.string.start);
            running_indicator.setBackgroundColor(Color.parseColor("#2196F3"));
        }
    }

    public void rateApp(View v) {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
        Preferences.put(MainActivity.this, "rated",true);
    }

    public void openDNSInfoDialog(View v) {
        dialog1 = new AlertDialog.Builder(this).setTitle(R.string.info_dns_button).setMessage(R.string.dns_info_text).setCancelable(true).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ConnectivityBackgroundService.class));
        setContentView(R.layout.activity_main);
        met_dns1 = (MaterialEditText) findViewById(R.id.met_dns1);
        met_dns2 = (MaterialEditText) findViewById(R.id.met_dns2);
        dns1 = (EditText) findViewById(R.id.dns1);
        dns2 = (EditText) findViewById(R.id.dns2);
        connectionImage = (ImageView)findViewById(R.id.connection_status_image);
        connectionText = (TextView)findViewById(R.id.connection_status_text);
        defaultDNSView = (LinearLayout)findViewById(R.id.default_dns_view);
        rate = (Button)findViewById(R.id.rate);
        info = (Button)findViewById(R.id.dnsInfo);
        wrapper = (LinearLayout)findViewById(R.id.activity_main);
        importButton = (ImageButton)findViewById(R.id.default_dns_view_image);
        running_indicator = (View)findViewById(R.id.running_indicator);
        dns1.setText(Preferences.getString(MainActivity.this, "dns1", "185.228.168.168"));
        dns2.setText(Preferences.getString(MainActivity.this, "dns2", "185.228.169.168"));
        startStopButton = (Button) findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = VpnService.prepare(MainActivity.this);
                if (i != null){
                    dialog2 = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.information).setMessage(R.string.vpn_explain)
                            .setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            startActivityForResult(i, 0);
                        }
                    }).show();
                }
                else onActivityResult(0, RESULT_OK, null);
            }
        });
        dns1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(vpnRunning && doStopVPN)stopVpn();
                if (!API.isIP(s.toString(),settingV6)) {
                    met_dns1.setIndicatorState(MaterialEditText.IndicatorState.INCORRECT);
                } else {
                    met_dns1.setIndicatorState(MaterialEditText.IndicatorState.UNDEFINED);
                    Preferences.put(MainActivity.this, settingV6 ? "dns1-v6" :"dns1", s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dns2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(vpnRunning && doStopVPN)stopVpn();
                if (!API.isIP(s.toString(),settingV6)) {
                    met_dns2.setIndicatorState(MaterialEditText.IndicatorState.INCORRECT);
                } else {
                    met_dns2.setIndicatorState(MaterialEditText.IndicatorState.UNDEFINED);
                    Preferences.put(MainActivity.this, settingV6 ? "dns2-v6" : "dns2", s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        getSupportActionBar().setSubtitle(getString(R.string.subtitle_configuring).replace("[[x]]",settingV6 ? "Ipv6" : "Ipv4"));
        if(!Preferences.getBoolean(this, "first_run",true) && !Preferences.getBoolean(this, "rated",false) && new Random().nextInt(100) <= 8){
            new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rateApp(null);
                }
            }).setNegativeButton(R.string.dont_ask_again, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Preferences.put(MainActivity.this, "rated",true);
                    dialog.cancel();
                }
            }).setNeutralButton(R.string.not_now, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).setMessage(R.string.rate_request_text).setTitle(R.string.rate).show();
        }
        Preferences.put(this, "first_run", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vpnRunning = API.checkVPNServiceRunning(this);
        setIndicatorState(vpnRunning);
        registerReceiver(serviceStateReceiver, new IntentFilter(API.BROADCAST_SERVICE_STATUS_CHANGE));
        sendBroadcast(new Intent(API.BROADCAST_SERVICE_STATE_REQUEST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serviceStateReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        View layout = getLayoutInflater().inflate(R.layout.dialog_default_dns, null, false);
        final ListView list = (ListView) layout.findViewById(R.id.defaultDnsDialogList);
        list.setAdapter(new DefaultDNSAdapter());
        list.setDividerHeight(0);
        defaultDnsDialog = new AlertDialog.Builder(this).setView(layout).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle(R.string.default_dns_title).create();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                defaultDnsDialog.cancel();
                List<String> ips = settingV6 ? defaultDNS_V6.get(DefaultDNSKeys_V6.get(position)) : defaultDNS.get(defaultDNSKeys.get(position));
                dns1.setText(ips.get(0));
                dns2.setText(ips.get(1));
            }
        });
    }

    public void openDefaultDNSDialog(View v) {
        defaultDnsDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (!vpnRunning){
                startVpn();
            }else{
                stopVpn();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startVpn() {
        startService(new Intent(this, DNSVpnService.class).putExtra("start_vpn", true));
        vpnRunning = true;
        setIndicatorState(true);
    }

    private void stopVpn() {
        startService(new Intent(this, DNSVpnService.class).putExtra("stop_vpn", true));
        stopService(new Intent(this, DNSVpnService.class));
        vpnRunning = false;
        setIndicatorState(false);
    }

    private class DefaultDNSAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return settingV6 ? defaultDNS_V6.size() : defaultDNS.size();
        }

        @Override
        public Object getItem(int position) {
            return settingV6 ? defaultDNS_V6.get(position) : defaultDNS.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.item_default_dns, parent, false);
            ((TextView) v.findViewById(R.id.text)).setText(settingV6 ? DefaultDNSKeys_V6.get(position) : defaultDNSKeys.get(position));
            v.setTag(getItem(position));
            return v;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(settingV6 ? R.menu.menu_main_v6 : R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_switch_ip_version){
            doStopVPN = false;
            settingV6 = !settingV6;
            invalidateOptionsMenu();
            dns1.setText(Preferences.getString(this,settingV6 ? "dns1-v6" : "dns1", settingV6 ? "2001:4860:4860::8888" : "185.228.168.168"));
            dns2.setText(Preferences.getString(this,settingV6 ? "dns2-v6" : "dns2", settingV6 ? "2001:4860:4860::8844" : "185.228.169.168"));
            dns1.setInputType(InputType.TYPE_CLASS_TEXT);
            dns2.setInputType(InputType.TYPE_CLASS_TEXT);
            getSupportActionBar().setSubtitle(getString(R.string.subtitle_configuring).replace("[[x]]",settingV6 ? "Ipv6" : "Ipv4"));
            doStopVPN = true;
        }
        return super.onOptionsItemSelected(item);
    }
}
