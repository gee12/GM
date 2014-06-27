package com.bondar.gm;

import java.awt.Color;

/**
 * Polygon with indexes for vertexes.
 * @author truebondar
 */
public class Polygon3D {
    
    public enum States {
	VISIBLE,
	BACKFACE
    }
    public static final int TWO_SIDES = 0;
    public static final int LESS_3_VERTS = 1;
    
    private States state;
    protected int attribute;
    protected Point3D[] vertexes;
    protected int indexes[];
    protected int size;
    protected Color color;

    /////////////////////////////////////////////////////////
    public Polygon3D(Point3D[] verts, int[] inds, Color color) {
	this.vertexes = verts;
	this.indexes = inds;
	this.color = color;
	if (inds != null) {
	    this.size = inds.length;
	    this.state = States.VISIBLE;
	}
	if (this.size < 3)
	    setAttribute(LESS_3_VERTS);
    }
    
    /////////////////////////////////////////////////////////
    //
    public boolean isBackFace(CameraEuler cam) {
	//if (state == States.BACKFACE) return true;
	if (isSetAttribute(TWO_SIDES)
		|| isSetAttribute(LESS_3_VERTS)
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
	attribute &= attr;
    }
    
    public void setIsBackFace(CameraEuler cam) {
	if (isBackFace(cam))
	    state = States.BACKFACE;
	else state = States.VISIBLE;
    }
     
    public void setVertexes(Point3D[] verts) {
	this.vertexes = verts;
    }

    /////////////////////////////////////////////////////////
    // get
    public Point3D getVertex(int i) {
	if (vertexes == null || indexes == null) return null;
	return vertexes[indexes[i]];
    }
    
    public boolean isSetAttribute(int attr) {
	return (attribute & attr) != 0;
    }
    
    public int[] getIndexes() {
	return indexes;
    }
    
    public int getSize() {
	return size;
    }
    
    public Color getColor() {
	return color;
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
}
