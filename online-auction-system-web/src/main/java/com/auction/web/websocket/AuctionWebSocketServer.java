package com.auction.web.websocket;

import com.auction.ejb.event.AuctionUpdateEventData;
import com.auction.web.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint("/auction-websocket")
@Singleton
public class AuctionWebSocketServer {

    private static final Logger LOGGER = Logger.getLogger(AuctionWebSocketServer.class.getName());
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOGGER.log(Level.INFO, "WebSocket connection opened: {0}. Total sessions: {1}",
                new Object[]{session.getId(), sessions.size()});

        try {
            Map<String, String> connectionMessage = Collections.singletonMap("status", "WebSocket connected successfully to auction updates!");
            session.getAsyncRemote().sendText(gson.toJson(connectionMessage));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send connection confirmation to session " + session.getId(), e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        sessions.remove(session);
        LOGGER.log(Level.INFO, "WebSocket connection closed: {0}. Reason: {1}. Total sessions: {2}",
                new Object[]{session.getId(), closeReason.getReasonPhrase(), sessions.size()});
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        LOGGER.log(Level.SEVERE, "WebSocket error for session " + session.getId() + ": " + throwable.getMessage(), throwable);
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close session " + session.getId() + " after error.", e);
        }
    }

    public void onAuctionUpdate(@Observes AuctionUpdateEventData eventPayload) {
        if (eventPayload == null || eventPayload.getEventData() == null) {
            LOGGER.warning("Received null auction update event payload.");
            return;
        }

        LOGGER.log(Level.INFO, "WebSocket: Received auction update event to broadcast: {0}", eventPayload.getEventType());
        String jsonData = gson.toJson(eventPayload.getEventData());
        broadcast(jsonData);
    }

    private void broadcast(String message) {
        Set<Session> sessionsSnapshot;
        synchronized (sessions) {
            sessionsSnapshot = new HashSet<>(sessions);
        }

        LOGGER.log(Level.INFO, "Broadcasting message to {0} WebSocket sessions.", sessionsSnapshot.size());
        sessionsSnapshot.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                    LOGGER.log(Level.FINE, "Sent message to session: {0}", session.getId());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to send message to session " + session.getId() + ", removing session.", e);
                    sessions.remove(session);
                    try { session.close(); } catch (IOException ioException) { /* ignore */ }
                }
            } else {
                LOGGER.log(Level.INFO, "Session {0} was closed, removing from broadcast list.", session.getId());
                sessions.remove(session);
            }
        });
    }
}
