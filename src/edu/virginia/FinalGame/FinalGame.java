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

    public static int gameHeight = 800;
    public static int visibleGameHeight = 460; //this seems to be the actual bottom of the screen
    public static int gameWidth = 500;
    public static boolean collision = false;
    public static boolean win = false;
    public static int score = 0;
    public static double maxScale = 2.0;
    public static double minScale = 0.1;

    //parameters for physics
    public int gravity = 1;

    public int barWidth = 100;
    public int barGap = 10;
    public int recentDropIndex = 0;

    ArrayList<Sprite> foodQueue = new ArrayList<>();
    SoundManager soundmanager = new SoundManager();

    Sprite bar1 = new Sprite("bar1");
    Sprite bar2 = new Sprite("bar2");


    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    public ArrayList<Integer> previousPressedKeys;

    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public FinalGame() {
        super("Final Game", gameWidth, gameHeight);

        bar1.setPosition(0, 0);
        bar1.setHitbox(0, 0, barWidth, gameHeight);
        bar2.setPosition(barWidth + barGap, 0);
        bar2.setHitbox(barWidth + barGap, 0, barWidth, gameHeight);


        //create food icons and add to foodQueue
        Sprite avocado = new Sprite("avocado", "foods_resized/avocado_100.png", "veggie");
        foodQueue.add(avocado);
        Sprite carrot = new Sprite("carrot", "foods_resized/carrot_100.png", "veggie");
        foodQueue.add(carrot);
        Sprite steak = new Sprite("steak", "foods_resized/steak_100.png", "meat");
        foodQueue.add(steak);
        Sprite bacon = new Sprite("bacon", "foods_resized/bacon_100.png", "meat");
        foodQueue.add(bacon);

        for (int i = 0; i < foodQueue.size(); i++) {
            Sprite food = foodQueue.get(i);
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
        //update each food position by applying gravity
        for (int i = 0; i < foodQueue.size(); i++) {
            Sprite food = foodQueue.get(i);
            food.setPosition(food.getPosition().x, food.getPosition().y + gravity * (i+1));
        }

        super.update(pressedKeys);
        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        super.draw(g);
        bar1.draw(g);
        bar2.draw(g);

        for (int i = 0; i < foodQueue.size(); i++) {
            foodQueue.get(i).draw(g);
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
        FinalGame game = new FinalGame();
        game.start();
    }
}
