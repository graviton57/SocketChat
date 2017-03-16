package com.havrylyuk.chat.event;

import com.havrylyuk.chat.model.Message;

import java.util.List;

/**
 * Created by Igor Havrylyuk on 15.03.2017.
 */

public class GetMessagesListEvent {

    private List<Message> messages;

    public GetMessagesListEvent(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
