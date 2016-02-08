package com.jenblight.intrepidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jenblight.intrepidapp.net.PostQueue;

public class InternetReceiver extends BroadcastReceiver {
    public InternetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        new PostQueue.uploadQueueTask(context).execute();
    }
}
