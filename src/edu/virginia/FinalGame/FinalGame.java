package edu.virginia.FinalGame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;

import edu.virginia.engine.display.Sprite;
import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.SoundManager;
import edu.virginia.engine.display.DisplayObject;

public class FinalGame extends Game {

    // size parameters
    private static int gameHeight = 800;
    private static int visibleGameHeight = 700; //this seems to be the actual bottom of the screen
    private static int gameWidth = 1500;
    private int barWidth = 100;
    private int barHeight = visibleGameHeight - 100;
    private int barGap = 10;
    private int playProgressGap = 100;


    private Font myFont = new Font ("Courier New", 1, 20);

    // gameplay parameters
    private boolean playing;
    private boolean win;
    private boolean lose;
    private boolean tutorial;
    private int level;
    private final int TIME_BETWEEN_DROPS = 30;
    private static int multiples = 5; //how many same food item would be included in a gameplay
    private int recentDropIndex;
    private int foodIndex;
    private int totalNumFoods;
    private int totalNumBars;
    private int gravity;
    private ArrayList<String> categories;
    private int tutorialIndex; //for each step of tutorial
    private int tutorialTimeIndex; //to track how long the pause should continue

    // game elements
    // location,
    private ArrayList<Integer> progressBarParams = new ArrayList<>();
    private ArrayList<Sprite> bars = new ArrayList<>();
    private ArrayList<Sprite> waitingFoodQueue = new ArrayList<>();
    private ArrayList<Sprite> droppedFoodQueue = new ArrayList<>();
    private ArrayList<String> filenames = new ArrayList<>();
    private ArrayList<String> filecategories = new ArrayList<>();
    private ArrayList<String> textTutorial = new ArrayList<>();
    private ArrayList<String> foodNamesTutorial = new ArrayList<>(); //in case each food item has to be explained
    private Sprite player = new Sprite("player", "player.png");

    private Sprite left = new Sprite("left", "left.png");
    private Sprite right = new Sprite("right", "right.png");
    private Sprite up = new Sprite("up", "up.png");
    private Sprite down = new Sprite("down", "down.png");



    private SoundManager soundmanager = new SoundManager();

    //progress bars of food stacks
    private int goal;
    private int limit;
    //private final int LV1_GOAL = 3;
    //private final int LV1_LIMIT = 5;

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
    public FinalGame(int numBars, int gravity, int goal, int limit, ArrayList<String> categories, ArrayList<String> filenames, ArrayList<String> filecategories) {
        super("Final Game", gameWidth, gameHeight);

        this.filenames = filenames;
        this.filecategories = filecategories;
        level = 1;
        playing = true;
        win = false;
        lose = false;
        tutorial = true;
        recentDropIndex = TIME_BETWEEN_DROPS-1; // first food drops immediately; -1 to avoid starting with null
        foodIndex = 0;
        totalNumFoods = filenames.size() * 10;
        totalNumBars = numBars;
        this.goal = goal;
        this.limit = limit;
        this.gravity = gravity;
        this.categories = categories;
        this.tutorialIndex = 0; //start from tutorial step 0

        player.setPosition(barGap, barHeight + 10);
        player.setHitbox(barGap,barHeight+10,40,80);


        left.setPosition(gameWidth * 3/4 - 100, gameHeight /2);
        right.setPosition(gameWidth * 3/4 + 100, gameHeight /2);
        up.setPosition(gameWidth * 3/4, gameHeight /2 - 100);
        down.setPosition(gameWidth * 3/4, gameHeight /2);

        //for the gameplay bars
        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox((barWidth + barGap) * i, 0, barWidth, barHeight);
            bars.add(newBar);
        }
        //for the progress bars
        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox(playProgressGap + (barWidth + barGap) * (i + totalNumBars), 0, barWidth, barHeight);
            bars.add(newBar);
        }

        //initialize progressBarParams (x, y, width, height for each bar)
        for (int i = 0; i < totalNumBars; i++) {
            progressBarParams.add(playProgressGap + (barWidth + barGap) * (i + totalNumBars));
            progressBarParams.add(barHeight);
            progressBarParams.add(barWidth);
            progressBarParams.add(0); //0 height at first
        }


        for (int i = 0; i < categories.size(); i++) {
            stacks.add(new FoodStack(goal, limit, categories.get(i)));
        }

        for (int i = 0; i < filenames.size(); i++) {
            this.addFood(filenames.get(i), filenames.get(i), filecategories.get(i));
        }

        //maybe don't shuffle collections?
        //Collections.shuffle(this.waitingFoodQueue);


        this.textTutorial.add("highlight the character");
        this.textTutorial.add("push the right button");
        this.textTutorial.add("push the left button");
        this.textTutorial.add("drop & show the veggie group - food 1");
        this.textTutorial.add("drop & show the veggie group - food 2"); //should we do this?
        /*
        this.textTutorial.add("drop & show the veggie group - food 3");
        this.textTutorial.add("drop & show the veggie group - food 4");
        this.textTutorial.add("drop & show the meat group - food 1");
        this.textTutorial.add("drop & show the meat group - food 2");
        this.textTutorial.add("drop & show the meat group - food 3");
        this.textTutorial.add("drop & show the meat group - food 4");
        this.textTutorial.add("drop & show the grain group - food 1");
        this.textTutorial.add("drop & show the grain group - food 2");
        this.textTutorial.add("drop & show the grain group - food 3");
        this.textTutorial.add("drop & show the grain group - food 4");
        */
        this.textTutorial.add("collect radish!");
        this.textTutorial.add("avoid garlic!");
        this.textTutorial.add("collect shrimp and see the meat bar go up!");



    }



    private void addFood(String id, String fileName, String foodType) {
        Sprite food = new Sprite(id,fileName,foodType);
        food.setPosition((barWidth+barGap) * categories.indexOf(food.foodType), 0);
        /*
        if (food.foodType == "veggie") {
            food.setPosition(0, 0);
        } else if (food.foodType == "meat") {
            food.setPosition(barWidth+barGap, 0);
        }
        */
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
            //System.out.println("size of " + stack.id + " " + stack.stack.size());
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

        if (win || lose) {
            System.out.println("playing set to false, win: " + win + " lose: " + lose);
            playing = false;
        }

    }



    //updates the position of each g2d fillRect parameters (just y and barHeight) based on each food stack's size
    private void updateProgressBar() {
        for (int i = 0; i < stacks.size(); i++) {
            FoodStack stack = stacks.get(i);
            //the y coordinate of the ith bar: barheight - stacksize * height per each food
            progressBarParams.set(4*i + 1, barHeight - stack.stack.size() * barHeight / limit );
            //the height of the bar: stacksize * height per each food
            progressBarParams.set(4*i + 3, stack.stack.size() * barHeight / limit );
        }
    }

    //once a level is completed, update level parameters
    //
    private void updateLevel() {
        playing = true;
        win = false;
        lose = false;
        tutorial = false; //any new level should not be a tutorial
        recentDropIndex = TIME_BETWEEN_DROPS-1; // first food drops immediately; -1 to avoid starting with null
        foodIndex = 0;
        totalNumFoods += 1;
        totalNumBars += 1;
        goal += 1;
        limit += 1;
        level += 1;


        waitingFoodQueue = new ArrayList<>();
        droppedFoodQueue = new ArrayList<>();
        progressBarParams = new ArrayList<Integer>();
        bars = new ArrayList<Sprite>();
        stacks = new ArrayList<FoodStack>();

        player.setPosition(barGap, barHeight + 10);
        player.setHitbox(barGap,barHeight+10,40,80);

        //for the gameplay bars
        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox((barWidth + barGap) * i, 0, barWidth, barHeight);
            bars.add(newBar);
        }
        //for the progress bars
        for (int i = 0; i < totalNumBars; i++) {
            Sprite newBar = new Sprite("bar" + i);
            // no need to set position for bars; hitbox achieves the same effect needed for drawing
            newBar.setHitbox(playProgressGap + (barWidth + barGap) * (i + totalNumBars), 0, barWidth, barHeight);
            bars.add(newBar);
        }

        //initialize progressBarParams (x, y, width, height for each bar)
        for (int i = 0; i < totalNumBars; i++) {
            progressBarParams.add(playProgressGap + (barWidth + barGap) * (i + totalNumBars));
            progressBarParams.add(barHeight);
            progressBarParams.add(barWidth);
            progressBarParams.add(0); //0 height at first
        }

        if (level == 2) {
            categories.add("fruit");
            for (int i = 0; i < multiples; i++) {
                filenames.add("foods_resized/lime_100.png");
                filecategories.add("fruit");
                filenames.add("foods_resized/melon_100.png");
                filecategories.add("fruit");
                filenames.add("foods_resized/pear_100.png");
                filecategories.add("fruit");
                filenames.add("foods_resized/pineapple_100.png");
                filecategories.add("fruit");
            }
        }

        if (level == 3) {
            categories.add("dairy");
            for (int i = 0; i < multiples; i++) {
                filenames.add("foods_resized/icecream_100.png");
                filecategories.add("dairy");
                filenames.add("foods_resized/milk_100.png");
                filecategories.add("dairy");
                filenames.add("foods_resized/yogurt_100.png");
                filecategories.add("dairy");
                filenames.add("foods_resized/cheese_100.png");
                filecategories.add("dairy");
            }
        }

        for (int i = 0; i < categories.size(); i++) {
            stacks.add(new FoodStack(goal, limit, categories.get(i)));
        }

        for (int i = 0; i < filenames.size(); i++) {
            this.addFood(filenames.get(i), filenames.get(i), filecategories.get(i));
        }





        Collections.shuffle(this.waitingFoodQueue);
    }

    // run inside update only during tutorial stage
    private void updateTutorial(ArrayList<Integer> pressedKeys) {
        try {
            String text = textTutorial.get(tutorialIndex);
            tutorialTimeIndex += 1;
            //for the first highlighting of character, move to next instruction after 20 frames
            if (text.equals("highlight the character") && tutorialTimeIndex == 100) {
                tutorialIndex += 1; //move to next step
                tutorialTimeIndex = 0; //reset timer to 0
            }
            if (text.equals("push the right button") && pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                tutorialIndex += 1;
                tutorialTimeIndex = 0;
            }
            if (text.equals("push the left button") && pressedKeys.contains(KeyEvent.VK_LEFT)) {
                tutorialIndex += 1;
                tutorialTimeIndex = 0;
            }
        } catch (IndexOutOfBoundsException e) { }

        //for showing the food items, update would also be done at drawTutorial
    }

    // run inside draw only during tutorial stage
    private void drawTutorial(Graphics g) {
        try {
            String text = textTutorial.get(tutorialIndex);
            Color yellow = new Color(255, 200, 100, 100);
            Color black = new Color (0,0,0,100);

            if (text.equals("highlight the character")) {
                g.drawString("This is your character!", gameWidth * 3 / 4, gameHeight / 4);
                g.setColor(yellow);
                g.fillRect(player.getPosition().x, player.getPosition().y, player.getUnscaledWidth(), player.getUnscaledHeight());
                g.setColor(black);
            }
            if (text.equals("push the right button")) {
                g.drawString("Push the right arrow!", gameWidth * 3 / 4, gameHeight / 4);

                right.draw(g);
                //g.fillRect(right.getPosition().x, right.getPosition().y, right.getUnscaledWidth(), right.getUnscaledHeight());
            }
            if (text.equals("push the left button")) {
                g.drawString("Push the left arrow!", gameWidth * 3 / 4, gameHeight / 4);

                left.draw(g);
                //g.fillRect(left.getPosition().x, left.getPosition().y, left.getUnscaledWidth(), left.getUnscaledHeight());
            }
            if (text.equals("drop & show the veggie group - food 1")) {
                g.drawString("These are beans!", gameWidth * 3 / 4, gameHeight / 4);
                g.drawString("Beans are veggies!", gameWidth * 3 / 4, gameHeight / 4 + 30);

                Sprite food = waitingFoodQueue.get(foodIndex);
                if (tutorialTimeIndex == 0)
                    droppedFoodQueue.add(food);
                else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
                }
                food.draw(g);

                if (food.getPosition().y >= barHeight) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;
                }
            }
            if (text.equals("drop & show the veggie group - food 2")) {
                g.drawString("These are peppers!", gameWidth * 3 / 4, gameHeight / 4);
                g.drawString("Peppers are veggies!", gameWidth * 3 / 4, gameHeight / 4 + 30);

                Sprite food = waitingFoodQueue.get(foodIndex);
                if (tutorialTimeIndex == 0)
                    droppedFoodQueue.add(food);
                else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
                }
                food.draw(g);

                if (food.getPosition().y >= barHeight) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;
                }
            }

            if (text.equals("collect radish!")) {
                g.drawString("collect radish!", gameWidth * 3 / 4, gameHeight / 4);
                Sprite food = waitingFoodQueue.get(foodIndex);

                if (tutorialTimeIndex == 0)
                    droppedFoodQueue.add(food);
                else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
                }
                food.draw(g);

                if (player.collidesWith(food) && food.getPosition().y <= barHeight) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;
                } else if (food.getPosition().y > barHeight) {
                    food.setPosition(food.getPosition().x, 0);
                }
            }

            if (text.equals("avoid garlic!")) {
                g.drawString("avoid garlic!", gameWidth * 3 / 4, gameHeight / 4);
                Sprite food = waitingFoodQueue.get(foodIndex);

                if (tutorialTimeIndex == 0)
                    droppedFoodQueue.add(food);
                else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
                }
                food.draw(g);

                if (player.collidesWith(food) && food.getPosition().y <= barHeight) {
                    food.setPosition(food.getPosition().x, 0);
                } else if (food.getPosition().y > barHeight) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;

                    updateLevel(); //temporary end of tutorial
                }
            }
        } catch (IndexOutOfBoundsException e) { }
    }



    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        if (player == null) return; // player is null on first frame
        super.update(pressedKeys);


        if (tutorial) {
            updateTutorial(pressedKeys);
        }


        if (playing && !tutorial) {
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
            //System.out.println("waitingFoodQueue size: " + waitingFoodQueue.size());
            //System.out.println("droppedFoodQueue size: " + droppedFoodQueue.size());
            for (int i = 0; i < droppedFoodQueue.size(); i++) {
                Sprite food = droppedFoodQueue.get(i);

                // check for player collision before updating position
                // the second condition prevents the character from getting the food after the food escapes the bar
                if (player.collidesWith(food) && food.getPosition().y <= barHeight) {
                    droppedFoodQueue.remove(food);

                    System.out.println("collided");
                    //put on FoodStack

                    stacks.get(categories.indexOf(food.foodType)).push(food);
                    /*
                    if (food.foodType == "veggie") {
                        veggieStack.push(food);
                    } else if (food.foodType == "meat") {
                        meatStack.push(food);
                    }
                    */

                } else {
                    food.setPosition(food.getPosition().x, food.getPosition().y + gravity); //each food keeps on dropping
                    //prev_collision = false;
                }
            }
        }

        if (playing) {
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
        updateProgressBar();


        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        if (player == null) return;

        g.setFont(myFont); //how to run this only once?

        super.draw(g);
        player.draw(g);

        if (tutorial) {
            //g.drawString("This is a tutorial!", gameWidth * 3 / 4, gameHeight * 1 / 4);
            drawTutorial(g);
        }


        if (!playing && win) {
            //g.drawString("GAME PAUSED", gameWidth * 3 / 4, gameHeight * 1 / 4);
            if (level <= 2)
                updateLevel();
            else
                this.closeGame();
        }
        if (!playing && lose) {
            g.drawString("GAME OVER", gameWidth * 3 / 4, gameHeight / 4);
        }


        for (int i = 0; i < droppedFoodQueue.size(); i++) {
            droppedFoodQueue.get(i).draw(g);
        }

        if (win) {
            g.drawString("YOU WIN!", 10, 100);
            this.stop();
        } else {
            Graphics2D g2d = (Graphics2D) g;
            // bars have no sprites, just draw the hitboxes to achieve the same effect
            for (int i = 0; i < bars.size(); i++) {
                for (int j = 0; j < 4; j++) {
                    g2d.draw(bars.get(i).getHitbox().lines.get(j));
                }
            }
            int progBarStart = bars.size()/2;
            for (int i = progBarStart; i<bars.size(); i++) {
                if (i==progBarStart) {
                    g.drawString("VEGGIES", bars.get(i).getHitbox().p1.x+10, 30);
                } else if (i==progBarStart+1){
                    g.drawString("MEATS", bars.get(i).getHitbox().p1.x+10, 30);
                } else if (i==progBarStart+2) {
                    g.drawString("GRAINS", bars.get(i).getHitbox().p1.x+10, 30);
                } else if (i==progBarStart+3) {
                    g.drawString("FRUITS", bars.get(i).getHitbox().p1.x+10, 30);
                } else if (i==progBarStart+4) {
                    g.drawString("DAIRY", bars.get(i).getHitbox().p1.x+10, 30);
                }
            }

            //also draw the goal line
            g2d.draw(new Line2D.Float(playProgressGap + (barWidth + barGap) * totalNumBars, barHeight * (1 - goal / (float) limit), playProgressGap + (barWidth + barGap) * totalNumBars * 2 - barGap, barHeight * (1 - goal / (float) limit)));


            for (int i = 0; i < totalNumBars; i++) {
                g2d.fillRect(progressBarParams.get(i * 4), progressBarParams.get(i * 4 + 1), progressBarParams.get(i * 4 + 2), progressBarParams.get(i * 4 + 3));
            }

        }

    }

    /**
     * Quick main class that simply creates an instance of our game and starts the timer
     * that calls update() and draw() every frame
     * */
    public static void main(String[] args) {


        ArrayList<String> categories = new ArrayList<>();
        categories.add("veggie");
        categories.add("meat");
        categories.add("grain");


        // these would contain all 5 food groups: for lower levels, only use the lower indices

        ArrayList<String> filenames = new ArrayList<>();
        ArrayList<String> filecategories = new ArrayList<>();


        for (int i = 0; i < multiples; i++) {
            filenames.add("foods_resized/beans_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/pepper_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/radish_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/garlic_100.png");
            filecategories.add("veggie");

            filenames.add("foods_resized/shrimp_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/dumpling_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/duck_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/fishcake_100.png");
            filecategories.add("meat");

            filenames.add("foods_resized/noodles_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/rice_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/sushi_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/grain_100.png");
            filecategories.add("grain");
        }

        FinalGame game = new FinalGame(3, 5, 1, 5, categories, filenames, filecategories);

        game.start();



    }
}
