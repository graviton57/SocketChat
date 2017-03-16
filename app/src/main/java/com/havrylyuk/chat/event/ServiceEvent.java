package com.havrylyuk.chat.event;

/**
 *
 * Created by Igor Havrylyuk on 15.03.2017.
 */
public class ServiceEvent {

    private int event;

    public ServiceEvent(int event) {
        this.event = event;
    }

    public int getEvent() {
        return event;
    }
}
