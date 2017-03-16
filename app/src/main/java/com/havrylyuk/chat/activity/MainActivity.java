package com.havrylyuk.chat.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.havrylyuk.chat.R;
import com.havrylyuk.chat.adapter.SimpleRecyclerAdapter;
import com.havrylyuk.chat.event.SendEvent;
import com.havrylyuk.chat.model.Message;
import com.havrylyuk.chat.event.CallbakEvent;
import com.havrylyuk.chat.event.ServiceEvent;
import com.havrylyuk.chat.service.ChatService;
import com.havrylyuk.chat.service.ChatService.LocalBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 *
 * Created by Igor Havrylyuk on 15.03.2017.
 */
public class MainActivity extends AppCompatActivity {

    public static final String USER_NAME = "Anonymous";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private SimpleRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isBound;
    private ChatService chatService;
    private EditText messageInput;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();
        adapter = new SimpleRecyclerAdapter();
        setupRecyclerView();
        setupSendBuuton();
        setupMessageInput();
        EventBus.getDefault().register(this);
        bindService(new Intent(this, ChatService.class), sConn , 0);
    }

    private void setupMessageInput() {
        messageInput = (EditText) findViewById(R.id.message_input);
        if (messageInput != null) {
            messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                    if (id == R.id.send || id == EditorInfo.IME_NULL) {
                        attemptSend();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void attemptSend() {
        if (!isBound) return;
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            messageInput.requestFocus();
            return;
        }
        messageInput.setText("");
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        if (chatService.getSocket() != null) {
            chatService.getSocket().emit("identify", USER_NAME);
            chatService.getSocket().emit("message", message);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (isBound) {
            unbindService(sConn);
            isBound = false;
        }
        super.onDestroy();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupSendBuuton() {
        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Sending...", Toast.LENGTH_SHORT).show();
                    attemptSend();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave:
                Toast.makeText(MainActivity.this, "Chat Service is stopped.", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new ServiceEvent(ChatService.STOP_SERVICE));
                return true;
            case R.id.action_start_bot:
                EventBus.getDefault().post(new ServiceEvent(ChatService.START_BOT));
                return true;
            case R.id.action_stop_bot:
                EventBus.getDefault().post(new ServiceEvent(ChatService.STOP_BOT));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CallbakEvent event) {
        adapter.addAll(event.getMessages());
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(Message event) {
        adapter.append(event);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SendEvent event) {
        if (chatService.getSocket() != null) {
            chatService.getSocket().emit("identify", event.getMessage().getUserName());
            chatService.getSocket().emit("message", event.getMessage().getMessage());
        }
    }

    private ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected()");
            LocalBinder binder = (LocalBinder) service;
            chatService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDisconnected()");
            isBound = false;
        }
    };


}
