package com.frostnerd.dnschangerlight;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.NotificationCompat;

import com.frostnerd.dnschangerlight.API.API;
import com.frostnerd.dnschangerlight.API.Preferences;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
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
public class DNSVpnService extends VpnService {
    private boolean run = true, isRunning = false, stopped = false;
    private Thread thread;
    private ParcelFileDescriptor tunnelInterface;
    private Builder builder = new Builder();
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = 112;
    private Handler handler = new Handler();
    private String dns1, dns2, dns1_v6, dns2_v6;
    private BroadcastReceiver stateRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastServiceState(isRunning);
        }
    };

    private void broadcastServiceState(boolean vpnRunning) {
        sendBroadcast(new Intent(API.BROADCAST_SERVICE_STATUS_CHANGE).putExtra("vpn_running", vpnRunning));
    }

    private void updateNotification() { //TODO Fix Bug: Actions are not properly removed
        initNotification();
        if (stopped || notificationBuilder == null || !Preferences.getBoolean(this, "setting_show_notification", false) || notificationManager == null){
            if(notificationManager != null)notificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        android.support.v4.app.NotificationCompat.Action a1 = notificationBuilder.mActions.get(0);
        a1.icon = isRunning ? R.drawable.ic_stat_pause : R.drawable.ic_stat_resume;
        a1.title = getString(isRunning ? R.string.action_pause : R.string.action_resume);
        a1.actionIntent = PendingIntent.getService(this, 0, new Intent(this, DNSVpnService.class).setAction(new Random().nextInt(50) + "_action").putExtra(isRunning ? "stop_vpn" : "start_vpn", true), PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.mActions.get(1).actionIntent = PendingIntent.getService(this, 1, new Intent(this, DNSVpnService.class)
                .setAction(API.randomString(80) + "_action").putExtra("destroy", true), PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentTitle(getString(isRunning ? R.string.active : R.string.paused));
        notificationBuilder.setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().
                bigText("DNS 1: " + dns1 + "\nDNS 2: " + dns2 + "\nDNSV6 1: " + dns1_v6 + "\nDNSV6 2: " + dns2_v6));
        notificationBuilder.setSubText(getString(isRunning ? R.string.notification_running : R.string.notification_paused));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (notificationManager != null && notificationBuilder != null && !stopped)
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        }, 10);
    }

    private void initNotification() {
        if (notificationBuilder == null) {
            notificationBuilder = new android.support.v7.app.NotificationCompat.Builder(this);
            notificationBuilder.setSmallIcon(R.drawable.ic_stat_small_icon); //TODO Update Image
            notificationBuilder.setContentTitle(getString(R.string.app_name));
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            notificationBuilder.setAutoCancel(false);
            notificationBuilder.setOngoing(true);
            notificationBuilder.addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_stat_pause, getString(R.string.action_pause), null));
            notificationBuilder.addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_stat_stop, getString(R.string.action_stop), null));
            notificationBuilder.setUsesChronometer(true);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNotification();
        registerReceiver(stateRequestReceiver, new IntentFilter(API.BROADCAST_SERVICE_STATE_REQUEST));
    }

    @Override
    public void onDestroy() {
        stopped = true;
        run = false;
        if (thread != null) thread.interrupt();
        thread = null;
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager = null;
        notificationBuilder = null;
        unregisterReceiver(stateRequestReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("stop_vpn", false)) {
                if (thread != null) {
                    run = false;
                    thread.interrupt();
                    thread = null;
                }
            } else if (intent.getBooleanExtra("start_vpn", false)) {
                if (thread != null) {
                    run = false;
                    thread.interrupt();
                }
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DatagramChannel tunnel = null;
                        DatagramSocket tunnelSocket = null;
                        try {
                            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                                @Override
                                public void uncaughtException(Thread t, Throwable e) {
                                    System.out.println(e);
                                    ErrorDialogActivity.show(DNSVpnService.this, e);
                                    stopSelf();
                                }
                            });
                            initNotification();
                            if(notificationBuilder != null)notificationBuilder.setWhen(System.currentTimeMillis());
                            dns1 = Preferences.getString(DNSVpnService.this, "dns1", "185.228.168.168");
                            dns2 = Preferences.getString(DNSVpnService.this, "dns1", "185.228.169.168");
                            dns1_v6 = Preferences.getString(DNSVpnService.this, "dns1-v6", "2001:4860:4860::8888");
                            dns2_v6 = Preferences.getString(DNSVpnService.this, "dns2-v6", "2001:4860:4860::8844");
                            tunnelInterface = builder.setSession("DnsChanger").addAddress("172.31.255.250", 30)
                                    .addAddress(API.randomLocalIPv6Address(), 48).addDnsServer(dns1).addDnsServer(dns2)
                                    .addDnsServer(dns1_v6).addDnsServer(dns2_v6).establish();
                            tunnel = DatagramChannel.open();
                            tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                            protect(tunnelSocket=tunnel.socket());
                            isRunning = true;
                            sendBroadcast(new Intent(API.BROADCAST_SERVICE_STATUS_CHANGE).putExtra("vpn_running", true));
                            updateNotification();
                            try {
                                while (run) {
                                    Thread.sleep(250);
                                }
                            } catch (InterruptedException e2) {

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            isRunning = false;
                            sendBroadcast(new Intent(API.BROADCAST_SERVICE_STATUS_CHANGE).putExtra("vpn_running", false));
                            updateNotification();
                            if (tunnelInterface != null) try {
                                tunnelInterface.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(tunnel != null)try{
                                tunnel.close();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            if(tunnelSocket != null)try{
                                tunnelSocket.close();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                run = true;
                thread.start();
            } else if (intent.getBooleanExtra("destroy", false)) {
                stopped = true;
                if (thread != null) {
                    run = false;
                    thread.interrupt();
                }
                stopSelf();
            }
        }
        updateNotification();
        return START_STICKY;
    }
}
