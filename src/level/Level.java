package level;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import block.*;
import block.decorations.Decoration;
import block.decorations.DecorationTypes;
import block.decorations.FakeLightSpot;
import main.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Level {
    LevelManager manager;
    private Player player;
    private Door door;
    private Key key;
    private String nextLevel;

    int id;
    String name;
    int sizeWidth;
    int sizeHeight;
    ArrayList<String> lines;
    HashMap<Integer, TextMessage> textMessages;
    BlockGrid grid;
    private int textCounter;

    private final String levelDoc;
    public final double gravity = 9.8;
    public double scale = 2;
    String backgroundImgFilePath;
    Location spawnPoint;
    Location keyLoc;
    Location doorLoc;

    ArrayList<Decoration> decorations;
    ArrayList<FakeLightSpot> spotLights;
    ArrayList<Entity> entities;

    public Level(LevelManager manager, int id, String levelDoc) {
        this.manager = manager;
        this.id = id;
        this.levelDoc = levelDoc;
        this.lines = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.decorations = new ArrayList<>();
        this.spotLights = new ArrayList<>();
        this.textMessages = new HashMap<>();
        init();
    }

    public void init() {
        System.out.println("init!");
        File file = new File(levelDoc);
        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                lines.add(fileReader.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't locate file!");
            return;
        }

        name = lines.get(0).substring("name: ".length());
        backgroundImgFilePath = lines.get(1).substring("background: ".length());
        nextLevel = lines.get(2).substring("next_level: ".length());
        sizeWidth = Integer.parseInt(lines.get(3).substring("level_width: ".length()));
        sizeHeight = Integer.parseInt(lines.get(4).substring("level_height: ".length()));
        System.out.println(sizeWidth);
        System.out.println(sizeHeight);
        this.grid = new BlockGrid(sizeWidth, sizeHeight);
    }

    public void load() {
        int relY = 0;
        for (int y = 6; y < lines.size(); y++) {
            String line = lines.get(y);
            line = line.substring(3);
            for (int x = 0; x < line.length(); x++) {
                if (line.charAt(x) == 'P') {
                    double spawnX = x * Game.BLOCK_SIZE;
                    double spawnY = relY * Game.BLOCK_SIZE;

                    player = new Player(this, new Location(spawnX, spawnY));

                    spawnY = spawnY - (player.getHeight() - Game.BLOCK_SIZE);
                    player.setLocation(player.getLocation().getX(), spawnY);
                    spawnPoint = new Location(spawnX, spawnY);
                } else if (line.charAt(x) == 'K') {
                    keyLoc = new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE);
                    key = new Key(this, keyLoc);

                    double heightDiff = key.getLocation().getY() - (key.getHeight() - Game.BLOCK_SIZE);
                    key.setLocation(key.getLocation().getX(), heightDiff);
                    addEntity(key);
                } else if (line.charAt(x) == 'D' ||line.charAt(x) == 'd') {
                    doorLoc = new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE);
                    door = new Door(this, doorLoc);
                    if(line.charAt(x) == 'd'){
                        door.setType(EntityType.STONE_DOOR);
                    }

                    double heightDiff = door.getLocation().getY() - (door.getHeight() - Game.BLOCK_SIZE);
                    door.setLocation(door.getLocation().getX(), heightDiff);
                    addEntity(door);
                } else if (line.charAt(x) == 'X') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.DIRT, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'G') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.GRASS, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'L') {
                    grid.setBlock(x, relY, new BlockClimbable(BlockTypes.LADDER, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'E') {
                    Location enemyLoc = new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE);
                    EnemyPlant enemy = new EnemyPlant(this, enemyLoc);

                    double heightDiff = enemy.getLocation().getY() - Game.BLOCK_SIZE - 16;
                    enemy.setLocation(enemy.getLocation().getX(), heightDiff);
                    addEntity(enemy);
                } else if (line.charAt(x) == 'B') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.BARRIER, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == '@') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.FOREST_GRASS, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == '!') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.FOREST_GROUND, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'W') {
                    grid.setBlock(x, relY, new BlockLiquid(BlockTypes.WATER_TOP, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'O') {
                    grid.setBlock(x, relY, new BlockLiquid(BlockTypes.WATER_BOTTOM, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'S') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.STONE_FLOOR, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 's') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.STONE_FILLER, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'l') {
                    grid.setBlock(x, relY, new BlockLiquid(BlockTypes.LAVA, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'p') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.BRIDGE, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'm') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.BL, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'r') {
                    grid.setBlock(x, relY, new BlockSolid(BlockTypes.BR, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE)));
                } else if (line.charAt(x) == 'h') {
                    Location heartLoc = new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE);
                    Heart heart = new Heart(this, heartLoc);
                    addEntity(heart);
                } else if (line.charAt(x) == 'R') {
                    addDecoration(DecorationTypes.ROCK, x, relY);
                } else if (line.charAt(x) == 'b') {
                    addDecoration(DecorationTypes.BUSH, x, relY);
                } else if (line.charAt(x) == 't') {
                    addDecoration(DecorationTypes.TREE, x, relY);
                } else if (line.charAt(x) == 'z') {
                    addDecoration(DecorationTypes.LAMP_POST, x, relY);
                } else if (line.charAt(x) == '0') {
                    addDecoration(DecorationTypes.TREE_WILLOW_0, x, relY);
                } else if (line.charAt(x) == '1') {
                    addDecoration(DecorationTypes.TREE_WILLOW_1, x, relY);
                } else if (line.charAt(x) == '2') {
                    addDecoration(DecorationTypes.TREE_WILLOW_2, x, relY);
                } else if (line.charAt(x) == '3') {
                    addDecoration(DecorationTypes.HANGING_VINE, x, relY);
                } else if (line.charAt(x) == '4') {
                    addDecoration(DecorationTypes.HANGING_VINE_FLOWERS, x, relY);
                }

            }

            relY++;
        }
        if (!backgroundImgFilePath.isEmpty()) {
            getManager().getEngine().imageBank.put("background", Toolkit.getDefaultToolkit().createImage(backgroundImgFilePath));
        }
        if (player == null) {
            System.out.println("level.Level error: no player location specified.");
            return;
        }
        if (keyLoc == null) {
            System.out.println("level.Level error: no key location specified.");
            return;
        }
        if (doorLoc == null) {
            System.out.println("level.Level error: no door location specified.");
            return;
        }

        System.out.println("Player: " + player.getLocation().toString());
        System.out.println("Key: " + keyLoc.toString());
        System.out.println("Door: " + doorLoc.toString());
    }


    public void update(double dt) {
        getPlayer().playerMovement(getManager().getEngine().keysPressed);
        getPlayer().update(dt);

        Iterator<Entity> iter = getEntities().iterator();
        while (iter.hasNext()) {
            Entity entity = iter.next();
            if (entity.isActive()) {
                entity.update(dt);
            } else {
                iter.remove();
            }
        }

        for (FakeLightSpot spotLight : getSpotLights()) {
            spotLight.update(dt);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void reset() {
        getPlayer().setLocation(spawnPoint.getX(), spawnPoint.getY());
        getPlayer().setHealth(getPlayer().getMaxHealth());
    }

    public Door getDoor() {
        return door;
    }

    public String getNextLevel() {
        return nextLevel;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public LevelManager getManager() {
        return manager;
    }

    public int getWidth() {
        return sizeWidth;
    }

    public int getHeight() {
        return sizeHeight;
    }

    public Location getKeyLocation() {
        return keyLoc;
    }

    public Location getDoorLocation() {
        return doorLoc;
    }

    public double getGravity() {
        return 0.98;
    }

    public BlockGrid getBlockGrid() {
        return grid;
    }

    public ArrayList<FakeLightSpot> getSpotLights() {
        return spotLights;
    }

    public void addTextMessage(TextMessage text) {
        textMessages.put(textCounter, text);
        textCounter++;
    }

    public HashMap<Integer, TextMessage> getTextMessages() {
        return textMessages;
    }

    public void clearTextMessages() {
        textMessages.clear();
    }

    public ArrayList<Decoration> getDecorations() {
        return decorations;
    }

    private void addDecoration(DecorationTypes type, int x, int relY) {
        BufferedImage texture = (BufferedImage) getManager().getEngine().getTexture(type.toString());
        Decoration decoTree = new Decoration(type, new Location(x * Game.BLOCK_SIZE, relY * Game.BLOCK_SIZE), texture.getWidth(), texture.getHeight());
        decorations.add(decoTree);

        if (decoTree.getType().hasLightSpots()) {
            FakeLightSpot spotLight = new FakeLightSpot(decoTree);
            spotLights.add(spotLight);
        }
    }
}
