package main;

import level.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Heart extends Entity{
    public Heart( Level level, Location loc) {
        super(EntityType.HEART, level, loc);
    }
    private Timer timer;
    private GameEngine.AudioClip health;



    @Override
    public void processMovement(double dt) {

    }
    public void update(double dt) {
        health = getLevel().getManager().getEngine().loadAudio("resources/sounds/health.wav");

        if (getLevel().getPlayer().getCollisionBox().collidesWith(this.getCollisionBox())) {
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Increase player's health by 2
                    getLevel().getPlayer().setHealth(getLevel().getPlayer().getHealth() + 3);


                }
            });
            getLevel().getManager().getEngine().playAudio(health);

            timer.start();




            destroy();
        }
    }
    @Override
    public Image getIdleFrame() {


        return getLevel().getManager().getEngine().getTexture(getType().toString().toLowerCase());
    }

    public double getWidth() {
        return 50;
    }

    public double getHeight() {
        return 50;    }


}

