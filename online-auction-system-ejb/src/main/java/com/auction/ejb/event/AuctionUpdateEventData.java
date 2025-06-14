package com.auction.ejb.event;

import java.io.Serializable;
import java.util.Map;

public class AuctionUpdateEventData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Object> eventData;
    private final String eventType;

    public AuctionUpdateEventData(String eventType, Map<String, Object> eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }

    public String getEventType() {
        return eventType;
    }
}
