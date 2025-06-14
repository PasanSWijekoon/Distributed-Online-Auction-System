package com.auction.ejb.bean;


import com.auction.ejb.event.AuctionUpdateEventData;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/topic/AuctionUpdatesTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable"),
})
public class AuctionUpdateMDB implements MessageListener {
    private static final Logger LOGGER = Logger.getLogger(AuctionUpdateMDB.class.getName());

    @Inject
    private Event<AuctionUpdateEventData> auctionUpdateEvent;

    public AuctionUpdateMDB() {}

    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            try {
                String updateType = mapMessage.getString("updateType");
                String itemId = mapMessage.getString("itemId");

                LOGGER.log(Level.INFO, "MDB Received JMS: Type=''{0}'', ItemId=''{1}'' (for WebSocket via CDI Event)",
                        new Object[]{updateType, itemId});

                Map<String, Object> eventDataMap = new HashMap<>();
                Enumeration<?> jmsKeys = mapMessage.getMapNames();
                while(jmsKeys.hasMoreElements()) {
                    String jmsKey = (String) jmsKeys.nextElement();
                    eventDataMap.put(jmsKey, mapMessage.getObject(jmsKey));
                }

                if (!eventDataMap.containsKey("active")) {
                    if (mapMessage.propertyExists("active")) {
                        eventDataMap.put("active", mapMessage.getBoolean("active"));
                    } else {
                        LOGGER.warning("MDB: 'active' property missing in JMS message for item " + itemId + ". Defaulting for event data.");
                        eventDataMap.put("active", "AUCTION_CREATED".equals(updateType) || "NEW_HIGH_BID".equals(updateType));
                    }
                } else if (!(eventDataMap.get("active") instanceof Boolean)) {
                    eventDataMap.put("active", mapMessage.getBoolean("active"));
                }


                if (!eventDataMap.containsKey("name")) {
                    if (mapMessage.propertyExists("name")) {
                        eventDataMap.put("name", mapMessage.getString("name"));
                    } else {
                        LOGGER.warning("MDB: 'name' property missing in JMS message for item " + itemId);
                    }
                }


                StringBuilder sb = new StringBuilder("MDB: Final eventDataMap for WebSocket: {");
                for (Map.Entry<String, Object> entry : eventDataMap.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue())
                            .append(" (type: ").append(entry.getValue() == null ? "null" : entry.getValue().getClass().getSimpleName()).append("), ");
                }
                if (!eventDataMap.isEmpty()) { sb.setLength(sb.length() - 2); }
                sb.append("}");
                LOGGER.log(Level.INFO, sb.toString());

                if ("NEW_HIGH_BID".equals(updateType) || "AUCTION_ENDED".equals(updateType) || "AUCTION_CREATED".equals(updateType)) {
                    AuctionUpdateEventData eventPayload = new AuctionUpdateEventData("auctionUpdate", eventDataMap);
                    auctionUpdateEvent.fire(eventPayload);
                    LOGGER.log(Level.INFO, "MDB Fired CDI event for: {0}", itemId);
                } else {
                    LOGGER.log(Level.INFO, "MDB: Received unhandled updateType: {0}", updateType);
                }

            } catch (JMSException e) {
                LOGGER.log(Level.SEVERE, "MDB: Error processing JMS message", e);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "MDB: Error creating or firing CDI event", e);
            }
        } else {
            LOGGER.log(Level.WARNING, "MDB: Received message of unexpected type: {0}", message.getClass().getName());
        }
    }
}