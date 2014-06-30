package com.bondar.geom;

import com.bondar.gm.Camera;
import java.awt.Color;

/**
 * Polygon with indexes for vertexes.
 * @author truebondar
 */
public class Polygon3D {
    
    public static enum States {
	VISIBLE,
	INVISIBLE
    }
    public static enum Types {
	POLYGON,
	TRIANGLE,
	LINE,
	POINT
    }
    
    public static final int ATTR_TWO_SIDES = 1;
    
    protected States state;
    protected Types type;
    protected int attributes;
    protected Point3D[] vertexes;
    protected int indexes[];
    protected int size;
    protected Color fillColor;
    protected Color borderColor;

    /////////////////////////////////////////////////////////
    public Polygon3D(Point3D[] verts, int[] inds, Color fill, Color border, int attr) {
	this.vertexes = verts;
	this.indexes = inds;
	this.fillColor = fill;
	this.borderColor = border;
	this.attributes = attr;
	if (inds == null) return;
	this.size = inds.length;
	this.state = States.VISIBLE;
	switch (size) {
	    case 1: type = Types.POINT;
		break;
	    case 2: type = Types.LINE;
		break;
	    case 3: type = Types.TRIANGLE;
		break;
	    default: type = Types.POLYGON;
	}
    }
    
    /////////////////////////////////////////////////////////
    //
    public boolean isBackFace(Camera cam) {
	if (isSetAttribute(ATTR_TWO_SIDES)
		|| type == Types.LINE
		|| type == Types.POINT
		|| cam == null) return false;
	// build normal
	Vector3D u = new Vector3D(getVertex(0), getVertex(1));
	Vector3D v = new Vector3D(getVertex(0), getVertex(2));
	Vector3D n = Vector3D.mul(u, v);
	// build vector from object to camera
	Vector3D view = new Vector3D(getVertex(0), cam.getPosition());
	// scalar multiply
	double res = Vector3D.dot(n, view);
	return (res <= 0.0);
    }
    
    /////////////////////////////////////////////////////////
    // set
    public final void setAttribute(int attr) {
	attributes |= attr;
    }
    
    public void setIsBackFace(Camera cam) {
	if (isBackFace(cam))
	    state = States.INVISIBLE;
	else state = States.VISIBLE;
    }
     
    public void setVertexes(Point3D[] verts) {
	this.vertexes = verts;
    }

    /////////////////////////////////////////////////////////
    // get
    public Types getType() {
	return type;
    }
    
    public double getAverageZ() {
	double s = 0;
	for (Point3D v : getVertexes()) {
	    s += v.getZ();
	}
	return s / size;
    }
    
    public double getZbyXY(double x, double y) {
	return Triangle3D.getZbyXY(getVertex(0), getVertex(1), getVertex(2), x, y);
    }
	
    public Point3D getVertex(int i) {
	if (vertexes == null || indexes == null
		|| (i < 0 || i >= size) || (indexes[i] > vertexes.length)) return null;
	return vertexes[indexes[i]];
    }
    
    public boolean isSetAttribute(int attr) {
	return (attributes & attr) != 0;
    }
    
    public int[] getIndexes() {
	return indexes;
    }
    
    public int getSize() {
	return size;
    }
    
    public Color getFillColor() {
	return fillColor;
    }
     
    public Color getBorderColor() {
	return borderColor;
    }

    public Point3D[] getVertexes() {
	if (vertexes == null) return null;
	Point3D[] res = new Point3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[indexes[i]];
	}
	return res;
    }
    
    public States getState() {
	return state;
    }
    
    public int getAttributes() {
	return attributes;
    }
    
    public Polygon3D getCopy() {
	return new Polygon3D(vertexes, indexes, fillColor, borderColor, attributes);
    }
}
