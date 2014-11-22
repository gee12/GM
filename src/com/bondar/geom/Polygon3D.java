package com.bondar.geom;

import com.bondar.gm.Camera;
import java.awt.Color;

/**
 * 
 * @author truebondar
 */
public abstract class Polygon3D {

    public static enum States {
	VISIBLE,
	BACKFACE
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
    
    protected States state;
    protected Types type;
    protected int attributes;
    protected int size;
    protected Color[] colors;    // source/shade flat color[0], or verts gourad colors
    protected Color borderColor;
    //protected Bitmap texture;
    protected int materialId;
    protected Vector3D normal;
    protected double normalLength;
    protected double averageZ;

    /////////////////////////////////////////////////////////
    public Polygon3D(int size, Color src, Color border, int attr) {
	this.size = size;
//	this.color = src;
        this.colors = new Color[size];
        colors[0] = src;
//        colors[1] = src;
//        colors[2] = src;
        this.borderColor = border;
	this.attributes = attr;
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
    
    public final void unsetAttribute(int attr) {
	attributes &= ~attr;
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
    
    public void setState(States state) {
        this.state = state;
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
    
    public int getSize() {
	return size;
    }
    
    public Color getBorderColor() {
	return borderColor;
    }
    
    public States getState() {
	return state;
    }

    public Color getColor() {
        return colors[0];
    }

    public Color[] getColors() {
        return colors;
    }

    public Color getColor(int i) {
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

    /*public Polygon3D getCopy() {
	return new Polygon3D(size, srcColor, shadeColor, borderColor, attributes);
    }*/
    
    
}