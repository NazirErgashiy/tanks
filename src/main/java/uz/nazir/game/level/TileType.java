package uz.nazir.game.level;

import lombok.Getter;
import lombok.ToString;

@ToString
public enum TileType {
    EMPTY(0),
    BRICK(1),
    METAL(2),
    WATER(3),
    GRASS(4),
    ICE(5);

    @Getter
    private int n;

    TileType(int n) {
        this.n = n;
    }

    public static TileType getTileType(int n) {
        switch (n) {
            case 0:
                return EMPTY;
            case 1:
                return BRICK;
            case 2:
                return METAL;
            case 3:
                return WATER;
            case 4:
                return GRASS;
            case 5:
                return ICE;
        }
        return null;
    }
}
