package edu.virginia.engine.display;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.awt.geom.Line2D;


import javax.imageio.ImageIO;

/**
 * A very basic display object for a java based gaming engine
 * 
 * */
public class DisplayObject {

	/* All DisplayObject have a unique id */
	private String id;

	/* The image that is displayed by this object */
	private BufferedImage displayImage;

	private DisplayObject parent;

	public int frameCount;

	private boolean visible;
	private float alpha;
	private float oldAlpha;
	private double scaleX;
	private double scaleY;

	private Point position;
	private Point pivotPoint;
	private int Rotation;   //angle for rotation of the earth itself
	//needed to check for physical collisions. this.position doesn't change even when rotated with pivot points
	private Point physicalPosition;

	private boolean hasPhysics;



	public class Hitbox {  //new class Hitbox

		private Rectangle rec;
		private boolean onGround; //true if mario's hitboxes' lowestY == gameHeight

		//initialize a Hitbox as a Rectangle object
		//x, y of upper left point, height, width
		//public Hitbox(int x, int y, int w, int h) {
		//	rec = new Rectangle(x,y,w,h);
		//}


		private int width;
		private int height;
		public Point p1; //four points of rectangle, in clockwise order
		public Point p2;
		public Point p3;
		public Point p4;
		public Line2D l12; //line connecting a pair of points
		public Line2D l23;
		public Line2D l34;
		public Line2D l41;
		public ArrayList<Line2D.Float> lines = new ArrayList<>();


		//store four line2Ds in a global coordinate
		public Hitbox(int x, int y, int w, int h) {
			width = w;
			height = h;
			onGround = false;
			p1 = new Point(x, y);
			p2 = new Point(x + w, y);
			p3 = new Point(x + w, y + h);
			p4 = new Point(x, y + h);
			l12 = new Line2D.Float(p1.x, p1.y, p2.x, p2.y);
			l23 = new Line2D.Float(p2.x, p2.y, p3.x, p3.y);
			l34 = new Line2D.Float(p3.x, p3.y, p4.x, p4.y);
			l41 = new Line2D.Float(p4.x, p4.y, p1.x, p1.y);
			lines.add((Line2D.Float) l12);
			lines.add((Line2D.Float) l23);
			lines.add((Line2D.Float) l34);
			lines.add((Line2D.Float) l41);
			//l12 = new Line2D.Float(p1.x, p2.x, p1.y, p2.y);
			//l23 = new Line2D.Float(p2.x, p3.x, p2.y, p3.y);
			//l34 = new Line2D.Float(p3.x, p4.x, p3.y, p4.y);
			//l41 = new Line2D.Float(p4.x, p1.x, p4.y, p1.y);
		}

		public void updateHitbox(DisplayObject DO) {
			p1 = DO.getPhysicalPosition();
			//System.out.println("mario's physicalposition at updateHitbox: " + p1);
			int rotation = DO.getRotation();

			scaleX = DO.getScaleX();
			scaleY = DO.getScaleY();

			//System.out.println("p1 at updatehitbox: " + p1.x + " " + p1.y);
			p2 = new Point((int) (p1.x + scaleX * DO.getUnscaledWidth() * Math.cos(Math.toRadians(rotation))), (int) (p1.y + scaleY * DO.getUnscaledWidth() * Math.sin(Math.toRadians(rotation))));
			p4 = new Point((int) (p1.x - scaleX * DO.getUnscaledHeight() * Math.sin(Math.toRadians(rotation))), (int) (p1.y + scaleY * DO.getUnscaledHeight() * Math.cos(Math.toRadians(rotation))));
			p3 = new Point((int) (p4.x + scaleX * DO.getUnscaledWidth() * Math.cos(Math.toRadians(rotation))), (int) (p4.y + scaleY * DO.getUnscaledWidth() * Math.sin(Math.toRadians(rotation))));


			/*
			double lowestY = this.getLowestY();
			//if mario's onground, may have to double check if the hitbox doesn't hit the ground
			//e.g. when scaled up on the ground, mario should not grow downwards

			if (DO.getHitbox().getOnGround()) {
				double diff = lowestY - 460;
				if (diff > 0) {
					p1.setLocation(p1.getX(), p1.getY() - diff);
					p2.setLocation(p2.getX(), p2.getY() - diff);
					p3.setLocation(p3.getX(), p3.getY() - diff);
					p4.setLocation(p4.getX(), p4.getY() - diff);
				}
			}
			*/



			l12 = new Line2D.Float(p1.x, p1.y, p2.x, p2.y);
			l23 = new Line2D.Float(p2.x, p2.y, p3.x, p3.y);
			l34 = new Line2D.Float(p3.x, p3.y, p4.x, p4.y);
			l41 = new Line2D.Float(p4.x, p4.y, p1.x, p1.y);
			lines.add(0, (Line2D.Float) l12); //replace the original lines
			lines.add(1, (Line2D.Float) l23);
			lines.add(2, (Line2D.Float) l34);
			lines.add(3, (Line2D.Float) l41);

		}

		public void showHitbox() {
			//commented out because line2ds already correctly show the point positions
			//System.out.println("p1: " + p1.x + " " + p1.y);
			//System.out.println("p2: " + p2.x + " " + p2.y);
			//System.out.println("p3: " + p3.x + " " + p3.y);
			//System.out.println("p4: " + p4.x + " " + p4.y);

			//System.out.println("l12: " + l12.getP1() + " " + l12.getP2());
			//System.out.println("l23: " + l23.getP1() + " " + l23.getP2());
			//System.out.println("l34: " + l34.getP1() + " " + l34.getP2());
			//System.out.println("l41: " + l41.getP1() + " " + l41.getP2());
		}

		public double getLowestY() {
			double lowestY = this.p1.getY();
			//here, being larger means being lower
			if (this.p2.getY() > lowestY) lowestY = this.p2.getY();
			if (this.p3.getY() > lowestY) lowestY = this.p3.getY();
			if (this.p4.getY() > lowestY) lowestY = this.p4.getY();
			return lowestY;
		}

		public boolean getOnGround() {return this.onGround;}
		public void setOnGround(boolean onGround) {this.onGround = onGround;}

	}
	private Hitbox hitbox;

	/**
	 * Constructors: can pass in the id OR the id and image's file path and
	 * position OR the id and a buffered image and position
	 */
	public DisplayObject(String id) {
		this.setId(id);
		this.setVisible(true);
		this.setAlpha(1.0f);
		this.setOldAlpha(0.0f);
		this.setScaleX(1.0);
		this.setScaleY(1.0);
		this.position = new Point(0, 0);
		this.physicalPosition = new Point(0, 0);
		this.pivotPoint = new Point(0, 0);
		this.Rotation = 0;
		this.frameCount = 0;
		this.parent = null;
		this.hitbox = new Hitbox(0,0,0,0);
		this.hasPhysics = false;
	}

	public DisplayObject(String id, String fileName) {
		this.setId(id);
		this.setImage(fileName);
		this.setVisible(true);
		this.setAlpha(1.0f);
		this.setOldAlpha(0.0f);
		this.setScaleX(1.0);
		this.setScaleY(1.0);
		this.position = new Point(0, 0);
		this.physicalPosition = new Point(0, 0);
		this.pivotPoint = new Point(0, 0);
		this.Rotation = 0;
		this.frameCount = 0;
		this.parent = null;
		this.hitbox = new Hitbox(0,0,0,0);
		this.hasPhysics = false;
	}


	public void setPhysics(boolean physics) {
		this.hasPhysics = physics;
	}
	public boolean getPhysics() {
		return this.hasPhysics;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setPosition(int x, int y) {
		this.position.setLocation(x, y);
		//hitbox.setPosition(x,y);
	}
	public Point getPosition() { return this.position.getLocation(); }

	public void setPhysicalPosition(int x, int y) {
		this.physicalPosition.setLocation(x, y);
		this.hitbox.p1 = this.physicalPosition;
	}
	public Point getPhysicalPosition() { return this.physicalPosition.getLocation(); }

	public void setPivotPoint(int x, int y) { this.pivotPoint.setLocation(x, y); }
	public Point getPivotPoint() { return this.pivotPoint.getLocation(); }
	public void setRotation(int degree) { this.Rotation = degree; }
	public int getRotation() { return this.Rotation; }

	public void setHitbox(int x, int y, int w, int h) {
		this.hitbox = new Hitbox(x,y,w,h);
	}
	public Hitbox getHitbox() {
		return this.hitbox;
	}

	public boolean collidesWith(DisplayObject other) {
		//return getHitbox().getRec().intersects(other.getHitbox().getRec());
		ArrayList<Line2D.Float> thisLines = this.getHitbox().lines;
		ArrayList<Line2D.Float> otherLines = other.getHitbox().lines;
		boolean collided = false;

		ArrayList<String> thisStr = new ArrayList<>();
		thisStr.add("top"); //l12
		thisStr.add("right"); //l23
		thisStr.add("bottom"); //l34
		thisStr.add("left"); //l41
		ArrayList<String> otherStr = new ArrayList<>();
		otherStr.add("top");
		otherStr.add("right");
		otherStr.add("bottom");
		otherStr.add("left");

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (thisLines.get(i).intersectsLine(otherLines.get(j))) {
					//System.out.println("intersection:" + this.getId() + " " + thisStr.get(i) + " with " + other.getId() + " " + otherStr.get(j));
					//System.out.println("line of " + this.getId() + " " + thisLines.get(i).getP1() + thisLines.get(i).getP2());
					//System.out.println("line of " + other.getId() + " " + otherLines.get(j).getP1() + otherLines.get(j).getP2());
					//System.out.println("intersection at: " + thisLines.get(i).getP1() + " " + thisLines.get(i).getP2() + " " + otherLines.get(j).getP1() + " " + otherLines.get(j).getP2());
					collided = true; //to check for all intersections
				};
			}
		}
		return collided;
	}

	//returns boolean list for collided sides
	public ArrayList<Boolean> collidesWith2(DisplayObject other) {
		//return getHitbox().getRec().intersects(other.getHitbox().getRec());
		ArrayList<Line2D.Float> thisLines = this.getHitbox().lines;
		ArrayList<Line2D.Float> otherLines = other.getHitbox().lines;
		ArrayList<Boolean> collisions = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			collisions.add(false); //in top, right, bottom, left order (4 for this object, 4 for other)
		}

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (thisLines.get(i).intersectsLine(otherLines.get(j))) {
					//System.out.println("intersection:" + this.getId() + " " + thisStr.get(i) + " with " + other.getId() + " " + otherStr.get(j));
					//System.out.println("line of " + this.getId() + " " + thisLines.get(i).getP1() + thisLines.get(i).getP2());
					//System.out.println("line of " + other.getId() + " " + otherLines.get(j).getP1() + otherLines.get(j).getP2());
					//System.out.println("intersection at: " + thisLines.get(i).getP1() + " " + thisLines.get(i).getP2() + " " + otherLines.get(j).getP1() + " " + otherLines.get(j).getP2());
					collisions.set(i, true);
					collisions.set(4+j, true);

				};
			}
		}
		return collisions;
	}






	/**
	 * Returns the unscaled width and height of this display object
	 * */
	public int getUnscaledWidth() {
		if(displayImage == null) return 0;
		return displayImage.getWidth();
	}

	public int getUnscaledHeight() {
		if(displayImage == null) return 0;
		return displayImage.getHeight();
	}

	public void setParent(DisplayObject parent) { this.parent = parent;}
	public DisplayObject getParent() { return this.parent; }

	public void setVisible(boolean vis) {
		this.visible = vis;
	}
	public boolean getVisible() {
		return this.visible;
	}

	public void setAlpha(float a) {
		this.alpha = a;
	}
	public float getAlpha() {
		return this.alpha;
	}

	public void setOldAlpha(float a) {
		this.oldAlpha = a;
	}
	public float getOldAlpha() {
		return this.oldAlpha;
	}

	public void setScaleX(double sx) {
		this.scaleX = sx;
	}
	public double getScaleX() {
		return this.scaleX;
	}

	public void setScaleY(double sy) {
		this.scaleY = sy;
	}
	public double getScaleY() {
		return this.scaleY;
	}

	/*
		conversion functions between local - global
	 */

	/* given a global coordinate, return the local coordinate of*/
	public Point getLocal(int globalX, int globalY) {
		Point localGlobal = this.getGlobal(0, 0); /* local starting point for this DO */
		int localGlobalX = localGlobal.x;
		int localGlobalY = localGlobal.y;
		return new Point(globalX - localGlobalX, globalY - localGlobalY);
	}

	/* when run first, x, y would be local positions of the current node */
	public Point getGlobal(int x, int y) {
		if (parent == null) return new Point(x, y);
		else return parent.getGlobal(x + position.x, y + position.y);
	}

	//try to get a global coordinate without a parent
	//assume we always get mario's physical point
	public Point getGlobal2(int x, int y) {
		return new Point(this.physicalPosition.x + x, this.physicalPosition.y + y);
	}

	/**
	 * Helper function that simply reads an image from the given image name
	 * (looks in resources\\) and returns the bufferedimage for that filename
	 * */
	public BufferedImage readImage(String imageName) {
		BufferedImage image = null;
		try {
			String file = ("resources" + File.separator + imageName);
			image = ImageIO.read(new File(file));
		} catch (IOException e) {
			System.out.println("[Error in DisplayObject.java:readImage] Could not read image " + imageName);
			e.printStackTrace();
		}
		return image;
	}


	public void setImage(BufferedImage image) {
		if(image == null) return;
		displayImage = image;
	}

	public BufferedImage getDisplayImage() {
		return this.displayImage;
	}

	protected void setImage(String imageName) {
		if (imageName == null) {
			return;
		}
		displayImage = readImage(imageName);
		if (displayImage == null) {
			System.err.println("[DisplayObject.setImage] ERROR: " + imageName + " does not exist!");
		}
	}

	/**
	 * Invoked on every frame before drawing. Used to update this display
	 * objects state before the draw occurs. Should be overridden if necessary
	 * to update objects appropriately.
	 * */
	protected void update(ArrayList<Integer> pressedKeys) {
		if (frameCount < 60)
			frameCount ++;
		else
			frameCount = 0;
	}

	/**
	 * Draws this image. This should be overloaded if a display object should
	 * draw to the screen differently. This method is automatically invoked on
	 * every frame.
	 * */
	public void draw(Graphics g) {
		//System.out.println("mario.draw run");
		
		if (displayImage != null) {
			
			/*
			 * Get the graphics and apply this objects transformations
			 * (rotation, etc.)
			 */
			Graphics2D g2d = (Graphics2D) g;
			applyTransformations(g2d);





			/* Actually draw the image, perform the pivot point translation here */
			g2d.drawImage(displayImage, 0, 0,
					(int) (getUnscaledWidth()),
					(int) (getUnscaledHeight()), null);
			/*
			 * undo the transformations so this doesn't affect other display
			 * objects
			 */
			reverseTransformations(g2d);
		}
	}

	/**
	 * Applies transformations for this display object to the given graphics
	 * object
	 * */
	protected void applyTransformations(Graphics2D g2d) {
		g2d.translate(this.position.x, this.position.y); //just moves the pen to x and y

		g2d.scale(this.getScaleX(), this.getScaleY());


		g2d.rotate(Math.toRadians(this.getRotation()), this.getPivotPoint().x, this.getPivotPoint().y);
		//update physicalPoints
		//

		//System.out.println("position: " + this.position.x +" "+ this.position.y);
		//System.out.println("physical position: " + this.getPhysicalPosition().x + " " + this.getPhysicalPosition().y);
		//System.out.println("pivotpoint: " + this.getPivotPoint().x +" "+ this.getPivotPoint().y);
		//System.out.println("rotation: " + this.getRotation() );

		//System.out.println("location of 3 other points: " + (this.position.x + hitbox.getWidth() * Math.cos(-this.getRotation())) + " " + (this.position.y + hitbox.getHeight() * Math.sin(-this.getRotation())));


		//function to rotate the rectangle as well
		//g2d.draw(this.getHitbox().getRec()); //draw the rectangles first after rotation -> doesn't work
		//AffineTransform at = new AffineTransform();
		//at.rotate(this.getRotation());
		//System.out.println("rotate angle: " + this.getRotation());
		//Rectangle rotated = (Rectangle) at.createTransformedShape(this.getHitbox().getRec());
		//g2d.draw(rotated);
		//System.out.println(this.getHitbox().getRec());
		//g2d.draw(this.getHitbox().getRec());
		//System.out.println("x, y of rotated: " + rotated.getBounds());



		float curAlpha;
		if (!this.getVisible()) { // check visibility toggle
			this.oldAlpha = curAlpha = 0;
		} else { // if visibility toggled on, set transparency
			this.oldAlpha = curAlpha = ((AlphaComposite) g2d.getComposite()).getAlpha();
		}
		g2d.setComposite(AlphaComposite.getInstance(3, curAlpha * this.alpha));
	}

	/**
	 * Reverses transformations for this display object to the given graphics
	 * object
	 * */
	protected void reverseTransformations(Graphics2D g2d) {
		g2d.setComposite(AlphaComposite.getInstance(3, this.oldAlpha));
		g2d.scale(1.0, 1.0);
		g2d.rotate(Math.toRadians(-this.getRotation()), 0, 0);
		g2d.translate(-this.position.x, -this.position.y);
	}



}
