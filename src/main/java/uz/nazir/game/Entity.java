package uz.nazir.game;

import uz.nazir.debug.DebugDrawer;
import uz.nazir.game.level.TileCollisionBox;
import uz.nazir.input.Input;
import uz.nazir.network.data.Direction;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.UUID;

public abstract class Entity {
    public final EntityType type;
    public Direction direction;
    protected Rectangle collisionBox;
    public float x;
    public float y;

    @Getter
    @Setter
    private UUID networkId;

    @Getter
    @Setter
    private boolean isOwner;
    protected Entity(EntityType type, float x, float y, Rectangle collisionBox) {
        Game.addEntity(this);
        this.collisionBox = collisionBox;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public abstract void update(Input input);

    public abstract void render(Graphics2D g);

    public void onCollide(Entity other) {
    }

    public void onTileCollide(TileCollisionBox other) {
    }

    public void renderDebug(Graphics2D g, int color) {
        DebugDrawer.renderDebug(g, color, collisionBox);
    }
}
