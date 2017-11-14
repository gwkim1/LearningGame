package edu.virginia.lab1test;

import java.awt.Graphics;
import java.security.Key;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.Point;

import edu.virginia.engine.display.AnimatedSprite;
import edu.virginia.engine.display.Game;
import edu.virginia.engine.display.Sprite;

/**
 * Example game that utilizes our engine. We can create a simple prototype game with just a couple lines of code
 * although, for now, it won't be a very fun game :)
 * */
public class LabOneGame extends Game {

	/* Create a sprite object for our game. We'll use mario */
	AnimatedSprite mario = new AnimatedSprite("Mario", "Mario_default.png", new Point(0, 0));

	// List of keys pressed in the previous frame. Updated every frame. Used to prevent visibility flickering
	ArrayList<Integer> previousPressedKeys;

	/**
	 * Constructor. See constructor in Game.java for details on the parameters given
	 */
	public LabOneGame() {
		super("Lab One Test Game", 500, 300);
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
		}
		if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
			mario.setPosition(mario.getPosition().x, mario.getPosition().y + 5);
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
		}
		if (pressedKeys.contains(KeyEvent.VK_I)) {
			mario.setPivotPoint(mario.getPivotPoint().x, mario.getPivotPoint().y - 5);
		}
		if (pressedKeys.contains(KeyEvent.VK_J)) {
			mario.setPivotPoint(mario.getPivotPoint().x - 5, mario.getPivotPoint().y);
		}
		if (pressedKeys.contains(KeyEvent.VK_K)) {
			mario.setPivotPoint(mario.getPivotPoint().x, mario.getPivotPoint().y + 5);
		}
		if (pressedKeys.contains(KeyEvent.VK_L)) {
			mario.setPivotPoint(mario.getPivotPoint().x + 5, mario.getPivotPoint().y);
		}
		if (pressedKeys.contains(KeyEvent.VK_W)) {
			mario.setRotation(mario.getRotation() + 1);
		}
		if (pressedKeys.contains(KeyEvent.VK_Q)) {
			mario.setRotation(mario.getRotation() - 1);
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
            mario.animate(mario.getAnimation("scaleup"));
        }

        if (pressedKeys.contains(KeyEvent.VK_S)) {
			if (mario.getScaleX() > 0.1) {
				mario.setScaleX(mario.getScaleX() - 0.1);
				mario.setScaleY(mario.getScaleY() - 0.1);
                mario.animate(mario.getAnimation("scaledown"));
			}
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
		super.draw(g);
		
		/* Same, just check for null in case a frame gets thrown in before Mario is initialized */
		if(mario != null) mario.draw(g);
	}

	/**
	 * Quick main class that simply creates an instance of our game and starts the timer
	 * that calls update() and draw() every frame
	 * */
	public static void main(String[] args) {
		LabOneGame game = new LabOneGame();
		game.start();

	}
}
