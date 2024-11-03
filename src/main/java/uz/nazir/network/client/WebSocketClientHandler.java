package uz.nazir.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import uz.nazir.network.data.DataCall;
import uz.nazir.network.data.NetworkEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import uz.nazir.Application;

@Slf4j
@Component
public class WebSocketClientHandler implements WebSocketHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("CLIENT: CONNEXION ESTABLISE : DETAILS: {}", session.getRemoteAddress());
        Application.sessionManager.registerSession(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String s = (String) message.getPayload();
        NetworkEntity entity = objectMapper.readValue(s, NetworkEntity.class);
        if (entity.getDataCall() == DataCall.CREATE)
            log.info("SERVER: MESSAGE: {}", message.getPayload());

        Application.sessionManager.handleMessage(session, (TextMessage) message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("CLIENT: ERROR TRANSPORT");
        exception.printStackTrace();
        Application.sessionManager.exclude(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("CLIENT: CONNECTION CLOSE");
        log.info(closeStatus.getReason());
        Application.sessionManager.exclude(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
