package level;

import main.Game;
import main.GameEngine;
import main.Location;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class LevelManager {

    Game engine;

    HashMap<String, Level> levels;
    
    private Instant startTime;
    private Instant endTime;
    private boolean timerRunning;
    private Duration elapsedTime;

    // Define the level variable name.
    public Level DEMO;
    public Level LEVEL_4;
    public Level FOREST;
    public Level LEVEL_5;
    public Level END;
    public Level LEVEL_2;


    public LevelManager(Game engine) {
        this.engine = engine;
        this.levels = new HashMap<>();
        loadLevels();
        elapsedTime = Duration.ZERO;
    }

    public void loadLevels() {
        // Assign the level name to your level. This points to your level's file under resources/levels.
        // Format: *NAME* = new level.Level(id, spawn_location, key_location)
        DEMO = new Level(this, 0, "resources/levels/level_demo.txt");
        LEVEL_2 = new Level(this, 1, "resources/levels/nap_level.txt");
        LEVEL_4 = new Level(this,2,"resources/levels/zyra_map.txt");
        FOREST = new Level(this,3,"resources/levels/level_forest.txt");
        LEVEL_5 = new Level(this,4,"resources/levels/level5.txt");
        END = new Level(this,5,"resources/levels/endLevel.txt");




        levels.put("level_demo", DEMO);
        levels.put("nap_level", LEVEL_2);
        levels.put("level4", LEVEL_4);
        levels.put("forest", FOREST);
        levels.put("level_5", LEVEL_5);
        levels.put("end", END);

        /*
         *  This is for adding text to the level. You set the x and y coordinates where it should show.
         *  The boolean static means if the text should show at a fixed position in the world e.g. the player
         *  can walk past it or if static is set to true, then the text will show at a constant position on the screen
         *  and follow the player as they move.
         */
        DEMO.addTextMessage(new TextMessage(new Location(10, 100), "Welcome to our game!", 50, false, Color.black));
        DEMO.addTextMessage(new TextMessage(new Location(10, 200), "Press 'D' to move right and 'A' to move left.", 20, false, Color.black));
        DEMO.addTextMessage(new TextMessage(new Location(10, 330), "Hold 'Q' to attack with your sword.", 20, false, Color.black));
        DEMO.addTextMessage(new TextMessage(new Location(10, 360), "Press 'Space' to jump!", 20, false, Color.black));
        DEMO.addTextMessage(new TextMessage(new Location(10, 380), "Grab key to unlock door to proceed to next level!", 20, false, Color.black));
        DEMO.addTextMessage(new TextMessage(new Location(570, 670), "Press 'E' on door to enter!", 20, false, Color.black));
    }

        // Method to start the timer
    public void startTimer() {
        if (!timerRunning) {
            startTime = Instant.now();
            timerRunning = true;
        }
    }

    // Method to stop the timer
    public void stopTimer() {
        if (timerRunning) {
            endTime = Instant.now();
            elapsedTime = Duration.between(startTime, endTime); // Store the elapsed time
            timerRunning = false;
        }
    }

    // Method to get the elapsed time
    public Duration getElapsedTime() {
        if (timerRunning) {
            return Duration.between(startTime, Instant.now());
        }
        return elapsedTime; // Return the stored elapsed time when the timer is stopped
    }

    public void onTutorialComplete() {
        startTimer();
    }

    public void onLastLevelEnter() {
        stopTimer();
    }

    public Game getEngine() {
        return engine;
    }

    public HashMap<String, Level> getLevels() {
        return levels;
    }
}
