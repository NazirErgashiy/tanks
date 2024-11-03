package uz.nazir.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    private static final Color backColor = Color.BLACK;
    private static final int THRESHOLD = 10;
    private static final int TRANSPARENT = 0x00000000;  // 0x00000000;

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        newImage.getGraphics().drawImage(image, 0, 0, width, height, null);
        return newImage;
    }

    public static void transparent(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                Color color = new Color(pixel);

                int dr = Math.abs(color.getRed()   - backColor.getRed()),
                        dg = Math.abs(color.getGreen() - backColor.getGreen()),
                        db = Math.abs(color.getBlue()  - backColor.getBlue());

                if (dr < THRESHOLD && dg < THRESHOLD && db < THRESHOLD) {
                    image.setRGB(x, y, TRANSPARENT);
                }
            }
        }
    }
}
