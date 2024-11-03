package uz.nazir.game.level;

import uz.nazir.game.Game;
import uz.nazir.graphics.TextureAtlas;
import uz.nazir.utils.ImageUtils;
import uz.nazir.utils.LevelParser;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Level {
    public static final int TILE_SCALE = 8;
    public static final int TILE_IN_GAME_SCALE = 2;
    public static final int SCALED_TILE_SIZE = TILE_SCALE * TILE_IN_GAME_SCALE;
    public static final int TILE_IN_WIDTH = Game.WIDTH / SCALED_TILE_SIZE;
    public static final int TILE_IN_HEIGHT = Game.HEIGHT / SCALED_TILE_SIZE;

    @Setter
    @Getter
    private List<TileCollisionBox> collisionBoxes;

    @Getter
    private int[][] tileMap;

    private Tile brick;
    private Tile metal;
    private Tile water;
    private Tile grass;
    private Tile ice;
    private Tile empty;

    @Getter
    private Map<TileType, Tile> tiles;
    private List<Point> grassPoints;

    public Level(TextureAtlas atlas, String levelName) {
        tileMap = new int[TILE_IN_WIDTH][TILE_IN_HEIGHT];
        LevelParser.parseLevel(levelName, TILE_IN_WIDTH, TILE_IN_HEIGHT, tileMap);
        collisionBoxes = new CopyOnWriteArrayList<>();
        grassPoints = new ArrayList<>();

        tiles = new HashMap<>();
        initTiles(atlas);
        tiles.put(TileType.BRICK, brick);
        tiles.put(TileType.METAL, metal);
        tiles.put(TileType.WATER, water);
        tiles.put(TileType.GRASS, grass);
        tiles.put(TileType.ICE, ice);
        tiles.put(TileType.EMPTY, empty);

        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                Tile tile = tiles.get(TileType.getTileType(tileMap[i][j]));

                if (tile.getType() == TileType.GRASS)
                    grassPoints.add(new Point(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE));
            }
        }

        initColliders(false);
    }

    public void update() {

    }

    public void render(Graphics2D g) {
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                Tile tile = tiles.get(TileType.getTileType(tileMap[i][j]));

                if (tile.getType() != TileType.GRASS) {
                    tile.render(g, i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE);
                } else {
                    empty.render(g, i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE);
                }
            }
        }
    }

    public void postRender(Graphics2D g) {
        for (Point p : grassPoints) {
            tiles.get(TileType.GRASS).render(g, p.x, p.y);
        }
    }

    public void setTileMap(int[][] tileMap) {
        if (!Arrays.deepEquals(this.tileMap, tileMap)) {
            this.tileMap = tileMap;
            initColliders(true);
        }
    }

    public void initColliders(boolean clear) {
        List<TileCollisionBox> bricks = new ArrayList<>();
        List<TileCollisionBox> metals = new ArrayList<>();
        List<TileCollisionBox> waters = new ArrayList<>();
        List<TileCollisionBox> grasses = new ArrayList<>();
        List<TileCollisionBox> ices = new ArrayList<>();

        if (clear) {
            brick.removeAll();
            metal.removeAll();
            water.removeAll();
            grass.removeAll();
            ice.removeAll();
        }

        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                Tile tile = tiles.get(TileType.getTileType(tileMap[i][j]));
                if (tile.getType() != TileType.EMPTY) {
                    TileCollisionBox collisionBox;
                    switch (tile.getType()) {
                        case BRICK:
                            collisionBox = brick.addCopy(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE, i, j);
                            bricks.add(collisionBox);
                            collisionBoxes.add(collisionBox);
                            break;
                        case METAL:
                            collisionBox = metal.addCopy(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE, i, j);
                            metals.add(collisionBox);
                            collisionBoxes.add(collisionBox);
                            break;
                        case WATER:
                            collisionBox = water.addCopy(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE, i, j);
                            waters.add(collisionBox);
                            collisionBoxes.add(collisionBox);
                            break;
                        case GRASS:
                            collisionBox = grass.addCopy(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE, i, j);
                            grasses.add(collisionBox);
                            collisionBoxes.add(collisionBox);
                            break;
                        case ICE:
                            collisionBox = ice.addCopy(i * SCALED_TILE_SIZE, j * SCALED_TILE_SIZE, i, j);
                            ices.add(collisionBox);
                            collisionBoxes.add(collisionBox);
                            break;
                    }
                }
            }
        }
    }

    private void initTiles(TextureAtlas atlas) {
        brick = new Tile(atlas.cut(32 * TILE_SCALE, 0 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.BRICK, this);
        metal = new Tile(atlas.cut(32 * TILE_SCALE, 2 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.METAL, this);
        water = new Tile(atlas.cut(32 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.WATER, this);

        //Make grass with transparent background
        BufferedImage unTransparentImage = atlas.cut(34 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE);
        ImageUtils.transparent(unTransparentImage);
        grass = new Tile(unTransparentImage, TILE_IN_GAME_SCALE, TileType.GRASS, this);

        ice = new Tile(atlas.cut(36 * TILE_SCALE, 4 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.ICE, this);
        empty = new Tile(atlas.cut(42 * TILE_SCALE, 2 * TILE_SCALE, TILE_SCALE, TILE_SCALE), TILE_IN_GAME_SCALE, TileType.EMPTY, this);
    }
}
