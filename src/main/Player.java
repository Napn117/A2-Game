package main;

import block.Block;
import block.BlockClimbable;
import block.BlockTypes;
import level.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Set;

public class Player extends Entity {
    private JProgressBar healthBar;
    private GameEngine.AudioClip attack;

    private boolean keyObtained;
    private boolean doorTouched;
    private boolean attackRegistered = false;
    private boolean isJumping;
    private boolean hasKey;
    private double attackCounter;

    private double ATTACK_COOLDOWN = 0.4;

    private Timer runAnimationTimer;
    private int runFrameIndex;

    public Timer jumpAnimationTimer;
    private int jumpFrameIndex;
    private double timeJumping;
    private double maxJumpTime = 0.20; //seconds

    Image gifImage;
    Image plantAttack;
    Image gifImage2;
    Image level1;

    private Enemy target;

    public Player(Level level, Location loc) {
        super(EntityType.PLAYER, level, loc, 19, 29);



        setHitboxColor(Color.cyan);
        setMaxHealth(100);
        setHealth(getMaxHealth());
        setDirectionY(1);

        init();
    }

    public void init() {
        gifImage = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        plantAttack = Toolkit.getDefaultToolkit().createImage("resources/images/plantAttack.gif");

        gifImage2 = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        level1 = Toolkit.getDefaultToolkit().createImage("resources/images/level1.gif");

        this.healthBar = new JProgressBar(0, getMaxHealth());
        this.healthBar.setBounds(100, 25, 100, 10); // Adjust position and size as needed
        this.healthBar.setForeground(Color.RED); // Set the color
        this.healthBar.setValue(getMaxHealth()); // Set initial health
        this.healthBar.setStringPainted(true); // Show health value

        this.runAnimationTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runFrameIndex = (runFrameIndex + 1) % 4;
                //System.out.println("Run " + currentFrameIndex);
            }
        });

        this.jumpAnimationTimer = new Timer(200, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Jump: " + jumpFrameIndex);
                jumpFrameIndex = (jumpFrameIndex + 1) % 4;
            }
        });
    }

    public void update(double dt) {
        super.update(dt);
        animateCharacter();
        System.out.println(attackCounter);
        if (attackCounter < ATTACK_COOLDOWN) {
            attackCounter += 1 * dt;
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

        if (cam.showHitboxes) {
            game.changeColor(Color.magenta);

            double hitBoxOffsetX = getCollisionBox().getLocation().getX() + cam.centerOffsetX;
            double hitBoxOffsetY = getCollisionBox().getLocation().getY() + cam.centerOffsetY;

            Block blockBelow = getBlockBelowEntity();
            if (blockBelow != null) {
                game.drawRectangle(blockBelow.getLocation().getX() + cam.centerOffsetX, blockBelow.getLocation().getY() + cam.centerOffsetY, Game.BLOCK_SIZE, Game.BLOCK_SIZE);
            }

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

    public void playerMovement(Set<Integer> keysPressed) {
        if (keysPressed.contains(32)) {//SPACE
            if (!isJumping() && (isOnGround() || canClimb())) {
                //System.out.println("Jump!");
                jump();
            } else {
                //System.out.println("Not on ground!");
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
        if (keysPressed.contains(81)){
            Attack();
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
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_jump_" + runFrameIndex));
        }

        return getLevel().getManager().getEngine().getTexture("player_jump_" + runFrameIndex);
    }

    public boolean isAttacking() {
        return attackCounter <= ATTACK_COOLDOWN;
    }

    @Override
    public Image getActiveFrame() {
        if (isAttacking()) {
            return getAttackFrame();
        } else if (isMovingVertically()) {
            return getFallFrame();
        } else if (isMovingHorizontally()) {
            return getRunFrame();
        }

        return getIdleFrame();
    }

      public Image getAttackFrame(){
        return getLevel().getManager().getEngine().getTexture("player_attack");
     }

     public void Attack(){
        if (!canAttack()) {
            return;
        }


        attack = getLevel().getManager().getEngine().loadAudio("resources/sounds/attackSound.wav");
        getLevel().getManager().getEngine().playAudio(attack);
        attackCounter = 0;

        Enemy target = getTarget();
        if (target == null) {
            return;
        }
        System.out.println(target.getHealth());

        getTarget().setHealth(target.getHealth()- 2);
        System.out.println(target.getHealth());
        if (target.getHealth() <= 0){
            target.setDamage(0);
            target.destroy();
        }
    }

    public boolean canAttack() {
        return attackCounter >= ATTACK_COOLDOWN;
    }

    public Enemy getTarget() {
        for (Entity enemy : getLevel().getEntities()){
            if (enemy instanceof Enemy) {
                if (Location.calculateDistance(getLocation().getX(), getLocation().getY(), enemy.getLocation().getX(), enemy.getLocation().getY()) < 64) {
                    System.out.println("Close");
                    return (Enemy) enemy;
                }
            }
        }

        return null;
    }

    public void setTarget(Enemy p) {
        this.target = p;
    }








    public boolean canClimb() {
        return getBlockAtLocation() instanceof BlockClimbable;
    }

    public boolean hasObtainedKey() {
        return this.keyObtained;
    }

    public void setKeyObtained(boolean keyObtained) {
        this.keyObtained = keyObtained;
    }

    public boolean isTouchingDoor() {
        return this.doorTouched;
    }

    public void setTouchingDoor(boolean doorTouched) {
        this.doorTouched = doorTouched;
    }

    public boolean hasRegisteredAttack() {
        return this.attackRegistered;
    }

    public void setAttackRegistered(boolean attackRegistered) {
        this.attackRegistered = attackRegistered;
    }
}
