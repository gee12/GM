package com.bondar.geom;

/**
 *
 * @author Иван
 */
public class Vertex3D {
    
    private Point3D pos;
    private Vector3D normal;
    private Point2D texturePos;
    private float intent;
    private int attributes;

    public Vertex3D(Point3D pos, Vector3D normal, Point2D texturePos, float intent, int attr) {
        this.pos = pos.getCopy();
        this.normal = normal;
        this.texturePos = texturePos;
        this.intent = intent;
        this.attributes = attr;
    }
    
    public Vertex3D(Point3D pos) {
        this.pos = pos.getCopy();
        this.normal = new Vector3D();
        this.texturePos = new Point2D();
        this.intent = 0;
        this.attributes = 0;
    }
    
    // set
    public void setAttr(int attr) {    
        attributes |= attr;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public void setNormal(Vector3D normal) {
        this.normal = normal;
    }

    public void setTexturePos(Point2D texturePos) {
        this.texturePos = texturePos;
    }

    public void setIntent(float intent) {
        this.intent = intent;
    }

    public void setPosition(Point3D p) {
        this.pos = p;
    }
    
    // get
    public boolean isSetAttribute(int attr) {
	return (attributes & attr) != 0;
    }
    
    public Point2D getTexturePos() {
        return texturePos;
    }

    public float getIntent() {
        return intent;
    }

    public int getAttributes() {
        return attributes;
    }
    
    public Point3D getPosition() {
        return pos;
    }
	
    public Vertex3D getCopy() {
	return new Vertex3D(pos.getCopy(), normal, texturePos, intent, attributes);
    }
}