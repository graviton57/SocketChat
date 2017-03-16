package com.havrylyuk.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.havrylyuk.chat.BuildConfig;
import com.havrylyuk.chat.event.SendEvent;
import com.havrylyuk.chat.model.Message;
import com.havrylyuk.chat.event.CallbakEvent;
import com.havrylyuk.chat.event.ServiceEvent;
import com.havrylyuk.chat.util.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



/**
 *
 * Created by Igor Havrylyuk on 15.03.2017.
 */

public class ChatService extends Service {

    private static final String LOG_TAG = ChatService.class.getSimpleName();
    private final static String[] worlds = new String[]{"Как дела",
            "Привет","Вы серйозно?","Почему так тихо?",
            "Что будет если я и дальше буду спамить и список сообщений станет очень большой?",
            "Будете грузить весь список?","Как дела?",
            "для чего 'roster' на сервере?","Меня забанят?","Реально круто!","Я так и знал...","To be continued..."};
    public static final int STOP_SERVICE = 0;
    public static final int START_SERVICE = 1;
    public static final int START_BOT = 3;
    public static final int STOP_BOT = 4;

    private final IBinder binder = new LocalBinder();
    private List<Message> messages;
    private boolean isBind = false;
    private Timer botTimer;
    private AtomicInteger counter = new AtomicInteger();

    private Socket socket;
    private boolean isConnected = false;

    public class LocalBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        Log.d(LOG_TAG,"ChatService onCreate");
        messages = new ArrayList<>();
        setupSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "ChatService onStartCommand flag=" + flags + " startId=" + startId);
        return START_STICKY_COMPATIBILITY;
    }

    private void setupSocket() {
        Log.d(LOG_TAG, "ChatService setupSocket()");
        try {
            IO.Options options = new IO.Options();
            options.timeout = 10000;
            options.reconnection = true;
            options.reconnectionAttempts = 10;
            options.reconnectionDelay = 1000;
            options.forceNew = true;
            socket = IO.socket(BuildConfig.CHAT_SERVER_URL, options);
            openSocket();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBind = true;
        Log.d(LOG_TAG,"ChatService onBind");
        EventBus.getDefault().post(new CallbakEvent(messages));
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBind = false;
        Log.d(LOG_TAG,"ChatService onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        isBind = true;
        Log.d(LOG_TAG,"ChatService onRebind");
        super.onRebind(intent);
        EventBus.getDefault().post(new CallbakEvent(messages));
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"ChatService onDestroy");
        EventBus.getDefault().unregister(this);
        closeSocket();
        stopBot();
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(ServiceEvent event) {
        Log.d(LOG_TAG, "ChatService on ServiceEvent =" + event.getEvent());
        switch (event.getEvent()){
            case STOP_BOT:
                stopBot();
                break;
            case START_BOT:
                startBot();
                break;
            case STOP_SERVICE:
                stopSelf();
                break;
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void openSocket() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            socket.on("message", onNewMessage);
            socket.connect();
        }
    }

    public void closeSocket() {
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off("message", onNewMessage);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
              Log.d(LOG_TAG,  "Socket Connected");
              // EventBus.getDefault().post(new ServiceEvent("Connected"));
              isConnected = true;
        }
    };


    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG, "Socket diconnected");
            //EventBus.getDefault().post(new ServiceEvent("diconnected"));
            isConnected = false;
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(LOG_TAG, "Socket Error connecting");
           // EventBus.getDefault().post(new ServiceEvent("Error connecting"));
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(LOG_TAG, "Socket onNewMessage event");
            JSONObject data = (JSONObject) args[0];
            String username;
            String message;
            try {
                username = data.getString("name");
                if (TextUtils.isEmpty(username)) {
                    username = "Anonymous";
                }
                message = data.getString("text");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
                return;
            }
            messages.add(new Message(username, message));
            if (isBind){
                EventBus.getDefault().postSticky(new Message(username, message));
            } else {
                Utility.showNotification(getApplicationContext(), message, username);
            }
        }
    };

    private void startBot() {
        stopBot();
        botTimer = new Timer();
        botTimer.scheduleAtFixedRate(new BotTask(counter), 0, 60000*30);
    }

    private void stopBot() {
        if (botTimer != null) {
            botTimer.cancel();
            botTimer = null;
        }
    }


    private  class BotTask extends TimerTask {

        private AtomicInteger current;

        public BotTask(AtomicInteger current) {
            this.current = current;
        }

        @Override
        public void run() {
            Log.d(LOG_TAG,"ChatService create Chatterbot message");
            int indexName = new Random().nextInt(messages.size());
            String name = messages.get(indexName).getUserName();
            int indexWorld = new Random().nextInt(worlds.length);
            String world = worlds[indexWorld];
            if (isBind){
                Message message = new Message(current.addAndGet(1), "Chatterbot", name + " " + world);
                EventBus.getDefault().postSticky(new SendEvent(message));
            }
        }
    }
}
