package uz.nazir;

import uz.nazir.game.Game;
import uz.nazir.network.SessionManager;
import uz.nazir.network.client.MainWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class Application {

    public static final SessionManager sessionManager = new SessionManager();

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        System.setProperty("java.awt.headless", "false");

        System.setProperty("resources.path", args[0]);

        Environment environment = context.getEnvironment();
        String[] profiles = environment.getActiveProfiles();

        if (profiles.length == 1) {
            if (profiles[0].equals("server")) {
                Game tanks = new Game(sessionManager, true);
                sessionManager.setGame(tanks);
                tanks.start();
            }
            if (profiles[0].equals("client")) {
                var socket = context.getBean(MainWebSocketClient.class);
                Game tanks = new Game(sessionManager, false);
                sessionManager.setGame(tanks);
                socket.connect();
                tanks.start();
            }
        }
    }
}