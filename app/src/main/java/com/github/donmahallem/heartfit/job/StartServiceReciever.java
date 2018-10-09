package com.github.donmahallem.heartfit.job;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartServiceReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        JobUtil.scheduleJob(context);
    }
}