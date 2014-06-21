package com.bondar.gm;

import com.bondar.gm.Matrix.AXIS;
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

    private Point3D[] localVerts;
    private Point3D[] worldVerts;
    private int[][] indsToTrias;
    private Triangle3D[] triangles = null;
    private DrawOrMove[] doms;
    private String name;
    private Color edgesColor;
    private BoundingBox bounds;
    
    private Point3D angles;
    private Point3D trans;
    private Point3D scale;
    private boolean isNeedPerspective = false;
    private double dist, ro, theta, phi;

    /////////////////////////////////////////////////////////
    public Solid3D(String name, Point3D[] vertexes, int[][] indsToTrias, Color[] colors, DrawOrMove[] doms) {
	this.name = name;
	this.localVerts = vertexes;
	this.indsToTrias = indsToTrias;
	this.triangles = createTriangles(vertexes, indsToTrias, colors);
	this.doms = doms;
	reInit();
    }
    
    public Solid3D(String name, Point3D[] vertexes, int[][] indsToTrias, DrawOrMove[] doms) {
	this.name = name;
	this.localVerts = vertexes;
	this.indsToTrias = indsToTrias;
	this.triangles = createTriangles(vertexes, indsToTrias);
	this.doms = doms;
	reInit();
    }

    public Solid3D(String name, Point3D[] vertexes, int[][] indsToTrias, Color[] colors) {
	this.name = name;
	this.localVerts = vertexes;
	this.indsToTrias = indsToTrias;
	this.triangles = createTriangles(vertexes, indsToTrias, colors);
	this.doms = createDrawOrMoves(vertexes);
	reInit();
    }

    public Solid3D(String name, Point3D[] vertexes) {
	this.name = name;
	this.localVerts = vertexes;
	this.indsToTrias = createIndsForTrias(vertexes);
	this.triangles = createTriangles(vertexes, indsToTrias);
	this.doms = createDrawOrMoves(vertexes);
	reInit();
    }
    
    private void reInit() {
	this.bounds = new BoundingBox();
	this.worldVerts = this.localVerts;
	angles = new Point3D();
	trans = new Point3D();
	scale = new Point3D(1,1,1);
    }

    /////////////////////////////////////////////////////////
    // triangulation
    public static int[][] createIndsForTrias(Point3D[] vertexes) {
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
   
    private static Triangle3D[] createTriangles(Point3D[] vertexes, int[][] indsToTrias) {
	int size = indsToTrias.length;
	Color[] colors = new Color[size];
	Random rand = new Random();
	for (int i = 0; i < size; i++) {
	    colors[i] = new Color(
		    rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
	}
	return createTriangles(vertexes, indsToTrias, colors);
    }
    
    private static Triangle3D[] createTriangles(Point3D[] vertexes, int[][] indsToTrias, Color[] colors) {
	if (vertexes == null || indsToTrias == null || colors == null) return null;
	int size = indsToTrias.length;
	Triangle3D[] res = new Triangle3D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = new Triangle3D(
		    vertexes,
		    indsToTrias[i][0],
		    indsToTrias[i][1],
		    indsToTrias[i][2],
		    colors[i]);
	}
	return res;
    }
    
    /*public static Triangle3D[] createTriasFromVerts(Point3D[] vertexes, int[][] indsToTrias, Triangle3D[] oldTrias) {
	if (vertexes == null || indsToTrias == null) return null;
	int size = indsToTrias.length;
	Triangle3D[] res = new Triangle3D[size];
	for (int i = 0; i < size; i++) {
	    Color color = (oldTrias != null && oldTrias.length >= size) ? 
		    oldTrias[i].getColor() : Color.BLACK;
	    res[i] = new Triangle3D(
		    vertexes,
		    indsToTrias[i][0],
		    indsToTrias[i][1],
		    indsToTrias[i][2],
		    color);
	}
	return res;
    }*/
    /*
    public Triangle3D[] setTrianglesVertexes(Point3D[] vertexes) {
	if (vertexes == null) return null;
	for (int i = 0; i < indsToTrias.length; i++) {
	    triangles[i].setTriangle(
		    vertexes[indsToTrias[i][0]],
		    vertexes[indsToTrias[i][1]],
		    vertexes[indsToTrias[i][2]]);
	}
	return triangles;
    }
*/
    /////////////////////////////////////////////////////////
    // 
    public static DrawOrMove[] createDrawOrMoves(Point3D[] points) {
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
    }

    /////////////////////////////////////////////////////////
    // clipping
    /*public Triangle3D[] getVisibleTriangles(Point3DOdn camera) {
	return Solid3D.getVisibleTriangles(triangles, camera);
    }*/

    /*public static Triangle3D[] getVisibleTriangles(Triangle3D[] trias, Point3D camera) {
	if (trias == null || camera == null) {
	    return null;
	}
	//
	List<Triangle3D> res = new ArrayList();

	// отбор видимых граней

	return trias;//res.toArray(new Triangle3D[res.size()]);
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
	angles.add(new Point3D(ax, ay, az));
    }
    
    public void updateAngles(double ax, double ay, double az) {
	angles.add(new Point3D(ax, ay, az));
    }
    
    public void updateTransfers(double dx, double dy, double dz) {
	trans.add(new Point3D(dx, dy, dz));
    }
    
    public void updateScale(double s) {
	scale.add(new Point3D(s, s, s));
    }
    
    public void updateScale(double sx, double sy, double sz) {
	scale.add(new Point3D(sx, sy, sz));
    }
    
    public void setPerspective(boolean isNeedPerspective) {
	this.isNeedPerspective = isNeedPerspective;
    }
    
    public void setPerspective(double d, double ro, double theta, double phi) {
	this.dist += d;
	this.ro += ro;
	this.theta = theta;
	this.phi += phi;
    }
    
    public void setWorldVertexes(Point3D[] verts) {
	worldVerts = verts;
    }
    
    /////////////////////////////////////////////////////////
    // 
    public Point3D[] makeTransformations() {
	// create matrixes
	Matrix rotateXM = Matrix.getRotationMatrix(angles.getX(), AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(angles.getY(), AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(angles.getZ(), AXIS.Z);
	Matrix transM = Matrix.getTransferMatrix(trans.getX(), trans.getY(), trans.getZ());
	Matrix scaleM = Matrix.getScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	Matrix perspM = Matrix.getPerspectiveMatrix(dist);
	
	int size = localVerts.length;
	Point3D[] res = new Point3D[size];
	// transform all vertexes
	for (int i = 0; i < size; i++) {
	    Point3DOdn v = localVerts[i].toPoint3DOdn();
	    v.mul(rotateXM).mul(rotateYM).mul(rotateZM).mul(transM).mul(scaleM);
	    // perspective
	    if (isNeedPerspective) {
		v.mul(viewM).mul(perspM);
	    }
	    v.normalizeByW();
	    res[i] = (Point3D)v;
	}
	return res;
    }
    
    /////////////////////////////////////////////////////////
    // set
    public void setEdgesColor(Color col) {
	edgesColor = col;
    }
    
    public void setBounds(Point3D[] verts) {
	bounds.setBounds(verts);
    }

    /////////////////////////////////////////////////////////
    // get
    public int getSize() {
	return localVerts.length;
    }

    public Point3D getVertex(int i) {
	if (i >= localVerts.length || i < 0) {
	    return null;
	}
	return localVerts[i];
    }

    public Point3D[] getLocalVertexes() {
	return localVerts;
    }

    public static Point2D[] getVertexes2D(Point3D[] vertexes) {
	int size = vertexes.length;
	Point2D[] res = new Point2D[size];
	for (int i = 0; i < size; i++) {
	    res[i] = vertexes[i].toPoint2D();
	}
	return res;
    }

    public Triangle3D[] getTriangles() {
	return triangles;
    }
    
    public DrawOrMove[] getDoms() {
	return doms;
    }
    
    public int[][] getIndsToTrias() {
	return indsToTrias;
    }
    
    public String getName() {
	return name;
    }
    
    public Color getEdgesColor() {
	return (edgesColor != null) ? edgesColor : Color.BLACK;
    }
    
    public boolean isPerspective() {
	return isNeedPerspective;
    }
    
    public BoundingBox getBounds() {
	return bounds;
    }
    
    public static Point3D getCenter(BoundingBox bb) {
	if (bb == null) return null;
	double cx = (bb.getX().getMin() + bb.getX().getMax()) / 2;
	double cy = (bb.getY().getMin() + bb.getY().getMax()) / 2;
	double cz = (bb.getZ().getMin() + bb.getZ().getMax()) / 2;
	return new Point3D(cx, cy, cz);
    }
    
    public boolean isIntersect(BoundingBox bb) {
	return bounds.isIntersect(bb);
    }
    
    public Point3D[] getWorldVertexes() {
	return worldVerts;
    }
}
