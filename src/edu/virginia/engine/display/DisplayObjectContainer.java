package edu.virginia.engine.display;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplayObjectContainer extends DisplayObject {

    private ArrayList<DisplayObject> children;
    private double parentAngle;   //angular position of a planet with respect to its parent
    private HashMap<String, Integer> childRotationRadii; //how far away the child planets would be circling around

    public DisplayObjectContainer(String id) {
        super(id);
        children = new ArrayList<DisplayObject>();
        this.parentAngle = 0.0;
        childRotationRadii = new HashMap<String, Integer>();
    }
    public DisplayObjectContainer(String id, String fileName) {
        super(id, fileName);  //this would set the image in DO
        children = new ArrayList<DisplayObject>();
        this.parentAngle = 0.0;
        childRotationRadii = new HashMap<String, Integer>();
    }


    public void setParentAngle(double angle) {this.parentAngle = angle;}
    public double getParentAngle() {return this.parentAngle;}
    public void setChildRotationRadius(String id, int radius) {this.childRotationRadii.put(id, radius);}
    public int getChildRotationRadius(String id) {return this.childRotationRadii.get(id);}


    public ArrayList<DisplayObject> getChildren() {
        return children;
    }
    public void addChild(DisplayObject disObj) {
        children.add(disObj);
    }
    public void addChildAtIndex(DisplayObject disObj, int idx) {
        children.add(idx, disObj);
    }
    public boolean removeChild(DisplayObject disObj) {
        return children.remove(disObj);
    }
    public DisplayObject removeChildAtIndex(int idx) {
        return children.remove(idx);
    }
    public void removeAllChildren() {
        children.clear();
    }

    public boolean contains(DisplayObject disObj) {
        return children.contains(disObj);
    }

    public DisplayObject getChildByIndex(int idx) {
        return children.get(idx);
    }
    public DisplayObject getChildById(String id) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId().equals(id)) {
                return children.get(i);
            }
        }
        return null;
    }

    @Override
    public void update(ArrayList<Integer> pressedKeys) {
        super.update(pressedKeys);
    }

    @Override
    public void draw(Graphics g) {
        /*
		 * Get the graphics and apply this objects transformations
		 * (rotation, etc.)
		 */
        Graphics2D g2d = (Graphics2D) g;

        // draw yourself
        super.draw(g);
        //System.out.println("drawing DOC " + this.getId());

        applyTransformations(g2d);

        // draw each of the children
        for (int i = 0; i < children.size(); i++) {
            children.get(i).draw(g);
        }

        /*
		 * undo the transformations so this doesn't affect other display
		 * objects
		 */
        reverseTransformations(g2d);
    }

}
