package uz.nazir.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ResourceLoader {

    public static BufferedImage loadImage(String fileName) {
        BufferedImage image = null;
        try {
            String path = System.getProperty("resources.path");
            image = ImageIO.read(new File(path + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static String loadString(String fileName) {
        String result;
        String path = System.getProperty("resources.path");
        try (BufferedReader br = new BufferedReader(new FileReader(path + fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            result = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
