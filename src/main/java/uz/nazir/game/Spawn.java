package uz.nazir.game;

import uz.nazir.game.entities.Player;
import uz.nazir.graphics.TextureAtlas;
import uz.nazir.game.entities.Shell;

import java.lang.reflect.InvocationTargetException;

public class Spawn {

    public static <T extends Entity> T instantiate(Class<? extends Entity> clazz, Shell.Heading heading, int scale, float x, float y, TextureAtlas atlas, Player owner) {
        T instance = null;
        try {
            instance = (T) clazz.getDeclaredConstructors()[0].newInstance(heading, scale, x, y, atlas, owner);
            //Game.addEntity(instance);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
