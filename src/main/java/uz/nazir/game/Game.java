package uz.nazir.game;

import uz.nazir.Application;
import uz.nazir.debug.DebugConfiguration;
import uz.nazir.display.Display;
import uz.nazir.game.entities.Player;
import uz.nazir.game.entities.Shell;
import uz.nazir.game.level.Level;
import uz.nazir.graphics.TextureAtlas;
import uz.nazir.input.Input;
import uz.nazir.network.SessionManager;
import uz.nazir.network.data.DataCall;
import uz.nazir.network.data.Direction;
import uz.nazir.network.data.NetworkEntity;
import uz.nazir.network.data.Vector2;
import uz.nazir.utils.Time;
import org.springframework.web.socket.WebSocketSession;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game implements Runnable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR = 0xFF000000;
    public static final int NUM_BUFFERS = 3;

    public static final float UPDATE_RATE = 60.0f;
    public static final float UPDATE_INTERVAL = Time.SECOND / UPDATE_RATE;
    public static final long IDLE_TIME = 1;

    public static final String ATLAS_FILE_NAME = "texture_atlas.png";
    public static long DELTA_TIME = 0;
    public static boolean isServer;
    private static List<Entity> objects = new CopyOnWriteArrayList<>();
    private static List<Player> players = new CopyOnWriteArrayList<>();
    private boolean running;
    private Thread gameThread;
    private Graphics2D graphics;
    private Input input;
    private TextureAtlas atlas;
    private Level level;
    private SessionManager sessionManager;
    private long tick = 1000L;
    private final long tickSaved = tick;
    private int connects = 0;

    public Game(SessionManager sessionManager, boolean isServer) {
        this.sessionManager = sessionManager;
        Game.isServer = isServer;

        running = false;
        Display.create(WIDTH, HEIGHT, TITLE, CLEAR_COLOR, NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        atlas = new TextureAtlas(ATLAS_FILE_NAME);

        level = new Level(atlas, "level_0.txt");
    }

    public static void removePlayer(Player player) {
        players.remove(player);
        NetworkEntity networkEntity = NetworkEntity.builder()
                .id(player.getNetworkId())
                .position(new Vector2(0, 0))
                .direction(Direction.NORTH)
                .dataCall(DataCall.DESTROY)
                .entityType(EntityType.PLAYER)
                .isOwner(false)
                .build();

        Application.sessionManager.sendMessageToEveryone(networkEntity);
    }

    public static void addEntity(Entity entity) {
        objects.add(entity);
    }

    public static void removeEntity(Entity entity) {
        objects.remove(entity);
    }

    private Vector2 randomPos() {
        if (connects % 2 == 0) {
            return new Vector2(50, 50);
        }
        return new Vector2(300, 300);
    }

    public synchronized void onNewPlayerConnected(WebSocketSession session) {
        //float x = new Random().nextInt(100, 400);
        //float y = new Random().nextInt(100, 400);

        connects += 1;
        Vector2 spawnPos = randomPos();
        float x = spawnPos.x;
        float y = spawnPos.y;

        Player newPlayer = new Player(x, y, 2, 1.5f, atlas, false);
        UUID id = UUID.randomUUID();
        newPlayer.setNetworkId(id);
        newPlayer.direction = Direction.NORTH;
        players.add(newPlayer);

        NetworkEntity networkEntity = NetworkEntity.builder()
                .id(id)
                .entityType(EntityType.PLAYER)
                .dataCall(DataCall.CREATE)
                .direction(newPlayer.direction)
                .position(new Vector2(newPlayer.x, newPlayer.y))
                .isOwner(true)
                .build();

        sessionManager.sendMessage(session.getId(), networkEntity);

        networkEntity.setOwner(false);
        sessionManager.sendMessageToEveryone(networkEntity, session.getId());

        players.forEach(player1 -> {
            if (id != player1.getNetworkId()) {
                NetworkEntity oldPlayers = NetworkEntity.builder()
                        .id(player1.getNetworkId())
                        .entityType(EntityType.PLAYER)
                        .dataCall(DataCall.CREATE)
                        .direction(player1.direction)
                        .position(new Vector2(player1.x, player1.y))
                        .isOwner(false)
                        .build();

                sessionManager.sendMessage(session.getId(), oldPlayers);
            }
        });
    }

    public synchronized void onNetworkUpdate(WebSocketSession session, NetworkEntity networkEntity) {
        //System.out.println(networkEntity.toString());

        switch (networkEntity.getEntityType()) {
            case PLAYER:
                if (networkEntity.getDataCall() == DataCall.CREATE) {
                    Player player1 = new Player(networkEntity.getPosition().x, networkEntity.getPosition().y, 2, 1.5f, atlas, false);
                    player1.setNetworkId(networkEntity.getId());
                    player1.setOwnerSession(session);
                    player1.setOwner(networkEntity.isOwner());
                    player1.setDirection(networkEntity.getDirection());
                    players.add(player1);
                }
                if (networkEntity.getDataCall() == DataCall.UPDATE) {
                    if (Game.isServer) {
                        players.forEach(player1 -> {
                            if (player1.getNetworkId().equals(networkEntity.getId())) {
                                player1.x = networkEntity.getPosition().x;
                                player1.y = networkEntity.getPosition().y;
                                player1.setDirection(networkEntity.getDirection());
                            }
                        });
                    } else {
                        players.forEach(player1 -> {
                            if (player1.getNetworkId().equals(networkEntity.getId()) && !player1.isOwner()) {
                                player1.x = networkEntity.getPosition().x;
                                player1.y = networkEntity.getPosition().y;
                                player1.setDirection(networkEntity.getDirection());
                                player1.collisionBox.x = (int) networkEntity.getPosition().x;
                                player1.collisionBox.y = (int) networkEntity.getPosition().y;
                            }
                        });
                    }
                }
                if (networkEntity.getDataCall() == DataCall.DESTROY) {
                    players.forEach(player -> {
                        if (player.getNetworkId().equals(networkEntity.getId())) {
                            player.x = 0;
                            player.y = 0;
                            player.collisionBox.x = 0;
                            player.collisionBox.y = 0;
                            players.remove(player);
                            objects.remove(player);
                        }
                    });
                }
                break;
            case SHELL:
                if (networkEntity.getDataCall() == DataCall.CREATE) {
                    Shell shell = new Shell(null, 2, networkEntity.getPosition().x, networkEntity.getPosition().y, atlas, networkEntity.getOwnerId());
                    //Shell shell = Spawn.instantiate(Shell.class, Shell.Heading.WEST, 2, networkEntity.getPosition().x, networkEntity.getPosition().y + SHELL_SPAWN_OFFSET - 3, atlas, null);
                    shell.setDirection(networkEntity.getDirection());
                    shell.setNetworkId(networkEntity.getId());
                    //objects.add(shell);

                    if (Game.isServer)
                        sessionManager.sendMessageToEveryone(networkEntity);//Forward back to client and for others
                }
                if (networkEntity.getDataCall() == DataCall.UPDATE) {
                    objects.forEach(entity -> {
                        if (entity.getNetworkId().equals(networkEntity.getId())) {
                            entity.x = networkEntity.getPosition().x;
                            entity.y = networkEntity.getPosition().y;
                            entity.direction = networkEntity.getDirection();
                        }
                    });
                }
                if (networkEntity.getDataCall() == DataCall.DESTROY) {
                    objects.forEach(entity -> {
                        if (entity.getNetworkId().equals(networkEntity.getId())) {
                            entity.x = 0;
                            entity.y = 0;
                            removeEntity(entity);
                        }
                    });
                }
                break;
            case MAP:
                level.setTileMap(networkEntity.getMap());
                break;
        }
    }

    public void sendNetworkDataPerTick() {
        if (isServer) {
            objects.forEach(entity -> {
                        if (entity instanceof Shell) {
                            NetworkEntity networkEntity = NetworkEntity.builder()
                                    .id(entity.getNetworkId())
                                    .position(new Vector2(entity.x, entity.y))
                                    .direction(entity.direction)
                                    .dataCall(DataCall.UPDATE)
                                    .entityType(EntityType.SHELL)
                                    .build();

                            sessionManager.sendMessageToEveryone(networkEntity);
                        }
                    }
            );

            players.forEach(player1 -> {
                NetworkEntity networkEntity = NetworkEntity.builder()
                        .id(player1.getNetworkId())
                        .position(new Vector2(player1.x, player1.y))
                        .direction(player1.getDirection())
                        .dataCall(DataCall.UPDATE)
                        .entityType(EntityType.PLAYER)
                        .isOwner(false)
                        .build();

                sessionManager.sendMessageToEveryone(networkEntity);
            });

            NetworkEntity map = NetworkEntity.builder()
                    .map(level.getTileMap())
                    .isOwner(false)
                    .dataCall(DataCall.UPDATE)
                    .entityType(EntityType.MAP)
                    .build();

            sessionManager.sendMessageToEveryone(map);

        } else {
            players.forEach(player1 -> {
                if (player1.isOwner()) {
                    NetworkEntity networkEntity = NetworkEntity.builder()
                            .id(player1.getNetworkId())
                            .position(new Vector2(player1.x, player1.y))
                            .direction(player1.direction)
                            .dataCall(DataCall.UPDATE)
                            .entityType(EntityType.PLAYER)
                            .isOwner(false)
                            .build();

                    sessionManager.sendMessageToEveryone(networkEntity);
                }
            });
        }
    }

    public synchronized void start() {
        if (running)
            return;

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running)
            return;

        running = false;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cleanUp();
    }

    private void update() {
        if (isServer) {
            players.forEach(player1 -> {
                player1.update(null);
            });
        } else {
            players.forEach(player1 -> {
                if (player1.isOwner()) {
                    player1.update(input);
                } else {
                    //player1.update(null);
                }
            });

            if (input.getKey(KeyEvent.VK_SPACE)) {
                AtomicBoolean alive = new AtomicBoolean(false);
                players.forEach(player -> {
                    if (player.isOwner()) alive.set(true);
                });
                if (!alive.get()) {
                    NetworkEntity networkEntity = NetworkEntity.builder()
                            .dataCall(DataCall.RESPAWN)
                            .entityType(EntityType.PLAYER)
                            .build();

                    sessionManager.sendMessageToEveryone(networkEntity);
                }
            }
        }

        level.update();
        for (Entity object : objects) {
            if (object instanceof Shell) {
                object.update(null);
            }
        }

        //Handle Entity Collisions
        for (int i = 0; i < objects.size() - 1; i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                Entity object1 = objects.get(i);
                Entity object2 = objects.get(j);
                if (objects.get(i).collisionBox.intersects(objects.get(j).collisionBox)) {
                    object1.onCollide(object2);
                    object2.onCollide(object1);
                }
            }
        }

        //#2
        //Optimized
        //Handle Tile Collision with Entities
        level.getCollisionBoxes().forEach(tileCollisionBox ->
        {
            for (int k = 0; k < objects.size(); k++) {
                try {
                    Entity object = objects.get(k);
                    if (object.collisionBox != null) {
                        if (object.collisionBox.intersects(tileCollisionBox.getCollisionBox())) {
                            object.onTileCollide(tileCollisionBox);

                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        });

        /*objects.forEach(entity -> {
            if (entity instanceof Shell) {
                NetworkEntity networkEntity = NetworkShell.builder()
                        .position(new Vector2(entity.x, entity.y))
                        .direction(Direction.SOUTH)
                        .dataCall(DataCall.UPDATE)
                        .build();

                sessionManager.sendMessageToEveryone(networkEntity);

            }
        });*/
    }

    private void render() {
        Display.clear();
        level.render(graphics);

        //if (isServer) {
        players.forEach(player1 -> {
            player1.render(graphics);
            //player1.testRender(graphics);
        });
        objects.forEach(object -> {
                    if (object instanceof Shell) {
                        object.render(graphics);
                    }
                }
        );
        //}
        level.postRender(graphics);

        if (DebugConfiguration.DEBUG_COLLIDER) {
            level.getCollisionBoxes().forEach(tileCollisionBox -> {
                tileCollisionBox.render(graphics, DebugConfiguration.SELECTED_COLOR_LEVEL);
            });
            //player.renderDebug(graphics, DebugConfiguration.SELECTED_COLOR_ENTITY);
            for (Entity object : objects) {
                object.renderDebug(graphics, DebugConfiguration.SELECTED_COLOR_ENTITY);
            }
        }
        Display.swapBuffers();
    }

    public void run() {
        int fps = 0;
        int upd = 0;
        int updl = 0;

        long count = 0;

        float delta = 0;

        long lastTime = Time.get();
        while (running) {
            if (sessionManager.getGame() == null) sessionManager.setGame(this);

            long now = Time.get();
            long elapsedTime = now - lastTime;
            DELTA_TIME = elapsedTime;
            lastTime = now;

            count += elapsedTime;

            boolean render = false;
            delta += (elapsedTime / UPDATE_INTERVAL);
            while (delta > 1) {
                update();

                tick -= DELTA_TIME;

                upd++;
                delta--;
                if (render) {
                    updl++;
                } else {
                    render = true;
                }
            }

            if (render) {
                render();
                fps++;
            } else {
                try {
                    Thread.sleep(IDLE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (count >= Time.SECOND) {
                Display.setTitle(TITLE + " || Fps: " + fps + " | Upd: " + upd + " | Updl: " + updl);
                upd = 0;
                fps = 0;
                updl = 0;
                count = 0;
            }

            //NETWORK SENDING
            if (tick <= 0) {
                sendNetworkDataPerTick();
                tick = tickSaved;
            }
        }
    }

    private void cleanUp() {
        Display.destroy();
    }

}
