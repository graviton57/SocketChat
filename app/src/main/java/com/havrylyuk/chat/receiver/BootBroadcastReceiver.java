package com.havrylyuk.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.havrylyuk.chat.service.ChatService;

/**
 * Created by Igor Havrylyuk on 16.03.2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
         Intent serviceIntent = new Intent(context, ChatService.class);
         context.startService(serviceIntent);
         }
    }
}
