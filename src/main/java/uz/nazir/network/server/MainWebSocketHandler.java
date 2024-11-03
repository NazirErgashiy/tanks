package uz.nazir.network.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import uz.nazir.game.EntityType;
import uz.nazir.network.data.DataCall;
import uz.nazir.network.data.NetworkEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import uz.nazir.Application;

@Slf4j
public class MainWebSocketHandler implements WebSocketHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("SERVER: CONNECTION DETAILS: {}", session.getRemoteAddress());
        Application.sessionManager.registerSession(session);
        Application.sessionManager.handleConnect(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        String s = (String) message.getPayload();
        NetworkEntity entity = objectMapper.readValue(s, NetworkEntity.class);
        if (entity.getDataCall() == DataCall.CREATE)
            log.info("SERVER: MESSAGE: {}", message.getPayload());

        if (entity.getDataCall() == DataCall.RESPAWN && entity.getEntityType() == EntityType.PLAYER) {
            Application.sessionManager.handleConnect(session);
            return;
        }
        Application.sessionManager.handleMessage(session, (TextMessage) message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("SERVER: ERROR TRANSPORT");
        exception.printStackTrace();
        Application.sessionManager.exclude(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("SERVER: CONNECTION CLOSE");
        log.info(closeStatus.getReason());
        Application.sessionManager.exclude(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
