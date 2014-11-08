package com.bondar.geom;

import com.bondar.gm.Camera;
import com.bondar.gm.Matrix;
import java.awt.Color;
import java.util.Arrays;

/**
 * All-sufficient polygon (without indexes for vertexes).
 * @author truebondar
 */
public class Polygon3D {

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
    protected Vertex3D[] vertexes;
    protected int size;
    protected Color srcColor;
    protected Color borderColor;
    protected Color shadeColor;
    protected Color[] shadeVertsColors;
    //protected Bitmap texture;
    protected int materialId;
    protected Vector3D normal;
    protected double normalLength;
    protected double averageZ;

    /////////////////////////////////////////////////////////
    public Polygon3D(Vertex3D[] verts, Color src, Color shade, Color border, int attr) {
	this.vertexes = Arrays.copyOf(verts, verts.length);
	this.srcColor = src;
	this.shadeColor = shade;
        this.borderColor = border;
	this.attributes = attr;
	this.state = States.VISIBLE;
	this.size = verts.length;
        this.type = type(size);
	shadeVertsColors = null;
    }
    public Polygon3D(Point3D[] verts, Color src, Color shade, Color border, int attr) {
	this.vertexes = new Vertex3D[verts.length];
        for (int i = 0; i < verts.length; i++) {
            vertexes[i] = new Vertex3D(verts[i].getCopy());
        }
	this.srcColor = src;
	this.shadeColor = shade;
        this.borderColor = border;
	this.attributes = attr;
	this.state = States.VISIBLE;
	this.size = verts.length;
        this.type = type(size);
	shadeVertsColors = null;
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
    public boolean isBackFace(Camera cam) {
	if (cam == null) return true;
	if (isSetAttribute(ATTR_2_SIDES)
		|| type == Types.LINE
		|| type == Types.POINT) return false;
        Point3D p = cam.getPosition();
	return !isPointInHalfspace(p);
    }
    
    public boolean isPointInHalfspace(Point3D p) {
	if (p == null || type == Types.LINE
		|| type == Types.POINT) return false;
	Vector3D v = new Vector3D(vertexes[1].getPosition(), p);
	double res = Vector3D.dot(normal, v);
	return (res > 0.0); 
    }

    public static boolean isPointInHalfspace(Polygon3D poly, Point3D p) {
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
    }

    public static Vector3D normal(Point3D v0, Point3D v1, Point3D v2) {
	Vector3D u = new Vector3D(v0, v1);
	Vector3D v = new Vector3D(v0, v2);
	return Vector3D.mul(u, v);
    }

    public double averageZ() {
	double sumZ = 0;
	for (Vertex3D v : vertexes) {
	    sumZ += v.getPosition().getZ();
	}
	return sumZ / size;
    }

    public double minZ() {
	double minZ = vertexes[0].getPosition().getZ();
	for (Vertex3D v : getVertexes()) {
	    if (minZ > v.getPosition().getZ())
		minZ = v.getPosition().getZ();
	}
	return minZ;
    }
 
    public double maxZ() {
	double maxZ = vertexes[0].getPosition().getZ();
	for (Vertex3D v : getVertexes()) {
	    if (maxZ < v.getPosition().getZ())
		maxZ = v.getPosition().getZ();
	}
	return maxZ;
    }

    public double zByXY(double x, double y) {
 	if (type == Types.LINE || type == Types.POINT) return 0;
        return Triangle3D.zByXY(
                vertexes[0].getPosition(), 
                vertexes[1].getPosition(), 
                vertexes[2].getPosition(), x, y);
    }
	                      
    /////////////////////////////////////////////////////////
    // set
    public final void setAttribute(int attr) {
	attributes |= attr;
    }
    
    public final void unsetAttribute(int attr) {
	attributes &= ~attr;
    }     
    
    public void setIsBackFace(Camera cam) {
	if (isBackFace(cam))
	    state = States.BACKFACE;
	else state = States.VISIBLE;
    }
    
    public void setVertexes(Vertex3D[] verts) {
	this.vertexes = verts;
    }
    
    public void setVertexesPosition(Point3D[] points) {
        if (points == null) return;
        for (int i = 0; i < vertexes.length; i++) {
            vertexes[i].setPosition(points[i]);
        }
    }
     
    public void setShadeColor(Color col) {
	this.shadeColor = col;
    }
    
    public void setState(States state) {
        this.state = state;
    }
    
    public void resetAverageZ() {
        averageZ = averageZ();
    }
    
    public void resetNormal() {
 	if (type == Types.LINE || type == Types.POINT) return;
        normal = normal(
                vertexes[0].getPosition(), 
                vertexes[1].getPosition(), 
                vertexes[2].getPosition());
    }
      
    /////////////////////////////////////////////////////////
    // get
    public Types getType() {
	return type;
    }

    public Point3D getVertexPosition(int i) {
	if (vertexes == null || i < 0 || i >= size) return null;
	return vertexes[i].getPosition();
    }

    public Vertex3D[] getVertexes() {
	return vertexes;
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

    public Color[] getShadeVertsColors() {
        return shadeVertsColors;
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

    public Polygon3D getCopy() {
	return new Polygon3D(vertexes, srcColor, shadeColor, borderColor, attributes);
    }
    
    public void transToPerspectAndScreen(Camera cam) {
	if (cam == null) return;
	// create matrixes
	Matrix perspM = Matrix.perspectMatrix(cam.getViewDist(), cam.getAspectRatio());
	Matrix scrM = Matrix.perspectToScreenMatrix(
		cam.getViewPort().getWidth(), cam.getViewPort().getHeight());
	Matrix[] ms = new Matrix[] {perspM,scrM};
	// transform all camera vertexes
        for (Vertex3D v : vertexes) {
            transVertex(v, ms);
            Point3D p = v.getPosition();
                double[] d = new double[] {p.getX(), p.getY(), p.getZ()};
            System.out.println(String.format("[%f],[%f],[%f]",p.getX(), p.getY(), p.getZ()));
        }
    }
    
    public static void transVertex(Vertex3D v, Matrix[] ms) {
	if (v == null || ms == null) return;
        Point3D p = v.getPosition();
        double[] d = new double[] {p.getX(), p.getY(), p.getZ()};
	Point3DOdn res = p.toPoint3DOdn();
	for (Matrix m : ms) {
	    res.mul(m);
	}
	v.setPosition(res.divByW());
    }
}