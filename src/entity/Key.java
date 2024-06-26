package entity;

import level.Level;
import main.GameEngine;
import main.Location;

import java.awt.*;

public class Key extends Entity {

    private GameEngine.AudioClip keyObtained;
    public Key(Level level, Location loc) {
        super(EntityType.KEY, level, loc, 50, 50);
        setScale(1);
        setCanMove(false);
        keyObtained = level.getManager().getEngine().loadAudio("resources/sounds/keyObtained.wav");
    }

    public void update(double dt) {
        if (!getLevel().getPlayer().hasKey() && getLevel().getPlayer().getCollisionBox().collidesWith(this.getCollisionBox())) {
            getLevel().getPlayer().setHasKey(true);
            getLevel().getManager().getEngine().playAudio(keyObtained);
            destroy();
        }
    }
    @Override
    public Image getActiveFrame() {
        return getLevel().getManager().getEngine().getTexture(getType().toString().toLowerCase());
    }

    public double getWidth() {
        return 50 * getScale();
    }

    public double getHeight() {
        return 50 * getScale();
    }

    @Override
    public void processMovement(double dt) {
        return;
    }

    public boolean isFlipped(){
        return true;
    }
}


