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

    // size parameters
    private static int gameHeight = 800;
    private static int visibleGameHeight = 700; //this seems to be the actual bottom of the screen
    private static int gameWidth = 800;
    private int barWidth = 100;
    private int barHeight = visibleGameHeight - 100;
    private int barGap = 10;

    // gameplay parameters
    private boolean playing;
    private boolean win;
    private boolean lose;
    private final int TIME_BETWEEN_DROPS = 100;
    private int recentDropIndex;
    private int foodIndex;
    private int totalNumFoods;
    private int totalNumBars;
    private int gravity;

    // game elements
    private ArrayList<Sprite> bars = new ArrayList<>();
    private ArrayList<Sprite> waitingFoodQueue = new ArrayList<>();
    private ArrayList<Sprite> droppedFoodQueue = new ArrayList<>();
    private Sprite player = new Sprite("player", "player.png");
    private SoundManager soundmanager = new SoundManager();

    //progress bars of food stacks
    private final int LV1_GOAL = 5;
    private final int LV1_LIMIT = 7;

    private FoodStack meatStack = new FoodStack(LV1_GOAL, LV1_LIMIT, "meat");
    private FoodStack veggieStack = new FoodStack(LV1_GOAL, LV1_LIMIT, "veggie");
    private ArrayList<FoodStack> stacks = new ArrayList<>();




    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    private ArrayList<Integer> previousPressedKeys;



    private class FoodQueue {
        //inserted at the end, first element pops up
        private ArrayList<Sprite> queue;

        private FoodQueue() {
            queue = new ArrayList<>();
        }
        private void enqueue(Sprite s) {
            queue.add(s); //at end of list
        };
        private Sprite dequeue() {
            return queue.remove(0); //remove from the beginning. subsequent elements shifted
        };
        private Sprite next() {
            return queue.get(0);
        }
        private boolean isEmpty() {
            return queue.isEmpty();
        };
        private int size() {
            return queue.size();
        }
    }

    private class FoodStack { //for the progress bars
        //inserted at begining, removed from beginning
        private ArrayList<Sprite> stack;
        private int goal;
        private int limit;
        private String id;

        private FoodStack(int goal, int limit, String id) {
            stack = new ArrayList<>();
            this.goal = goal;
            this.limit = limit;
            this.id = id;
        }
        private void push(Sprite s) {
            stack.add(0, s); //at head of list
            System.out.println("pushed to " + id + ", new size: " + stack.size());
        };
        private Sprite pop() {
            return stack.remove(0); //remove from the beginning. subsequent elements shifted
        };
        private Sprite peek() {
            return stack.get(0);
        }
        private boolean isEmpty() {
            return stack.isEmpty();
        };
        private int size() {
            return stack.size();
        }
        private boolean reachedGoal() {
            return stack.size() >= this.goal;
        }
        private boolean reachedLimit() {
            return stack.size() >= this.limit;
        }

    }


    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public FinalGame(int numFoods, int numBars) {
        super("Final Game", gameWidth, gameHeight);

        playing = true;
        win = false;
        lose = false;
        recentDropIndex = TIME_BETWEEN_DROPS-1; // first food drops immediately; -1 to avoid starting with null
        foodIndex = 0;
        totalNumFoods = numFoods;
        totalNumBars = numBars;
        gravity = 5;

        player.setPosition(barGap, barHeight + 10);
        player.setHitbox(barGap,barHeight+10,40,80);

        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox((barWidth + barGap) * i, 0, barWidth, barHeight);
            bars.add(newBar);
        }

        stacks.add(meatStack);
        stacks.add(veggieStack);



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


    //updates the progress of the game by checking for each progress bar size against goals and limits
    private void updateProgress() {
        //win and lose would be false if this function is called
        boolean no_limit_reached = true; //first assume no limits are reached
        boolean all_goals_reached = true; //also assume not all goals are reached
        for (int i = 0; i < stacks.size(); i++) {
            FoodStack stack = stacks.get(i);
            System.out.println("size of " + stack.id + " " + stack.stack.size());
            if (stack.reachedLimit()) { //if a stack has exceeded limit
                //System.out.println("this stack reached limit");
                no_limit_reached = false;
            }
            if (!stack.reachedGoal()) { //if any one of the stacks have not reached goal, break. win = false
                //System.out.println("this stack did not reach goal");
                all_goals_reached = false;
            }

        }

        win = all_goals_reached;
        lose = !no_limit_reached;

        if (win == true || lose == true) {
            System.out.println("playing set to false, win: " + win + " lose: " + lose);
            playing = false;
        }

    }



    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        //boolean prev_collision = false;

        if (player == null) return; // player is null on first frame
        super.update(pressedKeys);

        if (playing) {
            // drop next food on a timed interval
            // if timer for this food exceeded TIME_BETWEEN_DROPS and there are more foods to come, reset dropindex
            // pick the most recent food from watingFoodQueue and add to droppedFoodQueue
            if (recentDropIndex >= TIME_BETWEEN_DROPS && foodIndex < totalNumFoods) {
                recentDropIndex = 0;
                droppedFoodQueue.add(waitingFoodQueue.get(foodIndex));
                foodIndex++;
            }
            recentDropIndex++;

            //update each food position by applying gravity
            System.out.println("droppedFoodQueue size: " + droppedFoodQueue.size());
            for (int i = 0; i < droppedFoodQueue.size(); i++) {
                Sprite food = droppedFoodQueue.get(i);

                // check for player collision before updating position
                if (player.collidesWith(food)) {
                    droppedFoodQueue.remove(food);

                    System.out.println("collided");
                    //put on FoodStack
                    if (food.foodType == "veggie") {
                        veggieStack.push(food);
                    } else if (food.foodType == "meat") {
                        meatStack.push(food);
                    }

                    //prev_collision = true;

                }
                else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity); //each food keeps on dropping
                    //prev_collision = false;
                }
            }

            // player controls
            if (player.getPosition().x > barGap &&
                    pressedKeys.contains(KeyEvent.VK_LEFT) &&
                    !previousPressedKeys.contains(KeyEvent.VK_LEFT)) {
                player.setPosition(player.getPosition().x - barWidth - barGap, player.getPosition().y);
            }
            if (player.getPosition().x < (totalNumBars - 1) * (barWidth + barGap) &&
                    pressedKeys.contains(KeyEvent.VK_RIGHT) &&
                    !previousPressedKeys.contains(KeyEvent.VK_RIGHT)) {
                player.setPosition(player.getPosition().x + barWidth + barGap, player.getPosition().y);
            }
        }

        // pause
        if (pressedKeys.contains(KeyEvent.VK_P) &&
                !previousPressedKeys.contains(KeyEvent.VK_P)){
            playing = !playing;
        }

        updateProgress(); //update progress of the game based on level goals and limits



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

        if (!playing) g.drawString("GAME PAUSED", gameWidth*3/4, gameHeight*1/4);

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
        FinalGame level1 = new FinalGame(40, 5);
        for (int i = 0; i < 10; i++) {
            level1.addFood("avocado", "foods_resized/avocado_100.png", "veggie");
            level1.addFood("steak", "foods_resized/steak_100.png", "meat");
            level1.addFood("carrot", "foods_resized/carrot_100.png", "veggie");
            level1.addFood("bacon", "foods_resized/bacon_100.png", "meat");
        }
        System.out.println("size: " + level1.waitingFoodQueue.size());
        level1.start();
    }
}
