package com.bondar.geom;

import com.bondar.gm.Camera;
import java.awt.Color;

/**
 * All-sufficient polygon (without indexes for vertexes).
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
    //private Vertex3D[] vertexes;
    protected int size;
    protected Color srcColor;
    protected Color borderColor;
    protected Color shadeColor;
    //protected Color[] shadeVertsColors;
    //protected Bitmap texture;
    protected int materialId;
    protected Vector3D normal;
    protected double normalLength;
    protected double averageZ;

    /////////////////////////////////////////////////////////
    public Polygon3D(int size, Color src, Color shade, Color border, int attr) {
	//this.vertexes = Arrays.copyOf(verts, verts.length);
	this.size = size;
	this.srcColor = src;
	this.shadeColor = shade;
        this.borderColor = border;
	this.attributes = attr;
	this.state = States.VISIBLE;
        this.type = type(size);
	//shadeVertsColors = null;
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
    
    //
    /*public boolean isPointInHalfspace(Vertex3D verts, Point3D p) {
	if (verts == null || p == null
		|| type == Types.LINE
		|| type == Types.POINT) return false;
	// build poly normal
	Vector3D n = normal(
                poly.getVertexPosition(0), 
                poly.getVertexPosition(1), 
                poly.getVertexPosition(2));
	// build vector from poly to target point
	Vector3D v = new Vector3D(poly.getVertexPosition(1), p);
	// scalar multiply
	double res = Vector3D.dot(n, v);
	return (res > 0.0);
    }*/
    
    /*public static boolean isPointInHalfspace(Polygon3D poly, Point3D p) {
	if (poly == null || p == null
		|| poly.getType() == Types.LINE
		|| poly.getType() == Types.POINT) return false;
	// build poly normal
	Vector3D n = normal(
                poly.getVertexPosition(0), 
                poly.getVertexPosition(1), 
                poly.getVertexPosition(2));
	// build vector from poly to target point
	Vector3D v = new Vector3D(poly.getVertexPosition(1), p);
	// scalar multiply
	double res = Vector3D.dot(n, v);
	return (res > 0.0);
    }*/

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

    public void setShadeColor(Color col) {
	this.shadeColor = col;
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

    public Color getSrcColor() {
        return srcColor;
    }

    public Color getShadeColor() {
        return shadeColor;
    }

    /*public Color[] getShadeVertsColors() {
        return shadeVertsColors;
    }*/

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