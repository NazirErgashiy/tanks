package uz.nazir.game.entities;

import uz.nazir.Application;
import uz.nazir.debug.DebugDrawer;
import uz.nazir.game.Entity;
import uz.nazir.game.EntityType;
import uz.nazir.game.Game;
import uz.nazir.game.level.TileCollisionBox;
import uz.nazir.game.level.TileType;
import uz.nazir.graphics.Sprite;
import uz.nazir.graphics.SpriteSheet;
import uz.nazir.graphics.TextureAtlas;
import uz.nazir.input.Input;
import uz.nazir.network.data.DataCall;
import uz.nazir.network.data.Direction;
import uz.nazir.network.data.NetworkEntity;
import uz.nazir.network.data.Vector2;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.socket.WebSocketSession;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ToString
public class Player extends Entity {
    public static final int SPRITE_SCALE = 16;
    public static final int SPRITES_PER_HEADING = 1;
    public static final int SHELL_SPAWN_OFFSET = 15;

    private TextureAtlas atlas;
    private Heading heading;
    private Map<Heading, Sprite> spriteMap;

    private float scale;
    private float speed;

    private long reloadTime = 200_000_000L;
    private long reloadTimeSaved;

    private boolean colliding = false;
    private boolean isLocalPlayer;

    private Vector2 rememberedPos;

    @Getter
    @Setter
    private WebSocketSession ownerSession;

    public Player(float x, float y, float scale, float speed, TextureAtlas atlas, boolean isLocalPlayer) {
        super(EntityType.PLAYER, x, y, new Rectangle((int) x + 2, (int) y + 2, (int) scale * SPRITE_SCALE - 2, (int) scale * SPRITE_SCALE - 2));

        this.isLocalPlayer = isLocalPlayer;
        reloadTimeSaved = reloadTime;
        this.atlas = atlas;
        heading = Heading.NORTH;
        spriteMap = new HashMap<>();
        this.scale = scale;
        this.speed = speed;

        for (Heading h : Heading.values()) {
            SpriteSheet sheet = new SpriteSheet(h.texture(this.atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
        }

    }

    public Direction getDirection() {
        switch (heading) {
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case EAST:
                return Direction.EAST;
            case WEST:
                return Direction.WEST;
        }
        return null;
    }

    public void setDirection(Direction direction) {
        if (direction != null)
            switch (direction) {
                case NORTH:
                    heading = Heading.NORTH;
                    break;
                case SOUTH:
                    heading = Heading.SOUTH;
                    break;
                case EAST:
                    heading = Heading.EAST;
                    break;
                case WEST:
                    heading = Heading.WEST;
                    break;
            }
    }

    @Override
    public void update(Input input) {

        //if (!isLocalPlayer) return;
        collisionBox.y = (int) y;
        collisionBox.x = (int) x;

        if (!isOwner()) return;

        float newX = x;
        float newY = y;

        //Shoot
        reloadTime -= Game.DELTA_TIME;
        if (input.getKey(KeyEvent.VK_Z) && reloadTime <= 0f) {
            NetworkEntity networkEntity = null;
            switch (heading) {
                case NORTH:
                    //shell = Spawn.instantiate(Shell.class, Shell.Heading.NORTH, 2, x + 6, y - SHELL_SPAWN_OFFSET + 8, atlas, this);
                    //shell.setNetworkId(UUID.randomUUID());
                    networkEntity = NetworkEntity.builder()
                            .id(UUID.randomUUID())
                            .isOwner(false)
                            .position(new Vector2(x + 6, y - SHELL_SPAWN_OFFSET + 8))
                            .dataCall(DataCall.CREATE)
                            .entityType(EntityType.SHELL)
                            .build();
                    break;
                case SOUTH:
                    //shell = Spawn.instantiate(Shell.class, Shell.Heading.SOUTH, 2, x + 6, y + SHELL_SPAWN_OFFSET + 8, atlas, this);
                    //shell.setNetworkId(UUID.randomUUID());
                    networkEntity = NetworkEntity.builder()
                            .id(UUID.randomUUID())
                            .isOwner(false)
                            .position(new Vector2(x + 6, y + SHELL_SPAWN_OFFSET + 15))
                            .dataCall(DataCall.CREATE)
                            .entityType(EntityType.SHELL)
                            .build();
                    break;
                case EAST:
                    //shell = Spawn.instantiate(Shell.class, Shell.Heading.EAST, 2, x + SHELL_SPAWN_OFFSET, y + SHELL_SPAWN_OFFSET - 3, atlas, this);
                    //shell.setNetworkId(UUID.randomUUID());
                    networkEntity = NetworkEntity.builder()
                            .id(UUID.randomUUID())
                            .isOwner(false)
                            .position(new Vector2(x + SHELL_SPAWN_OFFSET + 15, y + SHELL_SPAWN_OFFSET - 3))
                            .dataCall(DataCall.CREATE)
                            .entityType(EntityType.SHELL)
                            .build();
                    break;
                case WEST:
                    //shell = Spawn.instantiate(Shell.class, Shell.Heading.WEST, 2, x, y + SHELL_SPAWN_OFFSET - 3, atlas, this);
                    //shell.setNetworkId(UUID.randomUUID());
                    networkEntity = NetworkEntity.builder()
                            .id(UUID.randomUUID())
                            .isOwner(false)
                            .position(new Vector2(x - SHELL_SPAWN_OFFSET, y + SHELL_SPAWN_OFFSET - 3))
                            .dataCall(DataCall.CREATE)
                            .entityType(EntityType.SHELL)
                            .build();
                    break;
            }

            reloadTime = reloadTimeSaved;

            networkEntity.setOwnerId(getNetworkId());
            networkEntity.setDirection(direction);
            Application.sessionManager.sendMessageToEveryone(networkEntity);
        }

        if (!colliding) {
            rememberedPos = new Vector2(x, y);
        } else {
            x = rememberedPos.x;
            y = rememberedPos.y;
            colliding = false;
            return;
        }

        /*
        if (colliding) {
            if (heading == Heading.NORTH) {
                y += 1;
            }
            if (heading == Heading.SOUTH) {
                y -= 1;
            }
            if (heading == Heading.EAST) {
                x -= 1;
            }
            if (heading == Heading.WEST) {
                x += 1;
            }

            colliding = false;
            return;
        }*/

        //Movement
        if (input.getKey(KeyEvent.VK_UP)) {
            newY -= speed;
            heading = Heading.NORTH;
            direction = Direction.NORTH;
        } else if (input.getKey(KeyEvent.VK_RIGHT)) {
            newX += speed;
            heading = Heading.EAST;
            direction = Direction.EAST;
        } else if (input.getKey(KeyEvent.VK_DOWN)) {
            newY += speed;
            heading = Heading.SOUTH;
            direction = Direction.SOUTH;
        } else if (input.getKey(KeyEvent.VK_LEFT)) {
            newX -= speed;
            heading = Heading.WEST;
            direction = Direction.WEST;
        }

        //Restrict from moving off-screen
        if (newX < 0) {
            newX = 0;
        } else if (newX >= Game.WIDTH - SPRITE_SCALE * scale) {
            newX = Game.WIDTH - SPRITE_SCALE * scale;
        }

        if (newY < 0) {
            newY = 0;
        } else if (newY >= Game.HEIGHT - SPRITE_SCALE * scale) {
            newY = Game.HEIGHT - SPRITE_SCALE * scale;
        }

        collisionBox.x = (int) newX;
        collisionBox.y = (int) newY;
        //Apply new pos
        x = newX;
        y = newY;
    }

    public void testRender(Graphics2D graphics) {
        DebugDrawer.renderDebug(graphics, 0xFFFF00FF, collisionBox);
    }

    @Override
    public void render(Graphics2D g) {
        spriteMap.get(heading).render(g, x, y);
    }

    @Override
    public void onCollide(Entity other) {
        if (other instanceof Shell) {
            //System.out.println(other);
            Shell shell = (Shell) other;

            if (!shell.owner.equals(getNetworkId())) {
                if (Game.isServer) {
                    collisionBox.y = 0;
                    collisionBox.x = 0;
                    collisionBox.height = 1;
                    collisionBox.width = 1;
                    Game.removePlayer(this);
                }
            }
        }
        if (other instanceof Player) {
            colliding = true;
        }
    }

    @Override
    public void onTileCollide(TileCollisionBox other) {
        if (other.getParent().getType() == TileType.METAL ||
                other.getParent().getType() == TileType.BRICK ||
                other.getParent().getType() == TileType.WATER) {
            colliding = true;
        }
    }

    public enum Heading {
        NORTH(0 * SPRITE_SCALE, 0 * SPRITE_SCALE, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        EAST(6 * SPRITE_SCALE, 0 * SPRITE_SCALE, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        SOUTH(4 * SPRITE_SCALE, 0 * SPRITE_SCALE, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        WEST(2 * SPRITE_SCALE, 0 * SPRITE_SCALE, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE);

        private int x, y, h, w;

        Heading(int x, int y, int h, int w) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        protected BufferedImage texture(TextureAtlas atlas) {
            return atlas.cut(x, y, w, h);
        }
    }
}
