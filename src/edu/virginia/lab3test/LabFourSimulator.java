package edu.virginia.lab3test;

import java.awt.Graphics;
import java.security.Key;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.lang.Math;

import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.DisplayObjectContainer;
import edu.virginia.engine.display.DisplayObject;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.Sprite;

public class LabFourSimulator extends Game {

    //DisplayObjectContainer rootDOC = new DisplayObjectContainer("Root Node");
    Sprite sun;
    Sprite earth;
    Sprite mars;
    Sprite moon;


    // List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
    ArrayList<Integer> previousPressedKeys;

    /**
     * Constructor. See constructor in Game.java for details on the parameters given
     */
    public LabFourSimulator(int width, int height) {

        super("Lab Four Simulator", width, height); //doubled up frame size
        System.out.println("position of simulator: " + this.getPosition().x + " " + this.getPosition().y);

        sun = new Sprite("Sun", "planets/sun_100.png"); //a sprite is also a DOC and a DO

        int centerWidth = width / 2 - 50;
        int centerHeight = height / 2 - 50;
        sun.setPosition(centerWidth, centerHeight); //set initial position as center of the game frame
        //+-50 to offset the half-width and height of the sun_100.png file
        //doesn't seem to be correctly centering..

        //Game itself is also a DOC, so set it as sun's parent
        sun.setParent(this);
        this.addChildAtIndex(sun, 0);

        earth = new Sprite("Earth", "planets/earth_75.png");
        //earth.setPosition(sun.getPosition().x + 100, sun.getPosition().y); //place it to the right of sun
        sun.setChildRotationRadius("earth",200); //earth would rotation 200 units away from the sun
        // position must be set relatively to its parent, the sun
        earth.setPosition(sun.getChildRotationRadius("earth") * (int) Math.cos(Math.toDegrees(0)),
                          sun.getChildRotationRadius("earth") * (int) Math.sin(Math.toDegrees(0)));
        earth.setParent(sun);
        sun.addChildAtIndex(earth, 0);

        moon = new Sprite("Moon", "planets/moon_50.png");
        earth.setChildRotationRadius("moon",100);
        moon.setPosition(earth.getChildRotationRadius("moon") * (int) Math.cos(Math.toDegrees(0)),
                         earth.getChildRotationRadius("moon") * (int) Math.sin(Math.toDegrees(0)));
        moon.setParent(earth);
        earth.addChildAtIndex(moon, 0);

        mars = new Sprite("Mars", "planets/mars_75.png");
        sun.setChildRotationRadius("mars", 400);
        mars.setPosition(sun.getChildRotationRadius("mars") * (int) Math.cos(Math.toDegrees(0)),
                         sun.getChildRotationRadius("mars") * (int) Math.sin(Math.toDegrees(0)));
        mars.setParent(sun);
        sun.addChildAtIndex(mars, 1);

        System.out.println("position of sun: " + sun.getPosition().x + " " + sun.getPosition().y);
        System.out.println("global position of 0, 0 at sun: " + sun.getGlobal(0, 0));
        System.out.println("local position of sun for centerWidth+5, centerHeight+5: " + sun.getLocal(centerWidth+5, centerHeight+5));
        System.out.println("width and height: " + sun.getUnscaledWidth() + " " + sun.getUnscaledHeight());
        System.out.println("position of earth: " + earth.getPosition().x + " " + earth.getPosition().y);

    }


    /* this keeps track of the position of Mario before jumping (as jump results in Mario going up) */
    public Point beforeJump;

    /**
     * Engine will automatically call this update method once per frame and pass to us
     * the set of keys (as strings) that are currently being pressed down
     */
    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        super.update(pressedKeys);

        //System.out.println(pressedKeys);
        //System.out.println("framecount: " + mario.frameCount);

		/* Make sure mario is not null. Sometimes Swing can auto cause an extra frame to go before everything is initialized */
        if (sun != null) sun.update(pressedKeys);

        if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
            sun.setPosition(sun.getPosition().x, sun.getPosition().y - 5);
        }
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            sun.setPosition(sun.getPosition().x - 5, sun.getPosition().y);
        }
        if (pressedKeys.contains(KeyEvent.VK_UP)) {
            sun.setPosition(sun.getPosition().x, sun.getPosition().y + 5);
        }
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
            sun.setPosition(sun.getPosition().x + 5, sun.getPosition().y);
        }

        if (pressedKeys.contains(KeyEvent.VK_A)) {
            Point newPivotPoint = sun.getLocal(sun.getPosition().x, sun.getPosition().y);
            sun.setPivotPoint(newPivotPoint.x+50, newPivotPoint.y+50);
            sun.setRotation(sun.getRotation() - 1);
        }
        if (pressedKeys.contains(KeyEvent.VK_S)) {
            Point newPivotPoint = sun.getLocal(sun.getPosition().x, sun.getPosition().y);
            sun.setPivotPoint(newPivotPoint.x+50, newPivotPoint.y+50);
            sun.setRotation(sun.getRotation() + 1);
        }

        if (pressedKeys.contains(KeyEvent.VK_Q)) {
            sun.setPosition(sun.getPosition().x - 5, sun.getPosition().y - 5); // center on the sun
            sun.setScaleX(sun.getScaleX() + 0.1);
            sun.setScaleY(sun.getScaleY() + 0.1);
        }

        if (pressedKeys.contains(KeyEvent.VK_W)) {
            if (sun.getScaleX() > 0.1) {
                sun.setPosition(sun.getPosition().x + 5, sun.getPosition().y + 5); // center on the sun
                sun.setScaleX(sun.getScaleX() - 0.1);
                sun.setScaleY(sun.getScaleY() - 0.1);
            }
        }


        // need to change earth's position without rotating itself
        if (earth != null) {
            int earthRotationRadius = sun.getChildRotationRadius("earth");
            double earthParentAngle = earth.getParentAngle();

            earth.setParentAngle(earthParentAngle + 0.001);
            earth.setPosition((int) (earthRotationRadius * Math.cos(Math.toDegrees(earthParentAngle) % 360)) + 30,
                              (int) (earthRotationRadius * Math.sin(Math.toDegrees(earthParentAngle) % 360)) + 30);
            //System.out.println("earth's position (relative to sun): " + earth.getPosition());
        }
        if (moon != null) {
            int moonRotationRadius = earth.getChildRotationRadius("moon");
            double moonParentAngle = moon.getParentAngle();

            moon.setParentAngle(moonParentAngle + 0.002); //if set to same, earth and moon rotates together
            moon.setPosition((int) (moonRotationRadius * Math.cos(Math.toDegrees(moonParentAngle) % 360)) + 15,
                             (int) (moonRotationRadius * Math.sin(Math.toDegrees(moonParentAngle) % 360)) + 15);
            //System.out.println("moon's position (relative to earth): " + moon.getPosition());
        }
        if (mars != null) {
            int marsRotationRadius = sun.getChildRotationRadius("mars");
            double marsParentAngle = mars.getParentAngle();

            mars.setParentAngle(marsParentAngle + 0.0003);
            mars.setPosition((int) (marsRotationRadius * Math.cos(Math.toDegrees(marsParentAngle) % 360)) + 30,
                             (int) (marsRotationRadius * Math.sin(Math.toDegrees(marsParentAngle) % 360)) + 30);
        }



        // update previousPressedKeys
        previousPressedKeys = new ArrayList<Integer>(pressedKeys);
    }

    /**
     * Engine automatically invokes draw() every frame as well. If we want to make sure mario gets drawn to
     * the screen, we need to make sure to override this method and call mario's draw method.
     * */
    @Override
    public void draw(Graphics g){
        super.draw(g); //DOC.draw already recursively draws everything
    }

    /**
     * Quick main class that simply creates an instance of our game and starts the timer
     * that calls update() and draw() every frame
     * */
    public static void main(String[] args) {
        LabFourSimulator game = new LabFourSimulator(1500, 900);
        game.start();
    }
}
