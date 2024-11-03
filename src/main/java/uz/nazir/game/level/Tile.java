package uz.nazir.game.level;

import uz.nazir.utils.ImageUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class Tile {
    private BufferedImage image;
    private TileType type;
    private Level myLevel;
    private List<TileCollisionBox> copies;

    @Setter(AccessLevel.NONE)
    private int scale;

    protected Tile(BufferedImage image, int scale, TileType type, Level level) {
        myLevel = level;
        this.scale = scale;
        this.type = type;
        this.image = ImageUtils.resize(image, image.getWidth() * scale, image.getHeight() * scale);
        copies = new CopyOnWriteArrayList<>();
    }

    protected void render(Graphics2D g, int x, int y) {
        g.drawImage(image, x, y, null);
    }

    public TileCollisionBox addCopy(int x, int y, int arrI, int arrJ) {
        TileCollisionBox tileCollisionBox = new TileCollisionBox();
        tileCollisionBox.setCollisionBox(new Rectangle(x , y , 16, 16));
        tileCollisionBox.setParent(this);
        tileCollisionBox.setI(arrI);
        tileCollisionBox.setJ(arrJ);
        copies.add(tileCollisionBox);
        return tileCollisionBox;
    }

    public void removeCopy(TileCollisionBox box) {
        Rectangle rectangle = box.getCollisionBox();
        rectangle.x = 0;
        rectangle.y = 0;
        rectangle.width = 1;
        rectangle.height = 1;
        myLevel.getTileMap()[box.getI()][box.getJ()] = 0;
        copies.remove(box);
    }

    public void removeAll() {
        copies.forEach(tileCollisionBox -> {
            Rectangle rectangle = tileCollisionBox.getCollisionBox();
            rectangle.x = 0;
            rectangle.y = 0;
            rectangle.width = 1;
            rectangle.height = 1;
        });
        copies = new CopyOnWriteArrayList<>();
    }
}
