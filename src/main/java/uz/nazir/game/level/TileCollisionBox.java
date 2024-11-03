package uz.nazir.game.level;

import uz.nazir.debug.DebugDrawer;
import lombok.Data;
import lombok.ToString;

import java.awt.*;

@Data
public class TileCollisionBox {
    private Rectangle collisionBox;

    @ToString.Exclude
    private Tile parent;

    private int i;
    private int j;

    public void render(Graphics g, int color) {
        DebugDrawer.renderDebug(g, color, collisionBox);
    }
}
