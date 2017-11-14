package edu.virginia.lab4test;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import edu.virginia.engine.display.Sprite;
import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.SoundManager;

public class LabFourGame extends Game {

    public static int gameHeight = 500;
    public static int gameWidth = 1200;
    public static boolean collision = false;
    public static boolean win = false;
    public static int score = 0;

    ArrayList<Sprite> obstacles = new ArrayList<>();
    SoundManager soundmanager = new SoundManager();

    /* Create a sprite object for our game. We'll use mario */
    //y: gameHeight/2 changed to 0 to align with physical position
    AnimatedSprite mario = new AnimatedSprite("Mario", "Mario_default.png", new Point(0, 0));
    //Sprite mario = new Sprite("Mario", "Mario_default.png");

    Sprite sun = new Sprite("damage", "planets/sun_100.png");
    Sprite coin = new Sprite("coin", "coin.png");

    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    public ArrayList<Integer> previousPressedKeys;

    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public LabFourGame() {
        super("Lab Four Test Game", gameWidth, gameHeight);

        soundmanager.LoadSoundEffect("music", "music.wav");
        soundmanager.LoadSoundEffect("death","mario_die.wav");
        soundmanager.LoadSoundEffect("damage", "damage.wav");
        soundmanager.LoadSoundEffect("coin", "coin.wav");
        soundmanager.LoadSoundEffect("stage clear", "stage_clear.wav");
        soundmanager.PlayMusic("music");

        sun.setPosition((int)gameWidth/2, (int)gameHeight/3);
        sun.setHitbox(sun.getPosition().x,sun.getPosition().y,100,100);
        obstacles.add(sun);
        //sun.setParent(this);
        //this.addChildAtIndex(sun, 0);

        coin.setPosition((int)gameWidth*3/4, (int)gameHeight/2);
        coin.setHitbox(coin.getPosition().x,coin.getPosition().y,40,40);
        obstacles.add(coin);
        //coin.setParent(this);
        //this.addChildAtIndex(coin, 1);

        Point marioStart = mario.getPosition();
        mario.setHitbox(marioStart.x, marioStart.y, 40,60);
        //mario.setParent(this);
        //this.addChildAtIndex(mario, 0);

    }

    /* this keeps track of the position of Mario before jumping (as jump results in Mario going up) */
    public Point beforeJump;


    //for checking the coordinates of the 4 corner points of the mario image during game run
    public void checkMarioPosition(AnimatedSprite mario) {
        System.out.println("mario's position: " + mario.getPosition().x +" "+ mario.getPosition().y);
        System.out.println("mario's physical position: " + mario.getPhysicalPosition().x +" "+ mario.getPhysicalPosition().y);

    }





    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {

        //seems to be 0, 0 always
        //System.out.println("LabFourGame's position: " + this.getPosition().x + " " + this.getPosition().y);
        //checking for current angle of mario
        System.out.println("mario's angle: " + mario.getRotation());




        super.update(pressedKeys);

        //System.out.println(pressedKeys);
        //System.out.println("framecount: " + mario.frameCount);

		/* Make sure mario is not null. Sometimes Swing can auto cause an extra frame to go before everything is initialized */
        if (mario != null) mario.update(pressedKeys);

		/* speeding up and down. animation speed would range from 1 to 20 */
        if (pressedKeys.contains(KeyEvent.VK_O)) {
            if (mario.getAnimationSpeed() >= 2.0) mario.setAnimationSpeed(mario.getAnimationSpeed() - 1);
            System.out.println("Updated speed: " + mario.getAnimationSpeed());
        }
        if (pressedKeys.contains(KeyEvent.VK_P)) {
            if (mario.getAnimationSpeed() <= 19.0) mario.setAnimationSpeed(mario.getAnimationSpeed() + 1);
            System.out.println("Updated speed: " + mario.getAnimationSpeed());
        }
        if (pressedKeys.contains(KeyEvent.VK_UP)) {
            mario.setPosition(mario.getPosition().x, mario.getPosition().y - 5);
            mario.setPhysicalPosition(mario.getPhysicalPosition().x, mario.getPhysicalPosition().y - 5);
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_U) && !previousPressedKeys.contains(KeyEvent.VK_U)) {
            mario.animate(mario.getAnimation("jump"));
            mario.setPosition(mario.getPosition().x, mario.getPosition().y - 10);
            /* only store the position once */
            if (!previousPressedKeys.contains(KeyEvent.VK_U)) {
                beforeJump = mario.getPosition();
            }
        }
        if (mario != null
                && mario.getCurrentFrame() == mario.getAnimation("jump").getEndFrame()
                && mario.frameCount % mario.getAnimationSpeed() == 0) {
            mario.setPosition(beforeJump.x, beforeJump.y + 10);
        }
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
            mario.setPosition(mario.getPosition().x - 5, mario.getPosition().y);
            mario.setPhysicalPosition(mario.getPhysicalPosition().x - 5, mario.getPhysicalPosition().y);
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
            mario.setPosition(mario.getPosition().x, mario.getPosition().y + 5);
            mario.setPhysicalPosition(mario.getPhysicalPosition().x, mario.getPhysicalPosition().y + 5);
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            /* added condition so that animation is run only when startframe = currentframe */
            if (mario.getCurrentFrame() == mario.getAnimation("walk").getStartFrame()) {
                mario.animate(mario.getAnimation("walk"));
            }
            if (!previousPressedKeys.contains(KeyEvent.VK_RIGHT)) {
                mario.stopAnimation(0);
            }
            mario.setPosition(mario.getPosition().x + 5, mario.getPosition().y);
            mario.setPhysicalPosition(mario.getPhysicalPosition().x + 5, mario.getPhysicalPosition().y);
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_I)) {
            mario.setPivotPoint(mario.getPivotPoint().x, mario.getPivotPoint().y - 1);
            //System.out.println("new pivotpoint (global): " + mario.getGlobal(mario.getPivotPoint().x, mario.getPivotPoint().y));
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_J)) {
            mario.setPivotPoint(mario.getPivotPoint().x - 1, mario.getPivotPoint().y);
            //System.out.println("new pivotpoint (global): " + mario.getGlobal(mario.getPivotPoint().x, mario.getPivotPoint().y));
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_K)) {
            mario.setPivotPoint(mario.getPivotPoint().x, mario.getPivotPoint().y + 1);
            //System.out.println("new pivotpoint (global): " + mario.getGlobal(mario.getPivotPoint().x, mario.getPivotPoint().y));
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_L)) {
            mario.setPivotPoint(mario.getPivotPoint().x + 1, mario.getPivotPoint().y);
            //System.out.println("new pivotpoint (global): " + mario.getGlobal(mario.getPivotPoint().x, mario.getPivotPoint().y));
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            mario.setRotation((mario.getRotation() + 1) % 360);

            double x0 = mario.getPivotPoint().x + mario.getPosition().x;
            double y0 = mario.getPivotPoint().y + mario.getPosition().y;
            double x1 = mario.getPhysicalPosition().x;
            double y1 = mario.getPhysicalPosition().y;
            //double x0 = mario.getGlobal((int) mario.getPivotPoint().getX(), (int) mario.getPivotPoint().getY()).getX();
            //double y0 = mario.getGlobal((int) mario.getPivotPoint().getX(), (int) mario.getPivotPoint().getY()).getY();
            //double x1 = mario.getGlobal((int) mario.getPhysicalPosition().getX(), (int) mario.getPhysicalPosition().getY()).getX();
            //double y1 = mario.getGlobal((int) mario.getPhysicalPosition().getX(), (int) mario.getPhysicalPosition().getY()).getY();
            double angle = 1; //one degree added
            double x2 = x0 + Math.cos(Math.toRadians(-angle) * (x1-x0)) - Math.sin(Math.toRadians(-angle)*(y1-y0));
            double y2 = y0 + Math.sin(Math.toRadians(-angle) * (x1-x0)) + Math.cos(Math.toRadians(-angle)*(y1-y0));

            //adjustment for physicalpoint because it seems to follow pivotpoint
            int rotation = mario.getRotation();
            int pivotMoveHor = mario.getPivotPoint().x; //right means positive
            int pivotMoveVer = -mario.getPivotPoint().y; // up means positive
            //System.out.println("pivotmoves: " + pivotMoveHor + " " + pivotMoveVer);
            //for horizontal movement (also seems to work!)
            x2 = x2 - pivotMoveHor * Math.sin(Math.toRadians(90 - rotation)); //changed from +
            y2 = y2 - pivotMoveHor * Math.cos(Math.toRadians(90 - rotation));
            //for vertical movement (this seems to work)
            x2 = x2 - pivotMoveVer * Math.sin(Math.toRadians(rotation));
            y2 = y2 + pivotMoveVer * Math.cos(Math.toRadians(rotation)); //changed from -


            System.out.println("Before setphysicalposition: " + mario.getPhysicalPosition());
            mario.setPhysicalPosition((int)x2, (int)y2); //set to pivotpoint here
            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_Q)) {
            mario.setRotation((mario.getRotation() - 1) % 360);




            double x0 = mario.getPivotPoint().x + mario.getPosition().x;
            double y0 = mario.getPivotPoint().y + mario.getPosition().y;
            double x1 = mario.getPhysicalPosition().x;
            double y1 = mario.getPhysicalPosition().y;
            //double x0 = mario.getGlobal((int) mario.getPivotPoint().getX(), (int) mario.getPivotPoint().getY()).getX();
            //double y0 = mario.getGlobal((int) mario.getPivotPoint().getX(), (int) mario.getPivotPoint().getY()).getY();
            //double x1 = mario.getGlobal((int) mario.getPhysicalPosition().getX(), (int) mario.getPhysicalPosition().getY()).getX();
            //double y1 = mario.getGlobal((int) mario.getPhysicalPosition().getX(), (int) mario.getPhysicalPosition().getY()).getY();


            double angle = -1; //one degree adwwwwded
            //double x2 = x0 + Math.cos(Math.toRadians(-angle * (x1-x0))) - Math.sin(Math.toRadians(-angle*(y1-y0)));
            //double y2 = y0 + Math.sin(Math.toRadians(-angle * (x1-x0))) + Math.cos(Math.toRadians(-angle*(y1-y0)));
            double x2 = x0 + Math.cos(Math.toRadians(-angle) * (x1-x0)) - Math.sin(Math.toRadians(-angle)*(y1-y0));
            double y2 = y0 + Math.sin(Math.toRadians(-angle) * (x1-x0)) + Math.cos(Math.toRadians(-angle)*(y1-y0));


            int rotation = mario.getRotation();
            int pivotMoveHor = mario.getPivotPoint().x; //right means positive
            int pivotMoveVer = -mario.getPivotPoint().y; // up means positive
            //System.out.println("pivotmoves: " + pivotMoveHor + " " + pivotMoveVer);
            //for horizontal movement (also seems to work!)
            x2 = x2 - pivotMoveHor * Math.sin(Math.toRadians(90 - rotation)); //changed from +
            y2 = y2 - pivotMoveHor * Math.cos(Math.toRadians(90 - rotation));
            //for vertical movement (this seems to work)
            x2 = x2 - pivotMoveVer * Math.sin(Math.toRadians(rotation));
            y2 = y2 + pivotMoveVer * Math.cos(Math.toRadians(rotation)); //changed from -



            System.out.println("Before setphysicalposition: " + mario.getPhysicalPosition());
            mario.setPhysicalPosition((int)x2, (int)y2);

            mario.getHitbox().updateHitbox(mario);
            mario.getHitbox().showHitbox();
        }
        if (pressedKeys.contains(KeyEvent.VK_V) && !previousPressedKeys.contains(KeyEvent.VK_V)) {
            mario.setVisible(!mario.getVisible());
        }
        if (pressedKeys.contains(KeyEvent.VK_Z)) {
            float curAlpha = mario.getAlpha();
            if (curAlpha > 0.1f) {
                mario.setAlpha(curAlpha - 0.1f);
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_X)) {
            float curAlpha = mario.getAlpha();
            if (curAlpha < 1.0f) {
                mario.setAlpha(curAlpha + 0.1f);
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_A)) {
            mario.setScaleX(mario.getScaleX() + 0.1);
            mario.setScaleY(mario.getScaleY() + 0.1);
            //this.setScaleX(this.getScaleX() + 0.1);
            //this.setScaleY(this.getScaleY() + 0.1);
            System.out.println("mario's scale: " + mario.getScaleX());
            System.out.println("game's scale: " + this.getScaleX());
            mario.animate(mario.getAnimation("scaleup"));
            mario.getHitbox().updateHitbox(mario);
        }

        if (pressedKeys.contains(KeyEvent.VK_S)) {
            if (mario.getScaleX() > 0.1) {
                mario.setScaleX(mario.getScaleX() - 0.1);
                mario.setScaleY(mario.getScaleY() - 0.1);
                mario.animate(mario.getAnimation("scaledown"));
                mario.getHitbox().updateHitbox(mario);
            }
        }

        // collision check
        int i;
        if (!collision) {
            for (i = 0; i < obstacles.size(); i++) {
                Sprite s = obstacles.get(i);
                if (mario.collidesWith(s)) {
                    System.out.println("collision");
                    collision = true;
                    soundmanager.PlaySoundEffect(s.getId());
                    if (s.getId().equals("coin")) score++;
                    if (s.getId().equals("damage") && score > 0) score--;
                    break;
                }
            }
        } else {
            int xdiff = mario.getPhysicalPosition().x - mario.getPosition().x;
            int ydiff = mario.getPhysicalPosition().y - mario.getPosition().y;
            mario.setPosition(gameHeight/2, gameWidth/4);
            mario.setPhysicalPosition(gameHeight/2, gameWidth/4);
            mario.setPivotPoint(0, 0);
            mario.setRotation(0);
            mario.setScaleX(1.0);
            mario.setScaleY(1.0);
            mario.getHitbox().updateHitbox(mario);

            for (i = 0; i < obstacles.size(); i++) {
                if (mario.collidesWith(obstacles.get(i))) {
                    collision = true;
                    break;
                } else collision = false;
            }
        }

        if (score >= 5){
            soundmanager.StopMusic();
            soundmanager.PlaySoundEffect("stage clear");
            win = true;
        }





        //System.out.println("position: " + mario.getPosition());
        //System.out.println("pivotpoint (global): " + (mario.getPivotPoint().x + mario.getPosition().x) + " " + (mario.getPivotPoint().y + mario.getPosition().y));
        //System.out.println("physicalposition: " + mario.getPhysicalPosition());
        // update previousPressedKeys
        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        super.draw(g);
        g.drawString("Reach 5 points!",10,10);
        g.drawString("Score: "+score, 10,40);

        //maybe these should be drawn here? not changing position
        sun.draw(g);
        coin.draw(g);

        //g.drawLine((int) mario.getHitbox().p1.getX(), (int) mario.getHitbox().p1.getY(), (int) mario.getHitbox().p2.getX(), (int) mario.getHitbox().p2.getY() );
        //g.drawLine((int) mario.getHitbox().p2.getX(), (int) mario.getHitbox().p2.getY(), (int) mario.getHitbox().p3.getX(), (int) mario.getHitbox().p3.getY() );


        if (win) {
            g.drawString("YOU WIN!",10, 100);
            this.stop();
        } else {
            Graphics2D g2d = (Graphics2D) g;
		/* Same, just check for null in case a frame gets thrown in before Mario is initialized */

            g2d.draw(mario.getHitbox().lines.get(0));
            g2d.draw(mario.getHitbox().lines.get(1));
            g2d.draw(mario.getHitbox().lines.get(2));
            g2d.draw(mario.getHitbox().lines.get(3));
            g2d.draw(coin.getHitbox().lines.get(0));
            g2d.draw(coin.getHitbox().lines.get(1));
            g2d.draw(coin.getHitbox().lines.get(2));
            g2d.draw(coin.getHitbox().lines.get(3));
            g2d.draw(sun.getHitbox().lines.get(0));
            g2d.draw(sun.getHitbox().lines.get(1));
            g2d.draw(sun.getHitbox().lines.get(2));
            g2d.draw(sun.getHitbox().lines.get(3));

            //try to mark a visible pivotpoint
            Point globalPivot = new Point(mario.getPivotPoint().x + mario.getPosition().x, mario.getPivotPoint().y + mario.getPosition().y);
            g.drawLine(globalPivot.x, globalPivot.y, globalPivot.x+1, globalPivot.y+1);
            g.drawLine(globalPivot.x, globalPivot.y, globalPivot.x+1, globalPivot.y);
            g.drawLine(globalPivot.x, globalPivot.y, globalPivot.x, globalPivot.y+1);




            if (mario != null) {
                mario.draw(g);
                //mario's global getPhysicalPosition always aligns with p1
                System.out.println("mario's global position: " + mario.getPhysicalPosition());
                //not working...
                //g2d.draw(mario.getHitbox().l12);
                //g2d.draw(mario.getHitbox().l23);
                //g2d.draw(mario.getHitbox().l34);
                //g2d.draw(mario.getHitbox().l41);
                //g2d.draw(mario.getHitbox().getRec());
            }
            //if (sun != null) {
            //sun.draw(g);
            //g2d.draw(sun.getHitbox().getRec());
            //}
            //if (coin != null) {
            //    coin.draw(g);
            //g2d.draw(coin.getHitbox().getRec());
            //}
        }
    }

    /**
     * Quick main class that simply creates an instance of our game and starts the timer
     * that calls update() and draw() every frame
     * */
    public static void main(String[] args) {
        LabFourGame game = new LabFourGame();
        game.start();
    }
}
