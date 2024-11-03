package uz.nazir.network.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Component
public class MainWebSocketClient {
    private final WebSocketClient webSocketClient;

    private final WebSocketClientHandler webSocketClientHandler;
    private final Environment env;

    @Autowired
    public MainWebSocketClient(Environment env, WebSocketClientHandler webSocketClientHandler) {
        webSocketClient = new StandardWebSocketClient();
        this.webSocketClientHandler = webSocketClientHandler;
        this.env = env;
    }

    public void connect() {
        String uri = env.getRequiredProperty("client.url");
        webSocketClient.doHandshake(webSocketClientHandler, uri);
    }
}
