package edu.virginia.engine.display;

public class Animation {
    private String id;
    private int startFrame;
    private int endFrame;

    public Animation(String id, int startFrame, int endFrame) {
        this.id = id;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getStartFrame() {
        return this.startFrame;
    }
    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }
    public int getEndFrame() {
        return this.endFrame;
    }
    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }


}