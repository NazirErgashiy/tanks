package uz.nazir.network.server.config;

import uz.nazir.network.server.MainWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final Environment environment;

    @Autowired
    public WebSocketConfig(Environment environment) {
        this.environment = environment;
    }

    // Overriding a method which register the socket
    // handlers into a Registry
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry webSocketHandlerRegistry) {
        // For adding a Handler we give the Handler class we
        // created before with End point Also we are managing
        // the CORS policy for the handlers so that other
        // domains can also access the socket
        String server = environment.getRequiredProperty("server.enabled");
        if (server.equals("true")) {
            webSocketHandlerRegistry
                    .addHandler(getMainWebSocketHandler(), "")
                    .setAllowedOrigins("*");
        }
    }

    @Bean
    public MainWebSocketHandler getMainWebSocketHandler() {
        return new MainWebSocketHandler();
    }
}