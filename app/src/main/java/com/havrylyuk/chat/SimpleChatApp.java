package com.havrylyuk.chat;

import android.app.Application;
import android.content.Intent;

import com.havrylyuk.chat.service.ChatService;


/**
 * Created by Igor Havrylyuk on 15.03.2017.
 */

public class SimpleChatApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, ChatService.class));
    }
}
