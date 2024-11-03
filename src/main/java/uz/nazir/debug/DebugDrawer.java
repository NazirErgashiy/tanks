package uz.nazir.debug;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DebugDrawer {
    public static void renderDebug(Graphics g, int color, Rectangle bounds) {
        var square = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < square.getHeight(); y++) {
            for (int x = 0; x < square.getWidth(); x++) {
                square.setRGB(x, y, color);
            }
        }
        g.drawImage(square, bounds.x, bounds.y, null);
    }
}
