package uz.nazir.graphics;

import uz.nazir.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Sprite {
    private SpriteSheet sheet;
    private float scale;
    private BufferedImage image;

    public Sprite(SpriteSheet sheet, float scale) {
        this.scale = scale;
        this.sheet = sheet;
        image = sheet.getSprite(0);
        ImageUtils.transparent(image);//Replace image black color with transparent
        image = ImageUtils.resize(image, (int) (image.getWidth() * scale), (int) (image.getWidth() * scale));//Rescale
    }

    public void render(Graphics2D g, float x, float y) {
        g.drawImage(image, (int) (x), (int) (y), null);
    }
}
