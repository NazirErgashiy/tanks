package uz.nazir.utils;

public class LevelParser {

    public static void parseLevel(String levelName, int width, int height, int[][] tileMap) {
        String level = ResourceLoader.loadString(levelName);
        level = level.replace("\n", "").replace("\r", "");
        //level = level.replace("//", "");
        String[] splitted = level.split(",");
        //System.out.println(width);
        //System.out.println(height);
        int increment = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //System.out.println(splitted[i]);
                tileMap[i][j] = Integer.parseInt(splitted[increment]);
                increment++;
            }
        }
    }
}
