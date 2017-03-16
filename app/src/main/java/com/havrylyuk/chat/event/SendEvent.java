package com.havrylyuk.chat.event;

import com.havrylyuk.chat.model.Message;

/**
 * Created by Igor Havrylyuk on 16.03.2017.
 */

public class SendEvent {

    private Message message;

    public SendEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
