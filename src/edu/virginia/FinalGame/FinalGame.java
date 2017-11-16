package edu.virginia.FinalGame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import edu.virginia.engine.display.Sprite;
import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.SoundManager;
import edu.virginia.engine.display.DisplayObject;

public class FinalGame extends Game {

    private static int gameHeight = 800;
    private static int visibleGameHeight = 700; //this seems to be the actual bottom of the screen
    private static int gameWidth = 800;
    private static boolean collision = false;
    private static boolean win = false;
    private static int score = 0;
    private final int TIME_BETWEEN_DROPS = 100;

    //parameters for physics
    private int gravity = 5;

    private int barWidth = 100;
    private int barHeight = visibleGameHeight - 100;
    private int barGap = 10;
    private int recentDropIndex = 0;
    private int foodIndex;
    private int totalNumFoods;
    private int totalNumBars;

    private ArrayList<Sprite> bars = new ArrayList<>();
    private ArrayList<Sprite> waitingFoodQueue = new ArrayList<>();
    private ArrayList<Sprite> droppedFoodQueue = new ArrayList<>();
    private SoundManager soundmanager = new SoundManager();

    private Sprite player = new Sprite("player", "player.png");

    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    private ArrayList<Integer> previousPressedKeys;

    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public FinalGame(int numFoods, int numBars) {
        super("Final Game", gameWidth, gameHeight);

        recentDropIndex = TIME_BETWEEN_DROPS-1; // first food drops immediately; -1 to avoid starting with null
        foodIndex = 0;
        totalNumFoods = numFoods;
        totalNumBars = numBars;

        player.setPosition(barGap, barHeight + 10);
        player.setHitbox(barGap,barHeight+10,40,80);

        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox((barWidth + barGap) * i, 0, barWidth, barHeight);
            bars.add(newBar);
        }

    }

    private void addFood(String id, String fileName, String foodType) {
        Sprite food = new Sprite(id,fileName,foodType);
        if (food.foodType == "veggie") {
            food.setPosition(0, 0);
        } else if (food.foodType == "meat") {
            food.setPosition(barWidth+barGap, 0);
        }
        food.setHitbox(food.getPosition().x,food.getPosition().y,100,100);
        waitingFoodQueue.add(food);
    }

    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        if (player == null) return; // player is null on first frame
        super.update(pressedKeys);

        // drop next food on a timed interval
        if (recentDropIndex >= TIME_BETWEEN_DROPS && foodIndex < totalNumFoods) {
            recentDropIndex = 0;
            droppedFoodQueue.add(waitingFoodQueue.get(foodIndex));
            foodIndex++;
        }
        recentDropIndex++;

        //update each food position by applying gravity
        for (int i = 0; i < droppedFoodQueue.size(); i++) {
            Sprite food = droppedFoodQueue.get(i);

            // check for player collision before updating position
            if (player.collidesWith(food)) {
                droppedFoodQueue.remove(food);
            } else {
                food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
            }
        }

        // player movement
        if (player.getPosition().x > barGap &&
                pressedKeys.contains(KeyEvent.VK_LEFT) &&
                !previousPressedKeys.contains(KeyEvent.VK_LEFT)) {
            player.setPosition(player.getPosition().x - barWidth - barGap, player.getPosition().y);
        }
        if (player.getPosition().x < (totalNumBars-1) * (barWidth + barGap) &&
                pressedKeys.contains(KeyEvent.VK_RIGHT) &&
                !previousPressedKeys.contains(KeyEvent.VK_RIGHT)) {
            player.setPosition(player.getPosition().x + barWidth + barGap, player.getPosition().y);
        }

        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        if (player == null) return;

        super.draw(g);
        player.draw(g);

        for (int i = 0; i < droppedFoodQueue.size(); i++) {
            droppedFoodQueue.get(i).draw(g);
        }

        if (win) {
            g.drawString("YOU WIN!",10, 100);
            this.stop();
        } else {
            Graphics2D g2d = (Graphics2D) g;
            // bars have no sprites, just draw the hitboxes to achieve the same effect
            for (int i = 0; i < bars.size(); i++) {
                for (int j = 0; j < 4; j++) {
                    g2d.draw(bars.get(i).getHitbox().lines.get(j));
                }
            }
        }
    }

    /**
     * Quick main class that simply creates an instance of our game and starts the timer
     * that calls update() and draw() every frame
     * */
    public static void main(String[] args) {
        FinalGame level1 = new FinalGame(4, 5);
        level1.addFood("avocado", "foods_resized/avocado_100.png", "veggie");
        level1.addFood("steak", "foods_resized/steak_100.png", "meat");
        level1.addFood("carrot", "foods_resized/carrot_100.png", "veggie");
        level1.addFood("bacon", "foods_resized/bacon_100.png", "meat");
        level1.start();
    }
}
