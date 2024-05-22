package main;

import block.Block;
import block.BlockTypes;
import block.decorations.Decoration;
import block.decorations.FakeLightSpot;
import level.TextMessage;

import java.awt.*;
import java.util.Random;

/*
*   This class handles the rendering of all objects on the screen.
*
*   It works by making the players location the center of the screen and only drawings the objects that are
*   around the player, taking into account the dimensions of the screen.
*
* */
public class Camera {

    public Location loc, centerPoint;
    public Game game;
    public Player player;
    private GameEngine.AudioClip keyObtained;
    private boolean hasPlayedKeyAudio = false;

    public boolean debugMode;
    private int DEBUG_ENTITIES_ON_SCREEN;
    private int DEBUG_BLOCKS_ON_SCREEN;
    private int DEBUG_DECORATIONS_ON_SCREEN;

    private long lastFpsCheck = 0;
    private int currentFps = 0;
    private int totalFrames = 0;
    private CollisionBox collisionBox;

    public double centerOffsetX, centerOffsetY;

    public Camera(Game game, Player p) {
        this.game = game;
        this.player = p;
        this.loc = new Location(p.getLocation().getX(), p.getLocation().getY());
        keyObtained = this.game.loadAudio("resources/sounds/keyObtained.wav");





        /*
        *   'point1' is the top left location of the screen.
        *   'point2' is the bottom right location of the screen.
        *
        *   It works these out by getting the players location and then subtracting half of the screen's width and height
        *   to get the first point (point1), then adding the screen's width and height for the second point (point2).
        *
        *   By having these two points, we can see if world objects are between these two points, and if so then draw them. If they
        *   aren't then we can just ignore them.
        * */
        Location point1 = new Location(p.getLocation().getX() - (game.width() / 2), p.getLocation().getY() - (game.height() / 2));

        System.out.println("1: " + point1.getX() + ", " + point1.getY());
        System.out.println("2: " + (point1.getX() + game.width()) + ", " + (point1.getY() + game.height()));
        this.collisionBox = new CollisionBox(point1.getX(), point1.getY(), game.width(), game.height());
    }

    /*
    *   This method constantly updates the points. This is needed because the players location always changes.
    * */
    public void update() {
        totalFrames++;
        if (System.nanoTime() > lastFpsCheck + 1000000000) {
            lastFpsCheck = System.nanoTime();
            currentFps = totalFrames;
            totalFrames = 0;
        }

        this.loc.setX(player.getLocation().getX());
        this.loc.setY(player.getLocation().getY());

        centerPoint = new Location(game.width() / 2, game.height() / 2);
        centerOffsetX = centerPoint.getX() - player.getLocation().getX();
        centerOffsetY = centerPoint.getY() - player.getLocation().getY();

        collisionBox.setLocation(player.getLocation().getX() - (double) game.width() / 2, player.getLocation().getY() - (double) game.height() / 2);
        collisionBox.setSize(game.width(), game.height());
    }



    public void draw() {
        renderBackground();
        renderDecorations();
        renderSpotLights();
        renderBlocks();
        getPlayer().render(this);
        renderEntities();
        renderTextMessages();
        renderUI();
    }

    private void renderBackground() {
        if (game.imageBank.get("background") != null) {
            //System.out.println("Draw bg");
            game.drawImage(game.imageBank.get("background"), 0, 0, game.width(), game.height());
        }
    }

    private void renderDecorations() {
        DEBUG_DECORATIONS_ON_SCREEN = 0;
        for (Decoration deco : game.getActiveLevel().getDecorations()) {
            if (deco.getCollisionBox().collidesWith(this.getCollisionBox())) {
                double decoOffsetX = deco.getLocation().getX() + centerOffsetX;
                double decoOffsetY = deco.getLocation().getY() + centerOffsetY;

                Image texture = game.getTexture(deco.getType().toString());
                game.drawImage(texture, decoOffsetX, decoOffsetY - deco.getHeight() + Game.BLOCK_SIZE, deco.getWidth(), deco.getHeight());

                DEBUG_DECORATIONS_ON_SCREEN++;

                if (debugMode) {
                    double hitboxOffsetX = deco.getCollisionBox().getLocation().getX() + centerOffsetX;
                    double hitboxOffsetY = deco.getCollisionBox().getLocation().getY() + centerOffsetY;
                    game.changeColor(Color.GREEN);
                    game.drawRectangle(hitboxOffsetX, hitboxOffsetY, deco.getCollisionBox().getWidth(), deco.getCollisionBox().getHeight());
                }
            }
        }
    }

    private void renderSpotLights() {
        for (FakeLightSpot spotLight : game.getActiveLevel().getSpotLights()) {
            if (spotLight.getParent().getCollisionBox().collidesWith(this.getCollisionBox())) {
                double decoOffsetX = spotLight.getParent().getLocation().getX() + centerOffsetX;
                double decoOffsetY = spotLight.getParent().getLocation().getY() + centerOffsetY;

                game.drawImage(game.getTexture("spot_light"),
                        decoOffsetX + spotLight.getOffsetX(), decoOffsetY - spotLight.getParent().getHeight() + Game.BLOCK_SIZE + spotLight.getOffsetY(),
                        spotLight.getWidth(), spotLight.getHeight());
            }
        }
    }

    /*
    *   This is where each individual block is drawn. It goes through the entire world map and checks if the blocks are
    *   between our point1 and point2. If they are, this means they are visible to the camera and should be drawn.
    *
    * */
    private void renderBlocks() {
        DEBUG_BLOCKS_ON_SCREEN = 0;
        for (int x = 0; x < game.getActiveLevel().getBlockGrid().getWidth(); x++) {
            for (int y = 0; y < game.getActiveLevel().getBlockGrid().getHeight(); y++) { //Iterating over all the blocks
                Block b = game.getActiveLevel().getBlockGrid().getBlocks()[x][y]; //Getting the block from the grid based on the coordinates
                if (b.getType() == BlockTypes.VOID || b.getType() == BlockTypes.BARRIER) {
                    continue; //Skip void because its an empty block.
                }

                if (b.getLocation().isBlockBetween(getPoint1(), getPoint2())) {
                    /*
                    *   Here we have to convert the blocks coordinates to be relative to the camera.
                    *   Basically in update(dt) we calculate the centerOffset by getting the center of the screen and then subtracting
                    *   the players location from it.
                    *
                    *   Then in here, we get the block's location and add the centerOffset to it.
                    * */
                    double blockOffsetX = b.getLocation().getX() + centerOffsetX;
                    double blockOffsetY = b.getLocation().getY() + centerOffsetY;

                    b.drawBlock(this, blockOffsetX, blockOffsetY);
                    DEBUG_BLOCKS_ON_SCREEN++;
                }
            }
        }
    }

    public void renderEntities() {
        DEBUG_ENTITIES_ON_SCREEN = 0;
        for (Entity entity : game.getActiveLevel().getEntities()) {
            if (!entity.isActive()) {
                continue;
            }

            if (entity.getCollisionBox().collidesWith(this.getCollisionBox())) {
                entity.render(this);
                DEBUG_ENTITIES_ON_SCREEN++;
            }
        }
    }

    public void renderTextMessages() {
        for (TextMessage txtMsg : game.getActiveLevel().getTextMessages().values()) {
            if (txtMsg == null) {
                continue;
            }

            double localXDiff = txtMsg.getLocation().getX();
            double localYDiff = txtMsg.getLocation().getY();
            if (!txtMsg.isStatic()) {
                localXDiff += centerOffsetX;
                localYDiff += centerOffsetY;
            }

            game.changeColor(txtMsg.getColor());
            if (!txtMsg.isBold()) {
                game.drawText(localXDiff, localYDiff, txtMsg.getText(), "Serif", txtMsg.getFontSize());
            } else {
                game.drawBoldText(localXDiff, localYDiff, txtMsg.getText(), "Serif", txtMsg.getFontSize());
            }
        }
    }

    public void renderUI() {
        if (game.isPaused) {
            game.changeColor(Color.orange);
            game.drawText((game.width() / 2) - 100, game.height() / 2, "Paused", 75);
            return;
        }

        Location healthBarLoc = new Location(50, 35);

        double localXDiff = healthBarLoc.getX();
        double localYDiff = healthBarLoc.getY();

        game.changeColor(Color.white);
        game.drawText(50,35,"Health:",15);
        game.drawText(1200,50,"Key : ", 20);
        if (game.getActiveLevel().getPlayer().hasKey()) {
            game.drawImage(game.imageBank.get("key"), 1230, 20, 50, 50);

            // Check if the audio has not been played yet
            if (!hasPlayedKeyAudio) {
                this.game.playAudio(keyObtained);
                hasPlayedKeyAudio = true; // Set the flag to true after playing the audio
            }
        } else {
            // Optional: Reset the flag if the player no longer has the key
            hasPlayedKeyAudio = false;
        }

        game.changeColor(Color.RED);
        game.drawSolidRectangle(localXDiff,localYDiff, player.getHealth(), 15);
        game.drawText(localXDiff+50,localYDiff, String.valueOf(player.getHealth()), 20);


        if (debugMode) {
            game.changeColor(Color.yellow);
            game.drawText(25, 100, "fps: " + currentFps, "Serif", 20);
            game.drawText(25, 120, "entities on screen: " + DEBUG_ENTITIES_ON_SCREEN, "Serif", 20);
            game.drawText(25, 140, "blocks on screen: " + DEBUG_BLOCKS_ON_SCREEN, "Serif", 20);
            game.drawText(25, 160, "decorations on screen: " + DEBUG_DECORATIONS_ON_SCREEN, "Serif", 20);
            game.drawText(25, 200, "player:", "Serif", 20);
            game.drawText(35, 220, "pos: " + getPlayer().getLocation().toString(), "Serif", 20);
            game.drawText(35, 240, "velocity: " + Math.round(getPlayer().moveX) + ", " + Math.round(getPlayer().moveY), "Serif", 20);
            if (getPlayer().getTarget() != null) {
                game.drawText(35, 260, "target: " + getPlayer().getTarget().toString(), "Serif", 20);
            } else {
                game.drawText(35, 260, "target: null", "Serif", 20);
            }
            game.drawText(35, 280, "onGround: " + getPlayer().isOnGround(), "Serif", 20);
            game.drawText(35, 300, "hasKey: " + getPlayer().hasKey(), "Serif", 20);

            double hitboxOffsetX = getCollisionBox().getLocation().getX() + centerOffsetX;
            double hitboxOffsetY = getCollisionBox().getLocation().getY() + centerOffsetY;
            game.changeColor(Color.RED);
            game.drawRectangle(hitboxOffsetX, hitboxOffsetY, getCollisionBox().getWidth(), getCollisionBox().getHeight());
        }
    }

    public Player getPlayer() {
        return player;
    }

    public CollisionBox getCollisionBox() {
        return collisionBox;
    }

    public Location getPoint1() {
        return getCollisionBox().getLocation();
    }

    public Location getPoint2() {
        return getCollisionBox().getCorner();
    }

}
