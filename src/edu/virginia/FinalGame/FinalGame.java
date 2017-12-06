package edu.virginia.FinalGame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import edu.virginia.engine.display.Sprite;
//import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.SoundManager;
//import edu.virginia.engine.display.DisplayObject;

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
    private boolean noMoreFood;
    private boolean reachedLimit;
    private boolean tutorial;
    private int level;
    private final int TIME_BETWEEN_DROPS = 100;
    private final int TUTORIAL_PAUSE = 80;
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
    private Sprite player = new Sprite("player", "asian.png");

    private Sprite left = new Sprite("left", "left.png");
    private Sprite right = new Sprite("right", "right.png");

    private Sprite lastDropped;

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
        }
        private void reset() {stack.clear();}
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
        //totalNumFoods = filenames.size() * 10;
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


        //for (int i = 0; i < categories.size(); i++) {
        //    stacks.add(new FoodStack(goal, limit, categories.get(i)));
        //}
        stacks.add(new FoodStack(goal+1, limit, categories.get(0))); // veggies
        stacks.add(new FoodStack(goal-1, limit, categories.get(1))); // meats
        stacks.add(new FoodStack(goal, limit, categories.get(2))); // grains


        for (int i = 0; i < filenames.size(); i++) {
            this.addFood(filenames.get(i), filenames.get(i), filecategories.get(i));
        }

        //maybe don't shuffle collections?
        //Collections.shuffle(this.waitingFoodQueue);


        this.textTutorial.add("highlight the character");
        this.textTutorial.add("push the right button");
        this.textTutorial.add("push the left button");

        //each food item is involved in the tutorial. will also introduce each food
        this.textTutorial.add("collect radish!");
        this.textTutorial.add("avoid garlic!");
        this.textTutorial.add("collect sushi!");
        this.textTutorial.add("avoid brown rice!");
        this.textTutorial.add("collect noodles, grain bar goes up!");
        this.textTutorial.add("collect pepper, veggie bar goes up!");
        this.textTutorial.add("collect shrimp, meat bar goes up, goal reached!");
        this.textTutorial.add("collect rice, grain bar goes up, goal reached!");
        this.textTutorial.add("collect dumpling, meat bar goes up!");
        this.textTutorial.add("collect duck, meat bar goes up!");
        this.textTutorial.add("collect fishcake, meat bar goes up, limit reached!");
        this.textTutorial.add("collect beans, veggie bar goes up!");
        this.textTutorial.add("collect garlic, veggie bar goes up, goal reached!");
        this.textTutorial.add("great job! now, let's play the game!");
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
        if (stacks != null) {
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
        }
        if (playing) {
            win = all_goals_reached;
            lose = !no_limit_reached;
            if (lose) reachedLimit = true;
        }
        if (win || lose) {
            //System.out.println("playing set to false, win: " + win + " lose: " + lose);
            if (!tutorial)
                playing = false; //for tutorial, how a game can be lost would be shown and game resumed
        }

    }



    //updates the position of each g2d fillRect parameters (just y and barHeight) based on each food stack's size
    private void updateProgressBar() {
        if (stacks != null) {
            for (int i = 0; i < stacks.size(); i++) {
                FoodStack stack = stacks.get(i);
                //the y coordinate of the ith bar: barheight - stacksize * height per each food
                progressBarParams.set(4 * i + 1, barHeight - stack.stack.size() * barHeight / limit);
                //the height of the bar: stacksize * height per each food
                progressBarParams.set(4 * i + 3, stack.stack.size() * barHeight / limit);
            }
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
        //totalNumFoods += 1;
        totalNumBars += 1;
        level += 1;


        waitingFoodQueue = new ArrayList<>();
        droppedFoodQueue = new ArrayList<>();
        progressBarParams = new ArrayList<Integer>();
        bars = new ArrayList<Sprite>();
        for (int i=0; i<stacks.size(); i++) {
            stacks.get(i).reset();
            stacks.get(i).limit = limit;
        }
        //stacks = new ArrayList<FoodStack>();

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
            goal = 3;
            limit = 8;
            stacks = new ArrayList<>();
            stacks.add(new FoodStack(goal+1, limit, categories.get(0))); // veggies
            stacks.add(new FoodStack(goal-1, limit, categories.get(1))); // meats
            stacks.add(new FoodStack(goal, limit, categories.get(2))); // grains
            stacks.add(new FoodStack(goal+2, limit, categories.get(3))); // fruits
            //bars = new ArrayList<>();
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

            goal = 4;
            limit = 8;
            stacks = new ArrayList<>();
            stacks.add(new FoodStack(goal+1, limit, categories.get(0))); // veggies
            stacks.add(new FoodStack(goal-1, limit, categories.get(1))); // meats
            stacks.add(new FoodStack(goal, limit, categories.get(2))); // grains
            stacks.add(new FoodStack(goal+2, limit, categories.get(3))); // fruits
            stacks.add(new FoodStack(goal-2, limit, categories.get(4))); // dairy
        }

        //for (int i = 0; i < categories.size(); i++) {
        //    stacks.add(new FoodStack(goal, limit, categories.get(i)));
        //}

        for (int i = 0; i < filenames.size(); i++) {
            this.addFood(filenames.get(i), filenames.get(i), filecategories.get(i));
        }
        totalNumFoods = waitingFoodQueue.size();
        Collections.shuffle(this.waitingFoodQueue);
    }

    // run inside update only during tutorial stage
    private void updateTutorial(ArrayList<Integer> pressedKeys) {
        try {
            if (playing) {
                String text = textTutorial.get(tutorialIndex);
                //System.out.println(text);
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
            }
        } catch (IndexOutOfBoundsException e) { }

    }

    private Sprite dropFoodsForTutorial(Graphics g, boolean collect, boolean tutorialEnd, boolean updateBar) {
        Sprite food = waitingFoodQueue.get(foodIndex);
        if (playing) {
            if (tutorialTimeIndex == 0)
                droppedFoodQueue.add(food);
            else {
                food.setPosition(food.getPosition().x, food.getPosition().y + gravity);
            }
            //food.draw(g);

            if (player.collidesWith(food) && food.getPosition().y <= barHeight) {

                if (updateBar) {
                    stacks.get(categories.indexOf(food.foodType)).push(food);
                }

                if (collect) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;

                    if (tutorialEnd)
                        updateLevel();
                } else {
                    food.setPosition(food.getPosition().x, 0);
                }
            } else if (food.getPosition().y > barHeight) {
                if (!collect) {
                    tutorialIndex += 1;
                    tutorialTimeIndex = 0;
                    foodIndex += 1;
                    if (tutorialEnd)
                        updateLevel();
                } else {
                    food.setPosition(food.getPosition().x, 0);
                }
            }
        }
        return food;
    }

    // run inside draw only during tutorial stage
    private void drawTutorial(Graphics g) {
        try {
            String text = textTutorial.get(tutorialIndex);
            Color yellow = new Color(255, 200, 100, 100);
            Color orange = new Color(255, 120, 82, 100);
            if (text.equals("highlight the character")) {
                g.drawString("This is your character!", gameWidth * 3 / 4, gameHeight / 4);
                if (tutorialTimeIndex % 6 <= 2)
                    g.setColor(yellow);
                else
                    g.setColor(orange);
                g.fillRect(player.getPosition().x, player.getPosition().y, player.getUnscaledWidth(), player.getUnscaledHeight());
                g.setColor(Color.BLACK);
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

            if (text.equals("collect radish!")) {
                g.drawString("Try collecting radish!", gameWidth * 5/8, gameHeight / 4);
                g.drawString("Radish is a veggie!", gameWidth * 5/8, gameHeight / 3);
                dropFoodsForTutorial(g, true, false, false).draw(g);
            }

            if (text.equals("avoid garlic!")) {
                g.drawString("Try avoiding garlic!", gameWidth * 5/8, gameHeight / 4);
                g.drawString("Garlic is a veggie!", gameWidth * 5/8, gameHeight / 3);
                dropFoodsForTutorial(g, false, false, false).draw(g);
            }

            if (text.equals("collect sushi!")) {
                g.drawString("Try collecting sushi!", gameWidth * 5/8, gameHeight / 4);
                g.drawString("Sushi is a grain!", gameWidth * 5/8, gameHeight / 3);
                dropFoodsForTutorial(g, true, false, false).draw(g);
            }

            if (text.equals("avoid brown rice!")) {
                g.drawString("Try avoiding brown rice!", gameWidth * 5/8, gameHeight / 4);
                g.drawString("Brown rice is a grain!", gameWidth * 5/8, gameHeight / 3);
                dropFoodsForTutorial(g, false, false, false).draw(g);
            }

            if (text.equals("collect noodles, grain bar goes up!")) {
                g.drawString("Collect noodles, grain bar goes up!", gameWidth * 5/8, gameHeight / 4);
                g.drawString("Noodles are a grain!", gameWidth * 5/8, gameHeight / 3);
                dropFoodsForTutorial(g, true, false, true).draw(g);
            }

            if (text.equals("collect pepper, veggie bar goes up!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The grain bar went up!", gameWidth * 5/8, gameHeight / 4);
                }
                else {
                    g.drawString("Collect pepper, veggie bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Pepper is a veggie!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect shrimp, meat bar goes up, goal reached!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The veggie bar went up!", gameWidth * 5/8, gameHeight / 4);
                }
                else {
                    g.drawString("Collect shrimp, meat bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    //g.drawString("Goal reached!", gameWidth * 5/8, gameHeight / 3);
                    g.drawString("Shrimp is a meat!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect rice, grain bar goes up, goal reached!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The meat bar went up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Goal reached!", gameWidth * 5/8, gameHeight / 3);
                }
                else {
                    g.drawString("Collect rice, grain bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Rice is a grain!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect dumpling, meat bar goes up!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The grain bar went up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Goal reached!", gameWidth * 5/8, gameHeight / 3);
                }
                else {
                    g.drawString("Collect dumplings, meat bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Dumplings are a meat!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect duck, meat bar goes up!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The meat bar went up!", gameWidth * 5/8, gameHeight / 4);
                }
                else {
                    g.drawString("Collect duck, meat bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Duck is a meat!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect fishcake, meat bar goes up, limit reached!")) {
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The meat bar went up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Danger! Eating too much is bad!", gameWidth * 5/8, gameHeight / 3);
                }
                else {
                    g.drawString("Collect fishcake, meat bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Fishcake is a meat!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect beans, veggie bar goes up!")) {
                //System.out.println(this.playing);
                if (tutorialTimeIndex == TUTORIAL_PAUSE * 2 - 1) {
                    //System.out.println(stacks.get(1).size());
                    stacks.get(1).pop();
                    //System.out.println(stacks.get(1).size());
                }
                if (tutorialTimeIndex <= TUTORIAL_PAUSE * 2) {
                    g.drawString("The meat bar went up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Limit reached! Don't eat too much!", gameWidth * 5/8, gameHeight / 3);
                }
                else {
                    g.drawString("Collect beans, veggie bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Beans is a veggie!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("collect garlic, veggie bar goes up, goal reached!")) {
                //System.out.println(this.playing);
                if (tutorialTimeIndex <= TUTORIAL_PAUSE) {
                    g.drawString("The veggie bar went up!", gameWidth * 5/8, gameHeight / 4);
                }
                else {
                    g.drawString("Collect radish, veggie bar goes up!", gameWidth * 5/8, gameHeight / 4);
                    g.drawString("Radish is a veggie!", gameWidth * 5/8, gameHeight / 3);
                    dropFoodsForTutorial(g, true, false, true).draw(g);
                }
            }

            if (text.equals("great job! now, let's play the game!")) {
                if (tutorialTimeIndex <= 2*TUTORIAL_PAUSE) {
                    g.drawString("Great job! Now, let's play the game!", gameWidth * 5/8, gameHeight / 4);
                }
                else
                    updateLevel();
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
            if (foodIndex >= waitingFoodQueue.size()) {
                noMoreFood = true;
                lose = true;
                playing = false;
                return;
            }

            // drop next food on a timed interval
            // if timer for this food exceeded TIME_BETWEEN_DROPS and there are more foods to come, reset dropindex
            // pick the most recent food from watingFoodQueue and add to droppedFoodQueue

            // random number from 1 to level num
            int randomNum = ThreadLocalRandom.current().nextInt(1, level + 1);
            if (recentDropIndex >= TIME_BETWEEN_DROPS && foodIndex < totalNumFoods) {
                recentDropIndex = 0;
                for (int i=0; i<randomNum; i++) {
                    //System.out.println(foodIndex + ", " + totalNumFoods);
                    if (foodIndex < totalNumFoods) {
                        if (i == 1) {
                            while (waitingFoodQueue.get(foodIndex - 1).foodType.equals(waitingFoodQueue.get(foodIndex).foodType)) {
                                foodIndex++;
                                if (foodIndex >= totalNumFoods) return;
                            }
                        }
                        if (i == 2) {
                            while (waitingFoodQueue.get(foodIndex - 1).foodType.equals(waitingFoodQueue.get(foodIndex).foodType) ||
                                    waitingFoodQueue.get(foodIndex - 2).foodType.equals(waitingFoodQueue.get(foodIndex).foodType) ||
                                    waitingFoodQueue.get(foodIndex - 1).foodType.equals(waitingFoodQueue.get(foodIndex - 2).foodType)) {
                                foodIndex++;
                                if (foodIndex >= totalNumFoods) return;
                            }
                        }
                        if (foodIndex < totalNumFoods) { //need to check again since foodindex could have increased
                            droppedFoodQueue.add(waitingFoodQueue.get(foodIndex));
                        }
                        foodIndex++;
                    }
                }
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
                    if (food.getPosition().y > gameHeight) droppedFoodQueue.remove(food);
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
            if (pressedKeys.contains(KeyEvent.VK_U)) {
                this.updateLevel(); // for testing purposes
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

        if (!playing) {
            if (win) {
                if (level <= 2)
                    updateLevel();
                else {
                    g.drawString("YOU WIN!", gameWidth * 3 / 4, gameHeight / 4);
                    //this.closeGame();
                }
            } else if (lose) {
                if (noMoreFood){
                    g.drawString("There is no more food!", gameWidth * 3 / 4, gameHeight * 13 / 16);
                }
                if (reachedLimit) {
                    g.drawString("You ate too much", gameWidth * 3 / 4, gameHeight * 13 / 16);
                    g.drawString("of the same food group!", gameWidth * 3 / 4, gameHeight * 7 / 8);
                }
                g.drawString("GAME OVER", gameWidth * 7 / 8, gameHeight * 15/16);
            } else {
                g.drawString("GAME PAUSED", gameWidth * 7/8, gameHeight * 7 / 8);
            }

        } else {
            g.drawString("Press P to pause at any time", gameWidth * 3 / 4, gameHeight * 7 / 8);
        }
        if (!tutorial) {
            for (int i = 0; i < droppedFoodQueue.size(); i++) {
                droppedFoodQueue.get(i).draw(g);
            }
        }

        if (win && !tutorial) {
            //g.drawString("YOU WIN!", 10, 100);
            this.stop();
        } else {
            Graphics2D g2d = (Graphics2D) g;
            // bars have no sprites, just draw the hitboxes to achieve the same effect
            for (int i = 0; i < bars.size(); i++) {
                for (int j = 0; j < 4; j++) {
                    g2d.draw(bars.get(i).getHitbox().lines.get(j));
                }
            }

            Color yellow = new Color(255, 200, 100, 100);
            Color green = new Color(100, 200, 100, 100);
            Color red = new Color(255, 100, 100, 100);

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

                FoodStack currentStack = stacks.get(i-progBarStart);
                Hitbox currentBar = bars.get(i).getHitbox();
                int currentProgress = currentStack.stack.size();
                if (currentProgress < currentStack.goal) {
                    // green highlight
                    g2d.setColor(green);
                } else if (currentProgress >= currentStack.goal && currentProgress < currentStack.limit-1) {
                    // yellow highlight
                    g2d.setColor(yellow);
                } else {
                    // red highlight
                    g2d.setColor(red);
                }
                g2d.fillRect(currentBar.p1.x,
                             currentBar.p1.y,
                        currentBar.p2.x - currentBar.p1.x,
                        currentBar.p4.y - currentBar.p1.y);
                g2d.setColor(Color.BLACK);
            }

            for (int i=0; i<categories.size(); i++) {
                //also draw the goal line
                g2d.draw(new Line2D.Float(playProgressGap + (barWidth + barGap) * (progBarStart+i),
                        barHeight * (1 - stacks.get(i).goal / (float) limit),
                        playProgressGap + (barWidth + barGap) * (progBarStart+i+1) - barGap,
                        barHeight * (1 - stacks.get(i).goal / (float) limit)));
            }

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
            //according to the tutorial sequence
            filenames.add("foods_resized/radish_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/garlic_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/sushi_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/brownrice_100.png");
            filecategories.add("grain");

            filenames.add("foods_resized/noodles_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/pepper_100.png");
            filecategories.add("veggie");
            filenames.add("foods_resized/shrimp_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/rice_100.png");
            filecategories.add("grain");
            filenames.add("foods_resized/dumpling_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/duck_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/fishcake_100.png");
            filecategories.add("meat");
            filenames.add("foods_resized/beans_100.png");
            filecategories.add("veggie");
        }

        FinalGame game = new FinalGame(3, 4, 2, 4, categories, filenames, filecategories);

        game.start();



    }
}
