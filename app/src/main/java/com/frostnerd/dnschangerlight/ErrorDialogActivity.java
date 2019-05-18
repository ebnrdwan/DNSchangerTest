package com.frostnerd.dnschangerlight;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * Terms on usage of my code can be found here: https://git.frostnerd.com/PublicAndroidApps/DnsChanger/blob/master/README.md
 *
 * <p>
 * development@frostnerd.com
 */
public class ErrorDialogActivity extends Activity {
    private static final String LOG_TAG = "[ErrorDialogActivity]";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String crashReport = getIntent() != null ? getIntent().getStringExtra("stacktrace") : "";
        new AlertDialog.Builder(this).setTitle(getString(R.string.error) + " - " + getString(R.string.app_name)).setMessage(R.string.vpn_error_explain)
                .setCancelable(false).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setNeutralButton(R.string.send_crash_report, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","support@frostnerd.com", null));
                String body = "\n\n\n\n\n\n\nSystem:\nApp version: " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")\n"+
                        "Android: " + Build.VERSION.SDK_INT + " (" + Build.VERSION.RELEASE + " - " + Build.VERSION.CODENAME + ")\n\n\nStacktrace:\n" + crashReport;
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - crash");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "support@frostnerd.com");
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_developer)));
            }
        }).show();
    }

    public static void show(Context context,Throwable t){
        context.startActivity(new Intent(context, ErrorDialogActivity.class).putExtra("stacktrace",stacktraceToString(t)));
    }

    public static String stacktraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String res = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
