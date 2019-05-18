package com.frostnerd.dnschangerlight;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * Terms on usage of my code can be found here: https://git.frostnerd.com/PublicAndroidApps/DnsChanger/blob/master/README.md
 *
 * <p>
 * development@frostnerd.com
 */
public class BackgroundVpnConfigureActivity extends AppCompatActivity {
    private boolean startService = false;
    private static final int REQUEST_CODE = 112;
    private AlertDialog dialog1, dialog2;
    private long requestTime;

    public static void startBackgroundConfigure(Context context, boolean startService) {
        context.startActivity(new Intent(context, BackgroundVpnConfigureActivity.class).putExtra("startService", startService).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        Intent i = getIntent();
        final Intent conf = VpnService.prepare(this);
        startService = i != null && i.getBooleanExtra("startService", false);
        if (conf != null) {
            showDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestTime = System.currentTimeMillis();
                    startActivityForResult(conf, REQUEST_CODE);
                }
            });
        } else {
            if (startService)startService(new Intent(this, DNSVpnService.class).putExtra("start_vpn", true));
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showDialog(DialogInterface.OnClickListener click) {
        dialog1 = new AlertDialog.Builder(this).setTitle(getString(R.string.information) + " - " + getString(R.string.app_name)).setMessage(R.string.vpn_explain)
                .setCancelable(false).setPositiveButton(R.string.ok, click).show();
    }

    @Override
    protected void onDestroy() {
        if(dialog1 != null)dialog1.cancel();
        if(dialog2 != null)dialog2.cancel();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (startService)
                    startService(new Intent(this, DNSVpnService.class).putExtra("start_vpn", true));
                setResult(RESULT_OK);
            } else if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                if(System.currentTimeMillis() - requestTime <= 750){//Most likely the system
                    dialog2 = new AlertDialog.Builder(this).setTitle(getString(R.string.app_name) + " - " + getString(R.string.information)).setMessage(R.string.background_configure_error).setPositiveButton(R.string.open_app, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(BackgroundVpnConfigureActivity.this, MainActivity.class));
                            finish();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    }).show();
                }else finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
