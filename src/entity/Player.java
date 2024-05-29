package entity;

import block.BlockClimbable;
import block.BlockTypes;
import level.Level;
import level.ParticleTypes;
import main.Camera;
import main.Game;
import main.Location;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class Player extends EntityLiving {
    private JProgressBar healthBar;
    private boolean isJumping;
    private boolean hasKey;

    private Timer runAnimationTimer;
    private int runFrameIndex;

    public Timer jumpAnimationTimer;
    private int jumpFrameIndex;
    private double timeJumping;
    private double maxJumpTime = 0.20;
    public static int score;//seconds

    private double runParticleTimer;
    private double RUN_PARTICLE_FREQUENCY = 0.075;
    private Clip runningSound;

    Image gifImage;
    Image plantAttack;
    Image gifImage2;
    Image level1;

    public Player(Level level, Location loc) {
        super(EntityType.PLAYER, level, loc, 19, 29);

        setHitboxColor(Color.cyan);
        setMaxHealth(100);
        setDamage(5);
        setHealth(getMaxHealth());
        setDirectionY(1);
        initRunningSound();
        init();
    }

    public void init() {
        gifImage = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        plantAttack = Toolkit.getDefaultToolkit().createImage("resources/images/plantAttack.gif");

        gifImage2 = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        level1 = Toolkit.getDefaultToolkit().createImage("resources/images/level1.gif");

        setHitSound(getLevel().getManager().getEngine().loadAudio("resources/sounds/hitSound.wav"));
        setAttackSound(getLevel().getManager().getEngine().loadAudio("resources/sounds/attackSound.wav"));

        this.healthBar = new JProgressBar(0, getMaxHealth());
        this.healthBar.setBounds(100, 25, 100, 10); // Adjust position and size as needed
        this.healthBar.setForeground(Color.RED); // Set the color
        this.healthBar.setValue(getMaxHealth()); // Set initial health
        this.healthBar.setStringPainted(true); // Show health value

        this.runAnimationTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runFrameIndex = (runFrameIndex + 1) % 4;
            }
        });

        this.jumpAnimationTimer = new Timer(75, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (jumpFrameIndex < 3) {
                    jumpFrameIndex++;
                }
            }
        });
    }

    public void update(double dt) {
        super.update(dt);
        animateCharacter();
        if (runParticleTimer < RUN_PARTICLE_FREQUENCY) {
            runParticleTimer += 1 * dt;
        }

        if  (runParticleTimer >= RUN_PARTICLE_FREQUENCY) {
            if(isMovingHorizontally() && isOnGround()){
                double partVelX = 0.75;
                double partVelY = -0.5;
                if (isFlipped()) {
                    partVelX *= -1;
                }

                getLevel().spawnParticle(ParticleTypes.CLOUD, getLocation().getX(), getLocation().getY() + 48, partVelX, partVelY);
                runParticleTimer = 0;
            }
        }
        checkGoldCoinCollisions(getLevel().getManager().getEngine());
    }

    public void incrementScore(){
        score += 10;
    }

    // New method to check for collisions with gold coins
    public void checkGoldCoinCollisions(Game game) {
        ArrayList<Entity> entities = getLevel().getEntities();
        for (Entity entity : entities) {
            if (entity instanceof goldCoin) {
                goldCoin coin = (goldCoin) entity;
                if (!coin.isCollected() && getCollisionBox().collidesWith(coin.getCollisionBox())) {
                    coin.collect();
                    incrementScore(); // Increment score by 1 for each coin collected
                }
            }
        }
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }


    @Override
    public void processMovement(double dt) {
        moveX = getDirectionX() * (speed * dt);
        moveY = getDirectionY() * (speed * dt);

        moveX(moveX);
        moveY(moveY);

        if (isJumping()) {
            setDirectionY(-1.5);
            timeJumping += 1 * dt;

            if (timeJumping > maxJumpTime) {
                this.setJumping(false);
                this.setDirectionY(0);
                this.timeJumping = 0;
            }
            return;
        }

        if (isFalling() && !canClimb()) {
            if (fallAccel > 0) {
                fallAccel *= fallSpeedMultiplier;
                setDirectionY(1 * fallAccel);
            }
        } else {
            fallAccel = 1;
            setDirectionY(0);
        }
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setJumping(boolean isJumping) {
        this.isJumping = isJumping;
    }

    @Override
    public void render(Camera cam) {
        double playerOffsetX = getLocation().getX() + cam.centerOffsetX;
        double playerOffsetY = getLocation().getY() + cam.centerOffsetY;
        Game game = getLevel().getManager().getEngine();

        if (isAttacking()) {
            playerOffsetX = playerOffsetX - 31;
            playerOffsetY = playerOffsetY - 8;
        }

        game.drawImage(getActiveFrame(), playerOffsetX, playerOffsetY, getWidth(), getHeight());

        if (cam.debugMode) {
            game.changeColor(Color.magenta);

            if (getBlockBelowEntityLeft() != null) {
                game.drawRectangle(getBlockBelowEntityLeft().getLocation().getX() + cam.centerOffsetX, getBlockBelowEntityLeft().getLocation().getY() + cam.centerOffsetY, Game.BLOCK_SIZE, Game.BLOCK_SIZE);
            }

            if (getBlockBelowEntityRight() != null) {
                game.drawRectangle(getBlockBelowEntityRight().getLocation().getX() + cam.centerOffsetX, getBlockBelowEntityRight().getLocation().getY() + cam.centerOffsetY, Game.BLOCK_SIZE, Game.BLOCK_SIZE);
            }

            double hitBoxOffsetX = getCollisionBox().getLocation().getX() + cam.centerOffsetX;
            double hitBoxOffsetY = getCollisionBox().getLocation().getY() + cam.centerOffsetY;
            game.changeColor(getHitboxColor());
            game.drawRectangle(hitBoxOffsetX, hitBoxOffsetY, getCollisionBox().getWidth(), getCollisionBox().getHeight());
        }
    }

    public void jump() {
        this.isJumping = true;
        this.jumpFrameIndex = 0;
        this.jumpAnimationTimer.start();
        this.timeJumping = 0;
    }

    private void initRunningSound() {
        try {
            File soundFile = new File("resources/sounds/runningSound.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            runningSound = AudioSystem.getClip();
            runningSound.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playerMovement(Set<Integer> keysPressed) {
        if (keysPressed.contains(32)) {//SPACE
            if (!isJumping() && !isAttacking() && (isOnGround() || canClimb())) {
                jump();
            }
        }
        if (keysPressed.contains(87)) {//W
            if (canClimb() && getBlockAtLocation(0, -1).getType() != BlockTypes.VOID) {
                setDirectionY(-1);
            }
        }
        if (keysPressed.contains(65)) {//A
            setDirectionX(-calculateHorizontalMovement());
        }
        if (keysPressed.contains(83)) {//S
            if (canClimb()) {
                setDirectionY(1);
            }
        }
        if (keysPressed.contains(68)) {//D
            setDirectionX(calculateHorizontalMovement());
        }
        if (keysPressed.contains(81) && canAttack()){
            attack();
        }

        // Play running sound only when moving horizontally and on the ground
        if ((keysPressed.contains(65) || keysPressed.contains(68)) && isOnGround()) {
            if (!runningSound.isRunning()) {
                runningSound.loop(Clip.LOOP_CONTINUOUSLY); // Start playing the running sound
            }
        } else {
            // Stop running sound when A or D key is released or player is not on the ground
            if (runningSound.isRunning()) {
                runningSound.stop();
            }
        }
    }

    public JProgressBar getHealthBar() {
        return healthBar;
    }

    @Override
    public double getWidth() {
        if (isAttacking()) {
            return 50 * getScale();
        }

        return ((BufferedImage) getIdleFrame()).getWidth() * getScale();
    }

    @Override
    public double getHeight() {
        if (isAttacking()) {
            return 37 * getScale();
        }

        return ((BufferedImage) getIdleFrame()).getHeight() * getScale();
    }

    public double calculateHorizontalMovement() {
        if (isAttacking()) {
            return 0;
        }

        if (isMovingVertically()) {
            return 0.75;
        }

        return 1;
    }

    private void animateCharacter() {
        if (isMovingHorizontally() && !isMovingVertically()) {
            if (!this.runAnimationTimer.isRunning()) {
                this.runAnimationTimer.start();
            }
        } else {
            this.runAnimationTimer.stop();
        }
    }

    public Image getRunFrame() {
        if (!isFlipped()) {
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_run_" + runFrameIndex));
        }

        return getLevel().getManager().getEngine().getTexture("player_run_" + runFrameIndex);
    }

    public Image getFallFrame() {
        if (!isFlipped()) {
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_jump_3"));
        }

        return getLevel().getManager().getEngine().getTexture("player_jump_3");
    }

    public Image getJumpFrame() {
        if (!isFlipped()) {
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_jump_" + jumpFrameIndex));
        }

        return getLevel().getManager().getEngine().getTexture("player_jump_" + jumpFrameIndex);
    }

    @Override
    public Image getActiveFrame() {
        if (isAttacking()) {
            return getAttackFrame();
        } else if (isJumping()) {
            return getJumpFrame();
        } else if (isFalling()) {
            return getFallFrame();
        } else if (isMovingHorizontally()) {
            return getRunFrame();
        }

        return getIdleFrame();
    }

    public Image getAttackFrame(){
        return getLevel().getManager().getEngine().getTexture("player_attack");
     }

    public void handleDeath() {
        score /= 2;
        if(score < 0){
            score = 0;
        }
    }


    @Override
    public Enemy getTarget() {
        for (Entity enemy : getLevel().getEntities()) {
            if (!enemy.isActive()) {
                continue;
            }

            if (enemy instanceof Enemy) {
                double midX = getLocation().getX() + (getWidth() / 2);
                if (Math.abs(Location.calculateDistance(enemy.getLocation().getX() + (enemy.getWidth() / 2), enemy.getLocation().getY(), midX, getLocation().getY())) < 64) {
                    return (Enemy) enemy;
                }
            }
        }

        return null;
    }

    public boolean canClimb() {
        return getBlockAtLocation() instanceof BlockClimbable;
    }

    public int getScore() {
        return score;
    }
}
