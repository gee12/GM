package com.bondar.geom;

import com.bondar.gm.Camera;
import com.bondar.gm.CullManager;
import java.awt.Color;

/**
 * 
 * @author truebondar
 */
public abstract class Polygon3D {

    public static enum States {
	VISIBLE,
	BACKFACE,
        CULLED
    }
    public static enum Types {
	POLYGON,
	TRIANGLE,
	LINE,
	POINT
    }
    
    public static final int ATTR_FIXED = 1;
    public static final int ATTR_2_SIDES = 2;
    public static final int ATTR_TRANSPARENT = 4;
    public static final int ATTR_TEXTURED = 16;
    public static final int ATTR_SHADE_CONST = 32;
    public static final int ATTR_SHADE_FLAT = 64;
    public static final int ATTR_SHADE_GOURAD = 128;
    public static final int ATTR_SHADE_FONG = 256;
    
    protected States state;
    protected Types type;
    protected int attributes;
    protected int size;
    protected Color[] colors;    // source/shade flat color[0], or verts gourad colors
    protected float transp;
    
    protected Vector3D normal;
    protected double normalLength;
    protected double averageZ;
    
    protected int textureId;
    protected Point2D[] texturePoints;
    protected int materialId;

    /////////////////////////////////////////////////////////
    public Polygon3D(int size, float transp, int attr, Color src) {
	this.size = size;
        this.transp = transp;
	this.attributes = attr;
        this.colors = new Color[size];
        this.colors[0] = src;
	this.state = States.VISIBLE;
        this.type = type(size);
        this.normal = new Vector3D();
    }
    
    /////////////////////////////////////////////////////////
    public Polygon3D(int size, float transp, int attr, Color src, int textId, Point2D[] textPoints) {
	this.size = size;
        this.transp = transp;
	this.attributes = attr;
        this.textureId = textId;
        this.texturePoints = textPoints;
        this.colors = new Color[size];
        colors[0] = src;
	this.state = States.VISIBLE;
        this.type = type(size);
        this.normal = new Vector3D();
    }
    
    /////////////////////////////////////////////////////////
    public static Types type(int size) {
	switch (size) {
	    case 1: return Types.POINT;
	    case 2: return Types.LINE;
	    case 3: return Types.TRIANGLE;
	    default: return Types.POLYGON;
	}
    }
    
    /////////////////////////////////////////////////////////
    public static final Vector3D normal(Point3D v0, Point3D v1, Point3D v2) {
	Vector3D u = new Vector3D(v0, v1);
	Vector3D v = new Vector3D(v0, v2);
	return Vector3D.mul(u, v);
    }

    /////////////////////////////////////////////////////////
    // set
    public final void setAttribute(int attr) {
	attributes |= attr;
    }
    
    public final void setAttributes(int attr) {
	attributes = attr;
    }
    
    public final void unsetAttribute(int attr) {
	attributes &= ~attr;
    }
    
    public void setState(States state) {
        this.state = state;
    }

    public void setColor(Color col) {
	this.colors[0] = col;
    }

    public void setColor(int r, int g, int b) {
	this.colors[0] = new Color(r, g, b);
    }
        
    public void setColor(Color col, int i) {
	this.colors[i] = col;
    }
    
    public void setColor(int r, int g, int b, int i) {
	this.colors[i] = new Color(r, g, b);
    }
    
    public void setNormal(Vector3D n) {
        this.normal = n;
    }
    
    public void setTransparent(float transp) {
        this.transp = transp;
    }
    
    /////////////////////////////////////////////////////////
    // get
    public Types getType() {
	return type;
    }

    public int getAttributes() {
	return attributes;
    }

    public boolean isSetAttribute(int attr) {
	return (attributes & attr) != 0;
    }
    

    public static boolean isSetAttribute(int attributes, int attr) {
	return (attributes & attr) != 0;
    }
    
    public int getSize() {
	return size;
    }
    
    public States getState() {
	return state;
    }

    public Color getColor() {
//        if (isSetAttribute(Polygon3D.ATTR_TEXTURED))
//            return SRC_COLOR;
        return colors[0];
    }

    public Color[] getColors() {
        return colors;
    }

    public Color getColor(int i) {
//        if (isSetAttribute(Polygon3D.ATTR_TEXTURED))
//            return SRC_COLOR;
        return colors[i];
    }

    public int getMaterialId() {
        return materialId;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public double getNormalLength() {
        return normalLength;
    }

    public double getAverageZ() {
        return averageZ;
    }

    public int getTextureId() {
        return textureId;
    }

    public Point2D[] getTexturePoints() {
        return texturePoints;
    }

    /*public Polygon3D getCopy() {
	return new Polygon3D(size, srcColor, shadeColor, borderColor, attributes);
    }*/
    
    
}