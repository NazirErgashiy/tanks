package uz.nazir.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uz.nazir.game.Game;
import uz.nazir.network.data.NetworkEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class SessionManager {
    private Map<String, WebSocketSession> sessions = new HashMap<>();
    private List<String> sessionsIds = new CopyOnWriteArrayList<>();

    private ObjectMapper mapper = new ObjectMapper();

    @Getter
    @Setter
    private Game game;

    public synchronized void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sessionsIds.add(session.getId());
    }

    public synchronized void sendMessage(String sessionId, Object message) {
        try {
            String json = mapper.writeValueAsString(message);
            sessions.get(sessionId).sendMessage(new TextMessage(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessageToEveryone(Object message) {
        try {
            String json = mapper.writeValueAsString(message);
            sessionsIds.forEach(s ->
            {
                try {
                    sessions.get(s).sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessageToEveryone(Object message, String sessionId) {
        try {
            String json = mapper.writeValueAsString(message);
            sessionsIds.forEach(s ->
            {
                if (!sessionId.equals(s)) {
                    try {
                        sessions.get(s).sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void handleConnect(WebSocketSession session) {
        game.onNewPlayerConnected(session);
    }

    public synchronized void handleMessage(WebSocketSession session, TextMessage message) {
        try {
            NetworkEntity networkEntity = mapper.readValue(message.getPayload(), NetworkEntity.class);
            if (networkEntity == null) {
                log.warn("NULL NETWORK ENTITY");
                return;
            }
            game.onNetworkUpdate(session,networkEntity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public synchronized void exclude(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionsIds.remove(session.getId());
    }
}
