package com.bondar.geom;

import com.bondar.gm.Camera;
import java.awt.Color;

/**
 * Polygon with indexes for vertexes.
 * @author truebondar
 */
public class Polygon3D /*implements Comparable<Polygon3D>*/ {

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
    
    public static final int ATTR_2_SIDES = 1;
    public static final int ATTR_TRANSPARENT = 2;
    
    protected States state;
    protected Types type;
    protected int attributes;
    protected Point3D[] vertexes;
    protected int indexes[];
    protected int size;
    protected Color srcColor;
    protected Color borderColor;
    protected Color shadeColor;

    /////////////////////////////////////////////////////////
    public Polygon3D(Point3D[] verts, int[] inds, Color fill, Color border, int attr) {
	this.vertexes = verts;
	this.indexes = inds;
	this.srcColor = fill;
	this.shadeColor = fill;
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
	if (cam == null) return true;
	if (isSetAttribute(ATTR_2_SIDES)
		|| type == Types.LINE
		|| type == Types.POINT) return false;
	// build normal
	/*Vector3D u = new Vector3D(getVertex(0), getVertex(1));
	Vector3D v = new Vector3D(getVertex(0), getVertex(2));
	Vector3D n = Vector3D.mul(u, v);
	Vector3D n = normal(getVertex(0), getVertex(1), getVertex(2));
	// build vector from object to camera
	Vector3D view = new Vector3D(getVertex(0), cam.getPosition());
	// scalar multiply
	double res = Vector3D.dot(n, view);
	return (res <= 0.0);*/
	//
	return !isPointInHalfspace(this, cam.getPosition());
    }

    public static boolean isPointInHalfspace(Polygon3D poly, Point3D p) {
	if (poly == null || p == null
		|| poly.getType() == Types.LINE
		|| poly.getType() == Types.POINT) return false;
	// build poly normal
	Vector3D n = normal(poly.getVertex(0), poly.getVertex(1), poly.getVertex(2));
	// build vector from poly to target point
	Vector3D view = new Vector3D(poly.getVertex(0), p);
	// scalar multiply
	double res = Vector3D.dot(n, view);
	return (res > 0.0);
    }
     
    public static Vector3D normal(Point3D v0, Point3D v1, Point3D v2) {
	Vector3D u = new Vector3D(v0, v1);
	Vector3D v = new Vector3D(v0, v2);
	return Vector3D.mul(u, v);
    }
 
    /*@Override
    public int compareTo(Polygon3D t) {
	return 0;
    }*/

    public double averageZ() {
	double sumZ = 0;
	for (Point3D v : getVertexes()) {
	    sumZ += v.getZ();
	}
	return sumZ / size;
    }

    public double minZ() {
	double minZ = getVertex(0).getZ();
	for (Point3D v : getVertexes()) {
	    if (minZ > v.getZ())
		minZ = v.getZ();
	}
	return minZ;
    }
 
    public double maxZ() {
	double maxZ = getVertex(0).getZ();
	for (Point3D v : getVertexes()) {
	    if (maxZ < v.getZ())
		maxZ = v.getZ();
	}
	return maxZ;
    }

    public double zByXY(double x, double y) {
	return Triangle3D.zByXY(getVertex(0), getVertex(1), getVertex(2), x, y);
    }
	                      
    /////////////////////////////////////////////////////////
    // set
    public final void setAttribute(int attr) {
	attributes |= attr;
    }
    
    public void setIsBackFace(Camera cam) {
	if (isBackFace(cam))
	    state = States.BACKFACE;
	else state = States.VISIBLE;
    }
     
    public void setVertexes(Point3D[] verts) {
	this.vertexes = verts;
    }
     
    public void setLightColor(Color col) {
	this.shadeColor = col;
    }

    /////////////////////////////////////////////////////////
    // get
    public Types getType() {
	return type;
    }

    public Point3D getVertex(int i) {
	if (vertexes == null || indexes == null
		|| (i < 0 || i >= size) || (indexes[i] >= vertexes.length)) return null;
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
	return srcColor;
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
	return new Polygon3D(vertexes, indexes, srcColor, borderColor, attributes);
    }
}
