package uz.nazir.game.entities;

import uz.nazir.Application;
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
import lombok.Getter;
import lombok.ToString;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ToString
public class Shell extends Entity {
    public static final int SPRITE_SCALE = 8;
    public static final int SPRITES_PER_HEADING = 1;
    private TextureAtlas atlas;

    @Getter
    private Heading heading;
    private Map<Heading, Sprite> spriteMap;

    private float scale;
    private float speed = 4;

    public UUID owner;

    public Shell(Heading heading, int scale, float x, float y, TextureAtlas atlas, UUID owner) {
        super(EntityType.SHELL, x, y, new Rectangle((int) x, (int) y, scale * SPRITE_SCALE, scale * SPRITE_SCALE - 8));
        //System.out.println("Shell constructor invoked");
        this.scale = scale;
        this.owner = owner;
        this.heading = heading;
        spriteMap = new HashMap<>();
        this.atlas = atlas;
        for (Heading h : Heading.values()) {
            SpriteSheet sheet = new SpriteSheet(h.texture(this.atlas), SPRITES_PER_HEADING, SPRITE_SCALE);
            Sprite sprite = new Sprite(sheet, scale);
            spriteMap.put(h, sprite);
        }
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

        //System.out.println("X=" + x + " Y=" + y);
        float newX = x;
        float newY = y;

        if (heading == Heading.NORTH) {
            newY -= speed;
            direction = Direction.NORTH;
        } else if (heading == Heading.EAST) {
            newX += speed;
            direction = Direction.EAST;
        } else if (heading == Heading.SOUTH) {
            newY += speed;
            direction = Direction.SOUTH;
        } else if (heading == Heading.WEST) {
            newX -= speed;
            direction = Direction.WEST;
        }

        if (newX < 0) {
            Game.removeEntity(this);
            newX = 0;
        } else if (newX >= Game.WIDTH - SPRITE_SCALE * scale) {
            Game.removeEntity(this);
            newX = Game.WIDTH - SPRITE_SCALE * scale;
        }

        if (newY < 0) {
            Game.removeEntity(this);
            newY = 0;
        } else if (newY >= Game.HEIGHT - SPRITE_SCALE * scale) {
            Game.removeEntity(this);
            newY = Game.HEIGHT - SPRITE_SCALE * scale;
        }

        collisionBox.x = (int) newX;
        collisionBox.y = (int) newY;
        x = newX;
        y = newY;
    }

    @Override
    public void render(Graphics2D g) {
        if (spriteMap == null) {
            Game.removeEntity(this);
        }
        Sprite sprite = spriteMap.get(heading);

        if (sprite != null)
            sprite.render(g, x, y);
    }

    @Override
    public void onCollide(Entity other) {
        if (Game.isServer) {
            Game.removeEntity(this);

            NetworkEntity networkEntity = NetworkEntity.builder()
                    .dataCall(DataCall.DESTROY)
                    .id(getNetworkId())
                    .entityType(EntityType.SHELL)
                    .build();
            Application.sessionManager.sendMessageToEveryone(networkEntity);
        }
    }

    @Override
    public void onTileCollide(TileCollisionBox other) {
        if (Game.isServer) {
            if (other.getParent().getType() == TileType.BRICK || other.getParent().getType() == TileType.METAL) {
                if (other.getParent().getType() == TileType.BRICK) {
                    other.getParent().removeCopy(other);
                }
                Game.removeEntity(this);

                NetworkEntity networkEntity = NetworkEntity.builder()
                        .dataCall(DataCall.DESTROY)
                        .id(getNetworkId())
                        .entityType(EntityType.SHELL)
                        .build();
                Application.sessionManager.sendMessageToEveryone(networkEntity);
            }
        }
    }

    public enum Heading {
        NORTH(40 * SPRITE_SCALE, 13 * SPRITE_SCALE - 2, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        EAST(43 * SPRITE_SCALE, 13 * SPRITE_SCALE - 2, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        SOUTH(42 * SPRITE_SCALE, 13 * SPRITE_SCALE - 2, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE),
        WEST(41 * SPRITE_SCALE, 13 * SPRITE_SCALE - 2, 1 * SPRITE_SCALE, 1 * SPRITE_SCALE);

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
