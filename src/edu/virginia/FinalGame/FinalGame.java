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
    private static int gameWidth = 500;
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

    ArrayList<Sprite> waitingFoodQueue = new ArrayList<>();
    ArrayList<Sprite> droppedFoodQueue = new ArrayList<>();
    SoundManager soundmanager = new SoundManager();

    Sprite player = new Sprite("player", "player.png");
    Sprite bar1 = new Sprite("bar1");
    Sprite bar2 = new Sprite("bar2");

    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    public ArrayList<Integer> previousPressedKeys;

    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public FinalGame(int numFoods) {
        super("Final Game", gameWidth, gameHeight);

        recentDropIndex = TIME_BETWEEN_DROPS-1; // first food drops immediately; -1 to avoid starting with null
        foodIndex = 0;
        totalNumFoods = numFoods;

        player.setPosition(barGap, barHeight + 10);
        player.setHitbox(barGap,barHeight+10,40,80);
        bar1.setPosition(0, 0);
        bar1.setHitbox(0, 0, barWidth, barHeight);
        bar2.setPosition(barWidth + barGap, 0);
        bar2.setHitbox(barWidth + barGap, 0, barWidth, barHeight);


        //create food icons and add to foodQueue
        Sprite avocado = new Sprite("avocado", "foods_resized/avocado_100.png", "veggie");
        waitingFoodQueue.add(avocado);
        Sprite steak = new Sprite("steak", "foods_resized/steak_100.png", "meat");
        waitingFoodQueue.add(steak);
        Sprite carrot = new Sprite("carrot", "foods_resized/carrot_100.png", "veggie");
        waitingFoodQueue.add(carrot);
        Sprite bacon = new Sprite("bacon", "foods_resized/bacon_100.png", "meat");
        waitingFoodQueue.add(bacon);
        for (int i = 0; i < waitingFoodQueue.size(); i++) {
            Sprite food = waitingFoodQueue.get(i);
            if (food.foodType == "veggie")
                food.setPosition(0, 0);
            else
                food.setPosition(barWidth + barGap, 0);
        }
    }

    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        if (player == null) return; // player is null on first frame

        super.update(pressedKeys);

        if (recentDropIndex >= TIME_BETWEEN_DROPS && foodIndex < totalNumFoods) {
            recentDropIndex = 0;
            droppedFoodQueue.add(waitingFoodQueue.get(foodIndex));
            foodIndex++;
        }

        //update each food position by applying gravity
        for (int i = 0; i < droppedFoodQueue.size(); i++) {
            Sprite food = droppedFoodQueue.get(i);
            food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
        }

        recentDropIndex++;

        if (player.getPosition().x > barGap &&
                pressedKeys.contains(KeyEvent.VK_LEFT) &&
                !previousPressedKeys.contains(KeyEvent.VK_LEFT)) {
            player.setPosition(player.getPosition().x - barWidth, player.getPosition().y);
        }
        if (player.getPosition().x < gameWidth - barWidth - barGap &&
                pressedKeys.contains(KeyEvent.VK_RIGHT) &&
                !previousPressedKeys.contains(KeyEvent.VK_RIGHT)) {
            player.setPosition(player.getPosition().x + barWidth, player.getPosition().y);
        }

        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        super.draw(g);
        player.draw(g);
        bar1.draw(g);
        bar2.draw(g);

        for (int i = 0; i < droppedFoodQueue.size(); i++) {
            droppedFoodQueue.get(i).draw(g);
        }

        if (win) {
            g.drawString("YOU WIN!",10, 100);
            this.stop();
        } else {
            Graphics2D g2d = (Graphics2D) g;
            for (int i = 0; i < 4; i++) {
                g2d.draw(bar1.getHitbox().lines.get(i));
                g2d.draw(bar2.getHitbox().lines.get(i));
            }
        }
    }

    /**
     * Quick main class that simply creates an instance of our game and starts the timer
     * that calls update() and draw() every frame
     * */
    public static void main(String[] args) {
        FinalGame game = new FinalGame(4);
        game.start();
    }
}
