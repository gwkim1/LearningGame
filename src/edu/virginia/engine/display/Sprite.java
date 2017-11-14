package edu.virginia.engine.display;

import java.util.ArrayList;

/**
 * Nothing in this class (yet) because there is nothing specific to a Sprite yet that a DisplayObject
 * doesn't already do. Leaving it here for convenience later. you will see!
 * */
public class Sprite extends DisplayObjectContainer {

	public String foodType;

	public Sprite(String id) {
		super(id);
	}

	public Sprite(String id, String imageFileName) {
		super(id, imageFileName);
	}


	//added a new constructor to specify food type
	public Sprite(String id, String imageFileName, String foodType) {
		super(id, imageFileName);
		this.foodType = foodType;
	}


	@Override
	public void update(ArrayList<Integer> pressedKeys) {
		super.update(pressedKeys);
	}
}
