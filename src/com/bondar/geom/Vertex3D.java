package com.bondar.geom;

/**
 *
 * @author Иван
 */
public class Vertex3D extends Point3DOdn {
    
    private Vector3D normal;
    private Point2D texturePos;
    private float intent;
    private int attributes;

    public Vertex3D(Point3DOdn p3DOdn, Vector3D normal, Point2D texturePos, float intent, int attr) {
        super(p3DOdn);
        this.normal = normal;
        this.texturePos = texturePos;
        this.intent = intent;
        this.attributes = attr;
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
}
