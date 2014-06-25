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
    private BoundingBox3D bounds;
    
    private Point3D angles;
    private Point3D pos;
    private Point3D scale;
    private boolean isNeedPerspective = false;
    private double dist, ro, theta, phi;
    
    private boolean isVisible;
    private double maxRadius;

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
	bounds = new BoundingBox3D();
	worldVerts = localVerts;
	angles = new Point3D();
	pos = new Point3D();
	scale = new Point3D(1,1,1);
	edgesColor = null;
	
	maxRadius = defineMaxRadius(localVerts);
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
	if (indsToTrias == null) return null;
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

    public void resetTrianglesVertexes() {
	if (triangles == null) return;
	for (int i = 0; i < triangles.length; i++) {
	    triangles[i].setVertexes(worldVerts);
	}
    }

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

	return trias;//res.toArray3(new Triangle3D[res.size()]);
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
    public Point3D[] makeTransfer(CameraEuler cam) {
	if (cam == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.getRotationMatrix(angles.getX(), AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(angles.getY(), AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(angles.getZ(), AXIS.Z);
	Matrix transM = Matrix.getTransferMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.getScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	Matrix perspM = Matrix.getPerspectiveMatrix(dist);
	Matrix camM = cam.builtMatrix(Camera.CAM_ROT_SEQ_ZYX);
	
	int size = localVerts.length;
	Point3D[] res = new Point3D[size];
	// transform all vertexes
	for (int i = 0; i < size; i++) {
	    Point3DOdn v = localVerts[i].toPoint3DOdn();
	    v.mul(rotateXM).mul(rotateYM).mul(rotateZM).mul(transM).mul(scaleM).mul(camM);
	    if (isNeedPerspective) {
		v.mul(viewM).mul(perspM);
	    }
	    v.normalizeByW();
	    res[i] = (Point3D)v;
	}
	return res;
    }
    
    public Point3D makeTransfer(Point3D v, CameraEuler cam) {
	if (v == null || cam == null) return null;
	// create matrixes
	Matrix rotateXM = Matrix.getRotationMatrix(angles.getX(), AXIS.X);
	Matrix rotateYM = Matrix.getRotationMatrix(angles.getY(), AXIS.Y);
	Matrix rotateZM = Matrix.getRotationMatrix(angles.getZ(), AXIS.Z);
	Matrix transM = Matrix.getTransferMatrix(pos.getX(), pos.getY(), pos.getZ());
	Matrix scaleM = Matrix.getScaleMatrix(scale.getX(), scale.getY(), scale.getZ());
	Matrix viewM = Matrix.getViewMatrix(ro, theta, phi);
	Matrix perspM = Matrix.getPerspectiveMatrix(dist);
	Matrix camM = cam.builtMatrix(Camera.CAM_ROT_SEQ_ZYX);
	// transform vertex
	Point3DOdn res = v.toPoint3DOdn();
	res.mul(rotateXM).mul(rotateYM).mul(rotateZM).mul(transM).mul(scaleM).mul(camM);
	if (isNeedPerspective) {
	    res.mul(viewM).mul(perspM);
	}
	res.normalizeByW();
	return (Point3D)res;
    }

    /////////////////////////////////////////////////////////
    //
    public static double defineMaxRadius(Point3D[] verts) {
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

    // Cull solid, if it's fully out of clip bounds.
    public boolean isNeedCull(CameraEuler cam) {
	Point3D spherePos = makeTransfer(pos, cam);
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
    // set
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
    
    public void setEdgesColor(Color col) {
	edgesColor = col;
    }
    
    public void setBounds(Point3D[] verts) {
	bounds.setBounds(verts);
    }
    
    public void setVisible(boolean vis) {
	isVisible = vis;
    }
    
    /*public void setVisible(CameraEuler cam) {
	isVisible = !isNeedCull(cam);
    }*/

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
    
    public Point3D getWorldVertex(int i) {
	if (i >= worldVerts.length || i < 0)
	    return null;
	return worldVerts[i];
    }
    
    public Point3D[] getLocalVertexes() {
	return localVerts;
    }
    
    public Point3D[] getWorldVertexes() {
	return worldVerts;
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
	return edgesColor;
    }
    
    public boolean isPerspective() {
	return isNeedPerspective;
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

    public boolean isVisible() {
	return isVisible;
    }
}
