package com.bondar.gm;

import static com.bondar.gm.Matrix.AXIS.X;
import static com.bondar.gm.Matrix.AXIS.Y;
import static com.bondar.gm.Matrix.AXIS.Z;
import java.awt.Color;
import java.util.Random;

/**
 *
 * @author truebondar
 */
public class Solid3D {
    
    public enum States {
	VISIBLE,
	CULLED
    }
    
    public static final int ATTR_FIXED = 1;
    
    private States state;
    private int attribute;
    private Point3D[] localVerts;
    private Point3D[] transVerts;
    //private int[][] indexes;
    private Polygon3D[] polygons = null;
    private String name;
    private BoundingBox3D bounds;
    
    private Point3D dir;
    private Point3D pos;
    private Point3D scale;
    private boolean isNeedPerspective = false;
    private double maxRadius;

    /////////////////////////////////////////////////////////
    public Solid3D(String name, Point3D[] vertexes, Polygon3D[] polies) {
	this.name = name;
	this.localVerts = vertexes;
	//this.indexes = indsToTrias;
	this.polygons = polies;
	reInit();
    }
    
    public Solid3D(String name, Point3D[] vertexes, int[][] indsToTrias, Color[] fills, Color[] borders, int attr) {
	this.name = name;
	this.localVerts = vertexes;
	//this.indexes = indsToTrias;
	this.polygons = buildPolygons(vertexes, indsToTrias, fills, borders, attr);
	reInit();
    }

    public Solid3D(String name, Point3D[] vertexes) {
	this.name = name;
	this.localVerts = vertexes;
	//this.indexes = buildIndexes(vertexes);
	this.polygons = buildPolygons(vertexes, buildIndexes(vertexes));
	reInit();
    }
    
    private void reInit() {
	state = States.VISIBLE;
	bounds = new BoundingBox3D();
	transVerts = localVerts;
	dir = new Point3D();
	pos = new Point3D();
	scale = new Point3D(1,1,1);
	maxRadius = maxRadius(localVerts);
    }

    /////////////////////////////////////////////////////////
    // triangulation
    public static int[][] buildIndexes(Point3D[] vertexes) {
	if (vertexes == null) return null;
	int n = vertexes.length;
	// combination from n to 3
	int triasCount = n*(n-1)*(n-2)/6;
	int[][] res = new int[triasCount][3];
	// simple triangulation (with trias inside polygon & intersecting trias)
	int ind = 0;
	for (int i = 0; i < n-2; i++) {
	    for (int j = i+1; j < n-1; j++) {
		for (int k = j+1; k < n; k++,ind++) {
		    res[ind] = new int[] {i,j,k};
		}
	    }
	}
	return res;
    }
   
    private static Polygon3D[] buildPolygons(Point3D[] vertexes, int[][] indsToTrias) {
	if (indsToTrias == null) return null;
	int size = indsToTrias.length;
	Color[] fills = new Color[size];
	Color[] borders = new Color[size];
	Random rand = new Random();
	for (int i = 0; i < size; i++) {
	    fills[i] = new Color(
		    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
	    borders[i] = new Color(
		    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
	}
	return buildPolygons(vertexes, indsToTrias, fills, borders, 0);
    }
    
    private static Polygon3D[] buildPolygons(Point3D[] vertexes, int[][] inds, Color[] fills, Color[] borders, int attr) {
	if (vertexes == null || inds == null || fills == null || borders == null) return null;
	int size = inds.length;
	Polygon3D[] res = new Polygon3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = new Polygon3D(
		    vertexes,
		    inds[i],
		    fills[i],
		    borders[i],
		    attr);
	}
	return res;
    }

    public void resetTrianglesVertexes(Point3D[] verts) {
	if (polygons == null) return;
	for (Polygon3D poly : polygons) {
	    poly.setVertexes(verts);
	}
    }

    /////////////////////////////////////////////////////////
    // 
    /*public static DrawOrMove[] createDrawOrMoves(Point3D[] points) {
	if (points == null) {
	    return null;
	}
	int size = points.length;
	int arraySize = size * (size - 1);
	DrawOrMove[] res = new DrawOrMove[arraySize];
	int ind = 0;
	for (int i = 0; i < size-1; i++) {
	    for (int j = i+1; j < size; j++) {
		res[ind] = new DrawOrMove(i, DrawOrMove.Operation.MOVE);
		res[ind+1] = new DrawOrMove(j, DrawOrMove.Operation.DRAW);
		ind += 2;
	    }
	}
	return res;
    }*/
 
    /////////////////////////////////////////////////////////
    // operations
    public void updateAngle(double a, Matrix.AXIS axis) {
	double ax = 0, ay = 0, az = 0;
	switch (axis) {
	    case X: ax = a;
		break;
	    case Y: ay = a;
		break;
	    case Z: az = a;
		break;
	}
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateAngles(double ax, double ay, double az) {
	dir.add(new Point3D(ax, ay, az));
    }
    
    public void updateTransfers(double dx, double dy, double dz) {
	pos.add(new Point3D(dx, dy, dz));
    }
    
    public void updateScale(double s) {
	scale.add(new Point3D(s, s, s));
    }
    
    public void updateScale(double sx, double sy, double sz) {
	scale.add(new Point3D(sx, sy, sz));
    }

    /////////////////////////////////////////////////////////
    //
    public static double maxRadius(Point3D[] verts) {
	double max = Double.MIN_VALUE;
	if (verts == null) return max;
	for (Point3D v : verts) {
	    double x = Math.abs(v.getX());
	    if (x > max) max = x;
	    double y = Math.abs(v.getY());
	    if (y > max) max = y;
	    double z = Math.abs(v.getZ());
	    if (z > max) max = z;
	}
	return max;
    }

    /////////////////////////////////////////////////////////
    // Cull solid, if it's fully out of clip bounds.
    public boolean isNeedCull(CameraEuler cam) {
	if (cam == null) return false;
	Point3D spherePos = Transfer.transToCamera(pos, cam);
	// by z
	if (((spherePos.getZ() - maxRadius) > cam.getClipBox().getFarClipZ()) ||
	    ((spherePos.getZ() + maxRadius) < cam.getClipBox().getNearClipZ())) {
	    return true;
	}
	// by x
	double zTest = 0.5 * cam.getViewPlane().getWidth() * spherePos.getZ() / cam.getViewDist();
	if (((spherePos.getX() - maxRadius) > zTest)  || // right side
	    ((spherePos.getX() + maxRadius) < -zTest) ) { // left side, note sign change
	    return true;
	}
	// by y
	zTest = 0.5 * cam.getViewPlane().getHeight()* spherePos.getZ() / cam.getViewDist();
	if (((spherePos.getY() - maxRadius) > zTest)  || // right side
	    ((spherePos.getY() + maxRadius) < -zTest) ) { // left side, note sign change
	    return true;
	}
	return false;
    }
    
    /////////////////////////////////////////////////////////
    // Define backfaces triangles.
    public void defineBackfaces(CameraEuler cam) {
	for (Polygon3D poly: polygons) {
	    poly.setIsBackFace(cam);
	}
    }
    
    /////////////////////////////////////////////////////////
    // set
    public final void setAttribute(int attr) {
	attribute |= attr;
    }

    public final void unsetAttribute(int attr) {
	attribute &= ~attr;
    }   
    
    public void setIsNeedCulling(CameraEuler cam) {
	if (isNeedCull(cam))
	    state = States.CULLED;
	else state = States.VISIBLE;
    }
    
    public void setPerspective(boolean isNeedPerspective) {
	this.isNeedPerspective = isNeedPerspective;
    }

    public void setTransVertexes(Point3D[] verts) {
	transVerts = verts;
    }

    public void setBounds(Point3D[] verts) {
	bounds.setBounds(verts);
    }

    /////////////////////////////////////////////////////////
    // get
    public int getSize() {
	return localVerts.length;
    }

    public Point3D getLocalVertex(int i) {
	if (i >= localVerts.length || i < 0)
	    return null;
	return localVerts[i];
    }
    
    public Point3D getTransVertex(int i) {
	if (i >= transVerts.length || i < 0)
	    return null;
	return transVerts[i];
    }
    
    public Point3D[] getLocalVertexes() {
	return localVerts;
    }
    
    public Point3D[] getTransVertexes() {
	return transVerts;
    }
    
    public static Point2D[] getVertexes2D(Point3D[] vertexes) {
	int size = vertexes.length;
	Point2D[] res = new Point2D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[i].toPoint2D();
	}
	return res;
    }

    public Polygon3D[] getPolygons() {
	return polygons;
    }
    
    public String getName() {
	return name;
    }
    
    public boolean isPerspective() {
	return isNeedPerspective;
    }
    
    public boolean isSetAttribute(int attr) {
	return (attribute & attr) != 0;
    }   
    
    public BoundingBox3D getBounds() {
	return bounds;
    }
    
    public static Point3D getCenter(BoundingBox3D bb) {
	if (bb == null) return null;
	double cx = (bb.getX().getMin() + bb.getX().getMax()) / 2;
	double cy = (bb.getY().getMin() + bb.getY().getMax()) / 2;
	double cz = (bb.getZ().getMin() + bb.getZ().getMax()) / 2;
	return new Point3D(cx, cy, cz);
    }
    
    public boolean isIntersect(BoundingBox3D bb) {
	return bounds.isIntersect(bb);
    }

    /*public boolean isVisible() {
	return isVisible;
    }*/
    
    public States getState() {
	return state;
    }
    
    public Point3D getDirection() {
	return dir;
    }

    public Point3D getPosition() {
	return pos;
    }

    public Point3D getScale() {
	return scale;
    }

    public boolean isNeedPerspective() {
	return isNeedPerspective;
    }
}
