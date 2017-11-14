package edu.virginia.engine.display;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;

import edu.virginia.engine.util.GameClock;
import edu.virginia.engine.display.Animation;

public class AnimatedSprite extends Sprite {
    private ArrayList<Animation> animations = new ArrayList<>();
    private boolean playing;
    private String fileName;
    private ArrayList<BufferedImage> frames = new ArrayList<>();
    private int currentFrame;
    private int startFrame;
    private int endFrame;
    private static final int DEFAULT_ANIMATION_SPEED = 10;
    private float animationSpeed;
    private GameClock gameClock;

    // is this where we declare our animations?
    private Animation walk = new Animation("walk", 0, 1);
    private Animation jump = new Animation("jump", 2, 5);
    private Animation scaleup = new Animation("scaleup", 6, 7);
    private Animation scaledown = new Animation("scaledown", 8, 9);

    public AnimatedSprite(String id, String fileName, Point position) {
        super(id, fileName);
        /* may have to change Point position to non-ints */
        super.setPosition((int) position.getX(), (int) position.getY());
        currentFrame = 0;
        this.initializeFrames();
        animations.add(jump);
        animations.add(walk);
        animations.add(scaleup);
        animations.add(scaledown);
        gameClock = new GameClock();
        animationSpeed = DEFAULT_ANIMATION_SPEED;
    }

    public void initializeFrames() {
        frames.add(readImage("Mario_default.png"));
        frames.add(readImage("mario_walk1.png"));
        frames.add(readImage("mario_jump1.png"));
        frames.add(readImage("mario_jump2.png"));
        frames.add(readImage("mario_jump3.png"));
        frames.add(readImage("mario_jump4.png"));
        frames.add(readImage("Mario_default.png"));
        frames.add(readImage("Mario_scaleup.png"));
        frames.add(readImage("Mario_default.png"));
        frames.add(readImage("Mario_scaledown.png"));
    }

    public void initGameClock() {
        if (gameClock == null) {
            gameClock = new GameClock();
        }
    }


    public boolean getPlaying() {
        return this.playing;
    }
    public int getCurrentFrame() {
        return this.currentFrame;
    }
    public int getStartFrame() {
        return this.startFrame;
    }
    public int getEndFrame() {
        return this.endFrame;
    }


    public Animation getAnimation(String id) {
        for (int i = 0; i < animations.size(); i++) {
            if (animations.get(i).getId().equals(id)) {
                //System.out.println("image "+i+" matches id "+id);
                return animations.get(i);
            }
        }
        return null;
    }

    public void animate(Animation a) {
        this.playing = true;
        this.startFrame = a.getStartFrame();
        this.endFrame = a.getEndFrame();
        this.currentFrame = this.startFrame;
        //System.out.println("after animate, start end current: " + this.startFrame + this.endFrame + this.currentFrame);
    }
    public void animate(String id) {
        this.playing = true;
        Animation a = this.getAnimation(id);
        this.startFrame = a.getStartFrame();
        this.endFrame = a.getEndFrame();
    }
    public void animate(int startFrame, int endFrame) {
        this.playing = true;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public void stopAnimation (int frameNumber) {
        this.currentFrame = frameNumber;
        super.setImage(frames.get(frameNumber));
        this.playing = false;
    }
    public void stopAnimation() {
        stopAnimation(this.startFrame);
    }

    /* setter for a single animation in ArrayList animations
       not sure if the entire list must be updated
    */
    public void setAnimation(String id, int startFrame, int endFrame, int animationIndex) {
        Animation toUpdate = animations.get(animationIndex);
        toUpdate.setId(id);
        toUpdate.setStartFrame(startFrame);
        toUpdate.setEndFrame(endFrame);
    }
    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    public float getAnimationSpeed() {
        return this.animationSpeed;
    }

    // How to use GameClock for choosing whether to update frames?
    @Override
    public void draw(Graphics g) {
        if (playing == true) {
            //System.out.println("playing is true");
            // at every animationSpeed frame, update DisplayObject.displayImage
            if (this.frameCount % animationSpeed == 0) {
                //System.out.println("animated");
                if (this.currentFrame < this.endFrame)
                    this.currentFrame++;
                else
                    this.stopAnimation(0);  //stops animation after single iteration
                    //this.currentFrame = this.startFrame;

                //System.out.println("currentFrame:" + this.currentFrame);
                BufferedImage frame = frames.get(currentFrame);
                super.setImage(frame);
            }

        }
        // regardless of whether displayImage is updated, draw it
        if (super.getDisplayImage() != null) {
			/*
			 * Get the graphics and apply this objects transformations
			 * (rotation, etc.)
			 */
            Graphics2D g2d = (Graphics2D) g;
            applyTransformations(g2d);

			/* Actually draw the image, perform the pivot point translation here */
            g2d.drawImage(super.getDisplayImage(), 0, 0,
                    (int) (getUnscaledWidth()),
                    (int) (getUnscaledHeight()), null);

			/*
			 * undo the transformations so this doesn't affect other display
			 * objects
			 */
            reverseTransformations(g2d);
        }

        // reset after switching frame?
        gameClock.resetGameClock();
    }
}
